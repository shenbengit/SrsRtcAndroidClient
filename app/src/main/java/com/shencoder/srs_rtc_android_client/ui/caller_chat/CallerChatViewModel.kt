package com.shencoder.srs_rtc_android_client.ui.caller_chat

import android.app.Application
import com.shencoder.mvvmkit.base.repository.BaseNothingRepository
import com.shencoder.mvvmkit.base.viewmodel.BaseViewModel
import com.shencoder.mvvmkit.ext.launchOnUI
import com.shencoder.mvvmkit.ext.toastWarning
import com.shencoder.srs_rtc_android_client.helper.call.CallSocketIoClient
import com.shencoder.srs_rtc_android_client.helper.call.bean.ResInviteeInfoBean
import com.shencoder.srs_rtc_android_client.helper.call.bean.ResInviteSomePeopleBean
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

    /**
     * 邀请某个人
     */
    fun reqInviteSomeone(userId: String, success: (ResInviteeInfoBean) -> Unit = {}) {
        callSocketIoClient.reqInviteSomeone(userId, success, failure = { code, reason ->
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
        callSocketIoClient.reqInviteSomePeople(userIds, success, failure = { code, reason ->
            toastWarning(reason)
            launchOnUI {
                //延迟关闭画面
                delay(1000L)
                backPressed()
            }
        })
    }


}