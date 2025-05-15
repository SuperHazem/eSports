package models;

import java.time.LocalDateTime;

public class PublicationReport {
    private Integer id;
    private Publication publication;
    private Utilisateur utilisateur;
    private String raison;
    private LocalDateTime dateReport;
    private ReportStatus statut;

    public enum ReportStatus {
        EN_ATTENTE,
        TRAITE,
        REJETE
    }

    public PublicationReport() {
    }

    public PublicationReport(Publication publication, Utilisateur utilisateur, String raison) {
        this.publication = publication;
        this.utilisateur = utilisateur;
        this.raison = raison;
        this.dateReport = LocalDateTime.now();
        this.statut = ReportStatus.EN_ATTENTE;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Publication getPublication() {
        return publication;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public String getRaison() {
        return raison;
    }

    public void setRaison(String raison) {
        this.raison = raison;
    }

    public LocalDateTime getDateReport() {
        return dateReport;
    }

    public void setDateReport(LocalDateTime dateReport) {
        this.dateReport = dateReport;
    }

    public ReportStatus getStatut() {
        return statut;
    }

    public void setStatut(ReportStatus statut) {
        this.statut = statut;
    }
} 