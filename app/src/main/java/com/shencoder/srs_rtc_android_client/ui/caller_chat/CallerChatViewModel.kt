package com.shencoder.srs_rtc_android_client.ui.caller_chat

import android.app.Application
import android.media.MediaPlayer
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.elvishew.xlog.XLog
import com.shencoder.mvvmkit.base.repository.BaseNothingRepository
import com.shencoder.mvvmkit.base.viewmodel.BaseViewModel
import com.shencoder.mvvmkit.ext.launchOnUI
import com.shencoder.mvvmkit.ext.launchOnUIDelay
import com.shencoder.mvvmkit.ext.toastInfo
import com.shencoder.mvvmkit.ext.toastWarning
import com.shencoder.srs_rtc_android_client.R
import com.shencoder.srs_rtc_android_client.helper.call.CallSocketIoClient
import com.shencoder.srs_rtc_android_client.helper.call.SignalEventCallback
import com.shencoder.srs_rtc_android_client.helper.call.bean.*
import kotlinx.coroutines.delay
import org.koin.core.component.inject

/**
 *
 * @author  ShenBen
 * @date    2022/1/26 09:29
 * @email   714081644@qq.com
 */
class CallerChatViewModel(
    application: Application,
    repo: BaseNothingRepository
) : BaseViewModel<BaseNothingRepository>(application, repo) {

    private val callSocketIoClient: CallSocketIoClient by inject()

    val rejectCallLiveData = MutableLiveData<RejectCallBean>()
    val acceptCallLiveData = MutableLiveData<ClientInfoBean>()
    val playSteamLiveData = MutableLiveData<PlayStreamBean>()
    val hangUpLiveData = MutableLiveData<HangUpBean>()
    val offlineDuringCallLiveData = MutableLiveData<OfflineDuringCallBean>()


    private val signalEventCallback = object : SignalEventCallback {
        /**
         * 被强制下线
         */
        override fun forcedOffline() {

        }

        override fun inviteSomeoneIntoRoom(bean: InviteSomeoneBean) {

        }

        override fun inviteSomePeopleIntoRoom(bean: InviteSomePeopleBean) {

        }

        override fun rejectCall(bean: RejectCallBean) {
            rejectCallLiveData.value = bean
        }

        override fun acceptCall(bean: AcceptCallBean) {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
            acceptCallLiveData.value = bean.userInfo
        }

        override fun playSteam(bean: PlayStreamBean) {
            playSteamLiveData.value = bean
        }

        override fun hangUp(bean: HangUpBean) {
            hangUpLiveData.value = bean
        }

        override fun offlineDuringCall(bean: OfflineDuringCallBean) {
            offlineDuringCallLiveData.value = bean
        }
    }

    private lateinit var roomId: String

    private val mediaPlayer = MediaPlayer.create(applicationContext, R.raw.bell).apply {
        isLooping = true
    }

    override fun onCreate(owner: LifecycleOwner) {
        callSocketIoClient.addSignalEventCallback(signalEventCallback)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        callSocketIoClient.reqResetStatus()
        callSocketIoClient.removeSignalEventCallback(signalEventCallback)

        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        mediaPlayer.reset()
        mediaPlayer.release()
    }

    /**
     * 邀请某个人
     */
    fun reqInviteSomeone(userId: String, success: (ResInviteeInfoBean) -> Unit = {}) {
        mediaPlayer.start()

        callSocketIoClient.reqInviteSomeone(userId, success, failure = { code, reason ->
            XLog.e("failure-code:${code}, reason:${reason}")
            toastWarning(reason)
            launchOnUI {
                //延迟关闭画面
                delay(1000L)
                backPressed()
            }
        })
    }

    /**
     * 邀请一些人
     */
    fun reqInviteSomePeople(
        userIds: List<String>,
        success: (ResInviteSomePeopleBean) -> Unit = {}
    ) {
        mediaPlayer.start()

        callSocketIoClient.reqInviteSomePeople(userIds, success, failure = { code, reason ->
            XLog.e("failure-code:${code}, reason:${reason}")
            toastWarning(reason)
            launchOnUI {
                //延迟关闭画面
                delay(1000L)
                backPressed()
            }
        })
    }

    fun setRoomId(roomId: String) {
        this.roomId = roomId
    }

    fun publishStream(publishSteamUrl: String, success: () -> Unit) {
        callSocketIoClient.reqPublishStream(
            roomId,
            publishSteamUrl,
            success
        ) { code, reason ->
            XLog.e("publish stream failure-code:${code}, reason:${reason}")
            toastWarning(reason)
        }
    }

    /**
     * 挂断
     */
    fun hangUp() {
        if (this::roomId.isInitialized) {
            //考虑到房间号还没创建好就直接挂断了
            callSocketIoClient.reqHangUp(
                roomId,
                success = {
                    toastInfo(getString(R.string.call_ended))
                    delayBackPressed()
                },
                failure = { code, reason ->

                })
        }
    }

    fun delayBackPressed(timeMillis: Long = 1000L) {
        launchOnUIDelay(timeMillis) {
            backPressed()
        }
    }

}