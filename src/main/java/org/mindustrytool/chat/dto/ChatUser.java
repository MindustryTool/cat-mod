package org.mindustrytool.chat.dto;

import java.util.List;
import java.util.Optional;
import lombok.Data;

@Data
public class ChatUser {
    private String name;
    private String imageUrl;
    private List<SimpleRole> roles;
    private String state = "";

    public Optional<SimpleRole> getHighestRole() {
        if (roles == null || roles.isEmpty()) {
            return Optional.empty();
        }
        return getRoles().stream().max((a, b) -> Integer.compare(a.getLevel(), b.getLevel()));
    }

    @Data
    public static class SimpleRole {
        private String id;
        private String color;
        private String icon;
        private int level;
    }
}
