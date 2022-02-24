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
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.x.a_technologies.simple_chat.R
import com.x.a_technologies.simple_chat.databinding.FragmentProfileSettingsBinding
import com.x.a_technologies.simple_chat.datas.Datas
import com.x.a_technologies.simple_chat.datas.UriCallBack
import com.x.a_technologies.simple_chat.datas.UriChange
import com.x.a_technologies.simple_chat.models.Keys
import com.x.a_technologies.simple_chat.models.MemberInfo
import com.x.a_technologies.simple_chat.models.User
import java.io.ByteArrayOutputStream

class ProfileSettingsFragment : Fragment(), UriCallBack {

    lateinit var binding: FragmentProfileSettingsBinding
    var imageChanged = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentProfileSettingsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (Datas.currentUser.imageUrl != null) {
            Glide.with(requireActivity()).load(Datas.currentUser.imageUrl).into(binding.circleImageView)
        }
        binding.firstName.setText(Datas.currentUser.firstName)
        binding.lastName.setText(Datas.currentUser.lastName)

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.chooseImage.setOnClickListener {
            chooseImage()
        }

        binding.save.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            binding.save.visibility = View.GONE

            if (Datas.currentUser.firstName == binding.firstName.text.toString() &&
                    Datas.currentUser.lastName == binding.lastName.text.toString() && !imageChanged){
                findNavController().popBackStack()
            }else{
                if (imageChanged){
                    writeImageDatabase()
                }else{
                    saveData()
                }
            }
        }

    }

    private fun saveData(imageUrl:String?=null){
        val user = getUser(imageUrl)
        val memberInfo = getMemberInfo(user)
        val writeChatResMap = HashMap<String, Any>()

        Datas.currentUser.chatIdList.forEachIndexed { index, chatId ->
            writeChatResMap["${Keys.CHATS_INFO_KEY}/$chatId/membersInfoList/${getMemberIndex(index)}"] = memberInfo
        }

        Datas.refUser.child(user.number).setValue(user)
        Datas.refChat.updateChildren(writeChatResMap)

        binding.progressBar.visibility = View.GONE
        binding.save.visibility = View.VISIBLE
        Toast.makeText(requireActivity(), getString(R.string.changes_saved), Toast.LENGTH_SHORT).show()
        findNavController().popBackStack()
    }

    private fun getMemberIndex(index:Int):Int?{
        for (i in Datas.chatInfoList[index].membersInfoList.indices){
            if (Datas.currentUser.number == Datas.chatInfoList[index].membersInfoList[i].number){
                return i
            }
        }
        return null
    }

    private fun getMemberInfo(user: User):MemberInfo{
        return MemberInfo(user.number, user.firstName, user.lastName, user.imageUrl)
    }

    private fun getUser(imageUrl: String?):User{
        val user = Datas.currentUser
        user.firstName = binding.firstName.text.toString()
        user.lastName = binding.lastName.text.toString()
        if (imageUrl != null) {
            user.imageUrl = imageUrl
        }
        return user
    }

    private fun writeImageDatabase(){
        val bitmap: Bitmap = binding.circleImageView.drawable.toBitmap()
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
                saveData(task.result.toString())
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
        startActivityForResult(intentChooser, 1)
    }

    override fun selectedImage(uri: Uri) {
        Glide.with(requireActivity()).load(uri).into(binding.circleImageView)
        imageChanged = true
    }

}