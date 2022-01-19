package com.shencoder.srs_rtc_android_client.ui.login

import android.app.Application
import android.content.Intent
import androidx.databinding.ObservableField
import com.elvishew.xlog.XLog
import com.shencoder.mvvmkit.base.viewmodel.BaseViewModel
import com.shencoder.mvvmkit.ext.*
import com.shencoder.srs_rtc_android_client.R
import com.shencoder.srs_rtc_android_client.constant.MMKVConstant
import com.shencoder.srs_rtc_android_client.ui.login.data.LoginRepository
import com.shencoder.srs_rtc_android_client.ui.main.MainActivity
import com.shencoder.srs_rtc_android_client.ui.register.RegisterUserActivity
import kotlinx.coroutines.delay

class LoginViewModel(
    application: Application,
    repo: LoginRepository
) : BaseViewModel<LoginRepository>(application, repo) {

    val userIdField = ObservableField(mmkv.decodeString(MMKVConstant.USER_ID))
    val passwordField = ObservableField("")

    /**
     * 注册用户
     */
    fun registerUser() {
        val intent = Intent(applicationContext, RegisterUserActivity::class.java)
        startActivity(intent)
    }

    /**
     * 登录
     */
    fun login() {
        val userId = userIdField.get()
        if (userId.isNullOrBlank()) {
            toastWarning(getString(R.string.empty_user_id))
            return
        }
        val password = passwordField.get()
        if (password.isNullOrBlank()) {
            toastWarning(getString(R.string.empty_password))
            return
        }
        httpRequest(
            {
                repo.userLogin(userId, password)
            }, {
                val data = it.data
                if (data == null) {
                    toastWarning(getString(R.string.user_info_not_obtained))
                    return@httpRequest
                }

                toastSuccess(getString(R.string.login_succeeded))
                mmkv.encode(MMKVConstant.USER_ID, data.userId)
                mmkv.encode(MMKVConstant.USER_INFO, data)

                launchOnUI {
                    delay(1000L)
                    val intent = Intent(applicationContext, MainActivity::class.java)
                    startActivity(intent)
                    backPressed()
                }
            }, {
                XLog.w("登录失败：${it.msg}")
                toastWarning(it.msg)
            }, {
                XLog.e("登录Error：${it.throwable.message}")
                toastError("login error: ${it.throwable.message}")
            })
    }
}