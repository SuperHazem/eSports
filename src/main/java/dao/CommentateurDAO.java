package dao;

import models.Commentateur;
import utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentateurDAO implements GenericDAO<Commentateur, Integer> {

    private Connection connection;

    public CommentateurDAO() throws SQLException {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public void ajouter(Commentateur commentateur) {
        String query = "INSERT INTO Commentateur (nom, expertise, langue) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, commentateur.getNom());
            statement.setString(2, commentateur.getExpertise());
            statement.setString(3, commentateur.getLangue());
            statement.executeUpdate();

            // Récupérer l'ID auto-généré
            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                commentateur.setId(generatedKeys.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Commentateur lire(Integer id) {
        String query = "SELECT * FROM Commentateur WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return extractCommentateurFromResultSet(resultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Commentateur> lireTous() {
        String query = "SELECT * FROM Commentateur";
        List<Commentateur> commentateurs = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                commentateurs.add(extractCommentateurFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return commentateurs;
    }

    @Override
    public void modifier(Commentateur commentateur) {
        String query = "UPDATE Commentateur SET nom = ?, expertise = ?, langue = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, commentateur.getNom());
            statement.setString(2, commentateur.getExpertise());
            statement.setString(3, commentateur.getLangue());
            statement.setInt(4, commentateur.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void supprimer(Integer id) {
        String query = "DELETE FROM Commentateur WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Méthode utilitaire pour mapper un ResultSet à un objet Commentateur
    private Commentateur extractCommentateurFromResultSet(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("id");
        String nom = resultSet.getString("nom");
        String expertise = resultSet.getString("expertise");
        String langue = resultSet.getString("langue");
        return new Commentateur(id, nom, expertise, langue);
    }
}