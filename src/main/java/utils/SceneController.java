package utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneController {

    private static Stage primaryStage;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static void loadPage(String fxmlPath) throws IOException {
        Parent root = FXMLLoader.load(SceneController.class.getResource(fxmlPath));
        Scene scene = new Scene(root);

        // Add CSS if needed
        scene.getStylesheets().add(SceneController.class.getResource("/styles/application.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.show();
    }
}