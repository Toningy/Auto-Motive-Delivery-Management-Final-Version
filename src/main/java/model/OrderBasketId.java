package hk.edu.polyu.automotive_delivery.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class OrderBasketId implements Serializable {
    private Integer orderId;
    private Integer carId;
    
    public OrderBasketId() {}
    
    public OrderBasketId(Integer orderId, Integer carId) {
        this.orderId = orderId;
        this.carId = carId;
    }
    
    // Getters and Setters
    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }
    public Integer getCarId() { return carId; }
    public void setCarId(Integer carId) { this.carId = carId; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderBasketId that = (OrderBasketId) o;
        return Objects.equals(orderId, that.orderId) && Objects.equals(carId, that.carId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(orderId, carId);
    }
}