package models;

public class Sponsor {
    int id ;

    String fname ;
    String lname ;
    String address ;
    String email ;
    String phone ;
    double montant;

    public Sponsor() {}

    public Sponsor(int id, String fname, String lname, String address, String email, String phone, double montant) {
        this.id = id;
        this.fname = fname;
        this.lname = lname;
        this.address = address;
        this.email = email;
        this.phone = phone;
        this.montant = montant;
    }

    public      Sponsor(String fname, String lname, String address, String email, String phone, double montant) {

        this.fname = fname;
        this.lname = lname;
        this.address = address;
        this.email = email;
        this.phone = phone;
        this.montant = montant;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public  String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getLname() {
        return lname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }

    @Override
    public String toString() {
        return "Sponsor{" +
                "id=" + id +
                ", fname='" + fname + '\'' +
                ", lname='" + lname + '\'' +
                ", address='" + address + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", montant=" + montant +
                '}';
    }
}
