package dao;

import models.PublicationInteraction;
import models.Utilisateur;
import models.Publication;
import java.util.List;

public interface PublicationInteractionDAO extends GenericDAO<PublicationInteraction, Integer> {
    List<PublicationInteraction> lireParPublication(Publication publication);
    List<PublicationInteraction> lireParUtilisateur(Utilisateur utilisateur);
    boolean existeInteraction(Utilisateur utilisateur, Publication publication);
    int nombreLikes(Publication publication);
    int nombreDislikes(Publication publication);
    void supprimerInteraction(Utilisateur utilisateur, Publication publication);
} 