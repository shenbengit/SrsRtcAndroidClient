package com.shencoder.srs_rtc_android_client.ui.main

import android.os.Bundle
import com.shencoder.srs_rtc_android_client.BR
import com.shencoder.srs_rtc_android_client.R
import com.shencoder.srs_rtc_android_client.base.BaseActivity
import com.shencoder.srs_rtc_android_client.databinding.ActivityMainBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : BaseActivity<MainViewModel, ActivityMainBinding>() {

    override fun getLayoutId(): Int {
        return R.layout.activity_main
    }

    override fun injectViewModel(): Lazy<MainViewModel> {
        return viewModel()
    }

    override fun getViewModelId(): Int {
        return BR.viewModel
    }

    override fun initData(savedInstanceState: Bundle?) {

    }

    override fun initView() {

    }


}