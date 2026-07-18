package de.agrabellus.komp;

import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Quizansicht fuer Dateien im Format "Frage | Antwort". */
public class AnklickbarView extends ScrollPane {
    private final Stage stage;
    private final String dateiName;
    private final File quizFile;
    private final Map<String, String> questionAnswers = new LinkedHashMap<>();
    private final List<String> remainingQuestions = new ArrayList<>();
    private final Set<String> answeredCorrectly = new LinkedHashSet<>();

    private final VBox page = new VBox(16);
    private final VBox content = new VBox(16);
    private final Label correctValue = new Label();
    private final Label timeValue = new Label("0s");
    private Timeline timer;
    private MediaPlayer activeAudioPlayer;
    private int currentQuestionIndex;
    private int correctCount;
    private int elapsedSeconds;

    public AnklickbarView(Stage stage, String dateiName) {
        this(stage, dateiName, new File(dateiName));
    }

    public AnklickbarView(Stage stage, String dateiName, File quizFile) {
        this.stage = stage;
        this.dateiName = dateiName;
        this.quizFile = quizFile;
        configurePage();
        loadQuiz();
        showReadyState();
    }

    private void configurePage() {
        setFitToWidth(true);
        setStyle("-fx-background-color: linear-gradient(from 0% 0% to 0% 100%, #081122 0%, #0b1624 100%);"
                + "-fx-border-color: transparent;");
        page.getStyleClass().add("anklickbar-page");
        page.setPadding(new Insets(16));
        page.setFillWidth(true);
        page.minHeightProperty().bind(heightProperty().subtract(2));
        setContent(page);
    }

    private void loadQuiz() {
        if (quizFile == null || !quizFile.isFile()) {
            return;
        }
        try {
            for (String rawLine : Files.readAllLines(quizFile.toPath(), StandardCharsets.UTF_8)) {
                String line = rawLine.trim();
                int separator = line.indexOf('|');
                if (line.isEmpty() || separator < 0) continue;
                String question = line.substring(0, separator).trim();
                String answer = line.substring(separator + 1).trim();
                if (!question.isEmpty() && !answer.isEmpty()) questionAnswers.put(question, answer);
            }
            remainingQuestions.addAll(questionAnswers.keySet());
            Collections.shuffle(remainingQuestions);
        } catch (Exception ignored) {
            // Ein lesbarer Hinweis wird in showReadyState angezeigt.
        }
    }

    private void showReadyState() {
        stopTimer();
        page.getChildren().setAll(createHeader());
        if (questionAnswers.isEmpty()) {
            Label message = new Label("Die Quizdatei konnte nicht geladen werden oder enthält keine Fragen.");
            message.getStyleClass().add("quiz-error");
            page.getChildren().add(centered(message));
            return;
        }

        Label title = new Label("Quiz bereit");
        title.getStyleClass().add("state-title");
        Label detail = new Label(questionAnswers.size() + " Fragen warten auf dich");
        detail.getStyleClass().add("state-detail");
        Button start = new Button("Quiz starten");
        start.getStyleClass().add("start-btn");
        start.setOnAction(event -> startQuiz());
        VBox card = new VBox(12, title, detail, start);
        card.getStyleClass().add("state-card");
        card.setAlignment(Pos.CENTER);
        page.getChildren().add(centered(card));
    }

    private VBox createHeader() {
        Button back = new Button("← Zurück");
        back.getStyleClass().add("nav-btn");
        back.setOnAction(event -> stage.getScene().setRoot(new HomeView(stage)));
        Label title = new Label("Anklickbar");
        title.getStyleClass().add("quiz-title");
        Label file = new Label(dateiName);
        file.getStyleClass().add("quiz-filename");
        VBox labels = new VBox(2, title, file);
        HBox header = new HBox(12, back, labels);
        header.setAlignment(Pos.CENTER_LEFT);
        return new VBox(header);
    }

    private Region centered(javafx.scene.Node node) {
        VBox wrapper = new VBox(node);
        wrapper.setAlignment(Pos.CENTER);
        VBox.setVgrow(wrapper, Priority.ALWAYS);
        wrapper.setMinHeight(350);
        return wrapper;
    }

    private void startQuiz() {
        correctCount = 0;
        elapsedSeconds = 0;
        currentQuestionIndex = 0;
        answeredCorrectly.clear();
        correctValue.setText("0/" + questionAnswers.size());
        timeValue.setText("0s");
        startTimer();
        showQuiz();
    }

