package com.codingpixel.undiscoveredarchives.home.favorites.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.codingpixel.undiscoveredarchives.R
import com.codingpixel.undiscoveredarchives.databinding.ItemFavoritesBinding

class FavoriteDesignerItemAdapter(
//    val designersProductsList: List<FavoriteDesignerProduct>,
    val favoriteDesignersItemInterface: FavoriteDesignersItemInterface
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var mContext: Context

    interface FavoriteDesignersItemInterface {
        fun onItemClicked(productPosition: Int)
        fun onFavoriteClicked(productPosition: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        mContext = parent.context

        return Item(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_favorites,
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

    override fun getItemCount(): Int = 10

    inner class Item(val binding: ItemFavoritesBinding) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun onBind(position: Int) {
//            binding.ivFavorite.viewVisible()
//
//            if (!designersProductsList[position].files.isNullOrEmpty())
//                Glide.with(mContext)
//                    .load(designersProductsList[position].files[0].file_path)
//                    .placeholder(R.drawable.ic_file_placeholder)
//                    .into(binding.ivProductImage)
//
//
////            binding.tvProductCategory.text = designersProductsList[position].category?.title
//            binding.tvProductDescription.text = designersProductsList[position].product_description
//            binding.tvProductPrice.text = "$${designersProductsList[position].variants_min_price}"
//
//            binding.ivFavorite.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_heart_filled))
//
//            binding.ivFavorite.setOnClickListener {
//                favoriteDesignersItemInterface.onFavoriteClicked(position)
//            }
//
//            binding.itemFavProduct.setOnClickListener {
//                favoriteDesignersItemInterface.onItemClicked(position)
//            }
        }

    }
}