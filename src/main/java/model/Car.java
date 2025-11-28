package model;

import java.math.BigDecimal;

public class Car {
    private Integer carId;
    private String modelName;
    private Integer modelYear;
    private BigDecimal weight;
    private BigDecimal price;
    private Integer warehouseId;  // statt Warehouse-Objekt
    private Integer factoryId;    // statt Factory-Objekt

    // Konstruktoren
    public Car() {}

    public Car(String modelName, Integer modelYear, BigDecimal weight, BigDecimal price) {
        this.modelName = modelName;
        this.modelYear = modelYear;
        this.weight = weight;
        this.price = price;
    }

    // Getters und Setters
    public Integer getCarId() {
        return carId;
    }

    public void setCarId(Integer carId) {
        this.carId = carId;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public Integer getModelYear() {
        return modelYear;
    }

    public void setModelYear(Integer modelYear) {
        this.modelYear = modelYear;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Integer warehouseId) {
        this.warehouseId = warehouseId;
    }

    public Integer getFactoryId() {
        return factoryId;
    }

    public void setFactoryId(Integer factoryId) {
        this.factoryId = factoryId;
    }
}