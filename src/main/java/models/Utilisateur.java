package models;

import enums.Role;
import enums.UserStatus;
import java.time.LocalDate;

public class Utilisateur {
    private int id;
    private String email;
    private String motDePasseHash;
    private Role role;
    private String nom; // Last name
    private String prenom; // First name
    private String adresse;
    private String telephone;
    private LocalDate dateNaissance;
    private String profilePicturePath; // Path to profile picture file
    
    // Attributs pour la gestion des statuts d'utilisateur
    private UserStatus status = UserStatus.ACTIF; // Statut par défaut: actif
    private LocalDate suspensionDebut; // Date de début de suspension
    private LocalDate suspensionFin; // Date de fin de suspension
    private String suspensionRaison; // Raison de la suspension ou du bannissement

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

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public LocalDate getDateNaissance() { return dateNaissance; }
    public void setDateNaissance(LocalDate dateNaissance) { this.dateNaissance = dateNaissance; }

    public String getProfilePicturePath() { return profilePicturePath; }
    public void setProfilePicturePath(String profilePicturePath) { this.profilePicturePath = profilePicturePath; }
    
    // Getters et Setters pour les attributs de statut
    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }
    
    public LocalDate getSuspensionDebut() { return suspensionDebut; }
    public void setSuspensionDebut(LocalDate suspensionDebut) { this.suspensionDebut = suspensionDebut; }
    
    public LocalDate getSuspensionFin() { return suspensionFin; }
    public void setSuspensionFin(LocalDate suspensionFin) { this.suspensionFin = suspensionFin; }
    
    public String getSuspensionRaison() { return suspensionRaison; }
    public void setSuspensionRaison(String suspensionRaison) { this.suspensionRaison = suspensionRaison; }
    
    /**
     * Vérifie si l'utilisateur est actuellement suspendu
     * @return true si l'utilisateur est suspendu et la période de suspension est en cours
     */
    public boolean estSuspendu() {
        if (status != UserStatus.SUSPENDU) return false;
        LocalDate aujourdhui = LocalDate.now();
        return suspensionDebut != null && suspensionFin != null && 
               !aujourdhui.isBefore(suspensionDebut) && !aujourdhui.isAfter(suspensionFin);
    }
    
    /**
     * Vérifie si l'utilisateur est banni
     * @return true si l'utilisateur est banni
     */
    public boolean estBanni() {
        return status == UserStatus.BANNI;
    }
    
    /**
     * Vérifie si l'utilisateur peut se connecter
     * @return true si l'utilisateur peut se connecter (actif ou suspension terminée)
     */
    public boolean peutSeConnecter() {
        if (status == UserStatus.ACTIF) return true;
        if (status == UserStatus.BANNI) return false;
        if (status == UserStatus.SUSPENDU) {
            LocalDate aujourdhui = LocalDate.now();
            // Si la date de fin de suspension est passée, l'utilisateur peut se connecter
            if (suspensionFin != null && aujourdhui.isAfter(suspensionFin)) {
                // Réactiver automatiquement l'utilisateur
                status = UserStatus.ACTIF;
                return true;
            }
            return false;
        }
        return true; // Par défaut, autoriser la connexion
    }

    @Override
    public String toString() {
        return "Utilisateur{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", adresse='" + adresse + '\'' +
                ", telephone='" + telephone + '\'' +
                ", dateNaissance=" + dateNaissance +
                ", role=" + role +
                ", status=" + status +
                ", suspensionDebut=" + suspensionDebut +
                ", suspensionFin=" + suspensionFin +
                '}';
    }
}