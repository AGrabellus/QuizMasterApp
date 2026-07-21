package de.agrabellus.komp;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class HomeView extends ScrollPane {

    private static final File DEFAULT_QUIZ_FOLDER = new File("C:/QuizMasterApp");
    private static File lastLoadedFolder;

    private VBox rootContainer = new VBox(20);
    private HBox boxesContainer = new HBox(20);
    private HBox extraAppsContainer = new HBox(20);

    private List<String> allFiles = new ArrayList<>();

    private VBox anklickbarBoxContent = new VBox(10);
    private VBox smart10BoxContent = new VBox(10);
    private VBox memoryBoxContent = new VBox(10);

    private Stage primaryStage;
    private File loadedFolder;

    public HomeView(Stage primaryStage) {
        this.primaryStage = primaryStage;

        // 1. ScrollPane Einstellungen: Komplett transparent machen & Verlauf hier auflegen!
        this.setFitToWidth(true);
        this.setFitToHeight(true);
        this.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #081122 0%, #0b1624 100%);" +
                        "-fx-background: transparent;" + // Zwingt den inneren Viewport zur Transparenz
                        "-fx-border-color: transparent;" // Entfernt eventuelle Standard-Ränder
        );

        // 2. Hauptcontainer (VBox) stylen: Hintergrund hier entfernen (macht ja schon die ScrollPane)
        // Kein -fx-background-Shorthand: Dieser kann JavaFX-Scrollbars falsch vererben.
        this.setStyle("-fx-background-color: linear-gradient(to bottom, #081122 0%, #0b1624 100%);"
                + "-fx-border-color: transparent;");
        rootContainer.setStyle("-fx-background-color: transparent;");
        rootContainer.setPadding(new javafx.geometry.Insets(20, 20, 40, 20));
        rootContainer.setAlignment(Pos.TOP_CENTER);

        // Header Bereich
        Text title = new Text("🎓 Quiz Master");
        title.getStyleClass().add("main-title");
        Text subtitle = new Text("Teste dein Wissen mit Quizzen!");
        subtitle.getStyleClass().add("subtitle");
        VBox header = new VBox(5, title, subtitle);
        header.setAlignment(Pos.CENTER);

        // Ordner-Picker Box
        VBox pickerBox = new VBox(10);
        pickerBox.getStyleClass().add("box");
        pickerBox.setAlignment(Pos.CENTER);
        Text pickerTitle = new Text("📂 Quiz-Ordner laden");
        pickerTitle.getStyleClass().add("box-title");
        Button chooseFolderBtn = new Button("📁 Ordner auswählen");
        chooseFolderBtn.getStyleClass().add("load-btn");
        pickerBox.getChildren().addAll(pickerTitle, chooseFolderBtn);

        // Die 3 Haupt-Quiz-Boxen erstellen
        VBox anklickbarBox = createQuizBox("📚 Anklickbar", "Wähle die passende Antwort!", anklickbarBoxContent);
        VBox smart10Box = createQuizBox("🎮 Smart10", "Vermeide die falschen Antworten!", smart10BoxContent);
        VBox memoryBox = createQuizBox("🧩 Memory", "Finde die richtigen Paare!", memoryBoxContent);

        boxesContainer.getChildren().addAll(anklickbarBox, smart10Box, memoryBox);
        boxesContainer.setAlignment(Pos.TOP_CENTER);

        Line quizMakerDivider = new Line(0, 0, 800, 0);
        quizMakerDivider.setStroke(Color.web("#4fb871", 0.5));
        Text quizMakerHeading = new Text("QuizMaker");
        quizMakerHeading.getStyleClass().add("section-heading");
        VBox quizMakerSection = new VBox(10, quizMakerDivider, quizMakerHeading);
        quizMakerSection.setAlignment(Pos.CENTER);

        // NEU: Button zum Erstellen eines Anklickbar-Quiz unter den Boxen
        Button createAnklickbarBtn = new Button("➕ Anklickbar-Quiz erstellen");
        createAnklickbarBtn.getStyleClass().add("load-btn");
        createAnklickbarBtn.setStyle("-fx-font-size: 1.1em; -fx-padding: 10 20 10 20;"); // Etwas größer stylen
        createAnklickbarBtn.setOnAction(e -> {
            File base = lastLoadedFolder != null ? lastLoadedFolder : DEFAULT_QUIZ_FOLDER;
            primaryStage.getScene().setRoot(new AnklickbarErstellenView(primaryStage, base));
        });

        // NEU: Button zum Erstellen eines Smart10-Quiz unter den Boxen
        Button createSmartTenBtn = new Button("➕ SmartTen-Quiz erstellen");
        createSmartTenBtn.getStyleClass().add("load-btn");
        createSmartTenBtn.setStyle("-fx-font-size: 1.1em; -fx-padding: 10 20 10 20;"); // Etwas größer stylen
        createSmartTenBtn.setOnAction(e -> {
            File base = lastLoadedFolder != null ? lastLoadedFolder : DEFAULT_QUIZ_FOLDER;
            primaryStage.getScene().setRoot(new SmartTenErstellenView(primaryStage, base));
        });

        // NEU: Button zum Erstellen eines Memory-Quiz unter den Boxen
        Button createMemoryBtn = new Button("➕ Memory-Quiz erstellen");
        createMemoryBtn.getStyleClass().add("load-btn");
        createMemoryBtn.setStyle("-fx-font-size: 1.1em; -fx-padding: 10 20 10 20;"); // Etwas größer stylen
        createMemoryBtn.setOnAction(e -> {
            File base = lastLoadedFolder != null ? lastLoadedFolder : DEFAULT_QUIZ_FOLDER;
            primaryStage.getScene().setRoot(new MemoryErstellenView(primaryStage, base));
        });

        VBox actionArea = new VBox(createAnklickbarBtn,createSmartTenBtn,createMemoryBtn);
        actionArea.setAlignment(Pos.CENTER);
        actionArea.setSpacing(10);
        actionArea.setPadding(new javafx.geometry.Insets(10, 10, 10, 10));

        // Trennlinie für weitere Apps
        Line divider = new Line(0, 0, 800, 0);
        divider.setStroke(Color.web("#4fb871", 0.5));
        Text extraHeading = new Text("🧩 Weitere Apps 🎮");
        extraHeading.getStyleClass().add("section-heading");
        VBox dividerSection = new VBox(10, divider, extraHeading);
        dividerSection.setAlignment(Pos.CENTER);

        // Weitere Apps Boxen
        VBox wuerfelBox = createSimpleAppBox("🎲 Würfel", this::openWuerfel);
        VBox minesweeperBox = createSimpleAppBox("💥 Minesweeper", this::openMinesweeper);
        extraAppsContainer.getChildren().addAll(wuerfelBox, minesweeperBox);
        extraAppsContainer.setAlignment(Pos.TOP_CENTER);

        // Event-Handling
        chooseFolderBtn.setOnAction(e -> {
            DirectoryChooser chooser = new DirectoryChooser();
            File selectedFolder = chooser.showDialog(primaryStage);
            if (selectedFolder != null) {
                ladeOrdner(selectedFolder);
            }
        });

        // Alles zusammenbauen
        rootContainer.getChildren().addAll(header, pickerBox, boxesContainer, quizMakerSection, actionArea,
                dividerSection, extraAppsContainer);
        this.setContent(rootContainer);

        File initialFolder = lastLoadedFolder != null ? lastLoadedFolder : DEFAULT_QUIZ_FOLDER;
        if (initialFolder.isDirectory()) {
            ladeOrdner(initialFolder);
        }
    }


    private VBox createQuizBox(String titleText, String tooltipText, VBox contentTarget) {
        VBox box = new VBox();
        box.getStyleClass().add("box");
        box.setPrefWidth(320);
        box.setEffect(new DropShadow(20, Color.rgb(2, 6, 23, 0.5)));

        HBox boxHeader = new HBox();
        boxHeader.setAlignment(Pos.CENTER_LEFT);
        Text title = new Text(titleText);
        title.getStyleClass().add("box-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        javafx.scene.control.Label infoIcon = new javafx.scene.control.Label("i");
        infoIcon.getStyleClass().add("info-icon");
        infoIcon.setAlignment(Pos.CENTER);
        infoIcon.setPrefSize(18, 18); // Feste Größe für das runde Icon

        Tooltip tooltip = new Tooltip(tooltipText);
        tooltip.setShowDelay(javafx.util.Duration.millis(100)); // Schnelleres Aufklappen
        Tooltip.install(infoIcon, tooltip);

        boxHeader.getChildren().addAll(title, spacer, infoIcon);

        Text emptyMsg = new Text("Keine Beispiele gefunden.");
        emptyMsg.getStyleClass().add("empty-message");
        contentTarget.getChildren().add(emptyMsg);

        box.getChildren().addAll(boxHeader, contentTarget);
        return box;
    }

    private VBox createSimpleAppBox(String titleText, Runnable onStart) {
        VBox box = new VBox(10);
        box.getStyleClass().add("box");
        box.setPrefWidth(250);
        box.setAlignment(Pos.CENTER);

        Text title = new Text(titleText);
        title.getStyleClass().add("box-title");
        Button startBtn = new Button("▶ Starten");
        startBtn.getStyleClass().add("load-btn");
        startBtn.setOnAction(e -> onStart.run());

        box.getChildren().addAll(title, startBtn);
        return box;
    }

    private void ladeOrdner(File ordner) {
        loadedFolder = ordner;
        lastLoadedFolder = ordner;
        anklickbarBoxContent.getChildren().clear();
        smart10BoxContent.getChildren().clear();
        memoryBoxContent.getChildren().clear();
        allFiles.clear();

        File manifest = new File(ordner, "manifest.json");
        if (manifest.exists()) {
            try {
                String content = Files.readString(manifest.toPath());
                content = content.replace("[", "").replace("]", "").replace("\"", "");
                for (String file : content.split(",")) {
                    String cleanName = file.trim();
                    if (cleanName.toLowerCase().endsWith(".txt") && !isInOldFolder(cleanName)) {
                        allFiles.add(cleanName);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            try (Stream<Path> paths = Files.walk(ordner.toPath())) {
                paths.filter(Files::isRegularFile)
                        .filter(path -> path.getFileName().toString().toLowerCase().endsWith(".txt"))
                        .map(path -> ordner.toPath().relativize(path).toString().replace(File.separatorChar, '/'))
                        .filter(path -> !isInOldFolder(path))
                        .sorted(String.CASE_INSENSITIVE_ORDER)
                        .forEach(allFiles::add);
            } catch (Exception ex) {
                System.err.println("Quiz-Ordner konnte nicht durchsucht werden: " + ordner.getAbsolutePath());
                ex.printStackTrace(System.err);
            }
        }

        for (String dateiName : allFiles) {
            HBox row = new HBox(10);
            row.getStyleClass().add("resource-item");
            row.setAlignment(Pos.CENTER_LEFT);

            String displayName = getQuizDisplayName(dateiName);

            Text nameText = new Text(displayName);
            nameText.getStyleClass().add("resource-name");
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Button startBtn = new Button("▶ Starten");
            startBtn.getStyleClass().add("load-btn");

            startBtn.setOnAction(e -> openQuiz(dateiName));

            row.getChildren().addAll(nameText, spacer, startBtn);

            if (dateiName.toLowerCase().startsWith("anklickbar")) {
                anklickbarBoxContent.getChildren().add(row);
            } else if (dateiName.toLowerCase().startsWith("smart10")) {
                smart10BoxContent.getChildren().add(row);
            } else if (dateiName.toLowerCase().startsWith("memory")) {
                memoryBoxContent.getChildren().add(row);
            }
        }
    }

    /** Entfernt nur den Quizart-Praefix, z. B. AnklickbarBeispiel.txt -> Beispiel. */
    private String getQuizDisplayName(String dateiName) {
        String filename = new File(dateiName.replace('\\', '/')).getName();
        String withoutExtension = filename.replaceFirst("(?i)\\.txt$", "");
        return withoutExtension.replaceFirst("(?i)^(anklickbar|smart10|memory)", "").trim();
    }

    private boolean isInOldFolder(String relativePath) {
        for (String pathPart : relativePath.replace('\\', '/').split("/")) {
            if ("old".equalsIgnoreCase(pathPart)) return true;
        }
        return false;
    }

    // Navigations-Methoden (Ähnlich wie Angular Router)
    private void openQuiz(String dateiName) {
        String lower = dateiName.toLowerCase();
        File quizFile = loadedFolder == null ? new File(dateiName) : new File(loadedFolder, dateiName);
        if (lower.startsWith("anklickbar")) {
            primaryStage.getScene().setRoot(new AnklickbarView(primaryStage, dateiName, quizFile));
        } else if (lower.startsWith("smart10")) {
            primaryStage.getScene().setRoot(new SmartTenView(primaryStage, dateiName));
        } else if (lower.startsWith("memory")) {
            primaryStage.getScene().setRoot(new MemoryView(primaryStage, dateiName, quizFile)); // <-- Hier quizFile ergänzen
        }
    }

    private void openWuerfel() {
        System.out.println("Öffne Würfel-Klasse");
        // Tauscht die aktuelle Ansicht des Fensters gegen die Würfel-Ansicht aus
        primaryStage.getScene().setRoot(new WuerfelView(primaryStage));
    }

    private void openMinesweeper() {
        System.out.println("Öffne Minesweeper-Klasse");
    }
}
