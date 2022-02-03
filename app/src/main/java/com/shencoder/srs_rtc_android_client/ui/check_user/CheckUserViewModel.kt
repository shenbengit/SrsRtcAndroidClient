package com.shencoder.srs_rtc_android_client.ui.check_user

import android.app.Application
import android.content.Intent
import com.shencoder.mvvmkit.base.viewmodel.BaseViewModel
import com.shencoder.mvvmkit.ext.toastWarning
import com.shencoder.srs_rtc_android_client.R
import com.shencoder.srs_rtc_android_client.http.bean.UserInfoBean
import com.shencoder.srs_rtc_android_client.ui.caller_chat.CallerChatActivity
import com.shencoder.srs_rtc_android_client.ui.check_user.data.CheckUserRepository

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


    /**
     * 确认
     */
    fun confirm(list: List<UserInfoBean>) {
        if (list.isEmpty()) {
            toastWarning(getString(R.string.please_select_the_callee))
            return
        }
        val intent = Intent(applicationContext, CallerChatActivity::class.java)
        intent.putParcelableArrayListExtra(CallerChatActivity.CALLEE_INFO_LIST, ArrayList(list))
        startActivity(intent)
        backPressed()
    }
}