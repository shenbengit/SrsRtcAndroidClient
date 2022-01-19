package com.shencoder.srs_rtc_android_client.ui.register

import android.os.Bundle
import com.shencoder.srs_rtc_android_client.BR
import com.shencoder.srs_rtc_android_client.R
import com.shencoder.srs_rtc_android_client.base.BaseActivity
import com.shencoder.srs_rtc_android_client.databinding.ActivityRegisterUserBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * 用户注册
 */
class RegisterUserActivity : BaseActivity<RegisterUserViewModel, ActivityRegisterUserBinding>() {

    override fun getLayoutId(): Int {
        return R.layout.activity_register_user
    }

    override fun injectViewModel(): Lazy<RegisterUserViewModel> {
        return viewModel()
    }

    override fun getViewModelId(): Int {
        return BR.viewModel
    }

    override fun initView() {

    }

    override fun initData(savedInstanceState: Bundle?) {
    }


}