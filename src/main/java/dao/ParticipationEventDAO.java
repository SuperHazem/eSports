package dao;

import models.ParticipationEvent;
import models.Utilisateur;
import models.EventSocial;
import java.util.List;

public interface ParticipationEventDAO extends GenericDAO<ParticipationEvent, Integer> {
    List<ParticipationEvent> lireParUtilisateur(Utilisateur utilisateur);
    List<ParticipationEvent> lireParEvent(EventSocial event);
    boolean existeParticipation(Utilisateur utilisateur, EventSocial event);
    int nombreParticipants(EventSocial event);
} 