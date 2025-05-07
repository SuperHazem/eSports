package dao;

import enums.Role;
import models.Coach;
import models.Equipe;
import models.Joueur;
import utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EquipeDAO {
    public EquipeDAO() {
        // Pas besoin de stocker la connexion en tant que champ
    }

    // Méthode utilitaire pour vérifier si un utilisateur a un rôle spécifique
    private boolean verifierRole(int utilisateurId, String roleAttendu) throws SQLException {
        String query = "SELECT role FROM Utilisateur WHERE id = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, utilisateurId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String role = resultSet.getString("role");
                    return role.equalsIgnoreCase(roleAttendu);
                }
            }
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
        String query = "INSERT INTO Equipe (nom, coach_id, liste_joueurs, win_rate, statut) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, equipe.getNom());
            statement.setInt(2, equipe.getCoachId());
            statement.setString(3, convertListToJson(equipe.getListeJoueurs())); // Convertir la liste en JSON
            statement.setDouble(4, equipe.getWinRate());
            statement.setString(5, equipe.getStatus());
            statement.executeUpdate();

            // Récupérer l'ID auto-généré
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    equipe.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    // Lire une équipe par son ID
    public Equipe lire(int id) throws SQLException {
        String query = "SELECT * FROM Equipe WHERE id = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return extractEquipeFromResultSet(resultSet);
                }
            }
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
        String query = "UPDATE Equipe SET nom = ?, coach_id = ?, liste_joueurs = ?, win_rate = ?, statut = ? WHERE id = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, equipe.getNom());
            statement.setInt(2, equipe.getCoachId());
            statement.setString(3, convertListToJson(equipe.getListeJoueurs())); // Convertir la liste en JSON
            statement.setDouble(4, equipe.getWinRate());
            statement.setString(5, equipe.getStatus());
            statement.setInt(6, equipe.getId());
            statement.executeUpdate();
        }
    }

    // Supprimer une équipe
    public void supprimer(int id) throws SQLException {
        String query = "DELETE FROM Equipe WHERE id = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    // Calculer le win_rate d'une équipe à partir des win_rate des joueurs
    public double calculerWinRateEquipe(List<Integer> listeJoueurs) throws SQLException {
        double totalWinRate = 0.0;
        int nombreJoueurs = 0;
        String query = "SELECT win_rate FROM Joueur WHERE utilisateur_id = ?";

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            for (int joueurId : listeJoueurs) {
                statement.setInt(1, joueurId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        totalWinRate += resultSet.getDouble("win_rate");
                        nombreJoueurs++;
                    }
                }
            }
        }

        // Calculer la moyenne
        return nombreJoueurs == 0 ? 0.0 : totalWinRate / nombreJoueurs;
    }

    // Réinitialiser les stats d'une équipe (en cas de triche)
    public void reinitialiserStatsEquipe(int equipeId) throws SQLException {
        String query = "UPDATE Equipe SET win_rate = 0.0 WHERE id = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, equipeId);
            statement.executeUpdate();
        }
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

    // Méthode utilitaire pour mapper un ResultSet à un objet Equipe
    private Equipe extractEquipeFromResultSet(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("id");
        String nom = resultSet.getString("nom");
        int coachId = resultSet.getInt("coach_id");
        double winRate = resultSet.getDouble("win_rate");
        String statut = resultSet.getString("statut");
        // Get liste_joueurs as JSON string and convert to List
        String listeJoueursJson = resultSet.getString("liste_joueurs");
        List<Integer> listeJoueurs = convertJsonToList(listeJoueursJson);
        return new Equipe(id, nom, coachId, listeJoueurs, winRate, statut);
    }
// Dans EquipeDAO.java

    // Méthode surchargée pour recherche par pseudo uniquement
    public List<Joueur> chercherJoueurs(int i, String pseudo) throws SQLException {
        return chercherJoueurs(-1, pseudo);
    }
    /**
     * Lire toutes les équipes
     * @return Liste de toutes les équipes
     */
    public List<Equipe> lireTous() {
        List<Equipe> equipes = new ArrayList<>();
        String query = "SELECT * FROM Equipe";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                equipes.add(extractEquipeFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return equipes;
    }

    // Méthode pour rechercher des coaches par nom et prénom (utilise le bon constructeur de Coach)
    public List<Coach> chercherCoaches(String nom, String prenom) throws SQLException {
        List<Coach> resultats = new ArrayList<>();

        StringBuilder query = new StringBuilder("""
        SELECT u.id, u.email, u.mot_de_passe_hash, u.role,
               u.nom, u.prenom, c.strategie
        FROM Utilisateur u
        JOIN Coach c ON u.id = c.utilisateur_id
        WHERE u.role = 'COACH'
    """);

        if (nom != null && !nom.isEmpty())
            query.append(" AND u.nom ILIKE ?");

        if (prenom != null && !prenom.isEmpty())
            query.append(" AND u.prenom ILIKE ?");

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query.toString())) {

            int paramIndex = 1;
            if (nom != null && !nom.isEmpty())
                stmt.setString(paramIndex++, "%" + nom + "%");

            if (prenom != null && !prenom.isEmpty())
                stmt.setString(paramIndex++, "%" + prenom + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    // Utilisation du constructeur principal de Coach
                    Coach coach = new Coach(
                            Role.valueOf(rs.getString("role")), // role
                            rs.getString("mot_de_passe_hash"),  // motDePasseHash
                            rs.getString("email"),              // email
                            rs.getInt("id"),                    // id
                            rs.getString("nom"),                // nom
                            rs.getString("prenom"),             // prenom
                            rs.getString("strategie")           // strategie
                    );
                    resultats.add(coach);
                }
            }
        }
        return resultats;
    }

    // Méthode pour rechercher des joueurs par pseudo_jeu (utilise le bon constructeur de Joueur)
    public List<Joueur> chercherJoueurs(String pseudo) throws SQLException {
        List<Joueur> resultats = new ArrayList<>();

        if (pseudo == null || pseudo.isEmpty())
            return resultats;

        String query = """
        SELECT u.id, u.email, u.mot_de_passe_hash, u.role,
               u.nom, u.prenom, j.pseudo_jeu, j.rank, j.win_rate
        FROM Utilisateur u
        JOIN Joueur j ON u.id = j.utilisateur_id
        WHERE u.role = 'JOUEUR'
          AND j.pseudo_jeu ILIKE ?
    """;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, "%" + pseudo + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    // Utilisation du constructeur principal de Joueur
                    Joueur joueur = new Joueur(
                            Role.valueOf(rs.getString("role")),      // role
                            rs.getString("mot_de_passe_hash"),       // motDePasseHash
                            rs.getString("email"),                   // email
                            rs.getInt("id"),                         // id
                            rs.getString("nom"),                     // nom
                            rs.getString("prenom"),                  // prenom
                            rs.getString("pseudo_jeu"),              // pseudoJeu
                            rs.getDouble("win_rate"),                // winRate
                            rs.getString("rank")                     // rank
                    );
                    resultats.add(joueur);
                }
            }
        }
        return resultats;
    }


}