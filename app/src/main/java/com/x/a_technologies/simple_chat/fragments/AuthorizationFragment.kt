package com.x.a_technologies.simple_chat.fragments

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.x.a_technologies.simple_chat.R
import com.x.a_technologies.simple_chat.databinding.FragmentAuthorizationBinding
import com.x.a_technologies.simple_chat.models.PhoneMask
import com.x.a_technologies.simple_chat.models.viewModels.FragmentsCallBackViewModel
import com.x.a_technologies.simple_chat.utils.LoadingDialogManager
import com.x.a_technologies.simple_chat.utils.PhoneMaskManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class AuthorizationFragment : Fragment() {

    private lateinit var binding: FragmentAuthorizationBinding
    private lateinit var loadingDialogManager: LoadingDialogManager
    private val fragmentsCallBack: FragmentsCallBackViewModel by activityViewModels()

    private val phoneMasksList = ArrayList<PhoneMask>()
    private var phoneMaskPosition: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadingDialogManager = LoadingDialogManager(requireActivity())
        setFragmentResultListener("getPhoneMaskPosition") { requestKey, bundle ->
            phoneMaskPosition = bundle.getInt("position")
            updateUI()
        }
        getPhoneMasks()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAuthorizationBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateUI()

        binding.countryNameLayout.setOnClickListener {
            findNavController().navigate(R.id.action_authorizationFragment_to_selectCountryCodeFragment)
        }

        binding.countryName.setOnClickListener {
            findNavController().navigate(R.id.action_authorizationFragment_to_selectCountryCodeFragment)
        }

        binding.nextButton.setOnClickListener {
            val phoneNumber = "${binding.countryCode.text}${binding.phoneNumber.rawText}"

            if (phoneNumber.isEmpty() || phoneNumber == "+"){
                Toast.makeText(requireActivity(), getString(R.string.please_enter_your_phone_number), Toast.LENGTH_SHORT).show()
            }else{
                isLoading(true)
                sendSms(phoneNumber)
            }
        }

        binding.countryCode.addTextChangedListener {
            if (binding.countryCode.isFocused) {
                searchCountryCode(it.toString())
            }
        }

    }

    private fun getPhoneMasks(){
        lifecycleScope.launch{
            withContext(Dispatchers.Default){
                phoneMasksList.apply {
                    clear()
                    addAll(PhoneMaskManager().loadPhoneMusk())
                }
            }
        }
    }

    private fun searchCountryCode(code: String){
        lifecycleScope.launch{
            withContext(Dispatchers.Default){
                for ((index, phoneMask) in phoneMasksList.withIndex()){
                    if (phoneMask.countryCode == code){
                        phoneMaskPosition = index
                        break
                    }else if (index == phoneMasksList.size-1){
                        phoneMaskPosition = null
                    }
                }
            }

            updateUI()
        }
    }

    private fun updateUI() {
        if (phoneMaskPosition != null) {
            val item = phoneMasksList[phoneMaskPosition!!]
            binding.countryName.setText(item.name)
            if (!binding.countryCode.isFocused) {
                binding.countryCode.setText(item.countryCode)
            }
            binding.phoneNumber.hint = item.mask
                .replace("0", "-")
                .replace(" ", "-")
            binding.phoneNumber.mask = item.mask
        }else{
            if (binding.countryCode.text.toString() == "+" || binding.countryCode.text!!.isEmpty()){
                binding.countryName.setText(getString(R.string.choose_a_country))
            }else{
                binding.countryName.setText(getString(R.string.no_such_country_code))
            }
            binding.phoneNumber.hint = "--------------------"
            binding.phoneNumber.mask = "00000000000000000000"
        }
    }

    private fun sendSms(phoneNumber: String){
        Firebase.auth.useAppLanguage()
        val options = PhoneAuthOptions.newBuilder(Firebase.auth)
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(requireActivity())                 // Activity (for callback binding)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks(){

                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    fragmentsCallBack.autoAuthorizedCallBack.value = credential
                }

                override fun onVerificationFailed(p0: FirebaseException) {
                    verificationFailed()
                }

                override fun onCodeSent(verificationId: String, p1: PhoneAuthProvider.ForceResendingToken) {
                    super.onCodeSent(verificationId, p1)
                    codeSend(verificationId, phoneNumber)
                }

            }).build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun codeSend(verificationId:String, phoneNumber: String){
        isLoading(false)
        findNavController().navigate(R.id.action_authorizationFragment_to_verificationCodeFragment, bundleOf(
            "phoneNumber" to phoneNumber,
            "verificationId" to verificationId)
        )
    }

    fun verificationFailed(){
        isLoading(false)
        Toast.makeText(requireActivity(), R.string.validPhoneNumber, Toast.LENGTH_SHORT).show()
    }

    private fun closeKeyboard(){
        val inputMethodManager = requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.phoneNumber.windowToken, 0)
    }

    private fun isLoading(bool: Boolean) {
        if (bool) {
            closeKeyboard()
            loadingDialogManager.showDialog()
        } else {
            loadingDialogManager.dismissDialog()
        }
    }
}