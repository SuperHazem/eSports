package utils.validators;

import models.EventSocial;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EventSocialValidator {
    public static List<String> validateEvent(EventSocial event) {
        List<String> errors = new ArrayList<>();

        // Validation du nom
        if (event.getNom() == null || event.getNom().trim().isEmpty()) {
            errors.add("Le nom de l'événement est obligatoire");
        } else if (event.getNom().length() < 3) {
            errors.add("Le nom de l'événement doit contenir au moins 3 caractères");
        } else if (event.getNom().length() > 100) {
            errors.add("Le nom de l'événement ne doit pas dépasser 100 caractères");
        }

        // Validation de la date
        if (event.getDate() == null) {
            errors.add("La date de l'événement est obligatoire");
        } else if (event.getDate().isBefore(LocalDate.now())) {
            errors.add("La date de l'événement doit être dans le futur");
        } else if (event.getDate().isAfter(LocalDate.now().plusYears(1))) {
            errors.add("La date de l'événement ne peut pas être plus d'un an dans le futur");
        }

        // Validation du lieu
        if (event.getLieu() == null || event.getLieu().trim().isEmpty()) {
            errors.add("Le lieu de l'événement est obligatoire");
        } else if (!event.getLieu().contains(" - ")) {
            errors.add("Le format du lieu doit être 'Gouvernorat - Ville'");
        }

        // Validation de la description
        if (event.getDescription() == null || event.getDescription().trim().isEmpty()) {
            errors.add("La description de l'événement est obligatoire");
        } else if (event.getDescription().length() < 10) {
            errors.add("La description doit contenir au moins 10 caractères");
        } else if (event.getDescription().length() > 500) {
            errors.add("La description ne doit pas dépasser 500 caractères");
        }

        // Validation de la capacité
        if (event.getCapacite() <= 0) {
            errors.add("La capacité de l'événement doit être supérieure à 0");
        } else if (event.getCapacite() > 1000) {
            errors.add("La capacité de l'événement ne doit pas dépasser 1000 personnes");
        }

        return errors;
    }
} 