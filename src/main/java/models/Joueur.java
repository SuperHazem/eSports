package models;

import enums.Role;

public class Joueur extends Utilisateur {
    private String pseudoJeu;
    private String rank;
    private double winRate;

    public Joueur(Role role, String motDePasseHash, String email, int id, String nom, String prenom, String pseudoJeu, double winRate, String rank) {
        // Fix the parameter order to match the Utilisateur constructor
        super(id, email, motDePasseHash, role, nom, prenom);
        this.pseudoJeu = pseudoJeu;
        this.winRate = winRate;
        this.rank = rank;
    }

    public Joueur(int i, String motDePasseHash, String email, double v) {
    }

    public String getPseudoJeu() {
        return pseudoJeu;
    }

    public void setPseudoJeu(String pseudoJeu) {
        this.pseudoJeu = pseudoJeu;
    }

    public double getWinRate() {
        return winRate;
    }

    public void setWinRate(double winRate) {
        this.winRate = winRate;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }
}