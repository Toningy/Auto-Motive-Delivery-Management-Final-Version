package model;

import java.util.Date;
import db.DBUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DeliveryMission {
    private Integer missionId;
    private Integer orderId;
    private Integer deliveryManId;
    private Integer vehicleId;
    private String status;
    private Date startDate;
    private Date endDate;
    private String deliveryAddress;
    private String customerName;
    private String customerPhone;
    

    public DeliveryMission() {}

    public DeliveryMission(Integer orderId, String deliveryAddress, String customerName, String customerPhone) {
        this.orderId = orderId;
        this.deliveryAddress = deliveryAddress;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.status = "PENDING";
        this.startDate = new Date();
    }

    // Getters and Setters
    public Integer getMissionId() { return missionId; }
    public void setMissionId(Integer missionId) { this.missionId = missionId; }
    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }
    public Integer getDeliveryManId() { return deliveryManId; }
    public void setDeliveryManId(Integer deliveryManId) { this.deliveryManId = deliveryManId; }
    public Integer getVehicleId() { return vehicleId; }
    public void setVehicleId(Integer vehicleId) { this.vehicleId = vehicleId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }
    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    public Long countAllMissions() {
        String sql = "SELECT COUNT(*) FROM delivery_mission";
    
        try (Connection conn = DBUtil.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
        
        if (rs.next()) {
            return rs.getLong(1);
        }
    } catch (SQLException e) {
        throw new RuntimeException("Error counting all missions", e);
    }
    return 0L;
}

    public Long countMissionsByStatus(String status) {
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
        throw new RuntimeException("Error counting missions by status", e);
    }
    return 0L;
}

    public Long countCompletedMissionsBetweenDates(Date startDate, Date endDate) {
        String sql = "SELECT COUNT(*) FROM delivery_mission WHERE status = 'completed' AND end_date BETWEEN ? AND ?";
    
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
        throw new RuntimeException("Error counting completed missions between dates", e);
    }
    return 0L;
}
}