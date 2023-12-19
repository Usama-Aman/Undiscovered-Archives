package com.codingpixel.undiscoveredarchives.home.all_products.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.codingpixel.undiscoveredarchives.R
import com.codingpixel.undiscoveredarchives.databinding.ItemAllProductsBinding
import com.codingpixel.undiscoveredarchives.home.main.adapters.CategoriesAdapter
import com.codingpixel.undiscoveredarchives.home.main.models.CategoriesData

class ProductsWithCategoriesAdapter(
    val productsWithCategoriesList: MutableList<CategoriesData?>,
    val productsWithCategoriesInterface: ProductsWithCategoriesInterface
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var mContext: Context

    interface ProductsWithCategoriesInterface {
        fun onItemClicked(categoryPosition: Int, productPosition: Int)
        fun onViewMoreClicked(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        mContext = parent.context
        return Item(
            DataBindingUtil.inflate(LayoutInflater.from(parent.context), (R.layout.item_all_products), parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is Item -> holder.bind(position)
        }
    }

    override fun getItemCount(): Int = productsWithCategoriesList.size

    inner class Item(val binding: ItemAllProductsBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {

            binding.tvCategoryName.text = productsWithCategoriesList[position]?.title

            binding.tvViewMore.setOnClickListener {
                productsWithCategoriesInterface.onViewMoreClicked(position)
            }

            binding.recyclerCategory.adapter =
                CategoriesAdapter(
                    products = productsWithCategoriesList[position]?.products ?: ArrayList(),
                    categoriesInterface = object : CategoriesAdapter.CategoriesInterface {
                        override fun onItemClicked(productPosition: Int) {
                            productsWithCategoriesInterface.onItemClicked(position, productPosition)
                        }
                    })
        }
    }
}