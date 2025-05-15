package controllers;

import dao.*;
import javafx.animation.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import models.*;
import utils.IconGenerator;
import utils.SceneController;
import utils.UserSession;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

public class SpectateurController implements Initializable {

    // Main layout components
    @FXML private BorderPane spectateurBorderPane;
    @FXML private VBox sidebar;
    @FXML private StackPane contentArea;
    @FXML private ImageView logoImageView;
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;
    @FXML private ImageView userAvatarImage;

    // Navigation buttons
    @FXML private Button calendarBtn;
    @FXML private Button liveScoreBtn;
    @FXML private Button ticketsBtn;
    @FXML private Button reclamationsBtn;
    @FXML private Button eventSocialBtn;
    
    // Event Social components
    @FXML private TableView<EventSocial> eventTable;
    @FXML private TableColumn<EventSocial, String> nomColumn;
    @FXML private TableColumn<EventSocial, LocalDate> dateColumn;
    @FXML private TableColumn<EventSocial, String> lieuColumn;
    @FXML private TableColumn<EventSocial, String> descriptionColumn;
    @FXML private TableColumn<EventSocial, String> participantsColumn;
    @FXML private TableColumn<EventSocial, Void> actionsColumn;
    @FXML private TextField searchNameField;
    @FXML private DatePicker searchDateField;
    @FXML private StackPane notifyArea;
    @FXML private Label notifyLabel;

    // Controller state
    private Map<String, Parent> cachedViews = new HashMap<>();
    private Button currentActiveButton;
    private Utilisateur currentUser;
    
    // Event Social state
    private EventSocialDAO eventDAO;
    private ParticipationEventDAO pDAO;
    private ObservableList<EventSocial> eventData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize DAOs for event functionality
        eventDAO = new EventSocialDAOImpl();
        pDAO = new ParticipationEventDAOImpl();
        
        styleLogoImage();
        styleUserAvatar();

        UserSession session = UserSession.getInstance();
        if (session.isLoggedIn()) {
            setCurrentUser(session.getCurrentUser());
        }

