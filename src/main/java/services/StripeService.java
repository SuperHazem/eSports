package services;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import models.Ticket;

import java.util.HashMap;
import java.util.Map;

public class StripeService {

    // Clé API Stripe mise à jour avec votre clé secrète
    private static final String API_KEY = "sk_test_51RNDkZFPzhRviT48jNouwyG7zLIAoQsljBhY3p3zSGSLAa8vh4pKuMuwuwSFt2HNrOLB1njmrKOnG4ZJwEOlEp9X00Yv9XZl56";

    static {
        Stripe.apiKey = API_KEY;
    }

    /**
     * Crée une intention de paiement pour un ticket
     * @param ticket Le ticket à payer
     * @return L'ID du client secret pour le paiement côté client
     */
    public static String creerIntentionPaiement(Ticket ticket) throws StripeException {
        // Convertir le prix en centimes (Stripe utilise les centimes)
        long montantEnCentimes = Math.round(ticket.getPrix() * 100);

        // Utiliser Map au lieu de PaymentIntentCreateParams.Builder pour la compatibilité
        Map<String, Object> params = new HashMap<>();
        params.put("amount", montantEnCentimes);
        params.put("currency", "eur");
        params.put("description", "Ticket pour siège " + ticket.getSiege());

        // Ajouter les métadonnées
        Map<String, String> metadata = new HashMap<>();
        metadata.put("ticket_id", String.valueOf(ticket.getId()));
        params.put("metadata", metadata);

        // Ajouter les méthodes de paiement acceptées
        params.put("payment_method_types", new String[]{"card"});

        // Créer l'intention de paiement
        PaymentIntent paymentIntent = PaymentIntent.create(params);
        return paymentIntent.getClientSecret();
    }

    /**
     * Vérifie le statut d'un paiement
     * @param paymentIntentId L'ID de l'intention de paiement
     * @return true si le paiement est réussi, false sinon
     */
    public static boolean verifierPaiement(String paymentIntentId) throws StripeException {
        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
        return "succeeded".equals(paymentIntent.getStatus());
    }
}