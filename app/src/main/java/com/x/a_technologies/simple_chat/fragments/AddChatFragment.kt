package com.x.a_technologies.simple_chat.fragments

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.x.a_technologies.simple_chat.R
import com.x.a_technologies.simple_chat.database.Constants
import com.x.a_technologies.simple_chat.database.DatabaseRef
import com.x.a_technologies.simple_chat.database.Keys
import com.x.a_technologies.simple_chat.database.UserData
import com.x.a_technologies.simple_chat.databinding.FragmentAddChatBinding
import com.x.a_technologies.simple_chat.databinding.NotRegistredDialogLayoutBinding
import com.x.a_technologies.simple_chat.models.*
import com.x.a_technologies.simple_chat.models.viewModels.MainViewModel
import com.x.a_technologies.simple_chat.utils.LoadingDialogManager
import com.x.a_technologies.simple_chat.utils.PhoneMaskManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddChatFragment : Fragment() {

    private lateinit var binding: FragmentAddChatBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var loadingDialogManager: LoadingDialogManager
    private var phoneMaskPosition: Int? = null
    private lateinit var chatsInfoList: ArrayList<ChatInfo>

    private val phoneMasksList = ArrayList<PhoneMask>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadingDialogManager = LoadingDialogManager(requireActivity())
        chatsInfoList = arguments?.getParcelableArrayList("chatsInfoList")!!
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        setFragmentResultListener("getPhoneMaskPosition") { requestKey, bundle ->
            phoneMaskPosition = bundle.getInt("position")
            updateUI()
        }
        getPhoneMasks()
    }

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

        updateUI()

        binding.countryNameLayout.setOnClickListener {
            findNavController().navigate(R.id.action_addChatFragment_to_selectCountryCodeFragment)
        }

        binding.countryName.setOnClickListener {
            findNavController().navigate(R.id.action_addChatFragment_to_selectCountryCodeFragment)
        }

        binding.backStack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.check.setOnClickListener {
            checkNumber()
        }

        binding.countryCode.addTextChangedListener {
            if (binding.countryCode.isFocused) {
                searchCountryCode(it.toString())
            }
        }

        viewModel.errorData.observe(viewLifecycleOwner){
            Toast.makeText(requireActivity(), getString(R.string.error), Toast.LENGTH_SHORT).show()
            loadingDialogManager.dismissDialog()
        }

    }

    private fun checkNumber(){
        val phoneNumber = "${binding.countryCode.text}${binding.phoneNumber.rawText}"

        when (phoneNumber) {
            "+" -> {
                Toast.makeText(requireActivity(), getString(R.string.enter_a_phone_number), Toast.LENGTH_SHORT).show()
            }
            UserData.currentUser!!.phoneNumber -> {
                Toast.makeText(requireActivity(), getString(R.string.is_your_phone_number), Toast.LENGTH_SHORT).show()
            }
            else -> {
                checkContactNumberForThisChats(phoneNumber)
            }
        }
    }

    private fun checkContactNumberForThisChats(phoneNumber: String){
        for (chat in chatsInfoList){
            if (phoneNumber == getOtherUser(chat.membersInfoList)!!.phoneNumber){
                findNavController().navigate(R.id.action_addChatFragment_to_chatFragment, bundleOf(
                    "selectedChatInfo" to chat
                ))
                return
            }
        }

        checkUserForDatabase(phoneNumber)
    }

    private fun getOtherUser(membersList:List<MemberInfo>):MemberInfo?{
        for (user in membersList){
            if (UserData.currentUser!!.phoneNumber != user.phoneNumber){
                return user
            }
        }
        return null
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
            findNavController().navigate(R.id.action_addChatFragment_to_chatFragment, bundleOf(
                "selectedChatInfo" to chatInfo
            ))
        }
    }

    private fun getMembersInfo(user:User): MemberInfo {
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

    private fun searchCountryCode(code: String){
        lifecycleScope.launch{
            withContext(Dispatchers.Default){
                for ((index, phoneMask) in phoneMasksList.withIndex()){
                    if (phoneMask.countryCode == code){
                        phoneMaskPosition = index
                        break
                    }else if (index == phoneMasksList.size-1){
                        phoneMaskPosition = null
                    }
                }
            }

            updateUI()
        }
    }

    private fun getPhoneMasks(){
        lifecycleScope.launch{
            withContext(Dispatchers.Default){
                phoneMasksList.apply {
                    clear()
                    addAll(PhoneMaskManager().loadPhoneMusk())
                }
            }
        }
    }

    private fun updateUI() {
        if (phoneMaskPosition != null) {
            val item = phoneMasksList[phoneMaskPosition!!]
            binding.countryName.setText(item.name)
            if (!binding.countryCode.isFocused) {
                binding.countryCode.setText(item.countryCode)
            }
            binding.phoneNumber.hint = item.mask
                .replace("0", "-")
                .replace(" ", "-")
            binding.phoneNumber.mask = item.mask
        }else{
            if (binding.countryCode.text.toString() == "+" || binding.countryCode.text!!.isEmpty()){
                binding.countryName.setText(getString(R.string.choose_a_country))
            }else{
                binding.countryName.setText(getString(R.string.no_such_country_code))
            }
            binding.phoneNumber.hint = "--------------------"
            binding.phoneNumber.mask = "00000000000000000000"
        }
    }

}