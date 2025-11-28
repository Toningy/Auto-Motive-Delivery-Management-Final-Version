package repository;

import db.DBUtil;
import model.Client;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ClientRepository {

    private Client mapRowToClient(ResultSet rs) throws SQLException {
        Client c = new Client();
        c.setClientId(rs.getInt("client_id"));
        c.setShippingAddress(rs.getString("shipping_address"));
        c.setBillingAddress(rs.getString("billing_address"));
        return c;
    }

    // Find clients by shipping address containing specific text
    public List<Client> findByShippingAddressContaining(String address) {
        List<Client> result = new ArrayList<>();
        String sql = "SELECT client_id, shipping_address, billing_address FROM client WHERE shipping_address LIKE ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + address + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRowToClient(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding clients by shipping address", e);
        }
        return result;
    }

    // Find clients with pending invoices
    public List<Client> findClientsWithPendingInvoices() {
        List<Client> result = new ArrayList<>();
        String sql = "SELECT DISTINCT c.client_id, c.shipping_address, c.billing_address " +
                     "FROM client c " +
                     "JOIN invoice i ON c.client_id = i.client_id " +
                     "WHERE i.payment_status = 'pending'";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.add(mapRowToClient(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding clients with pending invoices", e);
        }
        return result;
    }

    // Find clients who have made orders in a specific date range
    public List<Client> findClientsWithOrdersBetweenDates(Date startDate, Date endDate) {
        List<Client> result = new ArrayList<>();
        String sql = "SELECT DISTINCT c.client_id, c.shipping_address, c.billing_address " +
                     "FROM client c " +
                     "JOIN `order` o ON c.client_id = o.client_id " +
                     "WHERE o.order_date BETWEEN ? AND ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, new java.sql.Date(startDate.getTime()));
            ps.setDate(2, new java.sql.Date(endDate.getTime()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRowToClient(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding clients with orders between dates", e);
        }
        return result;
    }

    // Count total clients
    public long countTotalClients() {
        String sql = "SELECT COUNT(*) FROM client";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting clients", e);
        }
        return 0L;
    }
}
