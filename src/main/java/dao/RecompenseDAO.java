package dao;

import models.Recompense;
import models.Equipe;
import enums.TypeRecompense;

import java.util.List;

public interface RecompenseDAO {

    // Create: Assign reward to a team
    void ajouter(Recompense recompense);

    // Read: Retrieve by ID
    Recompense lire(int id);

    // Read: Retrieve all rewards
    List<Recompense> lireTous();

    // Read: Retrieve rewards by team
    List<Recompense> lireParEquipe(int equipeId);

    // Read: Retrieve rewards by type
    List<Recompense> lireParType(TypeRecompense type);

    // Update: Modify an existing reward
    void modifier(Recompense recompense);

    // Delete: Remove a reward
    void supprimer(int id);
}