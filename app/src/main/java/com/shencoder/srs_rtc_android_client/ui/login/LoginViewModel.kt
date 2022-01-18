package com.shencoder.srs_rtc_android_client.ui.login

import android.app.Application
import com.shencoder.mvvmkit.base.viewmodel.BaseViewModel
import com.shencoder.srs_rtc_android_client.ui.login.data.LoginRepository

class LoginViewModel(
    application: Application,
    repo: LoginRepository
) : BaseViewModel<LoginRepository>(application, repo) {

}