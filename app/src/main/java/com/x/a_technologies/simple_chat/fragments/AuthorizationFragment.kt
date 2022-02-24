package com.x.a_technologies.simple_chat.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.x.a_technologies.simple_chat.R
import com.x.a_technologies.simple_chat.activities.MainActivity
import com.x.a_technologies.simple_chat.databinding.FragmentAuthorizationBinding
import com.x.a_technologies.simple_chat.datas.Datas
import com.x.a_technologies.simple_chat.models.User
import java.util.concurrent.TimeUnit

class AuthorizationFragment : Fragment() {
    lateinit var binding: FragmentAuthorizationBinding
    lateinit var phoneNumber: String

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

        binding.nextButton.setOnClickListener {
            if (binding.number.text.toString() == ""){
                Toast.makeText(requireActivity(), getString(R.string.please_enter_your_phone_number), Toast.LENGTH_SHORT).show()
            }else{
                inVisible()
                phoneNumber = "+${binding.countryCode.text}${binding.number.text}"
                sendSms()
            }
        }

    }

    private fun sendSms(){
        val options = PhoneAuthOptions.newBuilder(Datas.auth)
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(requireActivity())                 // Activity (for callback binding)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks(){

                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    signInWithPhoneAuthCredential(credential)
                }

                override fun onVerificationFailed(p0: FirebaseException) {
                    verificationFailed()
                }

                override fun onCodeSent(verificationId: String, p1: PhoneAuthProvider.ForceResendingToken) {
                    super.onCodeSent(verificationId, p1)
                    codeSend(verificationId)
                }

            }).build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun codeSend(verificationId:String){
        visible()
        findNavController().navigate(R.id.action_authorizationFragment_to_verificationCodeFragment, bundleOf(
            "phoneNumber" to phoneNumber,
            "verificationId" to verificationId)
        )
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        Datas.auth.signInWithCredential(credential).addOnCompleteListener(requireActivity()) { task ->
            if (task.isSuccessful) {
                checkUserDatabase()
            } else {
                visible()
                Toast.makeText(requireActivity(), getString(R.string.error), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkUserDatabase(){
        Datas.refUser.child(phoneNumber).addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value == null){
                    visible()
                    Toast.makeText(requireActivity(), getString(R.string.successfulAuthorized), Toast.LENGTH_SHORT).show()

                    findNavController().navigate(R.id.action_authorizationFragment_to_getUserInfoFragment)
                }else{
                    Datas.currentUser = snapshot.getValue(User::class.java)!!
                    visible()
                    Toast.makeText(requireActivity(), getString(R.string.successfulAuthorized), Toast.LENGTH_SHORT).show()

                    startActivity(Intent(requireActivity(), MainActivity::class.java))
                    requireActivity().finish()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                visible()
                Toast.makeText(requireActivity(), getString(R.string.error), Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun verificationFailed(){
        visible()
        Toast.makeText(requireActivity(), R.string.validPhoneNumber, Toast.LENGTH_SHORT).show()
    }

    fun visible(){
        binding.nextButton.visibility = View.VISIBLE
        binding.nextText.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE
    }

    fun inVisible(){
        binding.nextButton.visibility = View.INVISIBLE
        binding.nextText.visibility = View.INVISIBLE
        binding.progressBar.visibility = View.VISIBLE
    }
}