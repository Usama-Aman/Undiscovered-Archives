package com.codingpixel.undiscoveredarchives.home.main.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.codingpixel.undiscoveredarchives.R
import com.codingpixel.undiscoveredarchives.databinding.ItemShopByCategoryBinding
import com.codingpixel.undiscoveredarchives.home.main.models.CategoriesData

class ShopByCategoryAdapter(
    private val shopByCategoryList: MutableList<CategoriesData>,
    val shopByCategoryInterface: ShopByCategoryInterface
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var mContext: Context

    interface ShopByCategoryInterface {
        fun onItemClicked(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        mContext = parent.context
        return Item(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_shop_by_category,
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

    override fun getItemCount(): Int = shopByCategoryList.size

    inner class Item(val binding: ItemShopByCategoryBinding) : RecyclerView.ViewHolder(binding.root) {

        fun onBind(position: Int) {

            binding.tvCategoryName.text = shopByCategoryList[position].title

            Glide.with(mContext)
                .load(shopByCategoryList[position].image_path)
                .into(binding.ivShopImage)

            itemView.setOnClickListener {
                shopByCategoryInterface.onItemClicked(position)
            }
        }

    }

}