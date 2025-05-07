package models;

import enums.TypeRecompense;
import java.util.Date;
import java.util.Objects;

public class Recompense {
    private int id;
    private TypeRecompense type;     // PRIX, BONUS_FINANCIER, TROPHEE, AUTRE
    private double valeur;          // e.g., monetary value or XP points
    private Equipe equipe;          // Team being rewarded
    private String description;     // Optional description
    private Date dateAttribution;   // Date when the reward was given

    public Recompense(int id, TypeRecompense type, double valeur, Equipe equipe, String description) {
        this.id = id;
        this.type = type;
        this.valeur = valeur;
        this.equipe = equipe;
        this.description = description;
        this.dateAttribution = new Date(); // Default to current date
    }

    public Recompense(int id, TypeRecompense type, double valeur, Equipe equipe, String description, Date dateAttribution) {
        this.id = id;
        this.type = type;
        this.valeur = valeur;
        this.equipe = equipe != null ? equipe : new Equipe(-1, "Équipe inconnue", 0, 0.0);
        this.description = description;
        this.dateAttribution = dateAttribution;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public TypeRecompense getType() { return type; }
    public void setType(TypeRecompense type) { this.type = type; }

    public double getValeur() { return valeur; }
    public void setValeur(double valeur) { this.valeur = valeur; }

    public Equipe getEquipe() { return equipe; }
    public void setEquipe(Equipe equipe) { this.equipe = equipe; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Date getDateAttribution() { return dateAttribution; }
    public void setDateAttribution(Date dateAttribution) { this.dateAttribution = dateAttribution; }

    @Override
    public String toString() {
        return "Recompense{" +
                "id=" + id +
                ", type=" + type +
                ", valeur=" + valeur +
                ", equipe=" + (equipe != null ? equipe.getNom() : "null") +
                ", description='" + description + '\'' +
                ", dateAttribution=" + dateAttribution +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Recompense)) return false;
        Recompense that = (Recompense) o;
        return id == that.id &&
                Double.compare(that.valeur, valeur) == 0 &&
                Objects.equals(type, that.type) &&
                Objects.equals(equipe, that.equipe);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, valeur, equipe);
    }
}