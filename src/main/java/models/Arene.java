package models;

public class Arene {
    private int areneId;
    private String name;
    private String location;
    private int capacity;

    public Arene() {
    }

    public Arene(int areneId, String name, int capacity, String location) {
        this.areneId = areneId;
        this.name = name;
        this.capacity = capacity;
        this.location = location;
    }

    public int getAreneId() { return areneId; }
    public void setAreneId(int areneId) { this.areneId = areneId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
}