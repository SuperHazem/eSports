package dao;

import models.Tournoi;
import utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TournoiDAO implements GenericDAO<Tournoi, Integer> {
    private Connection connection;

    public TournoiDAO() throws SQLException {
        connection = DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public void ajouter(Tournoi tournoi) {
        String query = "INSERT INTO tournoi (nom, date_debut, date_fin, date_match, equipes, matches, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, tournoi.getNom());
            statement.setDate(2, tournoi.getDateDebut());
            statement.setDate(3, tournoi.getDateFin());
            statement.setDate(4, tournoi.getDateMatch());
            statement.setString(5, tournoi.getEquipes());
            statement.setString(6, tournoi.getMatches());
            statement.setString(7, tournoi.getStatus());
            statement.executeUpdate();
            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                tournoi.setId(generatedKeys.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Tournoi lire(Integer id) {
        String query = "SELECT * FROM tournoi WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                Tournoi tournoi = new Tournoi();
                tournoi.setId(resultSet.getInt("id"));
                tournoi.setNom(resultSet.getString("nom"));
                tournoi.setDateDebut(resultSet.getDate("date_debut"));
                tournoi.setDateFin(resultSet.getDate("date_fin"));
                tournoi.setDateMatch(resultSet.getDate("date_match"));
                tournoi.setEquipes(resultSet.getString("equipes"));
                tournoi.setMatches(resultSet.getString("matches"));
                tournoi.setStatus(resultSet.getString("status"));
                return tournoi;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Tournoi> lireTous() {
        List<Tournoi> tournois = new ArrayList<>();
        String query = "SELECT * FROM tournoi";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                Tournoi tournoi = new Tournoi();
                tournoi.setId(resultSet.getInt("id"));
                tournoi.setNom(resultSet.getString("nom"));
                tournoi.setDateDebut(resultSet.getDate("date_debut"));
                tournoi.setDateFin(resultSet.getDate("date_fin"));
                tournoi.setDateMatch(resultSet.getDate("date_match"));
                tournoi.setEquipes(resultSet.getString("equipes"));
                tournoi.setMatches(resultSet.getString("matches"));
                tournoi.setStatus(resultSet.getString("status"));
                tournois.add(tournoi);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tournois;
    }

    @Override
    public void modifier(Tournoi tournoi) {
        String query = "UPDATE tournoi SET nom = ?, date_debut = ?, date_fin = ?, date_match = ?, equipes = ?, matches = ?, status = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, tournoi.getNom());
            statement.setDate(2, tournoi.getDateDebut());
            statement.setDate(3, tournoi.getDateFin());
            statement.setDate(4, tournoi.getDateMatch());
            statement.setString(5, tournoi.getEquipes());
            statement.setString(6, tournoi.getMatches());
            statement.setString(7, tournoi.getStatus());
            statement.setInt(8, tournoi.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void supprimer(Integer id) {
        String query = "DELETE FROM tournoi WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}