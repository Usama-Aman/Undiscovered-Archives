package com.codingpixel.undiscoveredarchives.home.product_detail

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.astritveliu.boom.Boom
import com.bumptech.glide.Glide
import com.codingpixel.undiscoveredarchives.R
import com.codingpixel.undiscoveredarchives.base.AppController
import com.codingpixel.undiscoveredarchives.base.BaseActivity
import com.codingpixel.undiscoveredarchives.databinding.ActivityProductDetailBinding
import com.codingpixel.undiscoveredarchives.home.all_products.AllProductsActivity
import com.codingpixel.undiscoveredarchives.home.all_products.AllProductsData
import com.codingpixel.undiscoveredarchives.home.all_products.AllProductsModel
import com.codingpixel.undiscoveredarchives.home.cart.CartActivity
import com.codingpixel.undiscoveredarchives.home.dialogs.AskQuestion
import com.codingpixel.undiscoveredarchives.home.dialogs.CartConfirmationDialog
import com.codingpixel.undiscoveredarchives.home.favorites.models.*
import com.codingpixel.undiscoveredarchives.home.product_detail.adapters.*
import com.codingpixel.undiscoveredarchives.loader.Loader
import com.codingpixel.undiscoveredarchives.network.Api
import com.codingpixel.undiscoveredarchives.network.ApiStatus
import com.codingpixel.undiscoveredarchives.network.ApiTags
import com.codingpixel.undiscoveredarchives.network.RetrofitClient
import com.codingpixel.undiscoveredarchives.room.UserCartTable
import com.codingpixel.undiscoveredarchives.utils.*
import com.codingpixel.undiscoveredarchives.view_models.HomeViewModel
import com.codingpixel.undiscoveredarchives.view_models.ViewModelFactory
import com.faltenreich.skeletonlayout.Skeleton
import com.google.gson.Gson
import kotlinx.coroutines.*
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call

class ProductDetailActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivityProductDetailBinding
    private lateinit var mContext: Context
    private lateinit var retrofitClient: Api
    private lateinit var apiCall: Call<ResponseBody>

    private var suggestedProductsList: MutableList<AllProductsData?> = ArrayList()
    private var sizesList: MutableList<DropdownModel> = ArrayList()
    private var shippingList: MutableList<DropdownModel> = ArrayList()
    private var productPhotos: MutableList<File> = ArrayList()
    private var productReviewsList: MutableList<Review> = ArrayList()
    private lateinit var productDetailData: UserProductData
    private lateinit var productDetailPhotosAdapter: ProductDetailPhotosAdapter
    private lateinit var productRatingAdapter: ProductRatingAdapter
    private lateinit var suggestedProductsAdapter: SuggestedProductsAdapter
    private lateinit var sizesDropDownAdapter: CustomDropDownAdapter
    private lateinit var shippingDropDownAdapter: CustomDropDownAdapter


    private var productId = -1
    private var quantityCount = 1
    private var selectedSizePosition = 0
    private var selectedShippingPosition = 0

    private lateinit var skeletonLayout: Skeleton


    private val viewModel: HomeViewModel by viewModels {
        ViewModelFactory((application as AppController).myDatabaseRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        retrofitClient = RetrofitClient.getClient(this).create(Api::class.java)
        mContext = this

        blackStatusBarIcons()
        changeStatusBarColor(R.color.dividers)

        initVM()
        initViews()
        initListeners()
        initPager()
        initAdapter()
        observeApiResponse()
    }

    private fun initViews() {
        productId = intent.getIntExtra("productId", -1)
        skeletonLayout = binding.skeletonLayout

        if (productId != -1)
            getProductDetail()
        else {
            finish()
        }
    }

    private fun initVM() {
        binding.viewModel = viewModel
        binding.executePendingBindings()
        binding.lifecycleOwner = this
    }

    private fun initListeners() {
        binding.ivQuantityMinus.setOnClickListener(this)
        binding.ivQuantityPlus.setOnClickListener(this)
        binding.btnMessage.setOnClickListener(this)
        binding.btnAddToCart.setOnClickListener(this)
        binding.ivBack.setOnClickListener(this)
        binding.tvViewMoreSuggestedProducts.setOnClickListener(this)
        binding.tvDescriptionType.setOnClickListener(this)
        binding.tvMeasurementType.setOnClickListener(this)
        binding.ivFavorite.setOnClickListener(this)

        Boom(binding.btnMessage)
        Boom(binding.btnAddToCart)
        Boom(binding.ivFavorite)
    }

    private fun initPager() {
        productDetailPhotosAdapter = ProductDetailPhotosAdapter(productPhotos)
        binding.productPhotosViewPager.adapter = productDetailPhotosAdapter
        binding.productPhotosViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.instaDotIndicator.onPageChange(position)
            }
        })
    }

    private fun initAdapter() {
        productRatingAdapter = ProductRatingAdapter(productReviewsList)
        binding.ratingRecyclerView.adapter = productRatingAdapter

        suggestedProductsAdapter = SuggestedProductsAdapter(suggestedProductsList,
            object : SuggestedProductsAdapter.SuggestedProductsInterface {
                override fun onItemClicked(position: Int) {
                    val intent = Intent(mContext, ProductDetailActivity::class.java)
                    intent.putExtra("productId", suggestedProductsList[position]?.id)
                    startActivity(intent)
                }
            })
        binding.suggestedProductRecyclerView.adapter = suggestedProductsAdapter
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivQuantityMinus -> {
                if (quantityCount == 1)
                    return
                else
                    quantityCount--
                binding.tvSelectedQuantity.text = "$quantityCount"
            }
            R.id.ivQuantityPlus -> {
                if (this::productDetailData.isInitialized) {
                    if (selectedSizePosition != 0) {
                        if (quantityCount == productDetailData.variants[selectedSizePosition - 1].quantity) {
                            AppUtils.showToast(this@ProductDetailActivity, "Max quantity is reached", false)
                            return
                        } else
                            quantityCount++
                        binding.tvSelectedQuantity.text = "$quantityCount"
                    } else {
                        AppUtils.showToast(this@ProductDetailActivity, "Please select size", false)
                    }
                }
            }
            R.id.btnMessage -> {
                val sendMessageDialog = AskQuestion()
                sendMessageDialog.show(supportFragmentManager, "AskQuestionDialog")
            }
            R.id.ivBack -> {
                finish()
            }
            R.id.tvViewMoreSuggestedProducts -> {
                val intent = Intent(mContext, AllProductsActivity::class.java)
                intent.putExtra("isAllFilter", false)
                intent.putExtra("categoryId", productDetailData.category?.id)
                intent.putExtra("categoryName", productDetailData.category?.title)
                startActivity(intent)
            }
            R.id.tvDescriptionType -> {
                binding.descriptionExpandableLayout.toggle()
                if (binding.descriptionExpandableLayout.isExpanded)
                    binding.ivArrow1.scaleY = -1f
                else
                    binding.ivArrow1.scaleY = +1f
            }
            R.id.tvMeasurementType -> {
                binding.measurementExpandableLayout.toggle()
                if (binding.measurementExpandableLayout.isExpanded)
                    binding.ivArrow2.scaleY = -1f
                else
                    binding.ivArrow2.scaleY = +1f
            }
            R.id.ivFavorite -> {
                if (this@ProductDetailActivity::productDetailData.isInitialized) {
                    Loader.showLoader(this) {
                        if (::apiCall.isInitialized)
                            apiCall.cancel()
                    }
                    addProductsToFavorites(productDetailData.id)
                }
            }
            R.id.btnAddToCart -> {
                addProductToCart()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun observeApiResponse() {
        viewModel.getApiResponse().observe(this, {
            when (it.status) {
                ApiStatus.LOADING -> {

                }
                ApiStatus.ERROR -> {
                    Loader.hideLoader()
                    AppUtils.showToast(this, it.message!!, false)
                }
                ApiStatus.SUCCESS -> {
                    Loader.hideLoader()
                    when (it.tag) {
                        ApiTags.GET_PRODUCT_DETAIL -> {
                            val model = Gson().fromJson(it.data.toString(), UserProductDetailModel::class.java)
                            productDetailData = model.data
                            setUpProductData()
                        }
                        ApiTags.GET_ALL_PRODUCTS -> {
                            if (!isPanelShown(binding.coordinatorLayout)) {
                                binding.coordinatorLayout.viewVisible()
                                binding.skeletonLayout.viewGone()
                                skeletonLayout.showOriginal()
                            }


                            val model = Gson().fromJson(it.data.toString(), AllProductsModel::class.java)
                            handleProductsResponse(model.data)
                        }
                        ApiTags.ADD_PRODUCT_TO_FAVORITES -> {
                            val jsonObject = JSONObject(it.data.toString())
                            val data = jsonObject.getJSONObject("data")

                            AppUtils.showToast(this@ProductDetailActivity, jsonObject.getString("message"), true)

                            if (data.has("is_favourite"))
                                if (data.getInt("is_favourite") == 0)
                                    binding.ivFavorite.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            mContext,
                                            R.drawable.ic_heart_unfilled
                                        )
                                    )
                                else
                                    binding.ivFavorite.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            mContext,
                                            R.drawable.ic_heart_filled
                                        )
                                    )
                        }
                    }
                }
            }
        })
    }


    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    private fun setUpProductData() {
        productPhotos.addAll(productDetailData.files)
        productDetailPhotosAdapter.notifyDataSetChanged()
        binding.instaDotIndicator.noOfPages = productPhotos.size

        Glide.with(mContext)
            .load(productDetailData.display_image)
            .into(binding.ivStoreImage)

        if (productDetailData.is_favourite == 1)
            binding.ivFavorite.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_heart_filled))
        else
            binding.ivFavorite.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_heart_unfilled))

        binding.tvStoreName.text = productDetailData.vendor?.name

        if (productDetailData.vendor?.products_count ?: -1 > 1)
            binding.tvStoreProducts.text = "${productDetailData.vendor?.products_count} Products"
        else
            binding.tvStoreProducts.text = "${productDetailData.vendor?.products_count} Product"
        if (productDetailData.reviews_avg_rating != null)
            binding.ratingBar.rating = productDetailData.reviews_avg_rating.toFloat()
        binding.tvProductName.text = productDetailData.product_name
        binding.tvCondition.text = productDetailData.condition

        if (!productDetailData.variants.isNullOrEmpty()) {
            if (productDetailData.variants[0].compare_at_price.toDouble() == 0.0) {
                binding.tvActualPrice.viewGone()
            } else {
                binding.tvActualPrice.viewVisible()
                binding.tvActualPrice.text = "$${productDetailData.variants[0].compare_at_price}"
            }
            binding.tvDiscountedPrice.text = "$${productDetailData.variants[0].price}"
            if (productDetailData.variants[0].quantity > 0) {
                binding.btnAddToCart.isEnabled = true
                binding.llProductCounter.viewVisible()
                binding.tvOutOfStock.viewGone()
            } else {
                binding.btnAddToCart.isEnabled = false
                binding.llProductCounter.viewGone()
                binding.tvOutOfStock.viewVisible()
            }
        }

        sizesList.clear()
        sizesList.add(DropdownModel("Select Size", -1))
        productDetailData.variants.forEach {
            sizesList.add(DropdownModel(it.size.value, it.size.id))
        }

        shippingList.clear()
        shippingList.add(DropdownModel("Ask for Shipping", -1))
        productDetailData.zones.forEach {
            shippingList.add(DropdownModel(it.name, it.id))
        }

        sizesDropDownAdapter = CustomDropDownAdapter(mContext, sizesList)
        binding.sizeSpinner.adapter = sizesDropDownAdapter
        binding.sizeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedSizePosition = position

                sizesList.filter { it.isSelected }.forEach { it.isSelected = false }
                sizesList[position].isSelected = true

                if (!productDetailData.variants.isNullOrEmpty())
                    if (selectedShippingPosition > 1) {
                        binding.tvActualPrice.text = "$${productDetailData.variants[selectedSizePosition - 1].compare_at_price}"
                        binding.tvDiscountedPrice.text = "$${productDetailData.variants[selectedSizePosition - 1].price}"
                    } else {
                        binding.tvActualPrice.text = "$${productDetailData.variants[0].compare_at_price}"
                        binding.tvDiscountedPrice.text = "$${productDetailData.variants[0].price}"
                    }

