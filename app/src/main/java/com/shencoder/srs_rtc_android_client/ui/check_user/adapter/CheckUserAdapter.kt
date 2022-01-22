package com.shencoder.srs_rtc_android_client.ui.check_user.adapter

import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.shencoder.srs_rtc_android_client.R
import com.shencoder.srs_rtc_android_client.constant.ChatMode
import com.shencoder.srs_rtc_android_client.databinding.ItemCheckUserBinding
import com.shencoder.srs_rtc_android_client.http.bean.UserInfoBean

/**
 *
 * @author  ShenBen
 * @date    2022/1/19 17:23
 * @email   714081644@qq.com
 */
class CheckUserAdapter :
    BaseQuickAdapter<UserInfoBean, BaseDataBindingHolder<ItemCheckUserBinding>>(R.layout.item_check_user) {

    private companion object {
        private const val UPDATE_CHECKED = "UPDATE_CHECKED"
    }

    private var chatMode = ChatMode.PRIVATE_MODE
    private var lastCheckedPosition = RecyclerView.NO_POSITION

    fun setChatMode(chatMode: ChatMode) {
        this.chatMode = chatMode
        lastCheckedPosition = RecyclerView.NO_POSITION

        if (data.isNotEmpty()) {
            data.forEach { it.isSelected = false }
            notifyDataSetChanged()
        }
    }

    override fun onItemViewHolderCreated(
        viewHolder: BaseDataBindingHolder<ItemCheckUserBinding>,
        viewType: Int
    ) {
        viewHolder.dataBinding?.run {
            cbSelected.setOnCheckedChangeListener { _, isChecked ->
                val position = viewHolder.bindingAdapterPosition - headerLayoutCount
                val item = getItem(position)
                if (item.selectable.not()) {
                    return@setOnCheckedChangeListener
                }
                item.isSelected = isChecked
                when (chatMode) {
                    ChatMode.PRIVATE_MODE -> {
                        //私聊-单选
                        if (isChecked) {
                            //选中状态
                            if (lastCheckedPosition != RecyclerView.NO_POSITION) {
                                getItem(lastCheckedPosition).isSelected = false
                                notifyItemChanged(lastCheckedPosition, UPDATE_CHECKED)
                            }
                            lastCheckedPosition = position
                        }
                    }
                    ChatMode.GROUP_MODE -> {
                        //群聊-多选
                    }
                }
            }
        }
    }

    override fun convert(
        holder: BaseDataBindingHolder<ItemCheckUserBinding>,
        item: UserInfoBean,
        payloads: List<Any>
    ) {
        val any = payloads[0]
        if (any is String) {
            if (any == UPDATE_CHECKED) {
                val cbSelected: CheckBox = holder.getView(R.id.cbSelected)
                cbSelected.isChecked = item.isSelected
            }
        }
    }

    override fun convert(holder: BaseDataBindingHolder<ItemCheckUserBinding>, item: UserInfoBean) {
        val avatarDrawable = when ((holder.bindingAdapterPosition - headerLayoutCount) % 5) {
            0 -> R.drawable.ic_avatar01
            1 -> R.drawable.ic_avatar02
            2 -> R.drawable.ic_avatar03
            3 -> R.drawable.ic_avatar04
            else -> R.drawable.ic_avatar05
        }
        holder.run {
            this.dataBinding?.item = item
            setImageResource(R.id.ivAvatar, avatarDrawable)
            val cbSelected: CheckBox = getView(R.id.cbSelected)
            cbSelected.isChecked = item.isSelected
            cbSelected.isEnabled = item.selectable
        }
    }
}
