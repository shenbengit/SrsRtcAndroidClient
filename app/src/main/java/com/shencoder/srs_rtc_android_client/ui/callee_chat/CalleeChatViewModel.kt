package com.shencoder.srs_rtc_android_client.ui.callee_chat

import android.app.Application
import android.media.MediaPlayer
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.elvishew.xlog.XLog
import com.shencoder.mvvmkit.base.repository.BaseNothingRepository
import com.shencoder.mvvmkit.base.viewmodel.BaseViewModel
import com.shencoder.mvvmkit.ext.launchOnUIDelay
import com.shencoder.mvvmkit.ext.toastInfo
import com.shencoder.mvvmkit.ext.toastWarning
import com.shencoder.srs_rtc_android_client.R
import com.shencoder.srs_rtc_android_client.constant.SocketIoConnectionStatus
import com.shencoder.srs_rtc_android_client.helper.call.CallSocketIoClient
import com.shencoder.srs_rtc_android_client.helper.call.SignalEventCallback
import com.shencoder.srs_rtc_android_client.helper.call.SocketIoConnectionStatusCallback
import com.shencoder.srs_rtc_android_client.helper.call.bean.*
import org.koin.core.component.inject

/**
 *
 * @author  ShenBen
 * @date    2022/1/26 09:29
 * @email   714081644@qq.com
 */
class CalleeChatViewModel(
    application: Application,
    repo: BaseNothingRepository
) : BaseViewModel<BaseNothingRepository>(application, repo) {

    private val callSocketIoClient: CallSocketIoClient by inject()

    val inviteSomeoneIntoRoomLiveData = MutableLiveData<InviteSomeoneBean>()
    val inviteSomePeopleIntoRoomLiveData = MutableLiveData<InviteSomePeopleBean>()
    val rejectCallLiveData = MutableLiveData<RejectCallBean>()
    val acceptCallLiveData = MutableLiveData<ClientInfoBean>()
    val playSteamLiveData = MutableLiveData<PlayStreamBean>()
    val hangUpLiveData = MutableLiveData<HangUpBean>()
    val offlineDuringCallLiveData = MutableLiveData<OfflineDuringCallBean>()

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

        override fun inviteSomeoneIntoRoom(bean: InviteSomeoneBean) {
            toastInfo("[${bean.inviteInfo.username}] invited [${bean.inviteeInfo.username}]")
            inviteSomeoneIntoRoomLiveData.value = bean
        }

        override fun inviteSomePeopleIntoRoom(bean: InviteSomePeopleBean) {
            toastInfo(
                "[${bean.inviteInfo.username}] invited ${
                    bean.inviteeInfoList.map { info -> info.username }.toTypedArray()
                        .contentToString()
                }"
            )
            inviteSomePeopleIntoRoomLiveData.value = bean
        }

        override fun rejectCall(bean: RejectCallBean) {
            rejectCallLiveData.value = bean
        }

        override fun acceptCall(bean: AcceptCallBean) {
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

    private val mediaPlayer = MediaPlayer.create(applicationContext, R.raw.bell).apply {
        isLooping = true
    }

    private lateinit var roomId: String

    override fun onCreate(owner: LifecycleOwner) {
        callSocketIoClient.addConnectionStatusCallback(connectionStatusCallback)
        callSocketIoClient.addSignalEventCallback(signalEventCallback)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        callSocketIoClient.reqResetStatus()
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

    /**
     * 邀请某人进入自己房间
     */
    fun reqInviteSomeoneIntoRoom(userId: String, success: (ResInviteeInfoBean) -> Unit) {
        callSocketIoClient.reqInviteSomeoneIntoRoom(
            userId,
            roomId,
            success,
            failure = { code, reason ->
                XLog.e("req invite someone into room failure-code:${code}, reason:${reason}")
                toastWarning(reason)
            })
    }

    /**
     * 邀请某些人进入自己房间
     */
    fun reqInviteSomePeopleIntoRoom(
        userIds: List<String>,
        success: (ResInviteSomePeopleBean) -> Unit
    ) {
        callSocketIoClient.reqInviteSomePeopleIntoRoom(
            userIds,
            roomId,
            success = { bean ->
                toastInviteSomePeopleInfo(
                    bean.busyList,
                    bean.offlineOrNotExistsList,
                    bean.alreadyInRoomList
                )
                success.invoke(bean)
            },
            failure = { code, reason ->
                XLog.e("req invite some people into room failure-code:${code}, reason:${reason}")
                toastWarning(reason)
            })
    }

    /**
     * 拒接
     */
    fun rejectCall() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }

        callSocketIoClient.reqRejectCall(
            roomId,
            success = {
                toastInfo(getString(R.string.call_ended))
                delayBackPressed()
            },
            failure = { code, reason ->
                XLog.e("reject call failure-code:${code}, reason:${reason}")
                toastWarning(reason)
                delayBackPressed()
            })
    }


    /**
     * 接听
     */
    fun acceptCall(success: (ResAlreadyInRoomBean) -> Unit) {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }

        callSocketIoClient.reqAcceptCall(
            roomId,
            success,
            failure = { code, reason ->
                XLog.e("accept call failure-code:${code}, reason:${reason}")
                toastWarning(reason)
                delayBackPressed()
            })
    }

    fun publishStream(publishSteamUrl: String, success: () -> Unit) {
        callSocketIoClient.reqPublishStream(
            roomId,
            publishSteamUrl,
            success
        ) { code, reason ->
            XLog.e("publish stream failure-code:${code}, reason:${reason}")
            toastWarning(reason)
            delayBackPressed()
        }
    }

    fun hangUp() {
        callSocketIoClient.reqHangUp(
            roomId,
            success = {
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

    private fun toastInviteSomePeopleInfo(
        busyList: List<ClientInfoBean>,
        offlineOrNotExistsList: List<OfflineOrNotExistsBean>,
        alreadyInRoomList: List<ClientInfoBean>
    ) {
        val msg = buildString {
            if (busyList.isNotEmpty()) {
                append("busyList: [")
                busyList.forEachIndexed { index, info ->
                    append(info.username)
                    if (index != busyList.lastIndex) {
                        append("、")
                    }
                }
                append("], ")
            }

            if (offlineOrNotExistsList.isNotEmpty()) {
                append("offlineOrNotExistsList: userId[")
                offlineOrNotExistsList.forEachIndexed { index, info ->
                    append(info.userId)
                    if (index != offlineOrNotExistsList.lastIndex) {
                        append("、")
                    }
                }
                append("], ")
            }

            if (alreadyInRoomList.isNotEmpty()) {
                append("alreadyInRoomList: [")
                alreadyInRoomList.forEachIndexed { index, info ->
                    append(info.username)
                    if (index != alreadyInRoomList.lastIndex) {
                        append("、")
                    }
                }
                append("]")
            }
        }
        if (msg.isNotBlank()) {
            toastWarning(msg, Toast.LENGTH_LONG)
        }
    }
}