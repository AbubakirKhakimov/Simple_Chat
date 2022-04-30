package com.x.a_technologies.simple_chat.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.ValueEventListener
import com.x.a_technologies.simple_chat.R
import com.x.a_technologies.simple_chat.activities.IntroActivity
import com.x.a_technologies.simple_chat.adapters.ChatsListAdapter
import com.x.a_technologies.simple_chat.adapters.ChatsListCallBack
import com.x.a_technologies.simple_chat.databinding.FragmentChatsSelectBinding
import com.x.a_technologies.simple_chat.database.DatabaseRef
import com.x.a_technologies.simple_chat.models.ChatInfo
import com.x.a_technologies.simple_chat.database.Keys
import com.x.a_technologies.simple_chat.models.MainViewModel
import com.x.a_technologies.simple_chat.models.User
import de.hdodenhof.circleimageview.CircleImageView

class ChatsSelectFragment : Fragment(), ChatsListCallBack {

    lateinit var binding: FragmentChatsSelectBinding
    lateinit var viewModel: MainViewModel
    lateinit var chatsListAdapter: ChatsListAdapter
    private var trackerEventListener: ValueEventListener? = null

    val chatsInfoList = ArrayList<ChatInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        initObservers()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentChatsSelectBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (trackerEventListener == null) {
            initChatsSelectTracker()
        }

        chatsListAdapter = ChatsListAdapter(chatsInfoList, requireActivity(), this)
        binding.recyclerView.adapter = chatsListAdapter
        binding.navDrawerView.itemIconTintList = null

        navHeaderLayoutRes()

        binding.drawerMenuImg.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        binding.addChat.setOnClickListener {
            AddChatFragment().show(requireActivity().supportFragmentManager, tag)
        }

        binding.navDrawerView.setNavigationItemSelectedListener {
            binding.drawerLayout.closeDrawers()
            when(it.itemId){
                R.id.profileSettings -> {
                    findNavController().navigate(R.id.action_chatsSelectFragment_to_profileSettingsFragment)
                }
                R.id.changeLanguage -> {
                    ChangeLanguageFragment().show(requireActivity().supportFragmentManager, tag)
                }
                R.id.signOut -> {
                    openAlertDialog()
                }
            }
            true
        }
    }

    private fun initObservers(){
        viewModel.chatsSelectTracker.observe(this){
            chatsInfoList.apply {
                clear()
                addAll(it)
            }

            chatsListAdapter.notifyDataSetChanged()
            binding.progressBar.visibility = View.GONE

            if (chatsInfoList.size == 0){
                binding.adviceTitle.visibility = View.VISIBLE
            }else{
                binding.adviceTitle.visibility = View.GONE
            }
        }

        viewModel.errorData.observe(this){
            Toast.makeText(requireActivity(), getString(R.string.error), Toast.LENGTH_SHORT).show()
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun initChatsSelectTracker(){
        binding.progressBar.visibility = View.VISIBLE
        trackerEventListener = viewModel.initChatsSelectTracker()
    }

    private fun openAlertDialog(){
        val alertDialog = AlertDialog.Builder(requireActivity()).create()
        val layout = layoutInflater.inflate(R.layout.sign_out_alert_dialog, null)
        alertDialog.setView(layout)

        val yesButton = layout.findViewById<MaterialButton>(R.id.yesButton)
        val noButton = layout.findViewById<MaterialButton>(R.id.noButton)

        yesButton.setOnClickListener {
            DatabaseRef.currentUser = User()
            DatabaseRef.auth.signOut()

            startActivity(Intent(requireActivity(), IntroActivity::class.java))
            requireActivity().finish()
            alertDialog.dismiss()
        }

        noButton.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    override fun itemClick(position: Int) {
        findNavController().navigate(R.id.action_chatsSelectFragment_to_chatFragment, bundleOf(
            "selectedChatInfo" to chatsInfoList[position]
        ))
    }

    private fun navHeaderLayoutRes(){
        val headerView = binding.navDrawerView.getHeaderView(0)
        val name = headerView.findViewById<TextView>(R.id.profileName)
        val number = headerView.findViewById<TextView>(R.id.phoneNumber)
        val profileImage = headerView.findViewById<CircleImageView>(R.id.profile_image)

        val currentUser = DatabaseRef.currentUser
        name.text = "${currentUser.firstName} ${currentUser.lastName}"
        number.text = currentUser.number
        if (currentUser.imageUrl != null) {
            Glide.with(requireActivity()).load(currentUser.imageUrl).into(profileImage)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (trackerEventListener != null){
            DatabaseRef.chatsRef.child(Keys.CHATS_INFO_KEY)
                .removeEventListener(trackerEventListener!!)
        }
    }

}