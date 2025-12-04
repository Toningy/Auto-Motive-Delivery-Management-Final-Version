package hk.edu.polyu.automotivedelivery.model;


public class Manager {
    private Integer staffId;
    
    private Staff staff;
    
    private Factory factory;
    
    private Warehouse warehouse;
    
    public Manager() {}
    
    public Manager(Staff staff, Factory factory, Warehouse warehouse) {
        this.staff = staff;
        this.factory = factory;
        this.warehouse = warehouse;
    }
    
    // Getters and Setters
    public Integer getStaffId() { return staffId; }
    public void setStaffId(Integer staffId) { this.staffId = staffId; }
    public Staff getStaff() { return staff; }
    public void setStaff(Staff staff) { this.staff = staff; }
    public Factory getFactory() { return factory; }
    public void setFactory(Factory factory) { this.factory = factory; }
    public Warehouse getWarehouse() { return warehouse; }
    public void setWarehouse(Warehouse warehouse) { this.warehouse = warehouse; }
}