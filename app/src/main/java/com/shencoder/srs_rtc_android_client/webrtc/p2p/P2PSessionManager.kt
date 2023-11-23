package com.shencoder.srs_rtc_android_client.webrtc.p2p

import android.content.Context
import android.media.AudioManager
import com.shencoder.srs_rtc_android_client.constant.CallRoleType
import com.shencoder.srs_rtc_android_client.constant.CallType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.webrtc.AudioTrack
import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.MediaStreamTrack
import org.webrtc.SessionDescription
import org.webrtc.VideoTrack
import java.nio.ByteBuffer


/**
 *
 * @author Shenben
 * @date 2023/11/9 10:38
 * @description
 * @since
 */
class P2PSessionManager(
    private val context: Context,
    private val peerConnectionFactory: P2PPeerConnectionFactory,
    private val callType: CallType,
    private val callRoleType: CallRoleType
) {
    private val sessionManagerScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val audioManager: AudioManager =
        context.applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val originSpeakerphoneOn = audioManager.isSpeakerphoneOn

    var onAddStream: ((MediaStream) -> Unit)? = null
    var onRemoteVideoTrack: ((VideoTrack) -> Unit)? = null
    var onIceCandidate: ((IceCandidate) -> Unit)? = null
    var onDataChannel: ((DataChannel) -> Unit)? = null

    private val peerConnection by lazy {
        peerConnectionFactory.createPeerConnection(
            sessionManagerScope,
            callType,
            callRoleType,
            onRemoteTrack = {
                val track = it.receiver.track()
                if (track?.kind() == MediaStreamTrack.VIDEO_TRACK_KIND) {
                    val videoTrack = track as VideoTrack
                    onRemoteVideoTrack?.invoke(videoTrack)
                }
            },
            onIceCandidate = { iceCandidate, callRoleType ->
                onIceCandidate?.invoke(iceCandidate)
            },
            onDataChannel = {
                onDataChannel?.invoke(it)
            }
        )
    }

    fun createDataChannel(label: String, init: DataChannel.Init): DataChannel {
        return peerConnection.createDataChannel(label, init)
    }

    fun dataChannelSendMsg(dataChannel: DataChannel, msg: String) {
        dataChannel.send(DataChannel.Buffer(ByteBuffer.wrap(msg.toByteArray()), false))
    }

    fun dataChannelSendBinary(dataChannel: DataChannel, byteBuffer: ByteArray) {
        dataChannel.send(DataChannel.Buffer(ByteBuffer.wrap(byteBuffer), true))
    }

    fun getMsgFromDataChannelBuffer(buffer: DataChannel.Buffer): String {
        return if (!buffer.binary) {
            val data = buffer.data
            val byteArray = ByteArray(data.capacity())
            data.get(byteArray)
            String(byteArray)
        } else "binary data"
    }

    fun startCapture(callback: ((AudioTrack, VideoTrack?) -> Unit)? = null) {
        peerConnectionFactory.startCapture(peerConnection.connection, callback)
    }

    fun createOffer(offerSdp: (SessionDescription) -> Unit) {
        sessionManagerScope.launch {
            val offer = peerConnection.createOffer().getOrThrow()
            val result = peerConnection.setLocalDescription(offer)
            result.fold({
                offerSdp(offer)
            }, {

            })
        }
    }

    fun createAnswer(offerSdp: String, answerSdp: (SessionDescription) -> Unit) {
        sessionManagerScope.launch {
            peerConnection.setRemoteDescription(
                SessionDescription(
                    SessionDescription.Type.OFFER,
                    offerSdp
                )
            )
            val answer = peerConnection.createAnswer().getOrThrow()
            val result = peerConnection.setLocalDescription(answer)
            result.fold({
                answerSdp(answer)
            }, {

            })
        }
    }

    fun receiveAnswer(answerSdp: String) {
        sessionManagerScope.launch {
            peerConnection.setRemoteDescription(
                SessionDescription(
                    SessionDescription.Type.ANSWER,
                    answerSdp
                )
            )
        }
    }

    fun addIceCandidate(sdpMid: String, sdpMLineIndex: Int, sdp: String) {
        sessionManagerScope.launch {
            peerConnection.addIceCandidate(IceCandidate(sdpMid, sdpMLineIndex, sdp))
        }
    }

    fun setMicrophoneMute(mute: Boolean) {
        peerConnectionFactory.audioDeviceModule.setMicrophoneMute(mute)
    }

    fun switchCamera() {
        peerConnectionFactory.switchCamera()
    }

    /**
     * 操作扬声器
     */
    fun operateSpeakerphone(isSpeakerphone: Boolean) {
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
    }

    fun release() {
        operateSpeakerphone(originSpeakerphoneOn)
        peerConnection.release()
        peerConnectionFactory.release()
    }

}