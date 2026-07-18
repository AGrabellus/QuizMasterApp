package de.agrabellus.komp;

import javafx.animation.RotateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Random;

public class WuerfelView extends VBox {

    private Text resultHeading = new Text();
    private VBox resultBox = new VBox();
    private Random random = new Random();

    public WuerfelView(Stage stage) {
        // Layout-Einstellungen (passend zu deinem CSS)
        this.setStyle("-fx-background-color: linear-gradient(to bottom, #081122 0%, #0b1624 100%);");
        this.setPadding(new Insets(20));
        this.setSpacing(20);
        this.setAlignment(Pos.TOP_CENTER);
        this.setMinHeight(800);

        // Header Bereich (Zurück-Button)
        Button backBtn = new Button("◀ Zurück zum Menü");
        backBtn.getStyleClass().add("load-btn");
        backBtn.setOnAction(e -> stage.getScene().setRoot(new HomeView(stage)));

        HBox headerRow = new HBox(backBtn);
        headerRow.setAlignment(Pos.TOP_LEFT);
        this.getChildren().add(headerRow);

        // Titel & Info
        Text title = new Text("🎲 Würfel");
        title.setStyle("-fx-font-size: 2em; -fx-fill: #4fb871; -fx-font-weight: bold;");
        Text info = new Text("Wähle einen Würfel aus:");
        info.setStyle("-fx-font-size: 1.1em; -fx-fill: #cfe8ff;");
        this.getChildren().addAll(title, info);

        // Würfel-Buttons Container (FlowPane bricht automatisch um wie flex-wrap)
        FlowPane diceButtonsContainer = new FlowPane();
        diceButtonsContainer.setHgap(15);
        diceButtonsContainer.setVgap(15);
        diceButtonsContainer.setAlignment(Pos.CENTER);

        String[] diceTypes = {"d2", "d4", "d6", "d20", "d100"};
        for (String type : diceTypes) {
            Button diceBtn = new Button(type);
            diceBtn.getStyleClass().add("dice-btn");

            diceBtn.setOnAction(e -> rollDice(type, diceBtn));
            diceButtonsContainer.getChildren().add(diceBtn);
        }
        this.getChildren().add(diceButtonsContainer);

        // Ergebnis Box (unsichtbar am Start)
        resultBox.getStyleClass().add("result-box");
        resultBox.setAlignment(Pos.CENTER);
        resultBox.setMaxWidth(300);
        resultBox.setPadding(new Insets(20));
        resultBox.setVisible(false);

        resultHeading.getStyleClass().add("result-title");
        resultBox.getChildren().add(resultHeading);

        this.getChildren().add(resultBox);
    }

    private void rollDice(String type, Button button) {
        resultBox.setVisible(false);

        // 1. Rotation-Animation starten (360 Grad wie im Angular CSS)
        RotateTransition rt = new RotateTransition(Duration.millis(1000), button);
        rt.setByAngle(360);
        rt.setCycleCount(1);

        rt.setOnFinished(e -> {
            // 2. Nach der Animation Ergebnis berechnen
            int max = getMaxValue(type);
            int result = random.nextInt(max) + 1;

            // 3. Ergebnis anzeigen
            resultHeading.setText("Ergebnis: " + result);
            resultBox.setVisible(true);

            // 4. Text-to-Speech (Sprachausgabe) ausführen
            speakResult(result);
        });

        rt.play();
    }

    private int getMaxValue(String type) {
        return switch (type) {
            case "d2" -> 2;
            case "d4" -> 4;
            case "d6" -> 6;
            case "d20" -> 20;
            case "d100" -> 100;
            default -> 6;
        };
    }

    private void speakResult(int result) {
        // Nutzt ein PowerShell Skript im Hintergrund, damit wir keine externen dicken Audio-Libraries brauchen
        String text = "Ergebnis " + result;
        new Thread(() -> {
            try {
                String command = "PowerShell -Command \"Add-Type –AssemblyName System.Speech; " +
                        "$speak = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
                        "$speak.Rate = -1; " + // Leicht langsamer stellen
                        "$speak.Speak('" + text + "')\"";
                Runtime.getRuntime().exec(command);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }
}