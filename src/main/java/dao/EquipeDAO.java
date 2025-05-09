package dao;

import models.Equipe;
import utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EquipeDAO {

    public EquipeDAO() throws SQLException {
        // No need to store connection as a field anymore
    }

    // Méthode utilitaire pour vérifier si un utilisateur a un rôle spécifique
    private boolean verifierRole(int utilisateurId, String roleAttendu) throws SQLException {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DatabaseConnection.getInstance().getConnection();
            String query = "SELECT role FROM Utilisateur WHERE id = ?";
            statement = connection.prepareStatement(query);
            statement.setInt(1, utilisateurId);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String role = resultSet.getString("role");
                return role.equalsIgnoreCase(roleAttendu);
            }
        } finally {
            closeResources(resultSet, statement, null); // Don't close the connection
        }
        return false; // L'utilisateur n'existe pas ou n'a pas le rôle attendu
    }

    // Ajouter une équipe avec validation des rôles et calcul du win_rate
    public void ajouter(Equipe equipe) throws SQLException {
        // Validation du coach
        if (!verifierRole(equipe.getCoachId(), "COACH")) {
            throw new IllegalArgumentException("L'ID fourni ne correspond pas à un coach valide.");
        }

        // Validation des joueurs
        for (int joueurId : equipe.getListeJoueurs()) {
            if (!verifierRole(joueurId, "JOUEUR")) {
                throw new IllegalArgumentException("L'ID " + joueurId + " ne correspond pas à un joueur valide.");
            }
        }

        // Calcul du win_rate de l'équipe
        double winRateEquipe = calculerWinRateEquipe(equipe.getListeJoueurs());
        equipe.setWinRate(winRateEquipe);

        // Insertion de l'équipe
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet generatedKeys = null;

        try {
            connection = DatabaseConnection.getInstance().getConnection();
            String query = "INSERT INTO Equipe (nom, coach_id, liste_joueurs, win_rate) VALUES (?, ?, ?, ?)";
            statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

            statement.setString(1, equipe.getNom());
            statement.setInt(2, equipe.getCoachId());
            statement.setString(3, convertListToJson(equipe.getListeJoueurs())); // Convertir la liste en JSON
            statement.setDouble(4, equipe.getWinRate());
            statement.executeUpdate();

            // Récupérer l'ID auto-généré
            generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                equipe.setId(generatedKeys.getInt(1));
            }
        } finally {
            closeResources(generatedKeys, statement, null); // Don't close the connection
        }
    }

    // Lire une équipe par son ID
    public Equipe lire(int id) throws SQLException {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DatabaseConnection.getInstance().getConnection();
            String query = "SELECT * FROM Equipe WHERE id = ?";
            statement = connection.prepareStatement(query);
            statement.setInt(1, id);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return extractEquipeFromResultSet(resultSet);
            }
        } finally {
            closeResources(resultSet, statement, null); // Don't close the connection
        }
        return null;
    }

    // Modifier une équipe avec recalcul du win_rate
    public void modifier(Equipe equipe) throws SQLException {
        // Validation du coach
        if (!verifierRole(equipe.getCoachId(), "COACH")) {
            throw new IllegalArgumentException("L'ID fourni ne correspond pas à un coach valide.");
        }

        // Validation des joueurs
        for (int joueurId : equipe.getListeJoueurs()) {
            if (!verifierRole(joueurId, "JOUEUR")) {
                throw new IllegalArgumentException("L'ID " + joueurId + " ne correspond pas à un joueur valide.");
            }
        }

        // Recalcul du win_rate de l'équipe
        double winRateEquipe = calculerWinRateEquipe(equipe.getListeJoueurs());
        equipe.setWinRate(winRateEquipe);

        // Mise à jour de l'équipe
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = DatabaseConnection.getInstance().getConnection();
            String query = "UPDATE Equipe SET nom = ?, coach_id = ?, liste_joueurs = ?, win_rate = ? WHERE id = ?";
            statement = connection.prepareStatement(query);

            statement.setString(1, equipe.getNom());
            statement.setInt(2, equipe.getCoachId());
            statement.setString(3, convertListToJson(equipe.getListeJoueurs())); // Convertir la liste en JSON
            statement.setDouble(4, equipe.getWinRate());
            statement.setInt(5, equipe.getId());
            statement.executeUpdate();
        } finally {
            closeResources(null, statement, null); // Don't close the connection
        }
    }

    // Supprimer une équipe
    public void supprimer(int id) throws SQLException {
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = DatabaseConnection.getInstance().getConnection();
            String query = "DELETE FROM Equipe WHERE id = ?";
            statement = connection.prepareStatement(query);
            statement.setInt(1, id);
            statement.executeUpdate();
        } finally {
            closeResources(null, statement, null); // Don't close the connection
        }
    }

    // Calculer le win_rate d'une équipe à partir des win_rate des joueurs
    public double calculerWinRateEquipe(List<Integer> listeJoueurs) throws SQLException {
        double totalWinRate = 0.0;
        int nombreJoueurs = 0;
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DatabaseConnection.getInstance().getConnection();
            String query = "SELECT win_rate FROM Joueur WHERE utilisateur_id = ?";
            statement = connection.prepareStatement(query);

            for (int joueurId : listeJoueurs) {
                statement.setInt(1, joueurId);
                resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    totalWinRate += resultSet.getDouble("win_rate");
                    nombreJoueurs++;
                }
                resultSet.close(); // Close the result set for each iteration
            }
        } finally {
            closeResources(resultSet, statement, null); // Don't close the connection
        }

        // Calculer la moyenne
        return nombreJoueurs == 0 ? 0.0 : totalWinRate / nombreJoueurs;
    }

    // Réinitialiser les stats d'une équipe (en cas de triche)
    public void reinitialiserStatsEquipe(int equipeId) throws SQLException {
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = DatabaseConnection.getInstance().getConnection();
            String query = "UPDATE Equipe SET win_rate = 0.0 WHERE id = ?";
            statement = connection.prepareStatement(query);
            statement.setInt(1, equipeId);
            statement.executeUpdate();
        } finally {
            closeResources(null, statement, null); // Don't close the connection
        }
    }

    // Méthode utilitaire pour mapper un ResultSet à un objet Equipe
    private Equipe extractEquipeFromResultSet(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("id");
        String nom = resultSet.getString("nom");
        int coachId = resultSet.getInt("coach_id");
        double winRate = resultSet.getDouble("win_rate");

        // Get liste_joueurs as JSON string and convert to List
        String listeJoueursJson = resultSet.getString("liste_joueurs");
        List<Integer> listeJoueurs = convertJsonToList(listeJoueursJson);

        return new Equipe(id, nom, coachId, listeJoueurs, winRate);
    }

    // Méthode pour convertir une liste en JSON (String)
    private String convertListToJson(List<Integer> liste) {
        if (liste == null || liste.isEmpty()) {
            return "[]";
        }
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < liste.size(); i++) {
            json.append(liste.get(i));
            if (i < liste.size() - 1) {
                json.append(",");
            }
        }
        json.append("]");
        return json.toString();
    }

    // Méthode pour convertir un JSON (String) en liste
    private List<Integer> convertJsonToList(String json) {
        List<Integer> liste = new ArrayList<>();
        if (json == null || json.equals("[]")) {
            return liste;
        }
        json = json.replace("[", "").replace("]", ""); // Supprimer les crochets
        String[] elements = json.split(",");
        for (String element : elements) {
            if (!element.trim().isEmpty()) {
                liste.add(Integer.parseInt(element.trim()));
            }
        }
        return liste;
    }

    /**
     * Lire toutes les équipes
     * @return Liste de toutes les équipes
     */
    public List<Equipe> lireTous() {
        List<Equipe> equipes = new ArrayList<>();
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DatabaseConnection.getInstance().getConnection();
            String query = "SELECT * FROM Equipe";
            statement = connection.prepareStatement(query);
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                equipes.add(extractEquipeFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(resultSet, statement, null); // Don't close the connection
        }
        return equipes;
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