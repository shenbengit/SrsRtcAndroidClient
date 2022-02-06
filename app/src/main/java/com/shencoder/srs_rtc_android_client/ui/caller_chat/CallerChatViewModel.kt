package com.shencoder.srs_rtc_android_client.ui.caller_chat

import android.app.Application
import android.media.MediaPlayer
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.elvishew.xlog.XLog
import com.shencoder.mvvmkit.base.repository.BaseNothingRepository
import com.shencoder.mvvmkit.base.viewmodel.BaseViewModel
import com.shencoder.mvvmkit.ext.*
import com.shencoder.srs_rtc_android_client.R
import com.shencoder.srs_rtc_android_client.helper.call.CallSocketIoClient
import com.shencoder.srs_rtc_android_client.helper.call.SignalEventCallback
import com.shencoder.srs_rtc_android_client.helper.call.SocketIoConnectionStatusCallback
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
            toastInfo("[${bean.userInfo.username}]${getString(R.string.reject_call)}")
            rejectCallLiveData.value = bean
        }

        override fun acceptCall(bean: AcceptCallBean) {
            toastSuccess("[${bean.userInfo.username}]${getString(R.string.accept_call)}")

            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
            acceptCallLiveData.value = bean.userInfo
        }

        override fun playSteam(bean: PlayStreamBean) {
            playSteamLiveData.value = bean
        }

        override fun hangUp(bean: HangUpBean) {
            toastInfo("[${bean.userInfo.username}]${getString(R.string.hang_up)}")
            hangUpLiveData.value = bean
        }

        override fun offlineDuringCall(bean: OfflineDuringCallBean) {
            toastInfo("[${bean.userInfo.username}]${getString(R.string.offline)}")

            offlineDuringCallLiveData.value = bean
        }
    }

    private lateinit var roomId: String

    private val mediaPlayer = MediaPlayer.create(applicationContext, R.raw.bell).apply {
        isLooping = true
    }

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

    private fun setRoomId(roomId: String) {
        this.roomId = roomId
    }

    /**
     * 邀请某个人
     */
    fun reqInviteSomeone(userId: String, success: (ResInviteeInfoBean) -> Unit = {}) {
        mediaPlayer.start()

        callSocketIoClient.reqInviteSomeone(userId, success = {
            setRoomId(it.roomId)
            success.invoke(it)
        }, failure = { code, reason ->
            XLog.e("req invite someone failure-code:${code}, reason:${reason}")
            toastWarning(reason)
            delayBackPressed()
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

        callSocketIoClient.reqInviteSomePeople(userIds, success = { bean ->
            setRoomId(bean.roomId)
            toastInviteSomePeopleInfo(
                bean.busyList,
                bean.offlineOrNotExistsList,
                bean.alreadyInRoomList
            )
            success.invoke(bean)
        }, failure = { code, reason ->
            XLog.e("req invite some people failure-code:${code}, reason:${reason}")
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
        }
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
                    XLog.e("hang up failure-code:${code}, reason:${reason}")
                    toastWarning(reason)
                    delayBackPressed()
                })
        }
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