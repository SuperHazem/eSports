package models;

import enums.Statut;
import java.util.Date;

public class Reclamation {
    private int id;
    private String objet;
    private String description;
    private Date date;
    private Statut statut = Statut.EN_COURS; // Valeur par défaut
    private Utilisateur utilisateur;
    private Ticket ticket;
    private int ticketId; // Pour faciliter l'accès direct à l'ID du ticket

    public Reclamation() {
    }

    public Reclamation(int id, String objet, String description, Date date, Statut statut, Utilisateur utilisateur, Ticket ticket) {
        this.id = id;
        this.objet = objet;
        this.description = description;
        this.date = date;
        this.statut = statut;
        this.utilisateur = utilisateur;
        this.ticket = ticket;
        if (ticket != null) {
            this.ticketId = ticket.getId();
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getObjet() {
        return objet;
    }

    public void setObjet(String objet) {
        this.objet = objet;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Statut getStatut() {
        return statut;
    }

    public void setStatut(Statut statut) {
        this.statut = statut;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
        if (ticket != null) {
            this.ticketId = ticket.getId();
        }
    }

    public int getTicketId() {
        return ticketId;
    }

    public void setTicketId(int ticketId) {
        this.ticketId = ticketId;
    }

    public void setStatus(String status) {
        this.statut = Statut.fromString(status);
    }

    @Override
    public String toString() {
        return "Reclamation{" +
                "id=" + id +
                ", objet='" + objet + '\'' +
                ", description='" + description + '\'' +
                ", date=" + date +
                ", statut=" + statut +
                ", ticketId=" + ticketId +
                '}';
    }
}
