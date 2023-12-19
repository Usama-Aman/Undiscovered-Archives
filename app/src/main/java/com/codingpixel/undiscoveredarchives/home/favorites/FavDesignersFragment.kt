package com.codingpixel.undiscoveredarchives.home.favorites

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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codingpixel.undiscoveredarchives.base.AppController
import com.codingpixel.undiscoveredarchives.databinding.TabFavoriteFragmentBinding
import com.codingpixel.undiscoveredarchives.home.designers_details.DesignersDetailsActivity
import com.codingpixel.undiscoveredarchives.home.favorites.adapters.FavoriteDesignAdapter
import com.codingpixel.undiscoveredarchives.home.favorites.models.*
import com.codingpixel.undiscoveredarchives.home.main.models.DesignersData
import com.codingpixel.undiscoveredarchives.home.main.models.DesignersModel
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
import com.google.gson.Gson
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call

class FavDesignersFragment : Fragment() {

    private lateinit var binding: TabFavoriteFragmentBinding
    private lateinit var mContext: Context
    private lateinit var viewModel: HomeViewModel
    private lateinit var retrofitClient: Api
    private lateinit var apiCall: Call<ResponseBody>

    private var designersList: MutableList<DesignersData?> = ArrayList()
    private lateinit var favoriteDesignAdapter: FavoriteDesignAdapter

    private var skip = 0
    private var canLoadMore = false
    private var unfavoriteDesignerPosition = -1
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

        getFavoriteDesigners()
    }

    private fun initViews() {
    }

    private fun initVM() {
        viewModel = ViewModelProviders.of(this, ViewModelFactory()).get(HomeViewModel::class.java)
        binding.viewModel = viewModel
        binding.executePendingBindings()
        binding.lifecycleOwner = this
    }

    private fun initListeners() {
        binding.pullToRefresh.setOnRefreshListener {
            Loader.showLoader((activity as AppCompatActivity)) {
                if (this@FavDesignersFragment::apiCall.isInitialized)
                    apiCall.cancel()
            }
            skip = 0
            designersList.clear()
            getFavoriteDesigners()
        }
    }

    private fun initAdapter() {
        favoriteDesignAdapter = FavoriteDesignAdapter(designersList,
            object : FavoriteDesignAdapter.FavoriteDesignersInterface {
                override fun onItemClicked(designerPosition: Int) {
                    val intent = Intent(mContext, DesignersDetailsActivity::class.java)
                    intent.putExtra("designerData", designersList[designerPosition])
                    mContext.startActivity(intent)
                }

                override fun onFavoriteClicked(designerPosition: Int) {
                    unfavoriteDesignerPosition = designerPosition
                    addDesignerToFavorite(designersList[designerPosition]?.id ?: -1)
                }
            })
        binding.recyclerFavorites.adapter = favoriteDesignAdapter

        binding.recyclerFavorites.addOnScrollListener(
            object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val lm = recyclerView.layoutManager as LinearLayoutManager
                    if (lm.findLastCompletelyVisibleItemPosition() == designersList.size - 1)
                        if (canLoadMore) {
                            canLoadMore = false
                            getFavoriteDesigners()
                        }

                }
            })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun observeApiResponse() {
        viewModel.getApiResponse().observe(requireActivity(), {
            when (it.status) {
                ApiStatus.LOADING -> {
                    if (it.tag != ApiTags.GET_FAVORITE_DESIGNER)
                        Loader.showLoader((requireActivity() as AppCompatActivity)) {
                            if (this@FavDesignersFragment::apiCall.isInitialized)
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
                        ApiTags.GET_FAVORITE_DESIGNER -> {
                            val model = Gson().fromJson(it.data.toString(), DesignersModel::class.java)
                            handleFavoriteProductsResponse(model.data)
                        }
                        ApiTags.ADD_DESIGNER_TO_FAVORITES -> {
                            val jsonObject = JSONObject(it.data.toString())
                            AppUtils.showToast(requireActivity(), jsonObject.getString("message"), true)

                            designersList.removeAt(unfavoriteProductPosition)
                            favoriteDesignAdapter.notifyDataSetChanged()
                        }
                    }
                }
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun handleFavoriteProductsResponse(data: List<DesignersData>) {
        if (binding.pullToRefresh.isRefreshing)
            binding.pullToRefresh.isRefreshing = false

        if (designersList.size > 0)
            designersList.removeAt(designersList.size - 1)

        designersList.addAll(data)

        if (data.size >= AppController.PAGINATION_COUNT) {
            skip += AppController.PAGINATION_COUNT
            designersList.add(null)
            canLoadMore = true
        }


        if (designersList.isEmpty()) {
            binding.recyclerFavorites.viewGone()
            binding.llNoData.viewVisible()
        } else {
            binding.llNoData.viewGone()
            binding.recyclerFavorites.viewVisible()
        }

        favoriteDesignAdapter.notifyDataSetChanged()

    }

    private fun getFavoriteDesigners() {
        apiCall = retrofitClient.getFavoriteDesigners()
        viewModel.getFavoriteDesigners(apiCall)
    }

    private fun addDesignerToFavorite(id: Int) {
        apiCall = retrofitClient.addDesignerProductsToFavorite(id)
        viewModel.addDesignerProductsToFavorite(apiCall)
    }


}