    private void showQuiz() {
        page.getChildren().setAll(createHeader(), createStats());
        content.getChildren().setAll(createQuestionSection(), createAnswersSection());
        page.getChildren().add(content);
    }

    private HBox createStats() {
        HBox stats = new HBox(32, createStat("Richtig", correctValue), createStat("Zeit", timeValue));
        stats.getStyleClass().add("stats-section");
        return stats;
    }

    private VBox createStat(String label, Label value) {
        Label caption = new Label(label);
        caption.getStyleClass().add("stat-label");
        value.getStyleClass().add("stat-value");
        VBox stat = new VBox(3, caption, value);
        stat.getStyleClass().add("stat");
        stat.setAlignment(Pos.CENTER);
        return stat;
    }

    private VBox createQuestionSection() {
        String question = currentQuestion();
        Button previous = new Button("← Vorherige");
        previous.getStyleClass().add("nav-btn");
        previous.setOnAction(event -> changeQuestion(-1));
        Label counter = new Label((currentQuestionIndex + 1) + " / " + remainingQuestions.size());
        counter.getStyleClass().add("question-counter");
        Button next = new Button("Nächste →");
        next.getStyleClass().add("nav-btn");
        next.setOnAction(event -> changeQuestion(1));
        Region leftSpacer = new Region();
        Region rightSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);
        HBox navigation = new HBox(8, previous, leftSpacer, counter, rightSpacer, next);
        navigation.setAlignment(Pos.CENTER);
        VBox questionBox = new VBox(createValueNode(question, "question"));
        questionBox.getStyleClass().add("question-box");
        questionBox.setAlignment(Pos.CENTER);
        VBox section = new VBox(16, navigation, questionBox);
        section.getStyleClass().add("question-section");
        return section;
    }

    private VBox createAnswersSection() {
        Label title = new Label("Antworten");
        title.getStyleClass().add("answers-title");
        FlowPane answers = new FlowPane(12, 12);
        answers.getStyleClass().add("answers-list");
        for (String answer : new LinkedHashSet<>(questionAnswers.values())) {
            Button button = new Button();
            button.getStyleClass().add("answer-btn");
            button.setGraphic(createValueNode(answer, "answer"));
            button.setOnAction(event -> selectAnswer(answer));
            if (answeredCorrectly.contains(answer)) {
                button.setDisable(true);
                button.getStyleClass().add("correct");
            }
            answers.getChildren().add(button);
        }
        VBox section = new VBox(12, title, answers);
        section.getStyleClass().add("answers-section");
        VBox.setVgrow(answers, Priority.ALWAYS);
        return section;
    }

    private javafx.scene.Node createValueNode(String value, String type) {
        if (isImage(value)) {
            File imageFile = resolveResourceFile(value);
            if (!imageFile.isFile()) {
                logError("Bilddatei nicht gefunden", imageFile, null);
                return createTextNode("Bild konnte nicht geladen werden: " + value, type);
            }
            Image imageResource = new Image(imageFile.toURI().toString(), true);
            imageResource.errorProperty().addListener((observable, previous, hasError) -> {
                if (hasError) logError("Bild konnte nicht geladen werden", imageFile, imageResource.getException());
            });
            ImageView image = new ImageView(imageResource);
            image.setPreserveRatio(true);
            image.setFitHeight("question".equals(type) ? 220 : 140);
            image.setFitWidth(450);
            return image;
        }
        if (isAudio(value)) {
            File audioFile = resolveResourceFile(value);
            Button playButton = new Button("▶ Audio abspielen");
            playButton.getStyleClass().add("audio-btn");
            playButton.setOnAction(event -> playAudio(audioFile));
            return playButton;
        }
        return createTextNode(value, type);
    }

    private Label createTextNode(String value, String type) {
        Label text = new Label(value);
        text.getStyleClass().add("question".equals(type) ? "question-text" : "answer-text");
        text.setWrapText(true);
        text.setTextAlignment(TextAlignment.CENTER);
        return text;
    }

    private boolean isImage(String value) {
        return value != null && value.toLowerCase().matches(".*\\.(png|jpe?g|webp|gif|bmp)$");
    }

    private boolean isAudio(String value) {
        return value != null && value.toLowerCase().matches(".*\\.(mp3|wav|ogg|m4a|aac)$");
    }

    private File resolveResourceFile(String value) {
        File resource = new File(value);
        if (resource.isAbsolute()) return resource;
        File parent = quizFile == null ? null : quizFile.getParentFile();
        if (parent == null) return resource;

        File quizFolderResource = new File(new File(parent, getExplicitQuizName()), value);
        if (quizFolderResource.isFile()) return quizFolderResource;

        // Unterstuetzt auch Ordner, die den vollstaendigen Dateinamen ohne .txt tragen.
        File fullQuizNameResource = new File(new File(parent, getQuizFileBaseName()), value);
        if (fullQuizNameResource.isFile()) return fullQuizNameResource;

        return new File(parent, value);
    }

    private String getQuizFileBaseName() {
        return new File(dateiName.replace('\\', '/')).getName().replaceFirst("(?i)\\.txt$", "");
    }

    private String getExplicitQuizName() {
        return getQuizFileBaseName().replaceFirst("(?i)^anklickbar", "").trim();
    }

    private void playAudio(File audioFile) {
        if (!audioFile.isFile()) {
            logError("Audiodatei nicht gefunden", audioFile, null);
            return;
        }
        try {
            if (activeAudioPlayer != null) {
                activeAudioPlayer.stop();
                activeAudioPlayer.dispose();
            }
            Media media = new Media(audioFile.toURI().toString());
            media.setOnError(() -> logError("Audiodatei konnte nicht gelesen werden", audioFile, media.getError()));
            activeAudioPlayer = new MediaPlayer(media);
            activeAudioPlayer.setOnError(() -> logError("Audio konnte nicht abgespielt werden", audioFile, activeAudioPlayer.getError()));
            activeAudioPlayer.setOnEndOfMedia(() -> activeAudioPlayer.dispose());
            activeAudioPlayer.play();
        } catch (Exception exception) {
            logError("Audio konnte nicht vorbereitet werden", audioFile, exception);
        }
    }

    private void logError(String message, File resource, Throwable exception) {
        System.err.println("[Anklickbar] " + message + ": " + resource.getAbsolutePath());
        if (exception != null) exception.printStackTrace(System.err);
    }

    private String currentQuestion() {
        return remainingQuestions.isEmpty() ? "" : remainingQuestions.get(currentQuestionIndex);
    }

    private void changeQuestion(int direction) {
        if (remainingQuestions.isEmpty()) return;
        currentQuestionIndex = Math.floorMod(currentQuestionIndex + direction, remainingQuestions.size());
        showQuiz();
    }

    private void selectAnswer(String answer) {
        String question = currentQuestion();
        if (answer.equals(questionAnswers.get(question))) {
            correctCount++;
            correctValue.setText(correctCount + "/" + questionAnswers.size());
            answeredCorrectly.add(answer);
            removeCurrentQuestionAndContinue();
        } else {
            content.getStyleClass().add("wrong-effect");
            PauseTransition pause = new PauseTransition(Duration.millis(500));
            pause.setOnFinished(event -> {
                content.getStyleClass().remove("wrong-effect");
                removeCurrentQuestionAndContinue();
            });
            pause.play();
        }
    }

    private void removeCurrentQuestionAndContinue() {
        remainingQuestions.remove(currentQuestionIndex);
        if (currentQuestionIndex >= remainingQuestions.size() && !remainingQuestions.isEmpty()) {
            currentQuestionIndex--;
        }
        if (remainingQuestions.isEmpty()) {
            showFinishedState();
            return;
        }
        showQuiz();
    }

    private void showFinishedState() {
        stopTimer();
        Label title = new Label("Quiz abgeschlossen!");
        title.getStyleClass().add("finished-title");
        Label correct = new Label("Richtige Antworten\n" + correctCount + "/" + questionAnswers.size());
        Label time = new Label("Zeit\n" + elapsedSeconds + "s");
        correct.getStyleClass().add("result-item");
        time.getStyleClass().add("result-item");
        Button back = new Button("Zurück");
        back.getStyleClass().add("back-btn");
        back.setOnAction(event -> stage.getScene().setRoot(new HomeView(stage)));
        VBox results = new VBox(18, title, correct, time, back);
        results.getStyleClass().add("results-container");
        results.setAlignment(Pos.CENTER);
        page.getChildren().setAll(createHeader(), centered(results));
    }

    private void startTimer() {
        stopTimer();
        timer = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            elapsedSeconds++;
            timeValue.setText(elapsedSeconds + "s");
        }));
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }

    private void stopTimer() {
        if (timer != null) timer.stop();
    }
}
