package repository;

import db.DBUtil;
import model.Payment;
import model.Invoice;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PaymentRepository {

    private Payment mapRowToPayment(ResultSet rs) throws SQLException {
        Payment p = new Payment();
        p.setPaymentId(rs.getInt("payment_id"));
        Date paymentDate = rs.getDate("payment_date");
        p.setPaymentDate(paymentDate);
        p.setAmount(rs.getBigDecimal("amount"));

        Integer invoiceId = (Integer) rs.getObject("invoice_id");
        if (invoiceId != null) {
            Invoice inv = new Invoice();
            inv.setInvoiceId(invoiceId);
            p.setInvoice(inv);
        }
        return p;
    }

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
            throw new RuntimeException("Error finding payments by invoice id", e);
        }
        return result;
    }

    public List<Payment> findPaymentsBetweenDates(Date startDate, Date endDate) {
        List<Payment> result = new ArrayList<>();
        String sql = "SELECT payment_id, payment_date, amount, invoice_id " +
                     "FROM payment WHERE payment_date BETWEEN ? AND ?";

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
            throw new RuntimeException("Error finding payments between dates", e);
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
                    BigDecimal sum = rs.getBigDecimal(1);
                    return sum != null ? sum : BigDecimal.ZERO;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error calculating total revenue between dates", e);
        }
        return BigDecimal.ZERO;
    }
}
