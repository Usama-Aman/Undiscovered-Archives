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
import com.codingpixel.undiscoveredarchives.databinding.ItemFavDesignerBinding
import com.codingpixel.undiscoveredarchives.databinding.ItemLoadMoreBinding
import com.codingpixel.undiscoveredarchives.home.main.models.DesignersData

class FavoriteDesignAdapter(
    private val designersList: MutableList<DesignersData?>,
    val favoriteDesignersInterface: FavoriteDesignersInterface
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var mContext: Context

    companion object {
        const val LOAD_MORE = 0
        const val ITEM_TYPE = 1
    }

    interface FavoriteDesignersInterface {
        fun onItemClicked(designerPosition: Int)
        fun onFavoriteClicked(designerPosition: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        mContext = parent.context

        return if (viewType == ITEM_TYPE) Item(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_fav_designer,
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

    override fun getItemViewType(position: Int): Int {
        return if (designersList[position] == null) LOAD_MORE
        else ITEM_TYPE
    }

    override fun getItemCount(): Int = designersList.size

    inner class Item(val binding: ItemFavDesignerBinding) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun onBind(position: Int) {
            if (!designersList[position]?.logo_path.isNullOrEmpty())
                Glide.with(mContext)
                    .load(designersList[position]?.logo_path)
                    .placeholder(R.drawable.ic_file_placeholder)
                    .into(binding.ivDesignerImage)

            if (designersList[position]?.is_favourite == 1)
                binding.ivFavorite.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_heart_filled))
            else
                binding.ivFavorite.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_heart_unfilled))

            binding.tvDesignerName.text = designersList[position]?.name
            if (designersList[position]?.products_count == 1)
                binding.tvListingCount.text = "${designersList[position]?.products_count} Listing"
            else
                binding.tvListingCount.text = "${designersList[position]?.products_count} Listings"


            binding.ivFavorite.setOnClickListener {
                Log.e("Interface", "ivFaV")
                favoriteDesignersInterface.onFavoriteClicked(position)
            }

            itemView.setOnClickListener {
                Log.e("InterfaceFav", "ItemFav")
                favoriteDesignersInterface.onItemClicked(position)
            }

        }
    }

    inner class LoadMore(val binding: ItemLoadMoreBinding) : RecyclerView.ViewHolder(binding.root) {
        fun onBind() {

        }
    }
}