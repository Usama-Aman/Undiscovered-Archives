package com.codingpixel.undiscoveredarchives.home.notifications

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.codingpixel.undiscoveredarchives.R
import com.codingpixel.undiscoveredarchives.databinding.ItemNotificationsBinding


class NotificationsAdapter(val notificationsList: MutableList<NotificationsData>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var mContext: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        mContext = parent.context
        return Item(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                (R.layout.item_notifications), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is Item -> holder.bind(position)
        }
    }

    override fun getItemCount(): Int = notificationsList.size

    inner class Item(val binding: ItemNotificationsBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {

            if (notificationsList[position].is_read == 0) {
                binding.itemNotification.setBackgroundColor(
                    ContextCompat.getColor(
                        mContext,
                        R.color.unread_notification_background
                    )
                )
            } else
                binding.itemNotification.setBackgroundColor(ContextCompat.getColor(mContext, R.color.white))

//            Glide.with(mContext)
//                .load(notificationsList[position].)
//                    .into(binding.ivNotificationImage)

            binding.tvNotification.text = notificationsList[position].notification
            binding.tvNotificationTime.text = notificationsList[position].time
        }

    }
}