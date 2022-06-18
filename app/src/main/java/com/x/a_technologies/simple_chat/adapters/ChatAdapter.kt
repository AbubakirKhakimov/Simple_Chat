package com.x.a_technologies.simple_chat.adapters

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.x.a_technologies.simple_chat.databinding.ChatItemLayoutBinding
import com.x.a_technologies.simple_chat.database.UserData
import com.x.a_technologies.simple_chat.models.Message
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter(val messagesList:List<Message>):RecyclerView.Adapter<ChatAdapter.ItemHolder>() {

    inner class ItemHolder(val binding: ChatItemLayoutBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        return ItemHolder(ChatItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val item = messagesList[position]

        holder.binding.linerLayout.layoutParams = getParams(item)
        holder.binding.message.text = item.massage
        holder.binding.dateAndTime.text = getDateAndTime(item.sendTime)
    }

    override fun getItemCount(): Int {
        return messagesList.size
    }

    private fun getParams(item:Message):ViewGroup.LayoutParams{
        val params = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,FrameLayout.LayoutParams.WRAP_CONTENT)
        if (item.number == UserData.currentUser!!.phoneNumber){
            params.gravity = Gravity.END
            params.marginStart = 200
        }else{
            params.gravity = Gravity.START
            params.marginEnd = 200
        }
        return params
    }

    private fun getDateAndTime(longTime: Long):String{
        return SimpleDateFormat("dd MMM HH:mm").format(Date(longTime))
    }

}