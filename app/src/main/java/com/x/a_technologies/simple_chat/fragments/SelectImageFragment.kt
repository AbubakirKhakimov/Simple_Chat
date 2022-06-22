package com.x.a_technologies.simple_chat.fragments

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.x.a_technologies.simple_chat.R
import com.x.a_technologies.simple_chat.activities.MainActivity
import com.x.a_technologies.simple_chat.database.DatabaseRef
import com.x.a_technologies.simple_chat.database.UserData
import com.x.a_technologies.simple_chat.databinding.FragmentSelectImageBinding
import com.x.a_technologies.simple_chat.models.viewModels.MainViewModel
import com.x.a_technologies.simple_chat.models.User
import com.x.a_technologies.simple_chat.utils.LoadingDialogManager
import java.io.ByteArrayOutputStream

class SelectImageFragment : Fragment() {

    private lateinit var binding: FragmentSelectImageBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var loadingDialogManager: LoadingDialogManager
    private var imageSelected = false

    private lateinit var firstName:String
    private lateinit var lastName:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        loadingDialogManager = LoadingDialogManager(requireActivity())

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
            showImageChooser()
        }

        binding.nextButton.setOnClickListener {
            isLoading(true)

            if (imageSelected){
                writeImageDatabase()
            }else{
                writeUserData(null)
            }
        }

        viewModel.errorData.observe(viewLifecycleOwner){
            Toast.makeText(requireActivity(), getString(R.string.error), Toast.LENGTH_SHORT).show()
            isLoading(false)
        }

    }

    private fun writeUserData(imageUrl: String?) {
        val user = User(
            Firebase.auth.currentUser!!.phoneNumber!!,
            firstName,
            lastName,
            imageUrl
        )

        viewModel.writeValueInDatabase(
            DatabaseRef.usersRef.child(Firebase.auth.currentUser!!.phoneNumber!!),
            user
        ).observe(viewLifecycleOwner){
            isLoading(false)
            Toast.makeText(requireActivity(), getString(R.string.data_seved), Toast.LENGTH_SHORT).show()
            UserData.currentUser = user

            startActivity(Intent(requireActivity(), MainActivity::class.java))
            requireActivity().finish()
        }
    }

    private fun writeImageDatabase(){
        val bitmap:Bitmap = binding.circleImageView.drawable.toBitmap()
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()

        viewModel.uploadImage(byteArray).observe(viewLifecycleOwner){
            writeUserData(it)
        }
    }

    private val imageChooser = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null){
            Glide.with(requireActivity()).load(uri).into(binding.circleImageView)
            imageSelected = true
        }
    }

    private fun showImageChooser(){
        imageChooser.launch("image/*")
    }

    private fun isLoading(bool:Boolean){
        if (bool){
            loadingDialogManager.showDialog()
        }else{
            loadingDialogManager.dismissDialog()
        }
    }

}