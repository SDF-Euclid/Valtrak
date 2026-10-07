package com.example.valtrak.UI;

import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

/** Shared look and small helpers for the client screens. */
public final class Ui {
    public static final String BG      = "#1a1a2e";
    public static final String PANEL   = "#16213e";
    public static final String HOVER   = "#0f3460";
    public static final String ACCENT  = "#e8b84b";
    public static final String TEXT    = "#d4d4d4";
    public static final String DIM     = "#777777";
    public static final String ERROR   = "#ff6b6b";
    public static final String OK      = "#6bcf7f";

    private Ui() {}

    /** Runs a blocking call off the UI thread and delivers the result (or error) back on it. */
    public static <T> void async(Callable<T> work, Consumer<T> onSuccess, Consumer<Throwable> onError) {
        Task<T> task = new Task<>() {
            @Override protected T call() throws Exception { return work.call(); }
        };
        task.setOnSucceeded(e -> onSuccess.accept(task.getValue()));
        task.setOnFailed(e -> onError.accept(task.getException()));
        Thread t = new Thread(task, "ui-async");
        t.setDaemon(true);
        t.start();
    }

    public static Button button(String text, double width) {
        Button b = new Button(text);
        b.setMinWidth(width);
        b.setMinHeight(40);
        b.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        String base = "-fx-background-color: " + PANEL + "; -fx-text-fill: " + ACCENT + "; -fx-background-radius: 4; "
                + "-fx-border-color: " + ACCENT + "; -fx-border-radius: 4; -fx-border-width: 1;";
        String hover = base.replace(PANEL, HOVER);
        String off = "-fx-background-color: #2d2d2d; -fx-text-fill: #555555; -fx-background-radius: 4;";
        b.setStyle(base);
        b.setOnMouseEntered(e -> { if (!b.isDisabled()) b.setStyle(hover); });
        b.setOnMouseExited(e -> b.setStyle(b.isDisabled() ? off : base));
        b.disabledProperty().addListener((o, was, is) -> b.setStyle(is ? off : base));
        return b;
    }

    public static Button link(String text) {
        Button b = new Button(text);
        String base = "-fx-background-color: transparent; -fx-text-fill: " + DIM + "; -fx-underline: true; "
                + "-fx-font-size: 12px; -fx-cursor: hand;";
        b.setStyle(base);
        b.setOnMouseEntered(e -> b.setStyle(base.replace(DIM, ACCENT)));
        b.setOnMouseExited(e -> b.setStyle(base));
        return b;
    }

    public static TextField field(String prompt) {
        TextField f = new TextField();
        f.setPromptText(prompt);
        style(f);
        return f;
    }

    public static PasswordField password(String prompt) {
        PasswordField f = new PasswordField();
        f.setPromptText(prompt);
        style(f);
        return f;
    }

    private static void style(TextInputControl f) {
        f.setMaxWidth(320);
        f.setMinHeight(34);
        f.setStyle("-fx-control-inner-background: #0d1326; -fx-text-fill: white; -fx-prompt-text-fill: #555; "
                + "-fx-highlight-fill: " + ACCENT + "; -fx-highlight-text-fill: black; "
                + "-fx-border-color: #3a4a70; -fx-border-radius: 4; -fx-background-radius: 4;");
    }

    /** A dark dropdown matching the text fields. */
    public static void styleCombo(javafx.scene.control.ComboBox<String> box) {
        box.setStyle("-fx-background-color: #0d1326; -fx-border-color: #3a4a70; -fx-border-radius: 4; "
                + "-fx-background-radius: 4; -fx-mark-color: " + ACCENT + ";");
        box.setButtonCell(darkCell());
        box.setCellFactory(lv -> darkCell());
    }

    private static javafx.scene.control.ListCell<String> darkCell() {
        return new javafx.scene.control.ListCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setTextFill(Color.WHITE);
                setStyle("-fx-background-color: #0d1326;");
            }
        };
    }

    public static Label heading(String text, int size) {
        Label l = new Label(text);
        l.setFont(Font.font("Arial", FontWeight.BOLD, size));
        l.setTextFill(Color.web(ACCENT));
        return l;
    }

    public static Label body(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Arial", 12));
        l.setTextFill(Color.web(TEXT));
        l.setWrapText(true);
        l.setPrefWidth(380);
        l.setMaxWidth(380);
        l.setAlignment(javafx.geometry.Pos.CENTER);
        l.setStyle("-fx-text-alignment: center;");
        return l;
    }

    public static void message(Label l, String text, boolean error) {
        l.setPrefWidth(380);
        l.setAlignment(javafx.geometry.Pos.CENTER);
        l.setStyle("-fx-text-alignment: center;");
        l.setText(text);
        l.setTextFill(Color.web(error ? ERROR : OK));
    }
}
