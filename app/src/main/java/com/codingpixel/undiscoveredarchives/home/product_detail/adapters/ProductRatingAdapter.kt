package com.codingpixel.undiscoveredarchives.home.product_detail.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.codingpixel.undiscoveredarchives.R
import com.codingpixel.undiscoveredarchives.databinding.ItemProductRatingBinding
import com.codingpixel.undiscoveredarchives.home.favorites.models.Review

class ProductRatingAdapter(val productReviewsList: MutableList<Review>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var mContext: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        mContext = parent.context
        return Item(
            DataBindingUtil.inflate(LayoutInflater.from(parent.context), (R.layout.item_product_rating), parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is Item -> holder.bind(position)
        }
    }

    override fun getItemCount(): Int = productReviewsList.size

    inner class Item(val binding: ItemProductRatingBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {

            Glide.with(mContext)
                .load(productReviewsList[position].user.photo_path)
                .placeholder(R.drawable.ic_profile_placeholder)
                .into(binding.ivRatingUserImage)

            binding.tvRatingUserName.text = productReviewsList[position].user.name
            binding.tvComment.text = productReviewsList[position].comment
            binding.tvRatingDate.text = productReviewsList[position].created_at_formatted
            binding.ratingBar.rating = productReviewsList[position].rating.toFloat()
        }
    }
}