package com.codingpixel.undiscoveredarchives.home.favorites.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class UserProductModel(
    val count: Int,
    val `data`: ArrayList<UserProductData>,
    val message: String,
    val status: Boolean
)

data class UserProductDetailModel(
    val count: Int,
    val `data`: UserProductData,
    val message: String,
    val status: Boolean
)

data class UserProductData(
    val approval_status: String = "",
    val category: Category? = Category(),
    var condition: String = "",
    val designer: Designer? = Designer(),
    val files: ArrayList<File> = ArrayList(),
    val display_image: String = "",
    var id: Int = -1,
    val is_favourite: Int = 0,
    val is_stock_available: Int = 0,
    val orders_count: Int = -1,
    var product_description: String = "",
    var product_name: String = "",
    val published_at: String = "",
    val reviews: ArrayList<Review> = ArrayList(),
    val reviews_avg_rating: String = "",
    val status: String = "",
    val sub_category: SubCategory = SubCategory(),
    val trashed_at: String = "",
//    val variants: ArrayList<Variant> = ArrayList(),
    @SerializedName("variants")
    val variants: ArrayList<Variant> = ArrayList(),
    val variants_max_price: String = "",
    val variants_min_price: String = "",
    val vendor: Vendor? = Vendor(),
    var isChecked: Boolean = false,
    val zones: ArrayList<ShipingZone> = ArrayList(),
    var measurements: String = "",
    val currency: Currency = Currency()
) {

}

data class Category(
    val add_by: Int = -1,
    val description: String = "",
    var id: Int = -1,
    val image: String = "",
    val image_path: String = "",
    val title: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(add_by)
        parcel.writeString(description)
        parcel.writeInt(id)
        parcel.writeString(image)
        parcel.writeString(image_path)
        parcel.writeString(title)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Category> {
        override fun createFromParcel(parcel: Parcel): Category {
            return Category(parcel)
        }

        override fun newArray(size: Int): Array<Category?> {
            return arrayOfNulls(size)
        }
    }
}

