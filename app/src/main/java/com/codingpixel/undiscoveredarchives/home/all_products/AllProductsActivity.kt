package com.codingpixel.undiscoveredarchives.home.all_products

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codingpixel.undiscoveredarchives.base.AppController
import com.codingpixel.undiscoveredarchives.base.BaseActivity
import com.codingpixel.undiscoveredarchives.databinding.ActivityAllProductsBinding
import com.codingpixel.undiscoveredarchives.home.all_products.adapters.AllProductsAdapter
import com.codingpixel.undiscoveredarchives.home.all_products.adapters.ProductsWithCategoriesAdapter
import com.codingpixel.undiscoveredarchives.home.main.models.CategoriesData
import com.codingpixel.undiscoveredarchives.home.main.models.CategoriesModel
import com.codingpixel.undiscoveredarchives.home.product_detail.ProductDetailActivity
import com.codingpixel.undiscoveredarchives.home.search_result.CustomExpandableListAdapter
import com.codingpixel.undiscoveredarchives.home.search_result.SearchResultActivity
import com.codingpixel.undiscoveredarchives.loader.Loader
import com.codingpixel.undiscoveredarchives.network.Api
import com.codingpixel.undiscoveredarchives.network.ApiStatus
import com.codingpixel.undiscoveredarchives.network.ApiTags
import com.codingpixel.undiscoveredarchives.network.RetrofitClient
import com.codingpixel.undiscoveredarchives.utils.AppUtils
import com.codingpixel.undiscoveredarchives.utils.viewGone
import com.codingpixel.undiscoveredarchives.utils.viewVisible
import com.codingpixel.undiscoveredarchives.view_models.HomeViewModel
import com.codingpixel.undiscoveredarchives.view_models.ViewModelFactory
import com.faltenreich.skeletonlayout.Skeleton
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Call


class AllProductsActivity : BaseActivity() {

    private lateinit var binding: ActivityAllProductsBinding
    private lateinit var mContext: Context
    private lateinit var viewModel: HomeViewModel
    private lateinit var retrofitClient: Api
    private lateinit var apiCall: Call<ResponseBody>

    private var isAllFilter: Boolean = false
    private var headerList: MutableList<String> = ArrayList()
    private var itemList: HashMap<String, MutableList<String>> = HashMap()
    private lateinit var expandableListAdapter: CustomExpandableListAdapter

    private var orderBy = ""
    private var productCondition = ""
    private var categoryId = 0
    private var designerId = 0
    private var colorId = 0
    private var sizeId = 0
    private var skip = 0
    private var canLoadMore = false

    private lateinit var allProductsAdapter: AllProductsAdapter
    private var productList: MutableList<AllProductsData?> = ArrayList()
    private lateinit var productsWithCategoriesAdapter: ProductsWithCategoriesAdapter
    private var productsListWithCategories: MutableList<CategoriesData?> = ArrayList()

    private lateinit var skeletonLayout: Skeleton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllProductsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        retrofitClient = RetrofitClient.getClient(this@AllProductsActivity).create(Api::class.java)
        mContext = this

        blackStatusBarIcons()

