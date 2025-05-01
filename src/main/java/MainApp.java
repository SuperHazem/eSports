import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the authentication view
        Parent root = FXMLLoader.load(getClass().getResource("/AuthenticationView.fxml"));

        // Create the scene
        Scene scene = new Scene(root, 1000, 600);

        // Add the CSS file to the scene
        scene.getStylesheets().add(getClass().getResource("/styles/authentication.css").toExternalForm());

        // Set up the stage
        primaryStage.setTitle("eSports Arena Manager - Authentification");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}