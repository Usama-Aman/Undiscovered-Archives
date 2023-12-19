package com.codingpixel.undiscoveredarchives.home.shipping_products

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.codingpixel.undiscoveredarchives.R
import com.codingpixel.undiscoveredarchives.base.AppController
import com.codingpixel.undiscoveredarchives.base.BaseActivity
import com.codingpixel.undiscoveredarchives.databinding.ActivityShippingProductsBinding
import com.codingpixel.undiscoveredarchives.home.add_new_listing.AddListingActivity
import com.codingpixel.undiscoveredarchives.home.order_detail.OrderDetailActivity
import com.codingpixel.undiscoveredarchives.home.published_products.PublishedProductsActivity
import com.codingpixel.undiscoveredarchives.utils.*

class ShippingProductsActivity : BaseActivity() {
    private lateinit var binding: ActivityShippingProductsBinding
    private lateinit var mContext: Context
    private lateinit var shippingProductsAdapter: ShippingProductsAdapter

    private lateinit var dropDownAdapter: ArrayAdapter<DropdownModel>

    companion object {
        var isSelectEnable = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShippingProductsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mContext = this
        blackStatusBarIcons()

        initViews()
        initListeners()
        initAdapter()
    }

    @SuppressLint("SetTextI18n")
    private fun initViews() {
        binding.topBar.ivBack.viewVisible()
        binding.topBar.tvScreenTitle.text = "Shipping"

        /*Filter*/
        dropDownAdapter = CustomDropDownAdapter(mContext, AppController.filterDropDownList)
        binding.filter.adapter = dropDownAdapter
    }

    @SuppressLint("SetTextI18n")
    private fun initListeners() {
        /*Filter*/
        binding.filter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val orderBy = AppController.filterDropDownList[position].value
                AppController.filterDropDownList.filter { it.isSelected }.forEach { it.isSelected = false }
                AppController.filterDropDownList[position].isSelected = true

                binding.tvFilter.text = orderBy
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.topBar.ivBack.setOnClickListener {
            finish()
        }

        binding.tvHeader.setOnClickListener {
            if (binding.llNoShippingProducts.isVisible()) {
                binding.llNoShippingProducts.viewGone()
                binding.constraintProducts.viewVisible()
            } else {
                binding.llNoShippingProducts.viewVisible()
                binding.constraintProducts.viewGone()
            }
        }

        binding.btnStartSelling.setOnClickListener {
            startActivity(Intent(this, AddListingActivity::class.java))
        }


        binding.tvSelect.setOnClickListener {
            isSelectEnable = !isSelectEnable

            if (isSelectEnable)
                binding.tvSelect.text = "Cancel"
            else
                binding.tvSelect.text = "Select"

            /*TODO uncheck the selected item for next time selection*/

            shippingProductsAdapter.notifyDataSetChanged()
        }
    }

    private fun initAdapter() {
        shippingProductsAdapter =
            ShippingProductsAdapter(object : ShippingProductsAdapter.ShippingProductsInterface {
                override fun onItemClicked(position: Int) {
                    val intent = Intent(mContext, OrderDetailActivity::class.java)
                    intent.putExtra("fromWhere", OrderDetailEnum.SHIPPING.type)
                    startActivity(intent)
                }

                override fun showListOptions(position: Int) {
                    showViewSlideUp(binding.constraintListOption)
                }

            })
        binding.productsRecyclerView.adapter = shippingProductsAdapter
    }


    override fun onDestroy() {
        super.onDestroy()
        isSelectEnable = false
    }
}