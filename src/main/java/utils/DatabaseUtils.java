package utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import models.Spectateur;
import enums.Role;

/**
 * Utility class for direct database operations
 * This bypasses the DAO layer for special cases where we need custom handling
 */
public class DatabaseUtils {

    /**
     * Directly inserts a Google-authenticated user into the database
     * This method uses raw SQL to avoid issues with the Role enum
     */
    public static void insertGoogleUser(Connection connection, String email, String firstName, String lastName) throws SQLException {
        if (connection == null) {
            throw new SQLException("Database connection is null");
        }

        // First, check if the user already exists
        String checkSql = "SELECT id FROM utilisateur WHERE email = ?";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
            checkStmt.setString(1, email);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                // User already exists, no need to insert
                System.out.println("User already exists in database: " + email);
                return;
            }
        }

        // Insert into utilisateur table with hardcoded role value
        String userSql = "INSERT INTO utilisateur (role, motDePasseHash, email, id_equipe, nom, prenom) VALUES (?, ?, ?, ?, ?, ?)";
        int userId = -1;

        try (PreparedStatement userStmt = connection.prepareStatement(userSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            // Try different role values that might be compatible with the database
            // First try lowercase
            userStmt.setString(1, "spectateur");
            userStmt.setString(2, "google-oauth");
            userStmt.setString(3, email);
            userStmt.setInt(4, 0); // Default team ID
            userStmt.setString(5, lastName);
            userStmt.setString(6, firstName);

            int affectedRows = userStmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }

            try (ResultSet generatedKeys = userStmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    userId = generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
        }

        // Insert into spectateur table
        String spectateurSql = "INSERT INTO spectateur (id_utilisateur, date_inscription) VALUES (?, ?)";
        try (PreparedStatement spectateurStmt = connection.prepareStatement(spectateurSql)) {
            spectateurStmt.setInt(1, userId);
            spectateurStmt.setDate(2, new java.sql.Date(new Date().getTime()));

            spectateurStmt.executeUpdate();
        }

        System.out.println("Successfully inserted Google user: " + email + " with ID: " + userId);
    }
}
