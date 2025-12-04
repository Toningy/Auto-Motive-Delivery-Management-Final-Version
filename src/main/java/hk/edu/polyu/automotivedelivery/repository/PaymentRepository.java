package hk.edu.polyu.automotivedelivery.repository;

import hk.edu.polyu.automotivedelivery.db.DBUtil;
import hk.edu.polyu.automotivedelivery.model.Payment;
import hk.edu.polyu.automotivedelivery.model.Invoice;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PaymentRepository {
    
    public List<Payment> findByInvoiceId(Integer invoiceId) {
        List<Payment> result = new ArrayList<>();
        String sql = "SELECT payment_id, payment_date, amount, invoice_id FROM payment WHERE invoice_id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, invoiceId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRowToPayment(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding payments by invoice id: " + e.getMessage(), e);
        }
        return result;
    }
    
    public List<Payment> findPaymentsBetweenDates(Date startDate, Date endDate) {
        List<Payment> result = new ArrayList<>();
        String sql = "SELECT payment_id, payment_date, amount, invoice_id FROM payment WHERE payment_date BETWEEN ? AND ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setDate(1, new java.sql.Date(startDate.getTime()));
            ps.setDate(2, new java.sql.Date(endDate.getTime()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRowToPayment(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding payments between dates: " + e.getMessage(), e);
        }
        return result;
    }
    
    public BigDecimal getTotalRevenueBetweenDates(Date startDate, Date endDate) {
        String sql = "SELECT SUM(amount) FROM payment WHERE payment_date BETWEEN ? AND ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setDate(1, new java.sql.Date(startDate.getTime()));
            ps.setDate(2, new java.sql.Date(endDate.getTime()));
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error calculating total revenue between dates: " + e.getMessage(), e);
        }
        return BigDecimal.ZERO;
    }
    
    private Payment mapRowToPayment(ResultSet rs) throws SQLException {
        Payment payment = new Payment();
        payment.setPaymentId(rs.getInt("payment_id"));
        payment.setPaymentDate(rs.getDate("payment_date"));
        payment.setAmount(rs.getBigDecimal("amount"));
        
        Invoice invoice = new Invoice();
        invoice.setInvoiceId(rs.getInt("invoice_id"));
        payment.setInvoice(invoice);
        
        return payment;
    }
}