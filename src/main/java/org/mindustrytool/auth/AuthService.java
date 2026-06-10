package org.mindustrytool.auth;

import arc.Core;
import arc.Events;
import arc.util.Http;
import arc.util.Http.HttpMethod;
import arc.util.Log;
import arc.util.serialization.Jval;
import arc.util.serialization.Json;

import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.NoArgsConstructor;

import org.mindustrytool.auth.AuthState.AuthSnapshot;
import org.mindustrytool.auth.dto.UserSession;
import org.mindustrytool.libs.signal.MultithreadSignal;

import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicBoolean;

@Singleton
@NoArgsConstructor(onConstructor_ = @Inject)
public class AuthService {
    static final String API_URL = "https://api.mindustry-tool.com/api/v4/";
    static final String KEY_ACCESS_TOKEN = "mindustrytool.auth.access-token";
    static final String KEY_REFRESH_TOKEN = "mindustrytool.auth.refresh-token";
    static final String KEY_LOGIN_ID = "mindustrytool.auth.login-id";
    static final String KEY_LOGIN_EXPIRY = "mindustrytool.auth.login-expiry";

    private final MultithreadSignal<AuthSnapshot> stateSignal = new MultithreadSignal<>();
    private final AtomicBoolean loginCancelled = new AtomicBoolean(false);

    public void init() {
        stateSignal.update(AuthSnapshot.idle());
        checkSavedLogin();
        fetchSession();
    }

    public MultithreadSignal<AuthSnapshot> state() {
        return stateSignal;
    }

    public AuthSnapshot snapshot() {
        return stateSignal.state();
    }

    public UserSession getSession() {
        var s = stateSignal.state();
        return s.state() == AuthState.LOGGED_IN ? s.session() : null;
    }

    public boolean isLoggedIn() {
        return stateSignal.state().state() == AuthState.LOGGED_IN;
    }

    // ─── Session ─────────────────────────────────────────────────────────

    public void fetchSession() {
        stateSignal.mutateOnIO(
            s -> s.state() != AuthState.LOADING,
            s -> {
                String token = getAccessToken();
                if (token == null) return AuthSnapshot.idle();

                refreshTokenIfNeeded();
                String freshToken = getAccessToken();
                if (freshToken == null) return AuthSnapshot.idle();

                var ref = new HttpRef[]{new HttpRef(null, null)};
                Http.request(HttpMethod.GET, API_URL + "auth/session")
                    .header("Authorization", "Bearer " + freshToken)
                    .timeout(10000)
                    .error(err -> ref[0] = new HttpRef(null, err))
                    .block(res -> ref[0] = new HttpRef(res.getResultAsString(), null));

                if (ref[0].error != null) return AuthSnapshot.failed(ref[0].error);

                String json = (String) ref[0].result;
                UserSession session = (json == null || json.isEmpty())
                    ? null
                    : new Json().fromJson(UserSession.class, json);

                if (session != null) {
                    Events.fire(session);
                    return AuthSnapshot.loggedIn(session);
                }
                return AuthSnapshot.idle();
            }
        );
    }

    // ─── Login ───────────────────────────────────────────────────────────

    public record LoginHandle(String loginUrl, String loginId) {}

    public LoginHandle startLogin() {
        var ref = new HttpRef[]{new HttpRef(null, null)};
        Http.request(HttpMethod.GET, API_URL + "auth/app/login-uri")
            .timeout(10000)
            .error(err -> ref[0] = new HttpRef(null, err))
            .block(res -> ref[0] = new HttpRef(res.getResultAsString(), null));

        if (ref[0].error != null) throw new RuntimeException("Failed to get login URI", ref[0].error);

        Jval json = Jval.read((String) ref[0].result);
        String loginUrl = json.getString("loginUrl");
        String loginId = json.getString("loginId");

        Core.settings.put(KEY_LOGIN_ID, loginId);
        Core.settings.put(KEY_LOGIN_EXPIRY, Instant.now().plus(Duration.ofMinutes(5)).toEpochMilli());
        Core.settings.forceSave();

        return new LoginHandle(loginUrl, loginId);
    }

    public boolean isLoginCancelled() {
        return loginCancelled.get();
    }

    public void cancelLogin() {
        loginCancelled.set(true);
    }

    public void resetLoginCancel() {
        loginCancelled.set(false);
    }

