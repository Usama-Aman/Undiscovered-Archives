@file:Suppress("unused")

package com.codingpixel.undiscoveredarchives.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "CartTable")
class UserCartTable(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val user_id: Int,
    val variant_id: Int,
    val quantity: Int,
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
)
