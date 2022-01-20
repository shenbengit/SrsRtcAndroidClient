package com.shencoder.srs_rtc_android_client.ui.check_user

import android.os.Bundle
import com.shencoder.srs_rtc_android_client.BR
import com.shencoder.srs_rtc_android_client.R
import com.shencoder.srs_rtc_android_client.base.BaseActivity
import com.shencoder.srs_rtc_android_client.constant.ChatMode
import com.shencoder.srs_rtc_android_client.databinding.ActivityCheckUserBinding
import com.shencoder.srs_rtc_android_client.http.bean.UserInfoBean
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

        /**
         * 不可选择的会见人员列表
         * [ArrayList<UserInfoBean>]
         */
        const val NON_SELECTABLE_LIST = "NON_SELECTABLE_LIST"
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

    }

    override fun initData(savedInstanceState: Bundle?) {
        intent?.run {
            val chatMode = getSerializableExtra(CHAT_MODE) as ChatMode
            val list: ArrayList<UserInfoBean>? = getParcelableArrayListExtra(NON_SELECTABLE_LIST)

            mViewModel.initData(chatMode, list)
        }

    }


}