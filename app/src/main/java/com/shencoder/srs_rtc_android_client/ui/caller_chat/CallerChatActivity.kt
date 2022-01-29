package com.shencoder.srs_rtc_android_client.ui.caller_chat

import android.os.Bundle
import android.widget.Toast
import com.elvishew.xlog.XLog
import com.shencoder.mvvmkit.util.toastError
import com.shencoder.mvvmkit.util.toastInfo
import com.shencoder.mvvmkit.util.toastSuccess
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

    /**
     * 当前设备像SRS服务器推流地址
     */
    private lateinit var publishStreamUrl: String

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
                mViewModel.hangUp()
            }
        })
    }

    override fun initData(savedInstanceState: Bundle?) {
        mViewModel.rejectCallLiveData.observe(this) {
            removePlayStream(it.userInfo)
            toastInfo("[${it.userInfo.username}]${getString(R.string.reject_call)}")
            if (it.callEnded) {
                mViewModel.delayBackPressed()
            }
        }
        mViewModel.acceptCallLiveData.observe(this) {
            mBinding.callLayout.setInCallStatus()
            toastSuccess("[${it.username}]${getString(R.string.accept_call)}")
            //开始推流
            this@CallerChatActivity.publishStream(publishStreamUrl) {
                XLog.i("caller publish stream success.")
            }
        }
        mViewModel.playSteamLiveData.observe(this) {
            addPlayStream(it.userInfo, it.publishStreamUrl)
        }
        mViewModel.hangUpLiveData.observe(this) {
            removePlayStream(it.userInfo)
            toastInfo("[${it.userInfo.username}]${getString(R.string.hang_up)}")
            if (it.callEnded) {
                mViewModel.delayBackPressed()
            }
        }
        mViewModel.offlineDuringCallLiveData.observe(this) {
            removePlayStream(it.userInfo)
            toastInfo("[${it.userInfo.username}]${getString(R.string.offline)}")
            if (it.callEnded) {
                mViewModel.delayBackPressed()
            }
        }

        mBinding.callLayout.init()

        requestCallPermissions { allGranted ->
            if (allGranted.not()) {
                toastWarning("Permission not granted.")
                mViewModel.delayBackPressed()
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
            mViewModel.delayBackPressed()
            return
        }

        with(mBinding.callLayout) {
            publishStreamUrl = SRS.generatePublishWebRTCUrl(localUserInfo)
            XLog.i("caller chat publish stream url: $publishStreamUrl")

            val publishStreamBean = WebRTCStreamInfoBean(
                localUserInfo.userId,
                localUserInfo.userType,
                localUserInfo.username,
                randomAvatar(),
                publishStreamUrl
            )

            previewPublishStream(publishStreamBean, isShowPrompt = list.size != 1)

            if (list.size == 1) {
                //会见仅为一人时
                val calleeUserInfo = list[0]
                //显示被叫信息
                updatePreviewPublishStream(calleeUserInfo.username, randomAvatar())

                mViewModel.reqInviteSomeone(calleeUserInfo.userId) {
                    mViewModel.setRoomId(it.roomId)
                    //再显示回自己的信息
//                    updatePreviewPublishStream(getString(R.string.mine), publishStreamBean.avatar)
//                    this@CallerChatActivity.addPlayStream(it.inviteeInfo, null)

                    //存储当前会见的所有被叫信息
                    calleeInfoList.add(it.inviteeInfo)
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
                            append("offlineOrNotExistsList: userId[")
                            bean.offlineOrNotExistsList.forEachIndexed { index, info ->
                                append(info.userId)
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

                    bean.callList.forEach { clientInfo ->
                        this@CallerChatActivity.addPlayStream(clientInfo, null)
                    }
                    //存储当前会见的所有被叫信息
                    calleeInfoList.addAll(bean.callList)

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
            XLog.e("")
        })
    }

    private fun addPlayStream(bean: ClientInfoBean, publishStreamUrl: String?) {
        //播放流
        mBinding.callLayout.run {
            mBinding.callLayout.addPlayStream(WebRTCStreamInfoBean(
                bean.userId,
                bean.userType,
                bean.username,
                randomAvatar(),
                publishStreamUrl
            ), onSuccess = {
                XLog.i("play stream success: userId:${bean.userId}, userType: ${bean.userType}, userName: ${bean.username}")
            }, onFailure = {
                toastError(
                    "username: ${bean.username} play stream failure: ${it.message}.",
                    Toast.LENGTH_LONG
                )
                XLog.e("play stream failure: userId:${bean.userId}, userType: ${bean.userType}, userName: ${bean.username}, reason: ${it.message}")
                removePlayStream(bean)
            })
        }
    }

    private fun removePlayStream(bean: ClientInfoBean) {
        mBinding.callLayout.removePlayStream(bean.userId, bean.userType)
        calleeInfoList.remove(bean)
    }
}