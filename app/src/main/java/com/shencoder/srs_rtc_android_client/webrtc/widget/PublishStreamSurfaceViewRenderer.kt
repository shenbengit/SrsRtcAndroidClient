package com.shencoder.srs_rtc_android_client.webrtc.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.Toast
import com.elvishew.xlog.XLog
import com.shencoder.mvvmkit.util.toastError
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

    private var cameraVideoCapturer: CameraVideoCapturer? = null
    private var videoTrack: VideoTrack? = null
    private var surfaceTextureHelper: SurfaceTextureHelper? = null

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
            peerConnectionFactory.createPeerConnection(configuration, PeerConnectionObserver())!!
        //创建AudioSource，音频源
        val audioSource =
            peerConnectionFactory.createAudioSource(WebRTCUtil.createAudioConstraints())
        //创建AudioTrack，音频轨
        val audioTrack = peerConnectionFactory.createAudioTrack("local_audio_track", audioSource)

        cameraVideoCapturer = createVideoCapture(context).apply {
            val videoSource = peerConnectionFactory.createVideoSource(
                isScreencast
            )
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
            startCapture(640, 480, 25)
        }

        //这一步也必须调用，设置音视频资源，模式设置为仅发送即可-RtpTransceiver.RtpTransceiverDirection.SEND_ONLY
        peerConnection.addTransceiver(
            videoTrack,
            RtpTransceiverInit(RtpTransceiver.RtpTransceiverDirection.SEND_ONLY)
        )
        peerConnection.addTransceiver(
            audioTrack,
            RtpTransceiverInit(RtpTransceiver.RtpTransceiverDirection.SEND_ONLY)
        )
        return peerConnection
    }

    override fun afterInit() {
        svr.setZOrderMediaOverlay(true)
    }

    override fun beginRelease() {
        cameraVideoCapturer?.dispose()
        surfaceTextureHelper?.dispose()
        videoTrack?.dispose()
    }


    /**
     * 开始推流
     * @param webrtcUrl 推流地址
     */
    fun publishStream(webrtcUrl: String) {
        requestSrs(webrtcUrl, {
            isShowPrompt(false)
        }, {
            XLog.e("publishStream failure: ${it.message}")
            context.toastError("publishStream failure: ${it.message}", Toast.LENGTH_LONG)
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