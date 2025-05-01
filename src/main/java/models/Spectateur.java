package models;

import enums.Role;

import java.util.Date;

public class Spectateur extends Utilisateur {
    private Date dateInscription;

    public Spectateur(Role role, String motDePasseHash, String email, int id, String nom, String prenom, Date dateInscription) {
        super(id, email, motDePasseHash, role, nom, prenom);
        this.dateInscription = dateInscription;
    }

    public Date getDateInscription() {
        return dateInscription;
    }

    public void setDateInscription(Date dateInscription) {
        this.dateInscription = dateInscription;
    }
}