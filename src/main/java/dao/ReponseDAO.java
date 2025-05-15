package dao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import models.Reclamation;
import models.Utilisateur;
import models.Reponse;
import utils.DatabaseConnection;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReponseDAO {
    // Déclaration de la connexion comme attribut de classe
    private Connection connexion;

    // Constructeur qui initialise la connexion
    public ReponseDAO() throws SQLException {
        try {
            // Récupérer la connexion à la base de données
            this.connexion = DatabaseConnection.getInstance().getConnection();

            // Vérifier que la connexion est établie
            if (this.connexion == null) {
                throw new SQLException("Impossible d'établir la connexion à la base de données");
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de l'initialisation de ReponseDAO: " + e.getMessage());
            throw e;
        }
    }

    // Méthode pour ajouter une réponse
    public void ajouter(Reponse reponse) throws SQLException {
        // Vérifier que la connexion est établie
        if (this.connexion == null || this.connexion.isClosed()) {
            // Réinitialiser la connexion
            this.connexion = DatabaseConnection.getInstance().getConnection();
            if (this.connexion == null) {
                throw new SQLException("Impossible d'établir la connexion à la base de données");
            }
        }

        String sql = "INSERT INTO reponses (contenu, reclamation_id, admin_id, date) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstmt = connexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, reponse.getContenu());
            pstmt.setInt(2, reponse.getReclamationId());
            pstmt.setInt(3, reponse.getAdminId());
            pstmt.setDate(4, new java.sql.Date(reponse.getDate().getTime()));

            pstmt.executeUpdate();

            // Récupérer l'ID généré
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    reponse.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    // Méthode pour modifier une réponse
    public void modifier(Reponse reponse) throws SQLException {
        // Vérifier que la connexion est établie
        if (this.connexion == null || this.connexion.isClosed()) {
            // Réinitialiser la connexion
            this.connexion = DatabaseConnection.getInstance().getConnection();
            if (this.connexion == null) {
                throw new SQLException("Impossible d'établir la connexion à la base de données");
            }
        }

        String sql = "UPDATE reponses SET contenu = ?, reclamation_id = ?, admin_id = ? , date = ? WHERE id = ?";

        try (PreparedStatement pstmt = connexion.prepareStatement(sql)) {
            pstmt.setString(1, reponse.getContenu());
            pstmt.setInt(2, reponse.getReclamationId());
            pstmt.setInt(3, reponse.getAdminId());
            pstmt.setDate(4, new java.sql.Date(reponse.getDate().getTime()));
            pstmt.setInt(5, reponse.getId());

            pstmt.executeUpdate();
        }
    }

    // Méthode pour supprimer une réponse
    public void supprimer(int id) throws SQLException {
        // Vérifier que la connexion est établie
        if (this.connexion == null || this.connexion.isClosed()) {
            // Réinitialiser la connexion
            this.connexion = DatabaseConnection.getInstance().getConnection();
            if (this.connexion == null) {
                throw new SQLException("Impossible d'établir la connexion à la base de données");
            }
        }

        String sql = "DELETE FROM reponses WHERE id = ?";

        try (PreparedStatement pstmt = connexion.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    // Méthode pour lire une réponse par son ID
    public Reponse lire(int id) throws SQLException {
        // Vérifier que la connexion est établie
        if (this.connexion == null || this.connexion.isClosed()) {
            // Réinitialiser la connexion
            this.connexion = DatabaseConnection.getInstance().getConnection();
            if (this.connexion == null) {
                throw new SQLException("Impossible d'établir la connexion à la base de données");
            }
        }

        String sql = "SELECT * FROM reponses WHERE id = ?";

        try (PreparedStatement pstmt = connexion.prepareStatement(sql)) {
            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Reponse reponse = new Reponse();
                    reponse.setId(rs.getInt("id"));
                    reponse.setContenu(rs.getString("contenu"));
                    reponse.setReclamationId(rs.getInt("reclamation_id"));
                    reponse.setAdminId(rs.getInt("admin_id"));
                    reponse.setDate(rs.getDate("date"));
                    return reponse;
                }
            }
        }

        return null;
    }

    // Méthode pour lire toutes les réponses
    public List<Reponse> lireTous() throws SQLException {
        List<Reponse> reponses = new ArrayList<>();

        if (this.connexion == null || this.connexion.isClosed()) {
            this.connexion = DatabaseConnection.getInstance().getConnection();
            if (this.connexion == null) {
                throw new SQLException("Impossible d'établir la connexion à la base de données");
            }
        }

        String sql = """
        SELECT r.*, rec.objet AS reclamation_objet
        FROM reponses r
        JOIN reclamation rec ON r.reclamation_id = rec.id
        ORDER BY r.date DESC
    """;

        try (PreparedStatement stmt = connexion.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Reponse reponse = new Reponse();
                reponse.setId(rs.getInt("id"));
                reponse.setContenu(rs.getString("contenu"));
                reponse.setDate(rs.getTimestamp("date"));

                // Associer la réclamation avec objet
                Reclamation reclamation = new Reclamation();
                reclamation.setId(rs.getInt("reclamation_id"));
                reclamation.setObjet(rs.getString("reclamation_objet"));
                reponse.setReclamation(reclamation);

                // Associer l'admin (uniquement id)
                Utilisateur admin = new Utilisateur();
                admin.setId(rs.getInt("admin_id"));

                reponses.add(reponse);
            }
        }

        return reponses;
    }
    public List<Reponse> rechercherParContenu(String recherche) throws SQLException {
        List<Reponse> reponses = new ArrayList<>();

        if (this.connexion == null || this.connexion.isClosed()) {
            this.connexion = DatabaseConnection.getInstance().getConnection();
            if (this.connexion == null) {
                throw new SQLException("Impossible d'établir la connexion à la base de données");
            }
        }

        String sql = """
        SELECT r.*, rec.objet AS reclamation_objet
        FROM reponses r
        JOIN reclamation rec ON r.reclamation_id = rec.id
        WHERE r.contenu LIKE ?
        ORDER BY r.date DESC
    """;

        try (PreparedStatement pstmt = connexion.prepareStatement(sql)) {
            pstmt.setString(1, "%" + recherche + "%");

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Reponse reponse = new Reponse();
                    reponse.setId(rs.getInt("id"));
                    reponse.setContenu(rs.getString("contenu"));
                    reponse.setDate(rs.getTimestamp("date"));

                    // Réclamation avec son objet
                    Reclamation reclamation = new Reclamation();
                    reclamation.setId(rs.getInt("reclamation_id"));
                    reclamation.setObjet(rs.getString("reclamation_objet"));
                    reponse.setReclamation(reclamation);

                    // Admin (juste ID)
                    Utilisateur admin = new Utilisateur();
                    admin.setId(rs.getInt("admin_id"));
                    reponses.add(reponse);
                }
            }
        }

        return reponses;
    }


    // Méthode pour rechercher des réponses par date
    public List<Reponse> rechercherParDate(Date utilDate) throws SQLException {
        List<Reponse> reponses = new ArrayList<>();

        // Vérifier que la connexion est établie
        if (this.connexion == null || this.connexion.isClosed()) {
            // Réinitialiser la connexion
            this.connexion = DatabaseConnection.getInstance().getConnection();
            if (this.connexion == null) {
                throw new SQLException("Impossible d'établir la connexion à la base de données");
            }
        }

        // Formater la date pour la comparaison SQL
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dateStr = sdf.format(utilDate);

        // Utiliser la fonction DATE_FORMAT de SQL pour comparer uniquement la partie date
        String sql = "SELECT * FROM reponses WHERE DATE_FORMAT(date, '%Y-%m-%d') = ? ORDER BY date DESC";

        try (PreparedStatement pstmt = connexion.prepareStatement(sql)) {
            pstmt.setString(1, dateStr);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Reponse reponse = new Reponse();
                    reponse.setId(rs.getInt("id"));
                    reponse.setContenu(rs.getString("contenu"));
                    reponse.setReclamationId(rs.getInt("reclamation_id"));
                    reponse.setAdminId(rs.getInt("admin_id"));
                    reponse.setDate(rs.getDate("date"));
                    reponses.add(reponse);
                }
            }
        }

        return reponses;
    }
}
