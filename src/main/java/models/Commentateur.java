package models;

public class Commentateur {
    private int id;
    private String nom;
    private String expertise; // Domaine d'expertise (ex: "FIFA", "LOL")
    private String langue;    // Langue du commentaire (ex: "Français", "Anglais")

    // Constructeur par défaut
    public Commentateur() {}

    // Constructeur avec paramètres
    public Commentateur(int id, String nom, String expertise, String langue) {
        this.id = id;
        this.nom = nom;
        this.expertise = expertise;
        this.langue = langue;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getExpertise() {
        return expertise;
    }

    public void setExpertise(String expertise) {
        this.expertise = expertise;
    }

    public String getLangue() {
        return langue;
    }

    public void setLangue(String langue) {
        this.langue = langue;
    }

    @Override
    public String toString() {
        return "Commentateur{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", expertise='" + expertise + '\'' +
                ", langue='" + langue + '\'' +
                '}';
    }
}