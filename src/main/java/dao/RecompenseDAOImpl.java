package dao;

import models.Recompense;
import enums.TypeRecompense;
import utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RecompenseDAOImpl implements RecompenseDAO {

    private Connection connection;

    public RecompenseDAOImpl() throws SQLException {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public void ajouter(Recompense recompense) {
        String query = "INSERT INTO Recompense (type, valeur, utilisateur_id) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, recompense.getType().name()); // Convertit l'enum en String
            ps.setDouble(2, recompense.getValeur());
            ps.setInt(3, recompense.getUtilisateur().getId());

            ps.executeUpdate();

            // Récupérer l'ID généré
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    recompense.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Recompense lire(Integer id) {
        String query = "SELECT * FROM Recompense WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Recompense(
                            rs.getInt("id"),
                            TypeRecompense.valueOf(rs.getString("type")), // Convertit String en Enum
                            rs.getDouble("valeur"),
                            null // TODO: Charger l'utilisateur associé si nécessaire
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Recompense> lireTous() {
        List<Recompense> recompenses = new ArrayList<>();
        String query = "SELECT * FROM Recompense";
        try (PreparedStatement ps = connection.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                recompenses.add(new Recompense(
                        rs.getInt("id"),
                        TypeRecompense.valueOf(rs.getString("type")),
                        rs.getDouble("valeur"),
                        null // TODO: Charger l'utilisateur associé si nécessaire
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return recompenses;
    }

    @Override
    public void modifier(Recompense recompense) {
        String query = "UPDATE Recompense SET type = ?, valeur = ?, utilisateur_id = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, recompense.getType().name());
            ps.setDouble(2, recompense.getValeur());
            ps.setInt(3, recompense.getUtilisateur().getId());
            ps.setInt(4, recompense.getId());

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void supprimer(Integer id) {
        String query = "DELETE FROM Recompense WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Recompense> lireParType(TypeRecompense type) {
        List<Recompense> recompenses = new ArrayList<>();
        String query = "SELECT * FROM Recompense WHERE type = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, type.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    recompenses.add(new Recompense(
                            rs.getInt("id"),
                            TypeRecompense.valueOf(rs.getString("type")),
                            rs.getDouble("valeur"),
                            null // TODO: Charger l'utilisateur associé si nécessaire
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return recompenses;
    }
}