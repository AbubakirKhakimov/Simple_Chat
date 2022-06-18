package com.x.a_technologies.simple_chat.models.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.PhoneAuthCredential

class FragmentsCallBackViewModel:ViewModel() {

    val autoAuthorizedCallBack by lazy {
        MutableLiveData<PhoneAuthCredential>()
    }

}