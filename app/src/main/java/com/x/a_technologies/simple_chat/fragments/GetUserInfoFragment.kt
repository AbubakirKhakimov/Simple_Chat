package com.x.a_technologies.simple_chat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.x.a_technologies.simple_chat.R
import com.x.a_technologies.simple_chat.databinding.FragmentGetUserInfoBinding

class GetUserInfoFragment : Fragment() {

    lateinit var binding: FragmentGetUserInfoBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentGetUserInfoBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.nextButton.setOnClickListener {
            if (binding.firstName.text!!.isEmpty()){
                Toast.makeText(requireActivity(), getString(R.string.write_your_first_name), Toast.LENGTH_SHORT).show()
            }else{
                findNavController().navigate(R.id.action_getUserInfoFragment_to_selectImageFragment,
                bundleOf(
                    "firstName" to binding.firstName.text.toString(),
                    "lastName" to binding.lastName.text.toString()
                ))
            }
        }

    }
}