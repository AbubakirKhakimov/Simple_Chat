package com.x.a_technologies.simple_chat.fragments

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
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.x.a_technologies.simple_chat.R
import com.x.a_technologies.simple_chat.database.DatabaseRef
import com.x.a_technologies.simple_chat.database.Keys
import com.x.a_technologies.simple_chat.database.UserData
import com.x.a_technologies.simple_chat.databinding.FragmentProfileSettingsBinding
import com.x.a_technologies.simple_chat.models.ChatInfo
import com.x.a_technologies.simple_chat.models.viewModels.MainViewModel
import com.x.a_technologies.simple_chat.models.MemberInfo
import com.x.a_technologies.simple_chat.models.User
import java.io.ByteArrayOutputStream

class ProfileSettingsFragment : Fragment() {

    private lateinit var binding: FragmentProfileSettingsBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var chatsInfoList: ArrayList<ChatInfo>
    private var imageChanged = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        chatsInfoList = arguments?.getParcelableArrayList("chatsInfoList")!!
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
    }

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

        if (UserData.currentUser!!.imageUrl != null) {
            Glide.with(requireActivity()).load(UserData.currentUser!!.imageUrl).into(binding.circleImageView)
        }
        binding.firstName.setText(UserData.currentUser!!.firstName)
        binding.lastName.setText(UserData.currentUser!!.lastName)

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.chooseImage.setOnClickListener {
            showImageChooser()
        }

        binding.save.setOnClickListener {
            if (UserData.currentUser!!.firstName == binding.firstName.text.toString() &&
                UserData.currentUser!!.lastName == binding.lastName.text.toString() && !imageChanged){
                findNavController().popBackStack()
            }else{
                isLoading(true)

                if (imageChanged){
                    writeImageDatabase()
                }else{
                    saveData()
                }
            }
        }

        viewModel.errorData.observe(viewLifecycleOwner){
            Toast.makeText(requireActivity(), getString(R.string.error), Toast.LENGTH_SHORT).show()
            isLoading(false)
        }

    }

    private fun saveData(imageUrl:String? = null){
        val user = getUser(imageUrl)
        val memberInfo = getMemberInfo(user)
        val writeChatResMap = HashMap<String, Any>()

        writeChatResMap["${Keys.USERS_KEY}/${user.phoneNumber}"] = user
        UserData.currentUser!!.chatIdList.forEachIndexed { chatIndex, chatId ->
            writeChatResMap["${Keys.CHATS_KEY}/${Keys.CHATS_INFO_KEY}/$chatId/membersInfoList/${getMemberIndex(chatIndex)}"] = memberInfo
        }

        viewModel.writeInUpdateChildren(DatabaseRef.rootRef, writeChatResMap).observe(viewLifecycleOwner){
            isLoading(false)
            Toast.makeText(requireActivity(), getString(R.string.changes_saved), Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }
    }

    private fun getMemberIndex(chatIndex:Int): Int?{
        chatsInfoList[chatIndex].membersInfoList.forEachIndexed { memberIndex, memberInfo ->
            if (UserData.currentUser!!.phoneNumber == memberInfo.phoneNumber){
                return memberIndex
            }
        }
        return null
    }

    private fun getMemberInfo(user: User):MemberInfo{
        return MemberInfo(user.phoneNumber, user.firstName, user.lastName, user.imageUrl)
    }

    private fun getUser(imageUrl: String?):User{
        val user = UserData.currentUser!!
        user.firstName = binding.firstName.text.toString().trim()
        user.lastName = binding.lastName.text.toString().trim()
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

        viewModel.uploadImage(byteArray).observe(viewLifecycleOwner){
            saveData(it)
        }
    }

    private val imageChooser = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null){
            Glide.with(requireActivity()).load(uri).into(binding.circleImageView)
            imageChanged = true
        }
    }

    private fun showImageChooser(){
        imageChooser.launch("image/*")
    }

    private fun isLoading(bool: Boolean) {
        if (bool) {
            binding.progressBar.visibility = View.VISIBLE
            binding.save.visibility = View.GONE
        } else {
            binding.progressBar.visibility = View.GONE
            binding.save.visibility = View.VISIBLE
        }
    }

}