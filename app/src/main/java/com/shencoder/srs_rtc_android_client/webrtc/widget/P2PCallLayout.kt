package com.shencoder.srs_rtc_android_client.webrtc.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.shencoder.srs_rtc_android_client.R
import com.shencoder.srs_rtc_android_client.constant.CallRoleType
import com.shencoder.srs_rtc_android_client.constant.CallType
import com.shencoder.srs_rtc_android_client.constant.isVideo
import com.shencoder.webrtcextension.TextureViewRenderer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.webrtc.EglBase
import org.webrtc.VideoTrack
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

    private val textureRemote: TextureViewRenderer
    private val textureSelf: TextureViewRenderer
    private val clBeforeCall: ConstraintLayout
    private val tvAccept: TextView
    private val clCallingAction: ConstraintLayout
    private lateinit var tvTime: TextView
    private val tvMicrophoneMute: TextView
    private val tvSpeakerphone: TextView

    private var callType: CallType = CallType.Video
    private var callRoleType: CallRoleType = CallRoleType.CALLER

    /**
     * 音频输入是否静音，即：麦克风输入是否静音
     */
    private var isMicrophoneMute: Boolean = false

    /**
     * 扬声器
     */
    private var isSpeakerphone: Boolean = false

    private var actionCallback: CallActionCallback? = null

    /**
     * 协程
     */
    private val scope = SupervisorJob() + Dispatchers.Main.immediate
    override val coroutineContext: CoroutineContext
        get() = scope

    private var localVideoTrack: VideoTrack? = null
    private var remoteVideoTrack: VideoTrack? = null

    init {
        val typedArray =
            context.obtainStyledAttributes(attrs, R.styleable.P2PCallLayout)

        typedArray.recycle()

        keepScreenOn = true

        inflate(context, R.layout.layout_p2p_call, this)
        textureRemote = findViewById(R.id.textureRemote)
        textureSelf = findViewById(R.id.textureSelf)
        clBeforeCall = findViewById(R.id.clBeforeCall)
        clCallingAction = findViewById(R.id.clCallingAction)
        //拒接
        clBeforeCall.findViewById<TextView>(R.id.tvReject).setOnClickListener {
            if (callRoleType == CallRoleType.CALLEE) {
                actionCallback?.rejectCall()
            } else {
                actionCallback?.hangUpCall()
            }
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
            switchCamera()
        }
        tvSpeakerphone = clCallingAction.findViewById(R.id.tvSpeakerphone)
        tvSpeakerphone.setOnClickListener {
            this.isSpeakerphone = isSpeakerphone.not()

        }
        //挂断
        clCallingAction.findViewById<TextView>(R.id.tvHangUp).setOnClickListener {
            actionCallback?.hangUpCall()
        }

        clBeforeCall.isVisible = true
        clCallingAction.isVisible = false

    }

    /**
     * 初始化
     */
    fun init(context: EglBase.Context, callType: CallType, callRoleType: CallRoleType) {
        textureRemote.init(context)
        textureSelf.init(context)

        this.callType = callType
        textureRemote.isVisible = callType.isVideo()
        textureSelf.isVisible = callType.isVideo()

        this.callRoleType = callRoleType

        when (callRoleType) {
            CallRoleType.CALLER -> {
                tvAccept.isVisible = false
            }

            CallRoleType.CALLEE -> {
                tvAccept.isVisible = true
            }
        }
    }

    fun setLocalVideoTrack(videoTrack: VideoTrack?) {
        if (videoTrack != null && callType.isVideo()) {
            localVideoTrack = videoTrack
            videoTrack.addSink(textureRemote)
        }
    }

    fun setCallActionCallback(back: CallActionCallback) {
        actionCallback = back
    }

    /**
     * 设置通话中状态
     */
    fun setInCallStatus() {
        clBeforeCall.isVisible = false
        clCallingAction.isVisible = true

        val videoTrack = localVideoTrack
        if (videoTrack != null && callType.isVideo()) {

            videoTrack.removeSink(textureRemote)
            textureRemote.clearImage()

            videoTrack.addSink(textureSelf)
        }
    }

    fun setRemoteVideoTrack(videoTrack: VideoTrack?) {
        if (videoTrack != null && callType.isVideo()) {
            remoteVideoTrack = videoTrack
            videoTrack.addSink(textureRemote)
        }
    }

    fun release() {
        localVideoTrack = null
        remoteVideoTrack = null
        textureRemote.release()
        textureSelf.release()
    }

    /**
     * 音频输入是否静音，即：麦克风输入是否静音
     */
    private fun setMicrophoneMute(mute: Boolean) {
        tvMicrophoneMute.isSelected = mute
        actionCallback?.setMicrophoneMute(mute)
    }

    /**
     * 操作扬声器
     */
    private fun operateSpeakerphone(isSpeakerphone: Boolean) {
        tvSpeakerphone.isSelected = isSpeakerphone
        actionCallback?.operateSpeakerphone(isSpeakerphone)
    }

    private fun switchCamera() {
        actionCallback?.switchCamera()
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

        fun setMicrophoneMute(mute: Boolean)

        fun operateSpeakerphone(isSpeakerphone: Boolean)

        fun switchCamera() {}
    }
}