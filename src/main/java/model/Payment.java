package model;

import java.math.BigDecimal;
import java.util.Date;

public class Payment {
    private Integer paymentId;
    
    private Date paymentDate;
    
    private BigDecimal amount;
    
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