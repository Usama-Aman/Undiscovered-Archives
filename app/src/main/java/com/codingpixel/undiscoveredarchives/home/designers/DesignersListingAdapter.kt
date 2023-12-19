package com.codingpixel.undiscoveredarchives.home.designers

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.codingpixel.undiscoveredarchives.R
import com.codingpixel.undiscoveredarchives.databinding.ItemDesignersBinding
import com.codingpixel.undiscoveredarchives.home.main.models.DesignersData

class DesignersListingAdapter(
    val designersList: MutableList<DesignersData>,
    val designerListingInterface: DesignerListingInterface
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var mContext: Context

    interface DesignerListingInterface {
        fun onItemClicked(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        mContext = parent.context
        val inflater = LayoutInflater.from(parent.context)
        val binding: ItemDesignersBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.item_designers,
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder -> {
                holder.onBind(position)
            }
        }
    }

    override fun getItemCount(): Int {
        return designersList.size
    }

    inner class ViewHolder(var binding: ItemDesignersBinding) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun onBind(position: Int) {

            Glide.with(mContext)
                .load(designersList[position].logo_path)
                .placeholder(R.drawable.ic_file_placeholder)
                .into(binding.ivDesignerImage)

            binding.tvDesignerName.text = designersList[position].name
            if (designersList[position].products_count == 1)
                binding.tvListingCount.text = "${designersList[position]?.products_count} Listing"
            else
                binding.tvListingCount.text = "${designersList[position]?.products_count} Listings"

            itemView.setOnClickListener {
                designerListingInterface.onItemClicked(position)
            }
        }
    }
}