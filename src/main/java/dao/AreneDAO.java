package dao;

import models.Arene;
import utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AreneDAO implements GenericDAO<Arene, Integer> {
    private Connection connection;

    public AreneDAO() throws SQLException {
        connection = DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public void ajouter(Arene arene) {
        String query = "INSERT INTO arene (name, location, capacity) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, arene.getName());
            statement.setString(2, arene.getLocation());
            statement.setInt(3, arene.getCapacity());
            statement.executeUpdate();
            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                arene.setAreneId(generatedKeys.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to add arena: " + e.getMessage());
        }
    }

    @Override
    public Arene lire(Integer id) {
        String query = "SELECT * FROM arene WHERE areneid = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                Arene arene = new Arene();
                arene.setAreneId(resultSet.getInt("areneid"));
                arene.setName(resultSet.getString("name"));
                arene.setLocation(resultSet.getString("location"));
                arene.setCapacity(resultSet.getInt("capacity"));
                return arene;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Arene> lireTous() {
        List<Arene> arenes = new ArrayList<>();
        String query = "SELECT * FROM arene";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                Arene arene = new Arene();
                arene.setAreneId(resultSet.getInt("areneid"));
                arene.setName(resultSet.getString("name"));
                arene.setLocation(resultSet.getString("location"));
                arene.setCapacity(resultSet.getInt("capacity"));
                arenes.add(arene);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return arenes;
    }

    @Override
    public void modifier(Arene arene) {
        String query = "UPDATE arene SET name = ?, location = ?, capacity = ? WHERE areneid = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, arene.getName());
            statement.setString(2, arene.getLocation());
            statement.setInt(3, arene.getCapacity());
            statement.setInt(4, arene.getAreneId());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to update arena: " + e.getMessage());
        }
    }

    @Override
    public void supprimer(Integer id) {
        String query = "DELETE FROM arene WHERE areneid = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to delete arena: " + e.getMessage());
        }
    }



    public boolean hasAssociatedMatches(Integer areneId) {
        String query = "SELECT COUNT(*) FROM match WHERE idArene = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, areneId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}