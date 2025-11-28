package repository;

import db.DBUtil;
import model.DeliveryVehicle;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DeliveryVehicleRepository {

    private DeliveryVehicle mapRowToDeliveryVehicle(ResultSet rs) throws SQLException {
        DeliveryVehicle v = new DeliveryVehicle();
        v.setVehicleId(rs.getInt("vehicle_id"));
        v.setPlateNumber(rs.getString("plate_number"));
        return v;
    }

    public DeliveryVehicle findById(Integer vehicleId) {
        String sql = "SELECT vehicle_id, plate_number FROM delivery_vehicle WHERE vehicle_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, vehicleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToDeliveryVehicle(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding delivery vehicle by id", e);
        }
        return null;
    }

    public List<DeliveryVehicle> findAll() {
        List<DeliveryVehicle> result = new ArrayList<>();
        String sql = "SELECT vehicle_id, plate_number FROM delivery_vehicle";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.add(mapRowToDeliveryVehicle(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching all delivery vehicles", e);
        }
        return result;
    }
}
