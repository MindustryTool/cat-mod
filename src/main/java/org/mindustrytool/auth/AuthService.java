package org.mindustrytool.auth;

import java.net.SocketTimeoutException;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.NoArgsConstructor;

import arc.Core;
import arc.Events;
import arc.scene.event.Touchable;
import arc.scene.ui.layout.Table;
import arc.util.Align;
import arc.util.Http;
import arc.util.Log;
import arc.util.Timer;
import arc.util.serialization.Jval;
import arc.util.serialization.Json;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.ui.Styles;
import org.mindustrytool.auth.dto.LoginEvent;
import org.mindustrytool.auth.dto.LogoutEvent;
import org.mindustrytool.auth.dto.UserSession;
import org.mindustrytool.libs.ui.components.CustomUIComponent;
import org.mindustrytool.libs.signal.Signal;
import org.mindustrytool.libs.signal.Effect;
import org.mindustrytool.util.AsyncHttp;

@Singleton
@NoArgsConstructor(onConstructor_ = @Inject)
public class AuthService {
    static final String API_URL = "https://api.mindustry-tool.com/api/v4/";
    static final String KEY_ACCESS_TOKEN = "mindustrytool.auth.access-token";
    static final String KEY_REFRESH_TOKEN = "mindustrytool.auth.refresh-token";
    static final String KEY_LOGIN_ID = "mindustrytool.auth.login-id";
    static final String KEY_LOGIN_EXPIRY = "mindustrytool.auth.login-expiry";

    private final Signal<UserSession> sessionSignal = Signal.of(null);
    private final Signal<Boolean> loadingSignal = Signal.of(false);
    private final Signal<Throwable> errorSignal = Signal.of(null);

    private CompletableFuture<Boolean> refreshFuture;
    private CompletableFuture<Void> loginFuture;
    private AuthLoginDialog loginDialog;

    public void init() {
        setupUI();
        checkSavedLogin();

        // Fetch session initially
        fetchSession();

        // Periodically refresh session status
        Timer.schedule(() -> {
            if (isLoggedIn()) {
                fetchSession();
            }
        }, 60 * 5, 60 * 5);
    }

    public Signal<UserSession> session() {
        return sessionSignal;
    }

    public Signal<Boolean> isLoading() {
        return loadingSignal;
    }

    public Signal<Throwable> error() {
        return errorSignal;
    }

    public UserSession getSession() {
        return sessionSignal.get();
    }

    public boolean isLoggedIn() {
        return sessionSignal.get() != null 
                && Core.settings.has(KEY_ACCESS_TOKEN)
                && Core.settings.has(KEY_REFRESH_TOKEN);
    }

    private void setupUI() {
        var wholeViewport = new Table();
        wholeViewport.name = "authWindow";
        wholeViewport.setFillParent(true);
        wholeViewport.top().right();

        Table authWindow = wholeViewport.table().get();
        authWindow.top().right();
        authWindow.touchable = Touchable.childrenOnly;

        Core.app.post(() -> {
            if (Vars.ui != null && Vars.ui.menuGroup != null) {
                Vars.ui.menuGroup.addChild(wholeViewport);
            }
        });

        Table content = new Table();
        content.setBackground(Styles.black6);

        authWindow.add(content).top().right().margin(8f);
        authWindow.toFront();

        // Reactive UI updates on signal changes (runs on Main thread)
        new Effect(() -> {
            UserSession user = sessionSignal.get();
            Throwable error = errorSignal.get();
            boolean isLoading = loadingSignal.get();

            content.clear();
            if (isLoading) {
                content.add(Core.bundle.get("loading", "Loading..."))
                        .wrapLabel(false)
                        .labelAlign(Align.left)
                        .padLeft(8);
            } else if (error != null) {
                content.add(Core.bundle.get("error", "Error"))
                        .labelAlign(Align.left)
                        .padLeft(8);
                content.add(error.getLocalizedMessage())
                        .labelAlign(Align.left)
                        .padLeft(8)
                        .row();
                content.button(Core.bundle.get("retry", "Retry"), Icon.refresh, this::startLoginUI);
                Log.err("Failed to login", error);
            } else if (user == null) {
                content.button(Core.bundle.get("login", "Login"), this::startLoginUI)
                        .wrapLabel(false);
            } else {
                if (user.getImageUrl() != null) {
                    content.add(CustomUIComponent.of()
                            .style(s -> s.loadImage(user.getImageUrl()).radius(6f))
                            .element()).size(64);
                }
                if (!Vars.mobile) {
                    content.add(user.getName())
                            .labelAlign(Align.left)
                            .padLeft(8);
                }
                content.touchable = Touchable.enabled;
                content.clicked(() -> {
                    Vars.ui.showConfirm("Logout", "Logged in as " + user.getName() + "\nDo you want to logout?",
                            this::logout);
                });
            }
            content.pack();
        });
    }

