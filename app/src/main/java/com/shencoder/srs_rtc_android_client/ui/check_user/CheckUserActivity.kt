package com.shencoder.srs_rtc_android_client.ui.check_user

import android.os.Bundle
import com.shencoder.mvvmkit.util.toastWarning
import com.shencoder.srs_rtc_android_client.BR
import com.shencoder.srs_rtc_android_client.R
import com.shencoder.srs_rtc_android_client.base.BaseActivity
import com.shencoder.srs_rtc_android_client.constant.CallType
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

        const val IS_P2P = "IS_P2P"
    }

    private var isP2P = false
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

            override fun onCheckUser(list: List<UserInfoBean>, callType: CallType) {
                if (list.isEmpty()) {
                    toastWarning(getString(R.string.please_select_the_callee))
                    return
                }
                if (isP2P) {
                    mViewModel.toMesh(list, callType)
                } else {
                    mViewModel.toSfu(list)
                }
            }
        })
    }

    override fun initData(savedInstanceState: Bundle?) {
        intent?.run {
            val chatMode = getSerializableExtra(CHAT_MODE) as ChatMode
            isP2P = getBooleanExtra(IS_P2P, false)

            mBinding.cuv.run {
                setChatMode(chatMode)
                showRgCallType(isP2P)
            }
        }
    }
}