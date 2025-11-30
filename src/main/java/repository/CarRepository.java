package hk.edu.polyu.automotivedelivery.repository;

import hk.edu.polyu.automotivedelivery.db.DBUtil;
import hk.edu.polyu.automotivedelivery.model.Car;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CarRepository {

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

    public List<Car> findByPriceBetween(Double minPrice, Double maxPrice) {
        List<Car> cars = new ArrayList<>();
        String sql = "SELECT car_id, model_name, model_year, weight, price, warehouse_id, factory_id FROM car WHERE price BETWEEN ? AND ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBigDecimal(1, BigDecimal.valueOf(minPrice));
            ps.setBigDecimal(2, BigDecimal.valueOf(maxPrice));
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

    public List<Car> findAvailableCars() {
        List<Car> cars = new ArrayList<>();
        String sql = "SELECT c.car_id, c.model_name, c.model_year, c.weight, c.price, c.warehouse_id, c.factory_id " +
                     "FROM car c " +
                     "WHERE c.car_id NOT IN (SELECT car_id FROM order_basket)";

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

    private Car mapRowToCar(ResultSet rs) throws SQLException {
        Car c = new Car();
        c.setCarId(rs.getInt("car_id"));
        c.setModelName(rs.getString("model_name"));
        c.setModelYear(rs.getInt("model_year"));
        c.setWeight(rs.getBigDecimal("weight"));
        c.setPrice(rs.getBigDecimal("price"));
        c.setWarehouseId(rs.getInt("warehouse_id"));
        c.setFactoryId(rs.getInt("factory_id"));
        return c;
    }
}