    private void checkSavedLogin() {
        String loginId = Core.settings.getString(KEY_LOGIN_ID);
        if (loginId != null) {
            Instant expiry = Instant.ofEpochMilli(Core.settings.getLong(KEY_LOGIN_EXPIRY, 0));
            if (expiry.isBefore(Instant.now())) {
                Core.settings.remove(KEY_LOGIN_ID);
                Core.settings.remove(KEY_LOGIN_EXPIRY);
            } else {
                pollLoginToken(loginId).exceptionally(e -> {
                    Log.err("Background login polling failed", e);
                    return null;
                });
            }
        }
    }

    private void startLoginUI() {
        login()
                .thenRun(() -> Vars.ui.showInfo("Login successful!"))
                .exceptionally(e -> {
                    Vars.ui.showException("Login failed or timed out.", e);
                    return null;
                });
    }

    public CompletableFuture<UserSession> fetchSession() {
        loadingSignal.set(true);
        errorSignal.set(null);

        CompletableFuture<String> future = new CompletableFuture<>();

        refreshTokenIfNeeded().thenRun(() -> {
            AsyncHttp.get(API_URL + "auth/session")
                .bearerAuth(getAccessToken())
                .timeout(10000)
                .submit(future::complete, future::completeExceptionally);
        }).exceptionally(e -> {
            future.completeExceptionally(e);
            return null;
        });

        return future.handle((json, err) -> {
            if (err != null) {
                loadingSignal.set(false);
                errorSignal.set(err);
                throw new RuntimeException(err);
            }

            UserSession session = json.isEmpty() ? null : new Json().fromJson(UserSession.class, json);
            sessionSignal.set(session);
            loadingSignal.set(false);
            errorSignal.set(null);

            if (session != null) {
                Events.fire(session);
            }
            return session;
        });
    }

    public synchronized CompletableFuture<Void> login() {
        if (loginFuture != null && !loginFuture.isDone()) {
            return loginFuture;
        }

        loginFuture = new CompletableFuture<>();

        if (loginDialog == null) {
            loginDialog = new AuthLoginDialog(this);
        }

        loginDialog.showLoading();
        loginDialog.show();

        AsyncHttp.get(API_URL + "auth/app/login-uri")
            .timeout(10000)
            .submit(
                result -> {
                    try {
                        Jval json = Jval.read(result);
                        String loginUrl = json.getString("loginUrl");
                        String loginId = json.getString("loginId");

                        loginDialog.showLoginUrl(loginUrl);

                        Core.settings.put(KEY_LOGIN_ID, loginId);
                        Core.settings.put(KEY_LOGIN_EXPIRY, Instant.now().plus(Duration.ofMinutes(5)).toEpochMilli());

                        pollLoginToken(loginId).whenComplete((v, e) -> {
                            if (e != null) {
                                loginFuture.completeExceptionally(e);
                            } else {
                                loginFuture.complete(null);
                            }
                            loginDialog.hide();
                        });

                        if (!Core.app.openURI(loginUrl)) {
                            Core.app.setClipboardText(loginUrl);
                        }
                    } catch (Exception e) {
                        loginFuture.completeExceptionally(new RuntimeException("Failed to start login flow", e));
                        loginDialog.hide();
                    }
                },
                err -> {
                    loginDialog.hide();
                    loginFuture.completeExceptionally(new RuntimeException("Failed to get login URI", err));
                }
            );

        return loginFuture;
    }

    void cancelLogin() {
        if (loginFuture != null && !loginFuture.isDone()) {
            loginFuture.completeExceptionally(new RuntimeException("Login cancelled"));
        }
    }

