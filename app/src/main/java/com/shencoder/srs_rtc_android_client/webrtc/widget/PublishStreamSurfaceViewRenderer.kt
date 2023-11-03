package com.shencoder.srs_rtc_android_client.webrtc.widget

import android.content.Context
import android.util.AttributeSet
import com.elvishew.xlog.XLog
import com.shencoder.srs_rtc_android_client.webrtc.util.WebRTCUtil
import com.shencoder.srs_rtc_android_client.webrtc.PeerConnectionObserver
import com.shencoder.srs_rtc_android_client.webrtc.constant.StreamType
import org.webrtc.*
import org.webrtc.PeerConnection.RTCConfiguration
import org.webrtc.RtpTransceiver.RtpTransceiverInit
import java.lang.RuntimeException

/**
 * 用于显示推流画面
 * @author  ShenBen
 * @date    2022/1/21 11:20
 * @email   714081644@qq.com
 */
class PublishStreamSurfaceViewRenderer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : BaseStreamSurfaceViewRenderer(context, attrs, defStyleAttr, defStyleRes) {

    private companion object {
        private const val DEFAULT_WIDTH = 640
        private const val DEFAULT_HEIGHT = 480
        private const val DEFAULT_FRAME_RATE = 25
    }

    private var cameraVideoCapturer: CameraVideoCapturer? = null
    private var videoTrack: VideoTrack? = null
    private var surfaceTextureHelper: SurfaceTextureHelper? = null

    private var captureWidth: Int = DEFAULT_WIDTH
    private var captureHeight: Int = DEFAULT_HEIGHT
    private var captureFrameRate: Int = DEFAULT_FRAME_RATE

    private var videoSource: VideoSource? = null
    private var videoProcessor: VideoProcessor? = null

    override fun streamType(): StreamType {
        return StreamType.PUBLISH
    }

    override fun createPeerConnection(
        peerConnectionFactory: PeerConnectionFactory,
        sharedContext: EglBase.Context
    ): PeerConnection {
        val configuration = RTCConfiguration(emptyList())
        //必须设置PeerConnection.SdpSemantics.UNIFIED_PLAN
        configuration.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
        val peerConnection =
            peerConnectionFactory.createPeerConnection(configuration,
                object : PeerConnectionObserver() {
                    /**
                     * 连接状态改变时调用
                     */
                    override fun onConnectionChange(newState: PeerConnection.PeerConnectionState) {
                        super.onConnectionChange(newState)
                        getConnectionChangeCallback()?.onPublishConnectionChange(
                            this@PublishStreamSurfaceViewRenderer,
                            newState
                        )
                    }
                })!!
        //创建AudioSource，音频源
        val audioSource =
            peerConnectionFactory.createAudioSource(WebRTCUtil.createAudioConstraints())
        //创建AudioTrack，音频轨
        val audioTrack = peerConnectionFactory.createAudioTrack("local_audio_track", audioSource)

        cameraVideoCapturer = createVideoCapture(context.applicationContext).apply {
            val videoSource = peerConnectionFactory.createVideoSource(
                isScreencast
            )
            //用于帧数据处理
            videoSource.setVideoProcessor(videoProcessor)

            videoTrack =
                peerConnectionFactory.createVideoTrack("local_video_track", videoSource).apply {
                    //这一步是为了将画面显示到SurfaceViewRenderer上
                    addVideoStream(this)
                }
            surfaceTextureHelper =
                SurfaceTextureHelper.create("surface_texture_thread", sharedContext)
            initialize(
                surfaceTextureHelper,
                context,
                videoSource.capturerObserver
            )
            //预览宽、高、帧率
            startCapture(captureWidth, captureHeight, captureFrameRate)
            this@PublishStreamSurfaceViewRenderer.videoSource = videoSource
        }

        //这一步也必须调用，设置音视频资源，模式设置为仅发送即可-RtpTransceiver.RtpTransceiverDirection.SEND_ONLY
        /**
         * tips: 调整peerConnection.addTransceiver调用顺序也会影响sdp中video、audio的顺序
         * @see WebRTCUtil.convertAnswerSdp
         * 或者srs版本升级到SRS/4.0.265以上
         */
        peerConnection.addTransceiver(
            audioTrack,
            RtpTransceiverInit(RtpTransceiver.RtpTransceiverDirection.SEND_ONLY)
        )
        peerConnection.addTransceiver(
            videoTrack,
            RtpTransceiverInit(RtpTransceiver.RtpTransceiverDirection.SEND_ONLY)
        )
        return peerConnection
    }

    override fun afterInit() {
        svr.setZOrderMediaOverlay(true)
    }

    override fun beginRelease() {
        videoTrack?.dispose()
        videoSource?.dispose()
        cameraVideoCapturer?.dispose()
        surfaceTextureHelper?.dispose()
    }

    /**
     * 设置Camera捕获帧数据相关参数
     * 最好在[init]之前调用
     *
     * @param width  宽度
     * @param height 高度
     * @param frameRate 帧率
     */
    fun setCameraCaptureFormat(width: Int, height: Int, frameRate: Int) {
        captureWidth = width
        captureHeight = height
        captureFrameRate = frameRate

        cameraVideoCapturer?.changeCaptureFormat(width, height, frameRate)
    }

    /**
     * 自行处理帧数据
     */
    fun setVideoProcessor(processor: VideoProcessor?) {
        videoProcessor = processor
        videoSource?.setVideoProcessor(processor)
    }

    /**
     * 开始推流
     */
    fun publishStream(
        onSuccess: () -> Unit = {},
        onFailure: (error: Throwable) -> Unit = {}
    ) {
        requestSrs({
            isShowPrompt(false)
            onSuccess.invoke()
        }, {
            onFailure.invoke(it)
            XLog.e("publishStream failure: ${it.message}")
        })
    }

    /**
     * 切换相机
     */
    fun switchCamera(
        switchDone: (isFrontCamera: Boolean) -> Unit,
        switchError: (errorDescription: String) -> Unit
    ) {
        cameraVideoCapturer?.switchCamera(object : CameraVideoCapturer.CameraSwitchHandler {

            override fun onCameraSwitchDone(isFrontCamera: Boolean) {
                switchDone.invoke(isFrontCamera)
            }

            override fun onCameraSwitchError(errorDescription: String) {
                switchError.invoke(errorDescription)
            }
        })
    }


    /**
     * 创建视频源
     *
     * @param context
     * @return Camera 视频源
     */
    private fun createVideoCapture(context: Context): CameraVideoCapturer {
        //优先使用Camera2
        val enumerator =
            if (Camera2Enumerator.isSupported(context)) Camera2Enumerator(context) else Camera1Enumerator()
        val deviceNames = enumerator.deviceNames
        //前置
        for (name in deviceNames) {
            if (enumerator.isFrontFacing(name)) {
                return enumerator.createCapturer(name, null)
            }
        }
        //后置
        for (name in deviceNames) {
            if (enumerator.isBackFacing(name)) {
                return enumerator.createCapturer(name, null)
            }
        }
        throw RuntimeException("No camera available.")
    }

}