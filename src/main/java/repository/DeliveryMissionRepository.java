package repository;

import db.DBUtil;
import model.DeliveryMission;
import model.DeliveryMan;
import model.Manager;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DeliveryMissionRepository {

    private DeliveryMission mapRowToDeliveryMission(ResultSet rs) throws SQLException {
        DeliveryMission dm = new DeliveryMission();
        dm.setMissionId(rs.getInt("mission_id"));
        Date missionDate = rs.getDate("mission_date");
        dm.setMissionDate(missionDate);
        dm.setStatus(rs.getString("status"));

        Integer deliveryManId = (Integer) rs.getObject("delivery_man_id");
        if (deliveryManId != null) {
            DeliveryMan d = new DeliveryMan();
            d.setStaffId(deliveryManId);
            dm.setDeliveryMan(d);
        }

        Integer managerId = (Integer) rs.getObject("manager_id");
        if (managerId != null) {
            Manager m = new Manager();
            m.setStaffId(managerId);
            dm.setManager(m);
        }

        return dm;
    }

    public List<DeliveryMission> findByDeliveryManId(Integer deliveryManId) {
        List<DeliveryMission> result = new ArrayList<>();
        String sql = "SELECT mission_id, mission_date, status, delivery_man_id, manager_id " +
                     "FROM delivery_mission WHERE delivery_man_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, deliveryManId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRowToDeliveryMission(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding missions by delivery man id", e);
        }
        return result;
    }

    public List<DeliveryMission> findByStatus(String status) {
        List<DeliveryMission> result = new ArrayList<>();
        String sql = "SELECT mission_id, mission_date, status, delivery_man_id, manager_id " +
                     "FROM delivery_mission WHERE status = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRowToDeliveryMission(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding missions by status", e);
        }
        return result;
    }

    public List<DeliveryMission> findMissionsBetweenDates(Date startDate, Date endDate) {
        List<DeliveryMission> result = new ArrayList<>();
        String sql = "SELECT mission_id, mission_date, status, delivery_man_id, manager_id " +
                     "FROM delivery_mission WHERE mission_date BETWEEN ? AND ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, new java.sql.Date(startDate.getTime()));
            ps.setDate(2, new java.sql.Date(endDate.getTime()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRowToDeliveryMission(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding missions between dates", e);
        }
        return result;
    }

    public List<DeliveryMission> findPendingMissionsByDeliveryMan(Integer deliveryManId) {
        List<DeliveryMission> result = new ArrayList<>();
        String sql = "SELECT mission_id, mission_date, status, delivery_man_id, manager_id " +
                     "FROM delivery_mission WHERE delivery_man_id = ? AND status = 'pending'";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, deliveryManId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRowToDeliveryMission(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding pending missions by delivery man", e);
        }
        return result;
    }
}
