package com.codingpixel.undiscoveredarchives.home.search_result

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codingpixel.undiscoveredarchives.base.AppController
import com.codingpixel.undiscoveredarchives.base.BaseActivity
import com.codingpixel.undiscoveredarchives.databinding.ActivitySearchResultBinding
import com.codingpixel.undiscoveredarchives.home.add_new_listing.ProductsGradientsData
import com.codingpixel.undiscoveredarchives.home.add_new_listing.ProductsGradientsModel
import com.codingpixel.undiscoveredarchives.home.all_products.AllProductsData
import com.codingpixel.undiscoveredarchives.home.all_products.AllProductsModel
import com.codingpixel.undiscoveredarchives.home.product_detail.ProductDetailActivity
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
import org.json.JSONObject
import retrofit2.Call


class SearchResultActivity : BaseActivity() {

    private lateinit var binding: ActivitySearchResultBinding
    private lateinit var mContext: Context
    private lateinit var viewModel: HomeViewModel
    private lateinit var retrofitClient: Api
    private lateinit var apiCall: Call<ResponseBody>

    private var headerList: MutableList<String> = ArrayList()
    private lateinit var expandableListAdapter: CustomExpandableListAdapter
    private var itemList: HashMap<String, MutableList<String>> = HashMap()
    private lateinit var searchAdapter: SearchResultAdapter

    private lateinit var dropDownAdapter: ArrayAdapter<DropdownModel>

    private var orderBy = ""
    private var productCondition = ""
    private var categoryId = 0
    private var designerId = 0
    private var colorId = 0
    private var sizeId = 0
    private var searchWord = ""
    private var skip = 0
    private var canLoadMore = false

    private var favoritePosition = -1

    private var productList: MutableList<AllProductsData?> = ArrayList()
    private lateinit var productGradients: ProductsGradientsData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mContext = this
        retrofitClient = RetrofitClient.getClient(this@SearchResultActivity).create(Api::class.java)

        blackStatusBarIcons()

        initViews()
        initVM()
        initListeners()
        initAdapter()
        expandableList()
        observeApiResponse()

