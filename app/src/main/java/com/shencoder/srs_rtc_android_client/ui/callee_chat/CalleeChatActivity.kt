package com.shencoder.srs_rtc_android_client.ui.callee_chat

import android.os.Bundle
import android.widget.Toast
import com.elvishew.xlog.XLog
import com.shencoder.mvvmkit.util.toastError
import com.shencoder.mvvmkit.util.toastInfo
import com.shencoder.mvvmkit.util.toastWarning
import com.shencoder.srs_rtc_android_client.BR
import com.shencoder.srs_rtc_android_client.R
import com.shencoder.srs_rtc_android_client.base.BaseActivity
import com.shencoder.srs_rtc_android_client.constant.MMKVConstant
import com.shencoder.srs_rtc_android_client.constant.SRS
import com.shencoder.srs_rtc_android_client.databinding.ActivityCalleeChatBinding
import com.shencoder.srs_rtc_android_client.helper.call.bean.ClientInfoBean
import com.shencoder.srs_rtc_android_client.helper.call.bean.PlayStreamBean
import com.shencoder.srs_rtc_android_client.helper.call.bean.RequestCallBean
import com.shencoder.srs_rtc_android_client.http.bean.UserInfoBean
import com.shencoder.srs_rtc_android_client.util.randomAvatar
import com.shencoder.srs_rtc_android_client.util.requestCallPermissions
import com.shencoder.srs_rtc_android_client.webrtc.bean.WebRTCStreamInfoBean
import com.shencoder.srs_rtc_android_client.webrtc.widget.CallLayout
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 私聊、群聊-被叫页
 */
class CalleeChatActivity : BaseActivity<CalleeChatViewModel, ActivityCalleeChatBinding>() {

    companion object {
        const val REQUEST_CALL = "REQUEST_CALL"
    }

    /**
     * 当前设备像SRS服务器推流地址
     */
    private lateinit var publishStreamUrl: String

    /**
     * 是否已经接受通话
     */
    private var acceptedCall = false

    /**
     * 未接收通话之前先把要拉的流暂存起来，接收通话之后统一播放
     * @see acceptedCall
     */
    private val tempPlayStreamList: MutableList<PlayStreamBean> = CopyOnWriteArrayList()

    override fun getLayoutId(): Int {
        return R.layout.activity_callee_chat
    }

    override fun injectViewModel(): Lazy<CalleeChatViewModel> {
        return viewModel()
    }

    override fun getViewModelId(): Int {
        return BR.viewModel
    }

    override fun initView() {
        mBinding.callLayout.setCallActionCallback(object : CallLayout.CallActionCallback {

            override fun rejectCall() {
                mViewModel.rejectCall()
            }

            override fun acceptCall() {
                mViewModel.acceptCall {
                    publishStream(publishStreamUrl) {
                        acceptedCall = true
                        tempPlayStreamList.forEach {
                            addPlayStream(it.userInfo, it.publishStreamUrl)
                        }
                        tempPlayStreamList.clear()

                        it.alreadyInRoomList.forEach { bean ->
                            addPlayStream(bean.userInfo, bean.publishStreamUrl)
                        }
                    }
                }
            }

            override fun hangUpCall() {
                mViewModel.hangUp()
            }
        })
    }

    override fun initData(savedInstanceState: Bundle?) {
        mViewModel.rejectCallLiveData.observe(this) {
            removePlayStream(it.userInfo, it.callEnded, getString(R.string.reject_call))
        }
        mViewModel.acceptCallLiveData.observe(this) {
            if (acceptedCall) {
                toastInfo("[${it.username}]${getString(R.string.accept_call)}")
            }
        }
        mViewModel.playSteamLiveData.observe(this) {
            if (acceptedCall) {
                addPlayStream(it.userInfo, it.publishStreamUrl)
            } else {
                //未接受通话时，先把数据加入暂存区
                tempPlayStreamList.add(it)
            }
        }
        mViewModel.hangUpLiveData.observe(this) {
            removePlayStream(it.userInfo, it.callEnded, getString(R.string.hang_up))
        }
        mViewModel.offlineDuringCallLiveData.observe(this) {
            removePlayStream(it.userInfo, it.callEnded, getString(R.string.offline))
        }

        //初始化
        mBinding.callLayout.init()

        requestCallPermissions { allGranted ->
            if (allGranted.not()) {
                toastWarning("Permission not granted.")
                mViewModel.delayBackPressed()
                return@requestCallPermissions
            }

            val requestCallBean = intent?.getParcelableExtra<RequestCallBean>(REQUEST_CALL)
            if (requestCallBean == null) {
                toastError("no request call info.")
                mViewModel.delayBackPressed()
                return@requestCallPermissions
            }
            mViewModel.setRoomId(requestCallBean.roomId)

            initCallLayout(requestCallBean)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mBinding.callLayout.release()
    }

    private fun initCallLayout(bean: RequestCallBean) {
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

            previewPublishStream(
                WebRTCStreamInfoBean(
                    localUserInfo.userId,
                    localUserInfo.userType,
                    localUserInfo.username,
                    randomAvatar(),
                    publishStreamUrl
                ), isShowPrompt = false
            )
            updatePreviewPublishStream(
                "[${bean.inviteInfo.username}]${getString(R.string.invite_you_to_call)}",
                randomAvatar()
            )
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
                mBinding.callLayout.removePlayStream(bean.userId, bean.userType)
            })
        }
    }

    /**
     * 删除播放流
     */
    private fun removePlayStream(bean: ClientInfoBean, isCallEnded: Boolean, toastStr: String) {
        if (isCallEnded) {
            toastInfo("[${bean.username}]${toastStr}")
            mViewModel.delayBackPressed()
            return
        }
        if (acceptedCall) {
            //已接受通话
            mBinding.callLayout.removePlayStream(bean.userId, bean.userType)
            toastInfo("[${bean.username}]${toastStr}")
        } else {
            //还未接受通话，从暂存区中删除
            tempPlayStreamList.removeAll { temp ->
                temp.userInfo.userId == bean.userId &&
                        temp.userInfo.userType == bean.userType
            }
        }
    }
}