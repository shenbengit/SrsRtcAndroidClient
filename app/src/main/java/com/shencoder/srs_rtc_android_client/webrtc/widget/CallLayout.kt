package com.shencoder.srs_rtc_android_client.webrtc.widget

import android.content.Context
import android.media.AudioManager
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.elvishew.xlog.XLog
import com.shencoder.mvvmkit.util.toastError
import com.shencoder.srs_rtc_android_client.R
import com.shencoder.srs_rtc_android_client.webrtc.bean.WebRTCStreamInfoBean
import com.shencoder.srs_rtc_android_client.webrtc.constant.IntoRoomType
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
    defStyleAttr: Int = 0
) :
    FrameLayout(context, attrs, defStyleAttr) {

    private val audioManager: AudioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private var actionCallback: CallActionCallback? = null

    private lateinit var peerConnectionFactory: PeerConnectionFactory
    private lateinit var audioDeviceModule: JavaAudioDeviceModule
    private lateinit var eglBase: EglBase
    private lateinit var eglBaseContext: EglBase.Context

    private val sgl: SteamGridLayout
    private val clBeforeCall: ConstraintLayout
    private val tvAccept: TextView
    private val clCallingAction: ConstraintLayout
    private val tvTime: TextView
    private val tvSpeakerMute: TextView
    private val tvSpeakerphone: TextView

    /**
     * 音频录入是否静音
     */
    private var isSpeakerMute: Boolean

    /**
     * 音频输出是否静音
     */
    private var isMicrophoneMute: Boolean

    /**
     * 扬声器
     */
    private var isSpeakerphone: Boolean

    private val originSpeakerphoneOn = audioManager.isSpeakerphoneOn

    init {
        val typedArray =
            context.obtainStyledAttributes(attrs, R.styleable.CallLayout)
        val pipMode = typedArray.getBoolean(
            R.styleable.CallLayout_cl_sgl_pip_mode,
            SteamGridLayout.DEFAULT_PIP_MODE
        )
        val pipPercent =
            typedArray.getFloat(
                R.styleable.CallLayout_cl_sgl_pip_percent,
                SteamGridLayout.DEFAULT_PIP_PERCENT
            )
        val pipMarginTop =
            typedArray.getDimensionPixelSize(
                R.styleable.CallLayout_cl_sgl_pip_margin_top,
                SteamGridLayout.DEFAULT_PIP_MARGIN
            )
        val pipMarginEnd =
            typedArray.getDimensionPixelSize(
                R.styleable.CallLayout_cl_sgl_pip_margin_end,
                SteamGridLayout.DEFAULT_PIP_MARGIN
            )
        isSpeakerMute = typedArray.getBoolean(R.styleable.CallLayout_cl_speaker_mute_on, false)
        isMicrophoneMute =
            typedArray.getBoolean(R.styleable.CallLayout_cl_microphone_mute_on, false)
        isSpeakerphone = typedArray.getBoolean(R.styleable.CallLayout_cl_speakerphone_on, true)

        typedArray.recycle()

        inflate(context, R.layout.layout_call, this)
        sgl = findViewById(R.id.sgl)
        clBeforeCall = findViewById(R.id.clBeforeCall)
        clCallingAction = findViewById(R.id.clCallingAction)
        //拒接
        clBeforeCall.findViewById<TextView>(R.id.tvReject).setOnClickListener {
            actionCallback?.rejectCall()
        }
        //接听
        tvAccept = clBeforeCall.findViewById(R.id.tvAccept)
        tvAccept.setOnClickListener {
            clBeforeCall.isVisible = false
            clCallingAction.isVisible = true
            actionCallback?.acceptCall()
        }
        tvTime = clCallingAction.findViewById(R.id.tvTime)
        tvSpeakerMute = clCallingAction.findViewById(R.id.tvSpeakerMute)
        tvSpeakerMute.setOnClickListener {
            setSpeakerMute(isSpeakerMute.not())
        }
        clCallingAction.findViewById<TextView>(R.id.tvSwitchCamera).setOnClickListener {
            switchCamera()
        }
        tvSpeakerphone = clCallingAction.findViewById(R.id.tvSpeakerphone)
        tvSpeakerphone.setOnClickListener {
            this.isSpeakerphone = isSpeakerphone.not()
            tvSpeakerphone.isSelected = isSpeakerphone
            operateSpeakerphone(isSpeakerphone)
        }
        //挂断
        clCallingAction.findViewById<TextView>(R.id.tvHangUp).setOnClickListener {
            actionCallback?.hangUpCall()
        }

        sgl.setPipMode(pipMode)
        sgl.setPipMarginPercent(pipPercent)
        sgl.setPipMarginTop(pipMarginTop)
        sgl.setPipMarginEnd(pipMarginEnd)

        setSpeakerMute(isSpeakerMute)
        setMicrophoneMute(isMicrophoneMute)
        operateSpeakerphone(isSpeakerphone)
    }

    fun setCallActionCallback(back: CallActionCallback) {
        actionCallback = back
    }

    /**
     * 初始化
     * @param type
     */
    fun init(type: IntoRoomType) {
        eglBase = EglBase.create()
        eglBaseContext = eglBase.eglBaseContext
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
                setSpeakerMute(isSpeakerMute)
                setMicrophoneMute(isMicrophoneMute)
            }

        peerConnectionFactory = PeerConnectionFactory.builder()
            .setOptions(options)
            .setVideoEncoderFactory(encoderFactory)
            .setVideoDecoderFactory(decoderFactory)
            .setAudioDeviceModule(audioDeviceModule)
            .createPeerConnectionFactory()

        sgl.init(peerConnectionFactory, eglBaseContext)

        when (type) {
            IntoRoomType.INVITE_INTO_ROOM -> {
                clBeforeCall.isVisible = true
                tvAccept.isVisible = false
                clCallingAction.isVisible = false
            }
            IntoRoomType.BE_INVITED_INTO_ROOM -> {
                clBeforeCall.isVisible = true
                tvAccept.isVisible = true
                clCallingAction.isVisible = false
            }
            IntoRoomType.DIRECTLY_INTO_ROOM -> {
                clBeforeCall.isVisible = false
                clCallingAction.isVisible = true
            }
        }
    }

    fun previewPublishStream(bean: WebRTCStreamInfoBean) {
        val renderer = PublishStreamSurfaceViewRenderer(context)
        renderer.setWebRTCStreamInfoBean(bean)
        sgl.addView(renderer)
    }

    /**
     * call after [previewPublishStream].
     */
    fun publishStream(
        onSuccess: () -> Unit = {},
        onFailure: (error: Throwable) -> Unit = {}
    ) {
        val publishStreamSurfaceViewRenderer = sgl.getPublishStreamSurfaceViewRenderer()
        publishStreamSurfaceViewRenderer?.publishStream(onSuccess, onFailure) ?: let {
            onFailure.invoke(NullPointerException("PublishStreamSurfaceViewRenderer is null."))
        }
    }

    fun addPlayStream(
        bean: WebRTCStreamInfoBean,
        onSuccess: () -> Unit = {},
        onFailure: (error: Throwable) -> Unit = {}
    ) {
        val renderer = PlayStreamSurfaceViewRenderer(context)
        renderer.setWebRTCStreamInfoBean(bean)
        sgl.addView(renderer)
    }

    /**
     * call after [addPlayStream].
     */
    fun playStream(
        userId: String,
        userType: String,
        onSuccess: () -> Unit = {},
        onFailure: (error: Throwable) -> Unit = {}
    ) {
        sgl.getPlayStreamSurfaceViewRenderer(userId, userType)?.playStream() ?: let {
            onFailure.invoke(NullPointerException("PlayStreamSurfaceViewRenderer is null."))
        }
    }

    fun removePlayStream(userId: String, userType: String) {
        sgl.removePlayStreamSurfaceView(userId, userType)
    }

    /**
     * 释放
     */
    fun release() {
        sgl.release()
        if (this::audioDeviceModule.isInitialized) {
            audioDeviceModule.release()
        }
        if (this::peerConnectionFactory.isInitialized) {
            peerConnectionFactory.dispose()
        }
        if (this::eglBase.isInitialized) {
            eglBase.release()
        }
        operateSpeakerphone(originSpeakerphoneOn)
    }

    private fun setSpeakerMute(mute: Boolean) {
        isSpeakerMute = mute
        if (this::audioDeviceModule.isInitialized) {
            audioDeviceModule.setSpeakerMute(mute)
        }
        tvSpeakerMute.isSelected = mute
    }

    private fun setMicrophoneMute(mute: Boolean) {
        isMicrophoneMute = mute
        if (this::audioDeviceModule.isInitialized) {
            audioDeviceModule.setMicrophoneMute(mute)
        }

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
            audioManager.isSpeakerphoneOn = false
        } else {
            //之前关闭的，打开
            audioManager.setStreamVolume(
                AudioManager.STREAM_VOICE_CALL,
                audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL),
                AudioManager.FX_KEY_CLICK
            )
            audioManager.isSpeakerphoneOn = true
        }
    }


    /**
     * 切换相机
     */
    private fun switchCamera() {
        sgl.getPublishStreamSurfaceViewRenderer()?.switchCamera({ isFrontCamera ->
            XLog.i("switch camera done, isFrontCamera: $isFrontCamera")
        }, { errorDescription ->
            XLog.e("switch camera error, errorDescription: $errorDescription")
            post { context.toastError(errorDescription) }
        })
    }


    interface CallActionCallback {
        /**
         * 拒接
         */
        fun rejectCall()

        /**
         * 接听
         */
        fun acceptCall()

        /**
         * 挂断
         */
        fun hangUpCall()
    }
}