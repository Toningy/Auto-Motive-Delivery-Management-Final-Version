package hk.edu.polyu.automotivedelivery.model;

import java.math.BigDecimal;
import java.util.Date;

public class Invoice {
    private Integer invoiceId;
    
    private Client client;
    
    private Order order;
    
    private Date issueDate;
    
    private Date dueDate;
    
    private BigDecimal amount;
    
    private String paymentStatus;
    
    public Invoice() {}
    
    public Invoice(Client client, Order order, Date issueDate, Date dueDate, BigDecimal amount, String paymentStatus) {
        this.client = client;
        this.order = order;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.amount = amount;
        this.paymentStatus = paymentStatus;
    }
    
    // Getters and Setters
    public Integer getInvoiceId() { return invoiceId; }
    public void setInvoiceId(Integer invoiceId) { this.invoiceId = invoiceId; }
    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
    public Date getIssueDate() { return issueDate; }
    public void setIssueDate(Date issueDate) { this.issueDate = issueDate; }
    public Date getDueDate() { return dueDate; }
    public void setDueDate(Date dueDate) { this.dueDate = dueDate; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
}