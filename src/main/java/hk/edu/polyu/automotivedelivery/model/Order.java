package hk.edu.polyu.automotivedelivery.model;

import java.math.BigDecimal;
import java.util.Date;

public class Order {
    private Integer orderId;
    private Integer clientId;
    private Date orderDate;
    private BigDecimal totalAmount;
    private String status;

    public Order() {}

    public Order(Integer clientId, Date orderDate, BigDecimal totalAmount) {
        this.clientId = clientId;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
        this.status = "PENDING";
    }

    // Getters and Setters
    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }
    public Integer getClientId() { return clientId; }
    public void setClientId(Integer clientId) { this.clientId = clientId; }
    public Date getOrderDate() { return orderDate; }
    public void setOrderDate(Date orderDate) { this.orderDate = orderDate; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}