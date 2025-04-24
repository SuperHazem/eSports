package dao;

import enums.Role;
import models.Utilisateur;
import utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UtilisateurDAO implements GenericDAO<Utilisateur, Integer> {

    private Connection connection;

    public UtilisateurDAO() throws SQLException {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public void ajouter(Utilisateur utilisateur) {
        String query = "INSERT INTO Utilisateur (email, motDePasseHash, role) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, utilisateur.getEmail());
            statement.setString(2, utilisateur.getMotDePasseHash());
            statement.setString(3, utilisateur.getRole().toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Utilisateur lire(Integer id) {
        String query = "SELECT * FROM Utilisateur WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return extractUtilisateurFromResultSet(resultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Utilisateur> lireTous() {
        String query = "SELECT * FROM Utilisateur";
        List<Utilisateur> utilisateurs = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                utilisateurs.add(extractUtilisateurFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return utilisateurs;
    }

    @Override
    public void modifier(Utilisateur utilisateur) {
        String query = "UPDATE Utilisateur SET email = ?, motDePasseHash = ?, role = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, utilisateur.getEmail());
            statement.setString(2, utilisateur.getMotDePasseHash());
            statement.setString(3, utilisateur.getRole().toString());
            statement.setInt(4, utilisateur.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void supprimer(Integer id) {
        String query = "DELETE FROM Utilisateur WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Helper method to map ResultSet to Utilisateur object
    private Utilisateur extractUtilisateurFromResultSet(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("id");
        String email = resultSet.getString("email");
        String motDePasseHash = resultSet.getString("motDePasseHash");
        Role role = Role.valueOf(resultSet.getString("role"));
        return new Utilisateur(id, email, motDePasseHash, role);
    }
}