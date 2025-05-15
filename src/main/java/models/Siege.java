package models;

/**
 * Modèle représentant un siège
 */
public class Siege {
    private String id;
    private String categorie;
    private double prix;
    private boolean disponible;

    public Siege() {
        // Constructeur par défaut
    }

    public Siege(String id, String categorie, double prix, boolean disponible) {
        this.id = id;
        this.categorie = categorie;
        this.prix = prix;
        this.disponible = disponible;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    @Override
    public String toString() {
        return "Siege{" +
                "id='" + id + '\'' +
                ", categorie='" + categorie + '\'' +
                ", prix=" + prix +
                ", disponible=" + disponible +
                '}';
    }
}
