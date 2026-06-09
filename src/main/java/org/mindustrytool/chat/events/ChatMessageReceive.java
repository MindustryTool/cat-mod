package org.mindustrytool.chat.events;

import arc.struct.Seq;
import org.mindustrytool.chat.dto.ChatMessage;

public class ChatMessageReceive {
    public final Seq<ChatMessage> messages;

    public ChatMessageReceive(Seq<ChatMessage> messages) {
        this.messages = messages;
    }
}
