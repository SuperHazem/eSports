package utils;

import models.Siege;

/**
 * Événement lié aux sièges pour la communication entre interfaces
 */
public class SiegeEvent {
    // Types d'événements
    public static final String SIEGE_AJOUTE = "SIEGE_AJOUTE";
    public static final String SIEGE_SUPPRIME = "SIEGE_SUPPRIME";
    public static final String SIEGE_MODIFIE = "SIEGE_MODIFIE";

    private String type;
    private Siege siege;

    public SiegeEvent(String type, Siege siege) {
        this.type = type;
        this.siege = siege;
    }

    public String getType() {
        return type;
    }

    public Siege getSiege() {
        return siege;
    }
}
