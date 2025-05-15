package enums;

/**
 * Énumération des différents statuts possibles pour un utilisateur
 */
public enum UserStatus {
    /**
     * Utilisateur actif avec accès complet au système
     */
    ACTIF,
    
    /**
     * Utilisateur temporairement suspendu (time-out)
     * L'accès sera automatiquement rétabli après la période de suspension
     */
    SUSPENDU,
    
    /**
     * Utilisateur banni de façon permanente
     * L'accès ne peut être rétabli que manuellement par un administrateur
     */
    BANNI
}