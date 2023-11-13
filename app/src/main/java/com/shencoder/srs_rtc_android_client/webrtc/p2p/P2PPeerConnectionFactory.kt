package com.shencoder.srs_rtc_android_client.webrtc.p2p

import android.content.Context
import com.shencoder.srs_rtc_android_client.constant.CallRoleType
import com.shencoder.srs_rtc_android_client.constant.CallType
import com.shencoder.srs_rtc_android_client.constant.isVideo
import kotlinx.coroutines.CoroutineScope
import org.webrtc.AudioTrack
import org.webrtc.Camera1Enumerator
import org.webrtc.Camera2Enumerator
import org.webrtc.CameraVideoCapturer
import org.webrtc.DataChannel
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoTrack
import org.webrtc.audio.JavaAudioDeviceModule
import java.lang.RuntimeException
import java.util.UUID


/**
 *
 * @author Shenben
 * @date 2023/11/8 17:25
 * @description
 * @since
 */
class P2PPeerConnectionFactory(private val context: Context, private val callType: CallType) {

    private val eglBase by lazy { EglBase.create() }
    val eglBaseContext: EglBase.Context by lazy { eglBase.eglBaseContext }

    private val videoDecoderFactory by lazy {
        DefaultVideoDecoderFactory(
            eglBaseContext,
        )
    }

    private val videoEncoderFactory by lazy {
        DefaultVideoEncoderFactory(eglBaseContext, true, true)
    }

    private val rtcConfig = PeerConnection.RTCConfiguration(
        arrayListOf(
            // adding google's standard server
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer(),
        ),
    )

    val audioDeviceModule by lazy {
        JavaAudioDeviceModule
            .builder(context)
            .createAudioDeviceModule()
    }

    private val factory by lazy {
        PeerConnectionFactory.builder()
            .setVideoDecoderFactory(videoDecoderFactory)
            .setVideoEncoderFactory(videoEncoderFactory)
            .setAudioDeviceModule(audioDeviceModule)
            .createPeerConnectionFactory()
    }

    private val videoCapturer by lazy { createVideoCapture(context) }
    private val surfaceTextureHelper = SurfaceTextureHelper.create(
        "SurfaceTextureHelperThread",
        eglBaseContext,
    )

    private val localVideoSource by lazy {
        factory.createVideoSource(videoCapturer.isScreencast).apply {
            videoCapturer.initialize(surfaceTextureHelper, context, this.capturerObserver)
            videoCapturer.startCapture(640, 480, 25)
        }
    }

    private val localVideoTrack by lazy {
        factory.createVideoTrack("VideoTrack: ${UUID.randomUUID()}", localVideoSource)
    }

    private val localAudioSource by lazy {
        factory.createAudioSource(buildAudioConstraints())
    }

    private val localAudioTrack by lazy {
        factory.createAudioTrack("AudioTrack: ${UUID.randomUUID()}", localAudioSource)
    }

    private var remoteMediaStream: MediaStream? = null

    fun createPeerConnection(
        coroutineScope: CoroutineScope,
        callType: CallType,
        roleType: CallRoleType,
        onAddStream: ((MediaStream) -> Unit)? = null,
        onNegotiationNeeded: ((P2PPeerConnection, CallRoleType) -> Unit)? = null,
        onIceCandidate: ((IceCandidate, CallRoleType) -> Unit)? = null,
        onDataChannel: ((DataChannel) -> Unit)? = null
    ): P2PPeerConnection {
        val peerConnection = P2PPeerConnection(
            coroutineScope,
            callType,
            roleType,
            {
                remoteMediaStream = it
                onAddStream?.invoke(it)
            },
            onNegotiationNeeded,
            onIceCandidate,
            onDataChannel
        )
        val connection = requireNotNull(factory.createPeerConnection(rtcConfig, peerConnection))
        return peerConnection.apply { initialize(connection) }
    }

    fun startCapture(
        connection: PeerConnection,
        callback: ((AudioTrack, VideoTrack?) -> Unit)? = null
    ) {
        connection.addTrack(localAudioTrack)

        val videoTrack = if (callType.isVideo()) {
            connection.addTrack(localVideoTrack)
            localVideoTrack
        } else {
            null
        }

        callback?.invoke(localAudioTrack, videoTrack)
    }

    fun switchCamera() {
        videoCapturer.switchCamera(null)
    }

    fun release() {
        audioDeviceModule.release()
        localAudioSource.dispose()
        localAudioTrack.dispose()

        if (callType.isVideo()) {
            videoCapturer.dispose()
            surfaceTextureHelper.dispose()
            localVideoSource.dispose()
            localVideoTrack.dispose()
        }

        remoteMediaStream?.dispose()

        eglBase.release()
        factory.dispose()
    }

    private fun createVideoCapture(context: Context): CameraVideoCapturer {
        //优先使用Camera2
        val enumerator =
            if (Camera2Enumerator.isSupported(context)) Camera2Enumerator(context) else Camera1Enumerator()
        val deviceNames = enumerator.deviceNames
        //前置
        for (name in deviceNames) {
            if (enumerator.isFrontFacing(name)) {
                return enumerator.createCapturer(name, null)
            }
        }
        //后置
        for (name in deviceNames) {
            if (enumerator.isBackFacing(name)) {
                return enumerator.createCapturer(name, null)
            }
        }
        throw RuntimeException("No camera available.")
    }

    private fun buildAudioConstraints(): MediaConstraints {
        val mediaConstraints = MediaConstraints()
        val items = listOf(
            // 回声消除
            MediaConstraints.KeyValuePair(
                "googEchoCancellation",
                true.toString(),
            ),
            // 自动增益
            MediaConstraints.KeyValuePair(
                "googAutoGainControl",
                true.toString(),
            ),
            MediaConstraints.KeyValuePair(
                "googHighpassFilter",
                true.toString(),
            ),
            // 噪音抑制
            MediaConstraints.KeyValuePair(
                "googNoiseSuppression",
                true.toString(),
            ),
            // 噪音检测
            MediaConstraints.KeyValuePair(
                "googTypingNoiseDetection",
                true.toString(),
            ),
        )

        return mediaConstraints.apply {
            with(optional) {
                add(MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"))
                addAll(items)
            }
        }
    }
}