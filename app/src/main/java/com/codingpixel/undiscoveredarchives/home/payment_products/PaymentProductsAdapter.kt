package com.codingpixel.undiscoveredarchives.home.payment_products

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.codingpixel.undiscoveredarchives.R
import com.codingpixel.undiscoveredarchives.databinding.ItemProductsBinding
import com.codingpixel.undiscoveredarchives.home.payment_products.PaymentProductsActivity.Companion.isSelectEnable
import com.codingpixel.undiscoveredarchives.utils.viewGone
import com.codingpixel.undiscoveredarchives.utils.viewVisible

class PaymentProductsAdapter(val paymentProductsInterface: PaymentProductsInterface) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var mContext: Context

    interface PaymentProductsInterface {
        fun onItemClicked(position: Int)
        fun showListOptions(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        mContext = parent.context
        return Item(
            DataBindingUtil.inflate(LayoutInflater.from(parent.context), (R.layout.item_products), parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is Item -> holder.bind(position)
        }
    }

    override fun getItemCount(): Int = 20

    inner class Item(val binding: ItemProductsBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {

            if (isSelectEnable)
                binding.checkboxName.viewVisible()
            else
                binding.checkboxName.viewGone()

            /*TODO make it single selection*/

            binding.itemProduct.setOnClickListener {
                if (isSelectEnable) {
                    paymentProductsInterface.showListOptions(position)
                } else {
                    paymentProductsInterface.onItemClicked(position)
                }
            }

        }

    }
}