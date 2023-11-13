package com.shencoder.srs_rtc_android_client.ui.p2p

import android.os.Bundle
import com.shencoder.mvvmkit.util.toastWarning
import com.shencoder.srs_rtc_android_client.BR
import com.shencoder.srs_rtc_android_client.R
import com.shencoder.srs_rtc_android_client.base.BaseActivity
import com.shencoder.srs_rtc_android_client.constant.CallRoleType
import com.shencoder.srs_rtc_android_client.constant.CallType
import com.shencoder.srs_rtc_android_client.databinding.ActivityP2pCallerBinding
import com.shencoder.srs_rtc_android_client.helper.call.bean.P2pReceiveIceBean
import com.shencoder.srs_rtc_android_client.http.bean.UserInfoBean
import com.shencoder.srs_rtc_android_client.util.requestCallPermissions
import com.shencoder.srs_rtc_android_client.webrtc.p2p.P2PPeerConnectionFactory
import com.shencoder.srs_rtc_android_client.webrtc.p2p.P2PSessionManager
import com.shencoder.srs_rtc_android_client.webrtc.widget.P2PCallLayout
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * P2P聊天，主叫页面
 *
 * @constructor Create empty P2p caller activity
 */
class P2pCallerActivity : BaseActivity<P2pCallerViewModel, ActivityP2pCallerBinding>() {

    companion object {
        /**
         * 被叫信息
         */
        const val CALLEE_INFO_LIST = "CALLEE_INFO_LIST"

        /**
         * 通话类型
         */
        const val CALL_TYPE = "CALL_TYPE"
    }

    private lateinit var sessionManager: P2PSessionManager
    override fun getLayoutId(): Int {
        return R.layout.activity_p2p_caller
    }

    override fun getViewModelId(): Int {
        return BR.viewModel
    }

    override fun injectViewModel(): Lazy<P2pCallerViewModel> {
        return viewModel()
    }

    override fun initView() {
        mBinding.callLayout.setCallActionCallback(object : P2PCallLayout.CallActionCallback {
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
        val callType = intent.getParcelableExtra<CallType>(CALL_TYPE) ?: CallType.Video
        val list: ArrayList<UserInfoBean> =
            intent?.getParcelableArrayListExtra(CALLEE_INFO_LIST) ?: ArrayList()

        val peerConnectionFactory = P2PPeerConnectionFactory(this, callType)
        sessionManager =
            P2PSessionManager(this, peerConnectionFactory, callType, CallRoleType.CALLER).apply {
                onAddStream = { stream ->
                    val videoTracks = stream.videoTracks
                    if (videoTracks.isNotEmpty()) {
                        mBinding.callLayout.setRemoteVideoTrack(videoTracks[0])
                    }
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
            CallRoleType.CALLER
        )

        mViewModel.run {
            acceptCallLiveData.observe(this@P2pCallerActivity) {
                mBinding.callLayout.setInCallStatus()
                sessionManager.createOffer {
                    reqP2pSendOffer(it.description)
                }
            }
            receiveAnswerLiveData.observe(this@P2pCallerActivity) { sdp ->
                sessionManager.receiveAnswer(sdp)
            }

            receiveIceLiveData.observe(this@P2pCallerActivity) { ice ->
                sessionManager.addIceCandidate(ice.sdpMid, ice.sdpMLineIndex, ice.sdp)
            }
        }
        requestCallPermissions { allGranted ->
            if (!allGranted) {
                toastWarning("Permission not granted.")
                mViewModel.delayBackPressed()
                return@requestCallPermissions
            }
            if (list.size == 1) {
                val calleeUserInfo = list[0]
                mViewModel.reqP2pInviteSomeone(calleeUserInfo.userId, callType) {

                }
            } else {
                mViewModel.delayBackPressed()
            }

            sessionManager.startCapture { _, videoTrack ->
                mBinding.callLayout.setLocalVideoTrack(videoTrack)
            }
        }
    }

    override fun onDestroy() {
        mBinding.callLayout.release()
        sessionManager.release()
        super.onDestroy()
    }
}