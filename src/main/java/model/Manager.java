package hk.edu.polyu.automotive_delivery.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "manager")
public class Manager {
    @Id
    @Column(name = "staff_id")
    private Integer staffId;
    
    @OneToOne
    @MapsId
    @JoinColumn(name = "staff_id")
    private Staff staff;
    
    @ManyToOne
    @JoinColumn(name = "factory_id")
    private Factory factory;
    
    @ManyToOne
    @JoinColumn(name = "warehouse_id")
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