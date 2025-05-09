package utils.validators;

import models.EventSocial;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EventSocialValidator {
    public static List<String> validateEvent(EventSocial event) {
        List<String> errors = new ArrayList<>();

        // Validate name
        if (event.getNom() == null || event.getNom().trim().isEmpty()) {
            errors.add("Le nom de l'événement est obligatoire");
        } else if (event.getNom().length() > 100) {
            errors.add("Le nom de l'événement ne doit pas dépasser 100 caractères");
        }

        // Validate date
        if (event.getDate() == null) {
            errors.add("La date de l'événement est obligatoire");
        } else if (event.getDate().isBefore(LocalDate.now())) {
            errors.add("La date de l'événement doit être dans le futur");
        }

        // Validate location
        if (event.getLieu() == null || event.getLieu().trim().isEmpty()) {
            errors.add("Le lieu de l'événement est obligatoire");
        } else if (event.getLieu().length() > 255) {
            errors.add("Le lieu de l'événement ne doit pas dépasser 255 caractères");
        }

        // Validate description
        if (event.getDescription() == null || event.getDescription().trim().isEmpty()) {
            errors.add("La description de l'événement est obligatoire");
        }

        // Validate capacity
        if (event.getCapacite() <= 0) {
            errors.add("La capacité de l'événement doit être supérieure à 0");
        } else if (event.getCapacite() > 1000) {
            errors.add("La capacité de l'événement ne doit pas dépasser 1000 personnes");
        }

        return errors;
    }
} 