package com.shencoder.srs_rtc_android_client.base

import android.app.Dialog
import androidx.databinding.ViewDataBinding
import com.shencoder.loadingdialog.LoadingDialog
import com.shencoder.mvvmkit.base.repository.IRepository
import com.shencoder.mvvmkit.base.view.BaseSupportFragment
import com.shencoder.mvvmkit.base.viewmodel.BaseViewModel
import com.shencoder.srs_rtc_android_client.R
import com.shencoder.srs_rtc_android_client.constant.Constant
import me.jessyan.autosize.internal.CustomAdapt

/**
 *
 * @author  ShenBen
 * @date    2021/6/10 11:13
 * @email   714081644@qq.com
 */
abstract class BaseFragment<VM : BaseViewModel<out IRepository>, VDB : ViewDataBinding> :
    BaseSupportFragment<VM, VDB>(), CustomAdapt {


    override fun isBaseOnWidth(): Boolean {
        return true
    }

    override fun getSizeInDp(): Float {
        return Constant.DEFAULT_SIZE_IN_DP
    }

    override fun initLoadingDialog(): Dialog {
        return LoadingDialog.builder(requireContext())
            .setHintText(getString(R.string.loading))
            .create()
    }
}