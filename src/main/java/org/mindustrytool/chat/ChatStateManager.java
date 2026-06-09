package org.mindustrytool.chat;

import java.util.concurrent.atomic.AtomicBoolean;

import arc.Core;
import arc.Events;
import arc.util.Log;
import arc.util.Timer;
import lombok.RequiredArgsConstructor;
import mindustry.Vars;
import mindustry.core.GameState.State;
import mindustry.game.EventType.ClientServerConnectEvent;
import mindustry.game.EventType.StateChangeEvent;
import mindustry.game.EventType.WorldLoadEndEvent;
import org.mindustrytool.auth.AuthService;

@RequiredArgsConstructor
public class ChatStateManager {
    public static final String MENU_STATE = "menu";
    public static final String SERVER_PREFIX = "server: ";
    public static final String CAMPAIGN_PREFIX = "campaign: ";
    public static final String CUSTOM_GAME_STATE = "custom-game";
    public static final String EDITOR_STATE = "editing: ";

    private final ChatApiClient apiClient;
    private final ChatStreamClient streamClient;
    private final AtomicBoolean isSyncing = new AtomicBoolean(false);

    private String previousState = MENU_STATE;
    private String currentState = MENU_STATE;
    private String syncedState = "";

    public void init() {
        registerStateListeners();
        Timer.schedule(this::refreshCurrentState, 60, 60);
    }

    public void updateState(String state) {
        if (state == null || state.isEmpty()) {
            return;
        }

        if (state.equals(MENU_STATE)) {
            previousState = currentState;
        }

        currentState = state;
        syncState(false);
    }

    public void refreshCurrentState() {
        if (!Vars.state.isMenu() && currentState.equals(MENU_STATE)) {
            currentState = previousState;
        }

        syncState(true);
    }

    private void registerStateListeners() {
        Events.on(ClientServerConnectEvent.class, this::handleServerConnect);
        Events.on(StateChangeEvent.class, this::handleStateChange);
        Events.on(WorldLoadEndEvent.class, event -> handleWorldLoaded());
    }

    private void handleServerConnect(ClientServerConnectEvent event) {
        Vars.net.pingHost(event.ip, event.port, result -> {
            if (result != null) {
                updateState(SERVER_PREFIX + result.name);
            }
        }, e -> Log.err("Failed to ping host", e));
    }

    private void handleStateChange(StateChangeEvent event) {
        if (event.to == State.menu && !Core.graphics.isHidden()) {
            updateState(MENU_STATE);
        } else if (event.to == State.playing && !previousState.isEmpty()) {
            updateState(previousState);
        }
    }

    private void handleWorldLoaded() {
        try {
            if (Vars.net.client()) {
                return;
            }

            if (Vars.state.isCampaign()) {
                updateState(CAMPAIGN_PREFIX + Vars.state.map.name());
                return;
            }

            if (Vars.state.isEditor()) {
                updateState(EDITOR_STATE + Vars.state.map.name());
                return;
            }

            updateState(CUSTOM_GAME_STATE);
        } catch (Exception e) {
            Log.err("Failed to handle state change", e);
            Vars.ui.showInfoFade(e.getMessage());
        }
    }

    private void syncState(boolean force) {
        if (currentState.isEmpty() || !AuthService.getInstance().isLoggedIn() || !ChatConfig.status()
                || !streamClient.isConnected()) {
            return;
        }

        if (!force && currentState.equals(syncedState)) {
            return;
        }

        if (!isSyncing.compareAndSet(false, true)) {
            return;
        }

        String stateToSync = currentState;
        Log.info("Syncing chat state: @", stateToSync);

        apiClient.updateState(stateToSync)
                .thenRun(() -> syncedState = stateToSync)
                .exceptionally(e -> {
                    Log.err("Failed to update chat state", e);
                    return null;
                })
                .whenComplete((ignored, throwable) -> isSyncing.set(false));
    }
}