        // Set initial active button and load default view
        currentActiveButton = calendarBtn; // Default view changed to calendar
        if (currentActiveButton != null) {
            currentActiveButton.getStyleClass().add("active-nav-button");
            try {
                loadView("calendaTab"); // Default view name changed to calendaTab
            } catch (IOException e) {
                System.err.println("Error loading default view for Spectateur: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
             System.err.println("Default button (calendarBtn) is null.");
        }
    }

    private void styleLogoImage() {
        if (logoImageView != null) {
            // Attempt to load the image if not already set by FXML
            if (logoImageView.getImage() == null) {
                 try {
                    Image logo = new Image(getClass().getResourceAsStream("/images/logo.png"));
                    logoImageView.setImage(logo);
                } catch (Exception e) {
                    System.err.println("Error loading logo image for Spectateur sidebar: " + e.getMessage());
                }
            }
            if (logoImageView.getImage() != null) {
                double radius = Math.min(logoImageView.getFitWidth(), logoImageView.getFitHeight()) / 2;
                if (radius <= 0) radius = 50; // Default radius if fitWidth/Height are 0
                Circle clip = new Circle(radius, radius, radius);
                logoImageView.setClip(clip);

                DropShadow glow = new DropShadow();
                glow.setColor(Color.rgb(0, 247, 255, 0.7));
                glow.setRadius(15);
                logoImageView.setEffect(glow);

                logoImageView.setPreserveRatio(true);
                logoImageView.setSmooth(true);
                logoImageView.setCache(true);
            }
        }
    }

    private void styleUserAvatar() {
        if (userAvatarImage != null) {
            double radius = Math.min(userAvatarImage.getFitWidth(), userAvatarImage.getFitHeight()) / 2;
            if (radius <= 0) radius = 20; // Default radius
            Circle clip = new Circle(radius, radius, radius);
            userAvatarImage.setClip(clip);
            userAvatarImage.setPreserveRatio(true);
            userAvatarImage.setSmooth(true);
            userAvatarImage.setCache(true);
        }
    }

    public void setCurrentUser(Utilisateur user) {
        this.currentUser = user;
        if (user != null) {
            userNameLabel.setText(user.getPrenom() + " " + user.getNom());
            userRoleLabel.setText(user.getRole().toString());

            boolean isGoogleUser = user.getMotDePasseHash() != null && user.getMotDePasseHash().equals("google-oauth");

            if (user.getProfilePicturePath() != null && !user.getProfilePicturePath().isEmpty()) {
                try {
                    File imageFile = new File(user.getProfilePicturePath());
                    if (imageFile.exists()) {
                        Image image = new Image(imageFile.toURI().toString());
                        userAvatarImage.setImage(image);
                    } else {
                        generateProfileIcon(user);
                    }
                } catch (Exception e) {
                    System.err.println("Error loading profile image in Spectateur sidebar: " + e.getMessage());
                    generateProfileIcon(user);
                }
            } else {
                generateProfileIcon(user);
            }
        }
    }

    private void generateProfileIcon(Utilisateur user) {
        try {
            String initials = String.valueOf(user.getPrenom().charAt(0)) +
                             (user.getNom() != null && !user.getNom().isEmpty() ?
                              String.valueOf(user.getNom().charAt(0)) : "");
            String fullName = user.getPrenom() + user.getNom();
            int hash = fullName.hashCode();
            Color avatarColor = Color.hsb((hash % 360), 0.8, 0.9);
            Image generatedIcon = IconGenerator.createTextIcon(initials.toUpperCase(), avatarColor);
            userAvatarImage.setImage(generatedIcon);
        } catch (Exception e) {
            System.err.println("Error generating profile icon for Spectateur: " + e.getMessage());
        }
    }

    @FXML
    private void handleNavigation(ActionEvent event) throws IOException {
        Button clickedButton = (Button) event.getSource();

        if (currentActiveButton != null) {
            currentActiveButton.getStyleClass().remove("active-nav-button");
        }
        clickedButton.getStyleClass().add("active-nav-button");
        currentActiveButton = clickedButton;

        String viewName = "";
        if (clickedButton == calendarBtn) {
            viewName = "calendaTab"; // Corrected to match FXML file name
        } else if (clickedButton == liveScoreBtn) {
            viewName = "LiveScoreTab"; // Corrected to match FXML file name
        } else if (clickedButton == ticketsBtn) {
            viewName = "ticketView";
        } else if (clickedButton == reclamationsBtn) {
            viewName = "ReclamationView";
        } else if (clickedButton == eventSocialBtn) {
            viewName = "SpectatorEventView";
        }

        if (!viewName.isEmpty()) {
            loadView(viewName);
        }
    }
    
    // Event Social Methods
    
    private void initializeEventComponents() {
        if (eventTable == null) {
            System.err.println("Event table is null, cannot initialize components");
            return;
        }
        
        // Initialize table columns
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        lieuColumn.setCellValueFactory(new PropertyValueFactory<>("lieu"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        
        // Set up participants column to display count
        participantsColumn.setCellValueFactory(cellData -> {
            EventSocial event = cellData.getValue();
            int count = pDAO.getParticipantCount(event.getId());
            int capacity = event.getCapacite();
            return new SimpleStringProperty(count + "/" + capacity);
        });
        
        // Set up actions column with buttons
        actionsColumn.setCellFactory(column -> new TableCell<EventSocial, Void>() {
            private final Button participateButton = new Button("Participer");
            private final Label joinedLabel = new Label("Déjà rejoint");
            
            {
                participateButton.getStyleClass().add("participate-button");
                joinedLabel.getStyleClass().add("joined-label");
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                
                EventSocial event = getTableView().getItems().get(getIndex());
                int userId = UserSession.getInstance().getCurrentUser().getId();
                
                if (pDAO.isUserParticipating(userId, event.getId())) {
                    setGraphic(joinedLabel);
                } else {
                    participateButton.setOnAction(e -> joinEvent(event));
                    setGraphic(participateButton);
                }
            }
        });
        
        // Set up search functionality
        searchNameField.textProperty().addListener((obs, oldVal, newVal) -> rechercherEvent());
        searchDateField.valueProperty().addListener((obs, oldVal, newVal) -> rechercherEvent());
        
        // Load events
        loadEvents();
        
        // Set up notification area
        notifyArea.setVisible(false);
    }
    
    private void loadEvents() {
        try {
            List<EventSocial> events = eventDAO.getAllEvents();
            eventData.clear();
            eventData.addAll(events);
            eventTable.setItems(eventData);
        } catch (Exception e) {
            System.err.println("Error loading events: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void rechercherEvent() {
        String searchName = searchNameField.getText().toLowerCase();
        LocalDate searchDate = searchDateField.getValue();
        
        List<EventSocial> allEvents = eventDAO.getAllEvents();
        List<EventSocial> filteredEvents = allEvents.stream()
                .filter(event -> {
                    boolean nameMatch = searchName.isEmpty() || 
                            event.getNom().toLowerCase().contains(searchName);
                    boolean dateMatch = searchDate == null || 
                            event.getDate().equals(searchDate);
                    return nameMatch && dateMatch;
                })
                .collect(Collectors.toList());
        
        eventData.clear();
        eventData.addAll(filteredEvents);
        eventTable.setItems(eventData);
    }
    
    private void joinEvent(EventSocial event) {
        try {
            int userId = UserSession.getInstance().getCurrentUser().getId();
            int eventId = event.getId();
            
            // Check if user is already participating
            if (pDAO.isUserParticipating(userId, eventId)) {
                showNotification("Vous participez déjà à cet événement", "warning");
                return;
            }
            
            // Check if event is at capacity
            int currentParticipants = pDAO.getParticipantCount(eventId);
            if (currentParticipants >= event.getCapacite()) {
                showNotification("Cet événement est complet", "error");
                return;
            }
            
            // Add participation
            ParticipationEvent participation = new ParticipationEvent(0, userId, eventId, LocalDateTime.now());
            pDAO.addParticipation(participation);
            
            // Show success notification
            showNotification("Vous avez rejoint l'événement avec succès!", "success");
            
            // Refresh table to update UI
            loadEvents();
            
        } catch (Exception e) {
            showNotification("Erreur lors de la participation: " + e.getMessage(), "error");
            e.printStackTrace();
        }
    }
    
    private void showNotification(String message, String type) {
        notifyLabel.setText(message);
        notifyArea.getStyleClass().clear();
        notifyArea.getStyleClass().addAll("notification", "notification-" + type);
        notifyArea.setVisible(true);
        
        // Create fade out animation
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(3), notifyArea);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setDelay(Duration.seconds(2));
        fadeOut.setOnFinished(e -> notifyArea.setVisible(false));
        fadeOut.play();
    }

    private void loadView(String viewName) throws IOException {
        if (cachedViews.containsKey(viewName)) {
            contentArea.getChildren().clear();
            contentArea.getChildren().add(cachedViews.get(viewName));
            return;
        }

        String fxmlPath = "";
        // Adjust paths based on actual FXML file locations and naming conventions
        if (viewName.equalsIgnoreCase("calendaTab")) {
            fxmlPath = "/user/CalendarTab.fxml"; // Path from UserInterfaceController
        } else if (viewName.equalsIgnoreCase("LiveScoreTab")) {
            fxmlPath = "/user/LiveScoreTab.fxml"; // Path from UserInterfaceController
        } else if (viewName.equalsIgnoreCase("ticketView")) {
            fxmlPath = "/TicketView.fxml";
        } else if (viewName.equalsIgnoreCase("ReclamationView")) {
            fxmlPath = "/ReclamationView.fxml";
        } else if (viewName.equalsIgnoreCase("profile")) {
            fxmlPath = "/profile.fxml";
        } else if (viewName.equalsIgnoreCase("SpectatorEventView")) {
            fxmlPath = "/SpectatorEventView.fxml";
            // For this view, we need to set the controller manually to this instance
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setController(this); // Use this controller instance
            Parent view = loader.load();
            
            // Initialize event table components after loading
            initializeEventComponents();
            
            cachedViews.put(viewName, view);
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
            return;
        } else {
            // Fallback for general views if specific paths are not matched
            fxmlPath = "/views/" + viewName + ".fxml";
        }
        
        System.out.println("SpectateurController: Attempting to load FXML from: " + fxmlPath);

        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent view = loader.load();

        if (view == null) {
            throw new IOException("Could not find FXML file for view: " + viewName + " at path: " + fxmlPath);
        }

        cachedViews.put(viewName, view);
        contentArea.getChildren().clear();
        contentArea.getChildren().add(view);
    }

    @FXML
    void openProfileView() {
        try {
            loadView("profile");
            if (currentActiveButton != null) {
                currentActiveButton.getStyleClass().remove("active-nav-button");
            }
            // No button to set as active for profile view in this layout
        } catch (IOException e) {
            System.err.println("Error loading profile view from Spectateur layout: " + e.getMessage());
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to load profile");
            alert.setContentText("An error occurred while trying to load your profile.");
            alert.showAndWait();
        }
    }

    @FXML
    void logout(ActionEvent event) throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout Confirmation");
        alert.setHeaderText("Are you sure you want to log out?");
        alert.setContentText("You will be returned to the login screen.");

        ButtonType yesButton = new ButtonType("Yes");
        ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(yesButton, noButton);

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == yesButton) {
            UserSession.getInstance().cleanUserSession();
            // Assuming SceneController.loadPage can handle this path
            SceneController.loadPage("/AuthenticationView.fxml"); 
        }
    }
}