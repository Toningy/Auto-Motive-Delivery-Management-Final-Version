package repository;

import db.DBUtil;
import model.Order;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderRepository {
    
    public List<Order> findAll() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT order_id, client_id, order_date, total_price, status FROM orders";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                orders.add(mapRowToOrder(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error in findAll(): " + e.getMessage());
            e.printStackTrace();
        }
        return orders;
    }
    
    public Order findById(Integer orderId) {
        String sql = "SELECT order_id, client_id, order_date, total_price, status FROM orders WHERE order_id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToOrder(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error in findById(): " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    public List<Order> findByClientId(Integer clientId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT order_id, client_id, order_date, total_price, status FROM orders WHERE client_id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, clientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapRowToOrder(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error in findByClientId(): " + e.getMessage());
            e.printStackTrace();
        }
        return orders;
    }
    
    public Order save(Order order) {
        String sql = "INSERT INTO orders (client_id, order_date, total_price, status) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setInt(1, order.getClientId());
            ps.setDate(2, new java.sql.Date(order.getOrderDate().getTime()));
            ps.setBigDecimal(3, order.getTotalAmount());
            ps.setString(4, order.getStatus());
            
            ps.executeUpdate();
            
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    order.setOrderId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error in save(): " + e.getMessage());
            e.printStackTrace();
        }
        return order;
    }
    
    public Long countOrdersBetweenDates(Date startDate, Date endDate) {
        String sql = "SELECT COUNT(*) FROM orders WHERE order_date BETWEEN ? AND ?";
    
        try (Connection conn = DBUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
        
            ps.setDate(1, new java.sql.Date(startDate.getTime()));
            ps.setDate(2, new java.sql.Date(endDate.getTime()));
        
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting orders between dates: " + e.getMessage(), e);
        }
        return 0L;
    }

    public Long countOrdersByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM orders WHERE status = ?";
    
        try (Connection conn = DBUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
        
            ps.setString(1, status);
        
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
        return rs.getLong(1);
    }
}
    } catch (SQLException e) {
        throw new RuntimeException("Error counting orders by status: " + e.getMessage(), e);
    }
    return 0L;
}

    private Order mapRowToOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setOrderId(rs.getInt("order_id"));
        order.setClientId(rs.getInt("client_id"));
        order.setOrderDate(rs.getDate("order_date"));
        order.setTotalAmount(rs.getBigDecimal("total_price"));
        order.setStatus(rs.getString("status"));
        return order;
    }
}