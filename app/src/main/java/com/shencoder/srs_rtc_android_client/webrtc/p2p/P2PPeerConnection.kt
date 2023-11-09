package com.shencoder.srs_rtc_android_client.webrtc.p2p

import com.shencoder.srs_rtc_android_client.constant.CallRoleType
import com.shencoder.srs_rtc_android_client.constant.CallType
import com.shencoder.webrtcextension.ext.createSessionDescription
import com.shencoder.webrtcextension.ext.suspendSdpObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.SessionDescription
import java.lang.RuntimeException


/**
 *
 * @author Shenben
 * @date 2023/11/8 9:58
 * @description
 * @since
 */
class P2PPeerConnection(
    private val coroutineScope: CoroutineScope,
    private val callType: CallType,
    private val roleType: CallRoleType,
    private val onAddStream: ((MediaStream) -> Unit)?,
    private val onNegotiationNeeded: ((P2PPeerConnection, CallRoleType) -> Unit)?,
    private val onIceCandidate: ((IceCandidate, CallRoleType) -> Unit)?,
    private val onDataChannel: ((DataChannel) -> Unit)?,
) : PeerConnection.Observer {

    lateinit var connection: PeerConnection

    /**
     * Used to pool together and store [IceCandidate]s before consuming them.
     */
    private val pendingIceMutex = Mutex()
    private val pendingIceCandidates = mutableListOf<IceCandidate>()

    private val mediaConstraints = MediaConstraints().apply {
        mandatory.addAll(
            listOf(
                MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"),
                MediaConstraints.KeyValuePair(
                    "OfferToReceiveVideo",
                    if (callType == CallType.Video) "true" else "false"
                ),
            ),
        )
    }

    fun initialize(peerConnection: PeerConnection) {
        this.connection = peerConnection
    }

    fun createDataChannel(label: String, init: DataChannel.Init): DataChannel {
        return connection.createDataChannel(label, init)
    }

    suspend fun createOffer(): Result<SessionDescription> {
        return createSessionDescription { connection.createOffer(it, mediaConstraints) }
    }

    suspend fun createAnswer(): Result<SessionDescription> {
        return createSessionDescription { connection.createAnswer(it, mediaConstraints) }
    }

    suspend fun setLocalDescription(sessionDescription: SessionDescription): Result<Unit> {
        return suspendSdpObserver { connection.setLocalDescription(it, sessionDescription) }
    }

    suspend fun setRemoteDescription(sessionDescription: SessionDescription): Result<Unit> {
        return suspendSdpObserver { connection.setRemoteDescription(it, sessionDescription) }.also {
            if (it.isSuccess) {
                pendingIceMutex.withLock {
                    pendingIceCandidates.forEach { iceCandidate ->
                        connection.addIceCandidate(iceCandidate)
                    }
                    pendingIceCandidates.clear()
                }
            }
        }
    }

    suspend fun addIceCandidate(iceCandidate: IceCandidate): Result<Unit> {
        if (connection.remoteDescription == null) {
            pendingIceMutex.withLock {
                pendingIceCandidates.add(iceCandidate)
            }
            return Result.failure(RuntimeException("RemoteDescription is not set"))
        }
        connection.addIceCandidate(iceCandidate)
        return Result.success(Unit)
    }

    fun release() {
        connection.dispose()
    }

    override fun onSignalingChange(newState: PeerConnection.SignalingState?) {

    }

    override fun onIceConnectionChange(newState: PeerConnection.IceConnectionState?) {

    }

    override fun onConnectionChange(newState: PeerConnection.PeerConnectionState) {

    }

    override fun onIceConnectionReceivingChange(receiving: Boolean) {

    }

    override fun onIceGatheringChange(newState: PeerConnection.IceGatheringState?) {

    }

    override fun onIceCandidate(candidate: IceCandidate) {
        onIceCandidate?.invoke(candidate, roleType)
    }

    override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>?) {

    }

    override fun onAddStream(stream: MediaStream) {
        onAddStream?.invoke(stream)
    }

    override fun onRemoveStream(stream: MediaStream) {

    }

    override fun onDataChannel(dataChannel: DataChannel) {
        onDataChannel?.invoke(dataChannel)
    }

    override fun onRenegotiationNeeded() {
        onNegotiationNeeded?.invoke(this, roleType)
    }

}