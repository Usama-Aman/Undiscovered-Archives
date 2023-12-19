package com.codingpixel.undiscoveredarchives.home.favorites.adapters

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
import com.codingpixel.undiscoveredarchives.home.favorites.models.UserProductData
import com.codingpixel.undiscoveredarchives.utils.viewGone
import com.codingpixel.undiscoveredarchives.utils.viewVisible

class FavoriteItemAdapter(
    val favoriteProductList: MutableList<UserProductData?>,
    val favoriteInterface: FavoriteInterface,
    val isFavorite: Boolean = false
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var mContext: Context

    companion object {
        const val LOAD_MORE = 0
        const val ITEM = 1
    }

    interface FavoriteInterface {
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
            is LoadMore -> holder.onBind(position)
        }

    }

    override fun getItemCount(): Int = favoriteProductList.size

    override fun getItemViewType(position: Int): Int {
        return if (favoriteProductList[position] == null)
            LOAD_MORE
        else
            ITEM
    }

    inner class Item(val binding: ItemFavoritesBinding) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun onBind(position: Int) {
            if (isFavorite)
                binding.ivFavorite.viewVisible()
            else
                binding.ivFavorite.viewGone()

            if (!favoriteProductList[position]?.files.isNullOrEmpty())
                Glide.with(mContext)
                    .load(favoriteProductList[position]?.files?.get(0)?.file_path)
                    .placeholder(R.drawable.ic_file_placeholder)
                    .into(binding.ivProductImage)

            if (favoriteProductList[position]?.is_favourite == 1)
                binding.ivFavorite.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_heart_filled))
            else
                binding.ivFavorite.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_heart_unfilled))

            binding.tvProductCategory.text = favoriteProductList[position]?.category?.title
            binding.tvProductDescription.text = favoriteProductList[position]?.product_description
            binding.tvProductPrice.text = "$${favoriteProductList[position]?.variants_min_price}"

            binding.ivFavorite.setOnClickListener {
                Log.e("Interface", "ivFaV")
                favoriteInterface.onFavoriteClicked(position)
            }

            itemView.setOnClickListener {
                Log.e("InterfaceFav", "ItemFav")
                favoriteInterface.onItemClicked(position)
            }
        }
    }

    inner class LoadMore(val binding: ItemLoadMoreBinding) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(position: Int) {

        }
    }

}