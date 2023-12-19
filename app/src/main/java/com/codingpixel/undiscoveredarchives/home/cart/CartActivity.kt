package com.codingpixel.undiscoveredarchives.home.cart

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.codingpixel.undiscoveredarchives.R
import com.codingpixel.undiscoveredarchives.base.AppController
import com.codingpixel.undiscoveredarchives.base.BaseActivity
import com.codingpixel.undiscoveredarchives.databinding.ActivityCartBinding
import com.codingpixel.undiscoveredarchives.home.checkout.CheckoutActivity
import com.codingpixel.undiscoveredarchives.utils.AppUtils
import com.codingpixel.undiscoveredarchives.utils.viewGone
import com.codingpixel.undiscoveredarchives.utils.viewVisible
import com.codingpixel.undiscoveredarchives.view_models.HomeViewModel
import com.codingpixel.undiscoveredarchives.view_models.ViewModelFactory

class CartActivity : BaseActivity() {

    private lateinit var binding: ActivityCartBinding
    private lateinit var cartAdapter: CartAdapter

    private var cartList: MutableList<CartModel> = ArrayList()

    private val viewModel: HomeViewModel by viewModels {
        ViewModelFactory((application as AppController).myDatabaseRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        blackStatusBarIcons()

        initViews()
        initVM()
        initListeners()
        initAdapter()

        getCart()
    }

    private fun getCart() {
        val cl = viewModel.getCartList(if (AppController.isGuestUser) -1 else AppUtils.getUserDetails(this).id)
        cartList.clear()
        if (!cl.isNullOrEmpty()) {
            for (i in cl.indices)
                cartList.add(
                    CartModel(
                        cl[i].id, cl[i].user_id, cl[i].variant_id, cl[i].quantity, cl[i].price,
                        cl[i].vendor_id, cl[i].product_id, cl[i].zone_id, cl[i].product_name, cl[i].product_description,
                        cl[i].product_category, cl[i].product_category_id, cl[i].product_image, cl[i].total_quantity
                    )
                )
            binding.constraintCart.viewVisible()
            binding.llNoCart.viewGone()
            calculateTotal()
        } else {
            binding.constraintCart.viewGone()
            binding.llNoCart.viewVisible()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initViews() {
        binding.topBar.tvScreenTitle.text = "Shopping Cart"
        binding.topBar.ivBack.viewVisible()
    }

    private fun initVM() {
        binding.viewModel = viewModel
        binding.executePendingBindings()
        binding.lifecycleOwner = this
    }

    private fun initListeners() {
        binding.topBar.ivBack.setOnClickListener { finish() }

        binding.btnCheckOut.setOnClickListener {
            startActivity(Intent(this@CartActivity, CheckoutActivity::class.java))
        }
    }

    private fun initAdapter() {
        cartAdapter = CartAdapter(cartList, object : CartAdapter.CartInterface {
            override fun updateCart(position: Int, quantity: Int) {
                if (cartList[position].quantity < cartList[position].total_quantity) {
//                    Loader.showLoader(this@CartActivity) {}
                    viewModel.updateCart(
                        cartList[position].product_id,
                        cartList[position].variant_id, cartList[position].quantity,
                        if (AppController.isGuestUser) -1 else AppUtils.getUserDetails(this@CartActivity).id
                    )
                    cartAdapter.notifyItemChanged(position)
                    calculateTotal()
                } else {
                    cartList[position].quantity--
                    AppUtils.showToast(this@CartActivity, "Max quantity is reached", false)
                }
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun deleteSingleCartItem(position: Int) {
//                Loader.showLoader(this@CartActivity) {}
                viewModel.deleteSingleCartItem(
                    if (AppController.isGuestUser) -1 else AppUtils.getUserDetails(this@CartActivity).id,
                    cartList[position].product_id,
                    cartList[position].variant_id,
                )
                getCart()
            }
        })
        binding.cartRecyclerView.adapter = cartAdapter
    }

    @SuppressLint("SetTextI18n")
    private fun calculateTotal() {
        var totalPrice = 0.0
        for (i in cartList.indices)
            totalPrice += cartList[i].price.toDouble() * cartList[i].quantity

        binding.btnCheckOut.text = resources.getString(R.string.checkout, cartList.size.toString())
        binding.tvTotalPrice.text = "US $$totalPrice"
    }

}