package dao;

import models.PublicationInteraction;
import models.Utilisateur;
import models.Publication;
import utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PublicationInteractionDAOImpl implements PublicationInteractionDAO {
    private final Connection connection;

    public PublicationInteractionDAOImpl() {
        try {
            this.connection = DatabaseConnection.getInstance().getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la connexion à la base de données", e);
        }
    }

    @Override
    public void ajouter(PublicationInteraction interaction) {
        String sql = "INSERT INTO publication_interaction (publication_id, utilisateur_id, type_interaction, date_interaction) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, interaction.getPublication().getId());
            statement.setInt(2, interaction.getUtilisateur().getId());
            statement.setString(3, interaction.getTypeInteraction().toString());
            statement.setTimestamp(4, Timestamp.valueOf(interaction.getDateInteraction()));

            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    interaction.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'ajout de l'interaction", e);
        }
    }

    @Override
    public PublicationInteraction lire(Integer id) {
        String sql = "SELECT * FROM publication_interaction WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return extractInteractionFromResultSet(resultSet);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la lecture de l'interaction", e);
        }
        return null;
    }

    @Override
    public List<PublicationInteraction> lireTous() {
        List<PublicationInteraction> interactions = new ArrayList<>();
        String sql = "SELECT * FROM publication_interaction ORDER BY date_interaction DESC";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                interactions.add(extractInteractionFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la lecture des interactions", e);
        }
        return interactions;
    }

    @Override
    public void modifier(PublicationInteraction interaction) {
        String sql = "UPDATE publication_interaction SET publication_id = ?, utilisateur_id = ?, type_interaction = ?, date_interaction = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, interaction.getPublication().getId());
            statement.setInt(2, interaction.getUtilisateur().getId());
            statement.setString(3, interaction.getTypeInteraction().toString());
            statement.setTimestamp(4, Timestamp.valueOf(interaction.getDateInteraction()));
            statement.setInt(5, interaction.getId());

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la modification de l'interaction", e);
        }
    }

    @Override
    public void supprimer(Integer id) {
        String sql = "DELETE FROM publication_interaction WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression de l'interaction", e);
        }
    }

    @Override
    public List<PublicationInteraction> lireParPublication(Publication publication) {
        List<PublicationInteraction> interactions = new ArrayList<>();
        String sql = "SELECT * FROM publication_interaction WHERE publication_id = ? ORDER BY date_interaction DESC";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, publication.getId());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    interactions.add(extractInteractionFromResultSet(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la lecture des interactions de la publication", e);
        }
        return interactions;
    }

    @Override
    public List<PublicationInteraction> lireParUtilisateur(Utilisateur utilisateur) {
        List<PublicationInteraction> interactions = new ArrayList<>();
        String sql = "SELECT * FROM publication_interaction WHERE utilisateur_id = ? ORDER BY date_interaction DESC";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, utilisateur.getId());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    interactions.add(extractInteractionFromResultSet(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la lecture des interactions de l'utilisateur", e);
        }
        return interactions;
    }

    @Override
    public boolean existeInteraction(Utilisateur utilisateur, Publication publication) {
        String sql = "SELECT COUNT(*) FROM publication_interaction WHERE utilisateur_id = ? AND publication_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, utilisateur.getId());
            statement.setInt(2, publication.getId());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la vérification de l'interaction", e);
        }
        return false;
    }

    @Override
    public int nombreLikes(Publication publication) {
        String sql = "SELECT COUNT(*) FROM publication_interaction WHERE publication_id = ? AND type_interaction = 'LIKE'";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, publication.getId());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du comptage des likes", e);
        }
        return 0;
    }

    @Override
    public int nombreDislikes(Publication publication) {
        String sql = "SELECT COUNT(*) FROM publication_interaction WHERE publication_id = ? AND type_interaction = 'DISLIKE'";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, publication.getId());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du comptage des dislikes", e);
        }
        return 0;
    }

    @Override
    public void supprimerInteraction(Utilisateur utilisateur, Publication publication) {
        String sql = "DELETE FROM publication_interaction WHERE utilisateur_id = ? AND publication_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, utilisateur.getId());
            statement.setInt(2, publication.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression de l'interaction", e);
        }
    }

    private PublicationInteraction extractInteractionFromResultSet(ResultSet resultSet) throws SQLException {
        PublicationInteraction interaction = new PublicationInteraction();
        interaction.setId(resultSet.getInt("id"));
        
        // Get publication
        PublicationDAO publicationDAO = new PublicationDAOImpl();
        Publication publication = publicationDAO.lire(resultSet.getInt("publication_id"));
        interaction.setPublication(publication);
        
        // Get user
        UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
        Utilisateur utilisateur = utilisateurDAO.lire(resultSet.getInt("utilisateur_id"));
        interaction.setUtilisateur(utilisateur);
        
        interaction.setTypeInteraction(PublicationInteraction.InteractionType.valueOf(resultSet.getString("type_interaction")));
        interaction.setDateInteraction(resultSet.getTimestamp("date_interaction").toLocalDateTime());
        
        return interaction;
    }
} 