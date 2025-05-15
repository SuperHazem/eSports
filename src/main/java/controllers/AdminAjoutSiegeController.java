package controllers;

import dao.SiegeDAO;
import dao.TicketDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import models.Siege;
import utils.SiegeEvent;
import utils.SiegeEventManager;

import java.sql.SQLException;
import java.util.*;

public class AdminAjoutSiegeController {
    @FXML
    private ComboBox<String> categorieComboBox;

    @FXML
    private Label prochainNumeroLabel;

    @FXML
    private Label prixLabel;

    @FXML
    private Button annulerButton;

    @FXML
    private Button confirmerButton;

    @FXML
    private Button supprimerButton;

    @FXML
    private Button rafraichirButton;

    @FXML
    private Pane siegesContainer;

    private TicketDAO ticketDAO;
    private SiegeDAO siegeDAO;
    private AdminController parentController;

    // Stockage des boutons de siège par ID
    private Map<String, Button> siegeButtons = new HashMap<>();

    // Siège sélectionné actuellement
    private String siegeSelectionne = null;

    // Liste des numéros de sièges supprimés par catégorie
    private Map<String, Set<Integer>> numerosSiegeSupprimes = new HashMap<>();

    // Stockage des positions des sièges supprimés
    private Map<String, Map<Integer, double[]>> positionsSiegesSupprimes = new HashMap<>();

    // Prix fixes par catégorie
    private final double PRIX_CATEGORIE_A = 60.0;
    private final double PRIX_CATEGORIE_B = 40.0;
    private final double PRIX_CATEGORIE_C = 20.0;

    // Numéros de départ pour chaque catégorie
    private final int NUMERO_DEPART_A = 31;
    private final int NUMERO_DEPART_B = 47;
    private final int NUMERO_DEPART_C = 61;

    // Espacement entre les sièges
    private final int SIEGE_WIDTH = 45;
    private final int SIEGE_HEIGHT = 40;
    private final int SIEGE_SPACING = 5;
    private final int MAX_SIEGES_PAR_LIGNE = 17;

    // Positions Y pour chaque catégorie - Augmenter l'espacement entre A et B
    private final int POSITION_Y_CATEGORIE_A = 120;
    private final int POSITION_Y_CATEGORIE_B = 320; // Augmenté pour plus d'espace
    private final int POSITION_Y_CATEGORIE_C = 500;

    // Ensemble des IDs de sièges existants
    private Set<String> siegesExistants = new HashSet<>();

