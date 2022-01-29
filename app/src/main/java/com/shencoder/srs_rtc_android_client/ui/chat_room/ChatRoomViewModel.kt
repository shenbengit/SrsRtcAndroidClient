package com.shencoder.srs_rtc_android_client.ui.chat_room

import android.app.Application
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.elvishew.xlog.XLog
import com.shencoder.mvvmkit.base.repository.BaseNothingRepository
import com.shencoder.mvvmkit.base.viewmodel.BaseViewModel
import com.shencoder.mvvmkit.ext.launchOnUI
import com.shencoder.mvvmkit.ext.toastInfo
import com.shencoder.mvvmkit.ext.toastWarning
import com.shencoder.srs_rtc_android_client.helper.call.CallSocketIoClient
import com.shencoder.srs_rtc_android_client.helper.call.SignalEventCallback
import com.shencoder.srs_rtc_android_client.helper.call.bean.*
import kotlinx.coroutines.delay
import org.koin.core.component.inject

/**
 *
 * @author  ShenBen
 * @date    2022/01/19 21:10
 * @email   714081644@qq.com
 */
class ChatRoomViewModel(
    application: Application,
    repo: BaseNothingRepository
) : BaseViewModel<BaseNothingRepository>(application, repo) {

    private val callSocketIoClient: CallSocketIoClient by inject()

    private lateinit var roomId: String

    val joinChatRoomLiveData = MutableLiveData<ClientInfoBean>()
    val playSteamLiveData = MutableLiveData<PlayStreamBean>()
    val leaveChatRoomLiveData = MutableLiveData<ClientInfoBean>()

    private val signalEventCallback = object : SignalEventCallback {
        override fun joinChatRoom(bean: JoinChatRoomBean) {
            joinChatRoomLiveData.value = bean.userInfo
        }

        override fun leaveChatRoom(bean: LeaveChatRoomBean) {
            leaveChatRoomLiveData.value = bean.userInfo
        }

        override fun playSteam(bean: PlayStreamBean) {
            playSteamLiveData.value = bean
        }
    }

    override fun onCreate(owner: LifecycleOwner) {
        callSocketIoClient.addSignalEventCallback(signalEventCallback)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        callSocketIoClient.reqResetStatus()
        callSocketIoClient.removeSignalEventCallback(signalEventCallback)
    }

    fun setRoomId(roomId: String) {
        this.roomId = roomId
    }

    fun joinChatRoom(success: (ResAlreadyInRoomBean) -> Unit) {
        callSocketIoClient.reqJoinChatRoom(roomId, success, failure = { code, reason ->
            XLog.e("failure-code:${code}, reason:${reason}")
            toastWarning(reason)
        })
    }

    fun publishStream(publishSteamUrl: String, success: () -> Unit) {
        callSocketIoClient.reqPublishStream(
            roomId,
            publishSteamUrl,
            success
        ) { code, reason ->
            XLog.e("failure-code:${code}, reason:${reason}")
            toastWarning(reason)
        }
    }

    fun leaveChatRoom() {
        callSocketIoClient.reqLeaveChatRoom(roomId, success = {
            toastInfo("left the room.")
            delayBackPressed()
        }, failure = { code, reason ->
            XLog.e("failure-code:${code}, reason:${reason}")
            toastWarning(reason)
        })
    }

    fun delayBackPressed(timeMillis: Long = 1000L) {
        launchOnUI {
            delay(timeMillis)
            backPressed()
        }
    }
}