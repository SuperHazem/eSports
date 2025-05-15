package models;

import java.time.LocalDateTime;

public class PublicationInteraction {
    private Integer id;
    private Publication publication;
    private Utilisateur utilisateur;
    private InteractionType typeInteraction;
    private LocalDateTime dateInteraction;

    public enum InteractionType {
        LIKE,
        DISLIKE
    }

    public PublicationInteraction() {
    }

    public PublicationInteraction(Publication publication, Utilisateur utilisateur, InteractionType typeInteraction) {
        this.publication = publication;
        this.utilisateur = utilisateur;
        this.typeInteraction = typeInteraction;
        this.dateInteraction = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Publication getPublication() {
        return publication;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public InteractionType getTypeInteraction() {
        return typeInteraction;
    }

    public void setTypeInteraction(InteractionType typeInteraction) {
        this.typeInteraction = typeInteraction;
    }

    public LocalDateTime getDateInteraction() {
        return dateInteraction;
    }

    public void setDateInteraction(LocalDateTime dateInteraction) {
        this.dateInteraction = dateInteraction;
    }
} 