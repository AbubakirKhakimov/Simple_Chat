package com.x.a_technologies.simple_chat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.database.ValueEventListener
import com.x.a_technologies.simple_chat.R
import com.x.a_technologies.simple_chat.adapters.ChatAdapter
import com.x.a_technologies.simple_chat.databinding.FragmentChatBinding
import com.x.a_technologies.simple_chat.database.DatabaseRef
import com.x.a_technologies.simple_chat.database.Keys
import com.x.a_technologies.simple_chat.database.UserData
import com.x.a_technologies.simple_chat.models.ChatInfo
import com.x.a_technologies.simple_chat.models.viewModels.MainViewModel
import com.x.a_technologies.simple_chat.models.MemberInfo
import com.x.a_technologies.simple_chat.models.Message
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions
import java.util.*
import kotlin.collections.ArrayList

class ChatFragment : Fragment() {

    lateinit var binding: FragmentChatBinding
    lateinit var chatAdapter: ChatAdapter
    lateinit var viewModel: MainViewModel
    private var trackerEventListener: ValueEventListener? = null

    lateinit var currentChatInfo: ChatInfo
    lateinit var otherMemberInfo: MemberInfo
    var messagesList = ArrayList<Message>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        currentChatInfo = arguments?.getSerializable("selectedChatInfo") as ChatInfo
        otherMemberInfo = getOtherUser(currentChatInfo.membersInfoList)!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentChatBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initEmoji()
        updateUI()

        if (trackerEventListener == null) {
            initChatTracker()
        }
        initTrackerObservers()

        binding.sendButton.setOnClickListener {
            if (binding.message.text.toString().isEmpty()){
                Toast.makeText(requireActivity(), getString(R.string.please_write_something), Toast.LENGTH_SHORT).show()
            }else{
                sendMessage()
            }
        }

        binding.back.setOnClickListener {
            findNavController().popBackStack()
        }

    }

    private fun initChatTracker(){
        binding.progressBar.visibility = View.VISIBLE
        trackerEventListener = viewModel.initChatTracker(currentChatInfo.chatId)
    }

    private fun initTrackerObservers(){
        viewModel.chatTracker.observe(viewLifecycleOwner){
            messagesList.apply {
                clear()
                addAll(it)
            }

            chatAdapter.notifyDataSetChanged()
            binding.recyclerView.scrollToPosition(messagesList.size-1)
            binding.progressBar.visibility = View.GONE
        }

        viewModel.errorData.observe(viewLifecycleOwner){
            Toast.makeText(requireActivity(), getString(R.string.error), Toast.LENGTH_SHORT).show()
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun updateUI(){
        binding.name.text = "${otherMemberInfo.firstName} ${otherMemberInfo.lastName}"
        if (otherMemberInfo.imageUrl != null){
            Glide.with(requireActivity()).load(otherMemberInfo.imageUrl).into(binding.profileImage)
        }

        chatAdapter = ChatAdapter(messagesList)
        binding.recyclerView.adapter = chatAdapter
    }

    private fun sendMessage() {
        val messageId = DatabaseRef.chatsRef.push().key!!

        val message = Message(
            messageId,
            UserData.currentUser!!.phoneNumber,
            binding.message.text.toString().trim(),
            Date().time
        )

        val childUpdates = mapOf<String, Any>(
            "${Keys.CHATS_INFO_KEY}/${currentChatInfo.chatId}/lastMessage" to message,
            "${Keys.MESSAGES_KEY}/${currentChatInfo.chatId}/$messageId" to message
        )

        viewModel.writeInUpdateChildren(DatabaseRef.chatsRef, childUpdates).observe(viewLifecycleOwner){

        }

        binding.message.text.clear()
    }

    private fun getOtherUser(usersList:List<MemberInfo>): MemberInfo?{
        for (user in usersList){
            if (user.phoneNumber != UserData.currentUser!!.phoneNumber){
                return user
            }
        }
        return null
    }

    private fun initEmoji(){
        val emojIcon = EmojIconActions(requireActivity(), binding.root, binding.message, binding.emojiButton)
        emojIcon.ShowEmojIcon()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (trackerEventListener != null) {
            DatabaseRef.chatsRef.child(Keys.MESSAGES_KEY).child(currentChatInfo.chatId)
                .removeEventListener(trackerEventListener!!)
        }
    }

}