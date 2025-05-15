package models;

import java.time.LocalDateTime;

public class ParticipationEvent {
    private Integer id;
    private Utilisateur utilisateur;
    private EventSocial event;
    private LocalDateTime dateParticipation;
    private boolean confirme;

    public ParticipationEvent() {
    }

    public ParticipationEvent(Utilisateur utilisateur, EventSocial event) {
        this.utilisateur = utilisateur;
        this.event = event;
        this.dateParticipation = LocalDateTime.now();
        this.confirme = false;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public EventSocial getEvent() {
        return event;
    }

    public void setEvent(EventSocial event) {
        this.event = event;
    }

    public LocalDateTime getDateParticipation() {
        return dateParticipation;
    }

    public void setDateParticipation(LocalDateTime dateParticipation) {
        this.dateParticipation = dateParticipation;
    }

    public boolean isConfirme() {
        return confirme;
    }

    public void setConfirme(boolean confirme) {
        this.confirme = confirme;
    }

    @Override
    public String toString() {
        return "ParticipationEvent{" +
                "id=" + id +
                ", utilisateur=" + utilisateur.getNom() + " " + utilisateur.getPrenom() +
                ", event=" + event.getNom() +
                ", dateParticipation=" + dateParticipation +
                ", confirme=" + confirme +
                '}';
    }
} 