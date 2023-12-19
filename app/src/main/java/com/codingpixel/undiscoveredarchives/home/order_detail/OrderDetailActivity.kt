package com.codingpixel.undiscoveredarchives.home.order_detail

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager2.widget.ViewPager2
import com.astritveliu.boom.Boom
import com.codingpixel.undiscoveredarchives.R
import com.codingpixel.undiscoveredarchives.auth.models.LoginModel
import com.codingpixel.undiscoveredarchives.base.AppController
import com.codingpixel.undiscoveredarchives.base.AppController.Companion.productDataForOrderDetail
import com.codingpixel.undiscoveredarchives.base.BaseActivity
import com.codingpixel.undiscoveredarchives.databinding.ActivityOrderDetailBinding
import com.codingpixel.undiscoveredarchives.home.add_new_listing.AddListingActivity
import com.codingpixel.undiscoveredarchives.home.dialogs.*
import com.codingpixel.undiscoveredarchives.home.favorites.models.*
import com.codingpixel.undiscoveredarchives.home.product_detail.adapters.ProductDetailPhotosAdapter
import com.codingpixel.undiscoveredarchives.loader.Loader
import com.codingpixel.undiscoveredarchives.network.Api
import com.codingpixel.undiscoveredarchives.network.ApiStatus
import com.codingpixel.undiscoveredarchives.network.ApiTags
import com.codingpixel.undiscoveredarchives.network.RetrofitClient
import com.codingpixel.undiscoveredarchives.utils.AppUtils
import com.codingpixel.undiscoveredarchives.utils.BroadCastEnums
import com.codingpixel.undiscoveredarchives.utils.OrderDetailEnum.*
import com.codingpixel.undiscoveredarchives.utils.viewGone
import com.codingpixel.undiscoveredarchives.utils.viewVisible
import com.codingpixel.undiscoveredarchives.view_models.HomeViewModel
import com.codingpixel.undiscoveredarchives.view_models.ViewModelFactory
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Call

class OrderDetailActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivityOrderDetailBinding
    private lateinit var viewModel: HomeViewModel
    private lateinit var retrofitClient: Api
    private lateinit var apiCall: Call<ResponseBody>

    private var productPhotos: MutableList<File> = ArrayList()

    private var fromWhere = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        retrofitClient = RetrofitClient.getClient(this@OrderDetailActivity).create(Api::class.java)

