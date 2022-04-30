package com.x.a_technologies.simple_chat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.x.a_technologies.simple_chat.R
import com.x.a_technologies.simple_chat.databinding.FragmentAddChatBinding
import com.x.a_technologies.simple_chat.database.DatabaseRef
import com.x.a_technologies.simple_chat.database.Keys
import com.x.a_technologies.simple_chat.models.*

class AddChatFragment: BottomSheetDialogFragment() {
    
    lateinit var binding: FragmentAddChatBinding
    lateinit var number: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAddChatBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.checkNumber.setOnClickListener {
            number = "+${binding.countryCode.text}${binding.number.text}"

            when {
                number == "+" -> {
                    Toast.makeText(requireActivity(), getString(R.string.enter_a_phone_number), Toast.LENGTH_SHORT).show()
                }
                number == DatabaseRef.currentUser.number -> {
                    Toast.makeText(requireActivity(), getString(R.string.is_your_phone_number), Toast.LENGTH_SHORT).show()
                }
                checkNumberForThisChats() -> {
                    Toast.makeText(requireActivity(), getString(R.string.this_number_is_in_your_chat_list), Toast.LENGTH_SHORT).show()
                }
                else -> {
                    binding.checkNumber.visibility = View.INVISIBLE
                    binding.progressBar.visibility = View.VISIBLE

                    DatabaseRef.usersRef.child(number).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.value == null) {
                                Toast.makeText(requireActivity(), getString(R.string.number_is_not_registered), Toast.LENGTH_SHORT).show()

                                binding.checkNumber.visibility = View.VISIBLE
                                binding.progressBar.visibility = View.INVISIBLE
                            } else {
                                createNewChat(snapshot)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }
                    })
                }
            }

        }
        
    }

    private fun checkNumberForThisChats():Boolean{
        for (chat in DatabaseRef.chatInfoList){
            if (number == getOtherUser(chat.membersInfoList)!!.number){
                return true
            }
        }
        return false
    }
    
    private fun getOtherUser(membersList:List<MemberInfo>):MemberInfo?{
        for (user in membersList){
            if (DatabaseRef.currentUser.number != user.number){
                return user
            }
        }
        return null
    }

    fun createNewChat(snapshot: DataSnapshot){
        val currentChatRef = DatabaseRef.chatsRef.child(Keys.CHATS_INFO_KEY).push()
        val otherUser = snapshot.getValue(User::class.java)!!

        val chatInfo = ChatInfo(
            currentChatRef.key!!,
            Message(),
            listOf(getMembersInfo(DatabaseRef.currentUser), getMembersInfo(otherUser))
        )
        currentChatRef.setValue(chatInfo)

        DatabaseRef.currentUser.chatIdList.add(currentChatRef.key!!)
        otherUser.chatIdList.add(currentChatRef.key!!)

        val childUpdates = hashMapOf<String, Any>(
            "${DatabaseRef.currentUser.number}/${Keys.CHAT_ID_LIST_KEY}" to DatabaseRef.currentUser.chatIdList,
            "$number/${Keys.CHAT_ID_LIST_KEY}" to otherUser.chatIdList,
        )
        DatabaseRef.usersRef.updateChildren(childUpdates)

        binding.checkNumber.visibility = View.VISIBLE
        binding.progressBar.visibility = View.INVISIBLE
        dismiss()
    }

    fun getMembersInfo(user:User):MemberInfo{
        return MemberInfo(user.number, user.firstName, user.lastName, user.imageUrl)
    }

}