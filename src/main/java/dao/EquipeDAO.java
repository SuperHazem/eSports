package dao;

import models.Equipe;
import utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EquipeDAO implements GenericDAO<Equipe, Integer> {

    private Connection connection;

    public EquipeDAO() throws SQLException {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public void ajouter(Equipe equipe) {
        String query = "INSERT INTO Equipe (nom) VALUES (?)";
        try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, equipe.getNom());
            statement.executeUpdate();

            // Retrieve the auto-generated ID and set it in the Equipe object
            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                equipe.setId(generatedKeys.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Equipe lire(Integer id) {
        String query = "SELECT * FROM Equipe WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return extractEquipeFromResultSet(resultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Equipe> lireTous() {
        String query = "SELECT * FROM Equipe";
        List<Equipe> equipes = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                equipes.add(extractEquipeFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return equipes;
    }

    @Override
    public void modifier(Equipe equipe) {
        String query = "UPDATE Equipe SET nom = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, equipe.getNom());
            statement.setInt(2, equipe.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void supprimer(Integer id) {
        String query = "DELETE FROM Equipe WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Helper method to map ResultSet to Equipe object
    private Equipe extractEquipeFromResultSet(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("id");
        String nom = resultSet.getString("nom");

        Equipe equipe = new Equipe(id, nom);
        // Note: Associations like `joueurs` and `coach` are not handled here.
        // You can fetch them separately if needed.
        return equipe;
    }
}