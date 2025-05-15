package utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Gestionnaire d'événements pour les sièges (pattern Observer)
 */
public class SiegeEventManager {
    private static SiegeEventManager instance;
    private List<SiegeEventListener> listeners = new ArrayList<>();

    private SiegeEventManager() {
        // Constructeur privé pour le singleton
    }

    public static synchronized SiegeEventManager getInstance() {
        if (instance == null) {
            instance = new SiegeEventManager();
        }
        return instance;
    }

    /**
     * Ajoute un écouteur d'événements
     */
    public void ajouterEcouteur(SiegeEventListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Supprime un écouteur d'événements
     */
    public void supprimerEcouteur(SiegeEventListener listener) {
        listeners.remove(listener);
    }

    /**
     * Publie un événement à tous les écouteurs
     */
    public void publierEvenement(SiegeEvent event) {
        // Créer une copie de la liste pour éviter les problèmes de concurrence
        List<SiegeEventListener> listenersCopy = new ArrayList<>(listeners);

        System.out.println("Publication d'un événement " + event.getType() + " pour le siège " + event.getSiege().getId());
        System.out.println("Nombre d'écouteurs: " + listenersCopy.size());

        // Afficher la liste des écouteurs pour le débogage
        for (int i = 0; i < listenersCopy.size(); i++) {
            SiegeEventListener listener = listenersCopy.get(i);
            System.out.println("Écouteur #" + (i+1) + ": " + listener.getClass().getName());
        }

        for (SiegeEventListener listener : listenersCopy) {
            try {
                System.out.println("Envoi de l'événement à l'écouteur: " + listener.getClass().getName());
                listener.onSiegeEvent(event);
                System.out.println("Événement envoyé avec succès à: " + listener.getClass().getName());
            } catch (Exception e) {
                System.err.println("Erreur lors de la notification de l'écouteur " + listener.getClass().getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        System.out.println("Fin de la publication de l'événement " + event.getType());
    }
}
