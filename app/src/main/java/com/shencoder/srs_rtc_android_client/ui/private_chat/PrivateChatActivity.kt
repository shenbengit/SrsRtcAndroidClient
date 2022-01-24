package com.shencoder.srs_rtc_android_client.ui.private_chat

import android.os.Bundle
import com.shencoder.mvvmkit.util.toastWarning
import com.shencoder.srs_rtc_android_client.BR
import com.shencoder.srs_rtc_android_client.R
import com.shencoder.srs_rtc_android_client.base.BaseActivity
import com.shencoder.srs_rtc_android_client.constant.CallRoleType
import com.shencoder.srs_rtc_android_client.constant.MMKVConstant
import com.shencoder.srs_rtc_android_client.constant.SRS
import com.shencoder.srs_rtc_android_client.databinding.ActivityPrivateChatBinding
import com.shencoder.srs_rtc_android_client.http.bean.UserInfoBean
import com.shencoder.srs_rtc_android_client.util.randomAvatar
import com.shencoder.srs_rtc_android_client.util.requestCallPermissions
import com.shencoder.srs_rtc_android_client.webrtc.bean.WebRTCStreamInfoBean
import com.shencoder.srs_rtc_android_client.webrtc.constant.IntoRoomType
import com.shencoder.srs_rtc_android_client.webrtc.widget.CallLayout
import com.tencent.mmkv.MMKV
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * 私聊
 */
class PrivateChatActivity : BaseActivity<PrivateChatViewModel, ActivityPrivateChatBinding>(),
    KoinComponent {

    companion object {
        /**
         * 通话角色
         * 主叫还是被叫
         */
        const val CALL_ROLE_TYPE = "CALL_ROLE_TYPE"

        /**
         * 被叫信息
         */
        const val CALLEE_INFO = "CALLEE_INFO"

        /**
         * 被叫进入房间
         */
        const val ROOM_ID = "ROOM_ID"
    }

    private val mmkv: MMKV by inject()

    private lateinit var callRoleType: CallRoleType

    /**
     * 被叫信息
     * 仅在[callRoleType] = [CallRoleType.CALLER]时判断是否有值
     */
    private var inviteeUserInfo: UserInfoBean? = null

    /**
     * 被叫进入房间
     * 仅在[callRoleType] = [CallRoleType.CALLEE]时判断是否有值
     */
    private var roomId: String? = null

    override fun getLayoutId(): Int {
        return R.layout.activity_private_chat
    }

    override fun injectViewModel(): Lazy<PrivateChatViewModel> {
        return viewModel()
    }

    override fun getViewModelId(): Int {
        return BR.viewModel
    }

    override fun initView() {
        mBinding.callLayout.setCallActionCallback(object : CallLayout.CallActionCallback {

            override fun rejectCall() {

            }

            override fun acceptCall() {

            }

            override fun hangUpCall() {

            }
        })
    }

    override fun initData(savedInstanceState: Bundle?) {
        requestCallPermissions { allGranted ->
            if (allGranted) {
                intent?.run {
                    callRoleType = getSerializableExtra(CALL_ROLE_TYPE) as CallRoleType
                    when (callRoleType) {
                        CallRoleType.CALLER -> {
                            val userInfo: UserInfoBean? = getParcelableExtra(CALLEE_INFO)
                            if (userInfo == null) {
                                toastWarning("no callee info.")
                                return@run
                            }
                            inviteeUserInfo = userInfo
                            initCallLayout(IntoRoomType.INVITE_INTO_ROOM)
                        }
                        CallRoleType.CALLEE -> {
                            val roomId = getStringExtra(ROOM_ID)
                            if (roomId == null) {
                                toastWarning("no room id.")
                                return@run
                            }
                            this@PrivateChatActivity.roomId = roomId
                            initCallLayout(IntoRoomType.BE_INVITED_INTO_ROOM)
                        }
                    }
                }
            } else {
                toastWarning("Permission not granted.")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mBinding.callLayout.release()
    }

    private fun initCallLayout(intoRoomType: IntoRoomType) {
        val userInfo: UserInfoBean? =
            mmkv.decodeParcelable(MMKVConstant.USER_INFO, UserInfoBean::class.java)
        if (userInfo == null) {
            toastWarning("local user info is null.")
            return
        }
        mBinding.callLayout.init(intoRoomType)
        //预览本地推流
        mBinding.callLayout.previewPublishStream(
            WebRTCStreamInfoBean(
                userId = userInfo.userId,
                userType = userInfo.userType,
                username = getString(R.string.mine),
                avatar = randomAvatar(),
                webrtcUrl = SRS.generatePublishWebRTCUrl(userInfo)
            )
        )
    }
}