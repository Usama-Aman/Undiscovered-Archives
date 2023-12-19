package com.codingpixel.undiscoveredarchives.home.product_detail.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.codingpixel.undiscoveredarchives.R
import com.codingpixel.undiscoveredarchives.databinding.ItemProductDetailPhotoBinding
import com.codingpixel.undiscoveredarchives.home.favorites.models.File


class ProductDetailPhotosAdapter(val productPhotos: MutableList<File>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var mContext: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        mContext = parent.context
        return Item(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                (R.layout.item_product_detail_photo),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is Item -> holder.bind(position)
        }
    }

    override fun getItemCount(): Int = productPhotos.size

    inner class Item(val binding: ItemProductDetailPhotoBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {
            Glide.with(mContext)
                .load(productPhotos[position].file_path)
//                .apply(RequestOptions().centerCrop())
                .placeholder(R.drawable.ic_file_placeholder)
                .into(binding.ivProductImage)
        }
    }
}