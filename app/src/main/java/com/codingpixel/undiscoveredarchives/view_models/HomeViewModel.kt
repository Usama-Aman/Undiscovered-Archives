package com.codingpixel.undiscoveredarchives.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codingpixel.undiscoveredarchives.network.ApiTags
import com.codingpixel.undiscoveredarchives.network.Resource
import com.codingpixel.undiscoveredarchives.network.ResponseCallBack
import com.codingpixel.undiscoveredarchives.network.RetrofitClient
import com.codingpixel.undiscoveredarchives.room.MyDatabaseRepository
import com.codingpixel.undiscoveredarchives.room.UserCartTable
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call

class HomeViewModel(
    private val databaseRepository: MyDatabaseRepository? = null,
) : ViewModel(), ResponseCallBack {

    private val apiResponse = MutableLiveData<Resource<JSONObject>>()

    fun getCartList(userId: Int) = databaseRepository?.getCartList(userId)

    fun saveCartData(cartTable: UserCartTable) = viewModelScope.launch {
        databaseRepository?.insertCart(cartTable)
    }

    fun updateCart(productId: Int, variantId: Int, quantity: Int, userId: Int) = viewModelScope.launch {
        databaseRepository?.updateCartTable(productId, variantId, quantity, userId)
    }

    fun deleteCart(userId: Int) = viewModelScope.launch {
        databaseRepository?.deleteCart(userId)
    }

    fun deleteSingleCartItem(userId: Int, productId: Int, variantId: Int) = viewModelScope.launch {
        databaseRepository?.deleteSingleCartItem(userId, productId, variantId)
    }

    /*Meta Data*/
    fun getCountries(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading(ApiTags.GET_COUNTRIES)
        RetrofitClient.apiCall(call, this, ApiTags.GET_COUNTRIES)
    }

    fun getStates(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading(ApiTags.GET_STATES)
        RetrofitClient.apiCall(call, this, ApiTags.GET_STATES)
    }

    fun getCities(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading(ApiTags.GET_CITIES)
        RetrofitClient.apiCall(call, this, ApiTags.GET_CITIES)
    }

    fun logout(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading(ApiTags.LOGOUT)
        RetrofitClient.apiCall(call, this, ApiTags.LOGOUT)
    }

    /*Home*/
    fun getNotifications(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading(ApiTags.GET_NOTIFICATIONS)
        RetrofitClient.apiCall(call, this, ApiTags.GET_NOTIFICATIONS)
    }

    fun getCategories(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading(ApiTags.GET_CATEGORIES)
        RetrofitClient.apiCall(call, this, ApiTags.GET_CATEGORIES)
    }

    fun getCategoriesWithProducts(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading(ApiTags.GET_CATEGORIES_WITH_PRODUCTS)
        RetrofitClient.apiCall(call, this, ApiTags.GET_CATEGORIES_WITH_PRODUCTS)
    }

    fun getDesigners(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading(ApiTags.GET_DESIGNERS)
        RetrofitClient.apiCall(call, this, ApiTags.GET_DESIGNERS)
    }

    fun getFeaturedProducts(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading(ApiTags.GET_FEATURED_PRODUCTS)
        RetrofitClient.apiCall(call, this, ApiTags.GET_FEATURED_PRODUCTS)
    }

    fun getFavoriteProducts(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading(ApiTags.GET_FAVORITE_PRODUCTS)
        RetrofitClient.apiCall(call, this, ApiTags.GET_FAVORITE_PRODUCTS)
    }

    fun getFavoriteDesigners(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading(ApiTags.GET_FAVORITE_DESIGNER)
        RetrofitClient.apiCall(call, this, ApiTags.GET_FAVORITE_DESIGNER)
    }

    fun addProductToFavorite(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading(ApiTags.ADD_PRODUCT_TO_FAVORITES)
        RetrofitClient.apiCall(call, this, ApiTags.ADD_PRODUCT_TO_FAVORITES)
    }

    fun addDesignerProductsToFavorite(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading(ApiTags.ADD_DESIGNER_TO_FAVORITES)
        RetrofitClient.apiCall(call, this, ApiTags.ADD_DESIGNER_TO_FAVORITES)
    }

    fun getProfile(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading(ApiTags.GET_PROFILE)
        RetrofitClient.apiCall(call, this, ApiTags.GET_PROFILE)
    }

    fun saveProfile(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading(ApiTags.SAVE_PROFILE)
        RetrofitClient.apiCall(call, this, ApiTags.SAVE_PROFILE)
    }

    fun getAllProducts(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading(ApiTags.GET_ALL_PRODUCTS)
        RetrofitClient.apiCall(call, this, ApiTags.GET_ALL_PRODUCTS)
    }

    fun getProductDetail(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading(ApiTags.GET_PRODUCT_DETAIL)
        RetrofitClient.apiCall(call, this, ApiTags.GET_PRODUCT_DETAIL)
    }

    fun getDraftProducts(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading(ApiTags.GET_DRAFTS_PRODUCTS)
        RetrofitClient.apiCall(call, this, ApiTags.GET_DRAFTS_PRODUCTS)
    }

    fun getTrashProducts(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading(ApiTags.GET_TRASHED_PRODUCTS)
        RetrofitClient.apiCall(call, this, ApiTags.GET_TRASHED_PRODUCTS)
    }

    fun getPublishedProducts(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading(ApiTags.GET_PUBLISHED_PRODUCTS)
        RetrofitClient.apiCall(call, this, ApiTags.GET_PUBLISHED_PRODUCTS)
    }

    fun deleteProduct(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading(ApiTags.DELETE_PRODUCT)
        RetrofitClient.apiCall(call, this, ApiTags.DELETE_PRODUCT)
    }

    fun restoreTrashedProduct(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading(ApiTags.RESTORE_TRASHED_PRODUCT)
        RetrofitClient.apiCall(call, this, ApiTags.RESTORE_TRASHED_PRODUCT)
    }

    fun updateProductStatus(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading(ApiTags.UPDATE_PRODUCT_STATUS)
        RetrofitClient.apiCall(call, this, ApiTags.UPDATE_PRODUCT_STATUS)
    }

    fun getProductGradients(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading(ApiTags.GET_PRODUCT_GRADIENTS)
        RetrofitClient.apiCall(call, this, ApiTags.GET_PRODUCT_GRADIENTS)
    }

    fun createProducts(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading(ApiTags.CREATE_PRODUCTS)
        RetrofitClient.apiCall(call, this, ApiTags.CREATE_PRODUCTS)
    }

    fun getSettings(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading(ApiTags.GET_SETTINGS)
        RetrofitClient.apiCall(call, this, ApiTags.GET_SETTINGS)
    }

    fun becomeSeller(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading(ApiTags.BECOME_SELLER)
        RetrofitClient.apiCall(call, this, ApiTags.BECOME_SELLER)
    }

    fun deleteProductFiles(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading(ApiTags.DELETE_PRODUCT_FILES)
        RetrofitClient.apiCall(call, this, ApiTags.DELETE_PRODUCT_FILES)
    }

    fun saveNotificationSettings(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading(ApiTags.NOTIFICATION_SETTINGS)
        RetrofitClient.apiCall(call, this, ApiTags.NOTIFICATION_SETTINGS)
    }

    fun getAllDesigners(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading(ApiTags.GET_ALL_DESIGNERS)
        RetrofitClient.apiCall(call, this, ApiTags.GET_ALL_DESIGNERS)
    }


    override fun onSuccess(jsonObject: JSONObject, tag: String) {
        when (tag) {
            ApiTags.GET_COUNTRIES -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.GET_COUNTRIES))
            }
            ApiTags.GET_STATES -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.GET_STATES))
            }
            ApiTags.GET_CITIES -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.GET_CITIES))
            }
            ApiTags.GET_NOTIFICATIONS -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.GET_NOTIFICATIONS))
            }
            ApiTags.GET_CATEGORIES -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.GET_CATEGORIES))
            }
            ApiTags.GET_CATEGORIES_WITH_PRODUCTS -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.GET_CATEGORIES_WITH_PRODUCTS))
            }
            ApiTags.GET_DESIGNERS -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.GET_DESIGNERS))
            }
            ApiTags.GET_FEATURED_PRODUCTS -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.GET_FEATURED_PRODUCTS))
            }
            ApiTags.GET_FAVORITE_PRODUCTS -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.GET_FAVORITE_PRODUCTS))
            }
            ApiTags.GET_FAVORITE_DESIGNER -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.GET_FAVORITE_DESIGNER))
            }
            ApiTags.ADD_PRODUCT_TO_FAVORITES -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.ADD_PRODUCT_TO_FAVORITES))
            }
            ApiTags.ADD_DESIGNER_TO_FAVORITES -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.ADD_DESIGNER_TO_FAVORITES))
            }
            ApiTags.GET_PROFILE -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.GET_PROFILE))
            }
            ApiTags.SAVE_PROFILE -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.SAVE_PROFILE))
            }
            ApiTags.GET_ALL_PRODUCTS -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.GET_ALL_PRODUCTS))
            }
            ApiTags.GET_PRODUCT_DETAIL -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.GET_PRODUCT_DETAIL))
            }
            ApiTags.GET_DRAFTS_PRODUCTS -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.GET_DRAFTS_PRODUCTS))
            }
            ApiTags.GET_TRASHED_PRODUCTS -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.GET_TRASHED_PRODUCTS))
            }
            ApiTags.GET_PUBLISHED_PRODUCTS -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.GET_PUBLISHED_PRODUCTS))
            }
            ApiTags.DELETE_PRODUCT -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.DELETE_PRODUCT))
            }
            ApiTags.RESTORE_TRASHED_PRODUCT -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.RESTORE_TRASHED_PRODUCT))
            }
            ApiTags.UPDATE_PRODUCT_STATUS -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.UPDATE_PRODUCT_STATUS))
            }
            ApiTags.GET_PRODUCT_GRADIENTS -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.GET_PRODUCT_GRADIENTS))
            }
            ApiTags.CREATE_PRODUCTS -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.CREATE_PRODUCTS))
            }
            ApiTags.GET_SETTINGS -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.GET_SETTINGS))
            }
            ApiTags.BECOME_SELLER -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.BECOME_SELLER))
            }
            ApiTags.LOGOUT -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.LOGOUT))
            }
            ApiTags.DELETE_PRODUCT_FILES -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.DELETE_PRODUCT_FILES))
            }
            ApiTags.NOTIFICATION_SETTINGS -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.NOTIFICATION_SETTINGS))
            }
            ApiTags.GET_ALL_DESIGNERS -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.GET_ALL_DESIGNERS))
            }
        }
    }

    override fun onError(error: String, tag: String) {
        when (tag) {
            ApiTags.GET_COUNTRIES -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.GET_COUNTRIES))
            }
            ApiTags.GET_STATES -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.GET_STATES))
            }
            ApiTags.GET_CITIES -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.GET_CITIES))
            }
            ApiTags.GET_NOTIFICATIONS -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.GET_NOTIFICATIONS))
            }
            ApiTags.GET_CATEGORIES -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.GET_CATEGORIES))
            }
            ApiTags.GET_CATEGORIES_WITH_PRODUCTS -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.GET_CATEGORIES_WITH_PRODUCTS))
            }
            ApiTags.GET_DESIGNERS -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.GET_DESIGNERS))
            }
            ApiTags.GET_FEATURED_PRODUCTS -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.GET_FEATURED_PRODUCTS))
            }
            ApiTags.GET_FAVORITE_PRODUCTS -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.GET_FAVORITE_PRODUCTS))
            }
            ApiTags.GET_FAVORITE_DESIGNER -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.GET_FAVORITE_DESIGNER))
            }
            ApiTags.ADD_PRODUCT_TO_FAVORITES -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.ADD_PRODUCT_TO_FAVORITES))
            }
            ApiTags.ADD_DESIGNER_TO_FAVORITES -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.ADD_DESIGNER_TO_FAVORITES))
            }
            ApiTags.GET_PROFILE -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.GET_PROFILE))
            }
            ApiTags.SAVE_PROFILE -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.SAVE_PROFILE))
            }
            ApiTags.GET_ALL_PRODUCTS -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.GET_ALL_PRODUCTS))
            }
            ApiTags.GET_PRODUCT_DETAIL -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.GET_PRODUCT_DETAIL))
            }
            ApiTags.GET_DRAFTS_PRODUCTS -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.GET_DRAFTS_PRODUCTS))
            }
            ApiTags.GET_TRASHED_PRODUCTS -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.GET_TRASHED_PRODUCTS))
            }
            ApiTags.GET_PUBLISHED_PRODUCTS -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.GET_PUBLISHED_PRODUCTS))
            }
            ApiTags.DELETE_PRODUCT -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.DELETE_PRODUCT))
            }
            ApiTags.RESTORE_TRASHED_PRODUCT -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.RESTORE_TRASHED_PRODUCT))
            }
            ApiTags.UPDATE_PRODUCT_STATUS -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.UPDATE_PRODUCT_STATUS))
            }
            ApiTags.GET_PRODUCT_GRADIENTS -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.GET_PRODUCT_GRADIENTS))
            }
            ApiTags.CREATE_PRODUCTS -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.CREATE_PRODUCTS))
            }
            ApiTags.GET_SETTINGS -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.GET_SETTINGS))
            }
            ApiTags.BECOME_SELLER -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.BECOME_SELLER))
            }
            ApiTags.LOGOUT -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.LOGOUT))
            }
            ApiTags.DELETE_PRODUCT_FILES -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.DELETE_PRODUCT_FILES))
            }
            ApiTags.NOTIFICATION_SETTINGS -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.NOTIFICATION_SETTINGS))
            }
            ApiTags.GET_ALL_DESIGNERS -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.GET_ALL_DESIGNERS))
            }
        }
    }

    fun getApiResponse(): LiveData<Resource<JSONObject>> = apiResponse
}