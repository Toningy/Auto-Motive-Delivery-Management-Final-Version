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
                Car car = mapRowToCar(rs);
                cars.add(car);
            }
        } catch (SQLException e) {
            System.err.println("Error in findAll(): " + e.getMessage());
            e.printStackTrace();
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
            System.err.println("Error in findById(): " + e.getMessage());
            e.printStackTrace();
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
            System.err.println("Error in findByModelNameContaining(): " + e.getMessage());
            e.printStackTrace();
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
            System.err.println("Error in findByWarehouseId(): " + e.getMessage());
            e.printStackTrace();
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
            System.err.println("Error in findByPriceBetween(): " + e.getMessage());
            e.printStackTrace();
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
            System.err.println("Error in findAvailableCars(): " + e.getMessage());
            e.printStackTrace();
        }
        return cars;
    }
    
    private Car mapRowToCar(ResultSet rs) throws SQLException {
        Car car = new Car();
        car.setCarId(rs.getInt("car_id"));
        car.setModelName(rs.getString("model_name"));
        car.setModelYear(rs.getInt("model_year"));
        car.setWeight(rs.getBigDecimal("weight"));
        car.setPrice(rs.getBigDecimal("price"));
        car.setWarehouseId(rs.getInt("warehouse_id"));
        car.setFactoryId(rs.getInt("factory_id"));
        return car;
    }
}