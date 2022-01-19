package com.shencoder.srs_rtc_android_client.ui.register

import android.app.Application
import android.text.TextUtils
import androidx.databinding.ObservableField
import com.elvishew.xlog.XLog
import com.shencoder.mvvmkit.base.viewmodel.BaseViewModel
import com.shencoder.mvvmkit.ext.httpRequest
import com.shencoder.mvvmkit.ext.launchOnUI
import com.shencoder.mvvmkit.ext.toastSuccess
import com.shencoder.mvvmkit.ext.toastWarning
import com.shencoder.srs_rtc_android_client.R
import com.shencoder.srs_rtc_android_client.ui.register.data.RegisterUserRepository
import kotlinx.coroutines.delay

/**
 *
 * @author  ShenBen
 * @date    2022/1/19 14:44
 * @email   714081644@qq.com
 */
class RegisterUserViewModel(
    application: Application,
    repo: RegisterUserRepository
) : BaseViewModel<RegisterUserRepository>(application, repo) {

    val userIdField = ObservableField("")
    val usernameField = ObservableField("")
    val passwordField = ObservableField("")
    val confirmPasswordField = ObservableField("")

    /**
     * 注册用户
     */
    fun registerUser() {
        val userId = userIdField.get()
        if (userId.isNullOrBlank()) {
            toastWarning("UserId is empty.")
            return
        }
        val username = usernameField.get()
        if (username.isNullOrBlank()) {
            toastWarning("Username is empty.")
            return
        }
        val password = passwordField.get()
        if (password.isNullOrBlank()) {
            toastWarning("Password is empty.")
            return
        }
        val confirmPassword = confirmPasswordField.get()
        if (confirmPassword.isNullOrBlank()) {
            toastWarning("ConfirmPassword is empty.")
            return
        }
        if (TextUtils.equals(password, confirmPassword).not()) {
            toastWarning(getString(R.string.inconsistent_passwords))
            return
        }

        httpRequest({ repo.checkUserId(userId) }, {
            registerUser(userId, username, password)
        }, {
            XLog.w("checkUserId failed: ${it.msg}")
            toastWarning("checkUserId failed: ${it.msg}")
        }, {
            XLog.w("checkUserId error: ${it.throwable.message}")
            toastWarning("checkUserId error: ${it.throwable.message}")
        })
    }

    private fun registerUser(userId: String, username: String, password: String) {
        httpRequest({ repo.registerUser(userId, username, password) }, {
            toastSuccess(getString(R.string.registration_success))
            launchOnUI {
                delay(1000L)
                backPressed()
            }
        }, {
            XLog.w("registerUser failed: ${it.msg}")
            toastWarning("registerUser failed: ${it.msg}")
        }, {
            XLog.w("registerUser error: ${it.throwable.message}")
            toastWarning("registerUser error: ${it.throwable.message}")
        })
    }

}