package hk.edu.polyu.automotivedelivery.repository;

import hk.edu.polyu.automotivedelivery.db.DBUtil;
import hk.edu.polyu.automotivedelivery.model.Invoice;
import hk.edu.polyu.automotivedelivery.model.Client;
import hk.edu.polyu.automotivedelivery.model.Order;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InvoiceRepository {
    
    public List<Invoice> findByClientId(Integer clientId) {
        List<Invoice> result = new ArrayList<>();
        String sql = "SELECT invoice_id, client_id, order_id, issue_date, due_date, amount, payment_status " +
                     "FROM invoice WHERE client_id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, clientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRowToInvoice(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding invoices by client id: " + e.getMessage(), e);
        }
        return result;
    }
    
    public List<Invoice> findByPaymentStatus(String paymentStatus) {
        List<Invoice> result = new ArrayList<>();
        String sql = "SELECT invoice_id, client_id, order_id, issue_date, due_date, amount, payment_status " +
                     "FROM invoice WHERE payment_status = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, paymentStatus);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRowToInvoice(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding invoices by payment status: " + e.getMessage(), e);
        }
        return result;
    }
    
    public List<Invoice> findByMinAmount(BigDecimal minAmount) {
        List<Invoice> result = new ArrayList<>();
        String sql = "SELECT invoice_id, client_id, order_id, issue_date, due_date, amount, payment_status " +
                     "FROM invoice WHERE amount > ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setBigDecimal(1, minAmount);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRowToInvoice(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding invoices by min amount: " + e.getMessage(), e);
        }
        return result;
    }
    
    private Invoice mapRowToInvoice(ResultSet rs) throws SQLException {
        Invoice invoice = new Invoice();
        invoice.setInvoiceId(rs.getInt("invoice_id"));
        
        Client client = new Client();
        client.setClientId(rs.getInt("client_id"));
        invoice.setClient(client);
        
        Order order = new Order();
        order.setOrderId(rs.getInt("order_id"));
        invoice.setOrder(order);
        
        invoice.setIssueDate(rs.getDate("issue_date"));
        invoice.setDueDate(rs.getDate("due_date"));
        invoice.setAmount(rs.getBigDecimal("amount"));
        invoice.setPaymentStatus(rs.getString("payment_status"));
        
        return invoice;
    }
}