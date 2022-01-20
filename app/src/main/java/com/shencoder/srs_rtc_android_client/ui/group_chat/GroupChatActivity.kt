package com.shencoder.srs_rtc_android_client.ui.group_chat

import android.os.Bundle
import com.shencoder.mvvmkit.util.toastWarning
import com.shencoder.srs_rtc_android_client.BR
import com.shencoder.srs_rtc_android_client.R
import com.shencoder.srs_rtc_android_client.base.BaseActivity
import com.shencoder.srs_rtc_android_client.databinding.ActivityGroupChatBinding
import com.shencoder.srs_rtc_android_client.http.bean.UserInfoBean
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * 群聊
 */
class GroupChatActivity : BaseActivity<GroupChatViewModel, ActivityGroupChatBinding>() {

    companion object {
        /**
         * 被叫信息
         */
        const val CALLEE_INFO_LIST = "CALLEE_INFO_LIST"
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_group_chat
    }

    override fun injectViewModel(): Lazy<GroupChatViewModel> {
        return viewModel()
    }

    override fun getViewModelId(): Int {
        return BR.viewModel
    }

    override fun initView() {

    }

    override fun initData(savedInstanceState: Bundle?) {
        val userInfo: ArrayList<UserInfoBean>? =
            intent?.getParcelableArrayListExtra(CALLEE_INFO_LIST)
        if (userInfo == null) {
            toastWarning("no callee info.")
            return
        }
    }
}