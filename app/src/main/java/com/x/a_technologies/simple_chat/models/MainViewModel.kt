package com.x.a_technologies.simple_chat.models

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.x.a_technologies.simple_chat.database.DatabaseRef
import com.x.a_technologies.simple_chat.database.Keys

class MainViewModel:ViewModel() {

    val currentUserData = MutableLiveData<User?>()
    val uploadedImageUrl = MutableLiveData<String>()
    val successfulWrited = MutableLiveData<Any>()
    val chatsSelectTracker = MutableLiveData<ArrayList<ChatInfo>>()
    val chatTracker = MutableLiveData<ArrayList<Message>>()

    val errorData = MutableLiveData<String>()

    fun getCurrentUser(){
        DatabaseRef.usersRef.child(Firebase.auth.currentUser!!.phoneNumber!!)
            .addListenerForSingleValueEvent(object: ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    currentUserData.value = snapshot.getValue(User::class.java)
                }

                override fun onCancelled(error: DatabaseError) {
                    errorData.value = error.message
                }

            })
    }

    fun uploadImage(byteArray: ByteArray){
        val imageRef = DatabaseRef.storageRef
            .child("${DatabaseRef.auth.currentUser!!.phoneNumber}_user_avatar")
        val uploadTask = imageRef.putBytes(byteArray)

        val urlTask = uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            imageRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                uploadedImageUrl.value = task.result.toString()
            } else {
                errorData.value = task.exception?.message
            }
        }
    }

    fun writeValueInDatabase(databaseRef: DatabaseReference, value: Any){
        databaseRef.setValue(value).addOnCompleteListener {
            if (it.isSuccessful){
                successfulWrited.value = value
            }else{
                errorData.value = it.exception?.message
            }
        }
    }

    fun writeInUpdateChildren(databaseRef: DatabaseReference, value: Map<String, Any>){
        databaseRef.updateChildren(value).addOnCompleteListener {
            if (it.isSuccessful){
                successfulWrited.value = value
            }else{
                errorData.value = it.exception?.message
            }
        }
    }

    fun initChatTracker(currentChatId: String): ValueEventListener{
        return DatabaseRef.chatsRef.child(Keys.MESSAGES_KEY).child(currentChatId)
            .addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = ArrayList<Message>()
                    for (it in snapshot.children){
                        list.add(it.getValue(Message::class.java)!!)
                    }
                    chatTracker.value = list
                }

                override fun onCancelled(error: DatabaseError) {
                    errorData.value = error.message
                }
            })
    }

    fun initChatsSelectTracker(): ValueEventListener{
        return DatabaseRef.chatsRef.child(Keys.CHATS_INFO_KEY)
            .addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    sortChats(snapshot)
                }

                override fun onCancelled(error: DatabaseError) {
                    errorData.value = error.message
                }
            })
    }

    private fun sortChats(snapshot: DataSnapshot){
        DatabaseRef.usersRef.child(DatabaseRef.currentUser.number).get().addOnSuccessListener {
            if (it.value != null) {
                DatabaseRef.currentUser = it.getValue(User::class.java)!!

                val list = ArrayList<ChatInfo>()
                for (chatId in DatabaseRef.currentUser.chatIdList){
                    list.add(snapshot.child(chatId).getValue(ChatInfo::class.java)!!)
                }
                chatsSelectTracker.value = list
            }else{
                errorData.value = "Error!"
            }
        }.addOnFailureListener {
            errorData.value = it.message
        }
    }

}