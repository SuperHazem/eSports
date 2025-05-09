package models;

import enums.Role;

public class Utilisateur {
    private int id;
    private String email;
    private String motDePasseHash;
    private Role role;
    private String nom; // Last name
    private String prenom; // First name

    public Utilisateur() {}

    // Constructor for new users (without ID)
    public Utilisateur(String email, String motDePasseHash, Role role, String nom, String prenom) {
        this.email = email;
        this.motDePasseHash = motDePasseHash;
        this.role = role;
        this.nom = nom;
        this.prenom = prenom;
    }

    // Constructor with ID for existing users - FIX THE PARAMETER ORDER HERE
    public Utilisateur(int id, String email, String motDePasseHash, Role role, String nom, String prenom) {
        this.id = id;
        this.email = email;
        this.motDePasseHash = motDePasseHash;
        this.role = role;
        this.nom = nom;
        this.prenom = prenom;
    }

    public Utilisateur(int id, String lefevre, String lucas, String mail, String hash123, Role role) {
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMotDePasseHash() { return motDePasseHash; }
    public void setMotDePasseHash(String motDePasseHash) { this.motDePasseHash = motDePasseHash; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    @Override
    public String toString() {
        return "Utilisateur{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", role=" + role +
                '}';
    }
}