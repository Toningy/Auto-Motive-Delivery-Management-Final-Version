package hk.edu.polyu.automotive_delivery.entity;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "`order`")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Integer orderId;
    
    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;
    
    @Column(name = "order_date")
    @Temporal(TemporalType.DATE)
    private Date orderDate;
    
    @OneToOne
    @JoinColumn(name = "mission_id")
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