package com.x.a_technologies.simple_chat.models

data class ChatInfo(
    var chatId: String = "",
    var lastMessage: Message = Message(),
    var membersInfoList:List<MemberInfo> = ArrayList()
)
