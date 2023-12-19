package com.codingpixel.undiscoveredarchives.home.trash_products

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
import com.codingpixel.undiscoveredarchives.home.favorites.adapters.FavoriteItemAdapter
import com.codingpixel.undiscoveredarchives.home.favorites.models.UserProductData
import com.codingpixel.undiscoveredarchives.home.trash_products.TrashProductsActivity.Companion.isSelectEnable
import com.codingpixel.undiscoveredarchives.utils.viewGone
import com.codingpixel.undiscoveredarchives.utils.viewVisible

class TrashProductsAdapter(
    val trashProductsList: MutableList<UserProductData?>,
    val trashProductsInterface: TrashProductsInterface
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var mContext: Context

    interface TrashProductsInterface {
        fun onItemClicked(position: Int)
        fun showListOptions(position: Int)
    }

    companion object {
        const val LOAD_MORE = 0
        const val ITEM = 1
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

    override fun getItemCount(): Int = trashProductsList.size

    override fun getItemViewType(position: Int): Int {
        return if (trashProductsList[position] == null)
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

            if (!trashProductsList[position]?.files.isNullOrEmpty())
                Glide.with(mContext)
                    .load(trashProductsList[position]?.files?.get(0)?.file_path)
                    .placeholder(R.drawable.ic_file_placeholder)
                    .into(binding.ivProductImage)

            binding.tvProductCategory.text = trashProductsList[position]?.category?.title
            binding.tvProductDescription.text = "$${trashProductsList[position]?.product_name}"
            binding.tvProductPrice.text = trashProductsList[position]?.variants_min_price
            binding.checkboxName.isChecked = (trashProductsList[position]?.isChecked == true)

            itemView.setOnClickListener {
                if (isSelectEnable) {
                    trashProductsInterface.showListOptions(position)
                } else {
                    trashProductsInterface.onItemClicked(position)
                }
            }
        }
    }


    inner class LoadMore(val binding: ItemLoadMoreBinding) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(position: Int) {}
    }
}