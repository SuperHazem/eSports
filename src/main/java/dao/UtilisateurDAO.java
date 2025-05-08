package dao;

import models.*;
import enums.Role;
import utils.DatabaseConnection;

import java.sql.*;
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
        String query = "INSERT INTO utilisateur (email, motDePasseHash, role, nom, prenom) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, utilisateur.getEmail());
            stmt.setString(2, utilisateur.getMotDePasseHash());
            stmt.setString(3, utilisateur.getRole().toString());
            stmt.setString(4, utilisateur.getNom());
            stmt.setString(5, utilisateur.getPrenom());
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
                String coachQuery = "INSERT INTO coach (utilisateur_id, strategie) VALUES (?, ?)";
                try (PreparedStatement stmt = connection.prepareStatement(coachQuery)) {
                    stmt.setInt(1, userId);
                    stmt.setString(2, ((Coach) utilisateur).getStrategie());
                    stmt.executeUpdate();
                }
                break;

            case JOUEUR:
                String joueurQuery = "INSERT INTO joueur (utilisateur_id, pseudo_jeu, rank, win_rate) VALUES (?, ?, ?, ?)";
                try (PreparedStatement stmt = connection.prepareStatement(joueurQuery)) {
                    stmt.setInt(1, userId);
                    stmt.setString(2, ((Joueur) utilisateur).getPseudoJeu());
                    stmt.setString(3, ((Joueur) utilisateur).getRank());
                    stmt.setDouble(4, ((Joueur) utilisateur).getWinRate());
                    stmt.executeUpdate();
                }
                break;

            case SPECTATEUR:
                String spectateurQuery = "INSERT INTO spectateur (utilisateur_id, date_inscription) VALUES (?, ?)";
                try (PreparedStatement stmt = connection.prepareStatement(spectateurQuery)) {
                    stmt.setInt(1, userId);
                    java.sql.Date sqlDate = new java.sql.Date(((Spectateur) utilisateur).getDateInscription().getTime());
                    stmt.setDate(2, sqlDate);
                    stmt.executeUpdate();
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

                // Make sure to create the Utilisateur with parameters in the correct order
                return new Utilisateur(userId, email, motDePasseHash, role, nom, prenom);
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
        String query = "UPDATE utilisateur SET email = ?, motDePasseHash = ?, role = ?, nom = ?, prenom = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, utilisateur.getEmail());
            stmt.setString(2, utilisateur.getMotDePasseHash());
            stmt.setString(3, utilisateur.getRole().toString());
            stmt.setString(4, utilisateur.getNom());
            stmt.setString(5, utilisateur.getPrenom());
            stmt.setInt(6, utilisateur.getId());
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
}