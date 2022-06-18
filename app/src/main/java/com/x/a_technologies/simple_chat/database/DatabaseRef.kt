package com.x.a_technologies.simple_chat.database

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.x.a_technologies.simple_chat.models.User

object DatabaseRef {
    val rootRef:DatabaseReference by lazy {
        Firebase.database.reference
    }
    val usersRef:DatabaseReference by lazy {
        Firebase.database.getReference(Keys.USERS_KEY)
    }
    val chatsRef:DatabaseReference by lazy {
        Firebase.database.getReference(Keys.CHATS_KEY)
    }
    val storageRef:StorageReference by lazy {
        FirebaseStorage.getInstance().getReference("users avatars")
    }
}