    public void initialize() {
        try {
            ticketDAO = new TicketDAO();
            siegeDAO = new SiegeDAO();

            // Nettoyer les sièges en double dans la base de données
            siegeDAO.nettoyerSiegesEnDouble();

            // Récupérer tous les IDs de sièges existants
            siegesExistants = siegeDAO.getTousLesSiegesIds();
            System.out.println("Sièges existants: " + siegesExistants);

            // Initialiser les ensembles de numéros supprimés
            numerosSiegeSupprimes.put("A", new TreeSet<>());
            numerosSiegeSupprimes.put("B", new TreeSet<>());
            numerosSiegeSupprimes.put("C", new TreeSet<>());

            // Initialiser les maps de positions des sièges supprimés
            positionsSiegesSupprimes.put("A", new HashMap<>());
            positionsSiegesSupprimes.put("B", new HashMap<>());
            positionsSiegesSupprimes.put("C", new HashMap<>());

            // Initialiser la combobox des catégories
            categorieComboBox.getItems().addAll("A", "B", "C");

            // Initialiser l'affichage des sièges avant de configurer les listeners
            initialiserAffichageSieges();

            // Écouter les changements de catégorie
            categorieComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    updateProchainNumero(newVal);
                }
            });

            // Configurer les boutons
            annulerButton.setOnAction(event -> fermerFenetre());
            confirmerButton.setOnAction(event -> ajouterSiege());
            supprimerButton.setOnAction(event -> supprimerSiege());

            // Ajouter un bouton de rafraîchissement si nécessaire
            if (rafraichirButton != null) {
                rafraichirButton.setOnAction(event -> rafraichirInterface());
            }

            // Désactiver les boutons jusqu'à ce qu'une action soit possible
            confirmerButton.setDisable(true);
            supprimerButton.setDisable(true);

            categorieComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                confirmerButton.setDisable(newVal == null);
            });

        } catch (SQLException e) {
            e.printStackTrace();
            afficherErreur("Erreur de connexion", "Impossible de se connecter à la base de données");
        }
    }

    /**
     * Rafraîchit complètement l'interface
     */
    private void rafraichirInterface() {
        try {
            // Récupérer à nouveau tous les IDs de sièges existants
            siegesExistants = siegeDAO.getTousLesSiegesIds();
            System.out.println("Sièges existants après rafraîchissement: " + siegesExistants);

            // Réinitialiser l'affichage
            initialiserAffichageSieges();

            // Mettre à jour le prochain numéro si une catégorie est sélectionnée
            String categorie = categorieComboBox.getValue();
            if (categorie != null) {
                updateProchainNumero(categorie);
            }

            // Afficher un message de confirmation
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Rafraîchissement");
            alert.setHeaderText(null);
            alert.setContentText("L'interface a été rafraîchie avec succès.");
            alert.showAndWait();
        } catch (SQLException e) {
            e.printStackTrace();
            afficherErreur("Erreur", "Impossible de rafraîchir l'interface: " + e.getMessage());
        }
    }

    private void initialiserAffichageSieges() {
        try {
            // Nettoyer le conteneur
            siegesContainer.getChildren().clear();
            siegeButtons.clear();

            // Créer les sections pour chaque catégorie avec des espaces entre elles
            creerSectionCategorieA();
            creerSectionCategorieB();
            creerSectionCategorieC();

            // Charger les sièges existants
            chargerSiegesExistants();

            // Charger les numéros de sièges supprimés
            chargerNumerosSiegesSupprimes();

            // Charger les positions des sièges supprimés
            chargerPositionsSiegesSupprimes();

            // Charger les sièges ajoutés (numéros >= NUMERO_DEPART_X)
            chargerSiegesAjoutes();
        } catch (Exception e) {
            e.printStackTrace();
            afficherErreur("Erreur", "Impossible d'initialiser l'affichage des sièges");
        }
    }

    private void chargerSiegesAjoutes() {
        try {
            // Récupérer tous les sièges
            for (Siege siege : siegeDAO.lireTous()) {
                String id = siege.getId();
                String categorie = id.substring(0, 1);
                int numero = Integer.parseInt(id.substring(1));

                // Vérifier si c'est un siège ajouté (numéro >= NUMERO_DEPART_X)
                boolean estSiegeAjoute = false;
                int positionY = 0;

                switch (categorie) {
                    case "A":
                        estSiegeAjoute = numero >= NUMERO_DEPART_A;
                        positionY = POSITION_Y_CATEGORIE_A;
                        break;
                    case "B":
                        estSiegeAjoute = numero >= NUMERO_DEPART_B;
                        positionY = POSITION_Y_CATEGORIE_B;
                        break;
                    case "C":
                        estSiegeAjoute = numero >= NUMERO_DEPART_C;
                        positionY = POSITION_Y_CATEGORIE_C;
                        break;
                }

                // Si c'est un siège ajouté et qu'il n'est pas déjà affiché
                if (estSiegeAjoute && !siegeButtons.containsKey(id)) {
                    // Calculer la position
                    List<String> siegesMemeCategorie = new ArrayList<>();
                    for (String existingId : siegeButtons.keySet()) {
                        if (existingId.startsWith(categorie)) {
                            int existingNumero = Integer.parseInt(existingId.substring(1));
                            if ((categorie.equals("A") && existingNumero >= NUMERO_DEPART_A) ||
                                    (categorie.equals("B") && existingNumero >= NUMERO_DEPART_B) ||
                                    (categorie.equals("C") && existingNumero >= NUMERO_DEPART_C)) {
                                siegesMemeCategorie.add(existingId);
                            }
                        }
                    }

                    // Pour la catégorie B, on veut que B47 soit à côté de B46
                    if (categorie.equals("B") && numero == 47) {
                        // Trouver le bouton B46
                        Button b46Button = siegeButtons.get("B46");
                        if (b46Button != null) {
                            double x = b46Button.getLayoutX() + SIEGE_WIDTH + SIEGE_SPACING;
                            double y = b46Button.getLayoutY();
                            creerSiege(id, x, y, SIEGE_WIDTH, SIEGE_HEIGHT, siege.isDisponible());
                            continue;
                        }
                    }

                    int index = siegesMemeCategorie.size();
                    int ligne = index / MAX_SIEGES_PAR_LIGNE;
                    int colonne = index % MAX_SIEGES_PAR_LIGNE;

                    double x = 20 + colonne * (SIEGE_WIDTH + SIEGE_SPACING);
                    double y = positionY + ligne * (SIEGE_HEIGHT + SIEGE_SPACING);

                    // Créer le siège
                    creerSiege(id, x, y, SIEGE_WIDTH, SIEGE_HEIGHT, siege.isDisponible());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement des sièges ajoutés: " + e.getMessage());
        }
    }

    private void chargerNumerosSiegesSupprimes() {
        try {
            // Réinitialiser les ensembles
            numerosSiegeSupprimes.get("A").clear();
            numerosSiegeSupprimes.get("B").clear();
            numerosSiegeSupprimes.get("C").clear();

            // Récupérer les numéros supprimés depuis la base de données
            Map<String, Set<Integer>> numeros = siegeDAO.getNumerosSiegesSupprimes();
            if (numeros != null) {
                for (String categorie : numeros.keySet()) {
                    if (numerosSiegeSupprimes.containsKey(categorie)) {
                        numerosSiegeSupprimes.get(categorie).addAll(numeros.get(categorie));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement des numéros de sièges supprimés: " + e.getMessage());
        }
    }

    private void chargerPositionsSiegesSupprimes() {
        try {
            // Réinitialiser les maps
            positionsSiegesSupprimes.get("A").clear();
            positionsSiegesSupprimes.get("B").clear();
            positionsSiegesSupprimes.get("C").clear();

            // Récupérer les positions depuis la base de données
            Map<String, Map<Integer, double[]>> positions = siegeDAO.getPositionsSiegesSupprimes();
            if (positions != null) {
                for (String categorie : positions.keySet()) {
                    if (positionsSiegesSupprimes.containsKey(categorie)) {
                        positionsSiegesSupprimes.get(categorie).putAll(positions.get(categorie));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement des positions des sièges supprimés: " + e.getMessage());
        }
    }

    private void creerSectionCategorieA() {
        // Ajouter le titre de la catégorie
        Label titreLabel = new Label("Catégorie A - Premium");
        titreLabel.setStyle("-fx-text-fill: #00b8d9; -fx-font-size: 14px;");
        titreLabel.setLayoutX(320);
        titreLabel.setLayoutY(0);
        siegesContainer.getChildren().add(titreLabel);

        int yPos = 30;
        int xStart = 20;

        // Première rangée (A1-A17)
        for (int i = 1; i <= 17; i++) {
            boolean disponible = i < 7 || i > 15; // A7-A15 sont gris
            creerSiege("A" + i, xStart + (i-1) * (SIEGE_WIDTH + SIEGE_SPACING), yPos, SIEGE_WIDTH, SIEGE_HEIGHT, disponible);
        }

        // Deuxième rangée (A18-A30)
        yPos += SIEGE_HEIGHT + SIEGE_SPACING;
        xStart = 20; // Commencer au début de la ligne

        for (int i = 18; i <= 30; i++) {
            boolean disponible = i != 27; // A27 est gris
            creerSiege("A" + i, xStart + (i-18) * (SIEGE_WIDTH + SIEGE_SPACING), yPos, SIEGE_WIDTH, SIEGE_HEIGHT, disponible);
        }

        // Espace réservé pour les nouveaux sièges de la catégorie A
        // Ajouter une ligne de séparation visuelle
        Label espaceLabel = new Label("Espace pour nouveaux sièges - Catégorie A");
        espaceLabel.setStyle("-fx-text-fill: #555555; -fx-font-size: 10px;");
        espaceLabel.setLayoutX(20);
        espaceLabel.setLayoutY(yPos + SIEGE_HEIGHT + 10);
        siegesContainer.getChildren().add(espaceLabel);
    }

    private void creerSectionCategorieB() {
        // Ajouter le titre de la catégorie avec un espacement supplémentaire
        Label titreLabel = new Label("Catégorie B - Standard");
        titreLabel.setStyle("-fx-text-fill: #00b8d9; -fx-font-size: 14px;");
        titreLabel.setLayoutX(320);
        titreLabel.setLayoutY(190); // Augmenter l'espacement
        siegesContainer.getChildren().add(titreLabel);

        int yPos = 220; // Position Y ajustée
        int xStart = 20;

        // Première rangée (B1-B17)
        for (int i = 1; i <= 17; i++) {
            boolean disponible = i != 8 && i != 9 && i != 11; // B8, B9, B11 sont gris
            creerSiege("B" + i, xStart + (i-1) * (SIEGE_WIDTH + SIEGE_SPACING), yPos, SIEGE_WIDTH, SIEGE_HEIGHT, disponible);
        }

        // Deuxième rangée (B18-B34)
        yPos += SIEGE_HEIGHT + SIEGE_SPACING;
        xStart = 20; // Commencer au début de la ligne
        for (int i = 18; i <= 34; i++) {
            boolean disponible = i != 19 && i != 27; // B19, B27 sont gris
            creerSiege("B" + i, xStart + (i-18) * (SIEGE_WIDTH + SIEGE_SPACING), yPos, SIEGE_WIDTH, SIEGE_HEIGHT, disponible);
        }

        // Troisième rangée (B35-B46)
        yPos += SIEGE_HEIGHT + SIEGE_SPACING;
        xStart = 20; // Commencer au début de la ligne
        for (int i = 35; i <= 46; i++) {
            creerSiege("B" + i, xStart + (i-35) * (SIEGE_WIDTH + SIEGE_SPACING), yPos, SIEGE_WIDTH, SIEGE_HEIGHT, true);
        }

        // Espace réservé pour les nouveaux sièges de la catégorie B
        Label espaceLabel = new Label("Espace pour nouveaux sièges - Catégorie B");
        espaceLabel.setStyle("-fx-text-fill: #555555; -fx-font-size: 10px;");
        espaceLabel.setLayoutX(20);
        espaceLabel.setLayoutY(yPos + SIEGE_HEIGHT + 10);
        siegesContainer.getChildren().add(espaceLabel);
    }

    private void creerSectionCategorieC() {
        // Ajouter le titre de la catégorie avec un espacement supplémentaire
        Label titreLabel = new Label("Catégorie C - Économique");
        titreLabel.setStyle("-fx-text-fill: #00b8d9; -fx-font-size: 14px;");
        titreLabel.setLayoutX(320);
        titreLabel.setLayoutY(370); // Augmenter l'espacement
        siegesContainer.getChildren().add(titreLabel);

        int yPos = 400; // Position Y ajustée
        int xStart = 20;

        // Première rangée (C1-C17)
        for (int i = 1; i <= 17; i++) {
            boolean disponible = i != 9; // C9 est gris
            creerSiege("C" + i, xStart + (i-1) * (SIEGE_WIDTH + SIEGE_SPACING), yPos, SIEGE_WIDTH, SIEGE_HEIGHT, disponible);
        }

        // Deuxième rangée (C18-C34)
        yPos += SIEGE_HEIGHT + SIEGE_SPACING;
        xStart = 20; // Commencer au début de la ligne
        for (int i = 18; i <= 34; i++) {
            boolean disponible = i != 26; // C26 est gris
            creerSiege("C" + i, xStart + (i-18) * (SIEGE_WIDTH + SIEGE_SPACING), yPos, SIEGE_WIDTH, SIEGE_HEIGHT, disponible);
        }

        // Troisième rangée (C35-C51)
        yPos += SIEGE_HEIGHT + SIEGE_SPACING;
        xStart = 20; // Commencer au début de la ligne
        for (int i = 35; i <= 51; i++) {
            creerSiege("C" + i, xStart + (i-35) * (SIEGE_WIDTH + SIEGE_SPACING), yPos, SIEGE_WIDTH, SIEGE_HEIGHT, true);
        }

        // Quatrième rangée (C52-C60)
        yPos += SIEGE_HEIGHT + SIEGE_SPACING;
        xStart = 20; // Commencer au début de la ligne
        for (int i = 52; i <= 60; i++) {
            creerSiege("C" + i, xStart + (i-52) * (SIEGE_WIDTH + SIEGE_SPACING), yPos, SIEGE_WIDTH, SIEGE_HEIGHT, true);
        }

        // Espace réservé pour les nouveaux sièges de la catégorie C
        Label espaceLabel = new Label("Espace pour nouveaux sièges - Catégorie C");
        espaceLabel.setStyle("-fx-text-fill: #555555; -fx-font-size: 10px;");
        espaceLabel.setLayoutX(20);
        espaceLabel.setLayoutY(yPos + SIEGE_HEIGHT + 10);
        siegesContainer.getChildren().add(espaceLabel);
    }

    private void creerSiege(String id, double x, double y, double width, double height, boolean disponible) {
        Button siegeButton = new Button(id);
        siegeButton.setPrefWidth(width);
        siegeButton.setPrefHeight(height);

        // Définir le style en fonction de la disponibilité
        if (disponible) {
            siegeButton.setStyle("-fx-background-color: #FFD700; -fx-text-fill: black;"); // Jaune pour disponible
        } else {
            siegeButton.setStyle("-fx-background-color: #A9A9A9; -fx-text-fill: white;"); // Gris pour indisponible
        }

        siegeButton.setLayoutX(x);
        siegeButton.setLayoutY(y);

        // Ajouter l'action de sélection
        siegeButton.setOnAction(event -> {
            // Désélectionner le siège précédent s'il y en a un
            if (siegeSelectionne != null) {
                Button previousButton = siegeButtons.get(siegeSelectionne);
                if (previousButton != null) {
                    // Restaurer le style d'origine sans le bord rouge
                    String style = previousButton.getStyle().replace("; -fx-border-color: red; -fx-border-width: 2", "");
                    previousButton.setStyle(style);
                }
            }

            // Sélectionner le nouveau siège
            siegeSelectionne = id;
            siegeButton.setStyle(siegeButton.getStyle() + "; -fx-border-color: red; -fx-border-width: 2");

            // Activer le bouton supprimer
            supprimerButton.setDisable(false);
        });

        // Ajouter le bouton à la map et au conteneur
        siegeButtons.put(id, siegeButton);
        siegesContainer.getChildren().add(siegeButton);

        // Ajouter l'ID à l'ensemble des sièges existants
        siegesExistants.add(id);
    }

    private void chargerSiegesExistants() {
        try {
            // Récupérer tous les sièges
            for (Siege siege : siegeDAO.lireTous()) {
                Button button = siegeButtons.get(siege.getId());
                if (button != null) {
                    if (siege.isDisponible()) {
                        button.setStyle("-fx-background-color: #FFD700; -fx-text-fill: black;"); // Jaune pour disponible
                    } else {
                        button.setStyle("-fx-background-color: #A9A9A9; -fx-text-fill: white;"); // Gris pour réservé
                    }
                }

                // Ajouter l'ID à l'ensemble des sièges existants
                siegesExistants.add(siege.getId());
            }

            // Récupérer les tickets pour marquer les sièges réservés
            for (String siegeId : ticketDAO.getSiegesReserves()) {
                Button button = siegeButtons.get(siegeId);
                if (button != null) {
                    button.setStyle("-fx-background-color: #A9A9A9; -fx-text-fill: white;"); // Gris pour réservé
                }
            }

            // Rendre tous les sièges jaunes (disponibles)
            for (Button button : siegeButtons.values()) {
                if (button.getStyle().contains("#A9A9A9")) {
                    button.setStyle("-fx-background-color: #FFD700; -fx-text-fill: black;");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            afficherErreur("Erreur", "Impossible de charger l'état des sièges");
        }
    }

    public void setParentController(AdminController controller) {
        this.parentController = controller;
    }

    private void updateProchainNumero(String categorie) {
        try {
            int prochainNumero;

            // Vérifier s'il y a des numéros supprimés à réutiliser
            Set<Integer> numerosSupprimesCategorie = numerosSiegeSupprimes.get(categorie);
            if (numerosSupprimesCategorie != null && !numerosSupprimesCategorie.isEmpty()) {
                // Prendre le plus petit numéro supprimé
                prochainNumero = Collections.min(numerosSupprimesCategorie);
            } else {
                // Sinon, utiliser la logique normale
                int dernierNumero = siegeDAO.getDernierNumeroSiege(categorie);

                // Utiliser directement le numéro retourné par la DAO
                prochainNumero = dernierNumero;
            }

            // Vérifier si le siège existe déjà dans la base de données ou dans l'interface
            String siegeId = categorie + prochainNumero;
            while (siegesExistants.contains(siegeId) || siegeButtons.containsKey(siegeId)) {
                prochainNumero++;
                siegeId = categorie + prochainNumero;
            }

            prochainNumeroLabel.setText(siegeId);

            // Mettre à jour le prix en fonction de la catégorie
            double prix = getPrixParCategorie(categorie.charAt(0));
            prixLabel.setText(String.format("%.2f dt", prix));

            // Afficher des logs pour le débogage
            System.out.println("Prochain numéro pour la catégorie " + categorie + ": " + prochainNumero);
        } catch (Exception e) {
            e.printStackTrace();
            afficherErreur("Erreur", "Impossible de récupérer le prochain numéro de siège: " + e.getMessage());
        }
    }

    private double getPrixParCategorie(char categorie) {
        switch (categorie) {
            case 'A':
                return PRIX_CATEGORIE_A;
            case 'B':
                return PRIX_CATEGORIE_B;
            case 'C':
                return PRIX_CATEGORIE_C;
            default:
                return 0.0;
        }
    }

    private void ajouterSiege() {
        try {
            String categorie = categorieComboBox.getValue();
            if (categorie == null || categorie.isEmpty()) {
                afficherErreur("Erreur", "Veuillez sélectionner une catégorie");
                return;
            }

            // Récupérer l'ID du siège à partir du label
            String nouveauSiegeId = prochainNumeroLabel.getText();

            // Vérifier si le siège existe déjà
            if (siegesExistants.contains(nouveauSiegeId) || siegeButtons.containsKey(nouveauSiegeId)) {
                afficherErreur("Erreur", "Impossible d'ajouter le siège: Duplicate entry '" + nouveauSiegeId + "' for key 'PRIMARY'");
                return;
            }

            // Extraire le numéro du siège
            int prochainNumero = Integer.parseInt(nouveauSiegeId.substring(1));
            boolean estNumeroRecycle = numerosSiegeSupprimes.get(categorie).contains(prochainNumero);

            // Si c'est un numéro recyclé, le retirer de la liste des numéros supprimés
            if (estNumeroRecycle) {
                numerosSiegeSupprimes.get(categorie).remove(prochainNumero);
            }

            // Créer un nouveau siège disponible
            Siege nouveauSiege = new Siege();
            nouveauSiege.setId(nouveauSiegeId);
            nouveauSiege.setCategorie(categorie);
            nouveauSiege.setPrix(getPrixParCategorie(categorie.charAt(0)));
            nouveauSiege.setDisponible(true);

            // Ajouter le siège à la base de données des sièges disponibles
            siegeDAO.ajouter(nouveauSiege);

            // Vérifier que le siège a bien été ajouté à la base de données
            if (!siegeDAO.siegeExiste(nouveauSiegeId)) {
                System.err.println("ERREUR: Le siège " + nouveauSiegeId + " n'a pas été ajouté à la base de données!");
                // Réessayer l'ajout
                siegeDAO.ajouter(nouveauSiege);

                // Vérifier à nouveau
                if (!siegeDAO.siegeExiste(nouveauSiegeId)) {
                    afficherErreur("Erreur", "Le siège n'a pas pu être ajouté à la base de données après plusieurs tentatives.");
                    return;
                }
            }

            // Ajouter l'ID à l'ensemble des sièges existants
            siegesExistants.add(nouveauSiegeId);

            // Déterminer la position du nouveau siège
            double x = 0, y = 0;

            // Vérifier si c'est un siège recyclé avec une position enregistrée
            if (estNumeroRecycle && positionsSiegesSupprimes.get(categorie).containsKey(prochainNumero)) {
                // Utiliser la position enregistrée
                double[] position = positionsSiegesSupprimes.get(categorie).get(prochainNumero);
                x = position[0];
                y = position[1];

                // Supprimer la position de la map
                positionsSiegesSupprimes.get(categorie).remove(prochainNumero);

                // Supprimer la position de la base de données
                siegeDAO.supprimerPositionSiege(categorie, prochainNumero);
            } else {
                // Cas spécial pour B47 (doit être à côté de B46)
                if (categorie.equals("B") && prochainNumero == 47) {
                    Button b46Button = siegeButtons.get("B46");
                    if (b46Button != null) {
                        x = b46Button.getLayoutX() + SIEGE_WIDTH + SIEGE_SPACING;
                        y = b46Button.getLayoutY();
                    } else {
                        // Fallback si B46 n'est pas trouvé
                        double[] position = calculerPositionNouveauSiege(categorie, prochainNumero);
                        x = position[0];
                        y = position[1];
                    }
                } else {
                    // Calculer la position en fonction de la catégorie
                    int positionY = 0;
                    switch (categorie) {
                        case "A":
                            positionY = POSITION_Y_CATEGORIE_A;
                            break;
                        case "B":
                            positionY = POSITION_Y_CATEGORIE_B;
                            break;
                        case "C":
                            positionY = POSITION_Y_CATEGORIE_C;
                            break;
                    }

                    // Compter les sièges déjà ajoutés de cette catégorie
                    List<String> siegesAjoutes = new ArrayList<>();
                    for (String id : siegeButtons.keySet()) {
                        if (id.startsWith(categorie)) {
                            try {
                                int numero = Integer.parseInt(id.substring(1));
                                if ((categorie.equals("A") && numero >= NUMERO_DEPART_A) ||
                                        (categorie.equals("B") && numero >= NUMERO_DEPART_B) ||
                                        (categorie.equals("C") && numero >= NUMERO_DEPART_C)) {
                                    siegesAjoutes.add(id);
                                }
                            } catch (NumberFormatException e) {
                                // Ignorer les ID non numériques
                            }
                        }
                    }

                    // Calculer la position en fonction du nombre de sièges déjà ajoutés
                    int index = siegesAjoutes.size();
                    int ligne = index / MAX_SIEGES_PAR_LIGNE;
                    int colonne = index % MAX_SIEGES_PAR_LIGNE;

                    x = 20 + colonne * (SIEGE_WIDTH + SIEGE_SPACING);
                    y = positionY + ligne * (SIEGE_HEIGHT + SIEGE_SPACING);
                }
            }

            // Créer le bouton pour le nouveau siège
            creerSiege(nouveauSiegeId, x, y, SIEGE_WIDTH, SIEGE_HEIGHT, true);

            // Mettre en évidence le nouveau siège
            Button button = siegeButtons.get(nouveauSiegeId);
            if (button != null) {
                button.setStyle("-fx-background-color: #32CD32; -fx-text-fill: white;"); // Vert pour nouveau siège
            }

            // Notifier les interfaces utilisateur qu'un nouveau siège a été ajouté
            notifierNouveauSiege(nouveauSiege);

            // Rafraîchir l'interface parent si nécessaire
            if (parentController != null) {
                parentController.rafraichirTableau();
            }

            // Afficher un message de confirmation
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Siège ajouté");
            alert.setHeaderText(null);
            alert.setContentText("Le siège " + nouveauSiegeId + " a été ajouté avec succès aux places disponibles.");
            alert.showAndWait();

            // Mettre à jour le prochain numéro
            updateProchainNumero(categorie);

            // Afficher des logs pour le débogage
            System.out.println("Siège " + nouveauSiegeId + " ajouté avec succès. Vérification dans la base de données: " + siegeDAO.siegeExiste(nouveauSiegeId));

        } catch (Exception e) {
            e.printStackTrace();
            afficherErreur("Erreur", "Impossible d'ajouter le siège: " + e.getMessage());
        }
    }

    /**
     * Notifie les interfaces utilisateur qu'un nouveau siège a été ajouté
     */
    private void notifierNouveauSiege(Siege nouveauSiege) {
        try {
            // Créer un événement personnalisé pour la notification
            SiegeEvent event = new SiegeEvent(SiegeEvent.SIEGE_AJOUTE, nouveauSiege);

            // Publier l'événement
            SiegeEventManager.getInstance().publierEvenement(event);

            System.out.println("Notification envoyée pour le nouveau siège: " + nouveauSiege.getId());
        } catch (Exception e) {
            System.err.println("Erreur lors de la notification du nouveau siège: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Méthode auxiliaire pour calculer la position d'un nouveau siège
    private double[] calculerPositionNouveauSiege(String categorie, int numero) {
        double x = 0;
        double y = 0;
        int positionY = 0;

        switch (categorie) {
            case "A":
                positionY = POSITION_Y_CATEGORIE_A;
                break;
            case "B":
                positionY = POSITION_Y_CATEGORIE_B;
                break;
            case "C":
                positionY = POSITION_Y_CATEGORIE_C;
                break;
        }

        // Compter les sièges déjà ajoutés de cette catégorie
        List<String> siegesAjoutes = new ArrayList<>();
        for (String id : siegeButtons.keySet()) {
            if (id.startsWith(categorie)) {
                try {
                    int existingNumero = Integer.parseInt(id.substring(1));
                    if ((categorie.equals("A") && existingNumero >= NUMERO_DEPART_A) ||
                            (categorie.equals("B") && existingNumero >= NUMERO_DEPART_B) ||
                            (categorie.equals("C") && existingNumero >= NUMERO_DEPART_C)) {
                        siegesAjoutes.add(id);
                    }
                } catch (NumberFormatException e) {
                    // Ignorer les ID non numériques
                }
            }
        }

        // Calculer la position en fonction du nombre de sièges déjà ajoutés
        int index = siegesAjoutes.size();
        int ligne = index / MAX_SIEGES_PAR_LIGNE;
        int colonne = index % MAX_SIEGES_PAR_LIGNE;

        x = 20 + colonne * (SIEGE_WIDTH + SIEGE_SPACING);
        y = positionY + ligne * (SIEGE_HEIGHT + SIEGE_SPACING);

        return new double[]{x, y};
    }

    private void supprimerSiege() {
        if (siegeSelectionne == null) {
            afficherErreur("Erreur", "Aucun siège sélectionné");
            return;
        }

        try {
            // Demander confirmation
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de suppression");
            alert.setHeaderText("Supprimer le siège");
            alert.setContentText("Êtes-vous sûr de vouloir supprimer le siège " + siegeSelectionne + " ?");

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        // Extraire la catégorie et le numéro du siège
                        String categorie = siegeSelectionne.substring(0, 1);
                        int numero = Integer.parseInt(siegeSelectionne.substring(1));

                        // Récupérer la position du siège avant de le supprimer
                        Button button = siegeButtons.get(siegeSelectionne);
                        if (button != null) {
                            double x = button.getLayoutX();
                            double y = button.getLayoutY();

                            // Enregistrer la position dans la map
                            positionsSiegesSupprimes.get(categorie).put(numero, new double[]{x, y});

                            // Enregistrer la position dans la base de données
                            siegeDAO.enregistrerPositionSiege(categorie, numero, x, y);
                        }

                        // Récupérer le siège avant de le supprimer pour la notification
                        Siege siegeASupprimer = null;
                        for (Siege siege : siegeDAO.lireTous()) {
                            if (siege.getId().equals(siegeSelectionne)) {
                                siegeASupprimer = siege;
                                break;
                            }
                        }

                        // Ajouter le numéro à la liste des numéros supprimés
                        Set<Integer> numerosSupprimesCategorie = numerosSiegeSupprimes.get(categorie);
                        if (numerosSupprimesCategorie != null) {
                            numerosSupprimesCategorie.add(numero);
                        }

                        // Supprimer le siège de la base de données
                        siegeDAO.supprimer(siegeSelectionne);

                        // Retirer l'ID de l'ensemble des sièges existants
                        siegesExistants.remove(siegeSelectionne);

                        // Supprimer le bouton de l'interface
                        if (button != null) {
                            siegesContainer.getChildren().remove(button);
                            siegeButtons.remove(siegeSelectionne);
                        }

                        // Notifier les interfaces utilisateur qu'un siège a été supprimé
                        if (siegeASupprimer != null) {
                            notifierSuppressionSiege(siegeASupprimer);
                        }

                        // Réinitialiser la sélection
                        siegeSelectionne = null;
                        supprimerButton.setDisable(true);

                        // Rafraîchir l'interface parent si nécessaire
                        if (parentController != null) {
                            parentController.rafraichirTableau();
                        }

                        // Mettre à jour le prochain numéro si la catégorie actuelle est celle du siège supprimé
                        String categorieSelectionnee = categorieComboBox.getValue();
                        if (categorieSelectionnee != null && categorieSelectionnee.equals(categorie)) {
                            updateProchainNumero(categorie);
                        }

                        // Afficher un message de confirmation
                        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                        successAlert.setTitle("Siège supprimé");
                        successAlert.setHeaderText(null);
                        successAlert.setContentText("Le siège a été supprimé avec succès.");
                        successAlert.showAndWait();

                    } catch (Exception e) {
                        e.printStackTrace();
                        afficherErreur("Erreur", "Impossible de supprimer le siège: " + e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            afficherErreur("Erreur", "Impossible de supprimer le siège");
        }
    }

    /**
     * Notifie les interfaces utilisateur qu'un siège a été supprimé
     */
    private void notifierSuppressionSiege(Siege siegeASupprimer) {
        try {
            System.out.println("Début de la notification de suppression du siège: " + siegeASupprimer.getId());

            // Créer un événement personnalisé pour la notification
            SiegeEvent event = new SiegeEvent(SiegeEvent.SIEGE_SUPPRIME, siegeASupprimer);

            // Publier l'événement
            SiegeEventManager.getInstance().publierEvenement(event);

            System.out.println("Notification envoyée pour la suppression du siège: " + siegeASupprimer.getId());

            // Attendre un court instant pour s'assurer que l'événement est traité
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la notification de suppression du siège: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void fermerFenetre() {
        Stage stage = (Stage) annulerButton.getScene().getWindow();
        stage.close();
    }

    private void afficherErreur(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
