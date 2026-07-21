package de.agrabellus.komp;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class SmartTenErstellenView extends VBox {
    private Stage primaryStage;
    private File baseFolder;

    private File currentQuizFile;
    private File currentQuizMediaFolder;

    private VBox rowsContainer;
    private HBox actionArea;
    private Text statusText;

    public SmartTenErstellenView(Stage primaryStage, File baseFolder) {
        this.primaryStage = primaryStage;
        this.baseFolder = baseFolder;

        // Styling für den Hintergrund (passend zur HomeView)
        this.setStyle("-fx-background-color: linear-gradient(to bottom, #081122 0%, #0b1624 100%);");
        this.setPadding(new Insets(20));
        this.setSpacing(20);
        this.setAlignment(Pos.TOP_CENTER);

        // --- HEADER ---
        Button backBtn = new Button("⬅ Zurück");
        backBtn.setStyle("-fx-background-color: #1e293b; -fx-text-fill: white; -fx-font-weight: bold;");
        backBtn.setOnAction(e -> primaryStage.getScene().setRoot(new HomeView(primaryStage)));

        Text title = new Text("📝 Smart10-Quiz Erstellen / Laden");
        title.setStyle("-fx-font-size: 24px; -fx-fill: white; -fx-font-weight: bold;");

        HBox header = new HBox(20, backBtn, title);
        header.setAlignment(Pos.CENTER_LEFT);

        // --- EINGABE-BEREICH (Name + Laden/Erstellen) ---
        HBox inputArea = new HBox(10);
        inputArea.setAlignment(Pos.CENTER);

        Label nameLabel = new Label("Quiz Name:");
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");

        TextField nameInput = new TextField();
        nameInput.setPromptText("z.B. Tiere");
        nameInput.setPrefWidth(200);

        Button loadCreateBtn = new Button("Laden / Erstellen");
        loadCreateBtn.setStyle("-fx-background-color: #4fb871; -fx-text-fill: white; -fx-font-weight: bold;");

        inputArea.getChildren().addAll(nameLabel, nameInput, loadCreateBtn);

        // --- AKTIONEN-BEREICH (Explorer, Neue Zeile, Speichern) ---
        actionArea = new HBox(15);
        actionArea.setAlignment(Pos.CENTER);

        Button openMediaFolderBtn = new Button("📂 Media-Ordner öffnen");
        Button addRowBtn = new Button("➕ Neue Zeile");
        Button saveBtn = new Button("💾 Speichern");

        String actionBtnStyle = "-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold;";
        openMediaFolderBtn.setStyle(actionBtnStyle);
        addRowBtn.setStyle(actionBtnStyle);
        saveBtn.setStyle("-fx-background-color: #eab308; -fx-text-fill: black; -fx-font-weight: bold;");

        actionArea.getChildren().addAll(openMediaFolderBtn, addRowBtn, saveBtn);

        // Initial verstecken, bis ein Quiz geladen/erstellt wurde
        actionArea.setVisible(false);
        actionArea.setManaged(false);

        // --- STATUS TEXT ---
        statusText = new Text("");
        statusText.setStyle("-fx-fill: #a3a8b4; -fx-font-size: 14px;");

        // --- EDITOR BEREICH (Zeilen für Frage/Antwort) ---
        rowsContainer = new VBox(10);
        rowsContainer.setAlignment(Pos.TOP_CENTER);

        ScrollPane scrollPane = new ScrollPane(rowsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // --- EVENTS ---
        loadCreateBtn.setOnAction(e -> handleLoadCreate(nameInput.getText()));
        openMediaFolderBtn.setOnAction(e -> openMediaFolder(currentQuizMediaFolder));
        addRowBtn.setOnAction(e -> addRow("", ""));
        saveBtn.setOnAction(e -> saveQuiz());

        // Alles zusammenbauen
        this.getChildren().addAll(header, inputArea, statusText, actionArea, scrollPane);
    }

    /**
     * Verarbeitet das Laden oder Erstellen der Dateien und Ordner
     */
    private void handleLoadCreate(String quizName) {
        if (quizName == null || quizName.trim().isEmpty()) {
            statusText.setText("❌ Bitte einen Namen eingeben.");
            statusText.setStyle("-fx-fill: #ef4444;");
            return;
        }

        String safeName = quizName.trim();

        // Hauptordner für diesen Quiz-Typ anlegen, z.B. C:/QuizMasterApp/Smart10
        File Smart10Dir = new File(baseFolder, "Smart10");
        if (!Smart10Dir.exists()) {
            Smart10Dir.mkdirs();
        }

        // Unterordner für Medien (Bilder etc.) anlegen, z.B. C:/QuizMasterApp/Smart10/Tiere
        currentQuizMediaFolder = new File(Smart10Dir, safeName);
        if (!currentQuizMediaFolder.exists()) {
            currentQuizMediaFolder.mkdirs();
        }

        // Textdatei im Smart10-Ordner anlegen, z.B. C:/QuizMasterApp/Smart10/Tiere.txt
        currentQuizFile = new File(Smart10Dir, safeName + ".txt");

        rowsContainer.getChildren().clear();

        try {
            if (currentQuizFile.exists()) {
                // Laden
                List<String> lines = Files.readAllLines(currentQuizFile.toPath());
                for (String line : lines) {
                    if (line.trim().isEmpty()) continue;
                    // Annahme: Format ist "Frage|Antwort"
                    String[] parts = line.split("\\|", 2);
                    String frage = parts.length > 0 ? parts[0] : "";
                    String antwort = parts.length > 1 ? parts[1] : "";
                    addRow(frage, antwort);
                }
                statusText.setText("✅ Quiz geladen: " + currentQuizFile.getAbsolutePath());
            } else {
                // Erstellen
                currentQuizFile.createNewFile();
                addRow("", ""); // Direkt eine leere Zeile anbieten
                statusText.setText("✨ Neues Quiz erstellt: " + currentQuizFile.getAbsolutePath());
            }
            statusText.setStyle("-fx-fill: #4fb871;");

            // Editor freischalten
            actionArea.setVisible(true);
            actionArea.setManaged(true);

        } catch (IOException ex) {
            statusText.setText("❌ Fehler beim Dateizugriff: " + ex.getMessage());
            statusText.setStyle("-fx-fill: #ef4444;");
            ex.printStackTrace();
        }
    }

    /**
     * Fügt eine neue Frage/Antwort-Zeile zur Ansicht hinzu
     */
    private void addRow(String frage, String antwort) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER);

        TextField frageFeld = new TextField(frage);
        frageFeld.setPromptText("Frage...");
        frageFeld.setPrefWidth(300);

        TextField antwortFeld = new TextField(antwort);
        antwortFeld.setPromptText("Antwort...");
        antwortFeld.setPrefWidth(300);

        Button deleteBtn = new Button("🗑");
        deleteBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white;");
        deleteBtn.setOnAction(e -> rowsContainer.getChildren().remove(row));

        row.getChildren().addAll(frageFeld, antwortFeld, deleteBtn);
        rowsContainer.getChildren().add(row);
    }

    /**
     * Schreibt den Inhalt aller Textfelder mit einem "|" getrennt in die .txt Datei
     */
    private void saveQuiz() {
        if (currentQuizFile == null) return;

        List<String> lines = new ArrayList<>();
        for (Node node : rowsContainer.getChildren()) {
            if (node instanceof HBox) {
                HBox row = (HBox) node;
                TextField frageFeld = (TextField) row.getChildren().get(0);
                TextField antwortFeld = (TextField) row.getChildren().get(1);

                String frage = frageFeld.getText().trim();
                String antwort = antwortFeld.getText().trim();

                // Leere Zeilen nicht speichern
                if (!frage.isEmpty() || !antwort.isEmpty()) {
                    lines.add(frage + "|" + antwort);
                }
            }
        }

        try {
            Files.write(currentQuizFile.toPath(), lines);
            statusText.setText("💾 Erfolgreich gespeichert!");
            statusText.setStyle("-fx-fill: #4fb871;");
        } catch (IOException ex) {
            statusText.setText("❌ Fehler beim Speichern: " + ex.getMessage());
            statusText.setStyle("-fx-fill: #ef4444;");
            ex.printStackTrace();
        }
    }

    /**
     * Öffnet den Media-Ordner im System-Dateimanager (Windows Explorer, Linux Nautilus, Mac Finder)
     */
    private void openMediaFolder(File folder) {
        if (folder != null && folder.exists()) {
            try {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                    Desktop.getDesktop().open(folder);
                } else {
                    statusText.setText("❌ Desktop-Öffnen wird auf diesem System nicht unterstützt.");
                    statusText.setStyle("-fx-fill: #eab308;");
                }
            } catch (IOException ex) {
                statusText.setText("❌ Fehler beim Öffnen des Media-Ordner.");
                ex.printStackTrace();
            }
        }
    }
}
