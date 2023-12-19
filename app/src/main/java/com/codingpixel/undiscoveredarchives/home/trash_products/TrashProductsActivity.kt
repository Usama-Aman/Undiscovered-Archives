package com.codingpixel.undiscoveredarchives.home.trash_products

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
import com.codingpixel.undiscoveredarchives.databinding.ActivityTrashProductsBinding
import com.codingpixel.undiscoveredarchives.home.add_new_listing.AddListingActivity
import com.codingpixel.undiscoveredarchives.home.dialogs.DeleteDialogInterface
import com.codingpixel.undiscoveredarchives.home.dialogs.PermanentlyDeleteProduct
import com.codingpixel.undiscoveredarchives.home.favorites.models.UserProductData
import com.codingpixel.undiscoveredarchives.home.favorites.models.UserProductModel
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

class TrashProductsActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivityTrashProductsBinding
    private lateinit var mContext: Context
    private lateinit var viewModel: HomeViewModel
    private lateinit var retrofitClient: Api
    private lateinit var apiCall: Call<ResponseBody>

    private lateinit var trashProductsAdapter: TrashProductsAdapter
    private lateinit var dropDownAdapter: ArrayAdapter<DropdownModel>
    private var trashProductsList: MutableList<UserProductData?> = ArrayList()

    private var orderBy = "All"
    private var skip = 0
    private var canLoadMore = false

    private var selectedProductPosition = -1

    companion object {
        var isSelectEnable = false
    }

    private var mBroadCastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == BroadCastEnums.REFRESH_TRASH_PRODUCTS.type) {
                skip = 0
                trashProductsList.clear()
                getTrashProducts()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrashProductsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mContext = this
        retrofitClient = RetrofitClient.getClient(this@TrashProductsActivity).create(Api::class.java)

        LocalBroadcastManager.getInstance(this)
            .registerReceiver(mBroadCastReceiver, IntentFilter(BroadCastEnums.REFRESH_TRASH_PRODUCTS.type))

        blackStatusBarIcons()

        initVM()
        initViews()
        initListeners()
        initAdapter()
        observeApiResponse()

        getTrashProducts()
    }

    @SuppressLint("SetTextI18n")
    private fun initViews() {
        binding.topBar.ivBack.viewVisible()
        binding.topBar.tvScreenTitle.text = "Trash"

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
                trashProductsList.clear()
                getTrashProducts()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.topBar.ivBack.setOnClickListener(this)
        binding.tvSelect.setOnClickListener(this)
        binding.btnRestore.setOnClickListener(this)
        binding.btnDeleteTrash.setOnClickListener(this)

        Boom(binding.tvSelect)
        Boom(binding.btnRestore)
        Boom(binding.btnDeleteTrash)

        binding.pullToRefresh.setOnRefreshListener {
            skip = 0
            trashProductsList.clear()
            getTrashProducts()
        }
    }

    private fun initAdapter() {
        trashProductsAdapter =
            TrashProductsAdapter(trashProductsList, object : TrashProductsAdapter.TrashProductsInterface {
                override fun onItemClicked(position: Int) {
                    AppController.productDataForOrderDetail = trashProductsList[position]
                    val intent = Intent(mContext, OrderDetailActivity::class.java)
                    intent.putExtra("fromWhere", OrderDetailEnum.TRASH_PRODUCTS.type)
                    startActivity(intent)
                }

                @SuppressLint("NotifyDataSetChanged")
                override fun showListOptions(position: Int) {
                    selectedProductPosition = position

                    if (trashProductsList[position]?.isChecked == true) {
                        trashProductsList.filter { it?.isChecked == true }.forEach { it?.isChecked = false }
                        hideViewSlideDown(binding.constraintListOption)
                    } else {
                        trashProductsList.filter { it?.isChecked == true }.forEach { it?.isChecked = false }
                        trashProductsList[position]?.isChecked = true
                        showViewSlideUp(binding.constraintListOption)
                    }

                    trashProductsAdapter.notifyDataSetChanged()
                }

            })
        binding.productsRecyclerView.adapter = trashProductsAdapter
        binding.productsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val lm = recyclerView.layoutManager as LinearLayoutManager
                if (lm.findLastCompletelyVisibleItemPosition() == trashProductsList.size - 1)
                    if (canLoadMore) {
                        canLoadMore = false
                        getTrashProducts()
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
                    trashProductsList.filter { it?.isChecked == true }.forEach { it?.isChecked = false }
                    hideViewSlideDown(binding.constraintListOption)
                }
                trashProductsAdapter.notifyDataSetChanged()
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
            R.id.btnRestore -> {
                if (selectedProductPosition != -1)
                    restoreTrashedProduct(trashProductsList[selectedProductPosition]?.id ?: -1)
                else
                    AppUtils.showToast(this@TrashProductsActivity, "Please select a product first", false)
            }
            R.id.btnDeleteTrash -> {
                if (selectedProductPosition != -1) {
                    val deleteDialog =
                        PermanentlyDeleteProduct(selectedProductPosition, object : DeleteDialogInterface {
                            override fun onDeleteClicked(productPosition: Int) {
                                deleteProduct(trashProductsList[productPosition]?.id ?: -1)
                            }
                        })
                    deleteDialog.show(supportFragmentManager, "PermanentlyDeleteDialog")
                } else
                    AppUtils.showToast(this@TrashProductsActivity, "Please select a product first", false)
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
                        ApiTags.GET_TRASHED_PRODUCTS -> {
                            val model = Gson().fromJson(it.data.toString(), UserProductModel::class.java)
                            binding.tvHeader.text = "${model.count} Listings Found"
                            handleDraftsProductsResponse(model.data)
                        }
                        ApiTags.RESTORE_TRASHED_PRODUCT -> {
                            val model = Gson().fromJson(it.data.toString(), LoginModel::class.java)
                            AppUtils.showToast(this, model.message, true)

                            trashProductsList.removeAt(selectedProductPosition)
                            updateView()
                        }
                        ApiTags.DELETE_PRODUCT -> {
                            val model = Gson().fromJson(it.data.toString(), LoginModel::class.java)
                            AppUtils.showToast(this, model.message, true)

                            trashProductsList.removeAt(selectedProductPosition)
                            updateView()
                        }
                    }
                }
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateView() {
        if (trashProductsList.isEmpty()) {
            hideViewSlideDown(binding.constraintListOption)
            binding.constraintProducts.viewGone()
            binding.llNoTrash.viewVisible()
        } else {
            binding.llNoTrash.viewGone()
            binding.constraintProducts.viewVisible()
            trashProductsAdapter.notifyDataSetChanged()
            selectedProductPosition = -1
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun handleDraftsProductsResponse(data: List<UserProductData>) {
        if (binding.pullToRefresh.isRefreshing)
            binding.pullToRefresh.isRefreshing = false

        if (trashProductsList.size > 0)
            trashProductsList.removeAt(trashProductsList.size - 1)

        trashProductsList.addAll(data)

        if (data.size >= AppController.PAGINATION_COUNT) {
            skip += AppController.PAGINATION_COUNT
            trashProductsList.add(null)
            canLoadMore = true
        }

        if (trashProductsList.isEmpty()) {
            binding.constraintProducts.viewGone()
            binding.llNoTrash.viewVisible()
        } else {
            binding.llNoTrash.viewGone()
            binding.constraintProducts.viewVisible()
        }

        trashProductsAdapter.notifyDataSetChanged()

    }


    private fun getTrashProducts() {
        apiCall = retrofitClient.getTrashProducts(order_by = orderBy, skip = skip)
        viewModel.getTrashProducts(apiCall)
    }

    private fun deleteProduct(id: Int) {
        apiCall = retrofitClient.deleteProduct(id)
        viewModel.deleteProduct(apiCall)
    }

    private fun restoreTrashedProduct(id: Int) {
        apiCall = retrofitClient.restoreTrashedProduct(id)
        viewModel.restoreTrashedProduct(apiCall)
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