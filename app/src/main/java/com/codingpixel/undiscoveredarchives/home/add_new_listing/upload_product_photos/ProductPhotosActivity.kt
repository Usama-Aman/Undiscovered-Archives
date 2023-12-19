package com.codingpixel.undiscoveredarchives.home.add_new_listing.upload_product_photos

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.webkit.URLUtil
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import com.astritveliu.boom.Boom
import com.codingpixel.undiscoveredarchives.R
import com.codingpixel.undiscoveredarchives.base.AppController.Companion.addEditUserProductData
import com.codingpixel.undiscoveredarchives.base.BaseActivity
import com.codingpixel.undiscoveredarchives.databinding.ActivityProductPhotosBinding
import com.codingpixel.undiscoveredarchives.home.add_new_listing.AddListingActivity.Companion.fromEdit
import com.codingpixel.undiscoveredarchives.home.dialogs.CancelListing
import com.codingpixel.undiscoveredarchives.home.dialogs.ProductApprovalPending
import com.codingpixel.undiscoveredarchives.home.dialogs.SaveDraft
import com.codingpixel.undiscoveredarchives.home.favorites.models.File
import com.codingpixel.undiscoveredarchives.home.main.HomeActivity
import com.codingpixel.undiscoveredarchives.loader.Loader
import com.codingpixel.undiscoveredarchives.network.Api
import com.codingpixel.undiscoveredarchives.network.ApiStatus
import com.codingpixel.undiscoveredarchives.network.ApiTags
import com.codingpixel.undiscoveredarchives.network.RetrofitClient
import com.codingpixel.undiscoveredarchives.utils.AppUtils
import com.codingpixel.undiscoveredarchives.utils.Constants.ProductCountForUpload
import com.codingpixel.undiscoveredarchives.utils.photo_util.PhotoUtil
import com.codingpixel.undiscoveredarchives.utils.viewGone
import com.codingpixel.undiscoveredarchives.utils.viewVisible
import com.codingpixel.undiscoveredarchives.view_models.HomeViewModel
import io.ak1.pix.helpers.PixEventCallback
import io.ak1.pix.helpers.addPixToActivity
import io.ak1.pix.models.Flash
import io.ak1.pix.models.Mode
import io.ak1.pix.models.Options
import io.ak1.pix.models.Ratio
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import java.util.*
import kotlin.collections.ArrayList


class ProductPhotosActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivityProductPhotosBinding
    private lateinit var viewModel: HomeViewModel
    private lateinit var retrofitClient: Api
    private lateinit var apiCall: Call<ResponseBody>

    private var editPhotoPosition = -1 // -1 in case if added
    private var isPixShowing = false

    private var productPhotoList: MutableList<ProductPhotoModel> = ArrayList()
    private lateinit var productPhotoAdapter: ProductPhotoAdapter

    private var isDraftProduct = false

    companion object {
        const val DRAFT_TYPE_PRODUCT = "Draft"
        const val PUBLISHED_TYPE_PRODUCT = "Published"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductPhotosBinding.inflate(layoutInflater)
        setContentView(binding.root)
        retrofitClient = RetrofitClient.getClient(this@ProductPhotosActivity).create(Api::class.java)
        blackStatusBarIcons()

        initViews()
        initVM()
        initListeners()
        initAdapter()
        observeApiResponse()
    }

    @SuppressLint("SetTextI18n")
    private fun initViews() {
        binding.topBar.tvScreenTitle.text = "Add New Listing"
        binding.topBar.ivBack.viewVisible()
    }

    private fun initVM() {
        viewModel = ViewModelProviders.of(this).get(HomeViewModel::class.java)
        binding.viewModel = viewModel
        binding.executePendingBindings()
        binding.lifecycleOwner = this
    }

    private fun initListeners() {
        binding.topBar.ivBack.setOnClickListener(this)
        binding.btnPublish.setOnClickListener(this)
        binding.btnSaveDrafts.setOnClickListener(this)
        binding.btnCancel.setOnClickListener(this)

        Boom(binding.btnPublish)
        Boom(binding.btnSaveDrafts)
        Boom(binding.btnCancel)
    }

    private fun initAdapter() {

        if (fromEdit) {
            if (!addEditUserProductData?.files.isNullOrEmpty()) {
                productPhotoList.clear()
                addEditUserProductData?.files?.forEach {
                    productPhotoList.add(
                        ProductPhotoModel(
                            it.id,
                            it.file_path,
                            isSelected = false,
                            canAddPhoto = false
                        )
                    )
                }

                if (productPhotoList.size < ProductCountForUpload)
                    productPhotoList.add(ProductPhotoModel(-1, "", isSelected = false, canAddPhoto = true))
            }
        } else {
            productPhotoList.clear()
            productPhotoList.add(ProductPhotoModel(-1, "", isSelected = false, canAddPhoto = true))
        }

        productPhotoAdapter = ProductPhotoAdapter(productPhotoList,
            object : ProductPhotoAdapter.ProductPhotoInterface {
                override fun addNewPhoto() {
                    editPhotoPosition = -1
                    showImagePicker()
                }

                override fun editPreviousPhoto(position: Int) {

                    if (URLUtil.isValidUrl(productPhotoList[position].path))
                        deleteProductFiles(productPhotoList[position].id)

                    editPhotoPosition = position
                    showImagePicker()
                }

                @SuppressLint("NotifyDataSetChanged")
                override fun deleteProductPhoto(position: Int) {
                    if (URLUtil.isValidUrl(productPhotoList[position].path))
                        deleteProductFiles(productPhotoList[position].id)

                    productPhotoList.removeAt(position)

                    if (productPhotoList.none { it.canAddPhoto })
                        productPhotoList.add(ProductPhotoModel(-1, "", isSelected = false, canAddPhoto = true))

                    productPhotoAdapter.notifyDataSetChanged()
                }

            })
        binding.photosRecyclerView.adapter = productPhotoAdapter
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivBack -> {
                finish()
            }
            R.id.btnPublish -> {
                createProducts(PUBLISHED_TYPE_PRODUCT)
            }
            R.id.btnSaveDrafts -> {
                isDraftProduct = true
                createProducts(DRAFT_TYPE_PRODUCT)
            }
            R.id.btnCancel -> {
                val cancelListingDialog = CancelListing(object : CancelListing.CancelListingInterface {
                    override fun onCancel() {
                        gotoProfile()
                    }

                    override fun saveAsDraft() {
                        isDraftProduct = true
                        createProducts(DRAFT_TYPE_PRODUCT)
                    }
                })
                cancelListingDialog.show(supportFragmentManager, "CancelListingDialog")
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showImagePicker() {

        val countt = if (productPhotoList[productPhotoList.size - 1].canAddPhoto) {
            ProductCountForUpload - (productPhotoList.size - 1)
        } else
            ProductCountForUpload - productPhotoList.size

        isPixShowing = true
        binding.container.viewVisible()

        val options = Options().apply {
            ratio = Ratio.RATIO_AUTO                        //Image/video capture ratio
            count = countt                                       //Number of images to restrict selection count
            spanCount = 4                                   //Number for columns in grid
            path = "Pix/Camera"                             //Custom Path For media Storage
            isFrontFacing = false                           //Front Facing camera on start
            mode = Mode.Picture                             //Option to select only pictures or videos or both
            flash = Flash.Auto                              //Option to select flash type
        }

        addPixToActivity(R.id.container, options) {
            when (it.status) {
                PixEventCallback.Status.SUCCESS -> {
                    onBackPressed()

                    val mPath = it.data

                    if (!mPath.isNullOrEmpty())
                        if (mPath.isNotEmpty()) {
                            if (editPhotoPosition == -1) {
                                if (productPhotoList[productPhotoList.size - 1].canAddPhoto)
                                    productPhotoList.removeAt(productPhotoList.size - 1)

                                mPath.forEachIndexed { _, s ->
                                    productPhotoList.add(
                                        ProductPhotoModel(
                                            -1, PhotoUtil.getPath(this, s) ?: "", isSelected = false, canAddPhoto = false
                                        )
                                    )
                                }
                                if (productPhotoList.size < ProductCountForUpload) {
                                    productPhotoList.add(ProductPhotoModel(-1, "", isSelected = false, canAddPhoto = true))
                                }
                            } else {
                                productPhotoList.removeAt(editPhotoPosition)
                                productPhotoList.add(
                                    editPhotoPosition, ProductPhotoModel(
                                        -1, PhotoUtil.getPath(this, mPath[0]) ?: "", isSelected = false, canAddPhoto = false
                                    )
                                )
                            }
                            productPhotoAdapter.notifyDataSetChanged()
                        }


                }
                PixEventCallback.Status.BACK_PRESSED -> {
                    Toast.makeText(this@ProductPhotosActivity, "Back", Toast.LENGTH_SHORT).show()
                    supportFragmentManager.popBackStack()
                }
            }
        }
    }

    private fun observeApiResponse() {
        viewModel.getApiResponse().observe(this, {
            when (it.status) {
                ApiStatus.LOADING -> {
                    if (it.tag != ApiTags.GET_CITIES && it.tag != ApiTags.GET_STATES
                        && it.tag != ApiTags.GET_PROFILE
                    )
                        Loader.showLoader(this) {
                            if (this@ProductPhotosActivity::apiCall.isInitialized)
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
                        ApiTags.CREATE_PRODUCTS -> {
                            if (isDraftProduct) {
                                val draftDialog = SaveDraft(object : SaveDraft.SavedAsDraftInterface {
                                    override fun onDone() {
                                        gotoProfile()
                                    }
                                })
                                draftDialog.show(supportFragmentManager, "SavedAsDraftDialog")
                            } else {
                                val productApprovalPending =
                                    ProductApprovalPending(object : ProductApprovalPending.ProductApprovalInterface {
                                        override fun onDone() {
                                            gotoProfile()
                                        }
                                    })
                                productApprovalPending.show(supportFragmentManager, "ProductApprovalPending")
                            }
                        }
                    }
                }
            }
        })
    }

    private fun deleteProductFiles(id: Int) {
        apiCall = retrofitClient.deleteProductFiles(id)
        viewModel.deleteProductFiles(apiCall)
    }

    private fun createProducts(status: String) {
        addEditUserProductData?.files?.clear()
        for (i in productPhotoList.indices)
            if (!productPhotoList[i].canAddPhoto)
                addEditUserProductData?.files?.add(
                    File(
                        productPhotoList[i].path.substring(productPhotoList[i].path.lastIndexOf("/") + 1),
                        productPhotoList[i].path,
                        -1, -1
                    )
                )

        val multipartBodiesList: MutableList<MultipartBody.Part> = ArrayList()

        val variantsArray = JSONArray()
        var variantsObject: JSONObject
        if (!addEditUserProductData?.variants.isNullOrEmpty())
            for (i in addEditUserProductData?.variants?.indices!!) {
                variantsObject = JSONObject()
                variantsObject.put("id", addEditUserProductData?.variants?.get(i)?.id)
                variantsObject.put("size_id", addEditUserProductData?.variants?.get(i)?.size_id)
                variantsObject.put("price", addEditUserProductData?.variants?.get(i)?.price)
                variantsObject.put("compare_at_price", addEditUserProductData?.variants?.get(i)?.compare_at_price)
                variantsObject.put("quantity", addEditUserProductData?.variants?.get(i)?.quantity)

                variantsArray.put(variantsObject)
            }
        val zonesArray = JSONArray()
        var zonesObject: JSONObject
        if (!addEditUserProductData?.zones.isNullOrEmpty())
            for (i in addEditUserProductData?.zones?.indices!!) {
                zonesObject = JSONObject()
                zonesObject.put("zone_id", addEditUserProductData?.zones?.get(i)?.id)
                zonesObject.put(
                    "shipping_charges",
                    addEditUserProductData?.zones?.get(i)?.pivot?.shipping_charges
                )

                zonesArray.put(zonesObject)
            }
        if (!addEditUserProductData?.files.isNullOrEmpty())
            for (i in addEditUserProductData?.files?.indices!!) {
                if (!URLUtil.isValidUrl(addEditUserProductData?.files?.get(i)?.file_path)) {
                    val file = java.io.File(addEditUserProductData?.files?.get(i)?.file_path!!)
                    multipartBodiesList.add(
                        MultipartBody.Part.createFormData(
                            "files[]", file.name, file.asRequestBody("image/*".toMediaTypeOrNull())
                        )
                    )
                }
            }
        val multipartBodiesArray: Array<MultipartBody.Part> = multipartBodiesList.toTypedArray()

        apiCall =
            if (!fromEdit)
                retrofitClient.createProduct(
                    status.toRequestBody("text/plain".toMediaTypeOrNull()),
                    addEditUserProductData?.category?.id.toString()
                        .toRequestBody("text/plain".toMediaTypeOrNull()),
                    addEditUserProductData?.sub_category?.id.toString()
                        .toRequestBody("text/plain".toMediaTypeOrNull()),
                    addEditUserProductData?.designer?.id.toString()
                        .toRequestBody("text/plain".toMediaTypeOrNull()),
                    addEditUserProductData?.product_name.toString()
                        .toRequestBody("text/plain".toMediaTypeOrNull()),
                    variantsArray.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                    addEditUserProductData?.condition.toString()
                        .toRequestBody("text/plain".toMediaTypeOrNull()),
                    addEditUserProductData?.currency?.id.toString()
                        .toRequestBody("text/plain".toMediaTypeOrNull()),
                    addEditUserProductData?.product_description.toString()
                        .toRequestBody("text/plain".toMediaTypeOrNull()),
                    addEditUserProductData?.measurements.toString()
                        .toRequestBody("text/plain".toMediaTypeOrNull()),
                    zonesArray.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                    multipartBodiesArray
                )
            else
                retrofitClient.updateProduct(
                    addEditUserProductData?.id ?: -1,
                    status.toRequestBody("text/plain".toMediaTypeOrNull()),
                    addEditUserProductData?.category?.id.toString()
                        .toRequestBody("text/plain".toMediaTypeOrNull()),
                    addEditUserProductData?.sub_category?.id.toString()
                        .toRequestBody("text/plain".toMediaTypeOrNull()),
                    addEditUserProductData?.designer?.id.toString()
                        .toRequestBody("text/plain".toMediaTypeOrNull()),
                    addEditUserProductData?.product_name.toString()
                        .toRequestBody("text/plain".toMediaTypeOrNull()),
                    variantsArray.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                    addEditUserProductData?.condition.toString()
                        .toRequestBody("text/plain".toMediaTypeOrNull()),
                    addEditUserProductData?.currency?.id.toString()
                        .toRequestBody("text/plain".toMediaTypeOrNull()),
                    addEditUserProductData?.product_description.toString()
                        .toRequestBody("text/plain".toMediaTypeOrNull()),
                    addEditUserProductData?.measurements.toString()
                        .toRequestBody("text/plain".toMediaTypeOrNull()),
                    zonesArray.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                    multipartBodiesArray
                )

        viewModel.createProducts(apiCall)
    }

    override fun onBackPressed() {
        if (isPixShowing) {
            isPixShowing = false
            binding.container.viewGone()
        } else
            super.onBackPressed()
    }

    private fun gotoProfile() {
        addEditUserProductData = null
        fromEdit = false
        val i = Intent(this, HomeActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        i.putExtra("goToProfile", true)
        startActivity(i)
        finish()
    }
}