        initVM()
        initViews()
        expandableList()
        initListeners()
        initAdapter()
        observeApiResponse()
    }

    private fun initViews() {
        isAllFilter = intent.getBooleanExtra("isAllFilter", false)

        if (isAllFilter) {
            binding.tvProductCategory.viewGone()
            binding.allProductConstraint.viewGone()
            binding.productsWithCategoriesSkeleton.viewVisible()
            skeletonLayout = binding.productsWithCategoriesSkeleton
            skeletonLayout.showSkeleton()
            getProductsWithAllCategories()
        } else {
            binding.tvProductCategory.viewVisible()

            categoryId = intent.getIntExtra("categoryId", 0)
            val categoryName = intent.getStringExtra("categoryName")
            binding.tvProductCategory.text = categoryName

            binding.allProductConstraint.viewGone()
            binding.allProductSkeleton.viewVisible()
            skeletonLayout = binding.allProductSkeleton
            skeletonLayout.showSkeleton()
            getAllProducts()
        }

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


    private fun initListeners() {
        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.ivFilter.setOnClickListener {
            toggleDrawer()
        }

        binding.cvSearch.setOnClickListener {
            startActivity(Intent(this@AllProductsActivity, SearchResultActivity::class.java))
        }

        binding.pullToRefresh.setOnRefreshListener {
            productList.clear()
            productsListWithCategories.clear()
            if (isAllFilter) {
                binding.allProductConstraint.viewGone()
                binding.productsWithCategoriesSkeleton.viewVisible()
                skeletonLayout = binding.productsWithCategoriesSkeleton
                skeletonLayout.showSkeleton()
                getProductsWithAllCategories()
            } else {
                categoryId = intent.getIntExtra("categoryId", 0)
                val categoryName = intent.getStringExtra("categoryName")
                binding.tvProductCategory.text = categoryName

                binding.allProductConstraint.viewGone()
                binding.allProductSkeleton.viewVisible()
                skeletonLayout = binding.allProductSkeleton
                skeletonLayout.showSkeleton()
                getAllProducts()
            }
        }

    }

    private fun initAdapter() {
        if (isAllFilter) {
            binding.productsWithCategoriesRecyclerView.viewVisible()
            binding.allProductsRecyclerView.viewGone()

            binding.productsWithCategoriesRecyclerView.layoutManager = LinearLayoutManager(this)
            productsWithCategoriesAdapter = ProductsWithCategoriesAdapter(
                productsListWithCategories,
                object : ProductsWithCategoriesAdapter.ProductsWithCategoriesInterface {
                    override fun onItemClicked(categoryPosition: Int, productPosition: Int) {
                        val intent = Intent(mContext, ProductDetailActivity::class.java)
                        intent.putExtra(
                            "productId",
                            productsListWithCategories[categoryPosition]?.products?.get(productPosition)?.id
                        )
                        startActivity(intent)
                    }

                    override fun onViewMoreClicked(position: Int) {
                        val intent = Intent(mContext, AllProductsActivity::class.java)
                        intent.putExtra("isAllFilter", false)
                        intent.putExtra("categoryId", productsListWithCategories[position]?.id)
                        intent.putExtra("categoryName", productsListWithCategories[position]?.title)
                        startActivity(intent)
                    }
                })
            binding.productsWithCategoriesRecyclerView.adapter = productsWithCategoriesAdapter

        } else {
            binding.allProductsRecyclerView.viewVisible()
            binding.productsWithCategoriesRecyclerView.viewGone()

            binding.allProductsRecyclerView.layoutManager = GridLayoutManager(this, 2)
            allProductsAdapter = AllProductsAdapter(
                products = productList,
                allProductsInterface = object : AllProductsAdapter.AllProductsInterface {
                    override fun onItemClicked(productPosition: Int) {
                        val intent = Intent(mContext, ProductDetailActivity::class.java)
                        intent.putExtra("productId", productList[productPosition]?.id)
                        startActivity(intent)
                    }
                })
            binding.allProductsRecyclerView.adapter = allProductsAdapter
        }

        binding.allProductsRecyclerView.setHasFixedSize(true)
        binding.productsWithCategoriesRecyclerView.setHasFixedSize(true)

        binding.productsWithCategoriesRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val lm = recyclerView.layoutManager as LinearLayoutManager
                if (lm.findLastCompletelyVisibleItemPosition() == productsListWithCategories.size - 1)
                    if (canLoadMore) {
                        canLoadMore = false
                        getProductsWithAllCategories()
                    }
            }
        })

        binding.allProductsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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

    @SuppressLint("NotifyDataSetChanged")
    private fun observeApiResponse() {
        viewModel.getApiResponse().observe(this, {
            when (it.status) {
                ApiStatus.LOADING -> {
//                    Loader.showLoader(this) {
//                        if (this@AllProductsActivity::apiCall.isInitialized)
//                            apiCall.cancel()
//                    }
                }
                ApiStatus.ERROR -> {
                    Loader.hideLoader()
                    AppUtils.showToast(this, it.message!!, false)
                }
                ApiStatus.SUCCESS -> {
                    Loader.hideLoader()
                    when (it.tag) {
                        ApiTags.GET_CATEGORIES_WITH_PRODUCTS -> {
                            if (binding.productsWithCategoriesSkeleton.isShown) {
                                binding.allProductConstraint.viewVisible()
                                binding.productsWithCategoriesSkeleton.viewGone()
                                binding.productsWithCategoriesSkeleton.showOriginal()
                            }

                            val model = Gson().fromJson(it.data.toString(), CategoriesModel::class.java)
                            handleCategoriesResponse(model.data)
                        }
                        ApiTags.GET_ALL_PRODUCTS -> {
                            if (binding.allProductSkeleton.isShown) {
                                binding.allProductConstraint.viewVisible()
                                binding.allProductSkeleton.viewGone()
                                binding.allProductSkeleton.showOriginal()
                            }

                            val model = Gson().fromJson(it.data.toString(), AllProductsModel::class.java)
                            handleProductsResponse(model.data)
                        }
                    }
                }
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun handleCategoriesResponse(data: List<CategoriesData>) {
        if (binding.pullToRefresh.isRefreshing)
            binding.pullToRefresh.isRefreshing = false

        if (productsListWithCategories.size > 0)
            productsListWithCategories.removeAt(productsListWithCategories.size - 1)

        productsListWithCategories.addAll(data)

        if (data.size >= AppController.PAGINATION_COUNT) {
            skip += AppController.PAGINATION_COUNT
            productsListWithCategories.add(null)
            canLoadMore = true
        }

        if (productsListWithCategories.isEmpty()) {
            binding.productsWithCategoriesRecyclerView.viewGone()
            binding.llNoData.viewVisible()
        } else {
            binding.llNoData.viewGone()
            binding.productsWithCategoriesRecyclerView.viewVisible()
        }

        productsWithCategoriesAdapter.notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun handleProductsResponse(data: List<AllProductsData>) {
        if (binding.pullToRefresh.isRefreshing)
            binding.pullToRefresh.isRefreshing = false

        if (productList.size > 0)
            productList.removeAt(productList.size - 1)

        productList.addAll(data)

        if (data.size >= AppController.PAGINATION_COUNT) {
            skip += AppController.PAGINATION_COUNT
            productList.add(null)
            canLoadMore = true
        }

        if (productList.isEmpty()) {
            binding.allProductsRecyclerView.viewGone()
            binding.llNoData.viewVisible()
        } else {
            binding.llNoData.viewGone()
            binding.allProductsRecyclerView.viewVisible()
        }

        allProductsAdapter.notifyDataSetChanged()
    }


    private fun getProductsWithAllCategories() {
        apiCall = retrofitClient.getCategories(with_products = 1, skip = skip)
        viewModel.getCategoriesWithProducts(apiCall)
    }

    private fun getAllProducts() {
        apiCall = retrofitClient.getAllProducts(
            orderBy, productCondition, categoryId, designerId, colorId, sizeId, skip = skip
        )
        viewModel.getAllProducts(apiCall)
    }


}