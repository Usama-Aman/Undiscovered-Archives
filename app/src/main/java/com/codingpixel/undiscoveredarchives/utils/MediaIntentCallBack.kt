package com.codingpixel.undiscoveredarchives.utils


interface MediaIntentCallBack {
    fun onPhotoIntentSuccess(imageUri: String)
    fun onMediaIntentSuccess(Uri: String, type: String)
    fun onMultipleImagesSuccess(imagesList : ArrayList<String>)
}