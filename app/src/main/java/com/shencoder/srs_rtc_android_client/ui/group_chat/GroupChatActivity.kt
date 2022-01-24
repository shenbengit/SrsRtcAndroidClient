package com.shencoder.srs_rtc_android_client.ui.group_chat

import android.os.Bundle
import com.shencoder.mvvmkit.util.toastWarning
import com.shencoder.srs_rtc_android_client.BR
import com.shencoder.srs_rtc_android_client.R
import com.shencoder.srs_rtc_android_client.base.BaseActivity
import com.shencoder.srs_rtc_android_client.constant.CallRoleType
import com.shencoder.srs_rtc_android_client.databinding.ActivityGroupChatBinding
import com.shencoder.srs_rtc_android_client.http.bean.UserInfoBean
import com.shencoder.srs_rtc_android_client.ui.private_chat.PrivateChatActivity
import com.shencoder.srs_rtc_android_client.util.requestCallPermissions
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * 群聊
 */
class GroupChatActivity : BaseActivity<GroupChatViewModel, ActivityGroupChatBinding>() {

    companion object {
        /**
         * 通话角色
         * 主叫还是被叫
         */
        const val CALL_ROLE_TYPE = "CALL_ROLE_TYPE"

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
        requestCallPermissions { allGranted ->
            if (allGranted) {
                intent?.run {
                    val callRoleType =
                        getSerializableExtra(PrivateChatActivity.CALL_ROLE_TYPE) as CallRoleType
                    val userInfo: ArrayList<UserInfoBean>? =
                        getParcelableArrayListExtra(CALLEE_INFO_LIST)
                    if (userInfo == null) {
                        toastWarning("no callee info.")
                    }
                }
            } else {
                toastWarning("Permission not granted.")
            }
        }


    }


}