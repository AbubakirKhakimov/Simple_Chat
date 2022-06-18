package com.x.a_technologies.simple_chat.models

import java.io.Serializable

data class PhoneMask(
    val imageUrl: String,
    val countryCode: String,
    val name: String,
    val mask: String
)