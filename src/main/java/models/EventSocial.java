package models;

import java.time.LocalDate;

public class EventSocial {
    private Integer id;
    private String nom;
    private LocalDate date;
    private String lieu;
    private String description;
    private int capacite;

    public EventSocial() {
    }

    public EventSocial(String nom, LocalDate date, String lieu, String description, int capacite) {
        this.nom = nom;
        this.date = date;
        this.lieu = lieu;
        this.description = description;
        this.capacite = capacite;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getLieu() {
        return lieu;
    }

    public void setLieu(String lieu) {
        this.lieu = lieu;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCapacite() {
        return capacite;
    }

    public void setCapacite(int capacite) {
        this.capacite = capacite;
    }

    @Override
    public String toString() {
        return nom;
    }
}
