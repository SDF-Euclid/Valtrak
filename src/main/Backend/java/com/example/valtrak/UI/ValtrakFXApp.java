package com.example.valtrak.UI;

import com.example.valtrak.Data.GameData.DataTransfer.AccountData.AccountDtos.LoginResponse;
import com.example.valtrak.UI.net.AccountSession;
import com.example.valtrak.UI.net.SavedSession;
import com.example.valtrak.UI.net.ServerApi;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * The desktop client. It holds no game data or rules of its own; everything
 * comes from the game server (see {@link com.example.valtrak.UI.net.ServerApi}).
 */
public class ValtrakFXApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Valtrak");
        var saved = SavedSession.load(ServerApi.BASE_URL);
        AccountSession.setRestoring(saved.isPresent());
        primaryStage.setScene(new MainMenuScene(primaryStage).build());
        primaryStage.setResizable(false);
        primaryStage.show();
        saved.ifPresent(token -> restore(primaryStage, token));
    }

    /** Checks a saved sign-in with the server; the menu updates when it's done. */
    private void restore(Stage stage, String token) {
        Scene menu = stage.getScene();
        Ui.async(() -> ServerApi.fetchProfile(token), profile -> {
            AccountSession.setRestoring(false);
            if (!AccountSession.isSignedIn()) AccountSession.signIn(new LoginResponse(token, profile), true);
            if (stage.getScene() == menu) stage.setScene(new MainMenuScene(stage).build());
        }, err -> {
            AccountSession.setRestoring(false);
            // an expired or revoked sign-in is forgotten; if the server just can't be reached, keep it for next time
            if (err instanceof ServerApi.ApiError api && api.status() == 401) SavedSession.clear();
            else AccountSession.setRestoreOffline(true);
            if (stage.getScene() == menu) stage.setScene(new MainMenuScene(stage).build());
        });
    }
}
