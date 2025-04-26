package dao;

import enums.TypeRecompense;
import models.Recompense;

import java.util.List;

public interface RecompenseDAO extends GenericDAO<Recompense, Integer> {
    // Méthode supplémentaire spécifique aux récompenses
    List<Recompense> lireParType(TypeRecompense type); // Récupérer toutes les récompenses d'un certain type
}