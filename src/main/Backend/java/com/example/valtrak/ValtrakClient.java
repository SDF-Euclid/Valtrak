package com.example.valtrak;

import com.example.valtrak.UI.ValtrakFXApp;
import javafx.application.Application;

/**
 * Launcher for the desktop client. Kept separate from {@link ValtrakFXApp}
 * so JavaFX starts correctly from a plain classpath.
 */
public class ValtrakClient {
    public static void main(String[] args) {
        Application.launch(ValtrakFXApp.class, args);
    }
}
