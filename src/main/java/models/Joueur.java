package models;

import enums.Role;

public class Joueur extends Utilisateur{
    private String pseudoJeu;
    private String rank;
    private double winRate;

    public Joueur(Role role, String motDePasseHash, String email, int id, String pseudoJeu, double winRate, String rank) {
        super(id, motDePasseHash, email, role);
        this.pseudoJeu = pseudoJeu;
        this.winRate = winRate;
        this.rank = rank;
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
