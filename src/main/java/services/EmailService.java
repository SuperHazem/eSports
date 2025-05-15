package services;

import models.Ticket;

import javax.mail.*;
import javax.mail.internet.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;

public class EmailService {

    // Adresse email et mot de passe d'application Gmail
    private static final String EMAIL_EXPEDITEUR = "hazemabbassi10@gmail.com";
    private static final String MOT_DE_PASSE = "lyyl odto eaad kjnf";

    /**
     * Envoie un email simple avec les informations du ticket
     * @param ticket Le ticket concerné
     * @param emailDestinataire L'adresse email du destinataire
     * @throws MessagingException en cas d'erreur d'envoi
     */
    public static void envoyerEmailTicketSimple(Ticket ticket, String emailDestinataire) throws MessagingException {
        // Configuration des propriétés SMTP pour Gmail
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        // Création de la session avec authentification
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_EXPEDITEUR, MOT_DE_PASSE);
            }
        });

        session.setDebug(true); // Affiche les logs SMTP dans la console

        // Formater la date d'achat du ticket
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String dateFormatee = dateFormat.format(ticket.getDateAchat());

        // Contenu HTML de l'email
        String htmlContent =
                "<html>" +
                        "<body style='font-family: Arial, sans-serif;'>" +
                        "<h2>Confirmation de votre ticket – eSports Arena</h2>" +
                        "<p>Bonjour,</p>" +
                        "<p>Merci pour votre inscription au tournoi en ligne eSports Arena.</p>" +
                        "<p>Voici les détails de votre ticket :</p>" +
                        "<ul>" +
                        "<li><strong>Identifiant :</strong> " + ticket.getId() + "</li>" +
                        "<li><strong>Siège :</strong> " + ticket.getSiege() + "</li>" +
                        "<li><strong>Prix :</strong> " + ticket.getPrix() + " DT</li>" +
                        "<li><strong>Date d'achat :</strong> " + dateFormatee + "</li>" +
                        "</ul>" +
                        "<p>Conservez cet email comme preuve d'inscription.</p>" +
                        "<p>Bonne chance !</p>" +
                        "<p>L’équipe eSports Arena</p>" +
                        "<footer style='font-size: 12px; color: #777; margin-top: 30px;'>" +
                        "© " + Calendar.getInstance().get(Calendar.YEAR) + " eSports Arena. Tous droits réservés." +
                        "</footer>" +
                        "</body>" +
                        "</html>";

        // Création de l'email
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(EMAIL_EXPEDITEUR));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailDestinataire));
        message.setSubject("Confirmation – eSports Arena Ticket");
        message.setContent(htmlContent, "text/html; charset=utf-8");

        // Envoi de l'email
        Transport.send(message);

        System.out.println("✅ Email simple envoyé à " + emailDestinataire);
    }
}
