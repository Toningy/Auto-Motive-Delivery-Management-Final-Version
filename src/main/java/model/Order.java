package model;

import java.util.Date;

public class Order {
    private Integer orderId;
    
    private Client client;
    
    private Date orderDate;
    
    private DeliveryMission deliveryMission;
    
    public Order() {}
    
    public Order(Client client, Date orderDate) {
        this.client = client;
        this.orderDate = orderDate;
    }
    
    // Getters and Setters
    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }
    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }
    public Date getOrderDate() { return orderDate; }
    public void setOrderDate(Date orderDate) { this.orderDate = orderDate; }
    public DeliveryMission getDeliveryMission() { return deliveryMission; }
    public void setDeliveryMission(DeliveryMission deliveryMission) { this.deliveryMission = deliveryMission; }
}