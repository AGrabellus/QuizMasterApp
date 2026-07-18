package de.agrabellus.komp;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class MemoryView extends VBox {
    public MemoryView(Stage stage, String dateiName) {
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

        this.getChildren().addAll(backBtn, title, fileText);
    }
}