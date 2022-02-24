package com.x.a_technologies.simple_chat.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.x.a_technologies.simple_chat.R
import com.x.a_technologies.simple_chat.activities.MainActivity
import com.x.a_technologies.simple_chat.databinding.FragmentSplashBinding
import com.x.a_technologies.simple_chat.datas.Datas
import com.x.a_technologies.simple_chat.models.User

class SplashFragment : Fragment() {

    lateinit var binding: FragmentSplashBinding

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

        if (Datas.auth.currentUser == null) {
            binding.animationView.pauseAnimation()
            findNavController().navigate(R.id.action_splashFragment_to_authorizationFragment)
        } else {
            checkUserDatabase()
        }

    }

    fun checkUserDatabase(){
        Datas.refUser.child(Datas.auth.currentUser!!.phoneNumber!!)
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.value == null){
                        binding.animationView.pauseAnimation()
                        findNavController().navigate(R.id.action_splashFragment_to_getUserInfoFragment)
                    }else{
                        Datas.currentUser = snapshot.getValue(User::class.java)!!

                        binding.animationView.pauseAnimation()
                        startActivity(Intent(requireActivity(), MainActivity::class.java))
                        requireActivity().finish()
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    binding.animationView.pauseAnimation()
                    startActivity(Intent(requireActivity(), MainActivity::class.java))
                    requireActivity().finish()
                }
            })
    }

}