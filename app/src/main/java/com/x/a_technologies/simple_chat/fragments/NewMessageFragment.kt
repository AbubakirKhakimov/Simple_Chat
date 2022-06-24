package com.x.a_technologies.simple_chat.fragments

import android.app.AlertDialog
import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.x.a_technologies.simple_chat.R
import com.x.a_technologies.simple_chat.adapters.ContactsListAdapter
import com.x.a_technologies.simple_chat.adapters.ContactsListAdapterCallBack
import com.x.a_technologies.simple_chat.database.Constants
import com.x.a_technologies.simple_chat.database.DatabaseRef
import com.x.a_technologies.simple_chat.database.Keys
import com.x.a_technologies.simple_chat.database.UserData
import com.x.a_technologies.simple_chat.databinding.FragmentNewMessageBinding
import com.x.a_technologies.simple_chat.databinding.NotRegistredDialogLayoutBinding
import com.x.a_technologies.simple_chat.models.*
import com.x.a_technologies.simple_chat.models.viewModels.MainViewModel
import com.x.a_technologies.simple_chat.utils.ContactManager
import com.x.a_technologies.simple_chat.utils.LoadingDialogManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class NewMessageFragment: Fragment(), ContactsListAdapterCallBack {
    
    private lateinit var binding: FragmentNewMessageBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var loadingDialogManager: LoadingDialogManager
    private lateinit var contactsListAdapter: ContactsListAdapter
    private lateinit var chatsInfoList: ArrayList<ChatInfo>

    private var contactsList = ArrayList<Contact>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        chatsInfoList = arguments?.getParcelableArrayList("chatsInfoList")!!
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        loadingDialogManager = LoadingDialogManager(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentNewMessageBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        contactsListAdapter = ContactsListAdapter(contactsList, this)
        binding.contactsRv.adapter = contactsListAdapter

        lifecycleScope.launch{
            readAllContacts()
        }

        binding.backStack.setOnClickListener {
            findNavController().popBackStack()
        }

        viewModel.errorData.observe(viewLifecycleOwner){
            Toast.makeText(requireActivity(), getString(R.string.error), Toast.LENGTH_SHORT).show()
            loadingDialogManager.dismissDialog()
        }

        binding.addChat.setOnClickListener {
            findNavController().navigate(R.id.action_newMessageFragment_to_addChatFragment, bundleOf(
                "chatsInfoList" to chatsInfoList
            ))
        }
        
    }

    override fun itemSelectedListener(contact: Contact) {
        checkContactNumberForThisChats(contact)
    }

    private fun checkContactNumberForThisChats(contact: Contact){
        for (chat in chatsInfoList){
            if (contact.phoneNumber == getOtherUser(chat.membersInfoList)!!.phoneNumber){
                findNavController().navigate(R.id.action_newMessageFragment_to_chatFragment, bundleOf(
                    "selectedChatInfo" to chat
                ))
                return
            }
        }

        checkUserForDatabase(contact.phoneNumber)
    }

    private suspend fun readAllContacts(){
        withContext(Dispatchers.IO) {
            contactsList.apply {
                clear()
                addAll(ContactManager(requireContext()).readPhoneContacts())
            }
        }

        contactsListAdapter.notifyDataSetChanged()
    }

    private fun checkUserForDatabase(phoneNumber: String){
        loadingDialogManager.showDialog()

        viewModel.checkUser(phoneNumber).apply {
            observe(viewLifecycleOwner){
                if (it == null) {
                    loadingDialogManager.dismissDialog()
                    showNotRegisteredDialog(phoneNumber)
                } else {
                    createNewChat(it)
                }

                removeObservers(viewLifecycleOwner)
            }
        }
    }
    
    private fun getOtherUser(membersList:List<MemberInfo>):MemberInfo?{
        for (user in membersList){
            if (UserData.currentUser!!.phoneNumber != user.phoneNumber){
                return user
            }
        }
        return null
    }

    private fun createNewChat(otherUser: User){
        val currentChatKey = DatabaseRef.chatsRef.child(Keys.CHATS_INFO_KEY).push().key

        val chatInfo = ChatInfo(
            currentChatKey!!,
            Message(),
            listOf(getMembersInfo(UserData.currentUser!!), getMembersInfo(otherUser))
        )
        UserData.currentUser!!.chatIdList.add(currentChatKey)
        otherUser.chatIdList.add(currentChatKey)

        val childUpdates = hashMapOf<String, Any>(
            "${Keys.CHATS_KEY}/${Keys.CHATS_INFO_KEY}/${currentChatKey}" to chatInfo,
            "${Keys.USERS_KEY}/${UserData.currentUser!!.phoneNumber}/${Keys.CHAT_ID_LIST_KEY}" to UserData.currentUser!!.chatIdList,
            "${Keys.USERS_KEY}/${otherUser.phoneNumber}/${Keys.CHAT_ID_LIST_KEY}" to otherUser.chatIdList
        )

        viewModel.writeInUpdateChildren(DatabaseRef.rootRef, childUpdates).observe(viewLifecycleOwner){
            loadingDialogManager.dismissDialog()
            findNavController().navigate(R.id.action_newMessageFragment_to_chatFragment, bundleOf(
                "selectedChatInfo" to chatInfo
            ))
        }
    }

    private fun getMembersInfo(user:User): MemberInfo{
        return MemberInfo(user.phoneNumber, user.firstName, user.lastName, user.imageUrl)
    }

    private fun showNotRegisteredDialog(phoneNumber: String){
        val customDialog = AlertDialog.Builder(requireActivity()).create()
        val dialogBinding = NotRegistredDialogLayoutBinding.inflate(layoutInflater)
        customDialog.setView(dialogBinding.root)

        dialogBinding.invite.setOnClickListener {
            openSendSMSApp(phoneNumber)
            customDialog.dismiss()
        }

        dialogBinding.cancel.setOnClickListener {
            customDialog.dismiss()
        }

        customDialog.show()
    }

    private fun openSendSMSApp(phoneNumber: String){
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:$phoneNumber"))
        intent.putExtra("sms_body", "${getString(R.string.invite_sms_text)} ${Constants.APP_REFERENCE}")
        startActivity(intent)
    }

}