package org.mindustrytool.chat.events;

public class ChatStateChange {
    public final boolean connected;

    public ChatStateChange(boolean connected) {
        this.connected = connected;
    }
}
