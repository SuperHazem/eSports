package utils;

/**
 * Interface pour les écouteurs d'événements de siège
 */
public interface SiegeEventListener {
    /**
     * Méthode appelée lorsqu'un événement de siège se produit
     */
    void onSiegeEvent(SiegeEvent event);
}
