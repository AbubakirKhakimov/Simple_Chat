package com.x.a_technologies.simple_chat.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.orhanobut.hawk.Hawk
import com.x.a_technologies.simple_chat.R
import com.x.a_technologies.simple_chat.datas.UriChange
import com.x.a_technologies.simple_chat.utils.LocaleManager

class IntroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null && data.data != null && resultCode == RESULT_OK){
            if (UriChange.uriCallBack != null) {
                UriChange.uriCallBack!!.selectedImage(data.data!!)
            }
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        Hawk.init(newBase).build()
        super.attachBaseContext(LocaleManager.setLocale(newBase))
    }

}