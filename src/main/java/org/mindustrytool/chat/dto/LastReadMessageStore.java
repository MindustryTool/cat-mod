package org.mindustrytool.chat.dto;

import arc.struct.ObjectMap;
import lombok.Data;

@Data
public class LastReadMessageStore {
    public ObjectMap<String, String> lastReadMessageIds = new ObjectMap<>();
}
