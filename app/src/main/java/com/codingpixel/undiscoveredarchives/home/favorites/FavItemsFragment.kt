package com.codingpixel.undiscoveredarchives.home.favorites

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codingpixel.undiscoveredarchives.base.AppController
import com.codingpixel.undiscoveredarchives.base.AppController.Companion.filterDropDownList
import com.codingpixel.undiscoveredarchives.databinding.TabFavoriteFragmentBinding
import com.codingpixel.undiscoveredarchives.home.favorites.adapters.FavoriteItemAdapter
import com.codingpixel.undiscoveredarchives.home.favorites.models.UserProductData
import com.codingpixel.undiscoveredarchives.home.favorites.models.UserProductModel
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

class FavItemsFragment : Fragment() {

    private lateinit var binding: TabFavoriteFragmentBinding
    private lateinit var mContext: Context
    private lateinit var viewModel: HomeViewModel
    private lateinit var retrofitClient: Api
    private lateinit var apiCall: Call<ResponseBody>

    private var favoriteProductList: MutableList<UserProductData?> = ArrayList()
    private lateinit var favoriteItemAdapter: FavoriteItemAdapter

    private lateinit var dropDownAdapter: ArrayAdapter<DropdownModel>

    private var orderBy = "All"
    private var skip = 0
    private var canLoadMore = false
    private var unfavoriteProductPosition = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = TabFavoriteFragmentBinding.inflate(inflater, container, false)
        mContext = requireContext()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        retrofitClient = RetrofitClient.getClient(requireContext()).create(Api::class.java)

        initViews()
        initVM()
        initListeners()
        initAdapter()
        observeApiResponse()

        getFavoriteProducts()
    }

    private fun initViews() {
        binding.filter.viewVisible()
        binding.tvFilter.viewVisible()

        /*Filter*/
        dropDownAdapter = CustomDropDownAdapter(mContext, filterDropDownList)
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
        binding.filter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                orderBy = filterDropDownList[position].value
                filterDropDownList.filter { it.isSelected }.forEach { it.isSelected = false }
                filterDropDownList[position].isSelected = true

                binding.tvFilter.text = orderBy

                skip = 0
                favoriteProductList.clear()
                getFavoriteProducts()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.pullToRefresh.setOnRefreshListener {
            skip = 0
            favoriteProductList.clear()
            getFavoriteProducts()
        }
    }

    private fun initAdapter() {
        favoriteItemAdapter = FavoriteItemAdapter(
            favoriteProductList,
            object : FavoriteItemAdapter.FavoriteInterface {
                override fun onItemClicked(position: Int) {
                    val intent = Intent(mContext, ProductDetailActivity::class.java)
                    intent.putExtra("productId", favoriteProductList[position]?.id)
                    startActivity(intent)
                }

                override fun onFavoriteClicked(position: Int) {
                    unfavoriteProductPosition = position
                    addProductsToFavorites(favoriteProductList[position]?.id ?: -1)
                }
            },
            true
        )
        binding.recyclerFavorites.adapter = favoriteItemAdapter

        binding.recyclerFavorites.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val lm = recyclerView.layoutManager as LinearLayoutManager
                if (lm.findLastCompletelyVisibleItemPosition() == favoriteProductList.size - 1)
                    if (canLoadMore) {
                        canLoadMore = false
                        getFavoriteProducts()
                    }
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun observeApiResponse() {
        viewModel.getApiResponse().observe(requireActivity(), {
            when (it.status) {
                ApiStatus.LOADING -> {
                    Loader.showLoader((requireActivity() as AppCompatActivity)) {
                        if (this@FavItemsFragment::apiCall.isInitialized)
                            apiCall.cancel()
                    }
                }
                ApiStatus.ERROR -> {
                    Loader.hideLoader()
                    if (binding.pullToRefresh.isRefreshing)
                        binding.pullToRefresh.isRefreshing = false
                    AppUtils.showToast(requireActivity(), it.message!!, false)
                }
                ApiStatus.SUCCESS -> {
                    Loader.hideLoader()
                    when (it.tag) {
                        ApiTags.GET_FAVORITE_PRODUCTS -> {
                            val model =
                                Gson().fromJson(it.data.toString(), UserProductModel::class.java)
                            handleFavoriteProductsResponse(model.data)
                        }
                        ApiTags.ADD_PRODUCT_TO_FAVORITES -> {
                            val jsonObject = JSONObject(it.data.toString())
                            AppUtils.showToast(
                                requireActivity(),
                                jsonObject.getString("message"),
                                true
                            )
                            favoriteProductList.removeAt(unfavoriteProductPosition)
                            favoriteItemAdapter.notifyDataSetChanged()
                            unfavoriteProductPosition = -1

                            if (favoriteProductList.isEmpty()) {
                                binding.recyclerFavorites.viewGone()
                                binding.llNoData.viewVisible()
                            } else {
                                binding.recyclerFavorites.viewVisible()
                                binding.llNoData.viewGone()
                            }
                        }
                    }
                }
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun handleFavoriteProductsResponse(data: List<UserProductData>) {
        if (binding.pullToRefresh.isRefreshing)
            binding.pullToRefresh.isRefreshing = false

        if (favoriteProductList.size > 0)
            favoriteProductList.removeAt(favoriteProductList.size - 1)

        favoriteProductList.addAll(data)

        if (data.size >= AppController.PAGINATION_COUNT) {
            skip += AppController.PAGINATION_COUNT
            favoriteProductList.add(null)
            canLoadMore = true
        }


        if (favoriteProductList.isEmpty()) {
            binding.recyclerFavorites.viewGone()
            binding.llNoData.viewVisible()
        } else {
            binding.llNoData.viewGone()
            binding.recyclerFavorites.viewVisible()
        }

        favoriteItemAdapter.notifyDataSetChanged()

    }

    private fun getFavoriteProducts() {
        apiCall = retrofitClient.getFavoriteProducts(order_by = orderBy, skip = skip)
        viewModel.getFavoriteProducts(apiCall)
    }

    private fun addProductsToFavorites(id: Int) {
        apiCall = retrofitClient.addProductToFavorite(id)
        viewModel.addProductToFavorite(apiCall)
    }


}
