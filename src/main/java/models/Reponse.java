package models;

import java.util.Date;

public class Reponse {
    private int id;
    private String contenu;
    private int reclamationId;
    private int adminId;
    private Date date;
    private Reclamation reclamation;

    public Reponse() {
    }

    public Reponse(int id, String contenu, int reclamationId, int adminId , Date date) {
        this.id = id;
        this.contenu = contenu;
        this.reclamationId = reclamationId;
        this.adminId = adminId;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public int getReclamationId() {
        return reclamationId;
    }

    public void setReclamationId(int reclamationId) {
        this.reclamationId = reclamationId;
    }

    public int getAdminId() {
        return adminId;
    }

    public void setAdminId(int adminId) {
        this.adminId = adminId;
    }


    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Reclamation getReclamation() {
        return reclamation;
    }

    public void setReclamation(Reclamation reclamation) {
        this.reclamation = reclamation;
    }

    @Override
    public String toString() {
        return "Reponse{" +
                "id=" + id +
                ", contenu='" + contenu + '\'' +
                ", reclamationId=" + reclamationId +
                ", adminId=" + adminId + '\'' +
                ", date=" + date +
                '}';
    }
}
