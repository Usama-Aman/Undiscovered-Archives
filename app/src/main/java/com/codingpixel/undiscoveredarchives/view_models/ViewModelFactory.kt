package com.codingpixel.undiscoveredarchives.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.codingpixel.undiscoveredarchives.room.MyDatabaseRepository

@Suppress("UNCHECKED_CAST")
class ViewModelFactory(
    private val repository: MyDatabaseRepository? = null
) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel() as T
            }
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(repository) as T
            }
            else -> throw IllegalArgumentException("Unknown class name")
        }
    }
}
