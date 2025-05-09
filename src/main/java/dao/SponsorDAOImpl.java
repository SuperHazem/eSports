package dao;

import models.Sponsor;
import utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SponsorDAOImpl implements SponsorDAO {

    private Connection connection;

    public SponsorDAOImpl() throws SQLException {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public void ajouter(Sponsor sponsor) {
        String query = "INSERT INTO sponsor (fname, lname, address, email, phone, montant) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, sponsor.getFname());
            statement.setString(2, sponsor.getLname());
            statement.setString(3, sponsor.getAddress());
            statement.setString(4, sponsor.getEmail());
            statement.setString(5, sponsor.getPhone());
            statement.setDouble(6, sponsor.getMontant());
            statement.executeUpdate();

            // Récupérer l'ID auto-généré
            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                sponsor.setId(generatedKeys.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Sponsor lire(Integer id) {
        String query = "SELECT * FROM sponsor WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return extractSponsorFromResultSet(resultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Sponsor> lireTous() {
        List<Sponsor> sponsors = new ArrayList<>();
        String query = "SELECT * FROM sponsor";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                sponsors.add(extractSponsorFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sponsors;
    }

    @Override
    public void modifier(Sponsor sponsor) {
        String query = "UPDATE sponsor SET fname = ?, lname = ?, address = ?, email = ?, phone = ?, montant = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, sponsor.getFname());
            statement.setString(2, sponsor.getLname());
            statement.setString(3, sponsor.getAddress());
            statement.setString(4, sponsor.getEmail());
            statement.setString(5, sponsor.getPhone());
            statement.setDouble(6, sponsor.getMontant());
            statement.setInt(7, sponsor.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void supprimer(Integer id) {
        String query = "DELETE FROM sponsor WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Sponsor> lireParMontant(double montantMin) {
        List<Sponsor> sponsors = new ArrayList<>();
        String query = "SELECT * FROM sponsor WHERE montant >= ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setDouble(1, montantMin);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                sponsors.add(extractSponsorFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sponsors;
    }

    @Override
    public List<Sponsor> lireParNom(String nom) {
        List<Sponsor> sponsors = new ArrayList<>();
        String query = "SELECT * FROM sponsor WHERE fname LIKE ? OR lname LIKE ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            String searchPattern = "%" + nom + "%";
            statement.setString(1, searchPattern);
            statement.setString(2, searchPattern);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                sponsors.add(extractSponsorFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sponsors;
    }

    private Sponsor extractSponsorFromResultSet(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("id");
        String fname = resultSet.getString("fname");
        String lname = resultSet.getString("lname");
        String address = resultSet.getString("address");
        String email = resultSet.getString("email");
        String phone = resultSet.getString("phone");
        double montant = resultSet.getDouble("montant");

        return new Sponsor(id, fname, lname, address, email, phone, montant);
    }
} 