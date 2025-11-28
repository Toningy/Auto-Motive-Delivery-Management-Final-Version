package repository;

import db.DBUtil;
import model.Order;
import model.Client;
import model.DeliveryMission;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OrderRepository {

    private Order mapRowToOrder(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setOrderId(rs.getInt("order_id"));

        Integer clientId = (Integer) rs.getObject("client_id");
        if (clientId != null) {
            Client c = new Client();
            c.setClientId(clientId);
            o.setClient(c);
        }

        Date orderDate = rs.getDate("order_date");
        o.setOrderDate(orderDate);

        Integer missionId = (Integer) rs.getObject("mission_id");
        if (missionId != null) {
            DeliveryMission dm = new DeliveryMission();
            dm.setMissionId(missionId);
            o.setDeliveryMission(dm);
        }
        return o;
    }

    public List<Order> findByClientId(Integer clientId) {
        List<Order> result = new ArrayList<>();
        String sql = "SELECT order_id, client_id, order_date, mission_id FROM `order` WHERE client_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, clientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRowToOrder(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding orders by client id", e);
        }
        return result;
    }

    public List<Order> findOrdersBetweenDates(Date startDate, Date endDate) {
        List<Order> result = new ArrayList<>();
        String sql = "SELECT order_id, client_id, order_date, mission_id FROM `order` WHERE order_date BETWEEN ? AND ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, new java.sql.Date(startDate.getTime()));
            ps.setDate(2, new java.sql.Date(endDate.getTime()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRowToOrder(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding orders between dates", e);
        }
        return result;
    }

    public List<Order> findOrdersWithoutDeliveryMission() {
        List<Order> result = new ArrayList<>();
        String sql = "SELECT order_id, client_id, order_date, mission_id FROM `order` WHERE mission_id IS NULL";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.add(mapRowToOrder(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding orders without delivery mission", e);
        }
        return result;
    }
}\n