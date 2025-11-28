package model;

import java.math.BigDecimal;
import java.util.Date;

public class Order {
    private Integer orderId;
    private Integer clientId;           // NEU: für JDBC
    private Client client;              // optional für komplexe Abfragen
    private Date orderDate;
    private BigDecimal totalPrice;      // NEU: Gesamtpreis
    private DeliveryMission deliveryMission;

    public Order() {}

    public Order(Integer clientId, Date orderDate, BigDecimal totalPrice) {
        this.clientId = clientId;
        this.orderDate = orderDate;
        this.totalPrice = totalPrice;
    }

    // Getters and Setters
    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }

    public Integer getClientId() { return clientId; }
    public void setClientId(Integer clientId) { this.clientId = clientId; }

    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }

    public Date getOrderDate() { return orderDate; }
    public void setOrderDate(Date orderDate) { this.orderDate = orderDate; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }

    public DeliveryMission getDeliveryMission() { return deliveryMission; }
    public void setDeliveryMission(DeliveryMission deliveryMission) { this.deliveryMission = deliveryMission; }
}