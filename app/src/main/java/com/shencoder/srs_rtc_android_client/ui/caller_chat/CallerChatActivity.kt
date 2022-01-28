package com.shencoder.srs_rtc_android_client.ui.caller_chat

import android.os.Bundle
import android.widget.Toast
import com.elvishew.xlog.XLog
import com.shencoder.mvvmkit.util.toastWarning
import com.shencoder.srs_rtc_android_client.BR
import com.shencoder.srs_rtc_android_client.R
import com.shencoder.srs_rtc_android_client.base.BaseActivity
import com.shencoder.srs_rtc_android_client.constant.MMKVConstant
import com.shencoder.srs_rtc_android_client.constant.SRS
import com.shencoder.srs_rtc_android_client.databinding.ActivityCallerChatBinding
import com.shencoder.srs_rtc_android_client.helper.call.bean.ClientInfoBean
import com.shencoder.srs_rtc_android_client.http.bean.UserInfoBean
import com.shencoder.srs_rtc_android_client.util.randomAvatar
import com.shencoder.srs_rtc_android_client.util.requestCallPermissions
import com.shencoder.srs_rtc_android_client.webrtc.bean.WebRTCStreamInfoBean
import com.shencoder.srs_rtc_android_client.webrtc.widget.CallLayout
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
     * 实际的被叫用户集合
     */
    private val calleeInfoList = mutableListOf<ClientInfoBean>()

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
            val list: ArrayList<UserInfoBean> =
                intent?.getParcelableArrayListExtra(CALLEE_INFO_LIST) ?: ArrayList()
            initCallLayout(list)
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
            onBackPressedSupport()
            return
        }

        with(mBinding.callLayout) {
            init()

            val publishStreamUrl = SRS.generatePublishWebRTCUrl(localUserInfo)
            XLog.i("caller chat publish stream url: $publishStreamUrl")

            previewPublishStream(
                WebRTCStreamInfoBean(
                    localUserInfo.userId,
                    localUserInfo.userType,
                    localUserInfo.username,
                    randomAvatar(),
                    publishStreamUrl
                ), list.size != 1
            )
            if (list.size == 1) {
                //会见仅为一人时
                val calleeUserInfo = list[0]
                //显示被叫信息
                updatePreviewPublishStream(calleeUserInfo.username, randomAvatar())

                mViewModel.reqInviteSomeone(calleeUserInfo.userId) {
                    mViewModel.setRoomId(it.roomId)

                    this@CallerChatActivity.publishStream(publishStreamUrl) {
                        //存储当前会见的所有被叫信息
                        calleeInfoList.add(it.inviteeInfo)
                        addPreparePlayStream(
                            WebRTCStreamInfoBean(
                                it.inviteeInfo.userId,
                                it.inviteeInfo.userType,
                                it.inviteeInfo.username,
                                randomAvatar()
                            )
                        )
                    }
                }
            } else {
                //会见多人时
                mViewModel.reqInviteSomePeople(list.map { it.userId }, success = { bean ->
                    mViewModel.setRoomId(bean.roomId)
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

                    //开始推流
                    this@CallerChatActivity.publishStream(publishStreamUrl) {
                        //存储当前会见的所有被叫信息
                        calleeInfoList.addAll(bean.callList)

                        bean.callList.forEach { clientInfo ->
                            addPreparePlayStream(
                                WebRTCStreamInfoBean(
                                    clientInfo.userId,
                                    clientInfo.userType,
                                    clientInfo.username,
                                    randomAvatar()
                                )
                            )
                        }
                    }
                })
            }
        }
    }

    private fun publishStream(streamUrl: String, success: () -> Unit) {
        mBinding.callLayout.publishStream(onSuccess = {
            mViewModel.publishStream(streamUrl) {
                success.invoke()
            }
        }, onFailure = {

        })
    }

}