package dao;

import models.Siege;
import utils.DatabaseConnection;

import java.sql.*;
import java.util.*;

public class SiegeDAO {
    private Connection connexion;

    // Numéros de départ pour chaque catégorie
    private final int NUMERO_DEPART_A = 31;
    private final int NUMERO_DEPART_B = 45;
    private final int NUMERO_DEPART_C = 61;

    public SiegeDAO() throws SQLException {
        try {
            // Récupérer la connexion à la base de données
            this.connexion = DatabaseConnection.getInstance().getConnection();

            // Vérifier que la connexion est établie
            if (this.connexion == null) {
                throw new SQLException("Impossible d'établir la connexion à la base de données");
            }

            // Vérifier si les tables nécessaires existent
            creerTablesNecessaires();

        } catch (SQLException e) {
            System.err.println("Erreur lors de l'initialisation de SiegeDAO: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Crée les tables nécessaires si elles n'existent pas
     */
    private void creerTablesNecessaires() throws SQLException {
        DatabaseMetaData dbm = connexion.getMetaData();

        // Vérifier si la table sieges existe
        ResultSet tables = dbm.getTables(null, null, "sieges", null);
        if (!tables.next()) {
            // Créer la table sieges
            String createTableSQL = "CREATE TABLE sieges (" +
                    "id VARCHAR(10) NOT NULL, " +
                    "categorie VARCHAR(1) NOT NULL, " +
                    "prix DOUBLE NOT NULL, " +
                    "disponible BOOLEAN NOT NULL DEFAULT TRUE, " +
                    "PRIMARY KEY (id))";
            try (Statement stmt = connexion.createStatement()) {
                stmt.execute(createTableSQL);
                System.out.println("Table sieges créée avec succès");
            }
        }

        // Vérifier si la table sieges_supprimes existe
        tables = dbm.getTables(null, null, "sieges_supprimes", null);
        if (!tables.next()) {
            // Créer la table sieges_supprimes
            String createTableSQL = "CREATE TABLE sieges_supprimes (" +
                    "categorie VARCHAR(1) NOT NULL, " +
                    "numero INT NOT NULL, " +
                    "PRIMARY KEY (categorie, numero))";
            try (Statement stmt = connexion.createStatement()) {
                stmt.execute(createTableSQL);
                System.out.println("Table sieges_supprimes créée avec succès");
            }
        }

        // Vérifier si la table positions_sieges_supprimes existe
        tables = dbm.getTables(null, null, "positions_sieges_supprimes", null);
        if (!tables.next()) {
            // Créer la table positions_sieges_supprimes
            String createTableSQL = "CREATE TABLE positions_sieges_supprimes (" +
                    "categorie VARCHAR(1) NOT NULL, " +
                    "numero INT NOT NULL, " +
                    "position_x DOUBLE NOT NULL, " +
                    "position_y DOUBLE NOT NULL, " +
                    "PRIMARY KEY (categorie, numero))";
            try (Statement stmt = connexion.createStatement()) {
                stmt.execute(createTableSQL);
                System.out.println("Table positions_sieges_supprimes créée avec succès");
            }
        }
    }

    /**
     * Vérifie si un siège existe déjà dans la base de données
     */
    public boolean siegeExiste(String siegeId) throws SQLException {
        // Vérifier que la connexion est établie
        if (this.connexion == null || this.connexion.isClosed()) {
            // Réinitialiser la connexion
            this.connexion = DatabaseConnection.getInstance().getConnection();
            if (this.connexion == null) {
                throw new SQLException("Impossible d'établir la connexion à la base de données");
            }
        }

        String sql = "SELECT id FROM sieges WHERE id = ?";

        try (PreparedStatement pstmt = connexion.prepareStatement(sql)) {
            pstmt.setString(1, siegeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next(); // Retourne true si le siège existe, false sinon
            }
        }
    }

    /**
     * Récupère tous les IDs de sièges existants
     */
    public Set<String> getTousLesSiegesIds() throws SQLException {
        Set<String> ids = new HashSet<>();

        // Vérifier que la connexion est établie
        if (this.connexion == null || this.connexion.isClosed()) {
            // Réinitialiser la connexion
            this.connexion = DatabaseConnection.getInstance().getConnection();
            if (this.connexion == null) {
                throw new SQLException("Impossible d'établir la connexion à la base de données");
            }
        }

        String sql = "SELECT id FROM sieges";

        try (Statement stmt = connexion.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ids.add(rs.getString("id"));
            }
        }

        return ids;
    }

    public void ajouter(Siege siege) throws SQLException {
        // Vérifier que la connexion est établie
        if (this.connexion == null || this.connexion.isClosed()) {
            // Réinitialiser la connexion
            this.connexion = DatabaseConnection.getInstance().getConnection();
            if (this.connexion == null) {
                throw new SQLException("Impossible d'établir la connexion à la base de données");
            }
        }

        // Vérifier si le siège existe déjà
        if (siegeExiste(siege.getId())) {
            throw new SQLException("Duplicate entry '" + siege.getId() + "' for key 'PRIMARY'");
        }

        String sql = "INSERT INTO sieges (id, categorie, prix, disponible) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstmt = connexion.prepareStatement(sql)) {
            pstmt.setString(1, siege.getId());
            pstmt.setString(2, siege.getCategorie());
            pstmt.setDouble(3, siege.getPrix());
            pstmt.setBoolean(4, siege.isDisponible());
            pstmt.executeUpdate();
            System.out.println("Siège " + siege.getId() + " ajouté avec succès");
        }
    }

    public List<Siege> lireTous() throws SQLException {
        List<Siege> sieges = new ArrayList<>();

        // Vérifier que la connexion est établie
        if (this.connexion == null || this.connexion.isClosed()) {
            // Réinitialiser la connexion
            this.connexion = DatabaseConnection.getInstance().getConnection();
            if (this.connexion == null) {
                throw new SQLException("Impossible d'établir la connexion à la base de données");
            }
        }

        String sql = "SELECT * FROM sieges";

        try (Statement stmt = connexion.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Siege siege = new Siege();
                siege.setId(rs.getString("id"));
                siege.setCategorie(rs.getString("categorie"));
                siege.setPrix(rs.getDouble("prix"));
                siege.setDisponible(rs.getBoolean("disponible"));
                sieges.add(siege);
            }
        }

        return sieges;
    }

