package dao;

import models.Publication;
import java.util.List;

public interface PublicationDAO extends GenericDAO<Publication, Integer> {
    List<Publication> lireParUtilisateur(Integer utilisateurId);
} 