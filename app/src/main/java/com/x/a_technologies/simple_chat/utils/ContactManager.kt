package com.x.a_technologies.simple_chat.utils

import android.content.Context
import android.provider.ContactsContract
import com.x.a_technologies.simple_chat.models.Contact

class ContactManager(private val context: Context) {

    fun readPhoneContacts(): ArrayList<Contact> {
        val list = ArrayList<Contact>()

        val contacts = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )

        while (contacts!!.moveToNext()) {
            val contact = Contact(
                contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)),
                contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            )
            list.add(contact)
        }

        contacts.close()
        return list

    }

}