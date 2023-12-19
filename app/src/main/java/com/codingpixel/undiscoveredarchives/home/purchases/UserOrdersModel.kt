package com.codingpixel.undiscoveredarchives.home.purchases

data class UserOrdersModel(
    val count: Int,
    val `data`: List<UserOrdersData>,
    val message: String,
    val status: Boolean
)

data class UserOrdersData(
    val app_charges: String,
    val created_at: String,
    val created_at_formatted: String,
    val discount: String,
    val grand_total: String,
    val id: Int,
    val is_archived: Int,
    val items: List<Item>,
    val note: String,
    val order_number: String,
    val shipping: Shipping,
    val shipping_charges: String,
    val status: String,
    val sub_total: String,
    val tax: String,
    val user: User,
    val vendor: Vendor
)

data class Item(
    val id: Int,
    val price: String,
    val product: Product,
    val product_id: String,
    val product_name: String,
    val quantity: Int,
    val size: String,
    val sub_total: String
)

data class Shipping(
    val city_id: Int,
    val country_id: Int,
    val email: String,
    val first_name: String,
    val id: Int,
    val last_name: String,
    val mobile_number: String,
    val state_id: Int,
    val street_address: String,
    val zip_code: String
)

data class User(
    val id: Int,
    val mobile_number: String,
    val name: String
)

data class Vendor(
    val id: Int,
    val location: Any,
    val name: String,
    val photo_path: String
)

data class Product(
    val approval_status: String,
    val category_id: Int,
    val condition: String,
    val currency_id: Int,
    val deleted_at: Any,
    val designer_id: Int,
    val files: List<File>,
    val id: Int,
    val measurements: Any,
    val product_description: String,
    val product_name: String,
    val published_at: Any,
    val status: String,
    val sub_category_id: Int,
    val user_id: Int
)

data class File(
    val file_name: String,
    val file_path: String,
    val id: Int,
    val product_id: Int
)