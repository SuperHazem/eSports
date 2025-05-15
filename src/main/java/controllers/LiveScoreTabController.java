package controllers;

import components.LivescoreView;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class LiveScoreTabController {

    @FXML
    private VBox livescoreContainer;

    private LivescoreController tableViewController;
    private LivescoreView cardView;
    private VBox tableView;
    private boolean isTableViewActive = true;

    @FXML
    public void initialize() {
        try {
            // Clear any existing children
            livescoreContainer.getChildren().clear();

            // Create view toggle controls
            ToggleGroup viewToggleGroup = new ToggleGroup();

            ToggleButton tableViewButton = new ToggleButton("Table View");
            tableViewButton.setToggleGroup(viewToggleGroup);

            ToggleButton cardViewButton = new ToggleButton("Card View");
            cardViewButton.setToggleGroup(viewToggleGroup);
            cardViewButton.setSelected(true);

            HBox toggleContainer = new HBox(10, tableViewButton, cardViewButton);
            toggleContainer.setAlignment(Pos.CENTER_RIGHT);
            toggleContainer.setPadding(new javafx.geometry.Insets(5, 10, 5, 10));

            // Initialize both views
            initializeTableView();
            initializeCardView();

            // Create content container that will go inside the scroll pane
            VBox contentContainer = new VBox();
            VBox.setVgrow(contentContainer, Priority.ALWAYS);

            // Create ScrollPane
            ScrollPane scrollPane = new ScrollPane(contentContainer);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefViewportHeight(600); // Set a reasonable height
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS); // Always show vertical scrollbar
            VBox.setVgrow(scrollPane, Priority.ALWAYS);

            // Add components to the main container
            livescoreContainer.getChildren().addAll(toggleContainer, scrollPane);

            // Set up toggle listeners
            tableViewButton.setOnAction(e -> {
                contentContainer.getChildren().clear();
                contentContainer.getChildren().add(tableView);
                isTableViewActive = true;
            });

            cardViewButton.setOnAction(e -> {
                contentContainer.getChildren().clear();
                contentContainer.getChildren().add(cardView);
                isTableViewActive = false;
            });

            // Show card view by default
            contentContainer.getChildren().add(cardView);
            isTableViewActive = false;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeTableView() {
        try {
            // Load the Livescore.fxml file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Livescore.fxml"));
            tableView = loader.load();

            // Get the controller
            tableViewController = loader.getController();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initializeCardView() {
        // Create the card view
        cardView = new LivescoreView();
    }
}