//                binding.tvSizeSpinnerText.text = productDetailData.variants[selectedSizePosition].size.value
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        shippingDropDownAdapter = CustomDropDownAdapter(mContext, shippingList)
        binding.shippingSpinner.adapter = shippingDropDownAdapter
        binding.shippingSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedShippingPosition = position

                shippingList.filter { it.isSelected }.forEach { it.isSelected = false }
                shippingList[position].isSelected = true

                if (!productDetailData.zones.isNullOrEmpty())
                    if (selectedShippingPosition == 0)
                        binding.tvShipping.text = "Select Shipping"
                    else {
                        if (productDetailData.zones[selectedShippingPosition - 1].pivot.shipping_charges.toDouble() == 0.0)
                            binding.tvShipping.text =
                                "Free Shipping To ${productDetailData.zones[selectedShippingPosition - 1].name}"
                        else
                            binding.tvShipping.text =
                                "$${productDetailData.zones[selectedShippingPosition - 1].pivot.shipping_charges} To ${productDetailData.zones[selectedShippingPosition - 1].name}"
                    }

//                binding.tvShippingSpinnerText.text = productDetailData.zones[selectedShippingPosition].name
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        if (productDetailData.product_description.isEmpty()) {
            binding.tvDescriptionType.viewGone()
            binding.descriptionExpandableLayout.viewGone()
        } else {
            binding.tvDescriptionType.viewVisible()
            binding.descriptionExpandableLayout.viewVisible()
            binding.tvDescription.text = productDetailData.product_description
        }

        if (productDetailData.product_description.isEmpty()) {
            binding.tvMeasurementType.viewGone()
            binding.measurementExpandableLayout.viewGone()
        } else {
            binding.tvMeasurementType.viewVisible()
            binding.measurementExpandableLayout.viewVisible()
            binding.tvMeasurement.text = productDetailData.measurements
        }

        productReviewsList.addAll(productDetailData.reviews)
        if (productReviewsList.isNullOrEmpty()) {
            binding.llNoReviews.viewVisible()
            binding.ratingRecyclerView.viewGone()
        } else {
            binding.llNoReviews.viewGone()
            binding.ratingRecyclerView.viewVisible()
            productRatingAdapter.notifyDataSetChanged()
        }

        getAllProducts()

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun handleProductsResponse(data: List<AllProductsData>) {
//        if (suggestedProductsList.size > 0)
//            suggestedProductsList.removeAt(suggestedProductsList.size - 1)

        suggestedProductsList.addAll(data)

//        if (data.size >= AppController.PAGINATION_COUNT) {
//            skip += AppController.PAGINATION_COUNT
//            suggestedProductsList.add(null)
//            canLoadMore = true
//        }

        if (suggestedProductsList.isEmpty()) {
            binding.suggestedProductRecyclerView.viewGone()
            binding.llSuggestedProduct.viewGone()
        } else {
            binding.suggestedProductRecyclerView.viewVisible()
            binding.llSuggestedProduct.viewVisible()
        }

        suggestedProductsAdapter.notifyDataSetChanged()
    }

    private fun getProductDetail() {
        binding.coordinatorLayout.viewGone()
        binding.skeletonLayout.viewVisible()
        skeletonLayout.showSkeleton()

        apiCall = retrofitClient.getUserProductDetail(productId)
        viewModel.getProductDetail(apiCall)
    }

    private fun getAllProducts() {
        apiCall = retrofitClient.getAllProducts(
            "", "", productDetailData.category?.id ?: -1, 0, 0, 0, skip = 0, take = 15
        )
        viewModel.getAllProducts(apiCall)
    }

    private fun addProductsToFavorites(id: Int) {
        apiCall = retrofitClient.addProductToFavorite(id)
        viewModel.addProductToFavorite(apiCall)
    }

    private fun addProductToCart() {
        val cartList = (viewModel.getCartList(if (AppController.isGuestUser) -1 else AppUtils.getUserDetails(this).id))

        if (selectedSizePosition == 0) {
            AppUtils.showToast(this@ProductDetailActivity, "Please select size!", false)
            return
        }
        if (selectedShippingPosition == 0) {
            AppUtils.showToast(this@ProductDetailActivity, "Please select shipping!", false)
            return
        }

        if (cartList.isNullOrEmpty()) {
            addToCart()
        } else {
            var isSameVendor = true
            cartList.forEach {
                if (it.vendor_id != productDetailData.vendor?.id) {
                    isSameVendor = false
                    return@forEach
                }
            }

            if (isSameVendor) {
                var isSameZone = true
                cartList.forEach {
                    if (it.zone_id != productDetailData.zones[selectedShippingPosition - 1].id) {
                        isSameZone = false
                        return@forEach
                    }
                }

                if (isSameZone) {
                    var isProductExist = false
                    cartList.forEach {
                        if (it.product_id == productDetailData.id) {
                            isProductExist = true
                            return@forEach
                        }
                    }

                    if (isProductExist) {
                        var isSameVariant = false
                        var sameVariantPositionInDB = -1
                        cartList.forEachIndexed { index, userCartTable ->
                            if (userCartTable.variant_id == productDetailData.variants[selectedSizePosition - 1].id) {
                                isSameVariant = true
                                sameVariantPositionInDB = index
                                return@forEachIndexed
                            }
                        }

                        if (isSameVariant) {
                            if (cartList[sameVariantPositionInDB].quantity + quantityCount > productDetailData.variants[selectedSizePosition - 1].quantity) {
                                AppUtils.showToast(this@ProductDetailActivity, "Max Quantity Reached!", false)
                                return
                            }
                            viewModel.updateCart(
                                productDetailData.id, cartList[sameVariantPositionInDB].variant_id,
                                cartList[sameVariantPositionInDB].quantity + quantityCount,
                                if (AppController.isGuestUser) -1 else AppUtils.getUserDetails(this).id
                            )
                            AppUtils.showToast(this, "Cart Updated", true)
                        } else {
                            addToCart()
                        }
                    } else {
                        addToCart()
                    }
                } else {
                    showCartConfirmationDialog("You can not add product from another zone to checkout. Or would you like to add this product and remove the existing ones in cart?")
                }
            } else {
                showCartConfirmationDialog("You can not add product from another vendor to checkout. Or would you like to add this product and remove the existing ones in cart?")
            }

        }
    }

    private fun showCartConfirmationDialog(titleMessage: String) {
        CartConfirmationDialog(titleMessage, object : CartConfirmationDialog.CartConfirmationInterface {
            override fun emptyCart() {
                viewModel.deleteCart(if (AppController.isGuestUser) -1 else AppUtils.getUserDetails(this@ProductDetailActivity).id)
                addProductToCart()
            }

            override fun manageCart() {
                startActivity(Intent(this@ProductDetailActivity, CartActivity::class.java))
            }
        }).show(supportFragmentManager, "CartConfirmationDialog")
    }

    private fun addToCart() {
        val cartTable = UserCartTable(
            0,
            if (AppController.isGuestUser) -1 else AppUtils.getUserDetails(this).id,
            productDetailData.variants[selectedSizePosition - 1].id,
            quantityCount,
            productDetailData.variants[selectedSizePosition - 1].price,
            productDetailData.vendor?.id ?: -1,
            productDetailData.id,
            productDetailData.zones[selectedShippingPosition - 1].id,
            productDetailData.product_name,
            productDetailData.product_description,
            productDetailData.category?.title ?: "",
            productDetailData.category?.id ?: -1,
            productDetailData.display_image,
            productDetailData.variants[selectedSizePosition - 1].quantity
        )
        viewModel.saveCartData(cartTable)
        AppUtils.showToast(this, "Product Added to cart", true)
    }


}