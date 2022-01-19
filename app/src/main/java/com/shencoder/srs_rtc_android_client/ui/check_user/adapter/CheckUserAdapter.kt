package com.shencoder.srs_rtc_android_client.ui.check_user.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.shencoder.srs_rtc_android_client.R
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

    override fun convert(
        holder: BaseDataBindingHolder<ItemCheckUserBinding>,
        item: UserInfoBean,
        payloads: List<Any>
    ) {

    }

    override fun convert(holder: BaseDataBindingHolder<ItemCheckUserBinding>, item: UserInfoBean) {

    }
}
