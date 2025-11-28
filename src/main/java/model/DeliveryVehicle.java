package hk.edu.polyu.automotive_delivery.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "delivery_vehicle")
public class DeliveryVehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vehicle_id")
    private Integer vehicleId;
    
    @Column(name = "plate_number", length = 50)
    private String plateNumber;
    
    public DeliveryVehicle() {}
    
    public DeliveryVehicle(String plateNumber) {
        this.plateNumber = plateNumber;
    }
    
    // Getters and Setters
    public Integer getVehicleId() { return vehicleId; }
    public void setVehicleId(Integer vehicleId) { this.vehicleId = vehicleId; }
    public String getPlateNumber() { return plateNumber; }
    public void setPlateNumber(String plateNumber) { this.plateNumber = plateNumber; }
}