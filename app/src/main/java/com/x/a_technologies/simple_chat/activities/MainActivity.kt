package com.x.a_technologies.simple_chat.activities

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.orhanobut.hawk.Hawk
import com.x.a_technologies.simple_chat.R
import com.x.a_technologies.simple_chat.utils.LocaleManager

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

    override fun attachBaseContext(newBase: Context?) {
        Hawk.init(newBase).build()
        super.attachBaseContext(LocaleManager.setLocale(newBase))
    }

}