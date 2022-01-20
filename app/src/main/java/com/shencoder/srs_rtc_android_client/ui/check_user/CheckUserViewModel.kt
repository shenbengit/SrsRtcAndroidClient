package com.shencoder.srs_rtc_android_client.ui.check_user

import android.app.Application
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.elvishew.xlog.XLog
import com.shencoder.mvvmkit.base.viewmodel.BaseViewModel
import com.shencoder.mvvmkit.ext.httpRequest
import com.shencoder.mvvmkit.ext.toastInfo
import com.shencoder.mvvmkit.ext.toastWarning
import com.shencoder.mvvmkit.util.dp2px
import com.shencoder.srs_rtc_android_client.R
import com.shencoder.srs_rtc_android_client.constant.ChatMode
import com.shencoder.srs_rtc_android_client.constant.MMKVConstant
import com.shencoder.srs_rtc_android_client.http.bean.UserInfoBean
import com.shencoder.srs_rtc_android_client.ui.check_user.adapter.CheckUserAdapter
import com.shencoder.srs_rtc_android_client.ui.check_user.data.CheckUserRepository
import com.shencoder.srs_rtc_android_client.ui.group_chat.GroupChatActivity
import com.shencoder.srs_rtc_android_client.ui.private_chat.PrivateChatActivity

/**
 *
 * @author  ShenBen
 * @date    2022/1/19 17:06
 * @email   714081644@qq.com
 */
class CheckUserViewModel(
    application: Application,
    repo: CheckUserRepository
) : BaseViewModel<CheckUserRepository>(application, repo) {

    val adapter = CheckUserAdapter()

    val itemDecoration = object : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            val padding = application.dp2px(10f)
            outRect.set(padding, padding, padding, padding)
        }
    }


    private lateinit var chatMode: ChatMode
    private val unSelectedSet: MutableSet<UserInfoBean> = LinkedHashSet()


    fun initData(chatMode: ChatMode, unSelectedList: List<UserInfoBean>?) {
        this.chatMode = chatMode
        if (unSelectedList != null) {
            unSelectedSet.addAll(unSelectedList)
        }
        //自身也不可选择
        val selfUserBean = mmkv.decodeParcelable(MMKVConstant.USER_INFO, UserInfoBean::class.java)
        if (selfUserBean != null) {
            unSelectedSet.add(selfUserBean)
        }

        adapter.setChatMode(chatMode)

        httpRequest({
            repo.getAllUser()
        }, {
            val data = it.data
            if (data == null) {
                toastWarning("userList is null.")
                return@httpRequest
            }
            analyticalData(data, unSelectedSet)
        }, {
            XLog.w("getAllUser failed: ${it.msg}")
            toastWarning("getAllUser failed: ${it.msg}")
        }, {
            XLog.w("getAllUser error: ${it.throwable.message}")
            toastWarning("getAllUser error: ${it.throwable.message}")
        })
    }

    /**
     * 解析数据
     */
    private fun analyticalData(list: List<UserInfoBean>, unSelectedSet: Set<UserInfoBean>) {
        list.forEach { item ->
            item.selectable = unSelectedSet.contains(item).not()
        }
        adapter.setList(list)
    }

    /**
     * 确认
     */
    fun confirm() {
        val list = adapter.data.filter { it.selectable && it.isSelected }
        if (list.isEmpty()) {
            toastWarning(getString(R.string.please_select_the_callee))
            return
        }
        val intent: Intent
        when (chatMode) {
            ChatMode.PRIVATE_MODE -> {
                val userInfoBean = list[0]
                intent = Intent(applicationContext, PrivateChatActivity::class.java)
                intent.putExtra(PrivateChatActivity.CALLEE_INFO, userInfoBean)

            }
            ChatMode.GROUP_MODE -> {
                intent = Intent(applicationContext, GroupChatActivity::class.java)
                intent.putParcelableArrayListExtra(GroupChatActivity.CALLEE_INFO_LIST, ArrayList(list))
            }
        }
        startActivity(intent)
        backPressed()
    }
}