    public String pollLoginToken(String loginId) {
        var ref = new HttpRef[]{new HttpRef(null, null)};
        Http.request(HttpMethod.GET, API_URL + "auth/app/login-token?loginId=" + loginId)
            .timeout(60 * 1000)
            .error(err -> ref[0] = new HttpRef(null, err))
            .block(res -> ref[0] = new HttpRef(res.getResultAsString(), null));

        if (ref[0].error != null) throw new RuntimeException("Failed to get login token", ref[0].error);

        Core.settings.remove(KEY_LOGIN_ID);
        Jval json = Jval.read((String) ref[0].result);

        if (!json.has("accessToken") || !json.has("refreshToken")) {
            throw new RuntimeException("Invalid response: missing tokens");
        }

        saveTokens(json.getString("accessToken"), json.getString("refreshToken"));
        fetchSession();
        return (String) ref[0].result;
    }

    // ─── Token Management ────────────────────────────────────────────────

    public void saveTokens(String accessToken, String refreshToken) {
        Core.settings.put(KEY_ACCESS_TOKEN, accessToken);
        Core.settings.put(KEY_REFRESH_TOKEN, refreshToken);
        Core.settings.forceSave();
    }

    public void logout() {
        String accessToken = Core.settings.getString(KEY_ACCESS_TOKEN, "");
        String refreshToken = Core.settings.getString(KEY_REFRESH_TOKEN, "");

        if (!accessToken.isEmpty() && !refreshToken.isEmpty()) {
            Jval json = Jval.newObject();
            json.put("accessToken", accessToken);
            json.put("refreshToken", refreshToken);

            Http.request(HttpMethod.POST, API_URL + "auth/app/logout")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + accessToken)
                .header("Accept", "application/json")
                .content(json.toString())
                .timeout(10000)
                .error(err -> {})
                .block(res -> {});
        }

        Core.settings.remove(KEY_ACCESS_TOKEN);
        Core.settings.remove(KEY_REFRESH_TOKEN);
        Core.settings.remove(KEY_LOGIN_ID);

        stateSignal.update(AuthSnapshot.idle());
        Log.info("Logged out");
    }

    public String getAccessToken() {
        return Core.settings.getString(KEY_ACCESS_TOKEN, null);
    }

    public String getRefreshToken() {
        return Core.settings.getString(KEY_REFRESH_TOKEN, null);
    }

    public boolean isTokenNearExpiry(String token) {
        if (token == null) return true;

        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return true;

            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            Jval json = Jval.read(payload);
            long exp = json.getLong("exp", 0);
            long now = System.currentTimeMillis() / 1000;
            return (exp - now) < 60;
        } catch (Exception e) {
            Log.err("Failed to parse token expiry", e);
            return true;
        }
    }

    public void refreshTokenIfNeeded() {
        String accessToken = getAccessToken();
        String refreshToken = getRefreshToken();

        if (refreshToken == null) return;
        if (accessToken != null && !isTokenNearExpiry(accessToken)) return;

        if (isTokenNearExpiry(refreshToken)) {
            Log.info("Refresh token near expiry, removed");
            Core.settings.remove(KEY_REFRESH_TOKEN);
            return;
        }

        Jval json = Jval.newObject();
        json.put("refreshToken", refreshToken);

        var ref = new HttpRef[]{new HttpRef(null, null)};
        Http.request(HttpMethod.POST, API_URL + "auth/app/refresh")
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .content(json.toString())
            .timeout(10000)
            .error(err -> ref[0] = new HttpRef(null, err))
            .block(res -> ref[0] = new HttpRef(res.getResultAsString(), null));

        if (ref[0].error != null) {
            if (ref[0].error instanceof Http.HttpStatusException se && se.status.code == 401) {
                Core.settings.remove(KEY_ACCESS_TOKEN);
                Core.settings.remove(KEY_REFRESH_TOKEN);
                Log.info("Removed invalid tokens on 401");
            }
            Log.err("Failed to refresh token", ref[0].error);
            return;
        }

        Jval resJson = Jval.read((String) ref[0].result);
        if (resJson.has("accessToken") && resJson.has("refreshToken")) {
            saveTokens(resJson.getString("accessToken"), resJson.getString("refreshToken"));
            Log.info("Token refreshed successfully");
        } else {
            Log.err("Invalid refresh response: " + resJson);
        }
    }

    // ─── Internals ───────────────────────────────────────────────────────

    private void checkSavedLogin() {
        String loginId = Core.settings.getString(KEY_LOGIN_ID, null);
        if (loginId == null) return;

        Instant expiry = Instant.ofEpochMilli(Core.settings.getLong(KEY_LOGIN_EXPIRY, 0));
        if (expiry.isBefore(Instant.now())) {
            Core.settings.remove(KEY_LOGIN_ID);
            Core.settings.remove(KEY_LOGIN_EXPIRY);
        }
    }

    private record HttpRef(Object result, Throwable error) {}
}
