package models;

import enums.Role;

public class Coach extends Utilisateur {
    private String strategie;

    public Coach(Role role, String motDePasseHash, String email, int id, String strategie) {
        super(id, motDePasseHash, email, role);
        this.strategie = strategie;
    }



    public String getStrategie() {
        return strategie;
    }

    public void setStrategie(String strategie) {
        this.strategie = strategie;
    }
}
