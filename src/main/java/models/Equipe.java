package models;

import java.util.List;

public class Equipe {
    private int id;
    private String nom;
    private List<Joueur> joueurs; // Association with Joueur
    private Coach coach; // Association with Coach

    public Equipe(int id, Coach coach, List<Joueur> joueurs, String nom) {
        this.id = id;
        this.coach = coach;
        this.joueurs = joueurs;
        this.nom = nom;
    }

    public Equipe(int id, String nom) {
        this.id = id;
        this.nom = nom;
    }

    public Equipe() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Coach getCoach() {
        return coach;
    }

    public void setCoach(Coach coach) {
        this.coach = coach;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public List<Joueur> getJoueurs() {
        return joueurs;
    }

    public void setJoueurs(List<Joueur> joueurs) {this.joueurs = joueurs;}
}
