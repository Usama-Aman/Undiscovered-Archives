package com.codingpixel.undiscoveredarchives.home.cart

data class CartModel(
    val id: Int,
    val user_id: Int,
    val variant_id: Int,
    var quantity: Int,
    val price: String,
    val vendor_id: Int,
    val product_id: Int,
    val zone_id: Int,
    val product_name: String,
    val product_description: String,
    val product_category: String,
    val product_category_id: Int,
    val product_image: String,
    val total_quantity: Int,
    var isChecked: Boolean = false
)