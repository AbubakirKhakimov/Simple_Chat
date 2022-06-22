package com.x.a_technologies.simple_chat.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.x.a_technologies.simple_chat.R
import com.x.a_technologies.simple_chat.activities.MainActivity
import com.x.a_technologies.simple_chat.databinding.FragmentVerificationCodeBinding
import com.x.a_technologies.simple_chat.database.UserData
import com.x.a_technologies.simple_chat.models.viewModels.FragmentsCallBackViewModel
import com.x.a_technologies.simple_chat.models.viewModels.MainViewModel
import com.x.a_technologies.simple_chat.models.User
import com.x.a_technologies.simple_chat.utils.LoadingDialogManager

class VerificationCodeFragment : Fragment() {

    private lateinit var binding: FragmentVerificationCodeBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var loadingDialogManager: LoadingDialogManager
    private val fragmentsCallBack: FragmentsCallBackViewModel by activityViewModels()
    private var permittedChangedListener = true

    private lateinit var verificationId:String
    private lateinit var phoneNumber: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        loadingDialogManager = LoadingDialogManager(requireActivity())
        phoneNumber = arguments?.getString("phoneNumber")!!
        verificationId = arguments?.getString("verificationId")!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentVerificationCodeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.number.text = "${getString(R.string.we_will_send_the_code_to)} $phoneNumber"

        viewModel.errorData.observe(viewLifecycleOwner){
            Toast.makeText(requireActivity(), getString(R.string.error), Toast.LENGTH_SHORT).show()
            isLoading(false)
        }

        fragmentsCallBack.autoAuthorizedCallBack.observe(viewLifecycleOwner){
            isLoading(true)
            binding.cetMyCode.text = it.smsCode!!
            signInWithPhoneAuthCredential(it)
        }

        binding.cetMyCode.setOnCodeChangedListener { (code, completed) ->
            if (completed && permittedChangedListener){
                isLoading(true)
                verification(code)
            }
        }

    }

    private fun verification(code: String){
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        Firebase.auth.signInWithCredential(credential).addOnCompleteListener(requireActivity()) { task ->
            if (task.isSuccessful) {
                viewModel.checkUser(Firebase.auth.currentUser!!.phoneNumber!!).observe(viewLifecycleOwner){
                    checkUser(it)
                }
            } else {
                isLoading(false)
                Toast.makeText(requireActivity(), getString(R.string.invalidCode), Toast.LENGTH_SHORT).show()
                binding.cetMyCode.text = ""
            }
        }
    }

    private fun checkUser(currentUser: User?) {
        Toast.makeText(requireActivity(), getString(R.string.successfulAuthorized), Toast.LENGTH_SHORT).show()
        isLoading(false)

        if (currentUser == null) {
            findNavController().navigate(R.id.action_verificationCodeFragment_to_getUserInfoFragment)
        } else {
            UserData.currentUser = currentUser

            startActivity(Intent(requireActivity(), MainActivity::class.java))
            requireActivity().finish()
        }
    }

    private fun closeKeyboard(){
        val inputMethodManager = requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.cetMyCode.windowToken, 0)
    }

    private fun isLoading(bool: Boolean) {
        if (bool) {
            closeKeyboard()
            loadingDialogManager.showDialog()
            permittedChangedListener = false
        } else {
            loadingDialogManager.dismissDialog()
            permittedChangedListener = true
        }
    }

}