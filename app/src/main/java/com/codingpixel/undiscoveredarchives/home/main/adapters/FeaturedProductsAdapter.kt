package com.codingpixel.undiscoveredarchives.home.main.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.codingpixel.undiscoveredarchives.R
import com.codingpixel.undiscoveredarchives.databinding.ItemFeaturedProductsBinding
import com.codingpixel.undiscoveredarchives.home.main.models.FeaturedProductsData

class FeaturedProductsAdapter(
    val featuredProductList: MutableList<FeaturedProductsData>,
    val featureProductInterface: FeatureProductInterface
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface FeatureProductInterface {
        fun onProductItemClicked(position: Int)
    }

    private lateinit var mContext: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        mContext = parent.context
        return Item(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_featured_products,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is Item -> {
                holder.onBind(position)
            }
        }
    }

    override fun getItemCount(): Int = featuredProductList.size

    inner class Item(val binding: ItemFeaturedProductsBinding) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun onBind(position: Int) {

            binding.tvProductDescription.text = featuredProductList[position].product_description
            binding.tvProductPrice.text = "$${featuredProductList[position].variants_min_price}"

            if (!featuredProductList[position].files.isNullOrEmpty())
                Glide.with(mContext)
                    .load(featuredProductList[position].files[0].file_path)
                    .into(binding.ivProductImage)


            itemView.setOnClickListener {
                featureProductInterface.onProductItemClicked(position)
            }

        }

    }

}