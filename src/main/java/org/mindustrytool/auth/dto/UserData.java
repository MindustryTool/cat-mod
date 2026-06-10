package org.mindustrytool.auth.dto;

import java.util.Comparator;
import java.util.List;

public record UserData(String id, String name, String imageUrl, List<SimpleRole> roles) {

    public SimpleRole getHighestRole() {
        if (roles == null || roles.isEmpty()) return null;
        return roles().stream().max(Comparator.comparingInt(SimpleRole::getLevel)).orElse(null);
    }
}
