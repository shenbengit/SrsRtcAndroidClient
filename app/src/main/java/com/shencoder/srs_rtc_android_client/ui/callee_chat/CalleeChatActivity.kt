package com.shencoder.srs_rtc_android_client.ui.callee_chat

import android.os.Bundle
import com.shencoder.mvvmkit.util.toastWarning
import com.shencoder.srs_rtc_android_client.BR
import com.shencoder.srs_rtc_android_client.R
import com.shencoder.srs_rtc_android_client.base.BaseActivity
import com.shencoder.srs_rtc_android_client.databinding.ActivityCalleeChatBinding
import com.shencoder.srs_rtc_android_client.util.requestCallPermissions
import com.shencoder.srs_rtc_android_client.webrtc.widget.CallLayout
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * 私聊、群聊-被叫页
 */
class CalleeChatActivity : BaseActivity<CalleeChatViewModel, ActivityCalleeChatBinding>() {

    companion object {
        const val ROOM_ID = "ROOM_ID"


    }

    override fun getLayoutId(): Int {
        return R.layout.activity_callee_chat
    }

    override fun injectViewModel(): Lazy<CalleeChatViewModel> {
        return viewModel()
    }

    override fun getViewModelId(): Int {
        return BR.viewModel
    }

    override fun initView() {
        mBinding.callLayout.setCallActionCallback(object : CallLayout.CallActionCallback {

            override fun rejectCall() {

            }

            override fun acceptCall() {

            }

            override fun hangUpCall() {

            }
        })
    }

    override fun initData(savedInstanceState: Bundle?) {
        requestCallPermissions { allGranted ->
            if (allGranted.not()) {
                toastWarning("Permission not granted.")
                onBackPressedSupport()
                return@requestCallPermissions
            }


        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mBinding.callLayout.release()
    }

}