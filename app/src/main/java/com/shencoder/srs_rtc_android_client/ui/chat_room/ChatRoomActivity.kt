package com.shencoder.srs_rtc_android_client.ui.chat_room

import android.os.Bundle
import com.shencoder.srs_rtc_android_client.BR
import com.shencoder.srs_rtc_android_client.R
import com.shencoder.srs_rtc_android_client.base.BaseActivity
import com.shencoder.srs_rtc_android_client.databinding.ActivityChatRoomBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * 聊天室
 */
class ChatRoomActivity : BaseActivity<ChatRoomViewModel, ActivityChatRoomBinding>() {

    companion object {
        const val ROOM_ID = "ROOM_ID"
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_chat_room
    }

    override fun injectViewModel(): Lazy<ChatRoomViewModel> {
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