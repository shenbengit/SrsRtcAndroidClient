package com.shencoder.srs_rtc_android_client.ui.p2p

import android.os.Bundle
import com.shencoder.mvvmkit.util.toastError
import com.shencoder.mvvmkit.util.toastWarning
import com.shencoder.srs_rtc_android_client.BR
import com.shencoder.srs_rtc_android_client.R
import com.shencoder.srs_rtc_android_client.base.BaseActivity
import com.shencoder.srs_rtc_android_client.constant.CallRoleType
import com.shencoder.srs_rtc_android_client.databinding.ActivityP2pCalleeBinding
import com.shencoder.srs_rtc_android_client.helper.call.bean.P2pReceiveIceBean
import com.shencoder.srs_rtc_android_client.helper.call.bean.P2pRequestCallBean
import com.shencoder.srs_rtc_android_client.ui.callee_chat.CalleeChatActivity
import com.shencoder.srs_rtc_android_client.util.requestCallPermissions
import com.shencoder.srs_rtc_android_client.webrtc.p2p.P2PPeerConnectionFactory
import com.shencoder.srs_rtc_android_client.webrtc.p2p.P2PSessionManager
import com.shencoder.srs_rtc_android_client.webrtc.widget.P2PCallLayout
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * P2P聊天 被叫页面
 *
 * @constructor Create empty P2p callee activity
 */
class P2pCalleeActivity : BaseActivity<P2pCalleeViewModel, ActivityP2pCalleeBinding>() {

    companion object {
        const val REQUEST_CALL = "REQUEST_CALL"
    }

    private lateinit var sessionManager: P2PSessionManager

    override fun getLayoutId(): Int {
        return R.layout.activity_p2p_callee
    }

    override fun getViewModelId(): Int {
        return BR.viewModel
    }

    override fun injectViewModel(): Lazy<P2pCalleeViewModel> {
        return viewModel()
    }

    override fun initView() {
        mBinding.callLayout.setCallActionCallback(object : P2PCallLayout.CallActionCallback {
            override fun rejectCall() {
                mViewModel.reqP2pRejectCall()
            }

            override fun acceptCall() {
                mViewModel.reqP2pAcceptCall {

                }
            }

            override fun hangUpCall() {
                mViewModel.reqP2pHangUp()
            }

            override fun setMicrophoneMute(mute: Boolean) {
                sessionManager.setMicrophoneMute(mute)
            }

            override fun operateSpeakerphone(isSpeakerphone: Boolean) {
                sessionManager.operateSpeakerphone(isSpeakerphone)
            }

            override fun switchCamera() {
                sessionManager.switchCamera()
            }
        })
    }

    override fun initData(savedInstanceState: Bundle?) {
        val requestCallBean =
            intent?.getParcelableExtra<P2pRequestCallBean>(CalleeChatActivity.REQUEST_CALL)
        if (requestCallBean == null) {
            toastError("no request call info.")
            mViewModel.delayBackPressed()
            return
        }
        val callType = requestCallBean.callType
        val peerConnectionFactory = P2PPeerConnectionFactory(this, callType)
        sessionManager =
            P2PSessionManager(
                this,
                peerConnectionFactory,
                callType,
                CallRoleType.CALLEE
            ).apply {
                onRemoteVideoTrack = { track ->
                    mBinding.callLayout.setRemoteVideoTrack(track)
                }
                onIceCandidate = {
                    mViewModel.reqP2pSendIce(
                        P2pReceiveIceBean.Ice(
                            it.sdpMid,
                            it.sdpMLineIndex,
                            it.sdp
                        )
                    )
                }
            }

        mBinding.callLayout.init(
            peerConnectionFactory.eglBaseContext,
            callType,
            CallRoleType.CALLEE
        )

        mViewModel.run {

            receiveOfferLiveData.observe(this@P2pCalleeActivity) { sdp ->
                sessionManager.createAnswer(sdp) { answerSdp ->
                    mViewModel.reqP2pSendAnswer(answerSdp.description)
                }
            }

            receiveIceLiveData.observe(this@P2pCalleeActivity) { ice ->
                sessionManager.addIceCandidate(ice.sdpMid, ice.sdpMLineIndex, ice.sdp)
            }
        }
        requestCallPermissions { allGranted ->
            if (allGranted.not()) {
                toastWarning("Permission not granted.")
                mViewModel.delayBackPressed()
                return@requestCallPermissions
            }

            mViewModel.setRoomId(requestCallBean.roomId)
            sessionManager.startCapture { _, videoTrack ->
                mBinding.callLayout.setLocalVideoTrack(videoTrack)
            }
        }
    }

    override fun onDestroy() {
        mBinding.callLayout.release()
        if (this::sessionManager.isInitialized) {
            sessionManager.release()
        }
        super.onDestroy()
    }
}