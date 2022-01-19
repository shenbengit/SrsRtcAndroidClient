package com.shencoder.srs_rtc_android_client.ui.check_user

import android.os.Bundle
import com.shencoder.srs_rtc_android_client.BR
import com.shencoder.srs_rtc_android_client.R
import com.shencoder.srs_rtc_android_client.base.BaseActivity
import com.shencoder.srs_rtc_android_client.databinding.ActivityCheckUserBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * 选择通话用户
 */
class CheckUserActivity : BaseActivity<CheckUserViewModel, ActivityCheckUserBinding>() {

    override fun getLayoutId(): Int {
        return R.layout.activity_check_user
    }

    override fun getViewModelId(): Int {
        return BR.viewModel
    }

    override fun injectViewModel(): Lazy<CheckUserViewModel> {
        return viewModel()
    }

    override fun initView() {

    }

    override fun initData(savedInstanceState: Bundle?) {

    }


}