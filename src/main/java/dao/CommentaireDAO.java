package dao;

import models.Commentaire;
import java.util.List;

public interface CommentaireDAO extends GenericDAO<Commentaire, Integer> {
    List<Commentaire> lireParPublication(Integer publicationId);
    List<Commentaire> lireParUtilisateur(Integer utilisateurId);
} 