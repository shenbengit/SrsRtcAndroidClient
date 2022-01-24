package com.shencoder.srs_rtc_android_client.webrtc.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.Toast
import com.elvishew.xlog.XLog
import com.shencoder.mvvmkit.util.toastError
import com.shencoder.srs_rtc_android_client.webrtc.PeerConnectionObserver
import com.shencoder.srs_rtc_android_client.webrtc.constant.StreamType
import org.webrtc.*
import org.webrtc.PeerConnection.RTCConfiguration
import org.webrtc.RtpTransceiver.RtpTransceiverInit

/**
 * 用于显示拉流画面
 *
 * @author  ShenBen
 * @date    2022/1/21 11:20
 * @email   714081644@qq.com
 */
class PlayStreamSurfaceViewRenderer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : BaseStreamSurfaceViewRenderer(context, attrs, defStyleAttr, defStyleRes) {


    override fun streamType(): StreamType {
        return StreamType.PLAY
    }

    override fun createPeerConnection(
        peerConnectionFactory: PeerConnectionFactory,
        sharedContext: EglBase.Context
    ): PeerConnection {

        val configuration = RTCConfiguration(emptyList())
        //必须设置PeerConnection.SdpSemantics.UNIFIED_PLAN
        configuration.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
        val peerConnection = peerConnectionFactory.createPeerConnection(
            configuration,
            object : PeerConnectionObserver() {
                /**
                 * 在获取到流的时候回调；
                 * 使用SRS服务时就是在拉流成功时回调
                 *
                 * @param mediaStream 音视频流
                 */
                override fun onAddStream(mediaStream: MediaStream?) {
                    super.onAddStream(mediaStream)
                    mediaStream?.let {
                        //仅处理视频轨即可。
                        val videoTracks = it.videoTracks
                        if (videoTracks.isNotEmpty()) {
                            //这一步就是为了显示，一般来说videoTracks的大小就为1.
                            //将画面显示到SurfaceViewRenderer上
                            addVideoStream(videoTracks[0])
                            isShowPrompt(false)
                        }
                    }
                }
            })!!
        //这一步也必须调用，接收音视频资源，模式设置为仅接收即可-RtpTransceiver.RtpTransceiverDirection.RECV_ONLY
        peerConnection.addTransceiver(
            MediaStreamTrack.MediaType.MEDIA_TYPE_VIDEO,
            RtpTransceiverInit(RtpTransceiver.RtpTransceiverDirection.RECV_ONLY)
        )
        peerConnection.addTransceiver(
            MediaStreamTrack.MediaType.MEDIA_TYPE_AUDIO,
            RtpTransceiverInit(RtpTransceiver.RtpTransceiverDirection.RECV_ONLY)
        )
        return peerConnection
    }

    override fun afterInit() {

    }

    override fun beginRelease() {

    }

    /**
     * 开始拉流
     */
    fun playStream(
        onSuccess: () -> Unit = {},
        onFailure: (error: Throwable) -> Unit = {}
    ) {
        requestSrs({
            onSuccess.invoke()
        }, {
            onFailure.invoke(it)
            XLog.e("playStream failure: ${it.message}")
            context.toastError("playStream failure: ${it.message}", Toast.LENGTH_LONG)
        })
    }

}