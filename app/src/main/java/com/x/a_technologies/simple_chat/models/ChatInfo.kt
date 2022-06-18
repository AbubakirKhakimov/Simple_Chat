package com.x.a_technologies.simple_chat.models

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

data class ChatInfo(
    var chatId: String = "",
    var lastMessage: Message = Message(),
    var membersInfoList:List<MemberInfo> = ArrayList()
):Serializable, Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readParcelable(Message::class.java.classLoader)!!,
        parcel.createTypedArrayList(MemberInfo)!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(chatId)
        parcel.writeParcelable(lastMessage, flags)
        parcel.writeTypedList(membersInfoList)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ChatInfo> {
        override fun createFromParcel(parcel: Parcel): ChatInfo {
            return ChatInfo(parcel)
        }

        override fun newArray(size: Int): Array<ChatInfo?> {
            return arrayOfNulls(size)
        }
    }
}
