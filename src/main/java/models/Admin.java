package models;

import enums.Role;

public class Admin extends Utilisateur {
    public Admin(Role role, String motDePasseHash, String email, int id, String nom, String prenom) {
        // Fix the parameter order to match the Utilisateur constructor
        super(id, email, motDePasseHash, role, nom, prenom);
    }
}