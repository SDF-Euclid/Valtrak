package com.example.valtrak;

import com.example.valtrak.UI.ValtrakFXApp;
import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ValtrakApplication {
    public static void main(String[] args) {
        Application.launch(ValtrakFXApp.class, args);
    }
}
