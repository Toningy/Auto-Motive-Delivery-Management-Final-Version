package hk.edu.polyu.automotive_delivery.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "car")
public class Car {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "car_id")
    private Integer carId;
    
    @Column(name = "model_name", length = 50)
    private String modelName;
    
    // CHANGED: year -> modelYear
    @Column(name = "model_year")
    private Integer modelYear;
    
    @Column(name = "weight", precision = 10, scale = 2)
    private BigDecimal weight;
    
    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;
    
    @ManyToOne
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;
    
    @ManyToOne
    @JoinColumn(name = "factory_id")
    private Factory factory;
    
    public Car() {}
    
    // CHANGED: year -> modelYear in constructor
    public Car(String modelName, Integer modelYear, BigDecimal weight, BigDecimal price) {
        this.modelName = modelName;
        this.modelYear = modelYear;
        this.weight = weight;
        this.price = price;
    }
    
    // Getters and Setters
    public Integer getCarId() { return carId; }
    public void setCarId(Integer carId) { this.carId = carId; }
    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }
    
    // CHANGED: getYear -> getModelYear
    public Integer getModelYear() { return modelYear; }
    
    // CHANGED: setYear -> setModelYear
    public void setModelYear(Integer modelYear) { this.modelYear = modelYear; }
    
    public BigDecimal getWeight() { return weight; }
    public void setWeight(BigDecimal weight) { this.weight = weight; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Warehouse getWarehouse() { return warehouse; }
    public void setWarehouse(Warehouse warehouse) { this.warehouse = warehouse; }
    public Factory getFactory() { return factory; }
    public void setFactory(Factory factory) { this.factory = factory; }
}