package model;


public class DeliveryVehicle {
    private Integer vehicleId;
    
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