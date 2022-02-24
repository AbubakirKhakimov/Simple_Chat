package com.x.a_technologies.simple_chat.datas

import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.x.a_technologies.simple_chat.models.ChatInfo
import com.x.a_technologies.simple_chat.models.Keys
import com.x.a_technologies.simple_chat.models.User

object Datas {
    val auth = Firebase.auth
    var refUser = Firebase.database.getReference(Keys.USERS_KEY)
    var refChat = Firebase.database.getReference(Keys.CHATS_KEY)
    var storageRef = FirebaseStorage.getInstance().getReference("users avatars")

    var currentUser = User()
    var chatInfoList = ArrayList<ChatInfo>()
}