package models;

import enums.TypeRecompense;

public class Recompense {
    private int id;
    private TypeRecompense type; // Enum pour le type de récompense
    private double valeur;       // Valeur monétaire ou points
    private Utilisateur utilisateur; // Récompensé (Joueur/Equipe)

    // Constructeur
    public Recompense(int id, TypeRecompense type, double valeur, Utilisateur utilisateur) {
        this.id = id;
        this.type = type;
        this.valeur = valeur;
        this.utilisateur = utilisateur;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public TypeRecompense getType() {
        return type;
    }

    public void setType(TypeRecompense type) {
        this.type = type;
    }

    public double getValeur() {
        return valeur;
    }

    public void setValeur(double valeur) {
        this.valeur = valeur;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    @Override
    public String toString() {
        return "Recompense{" +
                "id=" + id +
                ", type=" + type +
                ", valeur=" + valeur +
                ", utilisateur=" + utilisateur +
                '}';
    }
}