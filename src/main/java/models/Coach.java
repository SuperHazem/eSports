package models;

import enums.Role;

public class Coach extends Utilisateur {
    private String strategie;

    public Coach(Role role, String motDePasseHash, String email, int id, String nom, String prenom, String strategie) {
        // Fix the parameter order to match the Utilisateur constructor
        super(id, email, motDePasseHash, role, nom, prenom);
        this.strategie = strategie;
    }

    public String getStrategie() {
        return strategie;
    }

    public void setStrategie(String strategie) {
        this.strategie = strategie;
    }
}