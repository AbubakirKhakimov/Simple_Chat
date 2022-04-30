package com.x.a_technologies.simple_chat.fragments

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.x.a_technologies.simple_chat.R
import com.x.a_technologies.simple_chat.activities.MainActivity
import com.x.a_technologies.simple_chat.databinding.FragmentSelectImageBinding
import com.x.a_technologies.simple_chat.database.DatabaseRef
import com.x.a_technologies.simple_chat.database.UriCallBack
import com.x.a_technologies.simple_chat.database.UriChange
import com.x.a_technologies.simple_chat.models.MainViewModel
import com.x.a_technologies.simple_chat.models.User
import com.x.a_technologies.simple_chat.utils.ImageCallBack
import com.x.a_technologies.simple_chat.utils.MyLifecycleObserver
import java.io.ByteArrayOutputStream


class SelectImageFragment : Fragment(), ImageCallBack {

    lateinit var binding: FragmentSelectImageBinding
    lateinit var observer: MyLifecycleObserver
    lateinit var viewModel: MainViewModel

    lateinit var firstName:String
    lateinit var lastName:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        initObservers()
        observer = MyLifecycleObserver(requireActivity().activityResultRegistry, this)
        lifecycle.addObserver(observer)

        firstName = arguments?.getString("firstName")!!
        lastName = arguments?.getString("lastName")!!
    }

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

        binding.chooseImage.setOnClickListener {
            observer.selectImage()
        }

        binding.nextButton.setOnClickListener {
            isLoading(true)

            if (binding.circleImageView.drawable == null){
                writeUserData(null)
            }else{
                writeImageDatabase()
            }
        }

    }

    private fun initObservers(){
        viewModel.uploadedImageUrl.observe(this){
            writeUserData(it)
        }

        viewModel.successfulWrited.observe(this){
            Toast.makeText(requireActivity(), getString(R.string.data_seved), Toast.LENGTH_SHORT).show()
            DatabaseRef.currentUser = it as User

            startActivity(Intent(requireActivity(), MainActivity::class.java))
            requireActivity().finish()
        }

        viewModel.errorData.observe(this){
            isLoading(false)
            Toast.makeText(requireActivity(), getString(R.string.error), Toast.LENGTH_SHORT).show()
        }
    }

    private fun writeUserData(imageUrl: String?) {
        val user = User(
            DatabaseRef.auth.currentUser!!.phoneNumber!!,
            firstName,
            lastName,
            imageUrl
        )

        viewModel.writeValueInDatabase(
            DatabaseRef.usersRef.child(DatabaseRef.auth.currentUser!!.phoneNumber!!),
            user
        )
    }

    private fun writeImageDatabase(){
        val bitmap:Bitmap = binding.circleImageView.drawable.toBitmap()
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()

        viewModel.uploadImage(byteArray)
    }

    override fun imageSelected(uri: Uri) {
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