package com.shencoder.srs_rtc_android_client.ui.p2p

import android.os.Bundle
import com.shencoder.srs_rtc_android_client.BR
import com.shencoder.srs_rtc_android_client.R
import com.shencoder.srs_rtc_android_client.base.BaseActivity
import com.shencoder.srs_rtc_android_client.databinding.ActivityP2pCallerBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * P2P聊天，主叫页面
 *
 * @constructor Create empty P2p caller activity
 */
class P2pCallerActivity : BaseActivity<P2pCalleeViewModel, ActivityP2pCallerBinding>() {

    companion object {
        /**
         * 被叫信息
         */
        const val CALLEE_INFO_LIST = "CALLEE_INFO_LIST"

        /**
         * 通话类型
         */
        const val CALL_TYPE = "CALL_TYPE"
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_p2p_caller
    }

    override fun getViewModelId(): Int {
        return BR.viewModel
    }

    override fun injectViewModel(): Lazy<P2pCalleeViewModel> {
        return viewModel()
    }

    override fun initView() {

    }

    override fun initData(savedInstanceState: Bundle?) {

    }
}