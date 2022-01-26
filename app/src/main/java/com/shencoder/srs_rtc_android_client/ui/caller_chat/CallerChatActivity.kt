package com.shencoder.srs_rtc_android_client.ui.caller_chat

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.shencoder.mvvmkit.ext.launchOnUI
import com.shencoder.mvvmkit.ext.toastWarning
import com.shencoder.mvvmkit.util.toastWarning
import com.shencoder.srs_rtc_android_client.BR
import com.shencoder.srs_rtc_android_client.R
import com.shencoder.srs_rtc_android_client.base.BaseActivity
import com.shencoder.srs_rtc_android_client.constant.MMKVConstant
import com.shencoder.srs_rtc_android_client.constant.SRS
import com.shencoder.srs_rtc_android_client.databinding.ActivityCallerChatBinding
import com.shencoder.srs_rtc_android_client.http.bean.UserInfoBean
import com.shencoder.srs_rtc_android_client.util.randomAvatar
import com.shencoder.srs_rtc_android_client.util.requestCallPermissions
import com.shencoder.srs_rtc_android_client.webrtc.bean.WebRTCStreamInfoBean
import com.shencoder.srs_rtc_android_client.webrtc.widget.CallLayout
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * 私聊、群聊-主叫页
 */
class CallerChatActivity : BaseActivity<CallerChatViewModel, ActivityCallerChatBinding>() {

    companion object {
        /**
         * 被叫信息
         */
        const val CALLEE_INFO_LIST = "CALLEE_INFO_LIST"
    }

    /**
     * 被叫用户集合
     */
    private lateinit var calleeInfoList: ArrayList<UserInfoBean>

    override fun getLayoutId(): Int {
        return R.layout.activity_caller_chat
    }

    override fun injectViewModel(): Lazy<CallerChatViewModel> {
        return viewModel()
    }

    override fun getViewModelId(): Int {
        return BR.viewModel
    }

    override fun initView() {
        mBinding.callLayout.setCallActionCallback(object : CallLayout.CallActionCallback {

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
            calleeInfoList = intent?.getParcelableArrayListExtra(CALLEE_INFO_LIST) ?: ArrayList()
            initCallLayout(calleeInfoList)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mBinding.callLayout.release()
    }


    private fun initCallLayout(list: List<UserInfoBean>) {
        if (list.isEmpty()) {
            toastWarning("callee info list is empty.")
            return
        }
        //本地用户信息
        val localUserInfo =
            mmkv.decodeParcelable(MMKVConstant.USER_INFO, UserInfoBean::class.java)
        if (localUserInfo == null) {
            toastWarning("local user info is null.")
            return
        }

        with(mBinding.callLayout) {
            init()

            previewPublishStream(
                WebRTCStreamInfoBean(
                    localUserInfo.userId,
                    localUserInfo.userType,
                    localUserInfo.username,
                    randomAvatar(),
                    SRS.generatePublishWebRTCUrl(localUserInfo)
                ), list.size != 1
            )
            if (list.size == 1) {
                //会见仅为一人时
                val calleeUserInfo = list[0]
                //显示被叫信息
                updatePreviewPublishStream(calleeUserInfo.username, randomAvatar())

                mViewModel.reqInviteSomeone(calleeUserInfo.userId) {
                    //开始推流
                    publishStream(
                        onSuccess = {

                        },
                        onFailure = {

                        })
                }
            } else {
                //会见多人时
                list.forEach { userInfo ->
                    addPreparePlayStream(
                        WebRTCStreamInfoBean(
                            userInfo.userId,
                            userInfo.userType,
                            userInfo.username,
                            randomAvatar()
                        )
                    )
                }

                mViewModel.reqInviteSomePeople(list.map { it.userId }, success = { bean ->
                    lifecycleScope.launch {
                        val msg = buildString {
                            if (bean.busyList.isNotEmpty()) {
                                append("busyList: [")
                                bean.busyList.forEachIndexed { index, info ->
                                    append(info.username)
                                    if (index != bean.busyList.lastIndex) {
                                        append("、")
                                    }
                                }
                                append("], ")
                            }

                            if (bean.offlineOrNotExistsList.isNotEmpty()) {
                                append("offlineOrNotExistsList: [")
                                bean.offlineOrNotExistsList.forEachIndexed { index, info ->
                                    append(info.username)
                                    if (index != bean.offlineOrNotExistsList.lastIndex) {
                                        append("、")
                                    }
                                }
                                append("], ")
                            }

                            if (bean.alreadyInRoomList.isNotEmpty()) {
                                append("alreadyInRoomList: [")
                                bean.alreadyInRoomList.forEachIndexed { index, info ->
                                    append(info.username)
                                    if (index != bean.alreadyInRoomList.lastIndex) {
                                        append("、")
                                    }
                                }
                                append("]")
                            }
                        }

                        if (msg.isNotBlank()) {
                            toastWarning(msg, Toast.LENGTH_LONG)
                        }

                        //延迟一下 体验好点
                        delay(1000L)
                        bean.busyList.forEach { info ->
                            removePlayStream(info.userId, info.userType)
                        }
                        bean.offlineOrNotExistsList.forEach { info ->
                            removePlayStream(info.userId, info.userType)
                        }
                        bean.alreadyInRoomList.forEach { info ->
                            removePlayStream(info.userId, info.userType)
                        }

                        //开始推流
                        publishStream(
                            onSuccess = {

                            },
                            onFailure = {

                            })
                    }
                })
            }
        }
    }

}