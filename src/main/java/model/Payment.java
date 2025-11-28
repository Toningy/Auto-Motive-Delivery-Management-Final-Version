package hk.edu.polyu.automotive_delivery.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "payment")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Integer paymentId;
    
    @Column(name = "payment_date")
    @Temporal(TemporalType.DATE)
    private Date paymentDate;
    
    @Column(name = "amount", precision = 10, scale = 2)
    private BigDecimal amount;
    
    @ManyToOne
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;
    
    public Payment() {}
    
    public Payment(Date paymentDate, BigDecimal amount, Invoice invoice) {
        this.paymentDate = paymentDate;
        this.amount = amount;
        this.invoice = invoice;
    }
    
    // Getters and Setters
    public Integer getPaymentId() { return paymentId; }
    public void setPaymentId(Integer paymentId) { this.paymentId = paymentId; }
    public Date getPaymentDate() { return paymentDate; }
    public void setPaymentDate(Date paymentDate) { this.paymentDate = paymentDate; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public Invoice getInvoice() { return invoice; }
    public void setInvoice(Invoice invoice) { this.invoice = invoice; }
}