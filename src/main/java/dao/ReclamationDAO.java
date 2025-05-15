package dao;

import models.Reclamation;
import models.Utilisateur;
import models.Ticket;
import utils.DatabaseConnection;
import enums.Statut;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

public class ReclamationDAO implements GenericDAO<Reclamation, Integer> {

    private Connection connection;

    public ReclamationDAO() throws SQLException {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public List<Reclamation> lireTous() {
        List<Reclamation> reclamations = new ArrayList<>();
        String sql = "SELECT * FROM reclamation";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Reclamation r = new Reclamation();
                r.setId(rs.getInt("id"));
                r.setObjet(rs.getString("objet"));
                r.setDescription(rs.getString("description"));
                r.setDate(rs.getDate("date"));

                // Récupérer le statut et le convertir en enum
                try {
                    String statutStr = rs.getString("statut");
                    r.setStatut(statutStr != null ? Statut.fromString(statutStr) : Statut.EN_COURS);
                } catch (SQLException e) {
                    // Si la colonne n'existe pas encore, utiliser la valeur par défaut
                    r.setStatut(Statut.EN_COURS);
                }

                Utilisateur utilisateur = new Utilisateur();
                utilisateur.setId(rs.getInt("utilisateur_id"));
                r.setUtilisateur(utilisateur);

                Ticket ticket = new Ticket();
                ticket.setId(rs.getInt("ticket_id"));
                r.setTicket(ticket);

                reclamations.add(r);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reclamations;
    }

    @Override
    public void ajouter(Reclamation reclamation) {
        // Vérifier si la colonne statut existe
        boolean statutExiste = verifierColonneStatut();

        String sql;
        if (statutExiste) {
            sql = "INSERT INTO reclamation (objet, description, date, statut, ticket_id) VALUES (?, ?, ?, ?, ?)";
        } else {
            sql = "INSERT INTO reclamation (objet, description, date, ticket_id) VALUES (?, ?, ?, ?)";
        }

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, reclamation.getObjet());
            stmt.setString(2, reclamation.getDescription());
            stmt.setDate(3, new java.sql.Date(reclamation.getDate().getTime()));

            if (statutExiste) {
                stmt.setString(4, reclamation.getStatut().getLibelle());
                stmt.setInt(5, reclamation.getTicket().getId());
            } else {
                stmt.setInt(4, reclamation.getTicket().getId());
            }

            stmt.executeUpdate();

            // Récupérer l'ID généré
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    reclamation.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void modifier(Reclamation reclamation) {
        // Vérifier si la colonne statut existe
        boolean statutExiste = verifierColonneStatut();

        String sql;
        if (statutExiste) {
            sql = "UPDATE reclamation SET objet = ?, description = ?, date = ?, statut = ?,  ticket_id = ? WHERE id = ?";
        } else {
            sql = "UPDATE reclamation SET objet = ?, description = ?, date = ?,  ticket_id = ? WHERE id = ?";
        }

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, reclamation.getObjet());
            stmt.setString(2, reclamation.getDescription());
            stmt.setDate(3, new java.sql.Date(reclamation.getDate().getTime()));

            if (statutExiste) {
                // Convertir l'enum en chaîne pour la base de données
                stmt.setString(4, reclamation.getStatut().getLibelle());
                stmt.setInt(5, reclamation.getTicket().getId());
                stmt.setInt(6, reclamation.getId());
            } else {
                stmt.setInt(5, reclamation.getTicket().getId());
                stmt.setInt(6, reclamation.getId());
            }

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void supprimer(Integer id) {
        String sql = "DELETE FROM reclamation WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Reclamation lire(Integer id) {
        Reclamation reclamation = null;
        String sql = "SELECT * FROM reclamation WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                reclamation = new Reclamation();
                reclamation.setId(rs.getInt("id"));
                reclamation.setObjet(rs.getString("objet"));
                reclamation.setDescription(rs.getString("description"));
                reclamation.setDate(rs.getDate("date"));

                // Récupérer le statut et le convertir en enum
                try {
                    String statutStr = rs.getString("statut");
                    reclamation.setStatut(statutStr != null ? Statut.fromString(statutStr) : Statut.EN_COURS);
                } catch (SQLException e) {
                    // Si la colonne n'existe pas encore, utiliser la valeur par défaut
                    reclamation.setStatut(Statut.EN_COURS);
                }

                Utilisateur utilisateur = new Utilisateur();
                utilisateur.setId(rs.getInt("utilisateur_id"));
                reclamation.setUtilisateur(utilisateur);

                Ticket ticket = new Ticket();
                ticket.setId(rs.getInt("ticket_id"));
                reclamation.setTicket(ticket);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reclamation;
    }

    // Méthode pour rechercher des réclamations par statut et date
    public List<Reclamation> rechercherParStatutEtDate(Statut statut, Date date) {
        List<Reclamation> reclamations = new ArrayList<>();

        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM reclamation WHERE 1=1");
        List<Object> params = new ArrayList<>();

        // Vérifier si la colonne statut existe
        boolean statutExiste = verifierColonneStatut();

        if (statutExiste && statut != null) {
            sqlBuilder.append(" AND statut = ?");
            params.add(statut.getLibelle());
        }

        if (date != null) {
            sqlBuilder.append(" AND DATE(date) = ?");
            params.add(new java.sql.Date(date.getTime()));
        }

        try (PreparedStatement stmt = connection.prepareStatement(sqlBuilder.toString())) {
            // Définir les paramètres
            for (int i = 0; i < params.size(); i++) {
                Object param = params.get(i);
                if (param instanceof String) {
                    stmt.setString(i + 1, (String) param);
                } else if (param instanceof java.sql.Date) {
                    stmt.setDate(i + 1, (java.sql.Date) param);
                }
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Reclamation r = new Reclamation();
                r.setId(rs.getInt("id"));
                r.setObjet(rs.getString("objet"));
                r.setDescription(rs.getString("description"));
                r.setDate(rs.getDate("date"));

                if (statutExiste) {
                    String statutStr = rs.getString("statut");
                    r.setStatut(statutStr != null ? Statut.fromString(statutStr) : Statut.EN_COURS);
                } else {
                    r.setStatut(Statut.EN_COURS);
                }

                Utilisateur utilisateur = new Utilisateur();
                utilisateur.setId(rs.getInt("utilisateur_id"));
                r.setUtilisateur(utilisateur);

                Ticket ticket = new Ticket();
                ticket.setId(rs.getInt("ticket_id"));
                r.setTicket(ticket);

                reclamations.add(r);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return reclamations;
    }

    // Surcharge de la méthode pour accepter une chaîne de caractères (pour la compatibilité)
    public List<Reclamation> rechercherParStatutEtDate(String statutStr, Date date) {
        Statut statut = null;
        if (statutStr != null && !statutStr.isEmpty() && !statutStr.equals("Tous")) {
            statut = Statut.fromString(statutStr);
        }
        return rechercherParStatutEtDate(statut, date);
    }

    // Méthode pour vérifier si la colonne statut existe dans la table
    private boolean verifierColonneStatut() {
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, "reclamation", "statut");
            return columns.next(); // Si next() retourne true, la colonne existe
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Script SQL pour ajouter la colonne statut si elle n'existe pas
    public void ajouterColonneStatutSiNecessaire() {
        if (!verifierColonneStatut()) {
            try (Statement stmt = connection.createStatement()) {
                stmt.executeUpdate("ALTER TABLE reclamation ADD COLUMN statut VARCHAR(50) DEFAULT 'En cours'");
                System.out.println("Colonne statut ajoutée à la table reclamation");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    public Reclamation lireParId(int id) {
        return lire(id);
    }
    public List<Reclamation> getReclamationsEnAttente() {
        List<Reclamation> reclamations = new ArrayList<>();

        // Vérifier si la colonne statut existe
        boolean statutExiste = verifierColonneStatut();

        String sql;
        if (statutExiste) {
            sql = "SELECT * FROM reclamation WHERE statut = 'En cours'";
        } else {
            sql = "SELECT * FROM reclamation"; // Si pas de colonne statut, on prend toutes les réclamations
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Reclamation r = new Reclamation();
                r.setId(rs.getInt("id"));
                r.setObjet(rs.getString("objet"));
                r.setDescription(rs.getString("description"));
                r.setDate(rs.getDate("date"));

                // Récupérer le statut et le convertir en enum
                if (statutExiste) {
                    try {
                        String statutStr = rs.getString("statut");
                        r.setStatut(statutStr != null ? Statut.fromString(statutStr) : Statut.EN_COURS);
                    } catch (SQLException e) {
                        // Si la colonne n'existe pas encore, utiliser la valeur par défaut
                        r.setStatut(Statut.EN_COURS);
                    }
                } else {
                    r.setStatut(Statut.EN_COURS);
                }

                Utilisateur utilisateur = new Utilisateur();
                utilisateur.setId(rs.getInt("utilisateur_id"));
                r.setUtilisateur(utilisateur);

                Ticket ticket = new Ticket();
                ticket.setId(rs.getInt("ticket_id"));
                r.setTicket(ticket);

                reclamations.add(r);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return reclamations;
    }
    public void updateStatus(int id, String status) {
        // Vérifier si la colonne statut existe
        boolean statutExiste = verifierColonneStatut();

        if (!statutExiste) {
            ajouterColonneStatutSiNecessaire();
        }

        String sql = "UPDATE reclamation SET statut = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}