package dao;

import models.Match;

import java.util.List;

public class MatchDAO implements GenericDAO<Match,Integer>{
    @Override
    public void ajouter(Match entity) {

    }

    @Override
    public Match lire(Integer integer) {
        return null;
    }

    @Override
    public List<Match> lireTous() {
        return List.of();
    }

    @Override
    public void modifier(Match entity) {

    }

    @Override
    public void supprimer(Integer integer) {

    }
}
