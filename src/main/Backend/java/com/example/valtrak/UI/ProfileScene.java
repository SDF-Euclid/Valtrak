package com.example.valtrak.UI;

import com.example.valtrak.Data.GameData.DataTransfer.AccountData.AccountDtos.NationDto;
import com.example.valtrak.Data.GameData.DataTransfer.AccountData.AccountDtos.ProfileDto;
import com.example.valtrak.UI.net.AccountSession;
import com.example.valtrak.UI.net.ServerApi;
import com.example.valtrak.UI.net.ServerApi.ApiError;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/** View and customize the signed-in player's profile (display name and nation). */
public class ProfileScene {

    private final Stage stage;

    public ProfileScene(Stage stage) {
        this.stage = stage;
    }

    public Scene build() {
        ProfileDto profile = AccountSession.profile();

        Label email = Ui.body("Signed in as " + profile.email());
        TextField name = Ui.field("Display name");
        name.setText(profile.displayName());

        ComboBox<String> nation = new ComboBox<>();
        nation.setMinWidth(320);
        nation.setMaxWidth(320);
        nation.setPromptText("Loading nations...");
        nation.setDisable(true);
        Ui.styleCombo(nation);

        Label status = new Label();
        status.setWrapText(true);
        status.setMaxWidth(380);

        Button save = Ui.button("SAVE CHANGES", 220);
        save.setDisable(true);

        Ui.async(() -> ServerApi.fetchNations().stream().map(NationDto::name).toList(),
                names -> {
                    nation.getItems().setAll(names);
                    nation.setValue(profile.nation());
                    nation.setDisable(false);
                    save.setDisable(false);
                },
                err -> Ui.message(status, err.getMessage(), true));

        save.setOnAction(e -> {
            save.setDisable(true);
            Ui.message(status, "Saving...", false);
            Ui.async(() -> ServerApi.updateProfile(name.getText(), nation.getValue()),
                    updated -> {
                        AccountSession.updateProfile(updated);
                        save.setDisable(false);
                        Ui.message(status, "Saved.", false);
                    },
                    err -> {
                        save.setDisable(false);
                        if (err instanceof ApiError api && api.status() == 401) {
                            AccountSession.signOut();
                            goHome();
                        } else {
                            Ui.message(status, err.getMessage(), true);
                        }
                    });
        });

        Button signOut = Ui.button("SIGN OUT", 220);
        signOut.setOnAction(e -> {
            signOut.setDisable(true);
            // sign out locally even if the server can't be reached
            Ui.async(() -> { ServerApi.logout(); return true; },
                    ok -> finishSignOut(), err -> finishSignOut());
        });

        Button back = Ui.link("Back to menu");
        back.setOnAction(e -> goHome());

        Label nationLabel = new Label("Your nation (shown on your profile):");
        nationLabel.setStyle("-fx-text-fill: " + Ui.DIM + "; -fx-font-size: 11px;");

        VBox root = new VBox(12, Ui.heading("MY ACCOUNT", 26), email,
                Ui.body("Display name"), name, nationLabel, nation, save, status, signOut, back);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(36, 40, 24, 40));
        root.setStyle("-fx-background-color: " + Ui.BG + ";");
        return new Scene(root, MainMenuScene.WIDTH, MainMenuScene.HEIGHT);
    }

    private void finishSignOut() {
        AccountSession.signOut();
        goHome();
    }

    private void goHome() {
        stage.setScene(new MainMenuScene(stage).build());
    }
}
