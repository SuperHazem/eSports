package dao;

import java.util.List;

public interface GenericDAO<T, ID> {
    // CREATE: Add a new entity
    void ajouter(T entity);

    // READ: Retrieve an entity by its ID
    T lire(ID id);

    // READ ALL: Retrieve all entities
    List<T> lireTous();

    // UPDATE: Modify an existing entity
    void modifier(T entity);

    // DELETE: Remove an entity by its ID
    void supprimer(ID id);
}