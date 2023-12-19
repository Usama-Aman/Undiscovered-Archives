package com.codingpixel.undiscoveredarchives.home.add_new_listing

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.codingpixel.undiscoveredarchives.R
import com.codingpixel.undiscoveredarchives.base.AppController
import com.codingpixel.undiscoveredarchives.databinding.ItemProductVariantsBinding
import com.codingpixel.undiscoveredarchives.home.favorites.models.Variant

class AddVariantAdapter(
    private val variantsList: ArrayList<Variant>,
    val addVariantInterface: AddVariantInterface
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var mContext: Context

    interface AddVariantInterface {
        fun onRemoveClicked(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        mContext = parent.context
        return Item(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                (R.layout.item_product_variants),
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

    override fun getItemCount(): Int = variantsList.size

    inner class Item(val binding: ItemProductVariantsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
        fun bind(position: Int) {

            binding.tvVariantNumber.text = "Variant ${position + 1} Details"

            binding.tvSize.text = variantsList[position].sizeValue
            binding.tvNoOfItems.text = variantsList[position].quantity.toString()
            binding.tvPrice.text = "$${variantsList[position].price}"
            binding.tvCompareAtPrice.text =
                if (variantsList[position].compare_at_price.isEmpty()) "-" else "$${variantsList[position].compare_at_price}"
            binding.tvCommisionedPrice.text = variantsList[position].commissioned_price
            binding.tvSellerEarnings.text = "$${variantsList[position].seller_earnings}"

            binding.btnRemoveVariant.setOnClickListener {
                addVariantInterface.onRemoveClicked(position)
            }
        }

    }
}