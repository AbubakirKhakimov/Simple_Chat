package com.x.a_technologies.simple_chat.utils

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import com.x.a_technologies.simple_chat.databinding.LoadingDialogLayoutBinding

class LoadingDialogManager(val context: Context) {

    private var customDialog: AlertDialog? = null

    fun showDialog(){
        customDialog = AlertDialog.Builder(context).create()
        val dialogBinding = LoadingDialogLayoutBinding.inflate(LayoutInflater.from(context))
        customDialog?.setView(dialogBinding.root)

        customDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        customDialog?.setCancelable(false)
        customDialog?.show()
    }

    fun dismissDialog(){
        customDialog?.dismiss()
        customDialog = null
    }

}