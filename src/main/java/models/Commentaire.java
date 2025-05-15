package models;

import javafx.beans.property.*;
import java.util.Date;

public class Commentaire {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty contenu = new SimpleStringProperty();
    private final IntegerProperty note = new SimpleIntegerProperty();
    private final ObjectProperty<Date> date = new SimpleObjectProperty<>();
    private final ObjectProperty<Publication> publication = new SimpleObjectProperty<>();
    private static final int auteur = 1; // Utilisateur statique pour le développement

    public Commentaire() {
        this.date.set(new Date()); // Set current date by default
    }

    public Commentaire(String contenu, int note, Publication publication) {
        this();
        this.contenu.set(contenu);
        this.note.set(note);
        this.publication.set(publication);
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

    // Note
    public int getNote() {
        return note.get();
    }

    public void setNote(int note) {
        this.note.set(note);
    }

    public IntegerProperty noteProperty() {
        return note;
    }

    // Date
    public Date getDate() {
        return date.get();
    }

    public void setDate(Date date) {
        this.date.set(date);
    }

    public ObjectProperty<Date> dateProperty() {
        return date;
    }

    // Publication
    public Publication getPublication() {
        return publication.get();
    }

    public void setPublication(Publication publication) {
        this.publication.set(publication);
    }

    public ObjectProperty<Publication> publicationProperty() {
        return publication;
    }

    // Auteur (static)
    public static int getAuteur() {
        return auteur;
    }
}
