package com.codingpixel.undiscoveredarchives.home.main.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.codingpixel.undiscoveredarchives.R
import com.codingpixel.undiscoveredarchives.databinding.ItemCategoryProductsBinding
import com.codingpixel.undiscoveredarchives.databinding.ItemProductCategoriesBinding
import com.codingpixel.undiscoveredarchives.home.main.models.CategoriesProduct

class CategoriesAdapter(
    private val isFilter: Boolean = false,
    val products: List<CategoriesProduct>,
    val categoriesInterface: CategoriesInterface
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var mContext: Context

    interface CategoriesInterface {
        fun onItemClicked(productPosition: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        mContext = parent.context
        if (isFilter)
            return FilterByCategoryItem(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_category_products,
                    parent,
                    false
                )
            )
        else
            return Item(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_product_categories,
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

    override fun getItemCount(): Int = products.size

    inner class Item(val binding: ItemProductCategoriesBinding) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun onBind(position: Int) {

            binding.tvProductDescription.text = products[position].product_description
            binding.tvProductPrice.text = "$${products[position].variants_min_price}"

            itemView.setOnClickListener {
                categoriesInterface.apply {
                    onItemClicked(position)
                }
            }

            if (!products[position].files.isNullOrEmpty())
                Glide.with(mContext)
                    .load(products[position].files[0].file_path)
                    .into(binding.ivProductImage)

        }

    }

    inner class FilterByCategoryItem(val binding: ItemCategoryProductsBinding) : RecyclerView.ViewHolder(binding.root) {

        fun onBind(position: Int) {

        }

    }

}