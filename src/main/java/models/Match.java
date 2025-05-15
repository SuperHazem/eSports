package models;

import enums.StatutMatch;
import java.sql.Timestamp;

public class Match {
    private int matchId;
    private int idEquipe1;
    private int idEquipe2;
    private int idTournoi;
    private int idArene;
    private int scoreEquipe1;
    private int scoreEquipe2;
    private Integer vainqueur; // Nullable
    private Integer duree; // Nullable, in minutes
    private String nomJeu; // Nullable
    private Timestamp dateMatch;
    private StatutMatch statutMatch;
    private transient String equipe1Nom; // Transient for UI display
    private transient String equipe2Nom; // Transient for UI display
    private transient String tournoiNom; // Transient for UI display
    private transient String areneNom; // Transient for UI display

    public Match() {
    }

    public Match(int idEquipe1, int idEquipe2, int idTournoi, int idArene, int scoreEquipe1, int scoreEquipe2,
                 Integer vainqueur, Integer duree, String nomJeu, Timestamp dateMatch, StatutMatch statutMatch) {
        this.idEquipe1 = idEquipe1;
        this.idEquipe2 = idEquipe2;
        this.idTournoi = idTournoi;
        this.idArene = idArene;
        this.scoreEquipe1 = scoreEquipe1;
        this.scoreEquipe2 = scoreEquipe2;
        this.vainqueur = vainqueur;
        this.duree = duree;
        this.nomJeu = nomJeu;
        this.dateMatch = dateMatch;
        this.statutMatch = statutMatch;
    }

    public int getMatchId() { return matchId; }
    public void setMatchId(int matchId) { this.matchId = matchId; }

    public int getIdEquipe1() { return idEquipe1; }
    public void setIdEquipe1(int idEquipe1) { this.idEquipe1 = idEquipe1; }

    public int getIdEquipe2() { return idEquipe2; }
    public void setIdEquipe2(int idEquipe2) { this.idEquipe2 = idEquipe2; }

    public int getIdTournoi() { return idTournoi; }
    public void setIdTournoi(int idTournoi) { this.idTournoi = idTournoi; }

    public int getIdArene() { return idArene; }
    public void setIdArene(int idArene) { this.idArene = idArene; }

    public int getScoreEquipe1() { return scoreEquipe1; }
    public void setScoreEquipe1(int scoreEquipe1) { this.scoreEquipe1 = scoreEquipe1; }

    public int getScoreEquipe2() { return scoreEquipe2; }
    public void setScoreEquipe2(int scoreEquipe2) { this.scoreEquipe2 = scoreEquipe2; }

    public Integer getVainqueur() { return vainqueur; }
    public void setVainqueur(Integer vainqueur) { this.vainqueur = vainqueur; }

    public Integer getDuree() { return duree; }
    public void setDuree(Integer duree) { this.duree = duree; }

    public String getNomJeu() { return nomJeu; }
    public void setNomJeu(String nomJeu) { this.nomJeu = nomJeu; }

    public Timestamp getDateMatch() { return dateMatch; }
    public void setDateMatch(Timestamp dateMatch) { this.dateMatch = dateMatch; }

    public StatutMatch getStatutMatch() { return statutMatch; }
    public void setStatutMatch(StatutMatch statutMatch) { this.statutMatch = statutMatch; }

    public String getEquipe1Nom() { return equipe1Nom; }
    public void setEquipe1Nom(String equipe1Nom) { this.equipe1Nom = equipe1Nom; }

    public String getEquipe2Nom() { return equipe2Nom; }
    public void setEquipe2Nom(String equipe2Nom) { this.equipe2Nom = equipe2Nom; }

    public String getTournoiNom() { return tournoiNom; }
    public void setTournoiNom(String tournoiNom) { this.tournoiNom = tournoiNom; }

    public String getAreneNom() { return areneNom; }
    public void setAreneNom(String areneNom) { this.areneNom = areneNom; }
}