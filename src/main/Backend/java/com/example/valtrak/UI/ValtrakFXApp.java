package com.example.valtrak.UI;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * The desktop client. It holds no game data or rules of its own; everything
 * comes from the game server (see {@link com.example.valtrak.UI.net.ServerApi}).
 */
public class ValtrakFXApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Valtrak");
        primaryStage.setScene(new MainMenuScene(primaryStage).build());
        primaryStage.setResizable(false);
        primaryStage.show();
    }
}
