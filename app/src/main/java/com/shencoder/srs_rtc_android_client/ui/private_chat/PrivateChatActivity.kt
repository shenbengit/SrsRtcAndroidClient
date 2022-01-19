package com.shencoder.srs_rtc_android_client.ui.private_chat

import android.os.Bundle
import com.shencoder.srs_rtc_android_client.BR
import com.shencoder.srs_rtc_android_client.R
import com.shencoder.srs_rtc_android_client.base.BaseActivity
import com.shencoder.srs_rtc_android_client.databinding.ActivityPrivateChatBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * 私聊
 */
class PrivateChatActivity : BaseActivity<PrivateChatViewModel, ActivityPrivateChatBinding>() {

    override fun getLayoutId(): Int {
        return R.layout.activity_private_chat
    }

    override fun injectViewModel(): Lazy<PrivateChatViewModel> {
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