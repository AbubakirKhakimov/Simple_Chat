package com.x.a_technologies.simple_chat.fragments

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.x.a_technologies.simple_chat.R
import com.x.a_technologies.simple_chat.activities.IntroActivity
import com.x.a_technologies.simple_chat.adapters.ChatsListAdapter
import com.x.a_technologies.simple_chat.adapters.ChatsListCallBack
import com.x.a_technologies.simple_chat.databinding.FragmentChatsSelectBinding
import com.x.a_technologies.simple_chat.database.DatabaseRef
import com.x.a_technologies.simple_chat.models.ChatInfo
import com.x.a_technologies.simple_chat.database.Keys
import com.x.a_technologies.simple_chat.database.UserData
import com.x.a_technologies.simple_chat.models.viewModels.MainViewModel
import de.hdodenhof.circleimageview.CircleImageView

class ChatsSelectFragment : Fragment(), ChatsListCallBack {

    lateinit var binding: FragmentChatsSelectBinding
    lateinit var viewModel: MainViewModel
    lateinit var chatsListAdapter: ChatsListAdapter
    private var trackerEventListener: ValueEventListener? = null
    private var dataLoaded = false

    private val chatsInfoList = ArrayList<ChatInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
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
        initTrackerObservers()

        chatsListAdapter = ChatsListAdapter(chatsInfoList, requireActivity(), this)
        binding.recyclerView.adapter = chatsListAdapter
        binding.navDrawerView.itemIconTintList = null

        navHeaderLayoutRes()

        binding.drawerMenuImg.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        binding.navDrawerView.setNavigationItemSelectedListener {
            binding.drawerLayout.closeDrawers()
            when(it.itemId){
                R.id.profileSettings -> {
                    findNavController().navigate(R.id.action_chatsSelectFragment_to_profileSettingsFragment, bundleOf(
                        "chatsInfoList" to chatsInfoList
                    ))
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

        binding.newMessage.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED){
                navigateToNewMessage()
            }else{
                requestContactPermission()
            }
        }

    }

    private val contactPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()){
        if (it){
            navigateToNewMessage()
        }
    }

    private fun requestContactPermission(){
        contactPermission.launch(Manifest.permission.READ_CONTACTS)
    }

    private fun navigateToNewMessage(){
        findNavController().navigate(R.id.action_chatsSelectFragment_to_newMessageFragment, bundleOf(
            "chatsInfoList" to chatsInfoList
        ))
    }

    private fun initChatsSelectTracker(){
        runLoadingAnim(true)
        trackerEventListener = viewModel.initChatsSelectTracker()
    }

    private fun initTrackerObservers(){
        viewModel.chatsSelectTracker.observe(viewLifecycleOwner){
            chatsInfoList.apply {
                clear()
                addAll(it)
            }

            chatsListAdapter.notifyDataSetChanged()
            runLoadingAnim(false)

            if (chatsInfoList.size == 0){
                binding.adviceTitle.visibility = View.VISIBLE
            }else{
                binding.adviceTitle.visibility = View.GONE
            }
        }

        viewModel.errorData.observe(viewLifecycleOwner){
            Toast.makeText(requireActivity(), getString(R.string.error), Toast.LENGTH_SHORT).show()
            runLoadingAnim(false)
        }
    }

    private fun openAlertDialog(){
        val alertDialog = AlertDialog.Builder(requireActivity()).create()
        val layout = layoutInflater.inflate(R.layout.sign_out_alert_dialog, null)
        alertDialog.setView(layout)

        val yesButton = layout.findViewById<MaterialButton>(R.id.yesButton)
        val noButton = layout.findViewById<MaterialButton>(R.id.noButton)

        yesButton.setOnClickListener {
            UserData.currentUser = null
            Firebase.auth.signOut()

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

        val currentUser = UserData.currentUser!!
        name.text = "${currentUser.firstName} ${currentUser.lastName}"
        number.text = currentUser.phoneNumber
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

    private fun runLoadingAnim(isLoading: Boolean){
        val animTextDownMiddle = AnimationUtils.loadAnimation(requireActivity(), R.anim.text_down_middle_anim)

        if (isLoading){
            binding.title.text = getString(R.string.loading)
            binding.title.startAnimation(animTextDownMiddle)
        }else{
            if (!dataLoaded) {
                binding.title.text = getString(R.string.app_name)
                binding.title.startAnimation(animTextDownMiddle)
                dataLoaded = true
            }
        }
    }

}