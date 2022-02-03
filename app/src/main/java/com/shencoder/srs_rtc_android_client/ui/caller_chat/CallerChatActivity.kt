package com.shencoder.srs_rtc_android_client.ui.caller_chat

import android.os.Bundle
import android.widget.Toast
import androidx.core.view.isVisible
import com.elvishew.xlog.XLog
import com.shencoder.mvvmkit.util.toastError
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
import com.shencoder.srs_rtc_android_client.widget.CheckUserDialog
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
    private val calleeInfoList = mutableSetOf<ClientInfoBean>()

    private lateinit var checkUserDialog: CheckUserDialog

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
        checkUserDialog = CheckUserDialog(this).apply {
            setCheckUserCallback { list ->
                if (list.size == 1) {
                    mViewModel.reqInviteSomeoneIntoRoom(list[0].userId) {
                        addPlayStream(it.inviteeInfo, null)
                    }
                } else {
                    mViewModel.reqInviteSomePeopleIntoRoom(list.map { it.userId }) {
                        it.callList.forEach { bean->
                            addPlayStream(bean,null)
                        }
                    }
                }
            }
        }
        mBinding.run {
            btnAdd.setOnClickListener {
                checkUserDialog.run {
                    kotlin.runCatching {
                        setUnSelectedList(calleeInfoList.map {
                            UserInfoBean(
                                it.createdAt,
                                it.id,
                                it.userId,
                                it.userType,
                                it.username
                            )
                        })
                        show()
                    }.onFailure {
                        it.printStackTrace()
                    }

                }
            }
            callLayout.setCallActionCallback(object : CallLayout.CallActionCallback {

                override fun hangUpCall() {
                    mViewModel.hangUp()
                }
            })
        }
    }

    override fun initData(savedInstanceState: Bundle?) {
        mViewModel.inviteSomeoneIntoRoomLiveData.observe(this) {
            addPlayStream(it.inviteeInfo, null)
        }
        mViewModel.inviteSomePeopleIntoRoomLiveData.observe(this) {
            it.inviteeInfoList.forEach { bean ->
                addPlayStream(bean, null)
            }
        }
        mViewModel.rejectCallLiveData.observe(this) {
            removePlayStream(it.userInfo)
            if (it.callEnded) {
                mViewModel.delayBackPressed()
            }
        }
        mViewModel.acceptCallLiveData.observe(this) {
            mBinding.callLayout.setInCallStatus()
            //开始推流
            this@CallerChatActivity.publishStream(publishStreamUrl) {
                XLog.i("caller publish stream success.")
                //推流成功再显示添加按钮
                mBinding.btnAdd.isVisible = true
            }
        }
        mViewModel.playSteamLiveData.observe(this) {
            addPlayStream(it.userInfo, it.publishStreamUrl)
        }
        mViewModel.hangUpLiveData.observe(this) {
            removePlayStream(it.userInfo)
            if (it.callEnded) {
                mViewModel.delayBackPressed()
            }
        }
        mViewModel.offlineDuringCallLiveData.observe(this) {
            removePlayStream(it.userInfo)
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
                    //再显示回自己的信息
//                    updatePreviewPublishStream(getString(R.string.mine), publishStreamBean.avatar)
//                    this@CallerChatActivity.addPlayStream(it.inviteeInfo, null)
                }
            } else {
                //会见多人时
                mViewModel.reqInviteSomePeople(list.map { it.userId }, success = { bean ->
                    bean.callList.forEach { clientInfo ->
                        this@CallerChatActivity.addPlayStream(clientInfo, null)
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
            XLog.e("caller publish failure: ${it.message}")
            toastError("publish failure: ${it.message}")
        })
    }

    private fun addPlayStream(bean: ClientInfoBean, publishStreamUrl: String?) {
        //存储当前会见的所有被叫信息
        calleeInfoList.add(bean)
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