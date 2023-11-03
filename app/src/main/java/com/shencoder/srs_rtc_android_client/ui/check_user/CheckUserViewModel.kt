package com.shencoder.srs_rtc_android_client.ui.check_user

import android.app.Application
import android.content.Intent
import com.shencoder.mvvmkit.base.viewmodel.BaseViewModel
import com.shencoder.srs_rtc_android_client.constant.CallType
import com.shencoder.srs_rtc_android_client.http.bean.UserInfoBean
import com.shencoder.srs_rtc_android_client.ui.caller_chat.CallerChatActivity
import com.shencoder.srs_rtc_android_client.ui.check_user.data.CheckUserRepository
import com.shencoder.srs_rtc_android_client.ui.p2p.P2pCallerActivity

/**
 *
 * @author  ShenBen
 * @date    2022/1/19 17:06
 * @email   714081644@qq.com
 */
class CheckUserViewModel(
    application: Application,
    repo: CheckUserRepository
) : BaseViewModel<CheckUserRepository>(application, repo) {


    fun toSfu(list: List<UserInfoBean>) {
        val intent = Intent(applicationContext, CallerChatActivity::class.java)
        intent.putParcelableArrayListExtra(CallerChatActivity.CALLEE_INFO_LIST, ArrayList(list))
        startActivity(intent)
        backPressed()
    }

    fun toMesh(list: List<UserInfoBean>, callType: CallType) {
        val intent = Intent(applicationContext, P2pCallerActivity::class.java)
        intent.putParcelableArrayListExtra(P2pCallerActivity.CALLEE_INFO_LIST, ArrayList(list))
        intent.putExtra(P2pCallerActivity.CALL_TYPE, callType)
        startActivity(intent)
        backPressed()
    }
}