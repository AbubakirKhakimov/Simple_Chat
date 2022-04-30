package com.x.a_technologies.simple_chat.models

import java.io.Serializable

data class ChatInfo(
    var chatId: String = "",
    var lastMessage: Message = Message(),
    var membersInfoList:List<MemberInfo> = ArrayList()
):Serializable
