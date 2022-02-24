package com.x.a_technologies.simple_chat.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.x.a_technologies.simple_chat.R
import com.x.a_technologies.simple_chat.adapters.ChatAdapter
import com.x.a_technologies.simple_chat.databinding.FragmentChatBinding
import com.x.a_technologies.simple_chat.datas.Datas
import com.x.a_technologies.simple_chat.models.Keys
import com.x.a_technologies.simple_chat.models.MemberInfo
import com.x.a_technologies.simple_chat.models.Message
import java.util.*
import kotlin.collections.ArrayList

class ChatFragment : Fragment() {

    lateinit var binding: FragmentChatBinding
    lateinit var adapter: ChatAdapter
    private var trackerEventListener: ValueEventListener? = null
    var messageList = ArrayList<Message>()

    var position:Int? = null
    lateinit var otherMemberInfo: MemberInfo
    lateinit var currentChatId:String

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

        position = arguments?.getInt("clickPosition")
        currentChatId = Datas.chatInfoList[position!!].chatId
        otherMemberInfo = getOtherUser(Datas.chatInfoList[position!!].membersInfoList)!!

        binding.name.text = "${otherMemberInfo.firstName} ${otherMemberInfo.lastName}"
        if (otherMemberInfo.imageUrl != null){
            Glide.with(requireActivity()).load(otherMemberInfo.imageUrl).into(binding.profileImage)
        }

        adapter = ChatAdapter(messageList)
        binding.recyclerView.adapter = adapter

        if (trackerEventListener == null) {
            chatTracker()
        }

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

    private fun sendMessage() {
        val messageId = Datas.refChat.push().key!!

        val message = Message(
            messageId,
            Datas.currentUser.number,
            binding.message.text.toString(), Date().time
        )

        val childUpdates = mapOf<String, Any>(
            "${Keys.CHATS_INFO_KEY}/${currentChatId}/lastMessage" to message,
            "${Keys.MESSAGES_KEY}/${currentChatId}/$messageId" to message
        )
        Datas.refChat.updateChildren(childUpdates)

        binding.message.text.clear()
    }

    private fun chatTracker(){
        binding.progressBar.visibility = View.VISIBLE

        trackerEventListener = Datas.refChat.child(Keys.MESSAGES_KEY).child(currentChatId)
            .addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    messageList.clear()
                    for (it in snapshot.children){
                        messageList.add(it.getValue(Message::class.java)!!)
                    }

                    adapter.notifyDataSetChanged()
                    binding.recyclerView.scrollToPosition(messageList.size-1)
                    binding.progressBar.visibility = View.GONE
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.progressBar.visibility = View.GONE
                }
            })
    }

    private fun getOtherUser(usersList:List<MemberInfo>): MemberInfo?{
        for (user in usersList){
            if (user.number != Datas.currentUser.number){
                return user
            }
        }
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        if (trackerEventListener != null) {
            Datas.refChat.child(Keys.MESSAGES_KEY).child(currentChatId)
                .removeEventListener(trackerEventListener!!)
        }
    }

}