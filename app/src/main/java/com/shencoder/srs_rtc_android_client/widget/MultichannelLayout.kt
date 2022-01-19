//package com.shencoder.srs_rtc_android_client.widget
//
//import android.content.Context
//import android.media.AudioManager
//import android.util.AttributeSet
//import android.widget.FrameLayout
//import android.widget.TextView
//import androidx.annotation.CallSuper
//import androidx.constraintlayout.widget.ConstraintLayout
//import androidx.core.view.isVisible
//import bd.nj.meetingsystem.call.ui.call.MeetingRoleType
//import com.elvishew.xlog.XLog
//import com.shencoder.mvvmkit.util.toastError
//import com.shencoder.mvvmkit.util.toastSuccess
//import com.shencoder.mvvmkit.util.toastWarning
//import com.shencoder.srs_rtc_android_client.R
//import com.shencoder.srs_rtc_android_client.constant.Constant
//import com.shencoder.srs_rtc_android_client.http.RetrofitClient
//import com.shencoder.srs_rtc_android_client.http.bean.SrsRequestBean
//import com.shencoder.srs_rtc_android_client.webrtc.PeerConnectionObserver
//import com.shencoder.srs_rtc_android_client.webrtc.SdpAdapter
//import kotlinx.coroutines.*
//import org.koin.core.component.KoinComponent
//import org.koin.core.component.inject
//import org.webrtc.*
//import org.webrtc.audio.JavaAudioDeviceModule
//import kotlin.coroutines.CoroutineContext
//
///**
// *
// * @author  ShenBen
// * @date    2021/10/29 16:57
// * @email   714081644@qq.com
// */
//
///**
// * 接受会话
// */
//typealias AnswerCall = () -> Unit
///**
// * 拒绝会话
// */
//typealias HangUpCall = (Boolean) -> Unit
//
//data class PlaySessionBean(
//    val terId: Int,
//    val terName: String? = null
//) {
//    var webrtcUrl: String = ""
//
//    internal lateinit var playCallBean: PlayCallBean
//
//    override fun equals(other: Any?): Boolean {
//        if (this === other) return true
//        if (javaClass != other?.javaClass) return false
//
//        other as PlaySessionBean
//
//        if (terId != other.terId) return false
//
//        return true
//    }
//
//    override fun hashCode(): Int {
//        return terId
//    }
//
//}
//
//class MultichannelLayout @JvmOverloads constructor(
//    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
//) : FrameLayout(context, attrs, defStyleAttr), CoroutineScope, KoinComponent {
//
//    private val scope = SupervisorJob() + Dispatchers.Main.immediate
//    private val retrofitClient: RetrofitClient by inject()
//
//    private var answerCall: AnswerCall? = null
//    private var hangUpCall: HangUpCall? = null
//
//    private val peerConnectionFactory: PeerConnectionFactory
//    private val audioDeviceModule: JavaAudioDeviceModule
//    private val eglBaseContext = EglBase.create().eglBaseContext
//
//    private val cameraHandler = object : CameraVideoCapturer.CameraEventsHandler {
//        override fun onCameraError(errorDescription: String?) {
//            XLog.e("摄像头开启失败：${errorDescription}")
//            post {
//                context.toastError("摄像头开启失败：${errorDescription}")
//            }
//        }
//
//        override fun onCameraDisconnected() {
//        }
//
//        override fun onCameraFreezed(errorDescription: String?) {
//        }
//
//        override fun onCameraOpening(cameraName: String?) {
//        }
//
//        override fun onFirstFrameAvailable() {
//        }
//
//        override fun onCameraClosed() {
//        }
//    }
//
//    private val audioManager: AudioManager =
//        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
//
//    private val nineGridLayout: NineGridLayout
//    private val clBeforeCall: ConstraintLayout
//    private val tvBeforeCallAnswer: TextView
//    private val clInCall: ConstraintLayout
//    private val tvTime: TextView
//    private val tvInCallMute: TextView
//    private val tvInCallSwitchCamera: TextView
//    private val tvInCallSpeakerphone: TextView
//    private val tvInCallHangUp: TextView
//
//
//    /**
//     * 是否静音
//     */
//    private var isMute = false
//
//    /**
//     * 是否免提
//     */
//    private var isSpeakerphone = false
//
//    /**
//     * 推流的信息
//     * 即：本地的视频画面
//     */
//    private var publishCallBean: PublishCallBean? = null
//
//    private val playSessionList = mutableSetOf<PlaySessionBean>()
//
//    /**
//     * srs网络请求BaseUrl
//     * 如：
//     * https://ip:1990/
//     */
//    private lateinit var mSrsBaseUrl: String
//
//    /**
//     * 流地址-BaseUrl
//     * 如：
//     * webrtc://ip:1990/
//     */
//    private lateinit var mStreamBaseUrl: String
//
//    init {
//        val options = PeerConnectionFactory.Options()
//        val encoderFactory =
//            createCustomVideoEncoderFactory(eglBaseContext,
//                enableIntelVp8Encoder = true,
//                enableH264HighProfile = true,
//                videoEncoderSupportedCallback = { info -> //判断编码器是否支持
//                    true
//                })
//        val decoderFactory = DefaultVideoDecoderFactory(eglBaseContext)
//        audioDeviceModule =
//            JavaAudioDeviceModule.builder(context).createAudioDeviceModule().apply {
//                setSpeakerMute(isMute)
//            }
//
//        peerConnectionFactory = PeerConnectionFactory.builder()
//            .setOptions(options)
//            .setVideoEncoderFactory(encoderFactory)
//            .setVideoDecoderFactory(decoderFactory)
//            .setAudioDeviceModule(audioDeviceModule)
//            .createPeerConnectionFactory()
//
//        inflate(context, R.layout.layout_mutichannel, this)
//        nineGridLayout = findViewById(R.id.nineGridLayout)
//        clBeforeCall = findViewById(R.id.clBeforeCall)
//        clInCall = findViewById(R.id.clInCall)
//        tvBeforeCallAnswer = clBeforeCall.findViewById(R.id.tvBeforeCallAnswer)
//        tvBeforeCallAnswer.setOnClickListener {
//            answerCall()
//            answerCall?.invoke()
//        }
//        clBeforeCall.findViewById<TextView>(R.id.tvBeforeCallHangUp).setOnClickListener {
//            hangUp(true)
//        }
//
//        tvTime = clInCall.findViewById(R.id.tvTime)
//        tvInCallMute = clInCall.findViewById(R.id.tvInCallMute)
//        tvInCallMute.setOnClickListener {
//            isMute = isMute.not()
//            audioDeviceModule.setSpeakerMute(isMute)
//            it.isSelected = isMute
//        }
//        tvInCallSwitchCamera = clInCall.findViewById(R.id.tvInCallSwitchCamera)
//        tvInCallSwitchCamera.setOnClickListener {
//            publishCallBean?.switchCamera({
//                post { context.toastSuccess("切换相机成功") }
//            }, { errorDescription ->
//                post { context.toastError("切换相机失败：$errorDescription") }
//            })
//        }
//        tvInCallSpeakerphone = clInCall.findViewById(R.id.tvInCallSpeakerphone)
//        tvInCallSpeakerphone.setOnClickListener {
//            operateSpeakerphone()
//        }
//
//        tvInCallHangUp = clInCall.findViewById(R.id.tvInCallHangUp)
//        tvInCallHangUp.setOnClickListener {
//            hangUp(false)
//        }
//
//    }
//
//    override val coroutineContext: CoroutineContext
//        get() = scope
//
//    /**
//     * 统一使用srs服务器 https 请求方式
//     */
//    fun setBaseUrl(srsBaseUrl: String, streamBaseUrl: String) {
//        mSrsBaseUrl = srsBaseUrl
//        mStreamBaseUrl = streamBaseUrl
//    }
//
//    /**
//     * @param answerCall 接受会话
//     * @param hangUpCall 拒绝会话
//     */
//    fun setActionCallback(answerCall: AnswerCall?, hangUpCall: HangUpCall?) {
//        this.answerCall = answerCall
//        this.hangUpCall = hangUpCall
//    }
//
//    /**
//     * 设置会见类型
//     */
//    fun setMeetingRoleType(type: MeetingRoleType) {
//        clInCall.isVisible = type == MeetingRoleType.CALLER
//        clBeforeCall.isVisible = type == MeetingRoleType.CALLEE
//    }
//
//    fun answerCall() {
//        clBeforeCall.isVisible = false
//        clInCall.isVisible = true
//    }
//
//    /**
//     * 添加推流
//     * @param publishPath 推流的webrtc路径
//     * @param width 预览画面的宽度，px
//     * @param height 预览画面的高度，px
//     * @param framerate 帧率
//     */
//    fun addPublishStream(
//        publishPath: String,
//        width: Int = 640,
//        height: Int = 480,
//        framerate: Int = 20,
//        success: () -> Unit,
//        error: (Throwable) -> Unit
//    ) {
//        if (publishCallBean != null) {
//            context.toastWarning("当前以存在推流！")
//            return
//        }
//        if (nineGridLayout.childCount >= NineGridLayout.MAX_CHILD_COUNT) {
//            context.toastWarning("已经超出最大会话数量")
//            return
//        }
//        val publishUrl = mStreamBaseUrl + publishPath
//
//        publish(publishUrl, width, height, framerate, success, error)
//    }
//
//
//    fun addPlayStream(
//        list: List<PlaySessionBean>, success: () -> Unit = {},
//        error: (Throwable) -> Unit = {}
//    ) {
//        for (bean in list) {
//            if (nineGridLayout.childCount >= NineGridLayout.MAX_CHILD_COUNT) {
//                context.toastWarning("已经超出最大会话数量")
//                return
//            }
//            if (playSessionList.contains(bean)) {
//                context.toastWarning("该流已存在列表")
//                continue
//            }
//            createPlayPeerConnection(bean, success, error)
//        }
//    }
//
//    fun playStream(
//        bean: PlaySessionBean, webrtcUrl: String,
//        success: () -> Unit,
//        error: (Throwable) -> Unit
//    ) {
//        val find = playSessionList.find { it == bean }
//        if (find == null) {
//            bean.webrtcUrl = webrtcUrl
//            if (nineGridLayout.childCount >= NineGridLayout.MAX_CHILD_COUNT) {
//                context.toastWarning("已经超出最大会话数量")
//                return
//            }
//            //创建连接
//            createPlayPeerConnection(bean, success, error)
//        } else {
//            //直接播放
//            find.webrtcUrl = webrtcUrl
//            pullStream(find.playCallBean.peerConnection, webrtcUrl, success, error)
//        }
//    }
//
//    fun removePlayStream(bean: PlaySessionBean) {
//        val find = playSessionList.find { it == bean } ?: return
//        find.playCallBean.release()
//        nineGridLayout.removeView(find.playCallBean.videoLayout)
//    }
//
//    fun release() {
//        scope.cancel()
//        publishCallBean?.release()
//        publishCallBean = null
//        playSessionList.forEach { bean ->
//            bean.playCallBean.release()
//        }
//        playSessionList.clear()
//        audioDeviceModule.release()
//        peerConnectionFactory.dispose()
//    }
//
//    /**
//     * 挂断
//     * @param isRefuse true:拒接，false:正常挂断
//     */
//    private fun hangUp(isRefuse: Boolean) {
//        hangUpCall?.invoke(isRefuse)
//    }
//
//    private fun publish(
//        publishUrl: String,
//        width: Int,
//        height: Int,
//        framerate: Int,
//        success: () -> Unit,
//        error: (Throwable) -> Unit
//    ) {
//        val videoLayout = CustomVideoLayout(context)
//        videoLayout.init(eglBaseContext, null, true)
//        nineGridLayout.addView(videoLayout, 0)
//        val createAudioSource = peerConnectionFactory.createAudioSource(createAudioConstraints())
//        val audioTrack =
//            peerConnectionFactory.createAudioTrack("local_audio_track", createAudioSource)
//
//        val videoCapture = createVideoCapture(context)
//        var videoTrack: VideoTrack? = null
//        var surfaceTextureHelper: SurfaceTextureHelper? = null
//        videoCapture?.let { capture ->
//            val videoSource = peerConnectionFactory.createVideoSource(capture.isScreencast)
//
//            surfaceTextureHelper =
//                SurfaceTextureHelper.create("surface_texture_thread", eglBaseContext)
//            capture.initialize(surfaceTextureHelper, context, videoSource.capturerObserver)
//            capture.startCapture(width, height, framerate)
//            videoTrack =
//                peerConnectionFactory.createVideoTrack("local_video_track", videoSource).apply {
//                    videoLayout.addVideoStream(this)
//                }
//        }
//
//        val rtcConfig = PeerConnection.RTCConfiguration(emptyList())
//        // 这里不能用PLAN_B 会报错
//        rtcConfig.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
//
//        peerConnectionFactory.createPeerConnection(
//            rtcConfig,
//            object : PeerConnectionObserver() {
//                override fun onConnectionChange(newState: PeerConnection.PeerConnectionState) {
//                    super.onConnectionChange(newState)
//                    if (newState == PeerConnection.PeerConnectionState.DISCONNECTED
//                        || newState == PeerConnection.PeerConnectionState.FAILED
//                    ) {
//                        post { hangUp(false) }
//                    }
//                }
//            }
//        )?.apply {
//            if (videoTrack != null) {
//                addTransceiver(
//                    videoTrack,
//                    RtpTransceiver.RtpTransceiverInit(RtpTransceiver.RtpTransceiverDirection.SEND_ONLY)
//                )
//            }
//            addTransceiver(
//                audioTrack,
//                RtpTransceiver.RtpTransceiverInit(RtpTransceiver.RtpTransceiverDirection.SEND_ONLY)
//            )
//            createOffer(object : SdpAdapter("createOffer") {
//                override fun onCreateSuccess(description: SessionDescription?) {
//                    super.onCreateSuccess(description)
//                    description?.let {
//                        if (it.type == SessionDescription.Type.OFFER) {
//                            val offerSdp = it.description
//                            setLocalDescription(SdpAdapter("setLocalDescription"), it)
//
//                            val srsBean = SrsRequestBean(
//                                it.description,
//                                publishUrl
//                            )
//
//                            //请求srs
//                            launch {
//                                runCatching {
//                                    withContext(Dispatchers.IO) {
//                                        retrofitClient.getApiService().publish(
//                                            "$mSrsBaseUrl${Constant.SRS.PUBLISH_URL}",
//                                            srsBean
//                                        )
//                                    }
//                                }.onSuccess { bean ->
//                                    if (bean.isSuccess) {
//                                        val remoteSdp = SessionDescription(
//                                            SessionDescription.Type.ANSWER,
//                                            convertAnswerSdp(offerSdp, bean.sdp)
//                                        )
//                                        setRemoteDescription(
//                                            object : SdpAdapter("publish-setRemoteDescription") {
//                                                override fun onSetSuccess() {
//                                                    super.onSetSuccess()
//                                                    success()
//                                                }
//
//                                                override fun onSetFailure(str: String?) {
//                                                    super.onSetFailure(str)
//                                                    error.invoke(Exception("publish-setRemoteDescription failure:${str}"))
//                                                }
//                                            },
//                                            remoteSdp
//                                        )
//                                    } else {
//                                        context.toastWarning("推流请求失败，code：${bean.code}")
//                                        XLog.w("推流请求失败，code：${bean.code}")
//                                        error.invoke(Exception("推流请求失败，code：${bean.code}"))
//                                    }
//                                }.onFailure { e ->
//                                    XLog.e("推流请求Error：${e.message}")
//                                    context.toastError("推流请求Error：${e.message}")
//                                    error.invoke(e)
//                                }
//                            }
//                        }
//                    }
//                }
//            }, offerOrAnswerConstraint(false))
//
//            publishCallBean = PublishCallBean(
//                publishUrl,
//                videoLayout,
//                this,
//                videoTrack,
//                videoCapture,
//                surfaceTextureHelper
//            )
//        }
//    }
//
//    private fun pullStream(
//        peerConnection: PeerConnection,
//        playUrl: String,
//        success: () -> Unit,
//        error: (Throwable) -> Unit
//    ) {
//        peerConnection.createOffer(object : SdpAdapter("createOffer") {
//            override fun onCreateSuccess(description: SessionDescription?) {
//                super.onCreateSuccess(description)
//                description?.let {
//                    if (it.type == SessionDescription.Type.OFFER) {
//                        val offerSdp = it.description
//                        peerConnection.setLocalDescription(SdpAdapter("setLocalDescription"), it)
//                        val srsBean = SrsRequestBean(it.description, playUrl)
//                        //请求srs
//                        launch {
//                            runCatching {
//                                withContext(Dispatchers.IO) {
//                                    retrofitClient.getApiService()
//                                        .play("$mSrsBaseUrl${Constant.SRS.PLAY_URL}", srsBean)
//                                }
//                            }.onSuccess { bean ->
//                                if (bean.isSuccess) {
//                                    val remoteSdp = SessionDescription(
//                                        SessionDescription.Type.ANSWER,
//                                        convertAnswerSdp(offerSdp, bean.sdp)
//                                    )
//                                    peerConnection.setRemoteDescription(
//                                        object : SdpAdapter("play-setRemoteDescription") {
//                                            override fun onSetSuccess() {
//                                                super.onSetSuccess()
//                                                success()
//                                            }
//
//                                            override fun onSetFailure(str: String?) {
//                                                super.onSetFailure(str)
//                                                error.invoke(Exception("play-setRemoteDescription failure:${str}"))
//                                            }
//                                        },
//                                        remoteSdp
//                                    )
//                                } else {
//                                    context.toastWarning("拉流请求失败，code：${bean.code}")
//                                    XLog.w("拉流请求失败，code：${bean.code}")
//                                }
//                            }.onFailure { e ->
//                                XLog.e("拉流请求Error：${e.message}")
//                                context.toastError("拉流请求Error：${e.message}")
//                            }
//                        }
//                    }
//                }
//            }
//        }, offerOrAnswerConstraint(true))
//    }
//
//    private fun createPlayPeerConnection(
//        bean: PlaySessionBean, success: () -> Unit, error: (Throwable) -> Unit
//    ) {
//        val videoLayout = CustomVideoLayout(context)
//        videoLayout.init(eglBaseContext, bean.terName, false)
//        nineGridLayout.addView(videoLayout)
//
//        val rtcConfig = PeerConnection.RTCConfiguration(emptyList())
//        rtcConfig.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
//        peerConnectionFactory.createPeerConnection(
//            rtcConfig,
//            object : PeerConnectionObserver() {
//                override fun onAddStream(mediaStream: MediaStream?) {
//                    super.onAddStream(mediaStream)
//                    mediaStream?.let {
//                        if (it.videoTracks.isEmpty().not()) {
//                            videoLayout.addVideoStream(it.videoTracks[0])
//                        }
//                    }
//                }
//
//                override fun onConnectionChange(newState: PeerConnection.PeerConnectionState) {
//                    super.onConnectionChange(newState)
//                    if (newState == PeerConnection.PeerConnectionState.DISCONNECTED
//                        || newState == PeerConnection.PeerConnectionState.FAILED
//                    ) {
//                        post { hangUp(false) }
//                    }
//                }
//            })?.apply {
//            addTransceiver(
//                MediaStreamTrack.MediaType.MEDIA_TYPE_VIDEO,
//                RtpTransceiver.RtpTransceiverInit(RtpTransceiver.RtpTransceiverDirection.RECV_ONLY)
//            )
//            addTransceiver(
//                MediaStreamTrack.MediaType.MEDIA_TYPE_AUDIO,
//                RtpTransceiver.RtpTransceiverInit(RtpTransceiver.RtpTransceiverDirection.RECV_ONLY)
//            )
//
//            bean.playCallBean = PlayCallBean(videoLayout, this)
//            playSessionList.add(bean)
//
//            if (bean.webrtcUrl.isNotBlank()) {
//                //如果不为空，直接拉流
//                pullStream(this, bean.webrtcUrl, success, error)
//            }
//        }
//    }
//
//    /**
//     * 转换AnswerSdp
//     * @param offerSdp offerSdp：创建offer时生成的sdp
//     * @param answerSdp answerSdp：网络请求srs服务器返回的sdp
//     * @return 转换后的AnswerSdp
//     */
//    private fun convertAnswerSdp(offerSdp: String, answerSdp: String?): String {
//        if (answerSdp.isNullOrBlank()) {
//            return ""
//        }
//        val indexOfOfferVideo = offerSdp.indexOf("m=video")
//        val indexOfOfferAudio = offerSdp.indexOf("m=audio")
//        if (indexOfOfferVideo == -1 || indexOfOfferAudio == -1) {
//            return answerSdp
//        }
//        val indexOfAnswerVideo = answerSdp.indexOf("m=video")
//        val indexOfAnswerAudio = answerSdp.indexOf("m=audio")
//        if (indexOfAnswerVideo == -1 || indexOfAnswerAudio == -1) {
//            return answerSdp
//        }
//
//        val isFirstOfferVideo = indexOfOfferVideo < indexOfOfferAudio
//        val isFirstAnswerVideo = indexOfAnswerVideo < indexOfAnswerAudio
//        return if (isFirstOfferVideo == isFirstAnswerVideo) {
//            //顺序一致
//            answerSdp
//        } else {
//            //需要调换顺序
//            buildString {
//                append(answerSdp.substring(0, indexOfAnswerVideo.coerceAtMost(indexOfAnswerAudio)))
//                append(
//                    answerSdp.substring(
//                        indexOfAnswerVideo.coerceAtLeast(indexOfOfferVideo),
//                        answerSdp.length
//                    )
//                )
//                append(
//                    answerSdp.substring(
//                        indexOfAnswerVideo.coerceAtMost(indexOfAnswerAudio),
//                        indexOfAnswerVideo.coerceAtLeast(indexOfOfferVideo)
//                    )
//                )
//            }
//        }
//    }
//
//    private fun createAudioConstraints(): MediaConstraints {
//        val audioConstraints = MediaConstraints()
//        //回声消除
//        audioConstraints.mandatory.add(
//            MediaConstraints.KeyValuePair(
//                "googEchoCancellation",
//                "true"
//            )
//        )
//        //自动增益
//        audioConstraints.mandatory.add(MediaConstraints.KeyValuePair("googAutoGainControl", "true"))
//        //高音过滤
//        audioConstraints.mandatory.add(MediaConstraints.KeyValuePair("googHighpassFilter", "true"))
//        //噪音处理
//        audioConstraints.mandatory.add(
//            MediaConstraints.KeyValuePair(
//                "googNoiseSuppression",
//                "true"
//            )
//        )
//        return audioConstraints
//    }
//
//    private fun offerOrAnswerConstraint(isReceive: Boolean): MediaConstraints {
//        val mediaConstraints = MediaConstraints()
//        mediaConstraints.mandatory.add(
//            MediaConstraints.KeyValuePair(
//                "OfferToReceiveAudio",
//                isReceive.toString()
//            )
//        )
//        mediaConstraints.mandatory.add(
//            MediaConstraints.KeyValuePair(
//                "OfferToReceiveVideo",
//                isReceive.toString()
//            )
//        )
//        mediaConstraints.mandatory.add(
//            MediaConstraints.KeyValuePair("googCpuOveruseDetection", "true")
//        )
//        return mediaConstraints
//    }
//
//    private fun createVideoCapture(context: Context): CameraVideoCapturer? {
//        val enumerator: CameraEnumerator = if (Camera2Enumerator.isSupported(context)) {
//            Camera2Enumerator(context)
//        } else {
//            Camera1Enumerator()
//        }
//
//        for (name in enumerator.deviceNames) {
//            if (enumerator.isFrontFacing(name)) {
//                return enumerator.createCapturer(name, cameraHandler)
//            }
//        }
//        for (name in enumerator.deviceNames) {
//            if (enumerator.isBackFacing(name)) {
//                return enumerator.createCapturer(name, cameraHandler)
//            }
//        }
//        return null
//    }
//
//    private fun operateSpeakerphone() {
//        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
//
//        if (isSpeakerphone) {
//            //之前打开的，关闭
//            //设置音量，解决有些机型切换后没声音或者声音突然变大的问题
//            audioManager.setStreamVolume(
//                AudioManager.STREAM_VOICE_CALL,
//                audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL),
//                AudioManager.FX_KEY_CLICK
//            )
//            audioManager.isSpeakerphoneOn = false
//        } else {
//            //之前关闭的，打开
//            audioManager.setStreamVolume(
//                AudioManager.STREAM_VOICE_CALL,
//                audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL),
//                AudioManager.FX_KEY_CLICK
//            )
//            audioManager.isSpeakerphoneOn = true
//        }
//        isSpeakerphone = isSpeakerphone.not()
//        tvInCallSpeakerphone.isSelected = isSpeakerphone
//    }
//}
//
//internal open class CallBean(
//    val videoLayout: CustomVideoLayout,
//    val peerConnection: PeerConnection
//) {
//
//    @CallSuper
//    internal open fun release() {
//        videoLayout.release()
//        peerConnection.dispose()
//    }
//}
//
//internal class PublishCallBean(
//    val webrtcUrl: String,
//    videoLayout: CustomVideoLayout,
//    peerConnection: PeerConnection,
//    val videoTrack: VideoTrack?,
//    val cameraVideoCapturer: CameraVideoCapturer?,
//    val surfaceTextureHelper: SurfaceTextureHelper?
//) : CallBean(videoLayout, peerConnection) {
//
//    /**
//     * 切换摄像头
//     * @param switchDone 切换成功，结果回调在子线程
//     * @param switchError 切换失败，结果回调在子线程
//     */
//    internal inline fun switchCamera(
//        crossinline switchDone: (isFrontCamera: Boolean) -> Unit = { _ -> },
//        crossinline switchError: (errorDescription: String?) -> Unit = { _ -> }
//    ) {
//        cameraVideoCapturer?.switchCamera(object : CameraVideoCapturer.CameraSwitchHandler {
//
//            override fun onCameraSwitchDone(isFrontCamera: Boolean) {
//                switchDone.invoke(isFrontCamera)
//            }
//
//            override fun onCameraSwitchError(errorDescription: String?) {
//                switchError.invoke(errorDescription)
//            }
//        }) ?: let {
//            switchError.invoke("cameraVideoCapturer is null.")
//        }
//    }
//
//    override fun release() {
//        videoTrack?.dispose()
//        cameraVideoCapturer?.dispose()
//        surfaceTextureHelper?.dispose()
//        super.release()
//    }
//}
//
//internal class PlayCallBean(
//    videoLayout: CustomVideoLayout,
//    peerConnection: PeerConnection
//) : CallBean(videoLayout, peerConnection)
