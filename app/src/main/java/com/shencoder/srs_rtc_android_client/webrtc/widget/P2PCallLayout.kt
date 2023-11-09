package com.shencoder.srs_rtc_android_client.webrtc.widget

import android.content.Context
import android.media.AudioManager
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.shencoder.srs_rtc_android_client.R
import com.shencoder.webrtcextension.TextureViewRenderer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.PeerConnectionFactory
import org.webrtc.audio.JavaAudioDeviceModule
import kotlin.coroutines.CoroutineContext


/**
 *
 * @author Shenben
 * @date 2023/11/6 17:16
 * @description
 * @since
 */
class P2PCallLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    FrameLayout(context, attrs, defStyleAttr), CoroutineScope {

    private val audioManager: AudioManager =
        context.applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val textureRemote: TextureViewRenderer
    private val textureSelf: TextureViewRenderer
    private val clBeforeCall: ConstraintLayout
    private val tvAccept: TextView
    private val clCallingAction: ConstraintLayout
    private lateinit var tvTime: TextView
    private val tvMicrophoneMute: TextView
    private val tvSpeakerphone: TextView

    /**
     * 音频输出是否静音
     */
    private var isSpeakerMute: Boolean = false

    /**
     * 音频输入是否静音，即：麦克风输入是否静音
     */
    private var isMicrophoneMute: Boolean  = false

    /**
     * 扬声器
     */
    private var isSpeakerphone: Boolean = false

    private val originSpeakerphoneOn = audioManager.isSpeakerphoneOn

    private var actionCallback: CallActionCallback? = null

    private lateinit var peerConnectionFactory: PeerConnectionFactory
    private lateinit var audioDeviceModule: JavaAudioDeviceModule
    private lateinit var eglBase: EglBase
    private lateinit var eglBaseContext: EglBase.Context

    /**
     * 协程
     */
    private val scope = SupervisorJob() + Dispatchers.Main.immediate
    override val coroutineContext: CoroutineContext
        get() = scope

    init {
        val typedArray =
            context.obtainStyledAttributes(attrs, R.styleable.P2PCallLayout)

        typedArray.recycle()

        inflate(context, R.layout.layout_p2p_call, this)
        textureRemote = findViewById(R.id.textureRemote)
        textureSelf = findViewById(R.id.textureSelf)
        clBeforeCall = findViewById(R.id.clBeforeCall)
        clCallingAction = findViewById(R.id.clCallingAction)
        //拒接
        clBeforeCall.findViewById<TextView>(R.id.tvReject).setOnClickListener {
//            when (intoRoomType) {
//                CallLayout.ACTIVELY_INTO_ROOM -> {
//                    actionCallback?.hangUpCall()
//                }
//                CallLayout.BE_INVITED_INTO_ROOM -> {
//                    actionCallback?.rejectCall()
//                }
//            }
        }
        //接听
        tvAccept = clBeforeCall.findViewById(R.id.tvAccept)
        tvAccept.setOnClickListener {
            setInCallStatus()
            actionCallback?.acceptCall()
        }
        tvTime = clCallingAction.findViewById(R.id.tvTime)
        tvMicrophoneMute = clCallingAction.findViewById(R.id.tvMicrophoneMute)
        tvMicrophoneMute.setOnClickListener {
            setMicrophoneMute(isMicrophoneMute.not())
        }
        clCallingAction.findViewById<TextView>(R.id.tvSwitchCamera).setOnClickListener {
//            switchCamera()
        }
        tvSpeakerphone = clCallingAction.findViewById(R.id.tvSpeakerphone)
        tvSpeakerphone.setOnClickListener {
            this.isSpeakerphone = isSpeakerphone.not()
            operateSpeakerphone(isSpeakerphone)
        }
        //挂断
        clCallingAction.findViewById<TextView>(R.id.tvHangUp).setOnClickListener {
            actionCallback?.hangUpCall()
        }

    }

    /**
     * 初始化
     */
    fun init() {
        eglBase = EglBase.create()
        eglBaseContext = eglBase.eglBaseContext
        val options = PeerConnectionFactory.Options()
        val encoderFactory = DefaultVideoEncoderFactory(eglBaseContext, true, true)
        val decoderFactory = DefaultVideoDecoderFactory(eglBaseContext)
        audioDeviceModule =
            JavaAudioDeviceModule.builder(context).createAudioDeviceModule().apply {
                setSpeakerMute(isSpeakerMute)
                setMicrophoneMute(isMicrophoneMute)
            }

        peerConnectionFactory = PeerConnectionFactory.builder()
            .setOptions(options)
            .setVideoEncoderFactory(encoderFactory)
            .setVideoDecoderFactory(decoderFactory)
            .setAudioDeviceModule(audioDeviceModule)
            .createPeerConnectionFactory()

        textureRemote.init(eglBaseContext)
        textureSelf.init(eglBaseContext)
    }

    fun sendOffer(){
        launch {

        }
    }


    /**
     * 音频输入是否静音，即：麦克风输入是否静音
     */
    private fun setMicrophoneMute(mute: Boolean) {
        isMicrophoneMute = mute
        if (this::audioDeviceModule.isInitialized) {
            audioDeviceModule.setMicrophoneMute(mute)
        }
        tvMicrophoneMute.isSelected = mute
    }

    /**
     * 操作扬声器
     */
    private fun operateSpeakerphone(isSpeakerphone: Boolean) {
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        if (isSpeakerphone) {
            //之前打开的，关闭
            //设置音量，解决有些机型切换后没声音或者声音突然变大的问题
            audioManager.setStreamVolume(
                AudioManager.STREAM_VOICE_CALL,
                audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL),
                AudioManager.FX_KEY_CLICK
            )
            audioManager.isSpeakerphoneOn = true
        } else {
            //之前关闭的，打开
            audioManager.setStreamVolume(
                AudioManager.STREAM_VOICE_CALL,
                audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL),
                AudioManager.FX_KEY_CLICK
            )
            audioManager.isSpeakerphoneOn = false
        }
        tvSpeakerphone.isSelected = isSpeakerphone
    }

    /**
     * 设置通话中状态
     */
    fun setInCallStatus() {
        clBeforeCall.isVisible = false
        clCallingAction.isVisible = true
    }

    interface CallActionCallback {
        /**
         * 拒接
         */
        fun rejectCall() {}

        /**
         * 接听
         */
        fun acceptCall() {}

        /**
         * 挂断
         */
        fun hangUpCall()
    }
}