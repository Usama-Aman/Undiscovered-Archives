package com.codingpixel.undiscoveredarchives.home.main.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.codingpixel.undiscoveredarchives.R
import com.codingpixel.undiscoveredarchives.databinding.ItemMainCategoriesBinding
import com.codingpixel.undiscoveredarchives.home.main.models.CategoriesData

class CategoryMainAdapter(
    val categoriesList: MutableList<CategoriesData>,
    val homeCategoryInterface: HomeCategoryInterface
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var mContext: Context

    interface HomeCategoryInterface {
        fun onViewMoreClicked(position: Int)
        fun onProductItemClicked(categoryPosition: Int, productPosition: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        mContext = parent.context
        return Item(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_main_categories,
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

    override fun getItemCount(): Int = categoriesList.size

    inner class Item(val binding: ItemMainCategoriesBinding) : RecyclerView.ViewHolder(binding.root) {

        fun onBind(position: Int) {

            binding.tvProductType.text = categoriesList[position].title
            binding.categoriesRecyclerView.adapter = CategoriesAdapter(products = categoriesList[position].products,
                categoriesInterface = object : CategoriesAdapter.CategoriesInterface {
                    override fun onItemClicked(productPosition: Int) {
                        homeCategoryInterface.onProductItemClicked(position, productPosition)
                    }
                })
            binding.tvViewMore.setOnClickListener {
                homeCategoryInterface.onViewMoreClicked(position)
            }

        }

    }


}