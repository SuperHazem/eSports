package utils;

import models.Utilisateur;

public class UserSession {
    private static UserSession instance;
    private Utilisateur currentUser;

    private UserSession() {
        // Private constructor to enforce singleton pattern
    }

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void setUser(Utilisateur user) {
        this.currentUser = user;
    }

    public Utilisateur getUser() {
        return currentUser;
    }
    
    public Utilisateur getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public void cleanUserSession() {
        this.currentUser = null;
    }
}