//        window.setFlags(
//            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
//            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
//        )

        blackStatusBarIcons()
        changeStatusBarColor(R.color.dividers)

        initViews()
        initVM()
        initListeners()
        initPager()
        observeApiResponse()
    }

    private fun initViews() {
        fromWhere = intent?.getStringExtra("fromWhere") ?: ""
        setUpBottomButtons()
    }

    private fun initVM() {
        viewModel = ViewModelProviders.of(this, ViewModelFactory()).get(HomeViewModel::class.java)
        binding.viewModel = viewModel
        binding.executePendingBindings()
        binding.lifecycleOwner = this
    }


    @SuppressLint("SetTextI18n")
    private fun setUpBottomButtons() {
        when (fromWhere) {
            SALES_HISTORY.type -> {
                binding.btnDownloadSalesHistory.viewVisible()
                binding.llCurrentOrdersOptions.viewGone()
                binding.llPurchaseOptions.viewGone()
                binding.llPublishedProductOptions.viewGone()
                binding.llShippingOptions.viewGone()
                binding.llTrashOptions.viewGone()
                binding.constraintDraftsOption.viewGone()
            }
            CURRENT_SALES.type -> {
                binding.llCurrentOrdersOptions.viewVisible()
                binding.llPurchaseOptions.viewGone()
                binding.llPublishedProductOptions.viewGone()
                binding.llShippingOptions.viewGone()
                binding.btnDownloadSalesHistory.viewGone()
                binding.llTrashOptions.viewGone()
                binding.constraintDraftsOption.viewGone()
            }
            PURCHASE_HISTORY.type -> {
                binding.llPurchaseOptions.viewVisible()
                binding.llCurrentOrdersOptions.viewGone()
                binding.llPublishedProductOptions.viewGone()
                binding.llShippingOptions.viewGone()
                binding.btnDownloadSalesHistory.viewGone()
                binding.llTrashOptions.viewGone()
                binding.constraintDraftsOption.viewGone()
            }
            CURRENT_ORDERS.type -> {
                binding.llCurrentOrdersOptions.viewVisible()
                binding.llPurchaseOptions.viewGone()
                binding.llPublishedProductOptions.viewGone()
                binding.llShippingOptions.viewGone()
                binding.btnDownloadSalesHistory.viewGone()
                binding.llTrashOptions.viewGone()
                binding.constraintDraftsOption.viewGone()
            }
            PAYMENT.type -> {
                binding.btnDownloadSalesHistory.viewVisible()
                binding.llCurrentOrdersOptions.viewGone()
                binding.llPurchaseOptions.viewGone()
                binding.llPublishedProductOptions.viewGone()
                binding.llShippingOptions.viewGone()
                binding.llTrashOptions.viewGone()
                binding.constraintDraftsOption.viewGone()
            }
            TRASH_PRODUCTS.type -> {
                binding.llTrashOptions.viewVisible()
                binding.btnDownloadSalesHistory.viewGone()
                binding.llCurrentOrdersOptions.viewGone()
                binding.constraintDesignerDetails.viewGone()
                binding.llPurchaseOptions.viewGone()
                binding.llPublishedProductOptions.viewGone()
                binding.llShippingOptions.viewGone()
                binding.constraintDraftsOption.viewGone()
            }
            SHIPPING.type -> {
                binding.llShippingOptions.viewVisible()
                binding.llTrashOptions.viewGone()
                binding.btnDownloadSalesHistory.viewGone()
                binding.llCurrentOrdersOptions.viewGone()
                binding.llPurchaseOptions.viewGone()
                binding.llPublishedProductOptions.viewGone()
                binding.constraintDraftsOption.viewGone()
            }
            PUBLISHED_PRODUCTS.type -> {
                binding.llPublishedProductOptions.viewVisible()
                binding.llShippingOptions.viewGone()
                binding.llTrashOptions.viewGone()
                binding.constraintDesignerDetails.viewGone()
                binding.btnDownloadSalesHistory.viewGone()
                binding.llCurrentOrdersOptions.viewGone()
                binding.llPurchaseOptions.viewGone()
                binding.constraintDraftsOption.viewGone()
            }
            DRAFTS.type -> {
                binding.constraintDraftsOption.viewVisible()
                binding.constraintDesignerDetails.viewGone()
                binding.llPublishedProductOptions.viewGone()
                binding.llShippingOptions.viewGone()
                binding.llTrashOptions.viewGone()
                binding.btnDownloadSalesHistory.viewGone()
                binding.llCurrentOrdersOptions.viewGone()
                binding.llPurchaseOptions.viewGone()
            }
        }

        when (fromWhere) {
            PURCHASE_HISTORY.type, CURRENT_ORDERS.type -> {
                binding.tv1.text = "Order ID"
                binding.tv2.text = "Order Date"
                binding.tv3.text = "Payment Status"
                binding.tv4.text = "Seller"
            }
            SALES_HISTORY.type, CURRENT_SALES.type -> {
                binding.tv1.text = "Order ID"
                binding.tv2.text = "Sales Date"
                binding.tv3.text = "Unit Price"
                binding.tv4.text = "Quantity"
                binding.tv5.text = "Total Price"

                binding.tv5.viewVisible()
                binding.tv5Ans.viewVisible()
                binding.v5.viewVisible()
            }
            PAYMENT.type -> {
                binding.tv1.text = "Order ID"
                binding.tv2.text = "Commission %"
                binding.tv3.text = "Payment Method"
                binding.tv4.text = "Status"
            }
            PUBLISHED_PRODUCTS.type -> {
                binding.tv1.text = "Type"
                binding.tv2.text = "Publish Date"
                binding.tv3.text = "Seller"

                binding.tv4.viewGone()
                binding.tv5.viewGone()
                binding.tv4Ans.viewGone()
                binding.tv5Ans.viewGone()
                binding.v4.viewGone()
                binding.v5.viewGone()

                binding.tv1Ans.text = productDataForOrderDetail?.category?.title
                binding.tv2Ans.text = productDataForOrderDetail?.published_at
                binding.tv3Ans.text = productDataForOrderDetail?.vendor?.name
                binding.tvProductName.text = productDataForOrderDetail?.product_name
                binding.tvProductDescription.text = productDataForOrderDetail?.product_description
                binding.tvProductPrice.text = "$${productDataForOrderDetail?.variants_min_price}"

                if (productDataForOrderDetail != null)
                    if (!productDataForOrderDetail?.files.isNullOrEmpty()) {
                        productPhotos.addAll(productDataForOrderDetail?.files!!)
                        binding.instaDotIndicator.noOfPages = productDataForOrderDetail?.files?.size ?: -1
                    }

            }
            TRASH_PRODUCTS.type -> {
                binding.tv1.text = "Type"
                binding.tv2.text = "Publish Date"
                binding.tv3.text = "Seller"

                binding.tv4.viewGone()
                binding.tv5.viewGone()
                binding.tv4Ans.viewGone()
                binding.tv5Ans.viewGone()
                binding.v4.viewGone()
                binding.v5.viewGone()

                binding.tv1Ans.text = productDataForOrderDetail?.category?.title
                binding.tv2Ans.text = productDataForOrderDetail?.published_at
                binding.tv3Ans.text = productDataForOrderDetail?.vendor?.name
                binding.tvProductName.text = productDataForOrderDetail?.product_name
                binding.tvProductDescription.text = productDataForOrderDetail?.product_description
                binding.tvProductPrice.text = "$${productDataForOrderDetail?.variants_min_price}"

                if (productDataForOrderDetail != null)
                    if (!productDataForOrderDetail?.files.isNullOrEmpty()) {
                        productPhotos.addAll(productDataForOrderDetail?.files!!)
                        binding.instaDotIndicator.noOfPages = productDataForOrderDetail?.files?.size ?: -1
                    }

            }
            DRAFTS.type -> {
                binding.tv1.text = "Type"
                binding.tv2.text = "Publish Date"
                binding.tv3.text = "Seller"

                binding.tv4.viewGone()
                binding.tv5.viewGone()
                binding.tv4Ans.viewGone()
                binding.tv5Ans.viewGone()
                binding.v4.viewGone()
                binding.v5.viewGone()

                binding.tv1Ans.text = productDataForOrderDetail?.category?.title
                binding.tv2Ans.text = productDataForOrderDetail?.published_at
                binding.tv3Ans.text = productDataForOrderDetail?.vendor?.name
                binding.tvProductName.text = productDataForOrderDetail?.product_name
                binding.tvProductDescription.text = productDataForOrderDetail?.product_description
                binding.tvProductPrice.text = "$${productDataForOrderDetail?.variants_min_price}"

                if (productDataForOrderDetail != null)
                    if (!productDataForOrderDetail?.files.isNullOrEmpty()) {
                        productPhotos.addAll(productDataForOrderDetail?.files!!)
                        binding.instaDotIndicator.noOfPages = productDataForOrderDetail?.files?.size ?: -1
                    }

            }
            SHIPPING.type -> {
                binding.tv1.text = "Order ID"
                binding.tv2.text = "Unit Price"
                binding.tv3.text = "Quantity"
                binding.tv4.text = "Total Price"
                binding.tv5.text = "Status"

                binding.tv5.viewVisible()
                binding.tv5Ans.viewVisible()
                binding.v5.viewVisible()
            }
            else -> {
                binding.tv1.text = ""
                binding.tv2.text = ""
                binding.tv3.text = ""
                binding.tv4.text = ""
                binding.tv5.text = ""

                binding.tv5.viewVisible()
                binding.tv5Ans.viewVisible()
                binding.v5.viewVisible()
            }
        }
    }

    private fun initListeners() {
        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.btnRateProductPurchase.setOnClickListener(this)
        binding.btnMessageShipping.setOnClickListener(this)
        binding.btnShipNowShipping.setOnClickListener(this)
        binding.btnEditPublished.setOnClickListener(this)
        binding.btnDeletePublished.setOnClickListener(this)
        binding.btnRestoreTrash.setOnClickListener(this)
        binding.btnDeleteTrash.setOnClickListener(this)
        binding.btnPublishDrafts.setOnClickListener(this)
        binding.btnEditDrafts.setOnClickListener(this)
        binding.btnDeleteDrafts.setOnClickListener(this)

        Boom(binding.btnRateProductPurchase)
        Boom(binding.btnMessageShipping)
        Boom(binding.btnShipNowShipping)
        Boom(binding.btnEditPublished)
        Boom(binding.btnDeletePublished)
        Boom(binding.btnRestoreTrash)
        Boom(binding.btnDeleteTrash)
        Boom(binding.btnPublishDrafts)
        Boom(binding.btnEditDrafts)
        Boom(binding.btnDeleteDrafts)

    }

    private fun initPager() {
        binding.productPhotosViewPager.adapter = ProductDetailPhotosAdapter(productPhotos)
        binding.productPhotosViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.instaDotIndicator.onPageChange(position)
            }
        })

    }

    private fun observeApiResponse() {
        viewModel.getApiResponse().observe(this, {
            when (it.status) {
                ApiStatus.LOADING -> {
                    Loader.showLoader((this as AppCompatActivity)) {
                        if (this::apiCall.isInitialized)
                            apiCall.cancel()
                    }
                }
                ApiStatus.ERROR -> {
                    Loader.hideLoader()
                    AppUtils.showToast(this, it.message!!, false)
                }
                ApiStatus.SUCCESS -> {
                    Loader.hideLoader()
                    when (it.tag) {
                        ApiTags.UPDATE_PRODUCT_STATUS -> {
                            val model = Gson().fromJson(it.data.toString(), LoginModel::class.java)
                            AppUtils.showToast(this, model.message, true)

                            Handler(Looper.getMainLooper()).postDelayed({
                                val publishedIntent = Intent(BroadCastEnums.REFRESH_PUBLISHED_PRODUCTS.type)
                                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(publishedIntent)

                                val draftsIntent = Intent(BroadCastEnums.REFRESH_DRAFT_PRODUCTS.type)
                                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(draftsIntent)

                                finish()
                            }, 800)
                        }
                        ApiTags.RESTORE_TRASHED_PRODUCT -> {
                            val model = Gson().fromJson(it.data.toString(), LoginModel::class.java)
                            AppUtils.showToast(this, model.message, true)

                            Handler(Looper.getMainLooper()).postDelayed({
                                val intent = Intent(BroadCastEnums.REFRESH_TRASH_PRODUCTS.type)
                                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)

                                finish()
                            }, 800)
                        }
                        ApiTags.DELETE_PRODUCT -> {
                            val model = Gson().fromJson(it.data.toString(), LoginModel::class.java)
                            AppUtils.showToast(this, model.message, true)

                            Handler(Looper.getMainLooper()).postDelayed({
                                val intent = Intent(BroadCastEnums.REFRESH_TRASH_PRODUCTS.type)
                                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)

                                finish()
                            }, 800)
                        }
                    }
                }
            }
        })
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnRateProductPurchase -> {
                val rateDialog = RateProduct()
                rateDialog.show(supportFragmentManager, "RateProductDialog")
            }
            R.id.btnCancelCurrentOrders -> {
                val cancelOrderDialog = CancelOrder()
                cancelOrderDialog.show(supportFragmentManager, "RateProductDialog")
            }
            R.id.btnMessageShipping -> {
                val askQuestionDialog = AskQuestion()
                askQuestionDialog.show(supportFragmentManager, "AskQuestionDialog")
            }
            R.id.btnShipNowShipping -> {
                val shipNowDialog = ShipmentPlaced()
                shipNowDialog.show(supportFragmentManager, "ShipmentPlacedDialog")
            }
            R.id.btnEditPublished -> {
                editProduct()
            }
            R.id.btnDeletePublished -> {
                val deleteProductDialog =
                    DeleteProduct(-1, object : DeleteDialogInterface {
                        override fun onDeleteClicked(productPosition: Int) {
                            updateProductStatus(productDataForOrderDetail?.id ?: -1, "Trashed")
                        }
                    })
                deleteProductDialog.show(supportFragmentManager, "DeleteProductDialog")
            }
            R.id.btnDeleteTrash -> {
                val deleteDialog =
                    PermanentlyDeleteProduct(-1, object : DeleteDialogInterface {
                        override fun onDeleteClicked(productPosition: Int) {
                            deleteProduct(productDataForOrderDetail?.id ?: -1)
                        }
                    })
                deleteDialog.show(supportFragmentManager, "PermanentlyDeleteDialog")
            }
            R.id.btnRestoreTrash -> {
                restoreTrashedProduct(productDataForOrderDetail?.id ?: -1)
            }
            R.id.btnPublishDrafts -> {
                updateProductStatus(productDataForOrderDetail?.id ?: -1, "Published")
            }
            R.id.btnEditDrafts -> {
                editProduct()
            }
            R.id.btnDeleteDrafts -> {
                val deleteProductDialog =
                    DeleteProduct(-1, object : DeleteDialogInterface {
                        override fun onDeleteClicked(productPosition: Int) {
                            updateProductStatus(productDataForOrderDetail?.id ?: -1, "Trashed")
                        }
                    })
                deleteProductDialog.show(supportFragmentManager, "DeleteProductDialog")
            }
        }
    }

    private fun editProduct() {
        AppController.addEditUserProductData = UserProductData()
        AppController.addEditUserProductData?.id = productDataForOrderDetail?.id ?: -1
        AppController.addEditUserProductData?.category?.id = productDataForOrderDetail?.category?.id ?: -1
        AppController.addEditUserProductData?.sub_category?.id = productDataForOrderDetail?.sub_category?.id ?: -1
        AppController.addEditUserProductData?.designer?.id = productDataForOrderDetail?.designer?.id ?: -1
        AppController.addEditUserProductData?.product_name = productDataForOrderDetail?.product_name ?: ""

        productDataForOrderDetail?.variants?.forEach {
            AppController.addEditUserProductData?.variants?.add(
                Variant(
                    it.compare_at_price, it.id, it.price, it.quantity, it.size_id,
                    product_id = it.product_id, sizeValue = "", seller_earnings = "", commissioned_price = ""
                )
            )
        }

        AppController.addEditUserProductData?.condition = productDataForOrderDetail?.condition ?: ""
        AppController.addEditUserProductData?.currency?.id = productDataForOrderDetail?.currency?.id ?: -1
        AppController.addEditUserProductData?.product_description =
            productDataForOrderDetail?.product_description ?: ""
        AppController.addEditUserProductData?.measurements = productDataForOrderDetail?.measurements ?: ""

        productDataForOrderDetail?.zones?.forEach {
            AppController.addEditUserProductData?.zones?.add(
                ShipingZone(
                    countries_name = it.countries_name, id = it.id, name = it.name,
                    isChecked = true, isEmpty = false,
                    pivot = Pivot(it.pivot.product_id, it.pivot.shipping_charges, it.pivot.shipping_zone_id)
                )
            )
        }

        productDataForOrderDetail?.files?.forEach {
            AppController.addEditUserProductData?.files?.add(File(it.file_name, it.file_path, it.id, it.product_id))
        }

        val intent = Intent(this, AddListingActivity::class.java)
        intent.putExtra("fromEdit", true)
        startActivity(intent)
    }


    private fun updateProductStatus(id: Int, status: String) {
        apiCall = retrofitClient.updateProductStatus(id, status)
        viewModel.updateProductStatus(apiCall)
    }

    private fun deleteProduct(id: Int) {
        apiCall = retrofitClient.deleteProduct(id)
        viewModel.deleteProduct(apiCall)
    }

    private fun restoreTrashedProduct(id: Int) {
        apiCall = retrofitClient.restoreTrashedProduct(id)
        viewModel.restoreTrashedProduct(apiCall)
    }

}