    private CompletableFuture<Void> pollLoginToken(String loginId) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        AsyncHttp.get(API_URL + "auth/app/login-token?loginId=" + loginId)
            .timeout(60 * 1000)
            .submit(
                result -> {
                    try {
                        Core.settings.remove(KEY_LOGIN_ID);
                        Jval json = Jval.read(result);

                        if (json.has("accessToken") && json.has("refreshToken")) {
                            String accessToken = json.getString("accessToken");
                            String refreshToken = json.getString("refreshToken");

                            saveTokens(accessToken, refreshToken);

                            fetchSession().whenComplete((v, e) -> {
                                if (e != null) {
                                    future.completeExceptionally(e);
                                } else {
                                    Events.fire(new LoginEvent());
                                    future.complete(null);
                                }
                            });
                        } else {
                            future.completeExceptionally(new RuntimeException("Invalid response: missing tokens"));
                        }
                    } catch (Exception e) {
                        future.completeExceptionally(e);
                    }
                },
                err -> {
                    if (err instanceof SocketTimeoutException) {
                        future.completeExceptionally(err);
                        return;
                    }
                    Core.settings.remove(KEY_LOGIN_ID);
                    future.completeExceptionally(new RuntimeException("Failed to get login token", err));
                }
            );
        return future;
    }

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

            AsyncHttp.postJson(API_URL + "auth/app/logout", json)
                .bearerAuth(accessToken)
                .timeout(10000)
                .submit(
                    res -> Vars.ui.showInfoFade("Logout successful!"),
                    err -> Vars.ui.showInfo("Logout failed: " + err.getMessage())
                );
        }

        Core.settings.remove(KEY_ACCESS_TOKEN);
        Core.settings.remove(KEY_REFRESH_TOKEN);
        Core.settings.remove(KEY_LOGIN_ID);

        fetchSession();
        Events.fire(new LogoutEvent());
        Log.info("Logged out");
    }

    public String getAccessToken() {
        return Core.settings.getString(KEY_ACCESS_TOKEN, null);
    }

    public String getRefreshToken() {
        return Core.settings.getString(KEY_REFRESH_TOKEN, null);
    }

    public boolean isTokenNearExpiry(String token) {
        if (token == null) {
            return true;
        }

        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                return true;
            }

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

    public synchronized CompletableFuture<Boolean> refreshTokenIfNeeded() {
        if (refreshFuture != null && !refreshFuture.isDone()) {
            return refreshFuture;
        }

        refreshFuture = new CompletableFuture<>();
        String accessToken = getAccessToken();
        String refreshToken = getRefreshToken();

        if (refreshToken == null) {
            refreshFuture.complete(false);
            return refreshFuture;
        }

        if (accessToken != null && !isTokenNearExpiry(accessToken)) {
            refreshFuture.complete(false);
            return refreshFuture;
        }

        if (isTokenNearExpiry(refreshToken)) {
            Log.info("Refresh token near expiry, removed it");
            Core.settings.remove(KEY_REFRESH_TOKEN);
            refreshFuture.complete(false);
            return refreshFuture;
        }

        Jval json = Jval.newObject();
        json.put("refreshToken", refreshToken);

        AsyncHttp.postJson(API_URL + "auth/app/refresh", json)
            .timeout(10000)
            .submit(
                result -> {
                    try {
                        Jval resJson = Jval.read(result);
                        if (resJson.has("accessToken") && resJson.has("refreshToken")) {
                            saveTokens(resJson.getString("accessToken"), resJson.getString("refreshToken"));
                            Log.info("Token refreshed successfully");
                            refreshFuture.complete(true);
                        } else {
                            refreshFuture.completeExceptionally(
                                    new RuntimeException("Invalid refresh response: " + resJson));
                        }
                    } catch (Exception e) {
                        if (e instanceof Http.HttpStatusException httpError) {
                            if (httpError.status.code == 401) {
                                logout();
                            }
                        }
                        refreshFuture.completeExceptionally(new RuntimeException("Failed to refresh token", e));
                    }
                },
                err -> {
                    Log.err("Failed to refresh token", err);
                    if (err instanceof Http.HttpStatusException httpError) {
                        if (httpError.status.code == 401) {
                            Core.settings.remove(KEY_ACCESS_TOKEN);
                            Core.settings.remove(KEY_REFRESH_TOKEN);
                            Log.info("Removed invalid tokens");
                        }
                        Log.err(httpError.response.getResultAsString());
                    }
                    refreshFuture.completeExceptionally(err);
                }
            );

        return refreshFuture;
    }
}
