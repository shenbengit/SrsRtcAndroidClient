package com.shencoder.srs_rtc_android_client.webrtc.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.shencoder.srs_rtc_android_client.R
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

    private lateinit var peerConnectionFactory: PeerConnectionFactory
    private lateinit var audioDeviceModule: JavaAudioDeviceModule
    private lateinit var eglBase: EglBase
    private lateinit var eglBaseContext: EglBase.Context

    private val sgl: SteamGridLayout
    private val clBeforeCall: ConstraintLayout
    private val clCallingAction: ConstraintLayout
    private val tvTime: TextView
    private val tvMute: TextView
    private val tvSpeaker: TextView
    private var isMute: Boolean
    private var isSpeaker: Boolean

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
        isMute = typedArray.getBoolean(R.styleable.CallLayout_cl_mute_on, false)
        isSpeaker = typedArray.getBoolean(R.styleable.CallLayout_cl_speaker_on, true)

        typedArray.recycle()

        inflate(context, R.layout.layout_call, this)
        sgl = findViewById(R.id.sgl)
        clBeforeCall = findViewById(R.id.clBeforeCall)
        //拒接
        clBeforeCall.findViewById<TextView>(R.id.tvReject).setOnClickListener {

        }
        //接听
        clBeforeCall.findViewById<TextView>(R.id.tvAccept).setOnClickListener {

        }

        clCallingAction = findViewById(R.id.clCallingAction)
        tvTime = clCallingAction.findViewById(R.id.tvTime)
        tvMute = clCallingAction.findViewById(R.id.tvMute)
        tvMute.setOnClickListener {

        }
        clCallingAction.findViewById<TextView>(R.id.tvSwitchCamera).setOnClickListener {

        }
        tvSpeaker = clCallingAction.findViewById(R.id.tvSpeaker)
        tvSpeaker.setOnClickListener {

        }
        //挂断
        clCallingAction.findViewById<TextView>(R.id.tvHangUp).setOnClickListener {

        }

        sgl.setPipMode(pipMode)
        sgl.setPipMarginPercent(pipPercent)
        sgl.setPipMarginTop(pipMarginTop)
        sgl.setPipMarginEnd(pipMarginEnd)
    }

    /**
     *
     */
    fun init() {
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
//                setSpeakerMute(isMute)
            }

        peerConnectionFactory = PeerConnectionFactory.builder()
            .setOptions(options)
            .setVideoEncoderFactory(encoderFactory)
            .setVideoDecoderFactory(decoderFactory)
            .setAudioDeviceModule(audioDeviceModule)
            .createPeerConnectionFactory()

        sgl.init(peerConnectionFactory, eglBaseContext)
    }

    /**
     * 释放
     */
    fun release() {
        if (this::audioDeviceModule.isInitialized) {
            audioDeviceModule.release()
        }
        if (this::peerConnectionFactory.isInitialized) {
            peerConnectionFactory.dispose()
        }
        if (this::eglBase.isInitialized) {
            eglBase.release()
        }
        sgl.release()
    }
}