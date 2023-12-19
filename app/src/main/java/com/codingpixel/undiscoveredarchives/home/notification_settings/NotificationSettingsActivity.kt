package com.codingpixel.undiscoveredarchives.home.notification_settings

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import com.codingpixel.undiscoveredarchives.auth.models.LoginModel
import com.codingpixel.undiscoveredarchives.auth.models.UserDetail
import com.codingpixel.undiscoveredarchives.base.BaseActivity
import com.codingpixel.undiscoveredarchives.databinding.ActivityNotificationSettingsBinding
import com.codingpixel.undiscoveredarchives.loader.Loader
import com.codingpixel.undiscoveredarchives.network.Api
import com.codingpixel.undiscoveredarchives.network.ApiStatus
import com.codingpixel.undiscoveredarchives.network.ApiTags
import com.codingpixel.undiscoveredarchives.network.RetrofitClient
import com.codingpixel.undiscoveredarchives.utils.AppUtils
import com.codingpixel.undiscoveredarchives.utils.viewVisible
import com.codingpixel.undiscoveredarchives.view_models.HomeViewModel
import com.codingpixel.undiscoveredarchives.view_models.ViewModelFactory
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Call

class NotificationSettingsActivity : BaseActivity() {

    private lateinit var binding: ActivityNotificationSettingsBinding
    private lateinit var mContext: Context
    private lateinit var viewModel: HomeViewModel
    private lateinit var retrofitClient: Api
    private lateinit var apiCall: Call<ResponseBody>
    private lateinit var notificationsData: UserDetail

    private var isAdminApproval = 0
    private var isOrderUpdates = 0
    private var isMessages = 0
    private var isPaymentUpdates = 0
    private var isProfileUpdates = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mContext = this
        retrofitClient = RetrofitClient.getClient(this@NotificationSettingsActivity).create(Api::class.java)

        blackStatusBarIcons()

        initViews()
        initVM()
        initListeners()
        observeApiResponse()

    }

    @SuppressLint("SetTextI18n")
    private fun initViews() {
        binding.topBar.ivBack.viewVisible()
        binding.topBar.tvScreenTitle.text = "Notifications Setting"
        binding.topBar.tvRightButton.viewVisible()
        binding.topBar.tvRightButton.text = "Save"

        notificationsData = AppUtils.getUserDetails(mContext).user_detail

        if (::notificationsData.isInitialized) {
            isAdminApproval = notificationsData.admin_approvals_alert
            isOrderUpdates = notificationsData.order_updates_alert
            isMessages = notificationsData.messages_alert
            isPaymentUpdates = notificationsData.payment_updates_alert
            isProfileUpdates = notificationsData.profile_updates_alert

            binding.adminApprovalSwitch.isChecked = isAdminApproval == 1
            binding.orderUpdatesSwitch.isChecked = isOrderUpdates == 1
            binding.messageFromBuyerSellerSwitch.isChecked = isMessages == 1
            binding.paymentUpdatesSwitch.isChecked = isPaymentUpdates == 1
            binding.profileUpdatesSwitch.isChecked = isProfileUpdates == 1
        }


        binding.adminApprovalSwitch.setOnCheckedChangeListener { _, isChecked ->
            isAdminApproval = if (isChecked) 1 else 0
        }
        binding.orderUpdatesSwitch.setOnCheckedChangeListener { _, isChecked ->
            isOrderUpdates = if (isChecked) 1 else 0
        }
        binding.messageFromBuyerSellerSwitch.setOnCheckedChangeListener { _, isChecked ->
            isMessages = if (isChecked) 1 else 0
        }
        binding.paymentUpdatesSwitch.setOnCheckedChangeListener { _, isChecked ->
            isPaymentUpdates = if (isChecked) 1 else 0
        }
        binding.profileUpdatesSwitch.setOnCheckedChangeListener { _, isChecked ->
            isProfileUpdates = if (isChecked) 1 else 0
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

        binding.topBar.tvRightButton.setOnClickListener {
            apiCall = retrofitClient.saveNotificationSettings(
                isAdminApproval,
                isOrderUpdates,
                isMessages,
                isPaymentUpdates,
                isProfileUpdates
            )
            viewModel.saveNotificationSettings(apiCall)
        }
    }

    private fun observeApiResponse() {
        viewModel.getApiResponse().observe(this, {
            when (it.status) {
                ApiStatus.LOADING -> {
                    Loader.showLoader(this) {
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
                        ApiTags.NOTIFICATION_SETTINGS -> {
                            val model = Gson().fromJson(it.data.toString(), LoginModel::class.java)
                            AppUtils.showToast(this, model.message, true)

                            val userData = AppUtils.getUserDetails(mContext)

                            if (userData?.user_detail == null)
                                userData.user_detail = UserDetail()

                            userData.user_detail.admin_approvals_alert = isAdminApproval
                            userData.user_detail.order_updates_alert = isOrderUpdates
                            userData.user_detail.messages_alert = isMessages
                            userData.user_detail.payment_updates_alert = isPaymentUpdates
                            userData.user_detail.profile_updates_alert = isProfileUpdates

                            AppUtils.saveUserModel(mContext, userData)
                        }
                    }
                }
            }
        })
    }


}
