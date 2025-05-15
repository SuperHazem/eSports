package dao;

import enums.UserStatus;
import models.*;
import enums.Role;
import utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UtilisateurDAO implements GenericDAO<Utilisateur, Integer> {

    private Connection connection;

    public UtilisateurDAO() throws SQLException {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }
    
    // Add this method to expose the connection
    public Connection getConnection() {
        return this.connection;
    }

    @Override
    public void ajouter(Utilisateur utilisateur) {
        String query = "INSERT INTO utilisateur (email, motDePasseHash, role, nom, prenom, adresse, telephone, date_naissance, profile_picture_path) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, utilisateur.getEmail());
            stmt.setString(2, utilisateur.getMotDePasseHash());
            stmt.setString(3, utilisateur.getRole().toString());
            stmt.setString(4, utilisateur.getNom());
            stmt.setString(5, utilisateur.getPrenom());
            stmt.setString(6, utilisateur.getAdresse());
            stmt.setString(7, utilisateur.getTelephone());
            stmt.setDate(8, utilisateur.getDateNaissance() != null ? java.sql.Date.valueOf(utilisateur.getDateNaissance()) : null);
            stmt.setString(9, utilisateur.getProfilePicturePath());
            stmt.executeUpdate();

            // Retrieve the generated ID and set it in the object
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                int userId = rs.getInt(1);
                utilisateur.setId(userId);

                // Insert role-specific data
                insertRoleSpecificData(utilisateur);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void insertRoleSpecificData(Utilisateur utilisateur) throws SQLException {
        int userId = utilisateur.getId();
        Role role = utilisateur.getRole();

        switch (role) {
            case ADMIN:
                String adminQuery = "INSERT INTO admin (utilisateur_id) VALUES (?)";
                try (PreparedStatement stmt = connection.prepareStatement(adminQuery)) {
                    stmt.setInt(1, userId);
                    stmt.executeUpdate();
                }
                break;

            case COACH:
                if (utilisateur instanceof Coach) {
                    String coachQuery = "INSERT INTO coach (utilisateur_id, strategie) VALUES (?, ?)";
                    try (PreparedStatement stmt = connection.prepareStatement(coachQuery)) {
                        stmt.setInt(1, userId);
                        stmt.setString(2, ((Coach) utilisateur).getStrategie());
                        stmt.executeUpdate();
                    }
                } else {
                    // Gérer le cas où l'utilisateur n'est pas un Coach
                    String coachQuery = "INSERT INTO coach (utilisateur_id, strategie) VALUES (?, ?)";
                    try (PreparedStatement stmt = connection.prepareStatement(coachQuery)) {
                        stmt.setInt(1, userId);
                        stmt.setString(2, ""); // Valeur par défaut
                        stmt.executeUpdate();
                    }
                }
                break;

            case JOUEUR:
                if (utilisateur instanceof Joueur) {
                    String joueurQuery = "INSERT INTO joueur (utilisateur_id, pseudo_jeu, rank, win_rate) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement stmt = connection.prepareStatement(joueurQuery)) {
                        stmt.setInt(1, userId);
                        stmt.setString(2, ((Joueur) utilisateur).getPseudoJeu());
                        stmt.setString(3, ((Joueur) utilisateur).getRank());
                        stmt.setDouble(4, ((Joueur) utilisateur).getWinRate());
                        stmt.executeUpdate();
                    }
                } else {
                    // Gérer le cas où l'utilisateur n'est pas un Joueur
                    String joueurQuery = "INSERT INTO joueur (utilisateur_id, pseudo_jeu, rank, win_rate) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement stmt = connection.prepareStatement(joueurQuery)) {
                        stmt.setInt(1, userId);
                        stmt.setString(2, ""); // Valeur par défaut
                        stmt.setString(3, ""); // Valeur par défaut
                        stmt.setDouble(4, 0.0); // Valeur par défaut
                        stmt.executeUpdate();
                    }
                }
                break;

            case SPECTATEUR:
                if (utilisateur instanceof Spectateur) {
                    String spectateurQuery = "INSERT INTO spectateur (utilisateur_id, date_inscription) VALUES (?, ?)";
                    try (PreparedStatement stmt = connection.prepareStatement(spectateurQuery)) {
                        stmt.setInt(1, userId);
                        java.sql.Date sqlDate = new java.sql.Date(((Spectateur) utilisateur).getDateInscription().getTime());
                        stmt.setDate(2, sqlDate);
                        stmt.executeUpdate();
                    }
                } else {
                    // Gérer le cas où l'utilisateur n'est pas un Spectateur
                    String spectateurQuery = "INSERT INTO spectateur (utilisateur_id, date_inscription) VALUES (?, ?)";
                    try (PreparedStatement stmt = connection.prepareStatement(spectateurQuery)) {
                        stmt.setInt(1, userId);
                        java.sql.Date sqlDate = new java.sql.Date(new Date().getTime()); // Date actuelle
                        stmt.setDate(2, sqlDate);
                        stmt.executeUpdate();
                    }
                }
                break;
        }
    }


    // Excerpt from the UtilisateurDAO class - the lire method
    @Override
    public Utilisateur lire(Integer id) {
        String query = "SELECT * FROM utilisateur WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int userId = rs.getInt("id");
                String email = rs.getString("email");
                String motDePasseHash = rs.getString("motDePasseHash");
                Role role = Role.valueOf(rs.getString("role"));
                String nom = rs.getString("nom");
                String prenom = rs.getString("prenom");
                String adresse = rs.getString("adresse");
                String telephone = rs.getString("telephone");
                Date dateNaissanceSQL = rs.getDate("date_naissance");
                String profilePicturePath = rs.getString("profile_picture_path");

                // Create the basic Utilisateur with parameters in the correct order
                Utilisateur utilisateur = new Utilisateur(userId, email, motDePasseHash, role, nom, prenom);

                // Set the additional fields
                if (adresse != null) utilisateur.setAdresse(adresse);
                if (telephone != null) utilisateur.setTelephone(telephone);
                if (dateNaissanceSQL != null) utilisateur.setDateNaissance(((java.sql.Date) dateNaissanceSQL).toLocalDate());
                if (profilePicturePath != null) utilisateur.setProfilePicturePath(profilePicturePath);

                // Retrieve and set status fields
                String statusStr = rs.getString("status");
                if (statusStr != null) {
                    try {
                        utilisateur.setStatus(UserStatus.valueOf(statusStr));
                    } catch (IllegalArgumentException e) {
                        // Handle cases where statusStr might not be a valid enum constant
                        System.err.println("Invalid status value in database: " + statusStr + " for user ID: " + userId);
                        // Optionally set a default status or handle as an error
                        utilisateur.setStatus(UserStatus.ACTIF); // Example: default to ACTIF
                    }
                } else {
                     utilisateur.setStatus(UserStatus.ACTIF); // Default to ACTIF if status is null in DB
                }

                java.sql.Date suspensionDebutSQL = rs.getDate("suspension_debut");
                if (suspensionDebutSQL != null) {
                    utilisateur.setSuspensionDebut(suspensionDebutSQL.toLocalDate());
                }

                java.sql.Date suspensionFinSQL = rs.getDate("suspension_fin");
                if (suspensionFinSQL != null) {
                    utilisateur.setSuspensionFin(suspensionFinSQL.toLocalDate());
                }

                String suspensionRaison = rs.getString("suspension_raison");
                if (suspensionRaison != null) {
                    utilisateur.setSuspensionRaison(suspensionRaison);
                }

                return utilisateur;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Utilisateur> rechercherParNomPrenom(String searchTerm) {
        List<Utilisateur> utilisateurs = new ArrayList<>();
        String query = "SELECT * FROM utilisateur WHERE nom LIKE ? OR prenom LIKE ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            String searchPattern = "%" + searchTerm + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                utilisateurs.add(extractUtilisateurWithRoleData(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return utilisateurs;
    }

    @Override
    public List<Utilisateur> lireTous() {
        List<Utilisateur> utilisateurs = new ArrayList<>();
        String query = "SELECT * FROM utilisateur";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                utilisateurs.add(extractUtilisateurWithRoleData(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return utilisateurs;
    }

    @Override
    public void modifier(Utilisateur utilisateur) {
        String query = "UPDATE utilisateur SET email = ?, motDePasseHash = ?, role = ?, nom = ?, prenom = ?, adresse = ?, telephone = ?, date_naissance = ?, profile_picture_path = ?, status = ?, suspension_debut = ?, suspension_fin = ?, suspension_raison = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, utilisateur.getEmail());
            stmt.setString(2, utilisateur.getMotDePasseHash());
            stmt.setString(3, utilisateur.getRole().toString());
            stmt.setString(4, utilisateur.getNom());
            stmt.setString(5, utilisateur.getPrenom());
            stmt.setString(6, utilisateur.getAdresse());
            stmt.setString(7, utilisateur.getTelephone());
            stmt.setDate(8, utilisateur.getDateNaissance() != null ? java.sql.Date.valueOf(utilisateur.getDateNaissance()) : null);
            stmt.setString(9, utilisateur.getProfilePicturePath());
            
            // Add status fields
            stmt.setString(10, utilisateur.getStatus() != null ? utilisateur.getStatus().toString() : null);
            stmt.setDate(11, utilisateur.getSuspensionDebut() != null ? java.sql.Date.valueOf(utilisateur.getSuspensionDebut()) : null);
            stmt.setDate(12, utilisateur.getSuspensionFin() != null ? java.sql.Date.valueOf(utilisateur.getSuspensionFin()) : null);
            stmt.setString(13, utilisateur.getSuspensionRaison());
            
            stmt.setInt(14, utilisateur.getId());
            stmt.executeUpdate();

            // Update role-specific data
            updateRoleSpecificData(utilisateur);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateRoleSpecificData(Utilisateur utilisateur) throws SQLException {
        int userId = utilisateur.getId();
        Role role = utilisateur.getRole();

        // First, delete any existing role-specific data for this user
        deleteRoleSpecificData(userId);

        // Then insert new role-specific data
        insertRoleSpecificData(utilisateur);
    }

    private void deleteRoleSpecificData(int userId) throws SQLException {
        // Delete from all role-specific tables
        String[] tables = {"admin", "coach", "joueur", "spectateur"};
        for (String table : tables) {
            String query = "DELETE FROM " + table + " WHERE utilisateur_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, userId);
                stmt.executeUpdate();
            }
        }
    }

    @Override
    public void supprimer(Integer id) {
        // Due to foreign key constraints with ON DELETE CASCADE,
        // deleting from the utilisateur table will automatically delete
        // from the role-specific tables
        String query = "DELETE FROM utilisateur WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Utilisateur extractUtilisateurWithRoleData(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String email = rs.getString("email");
        String motDePasseHash = rs.getString("motDePasseHash");
        Role role = Role.valueOf(rs.getString("role"));
        String nom = rs.getString("nom");
        String prenom = rs.getString("prenom");
        String adresse = rs.getString("adresse");
        String telephone = rs.getString("telephone");
        Date dateNaissanceSQL = rs.getDate("date_naissance");
        String profilePicturePath = rs.getString("profile_picture_path");
        
        // Récupérer les informations de statut
        String statusStr = rs.getString("status");
        Date suspensionDebutSQL = rs.getDate("suspension_debut");
        Date suspensionFinSQL = rs.getDate("suspension_fin");
        String suspensionRaison = rs.getString("suspension_raison");

        Utilisateur utilisateur;

        switch (role) {
            case ADMIN:
                utilisateur = new Admin(role, motDePasseHash, email, id, nom, prenom);
                break;

            case COACH:
                String strategie = getCoachStrategie(id);
                utilisateur = new Coach(role, motDePasseHash, email, id, nom, prenom, strategie);
                break;

            case JOUEUR:
                Joueur joueur = getJoueurData(id);
                utilisateur = new Joueur(role, motDePasseHash, email, id, nom, prenom,
                        joueur.getPseudoJeu(), joueur.getWinRate(), joueur.getRank());
                break;

            case SPECTATEUR:
                Date dateInscription = getSpectateurDateInscription(id);
                utilisateur = new Spectateur(role, motDePasseHash, email, id, nom, prenom, dateInscription);
                break;

            default:
                utilisateur = new Utilisateur(id, email, motDePasseHash, role, nom, prenom);
        }
        
        // Set the additional fields for all user types
        if (adresse != null) utilisateur.setAdresse(adresse);
        if (telephone != null) utilisateur.setTelephone(telephone);
        if (dateNaissanceSQL != null) utilisateur.setDateNaissance(((java.sql.Date) dateNaissanceSQL).toLocalDate());
        if (profilePicturePath != null) utilisateur.setProfilePicturePath(profilePicturePath);
        
        // Set status fields
        if (statusStr != null) {
            try {
                utilisateur.setStatus(UserStatus.valueOf(statusStr));
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid status value in database: " + statusStr + " for user ID: " + id);
                utilisateur.setStatus(UserStatus.ACTIF); // Default to ACTIF
            }
        } else {
            utilisateur.setStatus(UserStatus.ACTIF); // Default to ACTIF if status is null in DB
        }

        if (suspensionDebutSQL != null) {
            utilisateur.setSuspensionDebut(((java.sql.Date) suspensionDebutSQL).toLocalDate());
        }
        if (suspensionFinSQL != null) {
            utilisateur.setSuspensionFin(((java.sql.Date) suspensionFinSQL).toLocalDate());
        }
        if (suspensionRaison != null) {
            utilisateur.setSuspensionRaison(suspensionRaison);
        }

        return utilisateur;
    }

    private String getCoachStrategie(int userId) throws SQLException {
        String query = "SELECT strategie FROM coach WHERE utilisateur_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("strategie");
            }
        }
        return "";
    }

    private Joueur getJoueurData(int userId) throws SQLException {
        String query = "SELECT pseudo_jeu, rank, win_rate FROM joueur WHERE utilisateur_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String pseudoJeu = rs.getString("pseudo_jeu");
                String rank = rs.getString("rank");
                double winRate = rs.getDouble("win_rate");
                return new Joueur(Role.JOUEUR, "", "", userId, "", "", pseudoJeu, winRate, rank);
            }
        }
        return new Joueur(Role.JOUEUR, "", "", userId, "", "", "", 0.0, "");
    }

    private Date getSpectateurDateInscription(int userId) throws SQLException {
        String query = "SELECT date_inscription FROM spectateur WHERE utilisateur_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDate("date_inscription");
            }
        }
        return new Date();
    }
    // Add this method to your existing UtilisateurDAO class

    public void updatePasswordByEmail(String email, String newPassword) {
        String query = "UPDATE utilisateur SET motDePasseHash = ? WHERE email = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, newPassword);
            statement.setString(2, email);
            int rowsAffected = statement.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("No user found with email: " + email);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to update password: " + e.getMessage());
        }
    }
    
    public Utilisateur findByEmail(String email) {
        String query = "SELECT * FROM utilisateur WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return extractUtilisateurWithRoleData(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Bannir un utilisateur de façon permanente
     * @param utilisateurId ID de l'utilisateur à bannir
     * @param raison Raison du bannissement
     * @return true si le bannissement a réussi, false sinon
     */
    public boolean bannirUtilisateur(int utilisateurId, String raison) {
        Utilisateur utilisateur = lire(utilisateurId);
        if (utilisateur == null) return false;
        
        utilisateur.setStatus(UserStatus.BANNI);
        utilisateur.setSuspensionRaison(raison);
        utilisateur.setSuspensionDebut(LocalDate.now());
        utilisateur.setSuspensionFin(null); // Pas de date de fin pour un bannissement permanent
        
        try {
            modifier(utilisateur);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Débannir un utilisateur précédemment banni
     * @param utilisateurId ID de l'utilisateur à débannir
     * @return true si le débannissement a réussi, false sinon
     */
    public boolean debannirUtilisateur(int utilisateurId) {
        Utilisateur utilisateur = lire(utilisateurId);
        if (utilisateur == null || utilisateur.getStatus() != UserStatus.BANNI) return false;
        
        utilisateur.setStatus(UserStatus.ACTIF);
        utilisateur.setSuspensionRaison(null);
        utilisateur.setSuspensionDebut(null);
        utilisateur.setSuspensionFin(null);
        
        try {
            modifier(utilisateur);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Suspendre temporairement un utilisateur (time-out)
     * @param utilisateurId ID de l'utilisateur à suspendre
     * @param raison Raison de la suspension
     * @param dureeJours Durée de la suspension en jours
     * @return true si la suspension a réussi, false sinon
     */
    public boolean suspendreUtilisateur(int utilisateurId, String raison, int dureeJours) {
        if (dureeJours <= 0) return false;
        
        Utilisateur utilisateur = lire(utilisateurId);
        if (utilisateur == null) return false;
        
        LocalDate dateDebut = LocalDate.now();
        LocalDate dateFin = dateDebut.plusDays(dureeJours);
        
        utilisateur.setStatus(UserStatus.SUSPENDU);
        utilisateur.setSuspensionRaison(raison);
        utilisateur.setSuspensionDebut(dateDebut);
        utilisateur.setSuspensionFin(dateFin);
        
        try {
            modifier(utilisateur);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Lever la suspension d'un utilisateur avant la date de fin prévue
     * @param utilisateurId ID de l'utilisateur dont la suspension doit être levée
     * @return true si la levée de suspension a réussi, false sinon
     */
    public boolean leverSuspension(int utilisateurId) {
        Utilisateur utilisateur = lire(utilisateurId);
        if (utilisateur == null || utilisateur.getStatus() != UserStatus.SUSPENDU) return false;
        
        utilisateur.setStatus(UserStatus.ACTIF);
        utilisateur.setSuspensionRaison(null);
        utilisateur.setSuspensionDebut(null);
        utilisateur.setSuspensionFin(null);
        
        try {
            modifier(utilisateur);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}