package com.codingpixel.undiscoveredarchives.home.notifications

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.codingpixel.undiscoveredarchives.databinding.FragmentNotificationsBinding
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
import retrofit2.Call

class NotificationsFragment : Fragment() {

    private lateinit var binding: FragmentNotificationsBinding
    private lateinit var mContext: Context
    private lateinit var viewModel: HomeViewModel
    private lateinit var retrofitClient: Api
    private lateinit var apiCall: Call<ResponseBody>

    private var notificationsList: MutableList<NotificationsData> = ArrayList()
    private lateinit var notificationAdapter: NotificationsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false)
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

        getNotifications()
    }

    @SuppressLint("SetTextI18n")
    private fun initViews() {
        binding.topBar.tvScreenTitle.text = "Notifications"
    }

    private fun initListeners() {
        binding.pullToRefresh.setOnRefreshListener {
            binding.pullToRefresh.isRefreshing = false
            notificationsList.clear()
            getNotifications()
        }
    }

    private fun initVM() {
        viewModel = ViewModelProviders.of(this, ViewModelFactory()).get(HomeViewModel::class.java)
        binding.viewModel = viewModel
        binding.executePendingBindings()
        binding.lifecycleOwner = this
    }

    private fun initAdapter() {
        notificationAdapter = NotificationsAdapter(notificationsList)
        binding.notificationsRecyclerView.adapter = notificationAdapter
    }

    private fun observeApiResponse() {
        viewModel.getApiResponse().observe(requireActivity(), {
            when (it.status) {
                ApiStatus.LOADING -> {
                    Loader.showLoader((requireActivity() as AppCompatActivity)) {
                        if (this@NotificationsFragment::apiCall.isInitialized)
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
                        ApiTags.GET_NOTIFICATIONS -> {
                            val model = Gson().fromJson(it.data.toString(), NotificationsModel::class.java)
                            notificationsList.addAll(model.data)

                            if (notificationsList.isNullOrEmpty()) {
                                binding.llNoData.viewVisible()
                                binding.notificationsRecyclerView.viewGone()
                            } else {
                                binding.notificationsRecyclerView.viewVisible()
                                binding.llNoData.viewGone()
                                notificationAdapter.notifyDataSetChanged()
                            }
                        }
                    }
                }
            }
        })
    }

    private fun getNotifications() {
        apiCall = retrofitClient.getNotifications()
        viewModel.getNotifications(apiCall)
    }


}