package utils;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import models.Ticket;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PDFGenerator {

    public static void genererRapportTickets(List<Ticket> tickets, String cheminFichier) throws Exception {
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(cheminFichier));
        document.open();

        // Ajouter un titre
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
        Paragraph title = new Paragraph("Rapport des Tickets", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(Chunk.NEWLINE);

        // Ajouter la date du rapport
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 12);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Paragraph dateParagraph = new Paragraph("Date du rapport: " + dateFormat.format(new Date()), normalFont);
        document.add(dateParagraph);
        document.add(Chunk.NEWLINE);

        // Créer le tableau
        PdfPTable table = new PdfPTable(5); // 5 colonnes
        table.setWidthPercentage(100);

        // Définir les largeurs relatives des colonnes
        float[] columnWidths = {1f, 1.5f, 1.5f, 2f, 2f};
        table.setWidths(columnWidths);

        // En-têtes du tableau
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE);
        String[] headers = {"ID", "Siège", "Prix (dt)", "Date d'achat", "Statut"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBackgroundColor(new BaseColor(52, 152, 219)); // Bleu
            cell.setPadding(5);
            table.addCell(cell);
        }

        // Données du tableau
        SimpleDateFormat ticketDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Font cellFont = new Font(Font.FontFamily.HELVETICA, 10);

        for (Ticket ticket : tickets) {
            // ID
            PdfPCell cell = new PdfPCell(new Phrase(String.valueOf(ticket.getId()), cellFont));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);

            // Siège
            cell = new PdfPCell(new Phrase(ticket.getSiege(), cellFont));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);

            // Prix
            cell = new PdfPCell(new Phrase(String.format("%.2f", ticket.getPrix()), cellFont));
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(cell);

            // Date d'achat
            cell = new PdfPCell(new Phrase(ticketDateFormat.format(ticket.getDateAchat()), cellFont));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);

            // Statut
            String statut = ticket.getStatutPaiement();
            Font statutFont = new Font(Font.FontFamily.HELVETICA, 10);
            if ("Payé".equals(statut)) {
                statutFont.setColor(new BaseColor(46, 204, 113)); // Vert
            } else {
                statutFont.setColor(new BaseColor(231, 76, 60)); // Rouge
            }

            cell = new PdfPCell(new Phrase(statut, statutFont));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }

        document.add(table);
        document.add(Chunk.NEWLINE);

        // Ajouter un résumé
        int totalTickets = tickets.size();
        int ticketsPayes = 0;
        double montantTotal = 0;

        for (Ticket ticket : tickets) {
            if ("Payé".equals(ticket.getStatutPaiement())) {
                ticketsPayes++;
            }
            montantTotal += ticket.getPrix();
        }

        int ticketsNonPayes = totalTickets - ticketsPayes;

        Font summaryFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
        document.add(new Paragraph("Résumé:", summaryFont));
        document.add(new Paragraph("Nombre total de tickets: " + totalTickets, normalFont));
        document.add(new Paragraph("Tickets payés: " + ticketsPayes, normalFont));
        document.add(new Paragraph("Tickets non payés: " + ticketsNonPayes, normalFont));
        document.add(new Paragraph("Montant total: " + String.format("%.2f dt", montantTotal), normalFont));

        document.close();
    }
}