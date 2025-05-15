package dao;

import models.PublicationReport;
import models.Utilisateur;
import models.Publication;
import java.util.List;

public interface PublicationReportDAO extends GenericDAO<PublicationReport, Integer> {
    List<PublicationReport> lireParPublication(Publication publication);
    List<PublicationReport> lireParUtilisateur(Utilisateur utilisateur);
    List<PublicationReport> lireParStatut(PublicationReport.ReportStatus statut);
    boolean existeReport(Utilisateur utilisateur, Publication publication);
    void mettreAJourStatut(Integer reportId, PublicationReport.ReportStatus nouveauStatut);
} 