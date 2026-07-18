package de.agrabellus;

import de.agrabellus.komp.HomeView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Wir erstellen unsere neue Home-Komponente
        HomeView homeView = new HomeView(primaryStage);

        Scene scene = new Scene(homeView, 1100, 800);
        scene.setFill(Color.TRANSPARENT);

        // CSS einbinden
        try {
            var cssResource = getClass().getResource("/style.css");
            if (cssResource != null) {
                scene.getStylesheets().add(cssResource.toExternalForm());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}