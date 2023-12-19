package com.codingpixel.undiscoveredarchives.home.payment_products

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
import com.codingpixel.undiscoveredarchives.databinding.ActivityPaymentProductsBinding
import com.codingpixel.undiscoveredarchives.home.order_detail.OrderDetailActivity
import com.codingpixel.undiscoveredarchives.utils.CustomDropDownAdapter
import com.codingpixel.undiscoveredarchives.utils.DropdownModel
import com.codingpixel.undiscoveredarchives.utils.OrderDetailEnum
import com.codingpixel.undiscoveredarchives.utils.viewVisible

class PaymentProductsActivity : BaseActivity() {

    private lateinit var binding: ActivityPaymentProductsBinding
    private lateinit var mContext: Context
    private lateinit var paymentProductsAdapter: PaymentProductsAdapter

    private lateinit var dropDownAdapter: ArrayAdapter<DropdownModel>

    companion object {
        var isSelectEnable = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentProductsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mContext = this

        blackStatusBarIcons()

        initViews()
        initFilterSpinner()
        initListeners()
        initAdapter()
    }

    @SuppressLint("SetTextI18n")
    private fun initViews() {
        binding.filter.viewVisible()
        binding.tvHeader.viewVisible()
        binding.tvHeader.text = "Payment Details"

        binding.topBar.tvScreenTitle.text = "Payment"
        binding.topBar.ivBack.viewVisible()

    }

    private fun initFilterSpinner() {
        /*Filter*/
        dropDownAdapter = CustomDropDownAdapter(mContext, AppController.filterDropDownList)
        binding.filter.adapter = dropDownAdapter
        binding.filter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val orderBy = AppController.filterDropDownList[position].value
                AppController.filterDropDownList.filter { it.isSelected }.forEach { it.isSelected = false }
                AppController.filterDropDownList[position].isSelected = true

                binding.tvFilter.text = orderBy
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initListeners() {


        binding.topBar.ivBack.setOnClickListener {
            finish()
        }


        binding.tvSelect.setOnClickListener {
            isSelectEnable = !isSelectEnable

            if (isSelectEnable)
                binding.tvSelect.text = "Cancel"
            else
                binding.tvSelect.text = "Select"

            /*TODO uncheck the selected item for next time selection*/

            paymentProductsAdapter.notifyDataSetChanged()
        }
    }

    private fun initAdapter() {
        paymentProductsAdapter = PaymentProductsAdapter(object : PaymentProductsAdapter.PaymentProductsInterface {
            override fun onItemClicked(position: Int) {
                val intent = Intent(mContext, OrderDetailActivity::class.java)
                intent.putExtra("fromWhere", OrderDetailEnum.PAYMENT.type)
                startActivity(intent)
            }

            override fun showListOptions(position: Int) {
                showViewSlideUp(binding.constraintListOption)
            }
        })
        binding.recyclerFavorites.adapter = paymentProductsAdapter
    }

}