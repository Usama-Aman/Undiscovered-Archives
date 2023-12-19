package com.codingpixel.undiscoveredarchives.home.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.codingpixel.undiscoveredarchives.databinding.FragmentHomeBinding
import com.codingpixel.undiscoveredarchives.home.all_products.AllProductsActivity
import com.codingpixel.undiscoveredarchives.home.designers.DesignersActivity
import com.codingpixel.undiscoveredarchives.home.designers_details.DesignersDetailsActivity
import com.codingpixel.undiscoveredarchives.home.main.adapters.*
import com.codingpixel.undiscoveredarchives.home.main.models.*
import com.codingpixel.undiscoveredarchives.home.product_detail.ProductDetailActivity
import com.codingpixel.undiscoveredarchives.home.search_result.SearchResultActivity
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


class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var mContext: Context
    private lateinit var viewModel: HomeViewModel
    private lateinit var retrofitClient: Api
    private lateinit var apiCall: Call<ResponseBody>

    private var filterChips = ArrayList<CategoriesData>()
    private var categoriesList: MutableList<CategoriesData> = ArrayList()
    private var shopByCategoryList: MutableList<CategoriesData> = ArrayList()
    private var designersList: MutableList<DesignersData> = ArrayList()
    private var featuredProductList: MutableList<FeaturedProductsData> = ArrayList()

    private lateinit var categoryFilterAdapter: CategoryFilterAdapter
    private lateinit var featuredProductsAdapter: FeaturedProductsAdapter
    private lateinit var shopByCategoryAdapter: ShopByCategoryAdapter
    private lateinit var designersAdapter: TopDesignersAdapter
    private lateinit var categoriesAdapter: CategoryMainAdapter

    private lateinit var skeletonLayout: Skeleton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        mContext = requireContext()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        retrofitClient = RetrofitClient.getClient(requireContext()).create(Api::class.java)

        initVM()
        initListeners()

        initCategoriesAdapter()
        initFeaturedAdapter()
        initShopByCategoryAdapter()
        initDesignersAdapter()

        observeApiResponse()

        skeletonLayout = binding.skeletonLayout
        getCategories()
    }

    private fun initVM() {
        viewModel = ViewModelProviders.of(this, ViewModelFactory()).get(HomeViewModel::class.java)
        binding.viewModel = viewModel
        binding.executePendingBindings()
        binding.lifecycleOwner = this
    }

    private fun initListeners() {
        binding.tvDesignersViewAll.setOnClickListener {
            startActivity(Intent(mContext, DesignersActivity::class.java))
        }

        binding.cvSearch.setOnClickListener {
            mContext.startActivity(Intent(mContext, SearchResultActivity::class.java))
        }

        binding.pullToRefresh.setOnRefreshListener {
            binding.pullToRefresh.isRefreshing = false
            filterChips.clear()
            categoriesList.clear()
            shopByCategoryList.clear()
            designersList.clear()
            featuredProductList.clear()
            getCategories()
        }
    }

    private fun initShopByCategoryAdapter() {
        shopByCategoryAdapter =
            ShopByCategoryAdapter(shopByCategoryList, object : ShopByCategoryAdapter.ShopByCategoryInterface {
                override fun onItemClicked(position: Int) {
                    val intent = Intent(mContext, AllProductsActivity::class.java)
                    intent.putExtra("isAllFilter", false)
                    intent.putExtra("categoryId", shopByCategoryList[position].id)
                    intent.putExtra("categoryName", shopByCategoryList[position].title)
                    startActivity(intent)
                }
            })
        binding.recyclerShopByCategory.adapter = shopByCategoryAdapter
    }

    private fun initCategoriesAdapter() {
        categoryFilterAdapter = CategoryFilterAdapter(filterChips, object : CategoryFilterAdapter.FilterInterface {
            override fun onFilterItemClick(position: Int) {
                val intent = Intent(mContext, AllProductsActivity::class.java)
                if (position == 0) {
                    intent.putExtra("isAllFilter", true)
                } else {
                    intent.putExtra("isAllFilter", false)
                    intent.putExtra("categoryId", filterChips[position].id)
                    intent.putExtra("categoryName", filterChips[position].title)
                }
                startActivity(intent)
            }
        })
        binding.recyclerFilterChips.adapter = categoryFilterAdapter

        categoriesAdapter = CategoryMainAdapter(categoriesList, object : CategoryMainAdapter.HomeCategoryInterface {
            override fun onViewMoreClicked(position: Int) {
                val intent = Intent(mContext, AllProductsActivity::class.java)
                intent.putExtra("isAllFilter", false)
                intent.putExtra("categoryId", categoriesList[position].id)
                intent.putExtra("categoryName", categoriesList[position].title)
                startActivity(intent)
            }

            override fun onProductItemClicked(categoryPosition: Int, productPosition: Int) {
                val intent = Intent(mContext, ProductDetailActivity::class.java)
                intent.putExtra("productId", categoriesList[categoryPosition].products[productPosition].id)
                startActivity(intent)
            }
        })
        binding.mainCategoriesRecyclerView.adapter = categoriesAdapter
    }

    private fun initDesignersAdapter() {
        designersAdapter = TopDesignersAdapter(designersList, object : TopDesignersAdapter.DesignersInterface {
            override fun onItemClicked(position: Int) {
                val intent = Intent(mContext, DesignersDetailsActivity::class.java)
                intent.putExtra("designerData", designersList[position])
                mContext.startActivity(intent)
            }
        })
        binding.recyclerTopDesigners.adapter = designersAdapter
    }

    private fun initFeaturedAdapter() {
        featuredProductsAdapter =
            FeaturedProductsAdapter(
                featuredProductList,
                object : FeaturedProductsAdapter.FeatureProductInterface {
                    override fun onProductItemClicked(position: Int) {
                        val intent = Intent(mContext, ProductDetailActivity::class.java)
                        intent.putExtra("productId", featuredProductList[position].id)
                        startActivity(intent)
                    }
                })
        binding.recyclerFeaturedProducts.adapter = featuredProductsAdapter

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun observeApiResponse() {
        viewModel.getApiResponse().observe(requireActivity(), {
            when (it.status) {
                ApiStatus.LOADING -> {
//                    Loader.showLoader((requireActivity() as AppCompatActivity)) {
//                        if (this@HomeFragment::apiCall.isInitialized)
//                            apiCall.cancel()
//                    }
                }
                ApiStatus.ERROR -> {
//                    Loader.hideLoader()
                    AppUtils.showToast(requireActivity(), it.message!!, false)
                }
                ApiStatus.SUCCESS -> {
                    when (it.tag) {
                        ApiTags.GET_CATEGORIES -> {
                            val model = Gson().fromJson(it.data.toString(), CategoriesModel::class.java)

                            filterChips.add(
                                CategoriesData(
                                    0, "", -1, "",
                                    "", ArrayList(), 0, "All"
                                )
                            )
                            filterChips.addAll(model.data)
                            categoryFilterAdapter.notifyDataSetChanged()

                            getFeaturedProducts()
                        }
                        ApiTags.GET_FEATURED_PRODUCTS -> {
                            val model = Gson().fromJson(it.data.toString(), FeaturedProductsModel::class.java)
                            featuredProductList.addAll(model.data)
                            featuredProductsAdapter.notifyDataSetChanged()

                            shopByCategoryList.addAll(filterChips)
                            shopByCategoryList.removeAt(0)
                            shopByCategoryAdapter.notifyDataSetChanged()

                            getDesigners()
                        }
                        ApiTags.GET_DESIGNERS -> {
                            val model = Gson().fromJson(it.data.toString(), DesignersModel::class.java)
                            designersList.addAll(model.data)
                            designersAdapter.notifyDataSetChanged()

                            getCategories(1)
                        }
                        ApiTags.GET_CATEGORIES_WITH_PRODUCTS -> {
//                            Loader.hideLoader()
                            binding.homeScrollView.viewVisible()
                            binding.skeletonLayout.viewGone()
                            skeletonLayout.showOriginal()

                            val model = Gson().fromJson(it.data.toString(), CategoriesModel::class.java)
                            categoriesList.addAll(model.data)
                            categoriesAdapter.notifyDataSetChanged()
                        }
                    }
                }
            }
        })
    }

    private fun getCategories(withProducts: Int = 0) {
        binding.homeScrollView.viewGone()
        binding.skeletonLayout.viewVisible()
        skeletonLayout.showSkeleton()

        if (withProducts == 0) {
            apiCall = retrofitClient.getCategories(
                with_products = withProducts,
                skip = 0,
                take = 1000
            ) //To get all the categories (chaypi)
            viewModel.getCategories(apiCall)
        } else {
            apiCall = retrofitClient.getCategories(with_products = withProducts, skip = 0, take = 5)
            viewModel.getCategoriesWithProducts(apiCall)
        }
    }

    private fun getDesigners() {
        apiCall = retrofitClient.getDesigners()
        viewModel.getDesigners(apiCall)
    }

    private fun getFeaturedProducts() {
        apiCall = retrofitClient.getFeaturedProducts()
        viewModel.getFeaturedProducts(apiCall)
    }


}