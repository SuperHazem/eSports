package models;

import java.util.Date;

public class Ticket {
    private int id;
    private double prix;
    private String siege;
    private Date dateAchat;
    private String titre;
    private String statutPaiement; // Nouveau champ

    // Constructeurs
    public Ticket() {
        this.dateAchat = new Date(); // Date actuelle par défaut
        this.statutPaiement = "Non payé"; // Statut par défaut
    }

    public Ticket(int id, double prix, String siege, Date dateAchat, String titre, String statutPaiement) {
        this.id = id;
        this.prix = prix;
        this.siege = siege;
        this.dateAchat = dateAchat;
        this.titre = titre;
        this.statutPaiement = statutPaiement;
    }

    // Getters et Setters existants
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public double getPrix() { return prix; }
    public void setPrix(double prix) { this.prix = prix; }

    public String getSiege() { return siege; }
    public void setSiege(String siege) { this.siege = siege; }

    public Date getDateAchat() { return dateAchat; }
    public void setDateAchat(Date dateAchat) { this.dateAchat = dateAchat; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    // Nouveaux getters et setters pour le statut de paiement
    public String getStatutPaiement() { return statutPaiement; }
    public void setStatutPaiement(String statutPaiement) { this.statutPaiement = statutPaiement; }

    // Méthode utilitaire pour calculer le prix en fonction du siège
    public static double calculerPrix(String siege) {
        if (siege == null || siege.isEmpty()) {
            return 0.0;
        }

        char categorie = siege.charAt(0);
        switch (categorie) {
            case 'A':
                return 60.0;
            case 'B':
                return 40.0;
            case 'C':
                return 20.0;
            default:
                return 0.0;
        }
    }
}