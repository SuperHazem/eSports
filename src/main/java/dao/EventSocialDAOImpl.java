package dao;

import models.EventSocial;
import utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EventSocialDAOImpl implements EventSocialDAO {
    private final Connection connection;

    public EventSocialDAOImpl() {
        try {
            this.connection = DatabaseConnection.getInstance().getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la connexion à la base de données", e);
        }
    }

    @Override
    public void ajouter(EventSocial event) {
        String sql = "INSERT INTO event_social (nom, date, lieu, description, capacite) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, event.getNom());
            statement.setDate(2, Date.valueOf(event.getDate()));
            statement.setString(3, event.getLieu());
            statement.setString(4, event.getDescription());
            statement.setInt(5, event.getCapacite());

            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    event.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'ajout de l'événement", e);
        }
    }

    @Override
    public EventSocial lire(Integer id) {
        String sql = "SELECT * FROM event_social WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return extractEventFromResultSet(resultSet);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la lecture de l'événement", e);
        }
        return null;
    }

    @Override
    public List<EventSocial> lireTous() {
        List<EventSocial> events = new ArrayList<>();
        String sql = "SELECT * FROM event_social";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                events.add(extractEventFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la lecture des événements", e);
        }
        return events;
    }

    @Override
    public void modifier(EventSocial event) {
        String sql = "UPDATE event_social SET nom = ?, date = ?, lieu = ?, description = ?, capacite = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, event.getNom());
            statement.setDate(2, Date.valueOf(event.getDate()));
            statement.setString(3, event.getLieu());
            statement.setString(4, event.getDescription());
            statement.setInt(5, event.getCapacite());
            statement.setInt(6, event.getId());

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la modification de l'événement", e);
        }
    }

    @Override
    public void supprimer(Integer id) {
        String sql = "DELETE FROM event_social WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression de l'événement", e);
        }
    }

    @Override
    public List<EventSocial> lireParNom(String nom) {
        List<EventSocial> events = new ArrayList<>();
        String sql = "SELECT * FROM event_social WHERE nom LIKE ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "%" + nom + "%");
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    events.add(extractEventFromResultSet(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche par nom", e);
        }
        return events;
    }

    @Override
    public List<EventSocial> lireParDate(LocalDate date) {
        List<EventSocial> events = new ArrayList<>();
        String sql = "SELECT * FROM event_social WHERE date = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, Date.valueOf(date));
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    events.add(extractEventFromResultSet(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche par date", e);
        }
        return events;
    }

    @Override
    public List<EventSocial> lireParLieu(String lieu) {
        List<EventSocial> events = new ArrayList<>();
        String sql = "SELECT * FROM event_social WHERE lieu LIKE ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "%" + lieu + "%");
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    events.add(extractEventFromResultSet(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche par lieu", e);
        }
        return events;
    }

    private EventSocial extractEventFromResultSet(ResultSet resultSet) throws SQLException {
        EventSocial event = new EventSocial();
        event.setId(resultSet.getInt("id"));
        event.setNom(resultSet.getString("nom"));
        event.setDate(resultSet.getDate("date").toLocalDate());
        event.setLieu(resultSet.getString("lieu"));
        event.setDescription(resultSet.getString("description"));
        event.setCapacite(resultSet.getInt("capacite"));
        return event;
    }
} 