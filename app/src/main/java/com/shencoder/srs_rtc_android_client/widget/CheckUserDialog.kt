package com.shencoder.srs_rtc_android_client.widget

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialog
import com.shencoder.mvvmkit.util.toastWarning
import com.shencoder.srs_rtc_android_client.R
import com.shencoder.srs_rtc_android_client.constant.CallType
import com.shencoder.srs_rtc_android_client.constant.ChatMode
import com.shencoder.srs_rtc_android_client.http.bean.UserInfoBean

/**
 *
 * @author  ShenBen
 * @date    2022/02/02 14:40
 * @email   714081644@qq.com
 */
typealias CheckUserCallback = (list: List<UserInfoBean>) -> Unit

class CheckUserDialog @JvmOverloads constructor(context: Context, theme: Int = R.style.MyDialog) :
    AppCompatDialog(context, theme) {

    private lateinit var checkUserView: CheckUserView

    private var callback: CheckUserCallback? = null
    private val unSelectedList = mutableListOf<UserInfoBean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_check_user)
        checkUserView = findViewById(R.id.cuv)!!
        checkUserView.setCheckUserCallback(object : CheckUserView.CheckUserCallback {
            override fun onClose() {
                dismiss()
            }

            override fun onCheckUser(list: List<UserInfoBean>, callType: CallType) {
                if (list.isEmpty()) {
                    context.toastWarning(context.getString(R.string.please_select_the_callee))
                    return
                }
                dismiss()
                callback?.invoke(list)
            }
        })
        checkUserView.setChatMode(ChatMode.GROUP_MODE)
    }

    fun setUnSelectedList(list: List<UserInfoBean>) {
        unSelectedList.clear()
        unSelectedList.addAll(list)
    }

    fun setCheckUserCallback(callback: CheckUserCallback) {
        this.callback = callback
    }

    override fun onStart() {
        super.onStart()
        checkUserView.setUnSelectedList(unSelectedList)

    }

}