package com.codingpixel.undiscoveredarchives.home.profile

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.astritveliu.boom.Boom
import com.bumptech.glide.Glide
import com.codingpixel.undiscoveredarchives.R
import com.codingpixel.undiscoveredarchives.auth.SignInActivity
import com.codingpixel.undiscoveredarchives.auth.models.LoginModel
import com.codingpixel.undiscoveredarchives.auth.models.UserDetailData
import com.codingpixel.undiscoveredarchives.databinding.FragmentProfileBinding
import com.codingpixel.undiscoveredarchives.home.add_new_listing.AddListingActivity
import com.codingpixel.undiscoveredarchives.home.become_seller.BecomeSellerActivity
import com.codingpixel.undiscoveredarchives.home.customer_service.CustomerServiceActivity
import com.codingpixel.undiscoveredarchives.home.dialogs.AccountPending
import com.codingpixel.undiscoveredarchives.home.draft_products.DraftProductsActivity
import com.codingpixel.undiscoveredarchives.home.edit_profile.EditProfileActivity
import com.codingpixel.undiscoveredarchives.home.manage_paypal.ManagePaypalActivity
import com.codingpixel.undiscoveredarchives.home.messages.AllMessagesActivity
import com.codingpixel.undiscoveredarchives.home.notification_settings.NotificationSettingsActivity
import com.codingpixel.undiscoveredarchives.home.payment_products.PaymentProductsActivity
import com.codingpixel.undiscoveredarchives.home.published_products.PublishedProductsActivity
import com.codingpixel.undiscoveredarchives.home.purchases.PurchasesActivity
import com.codingpixel.undiscoveredarchives.home.sales.SalesActivity
import com.codingpixel.undiscoveredarchives.home.shipping_products.ShippingProductsActivity
import com.codingpixel.undiscoveredarchives.home.trash_products.TrashProductsActivity
import com.codingpixel.undiscoveredarchives.loader.Loader
import com.codingpixel.undiscoveredarchives.network.Api
import com.codingpixel.undiscoveredarchives.network.ApiStatus
import com.codingpixel.undiscoveredarchives.network.ApiTags
import com.codingpixel.undiscoveredarchives.network.RetrofitClient
import com.codingpixel.undiscoveredarchives.utils.*
import com.codingpixel.undiscoveredarchives.view_models.HomeViewModel
import com.codingpixel.undiscoveredarchives.view_models.ViewModelFactory
import com.facebook.AccessToken
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Call


class ProfileFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var mContext: Context
    private lateinit var viewModel: HomeViewModel
    private lateinit var retrofitClient: Api
    private lateinit var apiCall: Call<ResponseBody>
    private lateinit var userDataModel: UserDetailData

    private var googleAccount: GoogleSignInAccount? = null
    private var facebookAccessToken: AccessToken? = null

    companion object {
        const val profileApproved = "Approved"
        const val profilePending = "Pending"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        mContext = requireContext()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        retrofitClient = RetrofitClient.getClient(requireContext()).create(Api::class.java)

        initViews()
        initVM()
        initListeners()
        observeApiResponse()

        getProfile()
    }

    @SuppressLint("SetTextI18n")
    private fun initViews() {
        /*To check whether is already sign in or not do it in splash*/
        googleAccount = GoogleSignIn.getLastSignedInAccount(mContext)
        facebookAccessToken = AccessToken.getCurrentAccessToken()

        binding.topBar.tvScreenTitle.text = "My Account"
        binding.topBar.ivRightButton.viewVisible()
        binding.topBar.ivRightButton.setImageResource(R.drawable.ic_share_profile)
    }


    private fun initVM() {
        viewModel = ViewModelProviders.of(this, ViewModelFactory()).get(HomeViewModel::class.java)
        binding.viewModel = viewModel
        binding.executePendingBindings()
        binding.lifecycleOwner = this
    }

    private fun initListeners() {
        Boom(binding.btnEditProfile)
        Boom(binding.btnPaypalAccounts)
        Boom(binding.btnShipping)
        Boom(binding.btnPaypalAccounts)
        Boom(binding.btnCustomerService)
        Boom(binding.btnPublishedProducts)
        Boom(binding.btnDrafts)
        Boom(binding.btnTrash)
        Boom(binding.btnPurchases)
        Boom(binding.btnSales)
        Boom(binding.btnSeeChats)
        Boom(binding.btnPayments)
        Boom(binding.btnNotificationsSettings)
        Boom(binding.btnSellerUpdates)
        Boom(binding.btnLogout)


        binding.btnSellerUpdates.setOnClickListener(this)
        binding.btnEditProfile.setOnClickListener(this)
        binding.btnPaypalAccounts.setOnClickListener(this)
        binding.btnShipping.setOnClickListener(this)
        binding.btnPaypalAccounts.setOnClickListener(this)
        binding.btnCustomerService.setOnClickListener(this)
        binding.btnPublishedProducts.setOnClickListener(this)
        binding.btnDrafts.setOnClickListener(this)
        binding.btnTrash.setOnClickListener(this)
        binding.btnPurchases.setOnClickListener(this)
        binding.btnSales.setOnClickListener(this)
        binding.btnSeeChats.setOnClickListener(this)
        binding.btnPayments.setOnClickListener(this)
        binding.btnNotificationsSettings.setOnClickListener(this)
        binding.topBar.ivRightButton.setOnClickListener(this)
        binding.btnLogout.setOnClickListener(this)

        binding.pullToRefresh.setOnRefreshListener {
            binding.pullToRefresh.isRefreshing = false
            getProfile()
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnPublishedProducts -> {
                startActivity(Intent(mContext, PublishedProductsActivity::class.java))
            }
            R.id.btnDrafts -> {
                startActivity(Intent(mContext, DraftProductsActivity::class.java))
            }
            R.id.btnTrash -> {
                startActivity(Intent(mContext, TrashProductsActivity::class.java))
            }
            R.id.btnPurchases -> {
                startActivity(Intent(mContext, PurchasesActivity::class.java))
            }
            R.id.btnSales -> {
                startActivity(Intent(mContext, SalesActivity::class.java))
            }
            R.id.btnSeeChats -> {
                startActivity(Intent(mContext, AllMessagesActivity::class.java))
            }
            R.id.btnPayments -> {
                startActivity(Intent(mContext, PaymentProductsActivity::class.java))
            }
            R.id.btnNotificationsSettings -> {
                startActivity(Intent(mContext, NotificationSettingsActivity::class.java))
            }
            R.id.btnShipping -> {
                mContext.startActivity(Intent(mContext, ShippingProductsActivity::class.java))
            }
            R.id.btnCustomerService -> {
                mContext.startActivity(Intent(mContext, CustomerServiceActivity::class.java))
            }
            R.id.btnPaypalAccounts -> {
                mContext.startActivity(Intent(mContext, ManagePaypalActivity::class.java))
            }
            R.id.btnEditProfile -> {
                mContext.startActivity(Intent(mContext, EditProfileActivity::class.java))
            }
            R.id.btnSellerUpdates -> {
                if (::userDataModel.isInitialized)
                    if (userDataModel.is_vendor == 1) {
                        if (userDataModel.approval_status == profileApproved)
                            mContext.startActivity(Intent(mContext, AddListingActivity::class.java))
                        else
                            AccountPending(object : AccountPending.AccountPendingInterface {
                                override fun onDone() {

                                }
                            }).show(activity?.supportFragmentManager!!, "AccountPendingDialog")
                    } else {
                        mContext.startActivity(Intent(mContext, BecomeSellerActivity::class.java))
                    }
            }
            R.id.ivRightButton -> {
                createCode()
            }
            R.id.btnLogout -> {

                if (googleAccount != null)
                    if (!googleAccount!!.isExpired) {
                        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
                        val googleSignInClient = GoogleSignIn.getClient(mContext, gso)
                        googleSignInClient.signOut()
                    }

                if (facebookAccessToken != null) {
                    if (!facebookAccessToken!!.isExpired) {
                        LoginManager.getInstance().logOut()
                    }
                }

                logout()
            }
        }
    }

    private fun createCode() {
        if (::userDataModel.isInitialized) {
            val builder: Uri.Builder = Uri.Builder()
            builder.scheme("http")
                .authority("www.undiscovered-archives.com")
                .appendPath(Constants.referralCode)
                .appendQueryParameter(Constants.referralCode, userDataModel.id.toString())
            val myUrl: String = builder.build().toString()


            val intent = Intent(Intent.ACTION_SEND)
            val shareBody = "Here is the link to Undiscovered Archives app \n\n$myUrl"
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
            intent.putExtra(Intent.EXTRA_TEXT, shareBody)
            startActivity(Intent.createChooser(intent, "Please select one option"))
        }
    }

    private fun observeApiResponse() {
        viewModel.getApiResponse().observe(requireActivity(), {
            when (it.status) {
                ApiStatus.LOADING -> {
                    Loader.showLoader((requireActivity() as AppCompatActivity)) {
                        if (this@ProfileFragment::apiCall.isInitialized)
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
                        ApiTags.GET_PROFILE -> {
                            val model = Gson().fromJson(it.data.toString(), LoginModel::class.java)
                            userDataModel = model.data

                            AppUtils.saveUserModel(mContext, userDataModel)
                            setProfileData()
                        }
                        ApiTags.LOGOUT -> {
                            SharedPreference.saveBoolean(mContext, Constants.isUserLoggedIn, false)
                            SharedPreference.saveString(mContext, Constants.accessToken, "")

                            val intent = Intent(mContext, SignInActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }
                    }
                }
            }
        })
    }

    private fun getProfile() {
        apiCall = retrofitClient.getProfile()
        viewModel.getProfile(apiCall)
    }

    @SuppressLint("SetTextI18n")
    private fun setProfileData() {
        Glide.with(mContext)
            .load(userDataModel.photo_path)
            .placeholder(R.drawable.ic_profile_placeholder)
            .into(binding.ivProfileImage)

        binding.tvProfileName.text = userDataModel.name
        binding.tvListingCount.text = "${userDataModel.listings_count}"
        binding.tvTransactionCount.text = "${userDataModel.transactions_count}"

        if (userDataModel.is_vendor == 1) {
            binding.btnPayments.viewVisible()
            binding.btnShipping.viewVisible()
            binding.btnSellerUpdates.text = "Start Selling"
        } else {
            binding.btnPayments.viewGone()
            binding.btnShipping.viewGone()
            binding.btnSellerUpdates.text = "Become a seller"
        }

    }

    override fun onResume() {
        super.onResume()

        Glide.with(mContext)
            .load(AppUtils.getUserDetails(mContext).photo_path)
            .placeholder(R.drawable.ic_profile_placeholder)
            .into(binding.ivProfileImage)

    }

    @SuppressLint("HardwareIds")
    private fun logout() {
        val deviceId = Settings.Secure.getString(activity?.contentResolver, Settings.Secure.ANDROID_ID)

        apiCall = retrofitClient.logout(deviceId)
        viewModel.logout(apiCall)
    }

}
