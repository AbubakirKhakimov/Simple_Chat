package com.x.a_technologies.simple_chat.models

import android.os.Parcel
import android.os.Parcelable

data class MemberInfo(
    var phoneNumber: String = "",
    var firstName: String = "",
    var lastName: String = "",
    var imageUrl:String? = null
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(phoneNumber)
        parcel.writeString(firstName)
        parcel.writeString(lastName)
        parcel.writeString(imageUrl)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MemberInfo> {
        override fun createFromParcel(parcel: Parcel): MemberInfo {
            return MemberInfo(parcel)
        }

        override fun newArray(size: Int): Array<MemberInfo?> {
            return arrayOfNulls(size)
        }
    }
}
