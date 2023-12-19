package com.codingpixel.undiscoveredarchives.home.designers_details

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.codingpixel.undiscoveredarchives.R
import com.codingpixel.undiscoveredarchives.base.AppController
import com.codingpixel.undiscoveredarchives.base.BaseActivity
import com.codingpixel.undiscoveredarchives.databinding.ActivityDesignersDetailsBinding
import com.codingpixel.undiscoveredarchives.home.all_products.AllProductsActivity
import com.codingpixel.undiscoveredarchives.home.all_products.adapters.ProductsWithCategoriesAdapter
import com.codingpixel.undiscoveredarchives.home.main.adapters.ShopByCategoryAdapter
import com.codingpixel.undiscoveredarchives.home.main.models.CategoriesData
import com.codingpixel.undiscoveredarchives.home.main.models.CategoriesModel
import com.codingpixel.undiscoveredarchives.home.main.models.DesignersData
import com.codingpixel.undiscoveredarchives.home.product_detail.ProductDetailActivity
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
import com.github.florent37.glidepalette.BitmapPalette.Profile.MUTED_DARK
import com.github.florent37.glidepalette.GlidePalette
import com.google.gson.Gson
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call


class DesignersDetailsActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivityDesignersDetailsBinding
    private lateinit var mContext: Context
    private lateinit var viewModel: HomeViewModel
    private lateinit var retrofitClient: Api
    private lateinit var apiCall: Call<ResponseBody>

    private lateinit var shopByCategoryAdapter: ShopByCategoryAdapter
    private lateinit var productsWithCategoriesAdapter: ProductsWithCategoriesAdapter
    private var shopByCategoryList: MutableList<CategoriesData> = ArrayList()
    private var designerCategoriesList: MutableList<CategoriesData?> = ArrayList()

    private var designerData: DesignersData? = null
    private var skip = 0
    private var canLoadMore = false

    private lateinit var skeletonLayout: Skeleton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDesignersDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        blackStatusBarIcons()
        transparentStatusBar()

        mContext = this
        retrofitClient = RetrofitClient.getClient(this@DesignersDetailsActivity).create(Api::class.java)


        initViews()
        initVM()
        initListeners()
        initAdapter()
        observeApiResponse()

        if (designerData != null)
            showDesignerDetail()
        else {
            AppUtils.showToast(this, "Something's wrong", false)
            Handler(Looper.getMainLooper()).postDelayed({
                finish()
            }, 500)
        }
    }

    private fun initViews() {
        designerData = intent?.getParcelableExtra("designerData")

        skeletonLayout = binding.skeletonLayout
        binding.ivBack.setOnClickListener {
            finish()
        }


    }

    private fun initVM() {
        viewModel = ViewModelProviders.of(this, ViewModelFactory()).get(HomeViewModel::class.java)
        binding.viewModel = viewModel
        binding.executePendingBindings()
        binding.lifecycleOwner = this
    }

    private fun initListeners() {
        binding.ivFavorite.setOnClickListener(this)
    }

    private fun initAdapter() {
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
        binding.rvShopByCategory.adapter = shopByCategoryAdapter

        binding.rvDesignerCategories.layoutManager = LinearLayoutManager(this)
        productsWithCategoriesAdapter = ProductsWithCategoriesAdapter(
            designerCategoriesList,
            object : ProductsWithCategoriesAdapter.ProductsWithCategoriesInterface {
                override fun onItemClicked(categoryPosition: Int, productPosition: Int) {
                    val intent = Intent(mContext, ProductDetailActivity::class.java)
                    intent.putExtra("productId", designerCategoriesList[categoryPosition]?.products?.get(productPosition)?.id)
                    startActivity(intent)
                }

                override fun onViewMoreClicked(position: Int) {
                    val intent = Intent(mContext, AllProductsActivity::class.java)
                    intent.putExtra("isAllFilter", false)
                    intent.putExtra("categoryId", designerCategoriesList[position]?.id)
                    intent.putExtra("categoryName", designerCategoriesList[position]?.title)
                    startActivity(intent)
                }
            })
        binding.rvDesignerCategories.adapter = productsWithCategoriesAdapter
        binding.rvDesignerCategories.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val lm = recyclerView.layoutManager as LinearLayoutManager
                if (lm.findLastCompletelyVisibleItemPosition() == designerCategoriesList.size - 1)
                    if (canLoadMore) {
                        canLoadMore = false
                        Loader.showLoader((this@DesignersDetailsActivity as AppCompatActivity)) {
                            if (this@DesignersDetailsActivity::apiCall.isInitialized)
                                apiCall.cancel()
                        }
                        getDesignerCategories()
                    }
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun showDesignerDetail() {

        Glide.with(mContext)
            .load(designerData?.logo_path)
            .placeholder(R.drawable.ic_file_placeholder)
            .listener(
                GlidePalette.with(designerData?.logo_path)
                    .use(MUTED_DARK)
                    .intoTextColor(binding.ivBack)
                    .intoTextColor(binding.ivShare)
            )
            .into(binding.ivDisplayImage)

        Glide.with(mContext)
            .load(designerData?.logo_path)
            .placeholder(R.drawable.ic_file_placeholder)
            .into(binding.ivStoreImage)

        binding.tvDesignerName.text = designerData?.name
        binding.tvDesignerDescription.text = designerData?.name

        if (designerData?.products_count == 1)
            binding.tvListingCount.text = "${designerData?.products_count} Listing"
        else
            binding.tvListingCount.text = "${designerData?.products_count} Listings"

        if (designerData?.is_favourite == 1)
            binding.ivFavorite.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_heart_filled))
        else
            binding.ivFavorite.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_heart_unfilled))


        binding.coordinatorLayout.viewGone()
        binding.skeletonLayout.viewVisible()
        skeletonLayout.showSkeleton()
        getDesignerCategories()
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
                        ApiTags.GET_CATEGORIES_WITH_PRODUCTS -> {
                            val model = Gson().fromJson(it.data.toString(), CategoriesModel::class.java)

                            if (!isPanelShown(binding.coordinatorLayout)) {
                                binding.coordinatorLayout.viewVisible()
                                binding.skeletonLayout.viewGone()
                                skeletonLayout.showOriginal()
                            }

                            if (shopByCategoryList.isEmpty()) {
                                shopByCategoryList.addAll(model.data)
                                shopByCategoryAdapter.notifyDataSetChanged()
                            }

                            handleCategoriesResponse(model.data)
                        }
                        ApiTags.ADD_DESIGNER_TO_FAVORITES -> {
                            val jsonObject = JSONObject(it.data.toString())
                            val data = jsonObject.getJSONObject("data")
                            AppUtils.showToast(this@DesignersDetailsActivity, jsonObject.getString("message"), true)

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

    @SuppressLint("NotifyDataSetChanged")
    private fun handleCategoriesResponse(data: List<CategoriesData>) {
        if (designerCategoriesList.size > 0)
            designerCategoriesList.removeAt(designerCategoriesList.size - 1)

        designerCategoriesList.addAll(data)

        if (data.size >= AppController.PAGINATION_COUNT) {
            skip += AppController.PAGINATION_COUNT
            designerCategoriesList.add(null)
            canLoadMore = true
        }

        if (designerCategoriesList.isEmpty()) {
            binding.rvDesignerCategories.viewGone()
            binding.llNoData.viewVisible()
        } else {
            binding.llNoData.viewGone()
            binding.rvDesignerCategories.viewVisible()
        }

        productsWithCategoriesAdapter.notifyDataSetChanged()
    }

    private fun getDesignerCategories() {
        apiCall = retrofitClient.getDesignersCategoriesWithProducts(designer_id = designerData?.id ?: -1, skip = skip)
        viewModel.getCategoriesWithProducts(apiCall)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivFavorite -> {
                if (designerData != null) {
                    Loader.showLoader((this@DesignersDetailsActivity as AppCompatActivity)) {
                        if (this@DesignersDetailsActivity::apiCall.isInitialized)
                            apiCall.cancel()
                    }
                    addDesignerToFavorites(designerData?.id ?: -1)
                }
            }
        }
    }

    private fun addDesignerToFavorites(id: Int) {
        apiCall = retrofitClient.addDesignerProductsToFavorite(id)
        viewModel.addDesignerProductsToFavorite(apiCall)
    }


}