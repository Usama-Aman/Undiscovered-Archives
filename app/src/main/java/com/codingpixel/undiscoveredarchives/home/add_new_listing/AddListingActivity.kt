package com.codingpixel.undiscoveredarchives.home.add_new_listing

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.astritveliu.boom.Boom
import com.codingpixel.undiscoveredarchives.R
import com.codingpixel.undiscoveredarchives.auth.models.*
import com.codingpixel.undiscoveredarchives.base.AppController.Companion.addEditUserProductData
import com.codingpixel.undiscoveredarchives.base.BaseActivity
import com.codingpixel.undiscoveredarchives.base.BaseActivityResult
import com.codingpixel.undiscoveredarchives.databinding.ActivityAddListing1Binding
import com.codingpixel.undiscoveredarchives.home.dialogs.AddVariantDialog
import com.codingpixel.undiscoveredarchives.home.favorites.models.UserProductData
import com.codingpixel.undiscoveredarchives.home.favorites.models.Variant
import com.codingpixel.undiscoveredarchives.home.searchable_spinner.SearchableSpinnerActivity
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
import java.util.*
import kotlin.collections.ArrayList


class AddListingActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivityAddListing1Binding
    private lateinit var mContext: Context
    private lateinit var viewModel: HomeViewModel
    private lateinit var retrofitClient: Api
    private lateinit var apiCall: Call<ResponseBody>

    companion object {
        var fromEdit = false
        lateinit var productGradients: ProductsGradientsData
        fun productGradientsInitialized() = ::productGradients.isInitialized
    }

    private var attributeList: MutableList<Attribute> = ArrayList()

    private var selectedCategoryId = -1
    private var selectedSubCategoryId = -1
    private var selectedDesignerId = -1
    var commissionedPrice = ""

    private var searchableList: ArrayList<SearchableSpinnerModel> = ArrayList()
    private lateinit var addVariantAdapter: AddVariantAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddListing1Binding.inflate(layoutInflater)
        setContentView(binding.root)
        mContext = this
        retrofitClient = RetrofitClient.getClient(this@AddListingActivity).create(Api::class.java)

        blackStatusBarIcons()

        initViews()
        initVM()

        initListeners()
        observeApiResponse()

        getProductsGradients()
        setEditTexts()
    }

    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    private fun initViews() {
        fromEdit = intent?.getBooleanExtra("fromEdit", false) ?: false
        if (!fromEdit)
            addEditUserProductData = UserProductData()

        binding.topBar.ivBack.viewVisible()
        binding.topBar.tvScreenTitle.text = "Add new listing"
    }


    private fun initVM() {
        viewModel = ViewModelProviders.of(this, ViewModelFactory()).get(HomeViewModel::class.java)
        binding.viewModel = viewModel
        binding.executePendingBindings()
        binding.lifecycleOwner = this
    }

    private fun initListeners() {
        binding.topBar.ivBack.setOnClickListener(this)
        binding.btnNext.setOnClickListener(this)
        binding.categorySpinner.setOnClickListener(this)
        binding.subCategorySpinner.setOnClickListener(this)
        binding.designerSpinner.setOnClickListener(this)
        binding.llAddVariant.setOnClickListener(this)

        Boom(binding.btnNext)
        Boom(binding.categorySpinner)
        Boom(binding.subCategorySpinner)
        Boom(binding.designerSpinner)
        Boom(binding.llAddVariant)

    }

    private fun initVariantsAdapter() {
        addVariantAdapter = AddVariantAdapter(
            addEditUserProductData?.variants!!,
            object : AddVariantAdapter.AddVariantInterface {
                @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
                override fun onRemoveClicked(position: Int) {
                    val sizeId = addEditUserProductData?.variants?.get(position)?.size_id
                    var isAlreadyExist = false

                    attributeList.forEach {
                        if (it.id == sizeId)
                            isAlreadyExist = true
                    }
                    if (!isAlreadyExist) {
                        attributeList.add(productGradients.attributes.filter { it.id == sizeId }[0])
                        attributeList.sortWith { l1, l2 ->
                            l1.id.compareTo(l2.id)
                        }
                    }

                    addEditUserProductData?.variants?.removeAt(position)
                    addVariantAdapter.notifyDataSetChanged()

                    if (addEditUserProductData?.variants?.isEmpty() == true)
                        binding.tvAddVariant.text = "Add Variant"

                }
            })
        binding.variantsRecyclerView.adapter = addVariantAdapter
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivBack -> {
                finish()
            }
            R.id.btnNext -> {
                addEditUserProductData?.category?.id = selectedCategoryId
                addEditUserProductData?.sub_category?.id = selectedSubCategoryId
                addEditUserProductData?.designer?.id = selectedDesignerId
                addEditUserProductData?.product_name =
                    binding.etProductName.text.toString()
                if (validate())
                    startActivity(Intent(this, AddListingActivity2::class.java))
            }
            R.id.categorySpinner -> {
                searchableList.clear()
                productGradients.categories.forEachIndexed { _, categoriesData ->
                    searchableList.add(
                        SearchableSpinnerModel(
                            categoriesData.id,
                            categoriesData.title,
                            categoriesData.id == selectedCategoryId
                        )
                    )
                }

                val intent = Intent(this, SearchableSpinnerActivity::class.java)
                intent.putParcelableArrayListExtra("listData", searchableList)
                intent.putExtra("title", "Select Category")
                activityLauncher.launch(
                    intent,
                    object : BaseActivityResult.OnActivityResult<ActivityResult> {
                        override fun onActivityResult(result: ActivityResult) {
                            if (result.resultCode == Activity.RESULT_OK) {
                                selectedCategoryId =
                                    result.data?.getIntExtra("selectedId", -1) ?: -1
                                binding.categorySpinner.text =
                                    result.data?.getStringExtra("selectedValue")
                            }
                        }
                    })
            }
            R.id.subCategorySpinner -> {
                searchableList.clear()
                productGradients.sub_categories.forEachIndexed { _, subCategoriesData ->
                    searchableList.add(
                        SearchableSpinnerModel(
                            subCategoriesData.id,
                            subCategoriesData.title,
                            subCategoriesData.id == selectedSubCategoryId
                        )
                    )
                }

                val intent = Intent(this, SearchableSpinnerActivity::class.java)
                intent.putParcelableArrayListExtra("listData", searchableList)
                intent.putExtra("title", "Select Sub Category")
                activityLauncher.launch(
                    intent,
                    object : BaseActivityResult.OnActivityResult<ActivityResult> {
                        override fun onActivityResult(result: ActivityResult) {
                            if (result.resultCode == Activity.RESULT_OK) {
                                selectedSubCategoryId =
                                    result.data?.getIntExtra("selectedId", -1) ?: -1
                                binding.subCategorySpinner.text =
                                    result.data?.getStringExtra("selectedValue")
                            }
                        }
                    })
            }
            R.id.designerSpinner -> {
                searchableList.clear()
                productGradients.designers.forEachIndexed { _, designersData ->
                    searchableList.add(
                        SearchableSpinnerModel(
                            designersData.id,
                            designersData.name,
                            designersData.id == selectedDesignerId
                        )
                    )
                }

                val intent = Intent(this, SearchableSpinnerActivity::class.java)
                intent.putParcelableArrayListExtra("listData", searchableList)
                intent.putExtra("title", "Select Designer")
                activityLauncher.launch(
                    intent,
                    object : BaseActivityResult.OnActivityResult<ActivityResult> {
                        override fun onActivityResult(result: ActivityResult) {
                            if (result.resultCode == Activity.RESULT_OK) {
                                selectedDesignerId =
                                    result.data?.getIntExtra("selectedId", -1) ?: -1
                                binding.designerSpinner.text =
                                    result.data?.getStringExtra("selectedValue")
                            }
                        }
                    })
            }
            R.id.llAddVariant -> {
                if (productGradientsInitialized()) {
                    if (selectedCategoryId != -1) {
                        binding.llAddVariant.setBackgroundResource(0)
                        binding.tilAddVariant.viewGone()

                        val addVariantDialog =
                            AddVariantDialog(
                                commissionedPrice,
                                attributeList.filter { it.category_id == selectedCategoryId },
                                object : AddVariantDialog.AddVariantInterface {
                                    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
                                    override fun onAddVariant(
                                        attributePosition: Int,
                                        noOfItems: String,
                                        pricePerItem: String,
                                        compareAtPrice: String,
                                        commissionedPrice: String,
                                        sellerEarnings: String
                                    ) {
                                        addEditUserProductData?.variants?.add(
                                            Variant(
                                                compareAtPrice, 0, // 0 is needed when updating variants
                                                pricePerItem, noOfItems.toInt(),
                                                attributeList[attributePosition].id,
                                                attributeList[attributePosition].value,
                                                -1,
                                                commissionedPrice, sellerEarnings
                                            )
                                        )

                                        //Remove the attribute that is selected one time
                                        attributeList.removeAt(attributePosition)

                                        addVariantAdapter.notifyDataSetChanged()
                                        binding.tvAddVariant.text = "Add Another Variant"

                                    }
                                })
                        addVariantDialog.show(supportFragmentManager, "AddVariantDialog")
                    } else {
                        binding.categorySpinner.setBackgroundResource(R.drawable.auth_edit_text_error)
                        binding.tilSelectCategory.viewVisible()
                        binding.tilSelectCategory.error = "Select Category First!"
                        binding.categorySpinner.requestFocus()
                    }
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    private fun observeApiResponse() {
        viewModel.getApiResponse().observe(this, {
            when (it.status) {
                ApiStatus.LOADING -> {
                    if (it.tag != ApiTags.GET_SETTINGS)
                        Loader.showLoader((this as AppCompatActivity)) {
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
                        ApiTags.GET_PRODUCT_GRADIENTS -> {
                            val model = Gson().fromJson(
                                it.data.toString(),
                                ProductsGradientsModel::class.java
                            )
                            productGradients = model.data
                            attributeList.addAll(productGradients.attributes)

                            getSettings()
                        }
                        ApiTags.GET_SETTINGS -> {
                            val model = Gson().fromJson(
                                it.data.toString(),
                                SettingsModel::class.java
                            )
                            commissionedPrice = model.data.value

                            if (fromEdit)
                                setUpProductData()
                            else
                                initVariantsAdapter()
                        }
                    }
                }
            }
        })
    }

    private fun setUpProductData() {
        //Category Id
        binding.categorySpinner.text =
            productGradients.categories.filter { it.id == addEditUserProductData?.category?.id ?: -1 }[0].title
        selectedCategoryId = productGradients.categories.filter { it.id == addEditUserProductData?.category?.id ?: -1 }[0].id

        //Subcategory
        binding.subCategorySpinner.text =
            productGradients.sub_categories.filter { it.id == addEditUserProductData?.sub_category?.id ?: -1 }[0].title
        selectedSubCategoryId =
            productGradients.sub_categories.filter { it.id == addEditUserProductData?.sub_category?.id ?: -1 }[0].id

        //Designer
        binding.designerSpinner.text =
            productGradients.designers.filter { it.id == addEditUserProductData?.designer?.id ?: -1 }[0].name
        selectedDesignerId = productGradients.designers.filter { it.id == addEditUserProductData?.designer?.id ?: -1 }[0].id

        //Product name
        binding.etProductName.setText(addEditUserProductData?.product_name ?: "")

        //Product variants
        if (addEditUserProductData?.variants != null) {
            for (i in addEditUserProductData?.variants?.indices!!) {
                if (addEditUserProductData?.variants?.get(i).toString().isNotEmpty())
                    if (addEditUserProductData?.variants?.get(i)?.price.toString().toDouble() > 0.0) {

                        if (attributeList.filter { it.id == addEditUserProductData?.variants?.get(i)?.size_id }.isNotEmpty()) {

                            addEditUserProductData?.variants?.get(i)?.sizeValue =
                                attributeList.filter { it.id == addEditUserProductData?.variants?.get(i)?.size_id }[0].value
                            addEditUserProductData?.variants?.get(i)?.commissioned_price = commissionedPrice

                            val price = addEditUserProductData?.variants?.get(i)?.price.toString().toDouble()
                            addEditUserProductData?.variants?.get(i)?.seller_earnings =
                                (price - (price * (commissionedPrice.toDouble() / 100))).toString()
                        }
                    }
            }
            initVariantsAdapter()
        }

    }


    private fun getProductsGradients() {
        apiCall = retrofitClient.getProductGradients()
        viewModel.getProductGradients(apiCall)
    }

    private fun getSettings() {
        apiCall = retrofitClient.getCommissionedAmount()
        viewModel.getSettings(apiCall)
    }

    override fun onDestroy() {
        super.onDestroy()
        addEditUserProductData = null
        fromEdit = false
    }

    private fun setEditTexts() {
        binding.categorySpinner.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.categorySpinner.setBackgroundResource(R.drawable.auth_edittext_drawable)
                binding.tilSelectCategory.viewGone()
                binding.tilSelectCategory.isErrorEnabled = false
            }
        })
        binding.subCategorySpinner.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.subCategorySpinner.setBackgroundResource(R.drawable.auth_edittext_drawable)
                binding.tilSelectSubCategory.viewGone()
                binding.tilSelectSubCategory.isErrorEnabled = false
            }
        })
        binding.designerSpinner.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.designerSpinner.setBackgroundResource(R.drawable.auth_edittext_drawable)
                binding.tilSelectDesigner.viewGone()
                binding.tilSelectDesigner.isErrorEnabled = false
            }
        })
        binding.etProductName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.etProductName.setBackgroundResource(R.drawable.auth_edittext_drawable)
                binding.tilProductName.error = null
                binding.tilProductName.isErrorEnabled = false
            }
        })
        if (selectedCategoryId != -1) {
            binding.llAddVariant.setBackgroundResource(0)
            binding.tilAddVariant.viewGone()
            binding.tilAddVariant.isErrorEnabled = false
        }
    }


    private fun validate(): Boolean {
        if (binding.categorySpinner.text.toString().isBlank()) {
            binding.categorySpinner.setBackgroundResource(R.drawable.auth_edit_text_error)
            binding.tilSelectCategory.viewVisible()
            binding.tilSelectCategory.error = "Select Category!"
            binding.categorySpinner.requestFocus()
            return false
        }
        if (binding.subCategorySpinner.text.toString().isBlank()) {
            binding.subCategorySpinner.setBackgroundResource(R.drawable.auth_edit_text_error)
            binding.tilSelectSubCategory.viewVisible()
            binding.tilSelectSubCategory.error = "Select Sub Category!"
            binding.subCategorySpinner.requestFocus()
            return false
        }
        if (binding.designerSpinner.text.toString().isBlank()) {
            binding.designerSpinner.setBackgroundResource(R.drawable.auth_edit_text_error)
            binding.tilSelectDesigner.viewVisible()
            binding.tilSelectDesigner.error = "Select Designer!"
            binding.designerSpinner.requestFocus()
            return false
        }
        if (binding.etProductName.text.toString().isBlank()) {
            binding.etProductName.setBackgroundResource(R.drawable.auth_edit_text_error)
            binding.tilProductName.viewVisible()
            binding.tilProductName.error = "Enter Product Name!"
            binding.etProductName.requestFocus()
            return false
        }

        if (addEditUserProductData?.variants.isNullOrEmpty()) {
            binding.llAddVariant.setBackgroundResource(R.drawable.auth_edit_text_error)
            binding.tilAddVariant.viewVisible()
            binding.tilAddVariant.error = "Add Variant!"
            binding.llAddVariant.requestFocus()
            return false
        }
        return true
    }

}