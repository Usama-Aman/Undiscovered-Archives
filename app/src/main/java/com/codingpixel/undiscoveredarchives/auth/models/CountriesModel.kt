package com.codingpixel.undiscoveredarchives.auth.models

import android.os.Parcel
import android.os.Parcelable

data class CountriesModel(
    val `data`: List<CountriesData>,
    val message: String,
    val status: Boolean
)

data class CountriesData(
    val code: String,
    val id: Int,
    val name: String,
    val phonecode: Int
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readInt()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(code)
        parcel.writeInt(id)
        parcel.writeString(name)
        parcel.writeInt(phonecode)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CountriesData> {
        override fun createFromParcel(parcel: Parcel): CountriesData {
            return CountriesData(parcel)
        }

        override fun newArray(size: Int): Array<CountriesData?> {
            return arrayOfNulls(size)
        }
    }
}