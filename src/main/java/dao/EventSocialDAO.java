package dao;

import models.EventSocial;
import java.util.List;
import java.time.LocalDate;

public interface EventSocialDAO extends GenericDAO<EventSocial, Integer> {
    List<EventSocial> lireParNom(String nom);
    List<EventSocial> lireParDate(LocalDate date);
    List<EventSocial> lireParLieu(String lieu);
} 