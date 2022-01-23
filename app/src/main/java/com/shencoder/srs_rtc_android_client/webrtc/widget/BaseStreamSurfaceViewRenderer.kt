package com.shencoder.srs_rtc_android_client.webrtc.widget

import com.shencoder.srs_rtc_android_client.webrtc.util.WebRTCUtil

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.CallSuper
import androidx.core.view.isVisible
import coil.loadAny
import com.shencoder.srs_rtc_android_client.R
import com.shencoder.srs_rtc_android_client.constant.SRS
import com.shencoder.srs_rtc_android_client.http.RetrofitClient
import com.shencoder.srs_rtc_android_client.http.bean.SrsRequestBean
import com.shencoder.srs_rtc_android_client.webrtc.SdpAdapter
import com.shencoder.srs_rtc_android_client.webrtc.constant.StreamType
import kotlinx.coroutines.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
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
    FrameLayout(context, attrs, defStyleAttr, defStyleRes), CoroutineScope, KoinComponent {

    /**
     * 协程
     */
    private val scope = SupervisorJob() + Dispatchers.Main.immediate

    protected val retrofitClient: RetrofitClient by inject()

    final override val coroutineContext: CoroutineContext
        get() = scope

    protected lateinit var peerConnection: PeerConnection
    protected val svr: SurfaceViewRenderer
    protected val tvPromptUsername: TextView
    protected val ivPromptAvatar: ImageView
    protected val streamType: StreamType

    init {
        inflate(context, R.layout.layout_stream_renderer, this)
        svr = findViewById(R.id.svr)
        tvPromptUsername = findViewById(R.id.tvPromptUsername)
        ivPromptAvatar = findViewById(R.id.ivPromptAvatar)
        streamType = streamType()
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
     * 获取流类型
     */
    abstract fun streamType(): StreamType

    /**
     * 创建连接
     */
    abstract fun createPeerConnection(
        peerConnectionFactory: PeerConnectionFactory,
        sharedContext: EglBase.Context
    ): PeerConnection

    /**
     * 初始化之后
     */
    abstract fun afterInit()


    /**
     * 开始释放之前
     */
    abstract fun beginRelease()

    /**
     * 向SRS提交请求
     */
    protected inline fun requestSrs(
        webrtcUrl: String,
        crossinline onSuccess: () -> Unit,
        crossinline onFailure: (error: Throwable) -> Unit
    ) {
        createOffer(streamType == StreamType.PLAY,
            onSuccess = { sdp ->
                //向srs服务器进行推拉流请求
                launch(Dispatchers.Main) {
                    runCatching {
                        withContext(Dispatchers.IO) {
                            val srsBean = SrsRequestBean(sdp, webrtcUrl)
                            if (streamType == StreamType.PLAY) {
                                retrofitClient.getApiService()
                                    .play(SRS.HTTPS_REQUEST_PLAY_URL, srsBean)
                            } else {
                                retrofitClient.getApiService()
                                    .publish(SRS.HTTPS_REQUEST_PUBLISH_URL, srsBean)
                            }
                        }
                    }.onSuccess { bean ->
                        if (bean.isSuccess) {
                            setRemoteDescription(
                                WebRTCUtil.convertAnswerSdp(sdp, bean.sdp),
                                onSuccess = {
                                    onSuccess.invoke()
                                }, onFailure = { error ->
                                    onFailure.invoke(Throwable(error))
                                })
                        } else {
                            val error = context.getString(R.string.check_srs_request_failure)
                            onFailure.invoke(Throwable("request srs failure, code: ${bean.code} , $error"))
                        }
                    }.onFailure {
                        onFailure.invoke(it)
                    }
                }
            }, onFailure = { error ->
                onFailure.invoke(Throwable(error))
            })
    }

    /**
     * 创建offer
     */
    protected inline fun createOffer(
        isReceive: Boolean,
        crossinline onSuccess: (sdp: String) -> Unit,
        crossinline onFailure: (error: String?) -> Unit
    ) {
        peerConnection.createOffer(object : SdpAdapter("createOffer") {
            override fun onCreateSuccess(description: SessionDescription) {
                super.onCreateSuccess(description)
                //local sdp 创建成功
                if (description.type == SessionDescription.Type.OFFER) {
                    peerConnection.setLocalDescription(object :
                        SdpAdapter("setLocalDescription") {
                        override fun onSetSuccess() {
                            super.onSetSuccess()
                            onSuccess.invoke(description.description)
                        }

                        override fun onSetFailure(error: String?) {
                            super.onSetFailure(error)
                            onFailure.invoke("set local description failure, reason:${error}")
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
                    onFailure.invoke("set remote description, reason:${error}")
                }
            }, answerDescription
        )
    }
}