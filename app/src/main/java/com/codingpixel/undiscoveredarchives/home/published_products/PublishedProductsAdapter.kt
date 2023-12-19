package com.codingpixel.undiscoveredarchives.home.published_products

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.codingpixel.undiscoveredarchives.R
import com.codingpixel.undiscoveredarchives.databinding.ItemLoadMoreBinding
import com.codingpixel.undiscoveredarchives.databinding.ItemProductsBinding
import com.codingpixel.undiscoveredarchives.home.favorites.models.UserProductData
import com.codingpixel.undiscoveredarchives.home.published_products.PublishedProductsActivity.Companion.isSelectEnable
import com.codingpixel.undiscoveredarchives.utils.viewGone
import com.codingpixel.undiscoveredarchives.utils.viewVisible

class PublishedProductsAdapter(
    val publishedProductsList: MutableList<UserProductData?>,
    val publishedProductInterface: PublishedProductInterface
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var mContext: Context

    interface PublishedProductInterface {
        fun onItemClicked(position: Int)
        fun showListOptions(position: Int)
    }

    companion object {
        const val LOAD_MORE = 0
        const val ITEM = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        mContext = parent.context
        return if (viewType == ITEM) Item(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                (R.layout.item_products),
                parent,
                false
            )
        )
        else
            LoadMore(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_load_more,
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

    override fun getItemCount(): Int = publishedProductsList.size

    override fun getItemViewType(position: Int): Int {
        return if (publishedProductsList[position] == null)
            LOAD_MORE
        else
            ITEM
    }

    inner class Item(val binding: ItemProductsBinding) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(position: Int) {

            if (isSelectEnable)
                binding.checkboxName.viewVisible()
            else
                binding.checkboxName.viewGone()

            if (!publishedProductsList[position]?.files.isNullOrEmpty())
                Glide.with(mContext)
                    .load(publishedProductsList[position]?.files?.get(0)?.file_path)
                    .placeholder(R.drawable.ic_file_placeholder)
                    .into(binding.ivProductImage)

            binding.tvProductCategory.text = publishedProductsList[position]?.category?.title
            binding.tvProductDescription.text = publishedProductsList[position]?.product_name
            binding.tvProductPrice.text = "$${publishedProductsList[position]?.variants_min_price}"
            binding.checkboxName.isChecked = (publishedProductsList[position]?.isChecked == true)

            itemView.setOnClickListener {
                if (isSelectEnable) {
                    publishedProductInterface.showListOptions(position)
                } else {
                    publishedProductInterface.onItemClicked(position)
                }
            }
        }

    }

    inner class LoadMore(val binding: ItemLoadMoreBinding) : RecyclerView.ViewHolder(binding.root) {
        fun onBind() {}
    }
}