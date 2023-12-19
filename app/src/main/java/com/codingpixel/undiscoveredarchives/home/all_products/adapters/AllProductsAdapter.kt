package com.codingpixel.undiscoveredarchives.home.all_products.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.codingpixel.undiscoveredarchives.R
import com.codingpixel.undiscoveredarchives.databinding.ItemCategoryProductsBinding
import com.codingpixel.undiscoveredarchives.home.all_products.AllProductsData

class AllProductsAdapter(
    val products: List<AllProductsData?>,
    val allProductsInterface: AllProductsInterface
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var mContext: Context

    interface AllProductsInterface {
        fun onItemClicked(productPosition: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        mContext = parent.context
        return Item(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_category_products,
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

    inner class Item(val binding: ItemCategoryProductsBinding) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun onBind(position: Int) {

            if (!products[position]?.files.isNullOrEmpty())
                Glide.with(mContext)
                    .load(products[position]?.files?.get(0)?.file_path)
                    .placeholder(R.drawable.ic_file_placeholder)
                    .into(binding.ivProductImage)


            binding.tvProductDescription.text = products[position]?.product_description
            binding.tvProductPrice.text = "$${products[position]?.variants_min_price}"

            itemView.setOnClickListener {
                allProductsInterface.apply {
                    onItemClicked(position)
                }
            }
        }

    }

}