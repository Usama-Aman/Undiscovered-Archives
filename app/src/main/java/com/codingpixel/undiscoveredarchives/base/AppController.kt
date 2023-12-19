package com.codingpixel.undiscoveredarchives.base

import android.app.Application
import com.codingpixel.undiscoveredarchives.home.favorites.models.UserProductData
import com.codingpixel.undiscoveredarchives.room.MyDatabase
import com.codingpixel.undiscoveredarchives.room.MyDatabaseRepository
import com.codingpixel.undiscoveredarchives.utils.DropdownModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class AppController : Application() {

    companion object {
        const val PAGINATION_COUNT = 10
        val filterDropDownList = ArrayList<DropdownModel>()

        var profileReferralCode = ""
        var isGuestUser = false
        var addEditUserProductData: UserProductData? = null
        var productDataForOrderDetail: UserProductData? = null
    }

    // No need to cancel this scope as it'll be torn down with the process
    val applicationScope = CoroutineScope(SupervisorJob())

    val myDatabase by lazy { MyDatabase.getDatabase(this, applicationScope) }
    val myDatabaseRepository by lazy { MyDatabaseRepository(myDatabase.myDao()) }


    override fun onCreate() {
        super.onCreate()
        populateFilterList()
    }

    private fun populateFilterList() {
        filterDropDownList.add(DropdownModel("All", 0, false))
        filterDropDownList.add(DropdownModel("High Price", 0, false))
        filterDropDownList.add(DropdownModel("Low Price", 0, false))
        filterDropDownList.add(DropdownModel("New", 0, false))
        filterDropDownList.add(DropdownModel("Popular", 0, false))
    }

}