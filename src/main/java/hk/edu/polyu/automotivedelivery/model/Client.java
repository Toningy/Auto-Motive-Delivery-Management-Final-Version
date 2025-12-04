package hk.edu.polyu.automotivedelivery.model;

import java.util.List;

public class Client {
    private Integer clientId;
    
    private Person person;
    
    private String shippingAddress;
    
    private String billingAddress;
    
    private List<Order> orders;
    
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