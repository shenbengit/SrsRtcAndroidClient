package com.shencoder.srs_rtc_android_client.webrtc.widget

import com.shencoder.srs_rtc_android_client.util.WebRTCUtil

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.CallSuper
import androidx.core.view.isVisible
import coil.loadAny
import com.shencoder.srs_rtc_android_client.R
import com.shencoder.srs_rtc_android_client.webrtc.SdpAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.webrtc.*
import kotlin.coroutines.CoroutineContext

/**
 *
 * @author  ShenBen
 * @date    2022/1/21 11:11
 * @email   714081644@qq.com
 */
@Suppress("LeakingThis")
abstract class BaseStreamSurfaceViewRenderer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) :
    FrameLayout(context, attrs, defStyleAttr, defStyleRes), CoroutineScope {

    /**
     * 协程
     */
    private val scope = SupervisorJob() + Dispatchers.Main.immediate

    final override val coroutineContext: CoroutineContext
        get() = scope

    protected lateinit var peerConnection: PeerConnection
    protected val svr: SurfaceViewRenderer
    protected val tvPromptUsername: TextView
    protected val ivPromptAvatar: ImageView

    init {
        inflate(context, R.layout.layout_stream_renderer, this)
        svr = findViewById(R.id.svr)
        tvPromptUsername = findViewById(R.id.tvPromptUsername)
        ivPromptAvatar = findViewById(R.id.ivPromptAvatar)
    }

    /**
     * 此方法必须调用
     * 与[release]对应
     */
    @CallSuper
    @JvmOverloads
    fun init(
        peerConnectionFactory: PeerConnectionFactory,
        sharedContext: EglBase.Context,
        rendererEvents: RendererCommon.RendererEvents? = null,
        configAttributes: IntArray = EglBase.CONFIG_PLAIN,
        drawer: RendererCommon.GlDrawer = GlRectDrawer()
    ) {
        svr.init(sharedContext, rendererEvents, configAttributes, drawer)
        peerConnection = createPeerConnection(peerConnectionFactory, sharedContext)
        afterInit()
    }

    fun setPrompt(name: String, data: Any?) {
        tvPromptUsername.text = name
        ivPromptAvatar.loadAny(data)
    }

    fun isShowPrompt(isVisible: Boolean) {
        post {
            tvPromptUsername.isVisible = isVisible
            ivPromptAvatar.isVisible = isVisible
        }
    }

    fun addVideoStream(videoTrack: VideoTrack) {
        videoTrack.addSink(svr)
    }

    fun removeVideoStream(videoTrack: VideoTrack) {
        videoTrack.removeSink(svr)
    }

    /**
     * 此方法必须调用
     * 与[init]对应
     */
    @CallSuper
    fun release() {
        beginRelease()

        svr.release()

        if (this::peerConnection.isInitialized) {
            peerConnection.dispose()
        }
        scope.cancel()

    }

    /**
     * 初始化之后
     */
    abstract fun afterInit()

    abstract fun createPeerConnection(
        peerConnectionFactory: PeerConnectionFactory,
        sharedContext: EglBase.Context
    ): PeerConnection

    /**
     * 开始释放之前
     */
    abstract fun beginRelease()

    /**
     * 创建offer
     */
    protected inline fun createOffer(
        isReceive: Boolean,
        crossinline onSuccess: (sdp: String) -> Unit,
        crossinline onFailure: (error: String?) -> Unit
    ) {
        val tag = if (isReceive) "play" else "publish"
        peerConnection.createOffer(object : SdpAdapter("${tag}-createOffer") {
            override fun onCreateSuccess(description: SessionDescription) {
                super.onCreateSuccess(description)
                //local sdp 创建成功
                if (description.type == SessionDescription.Type.OFFER) {
                    peerConnection.setLocalDescription(object :
                        SdpAdapter("${tag}-setLocalDescription") {
                        override fun onSetSuccess() {
                            super.onSetSuccess()
                            onSuccess.invoke(description.description)
                        }

                        override fun onSetFailure(error: String?) {
                            super.onSetFailure(error)
                            onFailure.invoke("create offer failure, reason:${error}")
                        }
                    }, description)
                }
            }

            override fun onCreateFailure(error: String?) {
                super.onCreateFailure(error)
                onFailure.invoke("create offer failure, reason:${error}")
            }
        }, WebRTCUtil.offerOrAnswerConstraint(isReceive))
    }

    protected inline fun setRemoteDescription(
        sdp: String,
        crossinline onSuccess: () -> Unit,
        crossinline onFailure: (error: String?) -> Unit
    ) {
        //手动设置为ANSWER类型，和OFFER对应
        val answerDescription = SessionDescription(SessionDescription.Type.ANSWER, sdp)
        peerConnection.setRemoteDescription(
            object : SdpAdapter("setRemoteDescription") {
                override fun onSetSuccess() {
                    super.onSetSuccess()
                    onSuccess.invoke()
                }

                override fun onSetFailure(error: String?) {
                    super.onSetFailure(error)
                    onFailure.invoke("create offer failure, reason:${error}")
                }
            }, answerDescription
        )
    }


}