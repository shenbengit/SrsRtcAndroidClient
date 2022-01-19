package com.shencoder.srs_rtc_android_client.ui.chat_room

import android.content.Intent
import android.os.Bundle
import com.shencoder.mvvmkit.base.viewmodel.DefaultViewModel
import com.shencoder.mvvmkit.util.toastWarning
import com.shencoder.srs_rtc_android_client.R
import com.shencoder.srs_rtc_android_client.base.BaseActivity
import com.shencoder.srs_rtc_android_client.databinding.ActivityEnterRoomIdBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * 输入进入的房间号
 */
class EnterRoomIdActivity : BaseActivity<DefaultViewModel, ActivityEnterRoomIdBinding>() {

    override fun getLayoutId(): Int {
        return R.layout.activity_enter_room_id
    }

    override fun injectViewModel(): Lazy<DefaultViewModel> {
        return viewModel()
    }

    override fun getViewModelId(): Int {
        return 0
    }

    override fun initView() {
        mBinding.btnEnterRoom.setOnClickListener {
            val roomId = mBinding.etRoomId.text.toString().trim()
            if (roomId.isBlank()) {
                toastWarning("roomId is empty.")
                return@setOnClickListener
            }
            val intent = Intent(this, ChatRoomActivity::class.java)
            intent.putExtra(ChatRoomActivity.ROOM_ID, roomId)
            startActivity(intent)
            onBackPressedSupport()
        }
    }

    override fun initData(savedInstanceState: Bundle?) {
    }
}