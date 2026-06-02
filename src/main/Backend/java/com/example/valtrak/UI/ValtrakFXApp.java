package com.example.valtrak.UI;

import com.example.valtrak.ValtrakApplication;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

public class ValtrakFXApp extends Application {

    private ConfigurableApplicationContext springContext;

    @Override
    public void init() {
        springContext = SpringApplication.run(ValtrakApplication.class);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Valtrak");
        primaryStage.setScene(new MainMenuScene(primaryStage).build());
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    @Override
    public void stop() {
        springContext.close();
        Platform.exit();
    }
}
