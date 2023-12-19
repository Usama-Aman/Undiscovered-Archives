package com.codingpixel.undiscoveredarchives.utils.photo_util


interface MediaIntentCallBack {
    fun onPhotoIntentSuccess(imageUri: String)
    fun onMediaIntentSuccess(Uri: String, type: String)
    fun onMultipleImagesSuccess(imagesList : ArrayList<String>)
}