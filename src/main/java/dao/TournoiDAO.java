package dao;

import models.Tournoi;

import java.util.List;

public class TournoiDAO implements GenericDAO<Tournoi,Integer>{
    @Override
    public void ajouter(Tournoi entity) {

    }

    @Override
    public Tournoi lire(Integer integer) {
        return null;
    }

    @Override
    public List<Tournoi> lireTous() {
        return List.of();
    }

    @Override
    public void modifier(Tournoi entity) {

    }

    @Override
    public void supprimer(Integer integer) {

    }
}
