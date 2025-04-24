package models;

import enums.Role;

public class Admin extends Utilisateur {
    public Admin(Role role, String motDePasseHash, String email, int id) {
        super(id, motDePasseHash, email, role);
    }



}
