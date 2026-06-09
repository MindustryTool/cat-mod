package org.mindustrytool.auth.dto;

public record SessionLoadEvent(UserSession user, Throwable error, boolean isLoading) {

}
