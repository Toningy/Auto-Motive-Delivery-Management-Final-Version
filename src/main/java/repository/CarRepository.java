package repository;

import db.DBUtil;
import model.Car;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CarRepository {

    // Alle Autos holen
    public List<Car> findAll() {
        List<Car> cars = new ArrayList<>();
        String sql = "SELECT car_id, model_name, model_year, weight, price, warehouse_id, factory_id FROM car";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                cars.add(mapRowToCar(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching all cars", e);
        }
        return cars;
    }

    // Auto nach ID
    public Car findById(Integer carId) {
        String sql = "SELECT car_id, model_name, model_year, weight, price, warehouse_id, factory_id FROM car WHERE car_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, carId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToCar(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching car by id", e);
        }
        return null;
    }

    // Autos nach Modellname (enthält String)
    public List<Car> findByModelNameContaining(String modelName) {
        List<Car> cars = new ArrayList<>();
        String sql = "SELECT car_id, model_name, model_year, weight, price, warehouse_id, factory_id FROM car WHERE model_name LIKE ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + modelName + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    cars.add(mapRowToCar(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding cars by model name", e);
        }
        return cars;
    }

    // Autos nach Warehouse-ID
    public List<Car> findByWarehouseId(Integer warehouseId) {
        List<Car> cars = new ArrayList<>();
        String sql = "SELECT car_id, model_name, model_year, weight, price, warehouse_id, factory_id FROM car WHERE warehouse_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, warehouseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    cars.add(mapRowToCar(rs));
                }
            }
        } catch (SQLException e) {
        throw new RuntimeException("Error finding cars by warehouse", e);
        }
        return cars;
    }

    // Autos nach Preis-Range
    public List<Car> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice) {
        List<Car> cars = new ArrayList<>();
        String sql = "SELECT car_id, model_name, model_year, weight, price, warehouse_id, factory_id FROM car WHERE price BETWEEN ? AND ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBigDecimal(1, minPrice);
            ps.setBigDecimal(2, maxPrice);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    cars.add(mapRowToCar(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding cars by price range", e);
        }
        return cars;
    }

    // Verfügbare Autos (nicht in order_basket)
    public List<Car> findAvailableCars() {
        List<Car> cars = new ArrayList<>();
        String sql = "SELECT car_id, model_name, model_year, weight, price, warehouse_id, factory_id " +
                     "FROM car " +
                     "WHERE car_id NOT IN (SELECT car_id FROM order_basket)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                cars.add(mapRowToCar(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding available cars", e);
        }
        return cars;
    }

    // Autos ab einem bestimmten Jahr
    public List<Car> findCarsByMinYear(Integer minYear) {
        List<Car> cars = new ArrayList<>();
        String sql = "SELECT car_id, model_name, model_year, weight, price, warehouse_id, factory_id FROM car WHERE model_year >= ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, minYear);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    cars.add(mapRowToCar(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding cars by min year", e);
        }
        return cars;
    }

    // Hilfsmethode: ResultSet → Car-Objekt
    private Car mapRowToCar(ResultSet rs) throws SQLException {
        Car c = new Car();
        c.setCarId(rs.getInt("car_id"));
        c.setModelName(rs.getString("model_name"));
        Integer year = (Integer) rs.getObject("model_year");
        c.setModelYear(year);
        c.setWeight(rs.getBigDecimal("weight"));
        c.setPrice(rs.getBigDecimal("price"));
        c.setWarehouseId((Integer) rs.getObject("warehouse_id"));
        c.setFactoryId((Integer) rs.getObject("factory_id"));
        return c;
    }
}