data class Designer(
    val description: String = "",
    var id: Int = -1,
    val logo: String = "",
    val logo_path: String = "",
    val name: String = "",
    val owner_id: Int = -1
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(description)
        parcel.writeInt(id)
        parcel.writeString(logo)
        parcel.writeString(logo_path)
        parcel.writeString(name)
        parcel.writeInt(owner_id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Designer> {
        override fun createFromParcel(parcel: Parcel): Designer {
            return Designer(parcel)
        }

        override fun newArray(size: Int): Array<Designer?> {
            return arrayOfNulls(size)
        }
    }
}

data class File(
    val file_name: String,
    val file_path: String,
    val id: Int,
    val product_id: Int
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(file_name)
        parcel.writeString(file_path)
        parcel.writeInt(id)
        parcel.writeInt(product_id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<File> {
        override fun createFromParcel(parcel: Parcel): File {
            return File(parcel)
        }

        override fun newArray(size: Int): Array<File?> {
            return arrayOfNulls(size)
        }
    }
}

data class SubCategory(
    val category_id: Int = -1,
    val description: String = "",
    var id: Int = -1,
    val image: String = "",
    val image_path: String = "",
    val title: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(category_id)
        parcel.writeString(description)
        parcel.writeInt(id)
        parcel.writeString(image)
        parcel.writeString(image_path)
        parcel.writeString(title)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SubCategory> {
        override fun createFromParcel(parcel: Parcel): SubCategory {
            return SubCategory(parcel)
        }

        override fun newArray(size: Int): Array<SubCategory?> {
            return arrayOfNulls(size)
        }
    }
}

//data class Variant(
//    val compare_at_price: String,
//    val id: Int,
//    val price: String,
//    val quantity: Int,
//    val size_id: Int,
//    var sizeValue: String,
//    val product_id: Int,
//    var commissioned_price: String,
//    var seller_earnings: String,
//) : Parcelable {
//    constructor(parcel: Parcel) : this(
//        parcel.readString()!!,
//        parcel.readInt(),
//        parcel.readString()!!,
//        parcel.readInt(),
//        parcel.readInt(),
//        parcel.readString()!!,
//        parcel.readInt(),
//        parcel.readString()!!,
//        parcel.readString()!!
//    )
//
//    override fun writeToParcel(parcel: Parcel, flags: Int) {
//        parcel.writeString(compare_at_price)
//        parcel.writeInt(id)
//        parcel.writeString(price)
//        parcel.writeInt(quantity)
//        parcel.writeInt(size_id)
//        parcel.writeString(sizeValue)
//        parcel.writeInt(product_id)
//        parcel.writeString(commissioned_price)
//        parcel.writeString(seller_earnings)
//    }
//
//    override fun describeContents(): Int {
//        return 0
//    }
//
//    companion object CREATOR : Parcelable.Creator<Variant> {
//        override fun createFromParcel(parcel: Parcel): Variant {
//            return Variant(parcel)
//        }
//
//        override fun newArray(size: Int): Array<Variant?> {
//            return arrayOfNulls(size)
//        }
//    }
//}

data class Variant(
    val compare_at_price: String = "",
    val id: Int = -1,
    val price: String = "",
    val quantity: Int = -1,
    val size_id: Int = -1,
    var sizeValue: String,
    val product_id: Int = -1,
    var commissioned_price: String,
    var seller_earnings: String,
    val size: Size = Size()


//    val compare_at_price: String,
//    val id: Int,
//    val price: String,
//    val quantity: Int,
//    val size_id: Int,
//    var sizeValue: String,
//    val product_id: Int,
//    var commissioned_price: String,
//    var seller_earnings: String,

)

data class Size(
    val category_id: Int = -1,
    val id: Int = -1,
    val type: String = "",
    val value: String = ""
)

data class Vendor(
    val country_code: String = "",
    val email: String = "",
    val id: Int = -1,
    val mobile_number: String = "",
    val name: String = "",
    val photo_path: String = "",
    val products_count: Int = -1
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(country_code)
        parcel.writeString(email)
        parcel.writeInt(id)
        parcel.writeString(mobile_number)
        parcel.writeString(name)
        parcel.writeString(photo_path)
        parcel.writeInt(products_count)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Vendor> {
        override fun createFromParcel(parcel: Parcel): Vendor {
            return Vendor(parcel)
        }

        override fun newArray(size: Int): Array<Vendor?> {
            return arrayOfNulls(size)
        }
    }
}

data class ShipingZone(
    val countries_name: List<String> = listOf(),
    val id: Int = -1,
    val name: String = "",
    var isChecked: Boolean = false,
    var isEmpty: Boolean = false,
    var pivot: Pivot = Pivot()

) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.createStringArrayList()!!,
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readParcelable(Pivot::class.java.classLoader)!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeStringList(countries_name)
        parcel.writeInt(id)
        parcel.writeString(name)
        parcel.writeByte(if (isChecked) 1 else 0)
        parcel.writeByte(if (isEmpty) 1 else 0)
        parcel.writeParcelable(pivot, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ShipingZone> {
        override fun createFromParcel(parcel: Parcel): ShipingZone {
            return ShipingZone(parcel)
        }

        override fun newArray(size: Int): Array<ShipingZone?> {
            return arrayOfNulls(size)
        }
    }
}

data class Pivot(
    val product_id: Int = -1,
    var shipping_charges: String = "",
    val shipping_zone_id: Int = -1
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(product_id)
        parcel.writeString(shipping_charges)
        parcel.writeInt(shipping_zone_id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Pivot> {
        override fun createFromParcel(parcel: Parcel): Pivot {
            return Pivot(parcel)
        }

        override fun newArray(size: Int): Array<Pivot?> {
            return arrayOfNulls(size)
        }
    }
}

data class Currency(
    val code: String = "",
    var id: Int = -1,
    val name: String = "",
    val symbol: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(code)
        parcel.writeInt(id)
        parcel.writeString(name)
        parcel.writeString(symbol)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Currency> {
        override fun createFromParcel(parcel: Parcel): Currency {
            return Currency(parcel)
        }

        override fun newArray(size: Int): Array<Currency?> {
            return arrayOfNulls(size)
        }
    }
}

data class Review(
    val comment: String,
    val created_at_formatted: String,
    val id: Int,
    val rating: Int,
    val user: User
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readInt(),
        parcel.readParcelable(User::class.java.classLoader)!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(comment)
        parcel.writeString(created_at_formatted)
        parcel.writeInt(id)
        parcel.writeInt(rating)
        parcel.writeParcelable(user, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Review> {
        override fun createFromParcel(parcel: Parcel): Review {
            return Review(parcel)
        }

        override fun newArray(size: Int): Array<Review?> {
            return arrayOfNulls(size)
        }
    }
}

data class User(
    val id: Int,
    val name: String,
    val photo: String,
    val photo_path: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(name)
        parcel.writeString(photo)
        parcel.writeString(photo_path)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }
}