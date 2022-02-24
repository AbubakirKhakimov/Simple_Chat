package com.x.a_technologies.simple_chat.fragments

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.x.a_technologies.simple_chat.R
import com.x.a_technologies.simple_chat.activities.IntroActivity
import com.x.a_technologies.simple_chat.adapters.ChatsListAdapter
import com.x.a_technologies.simple_chat.adapters.ChatsListCallBack
import com.x.a_technologies.simple_chat.databinding.FragmentChatsSelectBinding
import com.x.a_technologies.simple_chat.datas.Datas
import com.x.a_technologies.simple_chat.models.ChatInfo
import com.x.a_technologies.simple_chat.models.Keys
import com.x.a_technologies.simple_chat.models.User
import de.hdodenhof.circleimageview.CircleImageView

class ChatsSelectFragment : Fragment(), ChatsListCallBack {

    lateinit var binding: FragmentChatsSelectBinding
    lateinit var adapter: ChatsListAdapter
    lateinit var trackerEventListener: ValueEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentChatsSelectBinding.inflate(layoutInflater)
        binding.progressBar.visibility = View.VISIBLE

        chatInfoTracker()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ChatsListAdapter(requireActivity(), this)
        binding.recyclerView.adapter = adapter
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

    private fun openAlertDialog(){
        val alertDialog = AlertDialog.Builder(requireActivity()).create()
        val layout = layoutInflater.inflate(R.layout.sign_out_alert_dialog, null)
        alertDialog.setView(layout)

        val yesButton = layout.findViewById<MaterialButton>(R.id.yesButton)
        val noButton = layout.findViewById<MaterialButton>(R.id.noButton)

        yesButton.setOnClickListener {
            Datas.chatInfoList.clear()
            Datas.currentUser = User()
            Datas.auth.signOut()

            startActivity(Intent(requireActivity(), IntroActivity::class.java))
            requireActivity().finish()
            alertDialog.dismiss()
        }

        noButton.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    private fun chatInfoTracker(){
        trackerEventListener = Datas.refChat.child(Keys.CHATS_INFO_KEY)
            .addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                updateRes(snapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                binding.progressBar.visibility = View.GONE
            }
        })
    }

    fun updateRes(snapshot: DataSnapshot){
        Datas.refUser.child(Datas.currentUser.number).get().addOnSuccessListener {
            if (it.value != null) {
                Datas.currentUser = it.getValue(User::class.java)!!

                Datas.chatInfoList.clear()
                for (chatId in Datas.currentUser.chatIdList){
                    val chatInfo = snapshot.child(chatId).getValue(ChatInfo::class.java)!!
                    Datas.chatInfoList.add(chatInfo)
                }
                adapter.notifyDataSetChanged()
                binding.progressBar.visibility = View.GONE

                if (Datas.chatInfoList.size == 0){
                    binding.adviceTitle.visibility = View.VISIBLE
                }else{
                    binding.adviceTitle.visibility = View.GONE
                }
            }
        }
    }

    override fun itemClick(position: Int) {
        findNavController().navigate(R.id.action_chatsSelectFragment_to_chatFragment, bundleOf(
            "clickPosition" to position
        ))
    }

    private fun navHeaderLayoutRes(){
        val headerView = binding.navDrawerView.getHeaderView(0)
        val name = headerView.findViewById<TextView>(R.id.profileName)
        val number = headerView.findViewById<TextView>(R.id.phoneNumber)
        val profileImage = headerView.findViewById<CircleImageView>(R.id.profile_image)

        val currentUser = Datas.currentUser
        name.text = "${currentUser.firstName} ${currentUser.lastName}"
        number.text = currentUser.number
        if (currentUser.imageUrl != null) {
            Glide.with(requireActivity()).load(currentUser.imageUrl).into(profileImage)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Datas.refChat.child(Keys.CHATS_INFO_KEY).removeEventListener(trackerEventListener)
    }

}