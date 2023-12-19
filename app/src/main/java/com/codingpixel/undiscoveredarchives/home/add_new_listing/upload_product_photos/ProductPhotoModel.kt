package com.codingpixel.undiscoveredarchives.home.add_new_listing.upload_product_photos

data class ProductPhotoModel(
    var id: Int = -1,
    var path: String = "",
    var isSelected: Boolean = false,
    var canAddPhoto: Boolean = false
)