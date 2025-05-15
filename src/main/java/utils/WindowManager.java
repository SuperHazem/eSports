package utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.Stack;


import java.io.IOException;

public class WindowManager {
    private static WindowManager instance;
    private Stage primaryStage;
    private final Stack<Stage> windowStack = new Stack<>();
    private final Stack<Scene> sceneStack = new Stack<>();

    public void initialize(Stage stage) {
        this.primaryStage = stage;
    }

    private WindowManager() {}

    public static WindowManager getInstance() {
        if (instance == null) instance = new WindowManager();
        return instance;
    }



    public void openWindow(String fxmlPath, String title, boolean maximized) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Save current scene
            if (primaryStage.getScene() != null) {
                sceneStack.push(primaryStage.getScene());
            }


            Scene newScene = new Scene(root);

            String css = getClass().getResource("/styles/styles.css").toExternalForm();
            newScene.getStylesheets().add(css);

            primaryStage.setScene(newScene);
            primaryStage.setTitle(title);
            primaryStage.setMaximized(maximized);
            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeCurrentWindow() {
        if (windowStack.size() > 1) {
            Stage current = windowStack.pop();
            current.close();
            windowStack.peek().show();
        }
    }

    public void printNavigationStack() {
        System.out.println("Window Stack:");
        for (Stage stage : windowStack) {
            System.out.println("- " + stage.getTitle());
        }
    }
    public void goBack() {
        if (!sceneStack.isEmpty()) {
            Scene previousScene = sceneStack.pop();
            primaryStage.setScene(previousScene);
        }
    }
}
