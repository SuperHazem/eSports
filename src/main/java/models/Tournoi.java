package models;

import java.sql.Date;

public class Tournoi {
    private int id;
    private String nom;
    private Date dateDebut;
    private Date dateFin;
    private Date dateMatch;
    private String equipes;
    private String matches;
    private String status;

    public Tournoi() {
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public Date getDateDebut() { return dateDebut; }
    public void setDateDebut(Date dateDebut) { this.dateDebut = dateDebut; }

    public Date getDateFin() { return dateFin; }
    public void setDateFin(Date dateFin) { this.dateFin = dateFin; }

    public Date getDateMatch() { return dateMatch; }
    public void setDateMatch(Date dateMatch) { this.dateMatch = dateMatch; }

    public String getEquipes() { return equipes; }
    public void setEquipes(String equipes) { this.equipes = equipes; }

    public String getMatches() { return matches; }
    public void setMatches(String matches) { this.matches = matches; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}