        getProductsGradients()
    }

    private fun expandableList() {
        headerList.add("Categories")
        headerList.add("Designers")
        headerList.add("Size")
        headerList.add("Condition")

        val categoriesList: MutableList<String> = ArrayList()
        categoriesList.add("Tops")
        categoriesList.add("Bottoms")
        categoriesList.add("Outwear")
        categoriesList.add("Footwear")

        val designersList: MutableList<String> = ArrayList()
        designersList.add("Gucci")
        designersList.add("Gucci")
        designersList.add("Nike")
        designersList.add("Parada")

        val sizeList: MutableList<String> = ArrayList()
        sizeList.add("XS")
        sizeList.add("S")
        sizeList.add("M")
        sizeList.add("L")

        val conditionList: MutableList<String> = ArrayList()
        conditionList.add("New")
        conditionList.add("Used")
        conditionList.add("Mostly Worn")
        conditionList.add("Not Specified")

        itemList[headerList[0]] = categoriesList
        itemList[headerList[1]] = designersList
        itemList[headerList[2]] = sizeList
        itemList[headerList[3]] = conditionList
        expandableListAdapter = CustomExpandableListAdapter(this, headerList, itemList)
        binding.navigationView.expandableListView.setAdapter(expandableListAdapter)

    }

    private fun initVM() {
        viewModel = ViewModelProviders.of(this, ViewModelFactory()).get(HomeViewModel::class.java)
        binding.viewModel = viewModel
        binding.executePendingBindings()
        binding.lifecycleOwner = this
    }

    private fun toggleDrawer() {
        if (binding.drawerLayout.isDrawerOpen(binding.navView)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            binding.drawerLayout.openDrawer(GravityCompat.END)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initViews() {
        binding.topBar.tvScreenTitle.text = "Search Results"
        binding.topBar.ivBack.viewVisible()

        /*Filter*/
        dropDownAdapter = CustomDropDownAdapter(mContext, AppController.filterDropDownList)
        binding.filter.adapter = dropDownAdapter

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                searchWord = s.toString()
                if (s.toString().isBlank())
                    binding.ivSearchCross.viewGone()
                else
                    binding.ivSearchCross.viewVisible()
            }
        })

        binding.etSearch.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                //s Your action on done
                getAllProducts()
                true
            } else false
        }

    }

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

        binding.ivFilter.setOnClickListener {
            toggleDrawer()
        }
        binding.topBar.ivBack.setOnClickListener { finish() }

        binding.ivSearchCross.setOnClickListener {
            binding.etSearch.setText("")
            searchWord = ""
            getAllProducts()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun observeApiResponse() {
        viewModel.getApiResponse().observe(this, {
            when (it.status) {
                ApiStatus.LOADING -> {
                    Loader.showLoader(this) {
                        if (this@SearchResultActivity::apiCall.isInitialized)
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
                        ApiTags.GET_ALL_PRODUCTS -> {
                            val model = Gson().fromJson(it.data.toString(), AllProductsModel::class.java)
                            handleProductsResponse(model.data)
                        }
                        ApiTags.GET_PRODUCT_GRADIENTS -> {
                            val model = Gson().fromJson(
                                it.data.toString(),
                                ProductsGradientsModel::class.java
                            )
                            productGradients = model.data

                            initializeDrawer()
                            getAllProducts()
                        }

                        ApiTags.ADD_PRODUCT_TO_FAVORITES -> {
                            val jsonObject = JSONObject(it.data.toString())
                            val data = jsonObject.getJSONObject("data")

                            AppUtils.showToast(this, jsonObject.getString("message"), true)

                            if (data.has("is_favourite"))
                                productList[favoritePosition]?.is_favourite = data.getInt("is_favourite")

                            searchAdapter.notifyDataSetChanged()
                            favoritePosition = -1
                        }
                    }
                }
            }
        })
    }

    private fun initializeDrawer() {

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun handleProductsResponse(data: List<AllProductsData>) {
//        if (binding.pullToRefresh.isRefreshing)
//            binding.pullToRefresh.isRefreshing = false

        if (productList.size > 0)
            productList.removeAt(productList.size - 1)

        productList.addAll(data)

        if (data.size >= AppController.PAGINATION_COUNT) {
            skip += AppController.PAGINATION_COUNT
            productList.add(null)
            canLoadMore = true
        }

        if (productList.isEmpty()) {
            binding.searchRecycler.viewGone()
            binding.llNoData.viewVisible()
        } else {
            binding.llNoData.viewGone()
            binding.searchRecycler.viewVisible()
        }

        searchAdapter.notifyDataSetChanged()
    }


    private fun initAdapter() {
        searchAdapter = SearchResultAdapter(productList, object : SearchResultAdapter.SearchResultInterface {
            override fun onItemClicked(position: Int) {
                val intent = Intent(mContext, ProductDetailActivity::class.java)
                intent.putExtra("productId", productList[position]?.id)
                startActivity(intent)
            }

            override fun onFavoriteClicked(position: Int) {
                favoritePosition = position
                addProductsToFavorites(productList[position]?.id ?: -1)
            }
        })
        binding.searchRecycler.adapter = searchAdapter

        binding.searchRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val lm = recyclerView.layoutManager as LinearLayoutManager
                if (lm.findLastCompletelyVisibleItemPosition() == productList.size - 1)
                    if (canLoadMore) {
                        canLoadMore = false
                        getAllProducts()
                    }
            }
        })
    }

    private fun getProductsGradients() {
        apiCall = retrofitClient.getProductGradients()
        viewModel.getProductGradients(apiCall)
    }

    private fun getAllProducts() {
        apiCall = retrofitClient.getAllProducts(
            orderBy, productCondition, categoryId, designerId, colorId, sizeId, searchWord, skip
        )
        viewModel.getAllProducts(apiCall)
    }

    private fun addProductsToFavorites(id: Int) {
        apiCall = retrofitClient.addProductToFavorite(id)
        viewModel.addProductToFavorite(apiCall)
    }


}