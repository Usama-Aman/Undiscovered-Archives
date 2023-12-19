package com.codingpixel.undiscoveredarchives.home.main.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.codingpixel.undiscoveredarchives.R
import com.codingpixel.undiscoveredarchives.databinding.ItemTopDesignersBinding
import com.codingpixel.undiscoveredarchives.home.main.models.DesignersData

class TopDesignersAdapter(val designersList: MutableList<DesignersData>, val designersInterface: DesignersInterface) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var mContext: Context

    interface DesignersInterface {
        fun onItemClicked(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        mContext = parent.context
        return Item(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_top_designers,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is Item -> holder.onBind(position)
        }
    }

    override fun getItemCount(): Int = designersList.size

    inner class Item(val binding: ItemTopDesignersBinding) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun onBind(position: Int) {

            binding.tvDesignerName.text = designersList[position].name
            binding.tvListingCount.text = "${designersList[position].products_count} Listings"

            Glide.with(mContext)
                .load(designersList[position].logo_path)
                .into(binding.ivDesignerImage)

            itemView.setOnClickListener {
                designersInterface.onItemClicked(position)
            }
        }

    }

}