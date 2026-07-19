package de.agrabellus.komp;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class AnklickbarErstellenView extends VBox {
    public AnklickbarErstellenView(Stage stage) {

        this.setStyle("-fx-background-color: linear-gradient(to bottom, #081122 0%, #0b1624 100%);");
        this.setPadding(new Insets(20));
        this.setSpacing(20);
        this.setAlignment(Pos.TOP_CENTER);
        this.setMinHeight(800);

        Button backBtn = new Button("◀ Zurück zum Menü");
        backBtn.getStyleClass().add("load-btn");
        backBtn.setOnAction(e -> stage.getScene().setRoot(new HomeView(stage)));

        Text title = new Text("✨ Neues Anklickbar-Quiz erstellen");
        title.setStyle("-fx-font-size: 2em; -fx-fill: #4fb871; -fx-font-weight: bold;");

        Text info = new Text("Hier kannst du eigene \"Frage\"-Antwort Paare definieren und speichern.");
        info.setStyle("-fx-fill: #cfe8ff; -fx-font-size: 1.1em;");

        Text subinfo = new Text("Es sind auch Bilder und Sounds möglich, dafür bitte das den Namen mit Endung angeben.");
        subinfo.setStyle("-fx-fill: #cfe8ff; -fx-font-size: 1.1em;");

        this.getChildren().addAll(backBtn, title, info, subinfo);


    }
}
