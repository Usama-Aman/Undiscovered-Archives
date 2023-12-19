package com.codingpixel.undiscoveredarchives.home.cart

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.codingpixel.undiscoveredarchives.R
import com.codingpixel.undiscoveredarchives.databinding.ItemCartBinding

class CartAdapter(
    val cartList: MutableList<CartModel>,
    val cartInterface: CartInterface
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var mContext: Context

    interface CartInterface {
        fun updateCart(position: Int, quantity: Int)
        fun deleteSingleCartItem(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        mContext = parent.context
        return Item(
            DataBindingUtil.inflate(LayoutInflater.from(parent.context), (R.layout.item_cart), parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is Item -> holder.bind(position)
        }
    }

    override fun getItemCount(): Int = cartList.size

    inner class Item(val binding: ItemCartBinding) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
        fun bind(position: Int) {

            Glide.with(mContext)
                .load(cartList[position].product_image)
                .placeholder(R.drawable.ic_file_placeholder)
                .into(binding.ivProductImage)

            binding.tvProductCategory.text = cartList[position].product_category
            binding.tvProductDescription.text = cartList[position].product_description
            binding.tvProductPrice.text = "$${cartList[position].price}"
            binding.checkboxName.isChecked = cartList[position].isChecked

            itemView.setOnClickListener {
                cartList[position].isChecked = !cartList[position].isChecked
                notifyDataSetChanged()
            }

            binding.tvSelectedQuantity.text = cartList[position].quantity.toString()

            binding.ivQuantityPlus.setOnClickListener {
                cartList[position].quantity++
                cartInterface.updateCart(position, cartList[position].quantity)
            }

            binding.ivQuantityMinus.setOnClickListener {
                if (cartList[position].quantity > 1) {
                    cartList[position].quantity--
                    cartInterface.updateCart(position, cartList[position].quantity)
                }
            }

            binding.tvRemoveProduct.setOnClickListener {
                cartInterface.deleteSingleCartItem(position)
            }

        }

    }
}