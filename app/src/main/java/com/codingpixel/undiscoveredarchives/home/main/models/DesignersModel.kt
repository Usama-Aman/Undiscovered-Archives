package com.codingpixel.undiscoveredarchives.home.main.models

import android.os.Parcel
import android.os.Parcelable
import com.codingpixel.undiscoveredarchives.home.favorites.models.File

data class DesignersModel(
    val `data`: List<DesignersData>,
    val message: String,
    val count: Int,
    val status: Boolean
)

data class DesignersData(
    val description: String,
    val id: Int,
    val is_favourite: Int,
    val logo: String,
    val logo_path: String,
    val name: String,
    val owner_id: Int,
    val products_count: Int,
    var isAlphabet: Boolean = false
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readInt(),
        parcel.readByte() != 0.toByte()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(description)
        parcel.writeInt(id)
        parcel.writeInt(is_favourite)
        parcel.writeString(logo)
        parcel.writeString(logo_path)
        parcel.writeString(name)
        parcel.writeInt(owner_id)
        parcel.writeInt(products_count)
        parcel.writeByte(if (isAlphabet) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DesignersData> {
        override fun createFromParcel(parcel: Parcel): DesignersData {
            return DesignersData(parcel)
        }

        override fun newArray(size: Int): Array<DesignersData?> {
            return arrayOfNulls(size)
        }
    }
}

data class DesignerProducts(
    val category_id: Int,
    val condition: String,
    val deleted_at: String,
    val designer_id: Int,
    val files: List<File>,
    val id: Int,
    val is_approved: Int,
    val measurements: String,
    val product_description: String,
    val product_name: String,
    val published_at: String,
    val status: String,
    val sub_category_id: Int,
    val user_id: Int,
    val variants_max_price: String,
    val variants_min_price: String
)