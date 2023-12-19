package com.codingpixel.undiscoveredarchives.home.add_new_listing

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.codingpixel.undiscoveredarchives.R
import com.codingpixel.undiscoveredarchives.databinding.ItemListingZonesBinding
import com.codingpixel.undiscoveredarchives.home.favorites.models.ShipingZone
import com.codingpixel.undiscoveredarchives.utils.viewGone
import com.codingpixel.undiscoveredarchives.utils.viewVisible

class ShippingZonesAdapter(
    val shippingZones: List<ShipingZone>,
    val shippingZoneInterface: ShippingZonesInterface
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var mContext: Context

    interface ShippingZonesInterface {
        fun onZoneClicked(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        mContext = parent.context
        return Item(
            DataBindingUtil.inflate(LayoutInflater.from(parent.context), (R.layout.item_listing_zones), parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is Item -> holder.bind(position)
        }
    }

    override fun getItemCount(): Int = shippingZones.size

    inner class Item(val binding: ItemListingZonesBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {

            binding.checkbox.tag = position
            binding.checkbox.text = shippingZones[position].name
            binding.checkbox.isChecked = shippingZones[position].isChecked

            if (shippingZones[position].isChecked) {
                binding.llPrice.viewVisible()
                binding.etPrice.setText(shippingZones[position].pivot.shipping_charges)
            } else {
                binding.llPrice.viewGone()
                binding.etPrice.setText("")
            }


            binding.checkbox.setOnCheckedChangeListener { buttonView, _ ->
                if (buttonView.tag == position) {
                    shippingZones[position].isChecked = !shippingZones[position].isChecked
                    if (shippingZones[position].isChecked) {
                        binding.llPrice.viewVisible()
                        binding.etPrice.setText(shippingZones[position].pivot.shipping_charges)
                    } else {
                        binding.llPrice.viewGone()
                        shippingZones[position].pivot.shipping_charges = ""
                    }
                }
            }

            binding.etPrice.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    binding.llPrice.setBackgroundResource(R.drawable.auth_edittext_drawable)
                    binding.tilPrice.viewGone()
                    binding.tilPrice.isErrorEnabled = false
                    shippingZones[position].pivot.shipping_charges = s.toString()
                }
            })
        }

    }
}