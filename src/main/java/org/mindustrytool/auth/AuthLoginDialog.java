package org.mindustrytool.auth;

import arc.Core;
import mindustry.Vars;
import mindustry.ui.dialogs.BaseDialog;

public class AuthLoginDialog extends BaseDialog {
    public AuthLoginDialog(AuthService authService) {
        super(Core.bundle.get("login", "Login"));
        name = "loginDialog";

        buttons.button(Core.bundle.get("cancel", "Cancel"), () -> {
            authService.cancelLogin();
            hide();
        }).width(230);
    }

    void showLoading() {
        cont.clear();
        cont.add(Core.bundle.get("generate-loading-link", "Generating login link..."));
    }

    void showLoginUrl(String loginUrl) {
        cont.clear();
        cont.button(loginUrl, () -> {
            Core.app.setClipboardText(loginUrl);
            Vars.ui.showInfoFade(Core.bundle.get("copied", "Copied to clipboard!"));
        }).margin(40).growX().wrapLabel(true).fontScale(0.5f);
    }
}
