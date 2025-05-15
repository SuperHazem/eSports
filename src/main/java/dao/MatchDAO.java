package dao;

import enums.StatutMatch;
import models.Match;
import utils.DatabaseConnection;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class MatchDAO implements GenericDAO<Match, Integer> {
    private Connection connection;

    public MatchDAO() throws SQLException {
        connection = DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public void ajouter(Match match) {
        String query = "INSERT INTO matchtable (id_equipe1, id_equipe2, id_tournoi, id_arene, score_equipe1, score_equipe2, vainqueur, duree, nom_jeu, date_match, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, match.getIdEquipe1());
            statement.setInt(2, match.getIdEquipe2());
            statement.setInt(3, match.getIdTournoi());
            statement.setInt(4, match.getIdArene());
            statement.setInt(5, match.getScoreEquipe1());
            statement.setInt(6, match.getScoreEquipe2());
            if (match.getVainqueur() != null) {
                statement.setInt(7, match.getVainqueur());
            } else {
                statement.setNull(7, Types.INTEGER);
            }
            if (match.getDuree() != null) {
                statement.setInt(8, match.getDuree());
            } else {
                statement.setNull(8, Types.INTEGER);
            }
            statement.setString(9, match.getNomJeu());
            // In ajouter method
            statement.setTimestamp(10, match.getDateMatch());

            statement.setString(11, match.getStatutMatch().toString());

            statement.executeUpdate();

            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                match.setMatchId(generatedKeys.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Match lire(Integer id) {
        String query = "SELECT m.*, e1.nom AS equipe1_nom, e2.nom AS equipe2_nom, t.nom AS tournoi_nom, a.name AS arene_nom " +
                "FROM matchtable m " +
                "LEFT JOIN equipe e1 ON m.id_equipe1 = e1.id " +
                "LEFT JOIN equipe e2 ON m.id_equipe2 = e2.id " +
                "LEFT JOIN tournoi t ON m.id_tournoi = t.id " +
                "LEFT JOIN arene a ON m.id_arene = a.areneid WHERE m.matchId = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return extractMatchFromResultSet(resultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public List<Match> lireParTournoi(int tournoiId) {
        List<Match> matches = new ArrayList<>();
        String query = "SELECT m.*, e1.nom AS equipe1_nom, e2.nom AS equipe2_nom, t.nom AS tournoi_nom, a.name AS arene_nom " +
                "FROM matchtable m " +
                "LEFT JOIN equipe e1 ON m.id_equipe1 = e1.id " +
                "LEFT JOIN equipe e2 ON m.id_equipe2 = e2.id " +
                "LEFT JOIN tournoi t ON m.id_tournoi = t.id " +
                "LEFT JOIN arene a ON m.id_arene = a.areneid WHERE m.id_tournoi = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, tournoiId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                matches.add(extractMatchFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return matches;
    }

    public List<Match> lireTous() {
        List<Match> matches = new ArrayList<>();
        String query = "SELECT m.*, e1.nom AS equipe1_nom, e2.nom AS equipe2_nom, t.nom AS tournoi_nom, a.name AS arene_nom " +
                "FROM matchtable m " +
                "LEFT JOIN equipe e1 ON m.id_equipe1 = e1.id " +
                "LEFT JOIN equipe e2 ON m.id_equipe2 = e2.id " +
                "LEFT JOIN tournoi t ON m.id_tournoi = t.id " +
                "LEFT JOIN arene a ON m.id_arene = a.areneid";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                matches.add(extractMatchFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return matches;
    }

    @Override
    public void modifier(Match match) {
        String query = "UPDATE matchtable SET id_equipe1 = ?, id_equipe2 = ?, id_tournoi = ?, id_arene = ?, score_equipe1 = ?, score_equipe2 = ?, vainqueur = ?, duree = ?, nom_jeu = ?, date_match = ?, status = ? WHERE matchId = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, match.getIdEquipe1());
            statement.setInt(2, match.getIdEquipe2());
            statement.setInt(3, match.getIdTournoi());
            statement.setInt(4, match.getIdArene());
            statement.setInt(5, match.getScoreEquipe1());
            statement.setInt(6, match.getScoreEquipe2());
            if (match.getVainqueur() != null) {
                statement.setInt(7, match.getVainqueur());
            } else {
                statement.setNull(7, Types.INTEGER);
            }
            if (match.getDuree() != null) {
                statement.setInt(8, match.getDuree());
            } else {
                statement.setNull(8, Types.INTEGER);
            }
            statement.setString(9, match.getNomJeu());
            // In modifier method
            statement.setTimestamp(10, match.getDateMatch());

            statement.setString(11, match.getStatutMatch().toString());
            statement.setInt(12, match.getMatchId());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void supprimer(Integer id) {
        String query = "DELETE FROM matchtable WHERE matchId = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    public boolean isArenaAvailable(int areneId) {
        String query = "SELECT COUNT(*) FROM matchtable WHERE id_arene = ? AND status = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, areneId);
            statement.setString(2, StatutMatch.EN_COURS.toString());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) == 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }




    private Match extractMatchFromResultSet(ResultSet resultSet) throws SQLException {
        Match match = new Match();
        match.setMatchId(resultSet.getInt("matchId"));
        match.setIdEquipe1(resultSet.getInt("id_equipe1"));
        match.setIdEquipe2(resultSet.getInt("id_equipe2"));
        match.setIdTournoi(resultSet.getInt("id_tournoi"));
        match.setIdArene(resultSet.getInt("id_arene"));
        match.setScoreEquipe1(resultSet.getInt("score_equipe1"));
        match.setScoreEquipe2(resultSet.getInt("score_equipe2"));
        if (resultSet.getObject("vainqueur") != null) {
            match.setVainqueur(resultSet.getInt("vainqueur"));
        }
        if (resultSet.getObject("duree") != null) {
            match.setDuree(resultSet.getInt("duree"));
        }
        
        // Set game name with fallback
        String nomJeu = resultSet.getString("nom_jeu");
        match.setNomJeu(nomJeu != null && !nomJeu.isEmpty() ? nomJeu : "Unknown Game");
        
        // Set match date
        match.setDateMatch(resultSet.getTimestamp("date_match"));
        
        // Set match status with fallback to EN_ATTENTE if null
        String status = resultSet.getString("status");
        match.setStatutMatch(status != null ? StatutMatch.valueOf(status) : StatutMatch.EN_ATTENTE);
        
        // Set team names with fallbacks
        String equipe1Nom = resultSet.getString("equipe1_nom");
        match.setEquipe1Nom(equipe1Nom != null && !equipe1Nom.isEmpty() ? equipe1Nom : "Team 1");
        
        String equipe2Nom = resultSet.getString("equipe2_nom");
        match.setEquipe2Nom(equipe2Nom != null && !equipe2Nom.isEmpty() ? equipe2Nom : "Team 2");
        
        // Set tournament name with fallback
        String tournoiNom = resultSet.getString("tournoi_nom");
        match.setTournoiNom(tournoiNom != null && !tournoiNom.isEmpty() ? tournoiNom : "Unknown Tournament");
        
        // Set arena name with fallback
        String areneNom = resultSet.getString("arene_nom");
        match.setAreneNom(areneNom != null && !areneNom.isEmpty() ? areneNom : "Unknown Arena");
        
        return match;
    }
}