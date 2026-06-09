package org.mindustrytool.auth.dto;

import lombok.Data;

@Data
public class SimpleRole {
    private int level;
    private String color;
    private String name;
}
