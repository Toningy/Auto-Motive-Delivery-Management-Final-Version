package hk.edu.polyu.automotive_delivery.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "client")
public class Client {
    @Id
    @Column(name = "client_id")
    private Integer clientId;
    
    @OneToOne
    @MapsId
    @JoinColumn(name = "client_id")
    private Person person;
    
    @Column(name = "shipping_address", length = 250)
    private String shippingAddress;
    
    @Column(name = "billing_address", length = 50)
    private String billingAddress;
    
    @OneToMany(mappedBy = "client")
    private List<Order> orders;
    
    @OneToMany(mappedBy = "client")
    private List<Invoice> invoices;
    
    public Client() {}
    
    public Client(Person person, String shippingAddress, String billingAddress) {
        this.person = person;
        this.shippingAddress = shippingAddress;
        this.billingAddress = billingAddress;
    }
    
    // Getters and Setters
    public Integer getClientId() { return clientId; }
    public void setClientId(Integer clientId) { this.clientId = clientId; }
    public Person getPerson() { return person; }
    public void setPerson(Person person) { this.person = person; }
    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    public String getBillingAddress() { return billingAddress; }
    public void setBillingAddress(String billingAddress) { this.billingAddress = billingAddress; }
    public List<Order> getOrders() { return orders; }
    public void setOrders(List<Order> orders) { this.orders = orders; }
    public List<Invoice> getInvoices() { return invoices; }
    public void setInvoices(List<Invoice> invoices) { this.invoices = invoices; }
}