    public List<Siege> lireSiegesDisponibles() throws SQLException {
        List<Siege> sieges = new ArrayList<>();

        // Vérifier que la connexion est établie
        if (this.connexion == null || this.connexion.isClosed()) {
            // Réinitialiser la connexion
            this.connexion = DatabaseConnection.getInstance().getConnection();
            if (this.connexion == null) {
                throw new SQLException("Impossible d'établir la connexion à la base de données");
            }
        }

        String sql = "SELECT * FROM sieges WHERE disponible = TRUE";

        try (Statement stmt = connexion.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Siege siege = new Siege();
                siege.setId(rs.getString("id"));
                siege.setCategorie(rs.getString("categorie"));
                siege.setPrix(rs.getDouble("prix"));
                siege.setDisponible(rs.getBoolean("disponible"));
                sieges.add(siege);
            }
        }

        return sieges;
    }

    public void marquerCommeReserve(String siegeId) throws SQLException {
        // Vérifier que la connexion est établie
        if (this.connexion == null || this.connexion.isClosed()) {
            // Réinitialiser la connexion
            this.connexion = DatabaseConnection.getInstance().getConnection();
            if (this.connexion == null) {
                throw new SQLException("Impossible d'établir la connexion à la base de données");
            }
        }

        String sql = "UPDATE sieges SET disponible = FALSE WHERE id = ?";

        try (PreparedStatement pstmt = connexion.prepareStatement(sql)) {
            pstmt.setString(1, siegeId);
            pstmt.executeUpdate();
        }
    }

    public void marquerCommeDisponible(String siegeId) throws SQLException {
        // Vérifier que la connexion est établie
        if (this.connexion == null || this.connexion.isClosed()) {
            // Réinitialiser la connexion
            this.connexion = DatabaseConnection.getInstance().getConnection();
            if (this.connexion == null) {
                throw new SQLException("Impossible d'établir la connexion à la base de données");
            }
        }

        String sql = "UPDATE sieges SET disponible = TRUE WHERE id = ?";

        try (PreparedStatement pstmt = connexion.prepareStatement(sql)) {
            pstmt.setString(1, siegeId);
            pstmt.executeUpdate();
        }
    }

    public int getDernierNumeroSiege(String categorie) throws SQLException {
        // Vérifier que la connexion est établie
        if (this.connexion == null || this.connexion.isClosed()) {
            // Réinitialiser la connexion
            this.connexion = DatabaseConnection.getInstance().getConnection();
            if (this.connexion == null) {
                throw new SQLException("Impossible d'établir la connexion à la base de données");
            }
        }

        // Définir le numéro de départ en fonction de la catégorie
        int numeroDepart;
        switch (categorie) {
            case "A":
                numeroDepart = NUMERO_DEPART_A;
                break;
            case "B":
                numeroDepart = NUMERO_DEPART_B;
                break;
            case "C":
                numeroDepart = NUMERO_DEPART_C;
                break;
            default:
                numeroDepart = 1;
        }

        try {
            // Vérifier d'abord si la table existe
            DatabaseMetaData dbm = connexion.getMetaData();
            ResultSet tables = dbm.getTables(null, null, "sieges", null);
            if (!tables.next()) {
                // La table n'existe pas, retourner le numéro de départ
                return numeroDepart;
            }

            // La table existe, trouver le dernier numéro utilisé pour cette catégorie
            String sql = "SELECT id FROM sieges WHERE categorie = ? ORDER BY CAST(SUBSTRING(id, 2) AS UNSIGNED) DESC LIMIT 1";

            try (PreparedStatement pstmt = connexion.prepareStatement(sql)) {
                pstmt.setString(1, categorie);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        String id = rs.getString("id");
                        // Extraire le numéro de l'ID (ex: "A30" -> 30)
                        try {
                            int numero = Integer.parseInt(id.substring(1));
                            // Retourner le maximum entre le numéro trouvé et le numéro de départ
                            return Math.max(numero, numeroDepart - 1) + 1; // +1 pour le prochain numéro
                        } catch (NumberFormatException e) {
                            System.err.println("Format de numéro invalide: " + id);
                            return numeroDepart;
                        }
                    } else {
                        // Si aucun siège n'existe pour cette catégorie, retourner le numéro de départ
                        return numeroDepart;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du dernier numéro de siège: " + e.getMessage());
            // En cas d'erreur, retourner le numéro de départ
            return numeroDepart;
        }
    }

    /**
     * Récupère tous les sièges d'une catégorie spécifique
     */
    public List<Siege> lireSiegesParCategorie(String categorie) throws SQLException {
        List<Siege> sieges = new ArrayList<>();

        // Vérifier que la connexion est établie
        if (this.connexion == null || this.connexion.isClosed()) {
            // Réinitialiser la connexion
            this.connexion = DatabaseConnection.getInstance().getConnection();
            if (this.connexion == null) {
                throw new SQLException("Impossible d'établir la connexion à la base de données");
            }
        }

        String sql = "SELECT * FROM sieges WHERE categorie = ? ORDER BY CAST(SUBSTRING(id, 2) AS UNSIGNED)";

        try (PreparedStatement pstmt = connexion.prepareStatement(sql)) {
            pstmt.setString(1, categorie);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Siege siege = new Siege();
                    siege.setId(rs.getString("id"));
                    siege.setCategorie(rs.getString("categorie"));
                    siege.setPrix(rs.getDouble("prix"));
                    siege.setDisponible(rs.getBoolean("disponible"));
                    sieges.add(siege);
                }
            }
        }

        return sieges;
    }

    public void supprimer(String siegeId) throws SQLException {
        // Vérifier que la connexion est établie
        if (this.connexion == null || this.connexion.isClosed()) {
            // Réinitialiser la connexion
            this.connexion = DatabaseConnection.getInstance().getConnection();
            if (this.connexion == null) {
                throw new SQLException("Impossible d'établir la connexion à la base de données");
            }
        }

        // Extraire la catégorie et le numéro
        String categorie = siegeId.substring(0, 1);
        int numero = Integer.parseInt(siegeId.substring(1));

        // Supprimer le siège
        String sql = "DELETE FROM sieges WHERE id = ?";
        try (PreparedStatement pstmt = connexion.prepareStatement(sql)) {
            pstmt.setString(1, siegeId);
            pstmt.executeUpdate();
            System.out.println("Siège " + siegeId + " supprimé avec succès");
        }

        // Enregistrer le numéro supprimé
        String insertSQL = "INSERT INTO sieges_supprimes (categorie, numero) VALUES (?, ?)";
        try (PreparedStatement pstmt = connexion.prepareStatement(insertSQL)) {
            pstmt.setString(1, categorie);
            pstmt.setInt(2, numero);
            pstmt.executeUpdate();
            System.out.println("Numéro " + numero + " de catégorie " + categorie + " enregistré comme supprimé");
        } catch (SQLException e) {
            // Ignorer les erreurs de clé dupliquée
            if (!e.getMessage().contains("duplicate") && !e.getMessage().contains("Duplicate")) {
                throw e;
            }
        }
    }

    public Map<String, Set<Integer>> getNumerosSiegesSupprimes() throws SQLException {
        Map<String, Set<Integer>> numerosSiegesSupprimes = new HashMap<>();
        numerosSiegesSupprimes.put("A", new TreeSet<>());
        numerosSiegesSupprimes.put("B", new TreeSet<>());
        numerosSiegesSupprimes.put("C", new TreeSet<>());

        // Vérifier que la connexion est établie
        if (this.connexion == null || this.connexion.isClosed()) {
            // Réinitialiser la connexion
            this.connexion = DatabaseConnection.getInstance().getConnection();
            if (this.connexion == null) {
                throw new SQLException("Impossible d'établir la connexion à la base de données");
            }
        }

        // Vérifier si la table existe
        try {
            // La table existe, récupérer les données
            String sql = "SELECT categorie, numero FROM sieges_supprimes ORDER BY numero";

            try (Statement stmt = connexion.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    String categorie = rs.getString("categorie");
                    int numero = rs.getInt("numero");

                    if (numerosSiegesSupprimes.containsKey(categorie)) {
                        numerosSiegesSupprimes.get(categorie).add(numero);
                    }
                }
            }
        } catch (SQLException e) {
            // Si la table n'existe pas, on ignore l'erreur
            System.err.println("Erreur lors de la récupération des numéros de sièges supprimés: " + e.getMessage());
        }

        return numerosSiegesSupprimes;
    }

    /**
     * Enregistre la position d'un siège supprimé
     */
    public void enregistrerPositionSiege(String categorie, int numero, double x, double y) throws SQLException {
        // Vérifier que la connexion est établie
        if (this.connexion == null || this.connexion.isClosed()) {
            // Réinitialiser la connexion
            this.connexion = DatabaseConnection.getInstance().getConnection();
            if (this.connexion == null) {
                throw new SQLException("Impossible d'établir la connexion à la base de données");
            }
        }

        try {
            // Vérifier si la table existe
            String checkTableSQL = "SELECT 1 FROM positions_sieges_supprimes LIMIT 1";
            connexion.createStatement().executeQuery(checkTableSQL);

            // La table existe, insérer ou mettre à jour la position
            String upsertSQL = "REPLACE INTO positions_sieges_supprimes (categorie, numero, position_x, position_y) VALUES (?, ?, ?, ?)";

            try (PreparedStatement pstmt = connexion.prepareStatement(upsertSQL)) {
                pstmt.setString(1, categorie);
                pstmt.setInt(2, numero);
                pstmt.setDouble(3, x);
                pstmt.setDouble(4, y);
                pstmt.executeUpdate();
                System.out.println("Position du siège " + categorie + numero + " enregistrée: (" + x + ", " + y + ")");
            }
        } catch (SQLException e) {
            // Si la table n'existe pas, la créer
            try {
                String createTableSQL = "CREATE TABLE positions_sieges_supprimes (" +
                        "categorie VARCHAR(1) NOT NULL, " +
                        "numero INT NOT NULL, " +
                        "position_x DOUBLE NOT NULL, " +
                        "position_y DOUBLE NOT NULL, " +
                        "PRIMARY KEY (categorie, numero))";

                try (Statement stmt = connexion.createStatement()) {
                    stmt.execute(createTableSQL);
                    System.out.println("Table positions_sieges_supprimes créée");

                    // Maintenant insérer la position
                    String insertSQL = "INSERT INTO positions_sieges_supprimes (categorie, numero, position_x, position_y) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement pstmt = connexion.prepareStatement(insertSQL)) {
                        pstmt.setString(1, categorie);
                        pstmt.setInt(2, numero);
                        pstmt.setDouble(3, x);
                        pstmt.setDouble(4, y);
                        pstmt.executeUpdate();
                        System.out.println("Position du siège " + categorie + numero + " enregistrée: (" + x + ", " + y + ")");
                    }
                }
            } catch (SQLException e2) {
                System.err.println("Erreur lors de la création de la table positions_sieges_supprimes: " + e2.getMessage());
                throw e2;
            }
        }
    }

    /**
     * Récupère les positions des sièges supprimés
     */
    public Map<String, Map<Integer, double[]>> getPositionsSiegesSupprimes() throws SQLException {
        Map<String, Map<Integer, double[]>> positions = new HashMap<>();
        positions.put("A", new HashMap<>());
        positions.put("B", new HashMap<>());
        positions.put("C", new HashMap<>());

        // Vérifier que la connexion est établie
        if (this.connexion == null || this.connexion.isClosed()) {
            // Réinitialiser la connexion
            this.connexion = DatabaseConnection.getInstance().getConnection();
            if (this.connexion == null) {
                throw new SQLException("Impossible d'établir la connexion à la base de données");
            }
        }

        try {
            // Vérifier si la table existe
            String checkTableSQL = "SELECT 1 FROM positions_sieges_supprimes LIMIT 1";
            connexion.createStatement().executeQuery(checkTableSQL);

            // La table existe, récupérer les données
            String sql = "SELECT categorie, numero, position_x, position_y FROM positions_sieges_supprimes";

            try (Statement stmt = connexion.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    String categorie = rs.getString("categorie");
                    int numero = rs.getInt("numero");
                    double x = rs.getDouble("position_x");
                    double y = rs.getDouble("position_y");

                    if (positions.containsKey(categorie)) {
                        positions.get(categorie).put(numero, new double[]{x, y});
                    }
                }
            }
        } catch (SQLException e) {
            // Si la table n'existe pas, on ignore l'erreur
            System.err.println("La table positions_sieges_supprimes n'existe pas encore: " + e.getMessage());
        }

        return positions;
    }

    /**
     * Supprime la position d'un siège supprimé
     */
    public void supprimerPositionSiege(String categorie, int numero) throws SQLException {
        // Vérifier que la connexion est établie
        if (this.connexion == null || this.connexion.isClosed()) {
            // Réinitialiser la connexion
            this.connexion = DatabaseConnection.getInstance().getConnection();
            if (this.connexion == null) {
                throw new SQLException("Impossible d'établir la connexion à la base de données");
            }
        }

        try {
            // Vérifier si la table existe
            String checkTableSQL = "SELECT 1 FROM positions_sieges_supprimes LIMIT 1";
            connexion.createStatement().executeQuery(checkTableSQL);

            // La table existe, supprimer la position
            String sql = "DELETE FROM positions_sieges_supprimes WHERE categorie = ? AND numero = ?";

            try (PreparedStatement pstmt = connexion.prepareStatement(sql)) {
                pstmt.setString(1, categorie);
                pstmt.setInt(2, numero);
                pstmt.executeUpdate();
                System.out.println("Position du siège " + categorie + numero + " supprimée");
            }
        } catch (SQLException e) {
            // Si la table n'existe pas, on ignore l'erreur
            System.err.println("La table positions_sieges_supprimes n'existe pas encore: " + e.getMessage());
        }
    }

    /**
     * Nettoie la base de données des sièges en double
     */
    public void nettoyerSiegesEnDouble() throws SQLException {
        // Vérifier que la connexion est établie
        if (this.connexion == null || this.connexion.isClosed()) {
            // Réinitialiser la connexion
            this.connexion = DatabaseConnection.getInstance().getConnection();
            if (this.connexion == null) {
                throw new SQLException("Impossible d'établir la connexion à la base de données");
            }
        }

        // Récupérer tous les sièges
        List<Siege> sieges = lireTous();

        // Créer un ensemble pour détecter les doublons
        Set<String> siegesTraites = new HashSet<>();

        for (Siege siege : sieges) {
            String id = siege.getId();

            // Si l'ID est déjà traité, c'est un doublon
            if (siegesTraites.contains(id)) {
                System.out.println("Siège en double détecté: " + id + ". Suppression...");
                supprimer(id);
            } else {
                siegesTraites.add(id);
            }
        }
    }
}
