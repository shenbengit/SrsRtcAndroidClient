package com.shencoder.srs_rtc_android_client.ui.check_user.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.shencoder.srs_rtc_android_client.http.bean.UserInfoBean

/**
 *
 * @author  ShenBen
 * @date    2022/1/19 17:23
 * @email   714081644@qq.com
 */
class CheckUserAdapter : RecyclerView.Adapter<CheckUserAdapter.Holder>() {

    private val mData = mutableListOf<UserInfoBean>()

    fun setList(list: List<UserInfoBean>?) {
        mData.clear()
        if (list != null) {
            mData.addAll(list)
        }
        notifyDataSetChanged()
    }

    fun getItem(position: Int) = mData[position]

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val holder = Holder(LayoutInflater.from(parent.context).inflate(0, parent, false))
        holder.itemView.setOnClickListener {

        }
        return holder
    }

    override fun onBindViewHolder(holder: Holder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {

        }
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {

    }

    override fun getItemCount(): Int {
        return mData.size
    }


    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }

}
