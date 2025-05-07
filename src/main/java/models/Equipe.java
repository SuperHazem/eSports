package models;


import java.util.ArrayList;
import java.util.List;

public class Equipe {
    private int id;
    private String nom;
    private int coachId;
    private List<Integer> listeJoueurs; // Liste des IDs des joueurs
    private double winRate;
    private String status; // Champ ajouté

    // Premier constructeur avec tous les paramètres, y compris le statut
    public Equipe(int id, String nom, int coachId, List<Integer> listeJoueurs, double winRate, String status) {
        this.id = id;
        this.nom = nom;
        this.coachId = coachId;
        this.listeJoueurs = listeJoueurs;
        this.winRate = winRate;
        this.status = status;
    }

    // Deuxième constructeur avec initialisation de listeJoueurs vide et ajout du statut
    public Equipe(int id, String nom, int coachId, double winRate, String status) {
        this.id = id;
        this.nom = nom;
        this.coachId = coachId;
        this.winRate = winRate;
        this.status = status;
        this.listeJoueurs = new ArrayList<>(); // Initialisation vide
    }

    public Equipe(int id, String teamName, int coachId, ArrayList<Joueur> joueurs, double v) {
    }

    public Equipe(int equipeId, String équipeInconnue, int i, double v) {
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

    // Getter correct pour le statut
    public String getStatus() {
        return status;
    }

    // Setter pour le statut
    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Equipe{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", coachId=" + coachId +
                ", listeJoueurs=" + listeJoueurs +
                ", winRate=" + winRate +
                ", status='" + status + '\'' +
                '}';
    }
}