package hk.edu.polyu.automotivedelivery.model;


public class Warehouse {
    private Integer warehouseId;
    
    private Integer capacity;
    
    public Warehouse() {}
    
    public Warehouse(Integer capacity) {
        this.capacity = capacity;
    }
    
    // Getters and Setters
    public Integer getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Integer warehouseId) { this.warehouseId = warehouseId; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
}