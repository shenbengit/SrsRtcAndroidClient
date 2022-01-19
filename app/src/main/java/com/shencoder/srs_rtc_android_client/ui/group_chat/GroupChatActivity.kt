package com.shencoder.srs_rtc_android_client.ui.group_chat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.shencoder.srs_rtc_android_client.BR
import com.shencoder.srs_rtc_android_client.R
import com.shencoder.srs_rtc_android_client.base.BaseActivity
import com.shencoder.srs_rtc_android_client.databinding.ActivityGroupChatBinding
import com.shencoder.srs_rtc_android_client.databinding.ActivityPrivateChatBinding
import com.shencoder.srs_rtc_android_client.ui.private_chat.PrivateChatViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * 群聊
 */
class GroupChatActivity : BaseActivity<GroupChatViewModel, ActivityGroupChatBinding>() {

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

    }
}