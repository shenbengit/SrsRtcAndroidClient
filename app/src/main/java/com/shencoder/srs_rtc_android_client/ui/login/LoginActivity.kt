package com.shencoder.srs_rtc_android_client.ui.login

import android.os.Bundle
import com.shencoder.srs_rtc_android_client.BR
import com.shencoder.srs_rtc_android_client.R
import com.shencoder.srs_rtc_android_client.base.BaseActivity
import com.shencoder.srs_rtc_android_client.databinding.ActivityLoginBinding
import com.shencoder.srs_rtc_android_client.helper.call.CallSocketIoClient
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * 登录页
 */
class LoginActivity : BaseActivity<LoginViewModel, ActivityLoginBinding>() {

    override fun getLayoutId(): Int {
        return R.layout.activity_login
    }

    override fun injectViewModel(): Lazy<LoginViewModel> {
        return viewModel()
    }

    override fun getViewModelId(): Int {
        return BR.viewModel
    }

    override fun initView() {

    }

    override fun initData(savedInstanceState: Bundle?) {
        CallSocketIoClient.getInstance().connect("test")
    }

    override fun onDestroy() {
        super.onDestroy()
        CallSocketIoClient.getInstance().disconnect()
    }
}