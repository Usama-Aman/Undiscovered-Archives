package com.codingpixel.undiscoveredarchives.home.product_detail.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.codingpixel.undiscoveredarchives.R
import com.codingpixel.undiscoveredarchives.databinding.ItemSuggestedProductsBinding
import com.codingpixel.undiscoveredarchives.home.all_products.AllProductsData

class SuggestedProductsAdapter(
    val suggestedProductsList: MutableList<AllProductsData?>,
    val suggestedProductsInterface: SuggestedProductsInterface
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var mContext: Context

    interface SuggestedProductsInterface {
        fun onItemClicked(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        mContext = parent.context
        return Item(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                (R.layout.item_suggested_products),
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

    override fun getItemCount(): Int = suggestedProductsList.size

    inner class Item(val binding: ItemSuggestedProductsBinding) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(position: Int) {

            if (!suggestedProductsList[position]?.files.isNullOrEmpty())
                Glide.with(mContext)
                    .load(suggestedProductsList[position]?.files?.get(0)?.file_path)
                    .placeholder(R.drawable.ic_file_placeholder)
                    .into(binding.ivProductImage)


            binding.tvProductDescription.text = suggestedProductsList[position]?.product_description
            binding.tvProductPrice.text = "$${suggestedProductsList[position]?.variants_min_price}"

            itemView.setOnClickListener {
                suggestedProductsInterface.apply {
                    onItemClicked(position)
                }
            }


        }
    }
}