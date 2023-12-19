package com.codingpixel.undiscoveredarchives.home.purchases

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.codingpixel.undiscoveredarchives.R
import com.codingpixel.undiscoveredarchives.databinding.ItemFavoritesBinding
import com.codingpixel.undiscoveredarchives.databinding.ItemLoadMoreBinding
import com.codingpixel.undiscoveredarchives.utils.viewGone

class PurchaseAdapter(val purchaseOrdersList: MutableList<UserOrdersData?>, val purchaseInterface: PurchaseInterface) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var mContext: Context

    companion object {
        const val LOAD_MORE = 0
        const val ITEM = 1
    }

    interface PurchaseInterface {
        fun onItemClicked(position: Int)
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

    override fun getItemCount(): Int = purchaseOrdersList.size

    override fun getItemViewType(position: Int): Int {
        return if (purchaseOrdersList[position] == null)
            LOAD_MORE
        else
            ITEM
    }

    inner class Item(val binding: ItemFavoritesBinding) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun onBind(position: Int) {
            binding.ivFavorite.viewGone()
//
//            if (!purchaseOrdersList[position]?.items.files.isNullOrEmpty())
//                Glide.with(mContext)
//                    .load(purchaseOrdersList[position]?.files?.get(0)?.file_path)
//                    .into(binding.ivProductImage)
//
//            binding.tvProductCategory.text = "#${purchaseOrdersList[position]?.order_number}"
//            binding.tvProductDescription.text = purchaseOrdersList[position]?.item
//            binding.tvProductPrice.text = "$${purchaseOrdersList[position]?.variants_min_price}"
//
//            itemView.setOnClickListener {
//                Log.e("InterfaceFav", "ItemFav")
//                purchaseInterface.onItemClicked(position)
//            }
        }
    }

    inner class LoadMore(val binding: ItemLoadMoreBinding) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(position: Int) {}
    }

}