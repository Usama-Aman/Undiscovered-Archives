package com.codingpixel.undiscoveredarchives.home.draft_products

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.astritveliu.boom.Boom
import com.codingpixel.undiscoveredarchives.R
import com.codingpixel.undiscoveredarchives.auth.models.LoginModel
import com.codingpixel.undiscoveredarchives.base.AppController
import com.codingpixel.undiscoveredarchives.base.BaseActivity
import com.codingpixel.undiscoveredarchives.databinding.ActivityDraftProductsBinding
import com.codingpixel.undiscoveredarchives.home.add_new_listing.AddListingActivity
import com.codingpixel.undiscoveredarchives.home.dialogs.DeleteDialogInterface
import com.codingpixel.undiscoveredarchives.home.dialogs.DeleteProduct
import com.codingpixel.undiscoveredarchives.home.favorites.models.*
import com.codingpixel.undiscoveredarchives.home.order_detail.OrderDetailActivity
import com.codingpixel.undiscoveredarchives.loader.Loader
import com.codingpixel.undiscoveredarchives.network.Api
import com.codingpixel.undiscoveredarchives.network.ApiStatus
import com.codingpixel.undiscoveredarchives.network.ApiTags
import com.codingpixel.undiscoveredarchives.network.RetrofitClient
import com.codingpixel.undiscoveredarchives.utils.*
import com.codingpixel.undiscoveredarchives.view_models.HomeViewModel
import com.codingpixel.undiscoveredarchives.view_models.ViewModelFactory
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Call

class DraftProductsActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivityDraftProductsBinding
    private lateinit var mContext: Context
    private lateinit var viewModel: HomeViewModel
    private lateinit var retrofitClient: Api
    private lateinit var apiCall: Call<ResponseBody>

    private lateinit var draftProductsAdapter: DraftProductsAdapter
    private lateinit var dropDownAdapter: ArrayAdapter<DropdownModel>
    private var draftProductsList: MutableList<UserProductData?> = ArrayList()

    private var orderBy = "All"
    private var skip = 0
    private var canLoadMore = false

    private var selectedProductPosition = -1

    companion object {
        var isSelectEnable = false
    }

    private var mBroadCastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == BroadCastEnums.REFRESH_DRAFT_PRODUCTS.type) {
                skip = 0
                draftProductsList.clear()
                getDraftProducts()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDraftProductsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mContext = this
        retrofitClient = RetrofitClient.getClient(this@DraftProductsActivity).create(Api::class.java)

        LocalBroadcastManager.getInstance(this)
            .registerReceiver(mBroadCastReceiver, IntentFilter(BroadCastEnums.REFRESH_DRAFT_PRODUCTS.type))

        blackStatusBarIcons()

        initVM()
        initViews()
        initListeners()
        initAdapter()
        observeApiResponse()

        getDraftProducts()
    }

    @SuppressLint("SetTextI18n")
    private fun initViews() {
        binding.topBar.ivBack.viewVisible()
        binding.topBar.tvScreenTitle.text = "Drafts"

        /*Filter*/
        dropDownAdapter = CustomDropDownAdapter(mContext, AppController.filterDropDownList)
        binding.filter.adapter = dropDownAdapter
    }

    private fun initVM() {
        viewModel = ViewModelProviders.of(this, ViewModelFactory()).get(HomeViewModel::class.java)
        binding.viewModel = viewModel
        binding.executePendingBindings()
        binding.lifecycleOwner = this
    }

    private fun initListeners() {
        /*Filter*/
        binding.filter.setSelection(binding.filter.selectedItemPosition, false)
        binding.filter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                orderBy = AppController.filterDropDownList[position].value
                AppController.filterDropDownList.filter { it.isSelected }.forEach { it.isSelected = false }
                AppController.filterDropDownList[position].isSelected = true

                binding.tvFilter.text = orderBy

                skip = 0
                draftProductsList.clear()
                getDraftProducts()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.topBar.ivBack.setOnClickListener(this)
        binding.btnStartSelling.setOnClickListener(this)
        binding.btnAddNewListing.setOnClickListener(this)
        binding.tvSelect.setOnClickListener(this)
        binding.btnEdit.setOnClickListener(this)
        binding.btnDelete.setOnClickListener(this)
        binding.btnPublish.setOnClickListener(this)

        Boom(binding.btnStartSelling)
        Boom(binding.btnAddNewListing)
        Boom(binding.tvSelect)
        Boom(binding.btnEdit)
        Boom(binding.btnDelete)
        Boom(binding.btnPublish)

        binding.pullToRefresh.setOnRefreshListener {
            skip = 0
            draftProductsList.clear()
            getDraftProducts()
        }
    }

    private fun initAdapter() {
        draftProductsAdapter =
            DraftProductsAdapter(draftProductsList, object : DraftProductsAdapter.DraftProductsInterface {
                override fun onItemClicked(position: Int) {
                    AppController.productDataForOrderDetail = draftProductsList[position]
                    val intent = Intent(mContext, OrderDetailActivity::class.java)
                    intent.putExtra("fromWhere", OrderDetailEnum.DRAFTS.type)
                    startActivity(intent)
                }

                @SuppressLint("NotifyDataSetChanged")
                override fun showListOptions(position: Int) {
                    selectedProductPosition = position

                    if (draftProductsList[position]?.isChecked == true) {
                        draftProductsList.filter { it?.isChecked == true }.forEach { it?.isChecked = false }
                        hideViewSlideDown(binding.constraintListOption)
                    } else {
                        draftProductsList.filter { it?.isChecked == true }.forEach { it?.isChecked = false }
                        draftProductsList[position]?.isChecked = true
                        showViewSlideUp(binding.constraintListOption)
                    }

                    draftProductsAdapter.notifyDataSetChanged()
                }

            })
        binding.productsRecyclerView.adapter = draftProductsAdapter
        binding.productsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val lm = recyclerView.layoutManager as LinearLayoutManager
                if (lm.findLastCompletelyVisibleItemPosition() == draftProductsList.size - 1)
                    if (canLoadMore) {
                        canLoadMore = false
                        getDraftProducts()
                    }
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvSelect -> {
                isSelectEnable = !isSelectEnable
                if (isSelectEnable) {
                    binding.tvSelect.text = "Cancel"
                } else {
                    binding.tvSelect.text = "Select"
                    draftProductsList.filter { it?.isChecked == true }.forEach { it?.isChecked = false }
                    hideViewSlideDown(binding.constraintListOption)
                }
                draftProductsAdapter.notifyDataSetChanged()
            }
            R.id.btnAddNewListing, R.id.btnStartSelling -> {
                startActivity(Intent(mContext, AddListingActivity::class.java))
                if (isPanelShown(binding.constraintListOption)) {
                    binding.tvSelect.callOnClick()
                }
            }
            R.id.ivBack -> {
                finish()
            }
            R.id.btnPublish -> {
                if (selectedProductPosition != -1) {
                    updateProductStatus(draftProductsList[selectedProductPosition]?.id ?: -1, "Published")
                } else
                    AppUtils.showToast(this@DraftProductsActivity, "Please select a product first", false)
            }
            R.id.btnEdit -> {
                if (selectedProductPosition != -1) {
                    AppController.addEditUserProductData = UserProductData()
                    AppController.addEditUserProductData?.id = draftProductsList[selectedProductPosition]?.id ?: -1
                    AppController.addEditUserProductData?.category?.id =
                        draftProductsList[selectedProductPosition]?.category?.id ?: -1
                    AppController.addEditUserProductData?.sub_category?.id =
                        draftProductsList[selectedProductPosition]?.sub_category?.id ?: -1
                    AppController.addEditUserProductData?.designer?.id =
                        draftProductsList[selectedProductPosition]?.designer?.id ?: -1
                    AppController.addEditUserProductData?.product_name =
                        draftProductsList[selectedProductPosition]?.product_name ?: ""

                    draftProductsList[selectedProductPosition]?.variants?.forEach {
                        AppController.addEditUserProductData?.variants?.add(
                            Variant(
                                it.compare_at_price, it.id, it.price, it.quantity, it.size_id,
                                product_id = it.product_id, sizeValue = "", seller_earnings = "", commissioned_price = ""
                            )
                        )
                    }

                    AppController.addEditUserProductData?.condition = draftProductsList[selectedProductPosition]?.condition ?: ""
                    AppController.addEditUserProductData?.currency?.id =
                        draftProductsList[selectedProductPosition]?.currency?.id ?: -1
                    AppController.addEditUserProductData?.product_description =
                        draftProductsList[selectedProductPosition]?.product_description ?: ""
                    AppController.addEditUserProductData?.measurements =
                        draftProductsList[selectedProductPosition]?.measurements ?: ""

                    draftProductsList[selectedProductPosition]?.zones?.forEach {
                        AppController.addEditUserProductData?.zones?.add(
                            ShipingZone(
                                countries_name = it.countries_name, id = it.id, name = it.name,
                                isChecked = true, isEmpty = false,
                                pivot = Pivot(it.pivot.product_id, it.pivot.shipping_charges, it.pivot.shipping_zone_id)
                            )
                        )
                    }

                    draftProductsList[selectedProductPosition]?.files?.forEach {
                        AppController.addEditUserProductData?.files?.add(File(it.file_name, it.file_path, it.id, it.product_id))
                    }

                    val intent = Intent(this, AddListingActivity::class.java)
                    intent.putExtra("fromEdit", true)
                    startActivity(intent)
                } else
                    AppUtils.showToast(
                        this@DraftProductsActivity,
                        "Please select a product first",
                        false
                    )
            }
            R.id.btnDelete -> {
                if (selectedProductPosition != -1) {
                    val deleteProductDialog = DeleteProduct(selectedProductPosition, object : DeleteDialogInterface {
                        override fun onDeleteClicked(productPosition: Int) {
                            updateProductStatus(draftProductsList[productPosition]?.id ?: -1, "Trashed")
                        }
                    })
                    deleteProductDialog.show(supportFragmentManager, "DeleteProductDialog")
                } else
                    AppUtils.showToast(this@DraftProductsActivity, "Please select a product first", false)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
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
                        ApiTags.GET_DRAFTS_PRODUCTS -> {
                            val model = Gson().fromJson(it.data.toString(), UserProductModel::class.java)
                            binding.tvHeader.text = "${model.count} Listings Found"
                            handleDraftsProductsResponse(model.data)
                        }
                        ApiTags.UPDATE_PRODUCT_STATUS -> {
                            val model = Gson().fromJson(it.data.toString(), LoginModel::class.java)
                            AppUtils.showToast(this, model.message, true)

                            draftProductsList.removeAt(selectedProductPosition)
                            updateView()
                        }
                    }
                }
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateView() {
        if (draftProductsList.isEmpty()) {
            hideViewSlideDown(binding.constraintListOption)
            binding.constraintProducts.viewGone()
            binding.llNoDrafts.viewVisible()
        } else {
            binding.llNoDrafts.viewGone()
            binding.constraintProducts.viewVisible()
            draftProductsAdapter.notifyDataSetChanged()
            selectedProductPosition = -1
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun handleDraftsProductsResponse(data: List<UserProductData>) {
        if (binding.pullToRefresh.isRefreshing)
            binding.pullToRefresh.isRefreshing = false

        if (draftProductsList.size > 0)
            draftProductsList.removeAt(draftProductsList.size - 1)

        draftProductsList.addAll(data)

        if (data.size >= AppController.PAGINATION_COUNT) {
            skip += AppController.PAGINATION_COUNT
            draftProductsList.add(null)
            canLoadMore = true
        }

        if (draftProductsList.isEmpty()) {
            binding.constraintProducts.viewGone()
            binding.llNoDrafts.viewVisible()
        } else {
            binding.llNoDrafts.viewGone()
            binding.constraintProducts.viewVisible()
        }

        draftProductsAdapter.notifyDataSetChanged()

    }

    private fun getDraftProducts() {
        apiCall = retrofitClient.getDraftProducts(order_by = orderBy, skip = skip)
        viewModel.getDraftProducts(apiCall)
    }

    private fun updateProductStatus(id: Int, status: String) {
        apiCall = retrofitClient.updateProductStatus(id, status)
        viewModel.updateProductStatus(apiCall)
    }

    override fun onDestroy() {
        super.onDestroy()
        isSelectEnable = false
        AppController.productDataForOrderDetail = null
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadCastReceiver)
    }

    override fun onBackPressed() {
        if (isPanelShown(binding.constraintListOption))
            binding.tvSelect.callOnClick()
        else
            super.onBackPressed()
    }

}