package com.x.a_technologies.simple_chat.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.x.a_technologies.simple_chat.R
import com.x.a_technologies.simple_chat.activities.MainActivity
import com.x.a_technologies.simple_chat.databinding.FragmentSplashBinding
import com.x.a_technologies.simple_chat.database.DatabaseRef
import com.x.a_technologies.simple_chat.models.MainViewModel
import com.x.a_technologies.simple_chat.models.User

class SplashFragment : Fragment() {

    lateinit var binding: FragmentSplashBinding
    lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        initObservers()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSplashBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (Firebase.auth.currentUser == null) {
            binding.animationView.pauseAnimation()
            findNavController().navigate(R.id.action_splashFragment_to_authorizationFragment)
        } else {
            viewModel.getCurrentUser()
        }

    }

    private fun initObservers(){
        viewModel.currentUserData.observe(this){
            checkUser(it)
        }

        viewModel.errorData.observe(this){
            Toast.makeText(requireActivity(), "Error!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkUser(currentUser: User?){
        if (currentUser == null){
            binding.animationView.pauseAnimation()
            findNavController().navigate(R.id.action_splashFragment_to_getUserInfoFragment)
        }else{
            DatabaseRef.currentUser = currentUser
            binding.animationView.pauseAnimation()
            startActivity(Intent(requireActivity(), MainActivity::class.java))
            requireActivity().finish()
        }
    }

}