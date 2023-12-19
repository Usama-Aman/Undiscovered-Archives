package com.codingpixel.undiscoveredarchives.home.main.adapters

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.codingpixel.undiscoveredarchives.R
import com.codingpixel.undiscoveredarchives.databinding.ItemHomeFilterChipsBinding
import com.codingpixel.undiscoveredarchives.home.main.models.CategoriesData

class CategoryFilterAdapter(val filterChips: ArrayList<CategoriesData>, val filterInterface: FilterInterface) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var mContext: Context

    interface FilterInterface {
        fun onFilterItemClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        mContext = parent.context
        return Item(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_home_filter_chips,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is Item)
            holder.onBind(position)
    }

    override fun getItemCount(): Int = filterChips.size

    inner class Item(val binding: ItemHomeFilterChipsBinding) : RecyclerView.ViewHolder(binding.root) {

        fun onBind(position: Int) {

            binding.tvFilterName.text = filterChips[position].title

            binding.root.setOnClickListener {
                binding.tvFilterName.setTextColor(ContextCompat.getColor(mContext, R.color.white))
                binding.cvFilter.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.black))
                Handler(Looper.getMainLooper()).postDelayed({
                    filterInterface.onFilterItemClick(position)
                }, 100)
                binding.cvFilter.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.white))
                binding.tvFilterName.setTextColor(ContextCompat.getColor(mContext, R.color.black))
            }
        }

    }

}