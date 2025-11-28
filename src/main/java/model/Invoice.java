package hk.edu.polyu.automotive_delivery.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "invoice")
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invoice_id")
    private Integer invoiceId;
    
    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;
    
    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;
    
    @Column(name = "issue_date")
    @Temporal(TemporalType.DATE)
    private Date issueDate;
    
    @Column(name = "due_date")
    @Temporal(TemporalType.DATE)
    private Date dueDate;
    
    @Column(name = "amount", precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "payment_status", length = 50)
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