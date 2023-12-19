package com.codingpixel.undiscoveredarchives.home.search_result

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.codingpixel.undiscoveredarchives.R
import com.codingpixel.undiscoveredarchives.databinding.ItemFavoritesBinding
import com.codingpixel.undiscoveredarchives.databinding.ItemLoadMoreBinding
import com.codingpixel.undiscoveredarchives.home.all_products.AllProductsData

class SearchResultAdapter(
    val productList: MutableList<AllProductsData?>,
    val searchResultInterface: SearchResultInterface
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var mContext: Context

    companion object {
        const val LOAD_MORE = 0
        const val ITEM = 1
    }

    interface SearchResultInterface {
        fun onItemClicked(position: Int)
        fun onFavoriteClicked(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        mContext = parent.context
        return if (viewType == ITEM) Item(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_favorites,
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
            is Item -> holder.onBind(position)
            is LoadMore -> holder.onBind()
        }

    }

    override fun getItemCount(): Int = productList.size

    override fun getItemViewType(position: Int): Int = if (productList[position] == null) LOAD_MORE else ITEM

    inner class Item(val binding: ItemFavoritesBinding) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun onBind(position: Int) {

            if (!productList[position]?.files.isNullOrEmpty())
                Glide.with(mContext)
                    .load(productList[position]?.files?.get(0)?.file_path)
                    .placeholder(R.drawable.ic_file_placeholder)
                    .into(binding.ivProductImage)

            if (productList[position]?.is_favourite == 1)
                binding.ivFavorite.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_heart_filled))
            else
                binding.ivFavorite.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_heart_unfilled))

            binding.tvProductCategory.text = productList[position]?.category?.title
            binding.tvProductDescription.text = productList[position]?.product_description
            binding.tvProductPrice.text = "$${productList[position]?.variants_min_price}"

            binding.ivFavorite.setOnClickListener {
                Log.e("Interface", "ivFaV")
                searchResultInterface.onFavoriteClicked(position)
            }

            itemView.setOnClickListener {
                Log.e("InterfaceFav", "ItemFav")
                searchResultInterface.onItemClicked(position)
            }

        }
    }

    inner class LoadMore(val binding: ItemLoadMoreBinding) : RecyclerView.ViewHolder(binding.root) {
        fun onBind() {}
    }

}