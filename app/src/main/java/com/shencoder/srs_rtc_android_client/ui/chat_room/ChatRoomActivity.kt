package com.shencoder.srs_rtc_android_client.ui.chat_room

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
import com.shencoder.srs_rtc_android_client.databinding.ActivityChatRoomBinding
import com.shencoder.srs_rtc_android_client.helper.call.bean.ClientInfoBean
import com.shencoder.srs_rtc_android_client.http.bean.UserInfoBean
import com.shencoder.srs_rtc_android_client.util.randomAvatar
import com.shencoder.srs_rtc_android_client.util.requestCallPermissions
import com.shencoder.srs_rtc_android_client.webrtc.bean.WebRTCStreamInfoBean
import com.shencoder.srs_rtc_android_client.webrtc.widget.CallLayout
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * 聊天室
 */
class ChatRoomActivity : BaseActivity<ChatRoomViewModel, ActivityChatRoomBinding>() {

    companion object {
        const val ROOM_ID = "ROOM_ID"
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_chat_room
    }

    override fun injectViewModel(): Lazy<ChatRoomViewModel> {
        return viewModel()
    }

    override fun getViewModelId(): Int {
        return BR.viewModel
    }

    override fun initView() {
        mBinding.callLayout.setCallActionCallback(object : CallLayout.CallActionCallback {

            override fun hangUpCall() {
                mViewModel.leaveChatRoom()
            }
        })
    }

    override fun initData(savedInstanceState: Bundle?) {
        val roomId = intent?.getStringExtra(ROOM_ID)
        if (roomId.isNullOrBlank()) {
            toastWarning("roomId is empty.")
            mViewModel.delayBackPressed()
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

        mViewModel.joinChatRoomLiveData.observe(this) { bean ->
            toastInfo("[${bean.username}] join room.")
            XLog.i("userId:${bean.userId}, userType: ${bean.userType}, userName: ${bean.username} join room.")
            addPlayStream(bean, null)
        }
        mViewModel.playSteamLiveData.observe(this) { bean ->
            val userInfo = bean.userInfo
            XLog.i("play stream, userId:${userInfo.userId}, userType: ${userInfo.userType}, userName: ${userInfo.username}, stream url: ${bean.publishStreamUrl}.")
            addPlayStream(bean.userInfo, bean.publishStreamUrl)
        }
        mViewModel.leaveChatRoomLiveData.observe(this) { bean ->
            toastInfo("[${bean.username}] leave room.")
            XLog.i("userId:${bean.userId}, userType: ${bean.userType}, userName: ${bean.username} leave room.")
            mBinding.callLayout.removePlayStream(bean.userId, bean.userType)
        }

        mBinding.callLayout.init()

        requestCallPermissions { allGranted ->
            if (allGranted.not()) {
                toastWarning("Permission not granted.")
                mViewModel.delayBackPressed()
                return@requestCallPermissions
            }
            val publishStreamUrl = SRS.generatePublishWebRTCUrl(localUserInfo)
            XLog.i("chat room publish stream url: $publishStreamUrl")
            mBinding.callLayout.previewPublishStream(
                WebRTCStreamInfoBean(
                    localUserInfo.userId,
                    localUserInfo.userType,
                    localUserInfo.username,
                    randomAvatar(),
                    publishStreamUrl
                )
            )
            mViewModel.setRoomId(roomId)
            mViewModel.joinChatRoom { inRoomBean ->
                //拉房间中已存在的流
                inRoomBean.alreadyInRoomList.forEach { stream ->
                    addPlayStream(stream.userInfo, stream.publishStreamUrl)
                }
                //推流
                mBinding.callLayout.publishStream(onSuccess = {
                    XLog.i("chat room publish success.")
                    //向信令服务器发送流信息
                    mViewModel.publishStream(publishStreamUrl) {

                    }
                }, onFailure = {
                    XLog.w("chat room publish failure: ${it.message}.")
                    toastError("publish failure: ${it.message}.")
                })
            }
        }
    }

    override fun onDestroy() {
        mBinding.callLayout.release()
        super.onDestroy()
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
                removePlayStream(bean.userId, bean.userType)
            })
        }
    }
}