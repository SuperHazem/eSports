package dao;

import models.ParticipationEvent;
import models.Utilisateur;
import models.EventSocial;
import utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ParticipationEventDAOImpl implements ParticipationEventDAO {
    private final Connection connection;

    public ParticipationEventDAOImpl() {
        try {
            this.connection = DatabaseConnection.getInstance().getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la connexion à la base de données", e);
        }
    }

    @Override
    public void ajouter(ParticipationEvent participation) {
        String sql = "INSERT INTO participation_event (utilisateur_id, event_id, date_participation, confirme) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, participation.getUtilisateur().getId());
            statement.setInt(2, participation.getEvent().getId());
            statement.setTimestamp(3, Timestamp.valueOf(participation.getDateParticipation()));
            statement.setBoolean(4, participation.isConfirme());

            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    participation.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'ajout de la participation", e);
        }
    }

    @Override
    public ParticipationEvent lire(Integer id) {
        String sql = "SELECT * FROM participation_event WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return extractParticipationFromResultSet(resultSet);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la lecture de la participation", e);
        }
        return null;
    }

    @Override
    public List<ParticipationEvent> lireTous() {
        List<ParticipationEvent> participations = new ArrayList<>();
        String sql = "SELECT * FROM participation_event";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                participations.add(extractParticipationFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la lecture des participations", e);
        }
        return participations;
    }

    @Override
    public void modifier(ParticipationEvent participation) {
        String sql = "UPDATE participation_event SET utilisateur_id = ?, event_id = ?, date_participation = ?, confirme = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, participation.getUtilisateur().getId());
            statement.setInt(2, participation.getEvent().getId());
            statement.setTimestamp(3, Timestamp.valueOf(participation.getDateParticipation()));
            statement.setBoolean(4, participation.isConfirme());
            statement.setInt(5, participation.getId());

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la modification de la participation", e);
        }
    }

    @Override
    public void supprimer(Integer id) {
        String sql = "DELETE FROM participation_event WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression de la participation", e);
        }
    }

    @Override
    public List<ParticipationEvent> lireParUtilisateur(Utilisateur utilisateur) {
        List<ParticipationEvent> participations = new ArrayList<>();
        String sql = "SELECT * FROM participation_event WHERE utilisateur_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, utilisateur.getId());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    participations.add(extractParticipationFromResultSet(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la lecture des participations de l'utilisateur", e);
        }
        return participations;
    }

    @Override
    public List<ParticipationEvent> lireParEvent(EventSocial event) {
        List<ParticipationEvent> participations = new ArrayList<>();
        String sql = "SELECT * FROM participation_event WHERE event_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, event.getId());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    participations.add(extractParticipationFromResultSet(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la lecture des participations à l'événement", e);
        }
        return participations;
    }

    @Override
    public boolean existeParticipation(Utilisateur utilisateur, EventSocial event) {
        String sql = "SELECT COUNT(*) FROM participation_event WHERE utilisateur_id = ? AND event_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
           // statement.setInt(1, utilisateur.getId());
            statement.setInt(1, 1);
            statement.setInt(2, event.getId());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la vérification de la participation", e);
        }
        return false;
    }

    @Override
    public int nombreParticipants(EventSocial event) {
        String sql = "SELECT COUNT(*) FROM participation_event WHERE event_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, event.getId());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du comptage des participants", e);
        }
        return 0;
    }

    private ParticipationEvent extractParticipationFromResultSet(ResultSet resultSet) throws SQLException {
        ParticipationEvent participation = new ParticipationEvent();
        participation.setId(resultSet.getInt("id"));
        
        // Get user
        UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
        Utilisateur utilisateur = utilisateurDAO.lire(resultSet.getInt("utilisateur_id"));
        participation.setUtilisateur(utilisateur);
        
        // Get event
        EventSocialDAO eventDAO = new EventSocialDAOImpl();
        EventSocial event = eventDAO.lire(resultSet.getInt("event_id"));
        participation.setEvent(event);
        
        participation.setDateParticipation(resultSet.getTimestamp("date_participation").toLocalDateTime());
        participation.setConfirme(resultSet.getBoolean("confirme"));
        
        return participation;
    }
} 