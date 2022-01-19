package com.shencoder.srs_rtc_android_client.ui.main

import android.app.Application
import androidx.databinding.ObservableField
import androidx.lifecycle.LifecycleOwner
import com.elvishew.xlog.XLog
import com.shencoder.mvvmkit.base.repository.BaseNothingRepository
import com.shencoder.mvvmkit.base.viewmodel.BaseViewModel
import com.shencoder.mvvmkit.ext.toastWarning
import com.shencoder.srs_rtc_android_client.constant.MMKVConstant
import com.shencoder.srs_rtc_android_client.constant.SocketIoConnectionStatus
import com.shencoder.srs_rtc_android_client.helper.call.CallSocketIoClient
import com.shencoder.srs_rtc_android_client.helper.call.SocketIoConnectionStatusCallback
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

    override fun onCreate(owner: LifecycleOwner) {
        callSocketIoClient.addConnectionStatusCallback(connectionStatusCallback)
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
    }
}