package com.x.a_technologies.simple_chat.models

import android.os.Parcel
import android.os.Parcelable

data class Message(
    var messageId:String = "",
    var number: String = "",
    var massage: String = "",
    var sendTime: Long = 0
):Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(messageId)
        parcel.writeString(number)
        parcel.writeString(massage)
        parcel.writeLong(sendTime)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Message> {
        override fun createFromParcel(parcel: Parcel): Message {
            return Message(parcel)
        }

        override fun newArray(size: Int): Array<Message?> {
            return arrayOfNulls(size)
        }
    }
}
