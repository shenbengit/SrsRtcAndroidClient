package com.shencoder.srs_rtc_android_client.widget

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.elvishew.xlog.XLog
import com.google.android.material.button.MaterialButton
import com.shencoder.mvvmkit.util.dp2px
import com.shencoder.mvvmkit.util.toastWarning
import com.shencoder.srs_rtc_android_client.R
import com.shencoder.srs_rtc_android_client.constant.ChatMode
import com.shencoder.srs_rtc_android_client.constant.MMKVConstant
import com.shencoder.srs_rtc_android_client.http.RetrofitClient
import com.shencoder.srs_rtc_android_client.http.bean.UserInfoBean
import com.shencoder.srs_rtc_android_client.ui.check_user.adapter.CheckUserAdapter
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.coroutines.CoroutineContext

/**
 *
 * @author  ShenBen
 * @date    2022/02/02 14:30
 * @email   714081644@qq.com
 */
class CheckUserView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    FrameLayout(context, attrs, defStyleAttr), CoroutineScope, KoinComponent {
    /**
     * 协程
     */
    private val scope = SupervisorJob() + Dispatchers.Main.immediate
    private val retrofitClient: RetrofitClient by inject()

    private val mmkv: MMKV by inject()

    override val coroutineContext: CoroutineContext
        get() = scope

    private val mRecyclerView: RecyclerView

    private val itemDecoration = object : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            val padding = context.dp2px(10f)
            outRect.set(padding, padding, padding, padding)
        }
    }

    private val mAdapter = CheckUserAdapter()

    private val originList = mutableListOf<UserInfoBean>()
    private val unSelectedList = mutableListOf<UserInfoBean>()

    private var chatMode: ChatMode = ChatMode.GROUP_MODE
    private var callback: CheckUserCallback? = null


    init {
        inflate(context, R.layout.layout_check_user, this)
        mRecyclerView = findViewById(R.id.rvUserList)
        mRecyclerView.adapter = mAdapter
        mRecyclerView.layoutManager = LinearLayoutManager(context)
        mRecyclerView.addItemDecoration(itemDecoration)

        findViewById<ImageButton>(R.id.ibClose).setOnClickListener {
            callback?.onClose()
        }
        findViewById<MaterialButton>(R.id.btnConfirm).setOnClickListener {
            callback?.onCheckUser(getCheckList())
        }

        launch(Dispatchers.Main) {
            runCatching {
                withContext(Dispatchers.IO) {
                    retrofitClient.getApiService().getAllUser()
                }
            }.onSuccess {
                if (it.isSuccess()) {
                    val data = it.data
                    if (data != null) {
                        originList.addAll(data)
                    }
                    setChatMode(chatMode)
                    val list = unSelectedList.toList()
                    setUnSelectedList(list)
                } else {
                    XLog.w("getAllUser failed,code:${it.code}, msg: ${it.msg}")
                    context.toastWarning("getAllUser failed: ${it.msg}")
                }
            }.onFailure {
                XLog.w("getAllUser error: ${it.message}")
                context.toastWarning("getAllUser error: ${it.message}")
            }
        }
    }

    fun setCheckUserCallback(callback: CheckUserCallback?) {
        this.callback = callback
    }

    fun setChatMode(chatMode: ChatMode) {
        this.chatMode = chatMode
        mAdapter.setChatMode(chatMode)
    }

    fun setUnSelectedList(list: List<UserInfoBean>) {
        unSelectedList.clear()
        unSelectedList.addAll(list)

        val localUserInfo =
            mmkv.decodeParcelable(MMKVConstant.USER_INFO, UserInfoBean::class.java)
        //先还原数据
        originList.forEach {
            it.selectable = true
            it.isSelected = false
        }
        originList.forEach { item ->
            if (item == localUserInfo) {
                item.isSelected = true
                item.selectable = false
                return@forEach
            }
            val contains = list.contains(item)
            item.selectable = contains.not()
            item.isSelected = contains
        }
        mAdapter.setList(originList)
    }

    fun getCheckList(): List<UserInfoBean> {
        return mAdapter.data.filter { it.selectable && it.isSelected }
    }

    fun release() {
        scope.cancel()
    }

    interface CheckUserCallback {
        fun onClose()

        fun onCheckUser(list: List<UserInfoBean>)
    }
}