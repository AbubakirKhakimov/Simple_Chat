package com.x.a_technologies.simple_chat.utils

import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

interface ImageCallBack{
    fun imageSelected(uri: Uri)
}

class MyLifecycleObserver(private val registry: ActivityResultRegistry, val imageCallBack: ImageCallBack) :
    DefaultLifecycleObserver {

    private lateinit var getContent: ActivityResultLauncher<String>

    override fun onCreate(owner: LifecycleOwner) {
        getContent = registry.register("key", owner, ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                imageCallBack.imageSelected(uri)
            }
        }
    }

    fun selectImage() {
        getContent.launch("image/*")
    }
}