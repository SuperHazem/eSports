package org.example;

import dao.EquipeDAO;
import dao.RecompenseDAO;
import dao.RecompenseDAOImpl;
import dao.UtilisateurDAO;
import enums.Role;
import enums.TypeRecompense;
import models.Equipe;
import models.Recompense;
import models.Utilisateur;

import utils.DatabaseConnection;

import java.sql.*;

public class Main {
    public static void main(String[] args) throws SQLException {
        try {
            // Initialize DAO
            EquipeDAO equipeDAO = new EquipeDAO();

            // Create a new team
            Equipe nouvelleEquipe = new Equipe(0, "Les Dragons");
            equipeDAO.ajouter(nouvelleEquipe);

            System.out.println("Team added successfully! ID: " + nouvelleEquipe.getId());

            // Retrieve the team by ID
            Equipe equipe = equipeDAO.lire(nouvelleEquipe.getId());
            System.out.println("Team Found: " + equipe.getNom());

            // Update the team
            equipe.setNom("Les Phoenix");
            equipeDAO.modifier(equipe);

            // Retrieve all teams
            System.out.println("All Teams:");
            equipeDAO.lireTous().forEach(e -> System.out.println(e.getNom()));

            // Delete the team
            equipeDAO.supprimer(equipe.getId());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
