package com.shencoder.srs_rtc_android_client.webrtc.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.EglBase
import org.webrtc.PeerConnectionFactory
import org.webrtc.audio.JavaAudioDeviceModule
import org.webrtc.createCustomVideoEncoderFactory

/**
 *
 * @author  ShenBen
 * @date    2022/1/21 11:23
 * @email   714081644@qq.com
 */
class CallLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) :
    FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val peerConnectionFactory: PeerConnectionFactory
    private val audioDeviceModule: JavaAudioDeviceModule
    private val eglBase = EglBase.create()
    private val eglBaseContext = eglBase.eglBaseContext

    init {
        val options = PeerConnectionFactory.Options()
        val encoderFactory =
            createCustomVideoEncoderFactory(eglBaseContext,
                enableIntelVp8Encoder = true,
                enableH264HighProfile = true,
                videoEncoderSupportedCallback = { info -> //判断编码器是否支持
                    false
                })
        val decoderFactory = DefaultVideoDecoderFactory(eglBaseContext)
        audioDeviceModule =
            JavaAudioDeviceModule.builder(context).createAudioDeviceModule().apply {
//                setSpeakerMute(isMute)
            }

        peerConnectionFactory = PeerConnectionFactory.builder()
            .setOptions(options)
            .setVideoEncoderFactory(encoderFactory)
            .setVideoDecoderFactory(decoderFactory)
            .setAudioDeviceModule(audioDeviceModule)
            .createPeerConnectionFactory()
    }


    fun release(){
        audioDeviceModule.release()
        peerConnectionFactory.dispose()
        eglBase.release()
    }
}