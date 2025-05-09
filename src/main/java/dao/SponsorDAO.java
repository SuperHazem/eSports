package dao;

import models.Sponsor;
import java.util.List;

public interface SponsorDAO extends GenericDAO<Sponsor, Integer> {
    // Additional methods specific to Sponsor if needed
    List<Sponsor> lireParMontant(double montantMin);
    List<Sponsor> lireParNom(String nom);
} 