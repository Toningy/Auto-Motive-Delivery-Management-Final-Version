package hk.edu.polyu.automotivedelivery.repository;

import hk.edu.polyu.automotivedelivery.db.DBUtil;
import hk.edu.polyu.automotivedelivery.model.DeliveryMission;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DeliveryMissionRepository {
    
    public List<DeliveryMission> findAll() {
        List<DeliveryMission> result = new ArrayList<>();
        String sql = "SELECT mission_id, order_id, delivery_man_id, vehicle_id, status, " +
                     "start_date, end_date, delivery_address, customer_name, customer_phone " +
                     "FROM delivery_mission";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                result.add(mapRowToDeliveryMission(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching all missions: " + e.getMessage(), e);
        }
        return result;
    }
    
    public DeliveryMission findById(Integer missionId) {
        String sql = "SELECT mission_id, order_id, delivery_man_id, vehicle_id, status, " +
                     "start_date, end_date, delivery_address, customer_name, customer_phone " +
                     "FROM delivery_mission WHERE mission_id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, missionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToDeliveryMission(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding mission by id: " + e.getMessage(), e);
        }
        return null;
    }
    
    public List<DeliveryMission> findByDeliveryManId(Integer deliveryManId) {
        List<DeliveryMission> result = new ArrayList<>();
        String sql = "SELECT mission_id, order_id, delivery_man_id, vehicle_id, status, " +
                     "start_date, end_date, delivery_address, customer_name, customer_phone " +
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
            throw new RuntimeException("Error finding missions by delivery man id: " + e.getMessage(), e);
        }
        return result;
    }
    
    public List<DeliveryMission> findByStatus(String status) {
        List<DeliveryMission> result = new ArrayList<>();
        String sql = "SELECT mission_id, order_id, delivery_man_id, vehicle_id, status, " +
                     "start_date, end_date, delivery_address, customer_name, customer_phone " +
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
            throw new RuntimeException("Error finding missions by status: " + e.getMessage(), e);
        }
        return result;
    }
    
    public List<DeliveryMission> findPendingMissionsByDeliveryMan(Integer deliveryManId) {
        List<DeliveryMission> result = new ArrayList<>();
        String sql = "SELECT mission_id, order_id, delivery_man_id, vehicle_id, status, " +
                     "start_date, end_date, delivery_address, customer_name, customer_phone " +
                     "FROM delivery_mission WHERE delivery_man_id = ? AND status = 'PENDING'";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, deliveryManId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRowToDeliveryMission(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding pending missions by delivery man: " + e.getMessage(), e);
        }
        return result;
    }
    
    public DeliveryMission save(DeliveryMission mission) {
        String sql = "INSERT INTO delivery_mission " +
                     "(order_id, delivery_man_id, vehicle_id, status, start_date, end_date, " +
                     "delivery_address, customer_name, customer_phone) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setInt(1, mission.getOrderId());
            ps.setObject(2, mission.getDeliveryManId());
            ps.setObject(3, mission.getVehicleId());
            ps.setString(4, mission.getStatus());
            ps.setDate(5, mission.getStartDate() != null ? 
                new java.sql.Date(mission.getStartDate().getTime()) : null);
            ps.setObject(6, mission.getEndDate() != null ? 
                new java.sql.Date(mission.getEndDate().getTime()) : null);
            ps.setString(7, mission.getDeliveryAddress());
            ps.setString(8, mission.getCustomerName());
            ps.setString(9, mission.getCustomerPhone());
            
            ps.executeUpdate();
            
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    mission.setMissionId(keys.getInt(1));
                }
            }
            return mission;
            
        } catch (SQLException e) {
            throw new RuntimeException("Error saving delivery mission: " + e.getMessage(), e);
        }
    }
    
    public DeliveryMission updateStatus(Integer missionId, String status) {
        String sql = "UPDATE delivery_mission SET status = ? WHERE mission_id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, status);
            ps.setInt(2, missionId);
            
            int affected = ps.executeUpdate();
            if (affected > 0) {
                return findById(missionId);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error updating mission status: " + e.getMessage(), e);
        }
        return null;
    }
    
    public long countAllMissions() {
        String sql = "SELECT COUNT(*) FROM delivery_mission";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting all missions: " + e.getMessage(), e);
        }
        return 0L;
    }
    
    public long countMissionsByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM delivery_mission WHERE status = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting missions by status: " + e.getMessage(), e);
        }
        return 0L;
    }
    
    public long countCompletedMissionsBetweenDates(Date startDate, Date endDate) {
        String sql = "SELECT COUNT(*) FROM delivery_mission " +
                     "WHERE status = 'completed' AND end_date BETWEEN ? AND ?";
        
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
            throw new RuntimeException("Error counting completed missions between dates: " + e.getMessage(), e);
        }
        return 0L;
    }
    
    private DeliveryMission mapRowToDeliveryMission(ResultSet rs) throws SQLException {
        DeliveryMission mission = new DeliveryMission();
        mission.setMissionId(rs.getInt("mission_id"));
        mission.setOrderId(rs.getInt("order_id"));
        mission.setDeliveryManId(rs.getInt("delivery_man_id"));
        mission.setVehicleId(rs.getInt("vehicle_id"));
        mission.setStatus(rs.getString("status"));
        mission.setStartDate(rs.getDate("start_date"));
        mission.setEndDate(rs.getDate("end_date"));
        mission.setDeliveryAddress(rs.getString("delivery_address"));
        mission.setCustomerName(rs.getString("customer_name"));
        mission.setCustomerPhone(rs.getString("customer_phone"));
        return mission;
    }
}