package com.x.a_technologies.simple_chat.datas

import android.net.Uri

interface UriCallBack{
    fun selectedImage(uri: Uri)
}

object UriChange {
    var uriCallBack: UriCallBack? = null

    fun tracker(uriCallBack: UriCallBack){
        this.uriCallBack = uriCallBack
    }
}