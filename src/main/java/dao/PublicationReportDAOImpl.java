package dao;

import models.PublicationReport;
import models.Utilisateur;
import models.Publication;
import utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PublicationReportDAOImpl implements PublicationReportDAO {
    private final Connection connection;

    public PublicationReportDAOImpl() {
        try {
            this.connection = DatabaseConnection.getInstance().getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la connexion à la base de données", e);
        }
    }

    @Override
    public void ajouter(PublicationReport report) {
        String sql = "INSERT INTO publication_report (publication_id, utilisateur_id, raison, date_report, statut) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, report.getPublication().getId());
            statement.setInt(2, report.getUtilisateur().getId());
            statement.setString(3, report.getRaison());
            statement.setTimestamp(4, Timestamp.valueOf(report.getDateReport()));
            statement.setString(5, report.getStatut().toString());

            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    report.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'ajout du signalement", e);
        }
    }

    @Override
    public PublicationReport lire(Integer id) {
        String sql = "SELECT * FROM publication_report WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return extractReportFromResultSet(resultSet);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la lecture du signalement", e);
        }
        return null;
    }

    @Override
    public List<PublicationReport> lireTous() {
        List<PublicationReport> reports = new ArrayList<>();
        String sql = "SELECT * FROM publication_report ORDER BY date_report DESC";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                reports.add(extractReportFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la lecture des signalements", e);
        }
        return reports;
    }

    @Override
    public void modifier(PublicationReport report) {
        String sql = "UPDATE publication_report SET publication_id = ?, utilisateur_id = ?, raison = ?, date_report = ?, statut = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, report.getPublication().getId());
            statement.setInt(2, report.getUtilisateur().getId());
            statement.setString(3, report.getRaison());
            statement.setTimestamp(4, Timestamp.valueOf(report.getDateReport()));
            statement.setString(5, report.getStatut().toString());
            statement.setInt(6, report.getId());

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la modification du signalement", e);
        }
    }

    @Override
    public void supprimer(Integer id) {
        String sql = "DELETE FROM publication_report WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression du signalement", e);
        }
    }

    @Override
    public List<PublicationReport> lireParPublication(Publication publication) {
        List<PublicationReport> reports = new ArrayList<>();
        String sql = "SELECT * FROM publication_report WHERE publication_id = ? ORDER BY date_report DESC";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, publication.getId());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    reports.add(extractReportFromResultSet(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la lecture des signalements de la publication", e);
        }
        return reports;
    }

    @Override
    public List<PublicationReport> lireParUtilisateur(Utilisateur utilisateur) {
        List<PublicationReport> reports = new ArrayList<>();
        String sql = "SELECT * FROM publication_report WHERE utilisateur_id = ? ORDER BY date_report DESC";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, utilisateur.getId());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    reports.add(extractReportFromResultSet(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la lecture des signalements de l'utilisateur", e);
        }
        return reports;
    }

    @Override
    public List<PublicationReport> lireParStatut(PublicationReport.ReportStatus statut) {
        List<PublicationReport> reports = new ArrayList<>();
        String sql = "SELECT * FROM publication_report WHERE statut = ? ORDER BY date_report DESC";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, statut.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    reports.add(extractReportFromResultSet(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la lecture des signalements par statut", e);
        }
        return reports;
    }

    @Override
    public boolean existeReport(Utilisateur utilisateur, Publication publication) {
        String sql = "SELECT COUNT(*) FROM publication_report WHERE utilisateur_id = ? AND publication_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, utilisateur.getId());
            statement.setInt(2, publication.getId());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la vérification du signalement", e);
        }
        return false;
    }

    @Override
    public void mettreAJourStatut(Integer reportId, PublicationReport.ReportStatus nouveauStatut) {
        String sql = "UPDATE publication_report SET statut = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, nouveauStatut.toString());
            statement.setInt(2, reportId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise à jour du statut du signalement", e);
        }
    }

    private PublicationReport extractReportFromResultSet(ResultSet resultSet) throws SQLException {
        PublicationReport report = new PublicationReport();
        report.setId(resultSet.getInt("id"));
        
        // Get publication
        PublicationDAO publicationDAO = new PublicationDAOImpl();
        Publication publication = publicationDAO.lire(resultSet.getInt("publication_id"));
        report.setPublication(publication);
        
        // Get user
        UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
        Utilisateur utilisateur = utilisateurDAO.lire(resultSet.getInt("utilisateur_id"));
        report.setUtilisateur(utilisateur);
        
        report.setRaison(resultSet.getString("raison"));
        report.setDateReport(resultSet.getTimestamp("date_report").toLocalDateTime());
        report.setStatut(PublicationReport.ReportStatus.valueOf(resultSet.getString("statut")));
        
        return report;
    }
} 