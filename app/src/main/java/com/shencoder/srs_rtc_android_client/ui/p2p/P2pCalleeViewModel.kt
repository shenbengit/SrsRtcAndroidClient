package com.shencoder.srs_rtc_android_client.ui.p2p

import android.app.Application
import android.media.MediaPlayer
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.elvishew.xlog.XLog
import com.shencoder.mvvmkit.base.repository.BaseNothingRepository
import com.shencoder.mvvmkit.base.viewmodel.BaseViewModel
import com.shencoder.mvvmkit.ext.launchOnUIDelay
import com.shencoder.mvvmkit.ext.toastInfo
import com.shencoder.mvvmkit.ext.toastWarning
import com.shencoder.srs_rtc_android_client.R
import com.shencoder.srs_rtc_android_client.helper.call.CallSocketIoClient
import com.shencoder.srs_rtc_android_client.helper.call.SignalEventCallback
import com.shencoder.srs_rtc_android_client.helper.call.SocketIoConnectionStatusCallback
import com.shencoder.srs_rtc_android_client.helper.call.bean.HangUpBean
import com.shencoder.srs_rtc_android_client.helper.call.bean.OfflineDuringCallBean
import com.shencoder.srs_rtc_android_client.helper.call.bean.P2pReceiveIceBean
import com.shencoder.srs_rtc_android_client.helper.call.bean.P2pReceiveSdpBean
import com.shencoder.srs_rtc_android_client.helper.call.bean.ResAlreadyInRoomBean
import org.koin.core.component.inject


/**
 *
 * @author Shenben
 * @date 2023/11/2 16:39
 * @description
 * @since
 */
class P2pCalleeViewModel(
    application: Application,
    repo: BaseNothingRepository
) : BaseViewModel<BaseNothingRepository>(application, repo) {

    private val callSocketIoClient: CallSocketIoClient by inject()

    private val mediaPlayer = MediaPlayer.create(applicationContext, R.raw.bell).apply {
        isLooping = true
    }

    private var roomId = ""

    val receiveOfferLiveData = MutableLiveData<String>()
    val receiveIceLiveData = MutableLiveData<P2pReceiveIceBean.Ice>()

    /**
     * 添加连接状态回调
     */
    private val connectionStatusCallback = object : SocketIoConnectionStatusCallback {

        override fun disconnected() {
            toastWarning("disconnect signal server.")
            delayBackPressed()
        }
    }

    private val signalEventCallback = object : SignalEventCallback {
        /**
         * 被强制下线
         */
        override fun forcedOffline() {
            toastWarning("forced offline.")
            delayBackPressed()
        }

        override fun p2pReceiveOffer(bean: P2pReceiveSdpBean) {
            receiveOfferLiveData.value = bean.sdp
        }

        override fun p2pReceiveIce(bean: P2pReceiveIceBean) {
            receiveIceLiveData.value = bean.ice
        }

        override fun p2pHangUp(bean: HangUpBean) {
            toastInfo("[${bean.userInfo.username}]${getString(R.string.hang_up)}")
            delayBackPressed()
        }

        override fun p2pOfflineDuringCall(bean: OfflineDuringCallBean) {
            toastInfo("[${bean.userInfo.username}]${getString(R.string.offline)}")
            delayBackPressed()
        }
    }

    override fun onCreate(owner: LifecycleOwner) {
        callSocketIoClient.addConnectionStatusCallback(connectionStatusCallback)
        callSocketIoClient.addSignalEventCallback(signalEventCallback)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        callSocketIoClient.reqP2pResetStatus()
        callSocketIoClient.removeConnectionStatusCallback(connectionStatusCallback)
        callSocketIoClient.removeSignalEventCallback(signalEventCallback)

        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        mediaPlayer.reset()
        mediaPlayer.release()
    }

    fun setRoomId(roomId: String) {
        this.roomId = roomId
        mediaPlayer.start()
    }

    fun reqP2pRejectCall() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        callSocketIoClient.reqP2pRejectCall(roomId, {
            toastInfo(getString(R.string.call_ended))
            delayBackPressed()
        }, { code, reason ->
            XLog.e("reject call failure-code:${code}, reason:${reason}")
            toastWarning(reason)
            delayBackPressed()
        })
    }

    fun reqP2pAcceptCall(success: (ResAlreadyInRoomBean) -> Unit = {}) {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        callSocketIoClient.reqP2pAcceptCall(roomId, success) { _, _ ->

        }
    }

    fun reqP2pSendAnswer(sdp: String, success: () -> Unit = {}) {
        callSocketIoClient.reqP2pSendAnswer(roomId, sdp, success) { _, _ ->

        }
    }

    fun reqP2pSendIce(ice: P2pReceiveIceBean.Ice, success: () -> Unit = {}) {
        callSocketIoClient.reqP2pSendIce(roomId, ice, success) { _, _ ->

        }
    }

    fun reqP2pHangUp() {
        callSocketIoClient.reqP2pHangUp(roomId, success = {
            toastInfo(getString(R.string.call_ended))
            delayBackPressed()
        },
            failure = { code, reason ->
                XLog.e("hang up failure-code:${code}, reason:${reason}")
                toastWarning(reason)
                delayBackPressed()
            })
    }

    fun delayBackPressed(timeMillis: Long = 1000L) {
        launchOnUIDelay(timeMillis) {
            backPressed()
        }
    }
}