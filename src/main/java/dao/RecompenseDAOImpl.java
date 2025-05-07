package dao;

import enums.TypeRecompense;
import models.Equipe;
import models.Recompense;
import utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RecompenseDAOImpl implements RecompenseDAO {

    private EquipeDAO equipeDAO;

    public RecompenseDAOImpl() {
        this.equipeDAO = new EquipeDAO();
    }

    @Override
    public void ajouter(Recompense recompense) {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            connection = DatabaseConnection.getInstance().getConnection();
            String query = "INSERT INTO Recompense (type, valeur, equipe_id, description, date_attribution) VALUES (?, ?, ?, ?, ?)";
            stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

            stmt.setString(1, recompense.getType().toString());
            stmt.setDouble(2, recompense.getValeur());
            stmt.setInt(3, recompense.getEquipe().getId());
            stmt.setString(4, recompense.getDescription());

            // Convert Java util Date to SQL Date
            java.sql.Date sqlDate = new java.sql.Date(recompense.getDateAttribution().getTime());
            stmt.setDate(5, sqlDate);

            int affectedRows = stmt.executeUpdate();
            System.out.println("Rows affected by insert: " + affectedRows);

            // Get generated ID
            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                recompense.setId(rs.getInt(1));
                System.out.println("Generated ID for new reward: " + recompense.getId());
            }
        } catch (SQLException e) {
            System.err.println("Error adding reward: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(rs, stmt, null); // Don't close the connection
        }
    }

    @Override
    public Recompense lire(int id) {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Recompense recompense = null;

        try {
            connection = DatabaseConnection.getInstance().getConnection();
            String query = "SELECT r.*, e.nom as equipe_nom FROM Recompense r " +
                    "LEFT JOIN Equipe e ON r.equipe_id = e.id " +
                    "WHERE r.id = ?";
            stmt = connection.prepareStatement(query);

            stmt.setInt(1, id);
            rs = stmt.executeQuery();

            if (rs.next()) {
                recompense = extractRecompenseFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error reading reward with ID " + id + ": " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(rs, stmt, null); // Don't close the connection
        }
        return recompense;
    }

    @Override
    public List<Recompense> lireTous() {
        List<Recompense> recompenses = new ArrayList<>();
        Connection connection = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            connection = DatabaseConnection.getInstance().getConnection();
            // Join with Equipe table to get team name directly
            String query = "SELECT r.*, e.nom as equipe_nom, e.coach_id, e.win_rate FROM Recompense r " +
                    "LEFT JOIN Equipe e ON r.equipe_id = e.id";
            stmt = connection.createStatement();
            rs = stmt.executeQuery(query);

            System.out.println("Executing query: " + query);

            while (rs.next()) {
                Recompense recompense = extractRecompenseFromResultSet(rs);
                if (recompense != null) {
                    recompenses.add(recompense);
                    System.out.println("Added reward: " + recompense);
                }
            }

            System.out.println("Loaded " + recompenses.size() + " rewards from database.");
        } catch (SQLException e) {
            System.err.println("Error reading all rewards: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(rs, stmt, null); // Don't close the connection
        }
        return recompenses;
    }

    @Override
    public List<Recompense> lireParEquipe(int equipeId) {
        List<Recompense> recompenses = new ArrayList<>();
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            connection = DatabaseConnection.getInstance().getConnection();
            String query = "SELECT r.*, e.nom as equipe_nom, e.coach_id, e.win_rate FROM Recompense r " +
                    "LEFT JOIN Equipe e ON r.equipe_id = e.id " +
                    "WHERE r.equipe_id = ?";
            stmt = connection.prepareStatement(query);

            stmt.setInt(1, equipeId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                Recompense recompense = extractRecompenseFromResultSet(rs);
                if (recompense != null) {
                    recompenses.add(recompense);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error reading rewards for team ID " + equipeId + ": " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(rs, stmt, null); // Don't close the connection
        }
        return recompenses;
    }

    @Override
    public List<Recompense> lireParType(TypeRecompense type) {
        List<Recompense> recompenses = new ArrayList<>();
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            connection = DatabaseConnection.getInstance().getConnection();
            String query = "SELECT r.*, e.nom as equipe_nom, e.coach_id, e.win_rate FROM Recompense r " +
                    "LEFT JOIN Equipe e ON r.equipe_id = e.id " +
                    "WHERE r.type = ?";
            stmt = connection.prepareStatement(query);

            stmt.setString(1, type.toString());
            rs = stmt.executeQuery();

            while (rs.next()) {
                Recompense recompense = extractRecompenseFromResultSet(rs);
                if (recompense != null) {
                    recompenses.add(recompense);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error reading rewards of type " + type + ": " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(rs, stmt, null); // Don't close the connection
        }
        return recompenses;
    }

    @Override
    public void modifier(Recompense recompense) {
        Connection connection = null;
        PreparedStatement stmt = null;

        try {
            connection = DatabaseConnection.getInstance().getConnection();
            String query = "UPDATE Recompense SET type = ?, valeur = ?, equipe_id = ?, description = ?, date_attribution = ? WHERE id = ?";
            stmt = connection.prepareStatement(query);

            stmt.setString(1, recompense.getType().toString());
            stmt.setDouble(2, recompense.getValeur());
            stmt.setInt(3, recompense.getEquipe().getId());
            stmt.setString(4, recompense.getDescription());

            // Convert Java util Date to SQL Date
            java.sql.Date sqlDate = new java.sql.Date(recompense.getDateAttribution().getTime());
            stmt.setDate(5, sqlDate);

            stmt.setInt(6, recompense.getId());

            int affectedRows = stmt.executeUpdate();
            System.out.println("Rows affected by update: " + affectedRows);
        } catch (SQLException e) {
            System.err.println("Error updating reward with ID " + recompense.getId() + ": " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(null, stmt, null); // Don't close the connection
        }
    }

    @Override
    public void supprimer(int id) {
        Connection connection = null;
        PreparedStatement stmt = null;

        try {
            connection = DatabaseConnection.getInstance().getConnection();
            String query = "DELETE FROM Recompense WHERE id = ?";
            stmt = connection.prepareStatement(query);

            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();
            System.out.println("Rows affected by delete: " + affectedRows);
        } catch (SQLException e) {
            System.err.println("Error deleting reward with ID " + id + ": " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(null, stmt, null); // Don't close the connection
        }
    }

    private Recompense extractRecompenseFromResultSet(ResultSet rs) throws SQLException {
        try {
            int id = rs.getInt("id");
            String typeStr = rs.getString("type");
            TypeRecompense type = TypeRecompense.valueOf(typeStr);
            double valeur = rs.getDouble("valeur");
            int equipeId = rs.getInt("equipe_id");
            String description = rs.getString("description");
            Date dateAttribution = rs.getDate("date_attribution");

            System.out.println("Extracting reward ID " + id + " with equipe_id " + equipeId);

            // Create a basic Equipe object with data from the join
            Equipe equipe;
            try {
                String equipeName = rs.getString("equipe_nom");
                // If we have the team name from the join, use it
                if (equipeName != null) {
                    int coachId = rs.getInt("coach_id");
                    double winRate = rs.getDouble("win_rate");
                    equipe = new Equipe(equipeId, equipeName, coachId, winRate, "Active");
                    System.out.println("Created equipe from join: " + equipeName + " (ID: " + equipeId + ")");
                } else {
                    // If not, try to load from EquipeDAO
                    equipe = equipeDAO.lire(equipeId);
                    if (equipe == null) {
                        // Create a placeholder if all else fails
                        equipe = new Equipe(equipeId, "Équipe " + equipeId, 0, 0.0);
                        System.out.println("Created placeholder equipe for ID: " + equipeId);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error getting team data for reward " + id + ": " + e.getMessage());
                // Create a placeholder Equipe to avoid null pointer exceptions
                equipe = new Equipe(equipeId, "Équipe inconnue", 0, 0.0);
            }

            return new Recompense(id, type, valeur, equipe, description, dateAttribution);
        } catch (Exception e) {
            System.err.println("Error extracting reward from ResultSet: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Helper method to close resources
    private void closeResources(ResultSet rs, Statement stmt, Connection conn) {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            System.err.println("Error closing resources: " + e.getMessage());
            e.printStackTrace();
        }
    }
}