package com.shencoder.srs_rtc_android_client.ui.check_user

import android.os.Bundle
import com.shencoder.srs_rtc_android_client.BR
import com.shencoder.srs_rtc_android_client.R
import com.shencoder.srs_rtc_android_client.base.BaseActivity
import com.shencoder.srs_rtc_android_client.constant.ChatMode
import com.shencoder.srs_rtc_android_client.databinding.ActivityCheckUserBinding
import com.shencoder.srs_rtc_android_client.http.bean.UserInfoBean
import com.shencoder.srs_rtc_android_client.widget.CheckUserView
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * 选择通话用户
 */
class CheckUserActivity : BaseActivity<CheckUserViewModel, ActivityCheckUserBinding>() {

    companion object {
        /**
         * 聊天类型
         * @see [ChatMode]
         */
        const val CHAT_MODE = "CHAT_MODE"
    }

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
        mBinding.cuv.setCheckUserCallback(object : CheckUserView.CheckUserCallback {

            override fun onClose() {
                onBackPressedSupport()
            }

            override fun onCheckUser(list: List<UserInfoBean>) {
                mViewModel.confirm(list)
            }
        })
    }

    override fun initData(savedInstanceState: Bundle?) {
        intent?.run {
            val chatMode = getSerializableExtra(CHAT_MODE) as ChatMode
            mBinding.cuv.setChatMode(chatMode)
        }
    }
}