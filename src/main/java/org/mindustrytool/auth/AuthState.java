package org.mindustrytool.auth;

import org.mindustrytool.auth.dto.UserSession;

public enum AuthState {
    IDLE,
    LOADING,
    LOGGED_IN,
    FAILED;

    public record AuthSnapshot(AuthState state, UserSession session, Throwable error) {
        public static AuthSnapshot idle() {
            return new AuthSnapshot(IDLE, null, null);
        }

        public static AuthSnapshot loading() {
            return new AuthSnapshot(LOADING, null, null);
        }

        public static AuthSnapshot loggedIn(UserSession session) {
            return new AuthSnapshot(LOGGED_IN, session, null);
        }

        public static AuthSnapshot failed(Throwable error) {
            return new AuthSnapshot(FAILED, null, error);
        }
    }
}
