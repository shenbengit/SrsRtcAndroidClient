package com.shencoder.srs_rtc_android_client.ui.private_chat

import android.os.Bundle
import com.shencoder.mvvmkit.util.toastWarning
import com.shencoder.srs_rtc_android_client.BR
import com.shencoder.srs_rtc_android_client.R
import com.shencoder.srs_rtc_android_client.base.BaseActivity
import com.shencoder.srs_rtc_android_client.databinding.ActivityPrivateChatBinding
import com.shencoder.srs_rtc_android_client.http.bean.UserInfoBean
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * 私聊
 */
class PrivateChatActivity : BaseActivity<PrivateChatViewModel, ActivityPrivateChatBinding>() {

    companion object {
        /**
         * 被叫信息
         */
        const val CALLEE_INFO = "CALLEE_INFO"
    }

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
        val userInfo: UserInfoBean? = intent?.getParcelableExtra(CALLEE_INFO)
        if (userInfo == null) {
            toastWarning("no callee info.")
            return
        }

    }
}