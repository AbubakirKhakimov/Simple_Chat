package com.x.a_technologies.simple_chat.fragments

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.x.a_technologies.simple_chat.R
import com.x.a_technologies.simple_chat.activities.MainActivity
import com.x.a_technologies.simple_chat.databinding.FragmentSelectImageBinding
import com.x.a_technologies.simple_chat.datas.Datas
import com.x.a_technologies.simple_chat.datas.UriCallBack
import com.x.a_technologies.simple_chat.datas.UriChange
import com.x.a_technologies.simple_chat.models.User
import java.io.ByteArrayOutputStream


class SelectImageFragment : Fragment(), UriCallBack {

    lateinit var binding: FragmentSelectImageBinding
    lateinit var firstName:String
    lateinit var lastName:String
    var imageUrl:String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSelectImageBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firstName = arguments?.getString("firstName")!!
        lastName = arguments?.getString("lastName")!!

        binding.chooseImage.setOnClickListener {
            chooseImage()
        }

        binding.nextButton.setOnClickListener {
            isLoading(true)

            if (binding.circleImageView.drawable == null){
                writeUserData()
            }else{
                writeImageDatabase()
            }
        }

    }

    private fun writeUserData() {
        val user = User(
            Datas.auth.currentUser!!.phoneNumber!!,
            firstName,
            lastName,
            imageUrl
        )

        Datas.refUser.child(Datas.auth.currentUser!!.phoneNumber!!).setValue(user)
            .addOnSuccessListener {

                Toast.makeText(requireActivity(), getString(R.string.data_seved), Toast.LENGTH_SHORT).show()
                Datas.currentUser = user
                startActivity(Intent(requireActivity(), MainActivity::class.java))
                requireActivity().finish()

            }.addOnFailureListener {

                isLoading(false)
                Toast.makeText(requireActivity(), getString(R.string.error), Toast.LENGTH_SHORT).show()

            }
    }

    private fun writeImageDatabase(){
        val bitmap:Bitmap = binding.circleImageView.drawable.toBitmap()
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        val imageRef = Datas.storageRef.child("${Datas.auth.currentUser!!.phoneNumber}_user_avatar")
        val uploadTask = imageRef.putBytes(byteArray)

        val urlTask = uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            imageRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                imageUrl = task.result.toString()
                writeUserData()
            } else {
                Toast.makeText(requireActivity(), getString(R.string.error), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun chooseImage(){
        val intentChooser = Intent()
        intentChooser.type = "image/"
        intentChooser.action = Intent.ACTION_GET_CONTENT
        UriChange.tracker(this)
        startActivityForResult(intentChooser, 2)
    }

    override fun selectedImage(uri: Uri) {
        binding.animationView.pauseAnimation()
        binding.animationView.visibility = View.INVISIBLE
        Glide.with(requireActivity()).load(uri).into(binding.circleImageView)
    }

    private fun isLoading(bool:Boolean){
        if (bool){
            binding.nextButton.visibility = View.INVISIBLE
            binding.progressBar.visibility = View.VISIBLE
            binding.chooseImage.isClickable = false
        }else{
            binding.nextButton.visibility = View.VISIBLE
            binding.progressBar.visibility = View.INVISIBLE
            binding.chooseImage.isClickable = true
        }
    }

}