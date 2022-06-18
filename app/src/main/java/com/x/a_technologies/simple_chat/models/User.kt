package com.x.a_technologies.simple_chat.models

data class User(
    var phoneNumber: String = "",
    var firstName: String = "",
    var lastName: String = "",
    var imageUrl:String? = null,
    var chatIdList: ArrayList<String> = ArrayList()
)
