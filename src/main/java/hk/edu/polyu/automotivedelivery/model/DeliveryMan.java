package hk.edu.polyu.automotivedelivery.model;


public class DeliveryMan {
    private Integer staffId;
    
    private Staff staff;
    
    private String licenseNumber;
    
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