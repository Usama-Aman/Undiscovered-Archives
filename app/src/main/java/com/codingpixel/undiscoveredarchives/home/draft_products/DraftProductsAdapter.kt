package com.codingpixel.undiscoveredarchives.home.draft_products

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
import com.codingpixel.undiscoveredarchives.home.draft_products.DraftProductsActivity.Companion.isSelectEnable
import com.codingpixel.undiscoveredarchives.home.favorites.adapters.FavoriteItemAdapter
import com.codingpixel.undiscoveredarchives.home.favorites.models.UserProductData
import com.codingpixel.undiscoveredarchives.utils.viewGone
import com.codingpixel.undiscoveredarchives.utils.viewVisible

class DraftProductsAdapter(
    val draftProductsList: MutableList<UserProductData?>,
    var draftProductsInterface: DraftProductsInterface
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var mContext: Context

    companion object {
        const val LOAD_MORE = 0
        const val ITEM = 1
    }

    interface DraftProductsInterface {
        fun onItemClicked(position: Int)
        fun showListOptions(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        mContext = parent.context
        return if (viewType == FavoriteItemAdapter.ITEM)
            Item(
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

    override fun getItemCount(): Int = draftProductsList.size

    override fun getItemViewType(position: Int): Int {
        return if (draftProductsList[position] == null)
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

            if (!draftProductsList[position]?.files.isNullOrEmpty())
                Glide.with(mContext)
                    .load(draftProductsList[position]?.files?.get(0)?.file_path)
                    .placeholder(R.drawable.ic_file_placeholder)
                    .into(binding.ivProductImage)

            binding.tvProductCategory.text = draftProductsList[position]?.category?.title
            binding.tvProductDescription.text = draftProductsList[position]?.product_name
            binding.tvProductPrice.text = "$${draftProductsList[position]?.variants_min_price}"
            binding.checkboxName.isChecked = (draftProductsList[position]?.isChecked == true)

            itemView.setOnClickListener {
                if (isSelectEnable) {
                    draftProductsInterface.showListOptions(position)
                } else {
                    draftProductsInterface.onItemClicked(position)
                }
            }
        }

    }


    inner class LoadMore(val binding: ItemLoadMoreBinding) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(position: Int) {}
    }
}