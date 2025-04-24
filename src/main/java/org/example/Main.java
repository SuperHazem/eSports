package org.example;

import dao.EquipeDAO;
import dao.UtilisateurDAO;
import enums.Role;
import models.Equipe;
import models.Utilisateur;

import utils.DatabaseConnection;

import java.sql.*;

public class Main {
    public static void main(String[] args) {
        try {
            // Initialize DAO
            UtilisateurDAO utilisateurDAO = new UtilisateurDAO();

            // Create a new user
            Utilisateur nouvelUtilisateur = new Utilisateur(
                    0, // ID (auto-generated)
                    "hazem@example.com", // Email
                    "hashedPassword", // Password hash
                    Role.ADMIN // Role
            );

            // Add the user to the database
            utilisateurDAO.ajouter(nouvelUtilisateur);


            EquipeDAO equipeDAO = new EquipeDAO();

            // Create a new team
            Equipe nouvelleEquipe = new Equipe(0, "Les Dragons");
            equipeDAO.ajouter(nouvelleEquipe);

            System.out.println("Team added successfully! ID: " + nouvelleEquipe.getId());

            // Retrieve the user by ID
            Utilisateur utilisateur = utilisateurDAO.lire(1);
            System.out.println("User Found: " + utilisateur.getEmail());

            // Update the user
            utilisateur.setEmail("updated@example.com");
            utilisateurDAO.modifier(utilisateur);

            // Delete the user
            utilisateurDAO.supprimer(1);

            // Retrieve all users
            System.out.println("All Users:");
            utilisateurDAO.lireTous().forEach(u -> System.out.println(u.getEmail()));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}