package demo;

import components.LivescoreView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Standalone demo application for the E-Sports Livescore feature.
 * This class can be run independently to test the livescore functionality.
 */
public class LivescoreDemoApp extends Application {

    private LivescoreView livescoreView;

    @Override
    public void start(Stage primaryStage) {
        // Create the livescore view
        livescoreView = new LivescoreView();
        
        // Create the scene
        Scene scene = new Scene(livescoreView, 900, 600);
        
        // Configure the stage
        primaryStage.setTitle("E-Sports Livescore Demo");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    @Override
    public void stop() {
        // Properly shut down the livescore service
        if (livescoreView != null) {
            livescoreView.shutdown();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}