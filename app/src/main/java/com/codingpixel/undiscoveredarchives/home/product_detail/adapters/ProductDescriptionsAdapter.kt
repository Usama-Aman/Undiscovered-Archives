package com.codingpixel.undiscoveredarchives.home.product_detail.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.codingpixel.undiscoveredarchives.R
import com.codingpixel.undiscoveredarchives.databinding.ItemProductDescriptionBinding
import com.codingpixel.undiscoveredarchives.home.product_detail.MeasurementModel

class ProductDescriptionsAdapter(val descriptionList: ArrayList<MeasurementModel>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var mContext: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        mContext = parent.context
        return Item(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                (R.layout.item_product_description),
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

    override fun getItemCount(): Int = descriptionList.size

    inner class Item(val binding: ItemProductDescriptionBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {

            binding.tvDescriptionType.text = descriptionList[position].type
            binding.tvDescription.text = descriptionList[position].value

            binding.root.setOnClickListener {

                binding.expandableLayout.toggle()

                if (binding.expandableLayout.isExpanded)
                    binding.ivArrow.scaleY = -1f
                else
                    binding.ivArrow.scaleY = +1f
            }

        }
    }
}