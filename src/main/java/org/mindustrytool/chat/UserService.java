package org.mindustrytool.chat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import arc.struct.Seq;
import arc.util.Http;
import arc.util.Timer;
import arc.util.Http.HttpStatusException;
import org.mindustrytool.auth.AuthService;
import org.mindustrytool.auth.dto.UserData;

public class UserService {
    private static final ConcurrentHashMap<String, UserData> cache = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Seq<CompletableFuture<UserData>>> listeners = new ConcurrentHashMap<>();

    static {
        Timer.schedule(UserService::batch, 0, 0.2f);
    }

    private static synchronized void batch() {
        if (listeners.isEmpty()) {
            return;
        }

        List<String> ids = new ArrayList<>();
        var copy = new ConcurrentHashMap<>(listeners);
        listeners.clear();

        copy.forEach((id, callbacks) -> ids.add(id));

        HashMap<String, Object> body = new HashMap<>();
        body.put("ids", ids);

        Http.post(AuthService.API_URL + "users/batches", ChatUtils.toJson(body))
                .header("Content-Type", "application/json")
                .timeout(10000)
                .error(error -> {
                    if (error instanceof HttpStatusException http) {
                        copy.forEach((id, callbacks) -> {
                            for (var callback : callbacks) {
                                callback.completeExceptionally(
                                        new Exception("Failed to get user: " + http.response.getResultAsString()));
                            }
                        });
                    } else {
                        copy.forEach((id, callbacks) -> {
                            for (var callback : callbacks) {
                                callback.completeExceptionally(error);
                            }
                        });
                    }
                })
                .submit(res -> {
                    String data = res.getResultAsString();
                    Seq<UserData> users = ChatUtils.fromJsonArray(UserData.class, data);

                    copy.forEach((id, callbacks) -> {
                        var user = users.find(u -> u.getId().equals(id));
                        if (user != null) {
                            for (var callback : callbacks) {
                                cache.put(id, user);
                                callback.complete(user);
                            }
                        } else {
                            for (var callback : callbacks) {
                                callback.completeExceptionally(new Exception("User not found"));
                            }
                        }
                    });
                });
    }

    public static synchronized CompletableFuture<UserData> findUserById(String id) {
        CompletableFuture<UserData> future = new CompletableFuture<>();
        UserData cached = cache.get(id);

        if (cached != null) {
            future.complete(cached);
            return future;
        }

        var current = listeners.get(id);
        if (current == null) {
            final Seq<CompletableFuture<UserData>> callbacks = Seq.with(future);
            listeners.put(id, callbacks);
        } else {
            current.add(future);
        }

        return future;
    }
}
