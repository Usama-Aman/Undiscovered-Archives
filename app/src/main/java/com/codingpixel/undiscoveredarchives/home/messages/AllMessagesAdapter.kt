package com.codingpixel.undiscoveredarchives.home.messages

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.codingpixel.undiscoveredarchives.R
import com.codingpixel.undiscoveredarchives.databinding.ItemAllMessagesBinding

class AllMessagesAdapter(val allMessagesInterface: AllMessagesInterface) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var mContext: Context

    interface AllMessagesInterface {
        fun onItemClicked(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        mContext = parent.context
        return Item(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                (R.layout.item_all_messages), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is Item -> holder.bind(position)
        }
    }

    override fun getItemCount(): Int = 15

    inner class Item(val binding: ItemAllMessagesBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {
            binding.root.setOnClickListener {
                allMessagesInterface.onItemClicked(position)
            }
        }
    }
}