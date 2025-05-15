package dao;

import models.Commentaire;
import models.Publication;
import utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentaireDAOImpl implements CommentaireDAO {
    private final Connection connection;
    private final PublicationDAO publicationDAO;

    public CommentaireDAOImpl() {
        try {
            this.connection = DatabaseConnection.getInstance().getConnection();
            this.publicationDAO = new PublicationDAOImpl();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la connexion à la base de données", e);
        }
    }

    @Override
    public void ajouter(Commentaire commentaire) {
        String sql = "INSERT INTO commentaire (contenu, note, date, publication_id, auteur) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, commentaire.getContenu());
            statement.setInt(2, commentaire.getNote());
            statement.setTimestamp(3, new Timestamp(commentaire.getDate().getTime()));
            statement.setInt(4, commentaire.getPublication().getId());
            statement.setInt(5, Commentaire.getAuteur());
            
            int affectedRows = statement.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("La création du commentaire a échoué, aucune ligne affectée.");
            }
            
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    commentaire.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("La création du commentaire a échoué, aucun ID obtenu.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'ajout du commentaire: " + e.getMessage(), e);
        }
    }

    @Override
    public Commentaire lire(Integer id) {
        String sql = "SELECT * FROM commentaire WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return extractCommentaireFromResultSet(resultSet);
                }
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la lecture du commentaire: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Commentaire> lireTous() {
        List<Commentaire> commentaires = new ArrayList<>();
        String sql = "SELECT * FROM commentaire ORDER BY date DESC";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                commentaires.add(extractCommentaireFromResultSet(resultSet));
            }
            return commentaires;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la lecture des commentaires: " + e.getMessage(), e);
        }
    }

    @Override
    public void modifier(Commentaire commentaire) {
        String sql = "UPDATE commentaire SET contenu = ?, note = ? WHERE id = ? AND auteur = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, commentaire.getContenu());
            statement.setInt(2, commentaire.getNote());
            statement.setInt(3, commentaire.getId());
            statement.setInt(4, Commentaire.getAuteur());
            
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new RuntimeException("Commentaire non trouvé ou non autorisé à être modifié");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la modification du commentaire: " + e.getMessage(), e);
        }
    }

    @Override
    public void supprimer(Integer id) {
        String sql = "DELETE FROM commentaire WHERE id = ? AND auteur = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.setInt(2, Commentaire.getAuteur());
            
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new RuntimeException("Commentaire non trouvé ou non autorisé à être supprimé");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression du commentaire: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Commentaire> lireParPublication(Integer publicationId) {
        List<Commentaire> commentaires = new ArrayList<>();
        String sql = "SELECT * FROM commentaire WHERE publication_id = ? ORDER BY date DESC";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, publicationId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    commentaires.add(extractCommentaireFromResultSet(resultSet));
                }
            }
            return commentaires;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la lecture des commentaires de la publication: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Commentaire> lireParUtilisateur(Integer utilisateurId) {
        List<Commentaire> commentaires = new ArrayList<>();
        String sql = "SELECT * FROM commentaire WHERE utilisateur_id = ? ORDER BY date_creation DESC";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, utilisateurId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    commentaires.add(extractCommentaireFromResultSet(resultSet));
                }
            }
            return commentaires;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la lecture des commentaires de l'utilisateur", e);
        }
    }

    private Commentaire extractCommentaireFromResultSet(ResultSet resultSet) throws SQLException {
        Commentaire commentaire = new Commentaire();
        commentaire.setId(resultSet.getInt("id"));
        commentaire.setContenu(resultSet.getString("contenu"));
        commentaire.setNote(resultSet.getInt("note"));
        commentaire.setDate(resultSet.getTimestamp("date"));
        
        // Get the associated publication
        Publication publication = publicationDAO.lire(resultSet.getInt("publication_id"));
        if (publication == null) {
            throw new SQLException("La publication associée au commentaire n'existe pas");
        }
        commentaire.setPublication(publication);
        
        return commentaire;
    }
} 