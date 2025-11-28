package hk.edu.polyu.automotive_delivery.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "delivery_man")
public class DeliveryMan {
    @Id
    @Column(name = "staff_id")
    private Integer staffId;
    
    @OneToOne
    @MapsId
    @JoinColumn(name = "staff_id")
    private Staff staff;
    
    @Column(name = "license_number", length = 50)
    private String licenseNumber;
    
    @OneToOne
    @JoinColumn(name = "vehicle_id")
    private DeliveryVehicle deliveryVehicle;
    
    public DeliveryMan() {}
    
    public DeliveryMan(Staff staff, String licenseNumber, DeliveryVehicle deliveryVehicle) {
        this.staff = staff;
        this.licenseNumber = licenseNumber;
        this.deliveryVehicle = deliveryVehicle;
    }
    
    // Getters and Setters
    public Integer getStaffId() { return staffId; }
    public void setStaffId(Integer staffId) { this.staffId = staffId; }
    public Staff getStaff() { return staff; }
    public void setStaff(Staff staff) { this.staff = staff; }
    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }
    public DeliveryVehicle getDeliveryVehicle() { return deliveryVehicle; }
    public void setDeliveryVehicle(DeliveryVehicle deliveryVehicle) { this.deliveryVehicle = deliveryVehicle; }
}