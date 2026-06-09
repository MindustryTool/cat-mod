package org.mindustrytool.auth.dto;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import lombok.Data;

import org.mindustrytool.auth.dto.SimpleRole;

@Data
public class UserData {
    private String id;
    private String name;
    private String imageUrl;
    private List<SimpleRole> roles;

    public Optional<SimpleRole> getHighestRole() {
        if (roles == null || roles.isEmpty()) return Optional.empty();

        return getRoles().stream().max(Comparator.comparingInt(SimpleRole::getLevel));
    }
}
