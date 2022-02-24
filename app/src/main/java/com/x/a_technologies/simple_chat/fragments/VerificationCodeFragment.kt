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
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.x.a_technologies.simple_chat.R
import com.x.a_technologies.simple_chat.activities.MainActivity
import com.x.a_technologies.simple_chat.databinding.FragmentVerificationCodeBinding
import com.x.a_technologies.simple_chat.datas.Datas
import com.x.a_technologies.simple_chat.models.User

class VerificationCodeFragment : Fragment() {
    lateinit var binding: FragmentVerificationCodeBinding
    lateinit var verificationId:String
    lateinit var phoneNumber: String

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

        phoneNumber = arguments?.getString("phoneNumber")!!
        verificationId = arguments?.getString("verificationId")!!
        binding.number.text = phoneNumber

        listeners()

    }

    fun verification(){
        val credential = PhoneAuthProvider.getCredential(verificationId, getCode())
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        Datas.auth.signInWithCredential(credential).addOnCompleteListener(requireActivity()) { task ->
            if (task.isSuccessful) {
                checkUserDatabase()
            } else {
                inVisible()
                Toast.makeText(requireActivity(), getString(R.string.invalidCode), Toast.LENGTH_SHORT).show()
                clearEditText()
            }
        }
    }

    private fun checkUserDatabase(){
        Datas.refUser.child(phoneNumber).addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value == null){
                    inVisible()
                    Toast.makeText(requireActivity(), getString(R.string.successfulAuthorized), Toast.LENGTH_SHORT).show()

                    findNavController().navigate(R.id.action_verificationCodeFragment_to_getUserInfoFragment)

                }else{
                    Datas.currentUser = snapshot.getValue(User::class.java)!!
                    inVisible()
                    Toast.makeText(requireActivity(), getString(R.string.successfulAuthorized), Toast.LENGTH_SHORT).show()

                    startActivity(Intent(requireActivity(), MainActivity::class.java))
                    requireActivity().finish()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                inVisible()
                Toast.makeText(requireActivity(), getString(R.string.error), Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun getCode():String{
        var code = ""
        for (i in 0..5){
            code += getEditText(i).text.toString()
        }
        return code
    }

    fun clearEditText(){
        for (i in 0..5){
            val editText = getEditText(i)
            editText.text.clear()
            editText.clearFocus()
        }
        getEditText(0).requestFocus()
    }

    fun editTextController(position: Int, it: String) {
        if (it == "" && position != 0) {
            getEditText(position).clearFocus()
            getEditText(position-1).requestFocus()
        } else if (it != "" && position != 5){
            getEditText(position).clearFocus()
            getEditText(position+1).requestFocus()
        } else if (position == 5){
            val inputMethodManager = requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(getEditText(5).windowToken, 0)
            getEditText(5).clearFocus()
            visible()

            verification()
        }
    }

    fun listeners(){
        binding.oneCode.addTextChangedListener {
            editTextController(0, it.toString())
        }
        binding.twoCode.addTextChangedListener {
            editTextController(1, it.toString())
        }
        binding.threeCode.addTextChangedListener {
            editTextController(2, it.toString())
        }
        binding.fourCode.addTextChangedListener {
            editTextController(3, it.toString())
        }
        binding.fiveCode.addTextChangedListener {
            editTextController(4, it.toString())
        }
        binding.sixCode.addTextChangedListener {
            editTextController(5, it.toString())
        }
    }

    fun getEditText(position:Int): EditText {
        when(position){
            0 -> return binding.oneCode
            1 -> return binding.twoCode
            2 -> return binding.threeCode
            3 -> return binding.fourCode
            4 -> return binding.fiveCode
            5 -> return binding.sixCode
        }
        return binding.oneCode
    }

    fun visible(){
        binding.progressBar.visibility = View.VISIBLE
    }

    fun inVisible(){
        binding.progressBar.visibility = View.INVISIBLE
    }

}