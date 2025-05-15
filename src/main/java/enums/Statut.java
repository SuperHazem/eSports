package enums;

public enum Statut {
    EN_COURS("En cours"),
    RESOLU("Résolu"),
    REJETE("Rejeté");

    private final String libelle;

    Statut(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }

    public static Statut fromString(String text) {
        for (Statut s : Statut.values()) {
            if (s.libelle.equalsIgnoreCase(text)) {
                return s;
            }
        }
        return EN_COURS; // Valeur par défaut
    }
}
