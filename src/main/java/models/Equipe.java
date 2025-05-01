package models;

import java.util.ArrayList;
import java.util.List;

public class Equipe {
    private int id;
    private String nom;
    private int coachId;
    private List<Integer> listeJoueurs; // Liste des IDs des joueurs
    private double winRate;

    public Equipe(int id, String nom, int coachId, List<Integer> listeJoueurs, double winRate) {
        this.id = id;
        this.nom = nom;
        this.coachId = coachId;
        this.listeJoueurs = listeJoueurs;
        this.winRate = winRate;
    }

    public Equipe(int id, String nom, int coachId, double winRate) {
        this.id = id;
        this.nom = nom;
        this.coachId = coachId;
        this.winRate = winRate;
        // Initialize listeJoueurs as an empty list to avoid null pointer exceptions
        this.listeJoueurs = new ArrayList<>();
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public int getCoachId() { return coachId; }
    public void setCoachId(int coachId) { this.coachId = coachId; }
    public List<Integer> getListeJoueurs() { return listeJoueurs; }
    public void setListeJoueurs(List<Integer> listeJoueurs) { this.listeJoueurs = listeJoueurs; }
    public double getWinRate() { return winRate; }
    public void setWinRate(double winRate) { this.winRate = winRate; }

    @Override
    public String toString() {
        return "Equipe{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", coachId=" + coachId +
                ", listeJoueurs=" + listeJoueurs +
                ", winRate=" + winRate +
                '}';
    }
}