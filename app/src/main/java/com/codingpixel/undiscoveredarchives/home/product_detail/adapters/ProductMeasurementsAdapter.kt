package com.codingpixel.undiscoveredarchives.home.product_detail.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.codingpixel.undiscoveredarchives.R
import com.codingpixel.undiscoveredarchives.databinding.ItemProductMeasurementsBinding
import com.codingpixel.undiscoveredarchives.home.product_detail.MeasurementModel

class ProductMeasurementsAdapter(val measurementList: ArrayList<MeasurementModel>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var mContext: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        mContext = parent.context
        return Item(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                (R.layout.item_product_measurements),
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

    override fun getItemCount(): Int = measurementList.size

    inner class Item(val binding: ItemProductMeasurementsBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {
            binding.tvMeasurementType.text = measurementList[position].type
            binding.tvMeasurement.text = measurementList[position].value
        }
    }
}