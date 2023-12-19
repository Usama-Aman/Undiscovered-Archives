package com.codingpixel.undiscoveredarchives.home.designers

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.lifecycle.ViewModelProviders
import com.codingpixel.undiscoveredarchives.R
import com.codingpixel.undiscoveredarchives.base.BaseActivity
import com.codingpixel.undiscoveredarchives.databinding.ActivityDesignersBinding
import com.codingpixel.undiscoveredarchives.home.designers_details.DesignersDetailsActivity
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
import com.faltenreich.skeletonlayout.Skeleton
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Call

class DesignersActivity : BaseActivity() {

    private lateinit var binding: ActivityDesignersBinding
    private lateinit var mContext: Context
    private lateinit var viewModel: HomeViewModel
    private lateinit var retrofitClient: Api
    private lateinit var apiCall: Call<ResponseBody>

    private lateinit var listingAdapter: DesignersListingAdapter
    private lateinit var sortingAdapter: DesignersSortingAdapter
    private var designersList: MutableList<DesignersData> = ArrayList()
    private var designersSortingList: MutableList<DesignersData> = ArrayList()

    private lateinit var skeletonLayout: Skeleton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDesignersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        blackStatusBarIcons()
        mContext = this
        retrofitClient = RetrofitClient.getClient(this@DesignersActivity).create(Api::class.java)

        initViews()
        initVM()
        initListeners()
        initAdapters()
        observeApiResponse()

        getUserDesigners()
    }

    @SuppressLint("SetTextI18n")
    private fun initViews() {
        skeletonLayout = binding.skeletonLayout
        binding.topBar.ivBack.viewVisible()
        binding.topBar.tvScreenTitle.text = "Designers"

        //filter Spinner
        ArrayAdapter.createFromResource(
            mContext, R.array.filter_list_designer, android.R.layout.simple_spinner_item
        )
            .also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.filter.adapter = adapter
            }
    }

    private fun initVM() {
        viewModel = ViewModelProviders.of(this, ViewModelFactory()).get(HomeViewModel::class.java)
        binding.viewModel = viewModel
        binding.executePendingBindings()
        binding.lifecycleOwner = this
    }

    private fun initListeners() {
        binding.topBar.ivBack.setOnClickListener {
            finish()
        }
    }

    private fun initAdapters() {
        listingAdapter = DesignersListingAdapter(designersList, object : DesignersListingAdapter.DesignerListingInterface {
            override fun onItemClicked(position: Int) {
                val intent = Intent(this@DesignersActivity, DesignersDetailsActivity::class.java)
                intent.putExtra("designerData", designersList[position])
                startActivity(intent)
            }
        })
        binding.rvDesigners.adapter = listingAdapter

        sortingAdapter = DesignersSortingAdapter(designersSortingList, object : DesignersSortingAdapter.DesignerSortingInterface {
            override fun onItemClicked(position: Int) {
                val intent = Intent(this@DesignersActivity, DesignersDetailsActivity::class.java)
                intent.putExtra("designerData", designersSortingList[position])
                startActivity(intent)
            }
        })
        binding.rvDesignerListingWithSorting.adapter = sortingAdapter
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun observeApiResponse() {
        viewModel.getApiResponse().observe(this, {
            when (it.status) {
                ApiStatus.LOADING -> {
//                    Loader.showLoader((this as AppCompatActivity)) {
//                        if (this::apiCall.isInitialized)
//                            apiCall.cancel()
//                    }
                }
                ApiStatus.ERROR -> {
                    Loader.hideLoader()
                    AppUtils.showToast(this, it.message!!, false)
                }
                ApiStatus.SUCCESS -> {
//                    Loader.hideLoader()
                    when (it.tag) {
                        ApiTags.GET_ALL_DESIGNERS -> {
                            val model = Gson().fromJson(it.data.toString(), DesignersModel::class.java)

                            if (!isPanelShown(binding.designersScrollView)) {
                                binding.designersScrollView.viewVisible()
                                binding.skeletonLayout.viewGone()
                                skeletonLayout.showOriginal()
                            }

                            designersList.addAll(model.data)
                            listingAdapter.notifyDataSetChanged()

                            designersSortingList.clear()

                            val alphabetList = ArrayList<String>()
                            for (i in designersList.indices) {
                                if (!alphabetList.contains(designersList[i].name[0].toString()))
                                    alphabetList.add(designersList[i].name[0].toString())
                            }

                            for (i in alphabetList.indices) {
                                designersSortingList.add(
                                    DesignersData(
                                        "", -1, -1, "", "",
                                        alphabetList[i], -1, -1, true
                                    )
                                )
                                for (j in designersList.indices) {
                                    if (alphabetList[i] == designersList[j].name[0].toString())
                                        designersSortingList.add(designersList[j])
                                }
                            }

                            sortingAdapter.notifyDataSetChanged()
                        }
                    }
                }
            }
        })
    }


    private fun getUserDesigners() {
        binding.designersScrollView.viewGone()
        binding.skeletonLayout.viewVisible()
        skeletonLayout.showSkeleton()

        apiCall = retrofitClient.getAllDesigners()
        viewModel.getAllDesigners(apiCall)
    }
}