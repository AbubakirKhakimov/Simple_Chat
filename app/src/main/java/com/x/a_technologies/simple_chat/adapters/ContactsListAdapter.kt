package com.x.a_technologies.simple_chat.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.x.a_technologies.simple_chat.databinding.ContactsListItemLayoutBinding
import com.x.a_technologies.simple_chat.models.Contact

interface ContactsListAdapterCallBack{
    fun itemSelectedListener(contact: Contact)
}

class ContactsListAdapter(val contactsList: ArrayList<Contact>, val contactsListAdapterCallBack: ContactsListAdapterCallBack)
    : RecyclerView.Adapter<ContactsListAdapter.ItemHolder>() {
    inner class ItemHolder(val binding: ContactsListItemLayoutBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        return ItemHolder(ContactsListItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val item = contactsList[position]
        holder.binding.name.text = item.name
        holder.binding.phoneNumber.text = item.phoneNumber

        holder.binding.root.setOnClickListener {
            contactsListAdapterCallBack.itemSelectedListener(item)
        }

    }

    override fun getItemCount(): Int {
        return contactsList.size
    }
}