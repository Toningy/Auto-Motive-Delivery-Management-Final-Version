package hk.edu.polyu.automotive_delivery.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "order_basket")
public class OrderBasket {
    @EmbeddedId
    private OrderBasketId id;
    
    @ManyToOne
    @MapsId("orderId")
    @JoinColumn(name = "order_id")
    private Order order;
    
    @ManyToOne
    @MapsId("carId")
    @JoinColumn(name = "car_id")
    private Car car;
    
    public OrderBasket() {}
    
    public OrderBasket(Order order, Car car) {
        this.id = new OrderBasketId(order.getOrderId(), car.getCarId());
        this.order = order;
        this.car = car;
    }
    
    // Getters and Setters
    public OrderBasketId getId() { return id; }
    public void setId(OrderBasketId id) { this.id = id; }
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
    public Car getCar() { return car; }
    public void setCar(Car car) { this.car = car; }
}