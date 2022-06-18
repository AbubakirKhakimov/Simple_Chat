package com.x.a_technologies.simple_chat.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.x.a_technologies.simple_chat.R
import com.x.a_technologies.simple_chat.databinding.ChatsListItemLayoutBinding
import com.x.a_technologies.simple_chat.database.UserData
import com.x.a_technologies.simple_chat.models.ChatInfo
import com.x.a_technologies.simple_chat.models.MemberInfo
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

interface ChatsListCallBack{
    fun itemClick(position: Int)
}

class ChatsListAdapter(val chatsInfoList: ArrayList<ChatInfo>, val context:Context, val chatsListCallBack: ChatsListCallBack)
    :RecyclerView.Adapter<ChatsListAdapter.ItemHolder>() {
    inner class ItemHolder(val binding: ChatsListItemLayoutBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        return ItemHolder(ChatsListItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val item = chatsInfoList[position]
        val otherUser = getOtherUser(item.membersInfoList)!!

        holder.binding.name.text = "${otherUser.firstName} ${otherUser.lastName}"
        holder.binding.lastMessage.text = item.lastMessage.massage
        holder.binding.lastMessageTime.text =
            if (item.lastMessage.sendTime == 0L){
                ""
            }else {
                getTime(item.lastMessage.sendTime)
            }
        holder.binding.lastMessageDate.text =
            if (item.lastMessage.sendTime == 0L){
                ""
            }else {
                getDate(item.lastMessage.sendTime)
            }
        if (otherUser.imageUrl != null) {
            Glide.with(context).load(otherUser.imageUrl).into(holder.binding.profileImage)
        }else{
            holder.binding.profileImage.setImageResource(R.drawable.user_profile)
        }

        holder.binding.linerLayout.setOnClickListener {
            chatsListCallBack.itemClick(position)
        }

    }

    override fun getItemCount(): Int {
        return chatsInfoList.size
    }

    private fun getTime(longTime: Long):String{
        return SimpleDateFormat("HH:mm").format(Date(longTime))
    }

    private fun getDate(longTime: Long):String{
        return SimpleDateFormat("dd MMM").format(Date(longTime))
    }

    private fun getOtherUser(usersList:List<MemberInfo>):MemberInfo?{
        for (user in usersList){
            if (user.phoneNumber != UserData.currentUser!!.phoneNumber){
                return user
            }
        }
        return null
    }
}