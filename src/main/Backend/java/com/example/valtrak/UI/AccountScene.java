package com.example.valtrak.UI;

import com.example.valtrak.Data.GameData.DataTransfer.AccountData.AccountDtos.NationDto;
import com.example.valtrak.UI.net.AccountSession;
import com.example.valtrak.UI.net.ServerApi;
import com.example.valtrak.UI.net.ServerApi.ApiError;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.List;

/**
 * Sign in, create account, and email verification. Accounts are optional:
 * "Continue as guest" always returns to the main menu.
 */
public class AccountScene {

    private static final String DEFAULT_NATION = "United States";
    private static final int RESEND_SECONDS = 60;

    private final Stage stage;
    private final VBox content = new VBox(12);
    private List<String> nationNames;
    private Timeline resendTimer;

    public AccountScene(Stage stage) {
        this.stage = stage;
    }

    public Scene build() {
        Label logo = new Label("VALTRAK");
        logo.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        logo.setStyle("-fx-text-fill: " + Ui.ACCENT + ";");

        content.setAlignment(Pos.CENTER);
        VBox root = new VBox(14, logo, content);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(28, 40, 24, 40));
        root.setStyle("-fx-background-color: " + Ui.BG + ";");
        showSignIn("", null);
        return new Scene(root, MainMenuScene.WIDTH, MainMenuScene.HEIGHT);
    }

    private void goHome() {
        stopTimer();
        stage.setScene(new MainMenuScene(stage).build());
    }

    private void stopTimer() {
        if (resendTimer != null) resendTimer.stop();
    }

    // ── Sign in ──────────────────────────────────────────────────────────────

    private void showSignIn(String prefillEmail, String notice) {
        stopTimer();
        TextField email = Ui.field("Email");
        email.setText(prefillEmail);
        PasswordField password = Ui.password("Password");
        Label status = new Label();
        status.setWrapText(true);
        status.setMaxWidth(380);
        if (notice != null) Ui.message(status, notice, false);

        Button signIn = Ui.button("SIGN IN", 220);
        Runnable submit = () -> {
            signIn.setDisable(true);
            Ui.message(status, "Signing in...", false);
            Ui.async(() -> ServerApi.login(email.getText(), password.getText()),
                    login -> {
                        AccountSession.signIn(login);
                        goHome();
                    },
                    err -> {
                        signIn.setDisable(false);
                        if (err instanceof ApiError api && api.status() == 403) {
                            showVerify(email.getText().trim(), password.getText(), api.getMessage());
                        } else {
                            Ui.message(status, err.getMessage(), true);
                        }
                    });
        };
        signIn.setOnAction(e -> submit.run());
        password.setOnAction(e -> submit.run());

        Button create = Ui.link("New here? Create an account");
        create.setOnAction(e -> showCreate());
        Button guest = Ui.link("Continue as guest");
        guest.setOnAction(e -> goHome());

        content.getChildren().setAll(Ui.heading("SIGN IN", 20),
                Ui.body("An account lets you save favorites and customize your profile."),
                email, password, signIn, status, create, guest);
    }

    // ── Create account ───────────────────────────────────────────────────────

    private void showCreate() {
        stopTimer();
        TextField email = Ui.field("Email");
        TextField name = Ui.field("Display name (3-20 characters)");
        PasswordField password = Ui.password("Password (8+ characters)");
        PasswordField confirm = Ui.password("Confirm password");
        ComboBox<String> nation = new ComboBox<>();
        nation.setPromptText("Loading nations...");
        nation.setDisable(true);
        Ui.styleCombo(nation);
        nation.setMaxWidth(320);
        nation.setMinWidth(320);
        Label status = new Label();
        status.setWrapText(true);
        status.setMaxWidth(380);

        Button create = Ui.button("CREATE ACCOUNT", 220);
        create.setDisable(true);
        loadNations(nation, create, status);

        create.setOnAction(e -> {
            if (!password.getText().equals(confirm.getText())) {
                Ui.message(status, "Passwords don't match.", true);
                return;
            }
            create.setDisable(true);
            Ui.message(status, "Creating account...", false);
            Ui.async(() -> ServerApi.register(email.getText(), name.getText(), password.getText(), nation.getValue()),
                    reply -> showVerify(email.getText().trim().toLowerCase(), password.getText(), reply.message()),
                    err -> {
                        create.setDisable(false);
                        Ui.message(status, err.getMessage(), true);
                    });
        });

        Button back = Ui.link("Already have an account? Sign in");
        back.setOnAction(e -> showSignIn(email.getText(), null));
        Button guest = Ui.link("Continue as guest");
        guest.setOnAction(e -> goHome());

        Label nationLabel = new Label("Your nation (shown on your profile):");
        nationLabel.setFont(Font.font("Arial", 11));
        nationLabel.setStyle("-fx-text-fill: " + Ui.DIM + ";");

        content.getChildren().setAll(Ui.heading("CREATE ACCOUNT", 20),
                email, name, password, confirm, nationLabel, nation, create, status, back, guest);
        content.setSpacing(8);
    }

    private void loadNations(ComboBox<String> combo, Button create, Label status) {
        Runnable fill = () -> {
            combo.getItems().setAll(nationNames);
            combo.setValue(nationNames.contains(DEFAULT_NATION) ? DEFAULT_NATION : nationNames.get(0));
            combo.setDisable(false);
            create.setDisable(false);
        };
        if (nationNames != null) {
            fill.run();
            return;
        }
        Ui.async(() -> ServerApi.fetchNations().stream().map(NationDto::name).toList(),
                names -> {
                    nationNames = names;
                    fill.run();
                },
                err -> Ui.message(status, err.getMessage(), true));
    }

    // ── Verify email ─────────────────────────────────────────────────────────

    /** @param password the password the player just typed (the server checks it with the code) */
    private void showVerify(String email, String password, String notice) {
        stopTimer();
        content.setSpacing(12);
        TextField code = Ui.field("6-digit code");
        code.setMaxWidth(180);
        code.setAlignment(Pos.CENTER);
        code.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        code.textProperty().addListener((o, old, now) -> {
            String digits = now.replaceAll("\\D", "");
            if (digits.length() > 6) digits = digits.substring(0, 6);
            if (!digits.equals(now)) code.setText(digits);
        });
        Label status = new Label();
        status.setWrapText(true);
        status.setMaxWidth(380);
        if (notice != null) Ui.message(status, notice, false);

        Button verify = Ui.button("VERIFY", 220);
        Runnable submit = () -> {
            verify.setDisable(true);
            Ui.message(status, "Checking code...", false);
            Ui.async(() -> ServerApi.verify(email, code.getText(), password),
                    login -> {
                        AccountSession.signIn(login);
                        goHome();
                    },
                    err -> {
                        verify.setDisable(false);
                        Ui.message(status, err.getMessage(), true);
                    });
        };
        verify.setOnAction(e -> submit.run());
        code.setOnAction(e -> submit.run());

        Button resend = Ui.link("Resend code");
        resend.setOnAction(e -> {
            resend.setDisable(true);
            Ui.async(() -> ServerApi.resendCode(email),
                    reply -> {
                        Ui.message(status, reply.message(), false);
                        startResendCooldown(resend);
                    },
                    err -> {
                        Ui.message(status, err.getMessage(), true);
                        startResendCooldown(resend);
                    });
        });
        startResendCooldown(resend);

        Button back = Ui.link("Back to sign in");
        back.setOnAction(e -> showSignIn(email, null));

        content.getChildren().setAll(Ui.heading("VERIFY YOUR EMAIL", 20),
                Ui.body("Enter the 6-digit code we emailed to " + email + ". It expires in 15 minutes."),
                code, verify, status, resend, back);
    }

    private void startResendCooldown(Button resend) {
        stopTimer();
        int[] left = {RESEND_SECONDS};
        resend.setDisable(true);
        resend.setText("Resend code (" + left[0] + ")");
        resendTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            left[0]--;
            if (left[0] <= 0) {
                resend.setText("Resend code");
                resend.setDisable(false);
                resendTimer.stop();
            } else {
                resend.setText("Resend code (" + left[0] + ")");
            }
        }));
        resendTimer.setCycleCount(RESEND_SECONDS);
        resendTimer.play();
    }
}
