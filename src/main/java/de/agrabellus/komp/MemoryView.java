package de.agrabellus.komp;

import javafx.animation.PauseTransition;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class MemoryView extends VBox {

    private enum GameState { READY, STARTED, FINISHED }

    private GameState gameState = GameState.READY;
    private List<Player> players = new ArrayList<>();
    private int currentPlayerIndex = 0;

    private List<MemoryPair> pairs = new ArrayList<>();
    private List<MemoryCard> cards = new ArrayList<>();
    private List<MemoryCard> flippedCards = new ArrayList<>();

    private File quizFile;
    private File folder;

    // UI Container
    private VBox mainContent = new VBox(20);
    private Text currentPlayerNameText;
    private VBox scoreboardContainer = new VBox(10);

    public MemoryView(Stage stage, String dateiName, File quizFile) {
        this.quizFile = quizFile;
        if (quizFile != null) {
            // Name der Datei ohne Dateiendung ermitteln (z.B. "Pokemon.txt" -> "Pokemon")
            String fileName = quizFile.getName();
            int lastDot = fileName.lastIndexOf('.');
            String folderName = (lastDot > 0) ? fileName.substring(0, lastDot) : fileName;

            // Ordner auf den Unterordner setzen: C:\QuizMasterApp\memory\{QuizName}\
            this.folder = new File(quizFile.getParentFile(), folderName);
        }

        // CSS Style-Anleihen (Dunkles Theme)
        this.setStyle("-fx-background-color: linear-gradient(to bottom, #081122 0%, #0b1624 100%);");
        this.setPadding(new Insets(20));
        this.setSpacing(20);
        this.setAlignment(Pos.TOP_CENTER);
        this.setMinHeight(800);

        Button backBtn = new Button("◀ Zurück");
        backBtn.getStyleClass().add("load-btn");
        backBtn.setOnAction(e -> stage.getScene().setRoot(new HomeView(stage)));

        Text title = new Text("🧩 Memory-Spiel");
        title.setStyle("-fx-font-size: 2em; -fx-fill: #4fb871; -fx-font-weight: bold;");

        Text fileText = new Text("Datei: " + dateiName);
        fileText.setStyle("-fx-fill: #cfe8ff;");

        mainContent.setAlignment(Pos.TOP_CENTER);

        // ScrollPane, falls es viele Karten gibt
        ScrollPane scroll = new ScrollPane(mainContent);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        this.getChildren().addAll(backBtn, title, fileText, scroll);

        if (quizFile != null && quizFile.exists()) {
            loadMemoryData();
            initializeGame();
        }

        renderReadyState();
    }

    private void loadMemoryData() {
        try {
            List<String> lines = Files.readAllLines(quizFile.toPath());
            pairs.clear();
            for (String line : lines) {
                if (line.trim().isEmpty()) continue;
                String[] parts;
                if (line.contains("||")) {
                    parts = line.split("\\|\\|");
                } else {
                    parts = line.split("\\|");
                }

                if (parts.length >= 2) {
                    String first = parts[0].trim().replace("\"", "");
                    String second = parts[1].trim().replace("\"", "");
                    pairs.add(new MemoryPair(first, second, isImageValue(first), isImageValue(second)));
                }
            }
        } catch (Exception e) {
            System.err.println("Fehler beim Laden der Memory-Datei: " + e.getMessage());
        }
    }

    private boolean isImageValue(String value) {
        if (value == null) return false;
        String normalized = value.trim().toLowerCase();

        // Prüfe auf direkte Protokolle
        if (normalized.startsWith("http://") || normalized.startsWith("https://") || normalized.startsWith("data:image/")) {
            return true;
        }

        // Prüfe auf typische Bild-Endungen (auch wenn relativer Pfad wie "images/pic.png")
        return normalized.matches(".*\\.(png|jpe?g|webp|gif|bmp|svg|ico|tiff?|avif)$");
    }

    private void initializeGame() {
        cards.clear();
        int id = 0;
        for (MemoryPair pair : pairs) {
            cards.add(new MemoryCard(id++, pair.first, pair.firstIsImage));
            cards.add(new MemoryCard(id++, pair.second, pair.secondIsImage));
        }
        Collections.shuffle(cards);
    }

    // --- RENDER STATES ---

    private void renderReadyState() {
        mainContent.getChildren().clear();

        VBox setupContainer = new VBox(15);
        setupContainer.setAlignment(Pos.CENTER);
        setupContainer.setStyle("-fx-background-color: rgba(255, 255, 255, 0.05); -fx-padding: 30; -fx-background-radius: 12; -fx-border-color: rgba(79, 184, 113, 0.2); -fx-border-radius: 12;");
        setupContainer.setMaxWidth(500);

        Text header = new Text("👥 Spieler Setup");
        header.setStyle("-fx-fill: #4fb871; -fx-font-size: 28px; -fx-font-weight: bold;");
        Text subtitle = new Text("Füge mindestens einen Spieler hinzu");
        subtitle.setStyle("-fx-fill: #cfe8ff;");

        VBox playersList = new VBox(5);
        playersList.setAlignment(Pos.CENTER);

        Runnable updatePlayersList = () -> {
            playersList.getChildren().clear();
            if (players.isEmpty()) {
                Text noPlayers = new Text("Noch keine Spieler hinzugefügt");
                noPlayers.setStyle("-fx-fill: #999; -fx-font-style: italic;");
                playersList.getChildren().add(noPlayers);
            } else {
                for (Player p : players) {
                    HBox pBox = new HBox(10);
                    pBox.setAlignment(Pos.CENTER);
                    pBox.setStyle("-fx-background-color: rgba(79, 184, 113, 0.1); -fx-padding: 10; -fx-border-radius: 6; -fx-border-color: rgba(79, 184, 113, 0.3); -fx-border-width: 0 0 0 3;");

                    Text pName = new Text(p.name);
                    pName.setStyle("-fx-fill: #e6eef8; -fx-font-size: 16px; -fx-font-weight: bold;");
                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    Button rmBtn = new Button("✕");
                    rmBtn.setStyle("-fx-background-color: rgba(255, 100, 100, 0.2); -fx-text-fill: #ff6464; -fx-cursor: hand; -fx-font-weight: bold; -fx-border-color: rgba(255, 100, 100, 0.3); -fx-border-radius: 4;");
                    rmBtn.setOnAction(e -> {
                        players.remove(p);
                        renderReadyState();
                    });
                    pBox.getChildren().addAll(pName, spacer, rmBtn);
                    playersList.getChildren().add(pBox);
                }
            }
        };
        updatePlayersList.run();

        TextField nameInput = new TextField();
        nameInput.setPromptText("Name eingeben...");
        nameInput.setStyle("-fx-padding: 10; -fx-background-radius: 6; -fx-background-color: rgba(255, 255, 255, 0.05); -fx-text-fill: white; -fx-border-color: rgba(255, 255, 255, 0.1); -fx-border-radius: 6;");

        Button addBtn = new Button("➕ Speichern");
        addBtn.setStyle("-fx-background-color: linear-gradient(to bottom, #4fb871, #3d9659); -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 6; -fx-cursor: hand;");

        Runnable addPlayerAction = () -> {
            if (!nameInput.getText().trim().isEmpty()) {
                players.add(new Player(nameInput.getText().trim()));
                renderReadyState();
            }
        };
        addBtn.setOnAction(e -> addPlayerAction.run());
        nameInput.setOnAction(e -> addPlayerAction.run());

        HBox inputBox = new HBox(10, nameInput, addBtn);
        inputBox.setAlignment(Pos.CENTER);

        Button startBtn = new Button("🎮 Spiel starten");
        startBtn.setStyle("-fx-font-size: 1.2em; -fx-padding: 12 30; -fx-background-color: linear-gradient(to bottom, #2b6cb0, #1e4fa0); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");
        startBtn.setDisable(players.isEmpty());
        startBtn.setOnAction(e -> {
            gameState = GameState.STARTED;
            currentPlayerIndex = 0;
            renderStartedState();
        });

        setupContainer.getChildren().addAll(header, subtitle, playersList, inputBox, new Region(), startBtn);
        mainContent.getChildren().add(setupContainer);
    }

    private void renderStartedState() {
        mainContent.getChildren().clear();

        // Aktueller Spieler Anzeige
        VBox headerInfo = new VBox(5);
        headerInfo.setAlignment(Pos.CENTER);
        headerInfo.setStyle("-fx-background-color: rgba(255, 255, 255, 0.02); -fx-padding: 15; -fx-background-radius: 8; -fx-min-width: 400px;");
        Text currentTurnLabel = new Text("Aktueller Spieler");
        currentTurnLabel.setStyle("-fx-fill: #cfe8ff; -fx-font-size: 14px;");
        currentPlayerNameText = new Text(players.get(currentPlayerIndex).name);
        currentPlayerNameText.setStyle("-fx-fill: #4fb871; -fx-font-size: 22px; -fx-font-weight: bold;");
        headerInfo.getChildren().addAll(currentTurnLabel, currentPlayerNameText);

        // Grid-Layout Algorithmus: Möglichst quadratisch anordnen (z.B. 6 Spalten x 5 Zeilen bei 30)
        int numCards = cards.size();
        int bestCols = (int) Math.ceil(Math.sqrt(numCards));
        int bestRows = (int) Math.ceil((double) numCards / bestCols);

        // Versuche ein "perfektes" Raster ohne Lücken zu finden (z.B. 30 -> 6x5)
        for (int c = (int) Math.ceil(Math.sqrt(numCards)); c <= numCards; c++) {
            if (numCards % c == 0) {
                int r = numCards / c;
                if (c >= r) { // Wir wollen es tendenziell eher breiter als hoch
                    bestCols = c;
                    bestRows = r;
                    break;
                }
            }
        }

        // GridPane erstellen
        GridPane board = new GridPane();
        board.setAlignment(Pos.CENTER);
        board.setHgap(15);
        board.setVgap(15);

        // 1. Spalten-Köpfe generieren (A, B, C...)
        for (int c = 0; c < bestCols; c++) {
            Text colHeader = new Text(String.valueOf((char)('A' + c)));
            colHeader.setStyle("-fx-fill: #8892b0; -fx-font-size: 20px; -fx-font-weight: bold;");
            GridPane.setHalignment(colHeader, HPos.CENTER);
            board.add(colHeader, c + 1, 0); // Spalte + 1 (weil bei 0 die Zahlen stehen), Zeile 0
        }

        // 2. Zeilen-Köpfe generieren (1, 2, 3...)
        for (int r = 0; r < bestRows; r++) {
            Text rowHeader = new Text(String.valueOf(r + 1));
            rowHeader.setStyle("-fx-fill: #8892b0; -fx-font-size: 20px; -fx-font-weight: bold;");
            GridPane.setValignment(rowHeader, VPos.CENTER);
            board.add(rowHeader, 0, r + 1); // Spalte 0, Zeile + 1 (weil bei 0 die Buchstaben stehen)
        }

        // 3. Karten in das Raster füllen
        for (int i = 0; i < cards.size(); i++) {
            int row = i / bestCols;
            int col = i % bestCols;
            board.add(createCardNode(cards.get(i)), col + 1, row + 1);
        }

        renderScoreboard();

        mainContent.getChildren().addAll(headerInfo, board, scoreboardContainer);
    }

    private void renderScoreboard() {
        scoreboardContainer.getChildren().clear();
        scoreboardContainer.setAlignment(Pos.CENTER);
        scoreboardContainer.setStyle("-fx-background-color: rgba(255, 255, 255, 0.02); -fx-padding: 20; -fx-background-radius: 8;");

        Text title = new Text("📊 Punkte");
        title.setStyle("-fx-fill: #e6eef8; -fx-font-size: 20px; -fx-font-weight: bold;");
        scoreboardContainer.getChildren().add(title);

        FlowPane scoreBox = new FlowPane();
        scoreBox.setHgap(20);
        scoreBox.setVgap(10);
        scoreBox.setAlignment(Pos.CENTER);

        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            VBox pBox = new VBox(5);
            pBox.setAlignment(Pos.CENTER);

            if (i == currentPlayerIndex) {
                pBox.setStyle("-fx-background-color: rgba(79, 184, 113, 0.2); -fx-border-color: rgba(79, 184, 113, 0.5); -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10 20;");
            } else {
                pBox.setStyle("-fx-background-color: rgba(79, 184, 113, 0.1); -fx-border-color: rgba(79, 184, 113, 0.2); -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10 20;");
            }

            Text pName = new Text(p.name);
            pName.setStyle("-fx-fill: #e6eef8; -fx-font-weight: bold; -fx-font-size: 14px;");
            Text pScore = new Text(String.valueOf(p.score));
            pScore.setStyle("-fx-fill: #4fb871; -fx-font-weight: bold; -fx-font-size: 22px;");

            pBox.getChildren().addAll(pName, pScore);
            scoreBox.getChildren().add(pBox);
        }
        scoreboardContainer.getChildren().add(scoreBox);
    }

    private void renderFinishedState() {
        mainContent.getChildren().clear();

        VBox resultsBox = new VBox(15);
        resultsBox.setAlignment(Pos.CENTER);
        resultsBox.setStyle("-fx-background-color: rgba(255, 255, 255, 0.02); -fx-border-color: rgba(79, 184, 113, 0.2); -fx-padding: 40; -fx-background-radius: 12; -fx-border-radius: 12;");
        resultsBox.setMaxWidth(500);

        Text title = new Text("🏆 Spiel abgeschlossen!");
        title.setStyle("-fx-fill: #4fb871; -fx-font-size: 28px; -fx-font-weight: bold;");
        resultsBox.getChildren().add(title);

        // Spieler nach Punkten sortieren
        List<Player> sorted = new ArrayList<>(players);
        sorted.sort((a, b) -> Integer.compare(b.score, a.score));

        for (int i = 0; i < sorted.size(); i++) {
            Player p = sorted.get(i);
            HBox row = new HBox(15);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-background-color: rgba(79, 184, 113, 0.1); -fx-padding: 10 20; -fx-background-radius: 6;");

            Text rankText = new Text((i + 1) + ".");
            rankText.setStyle("-fx-fill: #4fb871; -fx-font-weight: bold; -fx-font-size: 18px;");

            Text pText = new Text(p.name);
            pText.setStyle("-fx-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Text scoreText = new Text(p.score + " Punkte");
            scoreText.setStyle("-fx-fill: #4fb871; -fx-font-size: 18px; -fx-font-weight: bold;");

            row.getChildren().addAll(rankText, pText, spacer, scoreText);
            resultsBox.getChildren().add(row);
        }

        Button restartBtn = new Button("🔄 Nochmal spielen");
        restartBtn.setStyle("-fx-background-color: linear-gradient(to bottom, #4fb871, #3d9659); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-padding: 12 30; -fx-background-radius: 8; -fx-cursor: hand;");
        restartBtn.setOnAction(e -> {
            for (Player p : players) p.score = 0;
            initializeGame();
            gameState = GameState.READY;
            renderReadyState();
        });

        resultsBox.getChildren().add(new Region()); // Spacer
        resultsBox.getChildren().add(restartBtn);
        mainContent.getChildren().add(resultsBox);
    }

    // --- GAME LOGIC ---

    private StackPane createCardNode(MemoryCard card) {
        StackPane pane = new StackPane();
        pane.setPrefSize(120, 120);

        // Rückseite der Karte
        Rectangle back = new Rectangle(120, 120, Color.web("#1e293b"));
        back.setArcWidth(16);
        back.setArcHeight(16);
        back.setStyle("-fx-fill: linear-gradient(to bottom right, #667eea, #764ba2);"); // Wie im CSS

        Text questionMark = new Text("?");
        questionMark.setStyle("-fx-fill: rgba(255,255,255,0.3); -fx-font-size: 48px; -fx-font-weight: bold;");
        StackPane backSide = new StackPane(back, questionMark);

        // Vorderseite der Karte
        Rectangle frontBg = new Rectangle(120, 120);
        frontBg.setArcWidth(16);
        frontBg.setArcHeight(16);
        frontBg.setStyle("-fx-fill: linear-gradient(to bottom right, #2c3e50, #34495e);"); // Wie im CSS

        Node frontContent;
        if (card.isImage) {
            ImageView imgView = new ImageView();
            imgView.setFitWidth(100);
            imgView.setFitHeight(100);
            imgView.setPreserveRatio(true);

            try {
                String path = card.content.trim();
                String imageUrl;

                if (path.toLowerCase().startsWith("http://") || path.toLowerCase().startsWith("https://") || path.toLowerCase().startsWith("data:image/")) {
                    imageUrl = path;
                } else {
                    // Datei-Pfad auflösen
                    File imgFile = new File(path);

                    // Falls es ein relativer Pfad ist und ein Ordner bekannt ist:
                    if (!imgFile.isAbsolute() && folder != null) {
                        imgFile = new File(folder, path);
                    }

                    imageUrl = imgFile.toURI().toString();
                }

                // 'backgroundLoading = true' verhindert Ruckler beim Aufdecken
                Image img = new Image(imageUrl, true);

                // Error-Listener für klares Debugging
                img.exceptionProperty().addListener((obs, oldEx, newEx) -> {
                    if (newEx != null) {
                        System.err.println("Fehler beim Laden des Bildes (" + imageUrl + "): " + newEx.getMessage());
                    }
                });

                imgView.setImage(img);
                frontContent = imgView;
            } catch (Exception e) {
                System.err.println("Fehler bei Bild-URI Erstellung: " + e.getMessage());
                Text errText = new Text("Bild-Fehler");
                errText.setStyle("-fx-fill: #ff6464; -fx-font-size: 11px;");
                frontContent = errText;
            }
        } else {
            Text text = new Text(card.content);
            text.setWrappingWidth(100);
            text.setTextAlignment(TextAlignment.CENTER);
            text.setStyle("-fx-font-size: 14px; -fx-fill: white; -fx-font-weight: bold;");
            frontContent = text;
        }

        StackPane frontSide = new StackPane(frontBg, frontContent);
        frontSide.setVisible(false);

        pane.getChildren().addAll(backSide, frontSide);
        pane.setStyle("-fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 4);");

        // Callback um den Status visuell darzustellen
        card.updateViewCallback = () -> {
            if (card.isMatched) {
                frontSide.setVisible(true);
                backSide.setVisible(false);
                frontBg.setStroke(Color.web("#eab308"));
                frontBg.setStrokeWidth(3);
                pane.setOpacity(0.5); // Card matched CSS
                pane.setStyle("-fx-cursor: default;");
            } else if (card.isFlipped) {
                frontSide.setVisible(true);
                backSide.setVisible(false);
            } else {
                frontSide.setVisible(false);
                backSide.setVisible(true);
            }
        };

        pane.setOnMouseClicked(e -> {
            if (card.isFlipped || card.isMatched || flippedCards.size() >= 2) return;

            card.isFlipped = true;
            card.updateViewCallback.run();
            flippedCards.add(card);

            if (flippedCards.size() == 2) {
                PauseTransition pause = new PauseTransition(Duration.seconds(1.0));
                pause.setOnFinished(ev -> checkMatch());
                pause.play();
            }
        });

        card.updateViewCallback.run(); // Initiale Sichtbarkeit

        return pane;
    }

    private void checkMatch() {
        MemoryCard c1 = flippedCards.get(0);
        MemoryCard c2 = flippedCards.get(1);

        boolean isMatch = false;
        for (MemoryPair p : pairs) {
            boolean matchDirect = p.first.equals(c1.content) && p.second.equals(c2.content);
            boolean matchReverse = p.second.equals(c1.content) && p.first.equals(c2.content);
            if (matchDirect || matchReverse) {
                isMatch = true;
                break;
            }
        }

        if (isMatch) {
            c1.isMatched = true;
            c2.isMatched = true;
            players.get(currentPlayerIndex).score++;
            // Gleicher Spieler darf nochmal
        } else {
            c1.isFlipped = false;
            c2.isFlipped = false;
            // Nächster Spieler ist dran
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
            currentPlayerNameText.setText(players.get(currentPlayerIndex).name);
        }

        flippedCards.clear();
        c1.updateViewCallback.run();
        c2.updateViewCallback.run();

        renderScoreboard();

        // Gewinn-Prüfung
        boolean allMatched = cards.stream().allMatch(c -> c.isMatched);
        if (allMatched) {
            gameState = GameState.FINISHED;
            renderFinishedState();
        }
    }

    // --- DATENMODELLE ---

    private static class Player {
        String id = UUID.randomUUID().toString();
        String name;
        int score = 0;

        Player(String name) {
            this.name = name;
        }
    }

    private static class MemoryPair {
        String first;
        String second;
        boolean firstIsImage;
        boolean secondIsImage;

        MemoryPair(String first, String second, boolean firstIsImage, boolean secondIsImage) {
            this.first = first;
            this.second = second;
            this.firstIsImage = firstIsImage;
            this.secondIsImage = secondIsImage;
        }
    }

    private static class MemoryCard {
        int id;
        String content;
        boolean isImage;
        boolean isFlipped = false;
        boolean isMatched = false;
        Runnable updateViewCallback; // UI Update Bindung

        MemoryCard(int id, String content, boolean isImage) {
            this.id = id;
            this.content = content;
            this.isImage = isImage;
        }
    }
}