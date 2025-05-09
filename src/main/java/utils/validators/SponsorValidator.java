package utils.validators;

import models.Sponsor;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class SponsorValidator {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9]{8,15}$");

    public static List<String> validateSponsor(Sponsor sponsor) {
        List<String> errors = new ArrayList<>();

        // Validate first name
        if (sponsor.getFname() == null || sponsor.getFname().trim().isEmpty()) {
            errors.add("Le prénom du sponsor est obligatoire");
        } else if (sponsor.getFname().length() > 50) {
            errors.add("Le prénom du sponsor ne doit pas dépasser 50 caractères");
        }

        // Validate last name
        if (sponsor.getLname() == null || sponsor.getLname().trim().isEmpty()) {
            errors.add("Le nom du sponsor est obligatoire");
        } else if (sponsor.getLname().length() > 50) {
            errors.add("Le nom du sponsor ne doit pas dépasser 50 caractères");
        }

        // Validate address
        if (sponsor.getAddress() == null || sponsor.getAddress().trim().isEmpty()) {
            errors.add("L'adresse du sponsor est obligatoire");
        } else if (sponsor.getAddress().length() > 255) {
            errors.add("L'adresse du sponsor ne doit pas dépasser 255 caractères");
        }

        // Validate email
        if (sponsor.getEmail() == null || sponsor.getEmail().trim().isEmpty()) {
            errors.add("L'email du sponsor est obligatoire");
        } else if (!EMAIL_PATTERN.matcher(sponsor.getEmail()).matches()) {
            errors.add("L'email du sponsor n'est pas valide");
        }

        // Validate phone
        if (sponsor.getPhone() == null || sponsor.getPhone().trim().isEmpty()) {
            errors.add("Le numéro de téléphone du sponsor est obligatoire");
        } else if (!PHONE_PATTERN.matcher(sponsor.getPhone()).matches()) {
            errors.add("Le numéro de téléphone du sponsor n'est pas valide");
        }

        // Validate amount
        if (sponsor.getMontant() <= 0) {
            errors.add("Le montant du sponsor doit être supérieur à 0");
        } else if (sponsor.getMontant() > 1000000) {
            errors.add("Le montant du sponsor ne doit pas dépasser 1,000,000");
        }

        return errors;
    }
} 