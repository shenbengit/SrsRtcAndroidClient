package com.shencoder.srs_rtc_android_client.ui.check_user

import android.app.Application
import com.shencoder.mvvmkit.base.viewmodel.BaseViewModel
import com.shencoder.srs_rtc_android_client.ui.check_user.adapter.CheckUserAdapter
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

    val adapter = CheckUserAdapter()

}