package enums;

public enum Role {
    ADMIN,
    COACH,
    JOUEUR,
    SPECTATEUR;

    // Override toString to return a value compatible with the database
    @Override
    public String toString() {
        // Return the name of the enum constant
        return this.name();
    }

    public void set(String role) {
    }

    public String get() {
        return null;
    }
}
