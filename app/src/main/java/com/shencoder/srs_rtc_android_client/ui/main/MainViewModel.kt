package com.shencoder.srs_rtc_android_client.ui.main

import android.app.Application
import android.content.Intent
import androidx.databinding.ObservableField
import androidx.lifecycle.LifecycleOwner
import com.elvishew.xlog.XLog
import com.shencoder.mvvmkit.base.repository.BaseNothingRepository
import com.shencoder.mvvmkit.base.viewmodel.BaseViewModel
import com.shencoder.mvvmkit.ext.toastWarning
import com.shencoder.srs_rtc_android_client.constant.ChatMode
import com.shencoder.srs_rtc_android_client.constant.MMKVConstant
import com.shencoder.srs_rtc_android_client.constant.SocketIoConnectionStatus
import com.shencoder.srs_rtc_android_client.helper.call.CallSocketIoClient
import com.shencoder.srs_rtc_android_client.helper.call.SignalEventCallback
import com.shencoder.srs_rtc_android_client.helper.call.SocketIoConnectionStatusCallback
import com.shencoder.srs_rtc_android_client.helper.call.bean.P2pRequestCallBean
import com.shencoder.srs_rtc_android_client.helper.call.bean.RequestCallBean
import com.shencoder.srs_rtc_android_client.http.bean.UserInfoBean
import com.shencoder.srs_rtc_android_client.ui.callee_chat.CalleeChatActivity
import com.shencoder.srs_rtc_android_client.ui.chat_room.EnterRoomIdActivity
import com.shencoder.srs_rtc_android_client.ui.check_user.CheckUserActivity
import com.shencoder.srs_rtc_android_client.ui.p2p.P2pCalleeActivity
import org.koin.core.component.inject

/**
 *
 * @author  ShenBen
 * @date    2022/1/19 10:32
 * @email   714081644@qq.com
 */
class MainViewModel(
    application: Application,
    repo: BaseNothingRepository
) : BaseViewModel<BaseNothingRepository>(application, repo) {

    private val callSocketIoClient: CallSocketIoClient by inject()

    /**
     * 本地用户信息
     */
    val userInfoField: ObservableField<UserInfoBean?> = ObservableField(
        mmkv.decodeParcelable(
            MMKVConstant.USER_INFO,
            UserInfoBean::class.java
        )
    )

    val connectionStatusField = ObservableField(SocketIoConnectionStatus.DISCONNECTED)

    /**
     * 添加连接状态回调
     */
    private val connectionStatusCallback = object : SocketIoConnectionStatusCallback {

        override fun connected() {
            connectionStatusField.set(SocketIoConnectionStatus.CONNECTED)
        }

        override fun disconnected() {
            connectionStatusField.set(SocketIoConnectionStatus.DISCONNECTED)
        }

        override fun connectError() {
            connectionStatusField.set(SocketIoConnectionStatus.DISCONNECTED)
        }
    }


    private val signalEventCallback = object : SignalEventCallback {
        /**
         * 收到会话请求
         */
        override fun requestCall(bean: RequestCallBean) {
            //跳转到被叫页面
            val intent = Intent(applicationContext, CalleeChatActivity::class.java)
            intent.putExtra(CalleeChatActivity.REQUEST_CALL, bean)
            startActivity(intent)
        }

        override fun p2pRequestCall(bean: P2pRequestCallBean) {
            val intent = Intent(applicationContext, P2pCalleeActivity::class.java)
            intent.putExtra(P2pCalleeActivity.REQUEST_CALL, bean)
            startActivity(intent)
        }
    }


    override fun onCreate(owner: LifecycleOwner) {
        callSocketIoClient.addConnectionStatusCallback(connectionStatusCallback)
        callSocketIoClient.addSignalEventCallback(signalEventCallback)
        val userId = mmkv.decodeString(MMKVConstant.USER_ID)
        if (userId.isNullOrBlank()) {
            XLog.w("user id not obtained.")
            toastWarning("user id not obtained.")
            return
        }
        connectionStatusField.set(SocketIoConnectionStatus.CONNECTING)
        callSocketIoClient.connect(userId)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        callSocketIoClient.disconnect()
        callSocketIoClient.removeConnectionStatusCallback(connectionStatusCallback)
        callSocketIoClient.removeSignalEventCallback(signalEventCallback)
    }


    /**
     * 私聊
     */
    fun privateChat() {
        if (isNotConnectedServer()) {
            return
        }
        val intent = Intent(applicationContext, CheckUserActivity::class.java)
        intent.putExtra(CheckUserActivity.CHAT_MODE, ChatMode.PRIVATE_MODE)
        startActivity(intent)
    }

    /**
     * 群聊
     */
    fun groupChat() {
        if (isNotConnectedServer()) {
            return
        }
        val intent = Intent(applicationContext, CheckUserActivity::class.java)
        intent.putExtra(CheckUserActivity.CHAT_MODE, ChatMode.GROUP_MODE)
        startActivity(intent)
    }

    /**
     * 聊天室
     */
    fun chatRoom() {
        if (isNotConnectedServer()) {
            return
        }
        val intent = Intent(applicationContext, EnterRoomIdActivity::class.java)
        startActivity(intent)
    }

    /**
     * p2p聊天
     *
     */
    fun p2p() {
        if (isNotConnectedServer()) {
            return
        }
        val intent = Intent(applicationContext, CheckUserActivity::class.java)
        intent.putExtra(CheckUserActivity.CHAT_MODE, ChatMode.PRIVATE_MODE)
        intent.putExtra(CheckUserActivity.IS_P2P, true)
        startActivity(intent)
    }

    private fun isNotConnectedServer(): Boolean {
        if (SocketIoConnectionStatus.CONNECTED != connectionStatusField.get()) {
            toastWarning("Signal server disconnected.")
            return true
        }
        return false
    }
}