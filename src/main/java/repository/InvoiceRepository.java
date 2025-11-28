package repository;

import db.DBUtil;
import model.Invoice;
import model.Client;
import model.Order;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class InvoiceRepository {

    private Invoice mapRowToInvoice(ResultSet rs) throws SQLException {
        Invoice i = new Invoice();
        i.setInvoiceId(rs.getInt("invoice_id"));

        Integer clientId = (Integer) rs.getObject("client_id");
        if (clientId != null) {
            Client c = new Client();
            c.setClientId(clientId);
            i.setClient(c);
        }

        Integer orderId = (Integer) rs.getObject("order_id");
        if (orderId != null) {
            Order o = new Order();
            o.setOrderId(orderId);
            i.setOrder(o);
        }

        Date issueDate = rs.getDate("issue_date");
        i.setIssueDate(issueDate);
        Date dueDate = rs.getDate("due_date");
        i.setDueDate(dueDate);
        i.setAmount(rs.getBigDecimal("amount"));
        i.setPaymentStatus(rs.getString("payment_status"));

        return i;
    }

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
            throw new RuntimeException("Error finding invoices by client id", e);
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
            throw new RuntimeException("Error finding invoices by payment status", e);
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
            throw new RuntimeException("Error finding invoices by min amount", e);
        }
        return result;
    }
}
