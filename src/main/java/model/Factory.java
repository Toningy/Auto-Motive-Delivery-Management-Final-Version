package hk.edu.polyu.automotive_delivery.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "factory")
public class Factory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "factory_id")
    private Integer factoryId;
    
    @Column(name = "location", length = 255)
    private String location;
    
    public Factory() {}
    
    public Factory(String location) {
        this.location = location;
    }
    
    // Getters and Setters
    public Integer getFactoryId() { return factoryId; }
    public void setFactoryId(Integer factoryId) { this.factoryId = factoryId; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}