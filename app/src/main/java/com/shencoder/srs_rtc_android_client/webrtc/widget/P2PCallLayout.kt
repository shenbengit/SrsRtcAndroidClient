package com.shencoder.srs_rtc_android_client.webrtc.widget

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.shencoder.srs_rtc_android_client.R
import com.shencoder.srs_rtc_android_client.constant.CallRoleType
import com.shencoder.srs_rtc_android_client.constant.CallType
import com.shencoder.srs_rtc_android_client.constant.isAudio
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
    companion object {
        private const val TIME_TAG = 101
    }

    private val textureRemote: TextureViewRenderer
    private val textureSelf: TextureViewRenderer
    private val clBeforeCall: ConstraintLayout
    private val tvAccept: TextView
    private val clCallingAction: ConstraintLayout
    private lateinit var tvTime: TextView
    private val tvMicrophoneMute: TextView
    private val tvSwitchCamera: TextView
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

    /**
     * 通话时长，单位：秒
     */
    private var callDuration = 0
    private val mHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                TIME_TAG -> {
                    ++callDuration
                    tvTime.text = changeTimeFormat(callDuration)
                    sendEmptyMessageDelayed(TIME_TAG, 1000L)
                }
            }
        }
    }

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
        tvSwitchCamera = clCallingAction.findViewById(R.id.tvSwitchCamera)
        tvSwitchCamera.setOnClickListener {
            switchCamera()
        }
        tvSpeakerphone = clCallingAction.findViewById(R.id.tvSpeakerphone)
        tvSpeakerphone.setOnClickListener {
            operateSpeakerphone(isSpeakerphone.not())
        }
        //挂断
        clCallingAction.findViewById<TextView>(R.id.tvHangUp).setOnClickListener {
            actionCallback?.hangUpCall()
        }

        clBeforeCall.isVisible = true
        clCallingAction.isVisible = false

        tvTime.text = changeTimeFormat(callDuration)
    }

    /**
     * 初始化
     */
    fun init(context: EglBase.Context, callType: CallType, callRoleType: CallRoleType) {
        textureRemote.init(context)
        textureSelf.init(context)

        this.callType = callType
        tvSwitchCamera.isInvisible = callType.isAudio()
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

        mHandler.sendEmptyMessage(TIME_TAG)
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
        mHandler.removeCallbacksAndMessages(null)
    }

    /**
     * 音频输入是否静音，即：麦克风输入是否静音
     */
    private fun setMicrophoneMute(mute: Boolean) {
        isMicrophoneMute = mute
        tvMicrophoneMute.isSelected = mute
        actionCallback?.setMicrophoneMute(mute)
    }

    /**
     * 操作扬声器
     */
    private fun operateSpeakerphone(isSpeakerphone: Boolean) {
        this.isSpeakerphone = isSpeakerphone
        tvSpeakerphone.isSelected = isSpeakerphone
        actionCallback?.operateSpeakerphone(isSpeakerphone)
    }

    private fun switchCamera() {
        actionCallback?.switchCamera()
    }

    /**
     * 秒转为HH:mm:ss
     */
    private fun changeTimeFormat(second: Int): String {
        val hour = second / 60 / 60
        val minute = second / 60 % 60
        val surplusSecond = second % 60
        return String.format("%02d:%02d:%02d", hour, minute, surplusSecond)
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