package models;

import enums.Role;

import java.util.Date;

public class Spectateur extends Utilisateur{
    private String nom;
    private Date dateInscription;

    public Spectateur(Role role, String motDePasseHash, String email, int id, String nom, Date dateInscription) {
        super(id, motDePasseHash, email, role);
        this.nom = nom;
        this.dateInscription = dateInscription;
    }


    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public Date getDateInscription() {
        return dateInscription;
    }

    public void setDateInscription(Date dateInscription) {
        this.dateInscription = dateInscription;
    }
}
