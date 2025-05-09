package models;

import javafx.beans.property.*;
import java.util.Date;

public class Publication {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty titre = new SimpleStringProperty();
    private final StringProperty contenu = new SimpleStringProperty();
    private final ObjectProperty<Date> datePublication = new SimpleObjectProperty<>();
    private static final int auteur = 2; // Utilisateur statique pour le développement

    public Publication() {
        this.datePublication.set(new Date()); // Set current date by default
    }

    public Publication(String titre, String contenu) {
        this();
        this.titre.set(titre);
        this.contenu.set(contenu);
    }

    // ID
    public int getId() {
        return id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public IntegerProperty idProperty() {
        return id;
    }

    // Titre
    public String getTitre() {
        return titre.get();
    }

    public void setTitre(String titre) {
        this.titre.set(titre);
    }

    public StringProperty titreProperty() {
        return titre;
    }

    // Contenu
    public String getContenu() {
        return contenu.get();
    }

    public void setContenu(String contenu) {
        this.contenu.set(contenu);
    }

    public StringProperty contenuProperty() {
        return contenu;
    }

    // Date Publication
    public Date getDatePublication() {
        return datePublication.get();
    }

    public void setDatePublication(Date datePublication) {
        this.datePublication.set(datePublication);
    }

    public ObjectProperty<Date> datePublicationProperty() {
        return datePublication;
    }

    // Auteur (static)
    public static int getAuteur() {
        return auteur;
    }
}
