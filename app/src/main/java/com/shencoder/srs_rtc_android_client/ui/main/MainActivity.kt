package com.shencoder.srs_rtc_android_client.ui.main

import android.Manifest
import android.content.Intent
import android.os.Bundle
import com.permissionx.guolindev.PermissionX
import com.shencoder.mvvmkit.util.toastWarning
import com.shencoder.srs_rtc_android_client.BR
import com.shencoder.srs_rtc_android_client.R
import com.shencoder.srs_rtc_android_client.base.BaseActivity
import com.shencoder.srs_rtc_android_client.constant.ChatMode
import com.shencoder.srs_rtc_android_client.databinding.ActivityMainBinding
import com.shencoder.srs_rtc_android_client.ui.chat_room.EnterRoomIdActivity
import com.shencoder.srs_rtc_android_client.ui.check_user.CheckUserActivity
import com.shencoder.srs_rtc_android_client.util.requestCallPermissions
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

    override fun initView() {
        mBinding.btnPrivateChat.setOnClickListener {
            privateChat()
        }
        mBinding.btnGroupChat.setOnClickListener {
            groupChat()
        }
        mBinding.btnChatRoom.setOnClickListener {
            chatRoom()
        }
    }

    override fun initData(savedInstanceState: Bundle?) {

    }

    /**
     * 私聊
     */
    private fun privateChat() {
        requestCallPermissions { allGranted ->
            if (allGranted) {
                val intent = Intent(applicationContext, CheckUserActivity::class.java)
                intent.putExtra(CheckUserActivity.CHAT_MODE, ChatMode.PRIVATE_MODE)
                startActivity(intent)
            } else {
                toastWarning("Permission not granted.")
            }
        }
    }

    /**
     * 群聊
     */
    private fun groupChat() {
        requestCallPermissions { allGranted ->
            if (allGranted) {
                val intent = Intent(applicationContext, CheckUserActivity::class.java)
                intent.putExtra(CheckUserActivity.CHAT_MODE, ChatMode.GROUP_MODE)
                startActivity(intent)
            } else {
                toastWarning("Permission not granted.")
            }
        }
    }

    /**
     * 聊天室
     */
    private fun chatRoom() {
        requestCallPermissions { allGranted ->
            if (allGranted) {
                val intent = Intent(applicationContext, EnterRoomIdActivity::class.java)
                startActivity(intent)
            } else {
                toastWarning("Permission not granted.")
            }
        }
    }

}