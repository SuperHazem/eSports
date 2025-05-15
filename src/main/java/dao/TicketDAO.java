package dao;

import models.Ticket;
import utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TicketDAO implements GenericDAO<Ticket, Integer> {

    private Connection connection;

    public TicketDAO() throws SQLException {
        this.connection = DatabaseConnection.getInstance().getConnection();
        // Vérifier si la colonne statut_paiement existe, sinon la créer
        ajouterColonneStatutPaiementSiNecessaire();
    }

    // Méthode pour ajouter la colonne statut_paiement si elle n'existe pas
    public void ajouterColonneStatutPaiementSiNecessaire() {
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, "ticket", "statut_paiement");

            if (!columns.next()) {
                // La colonne n'existe pas, on la crée
                Statement stmt = connection.createStatement();
                stmt.execute("ALTER TABLE ticket ADD COLUMN statut_paiement VARCHAR(50) DEFAULT 'Non payé'");
                stmt.close();
                System.out.println("Colonne statut_paiement ajoutée à la table ticket");
            }
            columns.close();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification/ajout de la colonne statut_paiement: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public List<Ticket> lireTous() {
        List<Ticket> tickets = new ArrayList<>();
        String sql = "SELECT * FROM ticket";

        // Ajout de log pour le débogage
        System.out.println("Exécution de la requête: " + sql);

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            // Log pour confirmer l'exécution de la requête
            System.out.println("Requête exécutée avec succès");
            int count = 0;

            while (rs.next()) {
                count++;
                Ticket ticket = new Ticket();
                ticket.setId(rs.getInt("id"));
                ticket.setPrix(rs.getDouble("prix"));
                ticket.setSiege(rs.getString("siege"));

                // Récupérer la date d'achat de manière sécurisée
                try {
                    // Essayer d'abord avec "date_achat"
                    ticket.setDateAchat(rs.getTimestamp("date_achat"));
                } catch (SQLException e) {
                    try {
                        // Essayer ensuite avec "dateachat" (sans underscore)
                        ticket.setDateAchat(rs.getTimestamp("dateachat"));
                    } catch (SQLException e2) {
                        // Si aucune colonne de date n'est trouvée, utiliser la date actuelle
                        ticket.setDateAchat(new Date());
                        System.out.println("Aucune colonne de date trouvée, utilisation de la date actuelle");
                    }
                }

                // Récupérer le statut de paiement
                try {
                    String statutPaiement = rs.getString("statut_paiement");
                    ticket.setStatutPaiement(statutPaiement != null ? statutPaiement : "Non payé");
                } catch (SQLException e) {
                    ticket.setStatutPaiement("Non payé");
                    System.out.println("Colonne statut_paiement non trouvée, utilisation de la valeur par défaut");
                }

                tickets.add(ticket);

                // Log pour chaque ticket chargé
                System.out.println("Ticket chargé: ID=" + ticket.getId() + ", Siège=" + ticket.getSiege());
            }

            // Log pour le nombre total de tickets
            System.out.println("Total des tickets chargés: " + count);
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des tickets: " + e.getMessage());
            e.printStackTrace();
        }
        return tickets;
    }

    @Override
    public void ajouter(Ticket ticket) {
        // Vérifier d'abord quelle colonne existe dans la table
        String nomColonneDate = verifierNomColonneDate();

        String sql = "INSERT INTO ticket (prix, siege";
        if (nomColonneDate != null) {
            sql += ", " + nomColonneDate;
        }
        sql += ", statut_paiement) VALUES (?, ?";
        if (nomColonneDate != null) {
            sql += ", ?";
        }
        sql += ", ?)";

        System.out.println("Exécution de la requête d'ajout: " + sql);

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setDouble(1, ticket.getPrix());
            stmt.setString(2, ticket.getSiege());

            int paramIndex = 3;
            if (nomColonneDate != null) {
                stmt.setTimestamp(paramIndex++, new java.sql.Timestamp(ticket.getDateAchat().getTime()));
            }

            stmt.setString(paramIndex, ticket.getStatutPaiement() != null ? ticket.getStatutPaiement() : "Non payé");

            int rowsAffected = stmt.executeUpdate();
            System.out.println("Ajout de ticket - Lignes affectées: " + rowsAffected);

            // Récupérer l'ID généré
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    ticket.setId(generatedKeys.getInt(1));
                    System.out.println("ID généré pour le nouveau ticket: " + ticket.getId());
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout du ticket: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void modifier(Ticket ticket) {
        // Vérifier d'abord quelle colonne existe dans la table
        String nomColonneDate = verifierNomColonneDate();

        String sql = "UPDATE ticket SET prix = ?, siege = ?";
        if (nomColonneDate != null) {
            sql += ", " + nomColonneDate + " = ?";
        }
        sql += ", statut_paiement = ? WHERE id = ?";

        System.out.println("Exécution de la requête de modification: " + sql);

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDouble(1, ticket.getPrix());
            stmt.setString(2, ticket.getSiege());

            int paramIndex = 3;
            if (nomColonneDate != null) {
                stmt.setTimestamp(paramIndex++, new java.sql.Timestamp(ticket.getDateAchat().getTime()));
            }

            stmt.setString(paramIndex++, ticket.getStatutPaiement() != null ? ticket.getStatutPaiement() : "Non payé");
            stmt.setInt(paramIndex, ticket.getId());

            int rowsAffected = stmt.executeUpdate();
            System.out.println("Modification de ticket - Lignes affectées: " + rowsAffected);
        } catch (SQLException e) {
            System.err.println("Erreur lors de la modification du ticket: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void supprimer(Integer id) {
        String sql = "DELETE FROM ticket WHERE id = ?";

        System.out.println("Exécution de la requête de suppression: " + sql);

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();
            System.out.println("Suppression de ticket - Lignes affectées: " + rowsAffected);
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression du ticket: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<Ticket> rechercherParSiegeEtDate(String siege, Date date) {
        List<Ticket> tickets = new ArrayList<>();

        // Vérifier d'abord quelle colonne existe dans la table
        String nomColonneDate = verifierNomColonneDate();

        String sql = "SELECT * FROM ticket WHERE 1=1";

        if (siege != null && !siege.isEmpty()) {
            sql += " AND siege LIKE ?";
        }

        if (date != null && nomColonneDate != null) {
            sql += " AND DATE(" + nomColonneDate + ") = ?";
        }

        System.out.println("Exécution de la requête de recherche: " + sql);

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int paramIndex = 1;

            if (siege != null && !siege.isEmpty()) {
                stmt.setString(paramIndex++, "%" + siege + "%");
            }

            if (date != null && nomColonneDate != null) {
                stmt.setDate(paramIndex, new java.sql.Date(date.getTime()));
            }

            ResultSet rs = stmt.executeQuery();
            int count = 0;

            while (rs.next()) {
                count++;
                Ticket ticket = new Ticket();
                ticket.setId(rs.getInt("id"));
                ticket.setPrix(rs.getDouble("prix"));
                ticket.setSiege(rs.getString("siege"));

                // Récupérer la date d'achat de manière sécurisée
                if (nomColonneDate != null) {
                    try {
                        ticket.setDateAchat(rs.getTimestamp(nomColonneDate));
                    } catch (SQLException e) {
                        ticket.setDateAchat(new Date());
                        System.out.println("Erreur lors de la récupération de la date, utilisation de la date actuelle");
                    }
                } else {
                    ticket.setDateAchat(new Date());
                }

                // Récupérer le statut de paiement
                try {
                    String statutPaiement = rs.getString("statut_paiement");
                    ticket.setStatutPaiement(statutPaiement != null ? statutPaiement : "Non payé");
                } catch (SQLException e) {
                    ticket.setStatutPaiement("Non payé");
                    System.out.println("Colonne statut_paiement non trouvée, utilisation de la valeur par défaut");
                }

                tickets.add(ticket);
            }

            System.out.println("Recherche de tickets - Résultats trouvés: " + count);
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche de tickets: " + e.getMessage());
            e.printStackTrace();
        }
        return tickets;
    }

    @Override
    public Ticket lire(Integer id) {
        Ticket ticket = null;
        String sql = "SELECT * FROM ticket WHERE id = ?";

        System.out.println("Exécution de la requête de lecture: " + sql);

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                ticket = new Ticket();
                ticket.setId(rs.getInt("id"));
                ticket.setPrix(rs.getDouble("prix"));
                ticket.setSiege(rs.getString("siege"));

                // Récupérer la date d'achat de manière sécurisée
                String nomColonneDate = verifierNomColonneDate();
                if (nomColonneDate != null) {
                    try {
                        ticket.setDateAchat(rs.getTimestamp(nomColonneDate));
                    } catch (SQLException e) {
                        ticket.setDateAchat(new Date());
                        System.out.println("Erreur lors de la récupération de la date, utilisation de la date actuelle");
                    }
                } else {
                    ticket.setDateAchat(new Date());
                }

                // Récupérer le statut de paiement
                try {
                    String statutPaiement = rs.getString("statut_paiement");
                    ticket.setStatutPaiement(statutPaiement != null ? statutPaiement : "Non payé");
                } catch (SQLException e) {
                    ticket.setStatutPaiement("Non payé");
                    System.out.println("Colonne statut_paiement non trouvée, utilisation de la valeur par défaut");
                }

                System.out.println("Ticket trouvé: ID=" + ticket.getId() + ", Siège=" + ticket.getSiege());
            } else {
                System.out.println("Aucun ticket trouvé avec l'ID: " + id);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la lecture du ticket: " + e.getMessage());
            e.printStackTrace();
        }
        return ticket;
    }

    public List<String> getSiegesReserves() {
        List<String> sieges = new ArrayList<>();
        String sql = "SELECT siege FROM ticket";

        System.out.println("Exécution de la requête pour obtenir les sièges réservés: " + sql);

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            int count = 0;
            while (rs.next()) {
                count++;
                sieges.add(rs.getString("siege"));
            }
            System.out.println("Nombre de sièges réservés trouvés: " + count);
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des sièges réservés: " + e.getMessage());
            e.printStackTrace();
        }
        return sieges;
    }

    // Méthode utilitaire pour vérifier le nom exact de la colonne de date dans la table
    private String verifierNomColonneDate() {
        try {
            // Récupérer les métadonnées de la table
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, "ticket", null);

            // Parcourir les colonnes pour trouver celle qui contient "date" dans son nom
            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME").toLowerCase();
                if (columnName.equals("date_achat") || columnName.equals("dateachat") ||
                        columnName.contains("date")) {
                    System.out.println("Colonne de date trouvée: " + columnName);
                    return columnName;
                }
            }
            System.out.println("Aucune colonne de date trouvée dans la table ticket");
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification du nom de la colonne de date: " + e.getMessage());
            e.printStackTrace();
        }

        // Si aucune colonne de date n'est trouvée, retourner null
        return null;
    }
}