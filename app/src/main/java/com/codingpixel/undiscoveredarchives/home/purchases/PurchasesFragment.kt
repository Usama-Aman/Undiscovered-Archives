package com.codingpixel.undiscoveredarchives.home.purchases

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.codingpixel.undiscoveredarchives.base.AppController
import com.codingpixel.undiscoveredarchives.databinding.TabFavoriteFragmentBinding
import com.codingpixel.undiscoveredarchives.home.order_detail.OrderDetailActivity
import com.codingpixel.undiscoveredarchives.loader.Loader
import com.codingpixel.undiscoveredarchives.network.Api
import com.codingpixel.undiscoveredarchives.network.ApiStatus
import com.codingpixel.undiscoveredarchives.network.ApiTags
import com.codingpixel.undiscoveredarchives.network.RetrofitClient
import com.codingpixel.undiscoveredarchives.utils.AppUtils
import com.codingpixel.undiscoveredarchives.utils.OrderDetailEnum
import com.codingpixel.undiscoveredarchives.utils.viewGone
import com.codingpixel.undiscoveredarchives.utils.viewVisible
import com.codingpixel.undiscoveredarchives.view_models.HomeViewModel
import com.codingpixel.undiscoveredarchives.view_models.ViewModelFactory
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Call

class PurchasesFragment : Fragment() {

    private lateinit var binding: TabFavoriteFragmentBinding
    private lateinit var mContext: Context
    private lateinit var viewModel: HomeViewModel
    private lateinit var retrofitClient: Api
    private lateinit var apiCall: Call<ResponseBody>

    private var purchaseOrdersList: MutableList<UserOrdersData?> = ArrayList()
    private lateinit var purchaseAdapter: PurchaseAdapter

    private var skip = 0
    private var canLoadMore = false

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
        retrofitClient = RetrofitClient.getClient(mContext).create(Api::class.java)

        initViews()
        initVM()
        initAdapter()
        observeApiResponse()

        getPurchaseHistory()
    }

    private fun initViews() {
    }

    private fun initVM() {
        viewModel = ViewModelProviders.of(this, ViewModelFactory()).get(HomeViewModel::class.java)
        binding.viewModel = viewModel
        binding.executePendingBindings()
        binding.lifecycleOwner = this
    }

    private fun initAdapter() {
        purchaseAdapter = PurchaseAdapter(purchaseOrdersList, object : PurchaseAdapter.PurchaseInterface {
            override fun onItemClicked(position: Int) {
                val intent = Intent(mContext, OrderDetailActivity::class.java)
                intent.putExtra("fromWhere", OrderDetailEnum.PURCHASE_HISTORY.type)
                startActivity(intent)
            }
        })
        binding.recyclerFavorites.adapter = purchaseAdapter
    }

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    private fun observeApiResponse() {
        viewModel.getApiResponse().observe(requireActivity(), {
            when (it.status) {
                ApiStatus.LOADING -> {
                    Loader.showLoader((requireActivity() as AppCompatActivity)) {
                        if (this::apiCall.isInitialized)
                            apiCall.cancel()
                    }
                }
                ApiStatus.ERROR -> {
                    Loader.hideLoader()
                    AppUtils.showToast(requireActivity(), it.message!!, false)
                }
                ApiStatus.SUCCESS -> {
                    Loader.hideLoader()
                    when (it.tag) {
                        ApiTags.GET_DRAFTS_PRODUCTS -> {
                            val model = Gson().fromJson(it.data.toString(), UserOrdersModel::class.java)
                            binding.tvHeader.text = "${model.count} Listings Found"
                            handleOrderResponse(model.data)
                        }
                    }
                }
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun handleOrderResponse(data: List<UserOrdersData>) {
        if (binding.pullToRefresh.isRefreshing)
            binding.pullToRefresh.isRefreshing = false

        if (purchaseOrdersList.size > 0)
            purchaseOrdersList.removeAt(purchaseOrdersList.size - 1)

        purchaseOrdersList.addAll(data)

        if (data.size >= AppController.PAGINATION_COUNT) {
            skip += AppController.PAGINATION_COUNT
            purchaseOrdersList.add(null)
            canLoadMore = true
        }

        if (purchaseOrdersList.isEmpty()) {
            binding.recyclerFavorites.viewGone()
            binding.llNoData.viewVisible()
        } else {
            binding.llNoData.viewGone()
            binding.recyclerFavorites.viewVisible()
        }

        purchaseAdapter.notifyDataSetChanged()

    }


    private fun getPurchaseHistory() {
        apiCall = retrofitClient.getPurchaseOrders(type = "history", skip = skip)
        viewModel.getDraftProducts(apiCall)
    }


}
