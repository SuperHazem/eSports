package dao;

import models.Publication;
import utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PublicationDAOImpl implements PublicationDAO {
    private final Connection connection;

    public PublicationDAOImpl() {
        try {
            this.connection = DatabaseConnection.getInstance().getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la connexion à la base de données", e);
        }
    }

    @Override
    public void ajouter(Publication publication) {
        String sql = "INSERT INTO publication (titre, contenu, date_publication, auteur) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, publication.getTitre());
            statement.setString(2, publication.getContenu());
            statement.setTimestamp(3, new Timestamp(publication.getDatePublication().getTime()));
            statement.setInt(4, Publication.getAuteur());
            
            int affectedRows = statement.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("La création de la publication a échoué, aucune ligne affectée.");
            }
            
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    publication.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("La création de la publication a échoué, aucun ID obtenu.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'ajout de la publication: " + e.getMessage(), e);
        }
    }

    @Override
    public Publication lire(Integer id) {
        String sql = "SELECT * FROM publication WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return extractPublicationFromResultSet(resultSet);
                }
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la lecture de la publication", e);
        }
    }

    @Override
    public List<Publication> lireTous() {
        List<Publication> publications = new ArrayList<>();
        String sql = "SELECT * FROM publication ORDER BY date_publication DESC";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                publications.add(extractPublicationFromResultSet(resultSet));
            }
            return publications;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la lecture des publications", e);
        }
    }

    @Override
    public void modifier(Publication publication) {
        String sql = "UPDATE publication SET titre = ?, contenu = ? WHERE id = ? AND auteur = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, publication.getTitre());
            statement.setString(2, publication.getContenu());
            statement.setInt(3, publication.getId());
            statement.setInt(4, Publication.getAuteur());
            
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new RuntimeException("Publication non trouvée ou non autorisée à être modifiée");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la modification de la publication", e);
        }
    }

    @Override
    public void supprimer(Integer id) {
        String sql = "DELETE FROM publication WHERE id = ? AND auteur = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.setInt(2, Publication.getAuteur());
            
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new RuntimeException("Publication non trouvée ou non autorisée à être supprimée");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression de la publication", e);
        }
    }

    @Override
    public List<Publication> lireParUtilisateur(Integer utilisateurId) {
        List<Publication> publications = new ArrayList<>();
        String sql = "SELECT * FROM publication WHERE utilisateur_id = ? ORDER BY date_creation DESC";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, utilisateurId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    publications.add(extractPublicationFromResultSet(resultSet));
                }
            }
            return publications;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la lecture des publications de l'utilisateur", e);
        }
    }

    private Publication extractPublicationFromResultSet(ResultSet resultSet) throws SQLException {
        Publication publication = new Publication();
        publication.setId(resultSet.getInt("id"));
        publication.setTitre(resultSet.getString("titre"));
        publication.setContenu(resultSet.getString("contenu"));
        publication.setDatePublication(resultSet.getTimestamp("date_publication"));
        return publication;
    }
} 