package models;

import enums.Role;

public class Utilisateur {
    private int id;
    private String email;
    private String motDePasseHash;
    private Role role; // Enum: ADMIN, COACH, JOUEUR, SPECTATEUR

    public Utilisateur(int id, String email, String motDePasseHash, Role role) {
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty.");
        }
        this.id = id;
        this.email = email;
        this.motDePasseHash = motDePasseHash;
        this.role = role;
    }

    public Utilisateur(Role role, String motDePasseHash, String email) {
        if (email == null || motDePasseHash == null || role == null) {
            throw new IllegalArgumentException("Email, mot de passe, et rôle sont obligatoires !");
        }
        this.role = role;
        this.motDePasseHash = motDePasseHash;
        this.email = email;
    }

    public Utilisateur(String email, String motDePasse, Role role) {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRole() {
        return String.valueOf(role);
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getMotDePasseHash() {
        return motDePasseHash;
    }

    public void setMotDePasseHash(String motDePasseHash) {
        this.motDePasseHash = motDePasseHash;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
