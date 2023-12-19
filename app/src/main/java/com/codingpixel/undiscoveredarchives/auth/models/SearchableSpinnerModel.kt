package com.codingpixel.undiscoveredarchives.auth.models

import android.os.Parcel
import android.os.Parcelable

data class SearchableSpinnerModel(
    var id: Int = -1,
    var value: String = "",
    var isSelected: Boolean = false,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readByte() != 0.toByte()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(value)
        parcel.writeByte(if (isSelected) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SearchableSpinnerModel> {
        override fun createFromParcel(parcel: Parcel): SearchableSpinnerModel {
            return SearchableSpinnerModel(parcel)
        }

        override fun newArray(size: Int): Array<SearchableSpinnerModel?> {
            return arrayOfNulls(size)
        }
    }

}