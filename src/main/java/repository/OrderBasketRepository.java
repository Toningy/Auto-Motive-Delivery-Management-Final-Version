package repository;

import db.DBUtil;
import model.Car;
import model.Order;
import model.OrderBasket;
import model.OrderBasketId;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderBasketRepository {
    
    public void save(OrderBasket orderBasket) {
        String sql = "INSERT INTO order_basket (order_id, car_id) VALUES (?, ?)";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, orderBasket.getOrder().getOrderId());
            ps.setInt(2, orderBasket.getCar().getCarId());
            ps.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Error saving order basket: " + e.getMessage(), e);
        }
    }
    
    public List<OrderBasket> findByOrderId(Integer orderId) {
        List<OrderBasket> result = new ArrayList<>();
        String sql = "SELECT order_id, car_id FROM order_basket WHERE order_id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderBasketId id = new OrderBasketId(
                        rs.getInt("order_id"),
                        rs.getInt("car_id")
                    );
                    
                    OrderBasket ob = new OrderBasket();
                    ob.setId(id);
                    
                    Order order = new Order();
                    order.setOrderId(rs.getInt("order_id"));
                    ob.setOrder(order);
                    
                    Car car = new Car();
                    car.setCarId(rs.getInt("car_id"));
                    ob.setCar(car);
                    
                    result.add(ob);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding order baskets: " + e.getMessage(), e);
        }
        return result;
    }
    
    public List<OrderBasket> findByCarId(Integer carId) {
        List<OrderBasket> result = new ArrayList<>();
        String sql = "SELECT order_id, car_id FROM order_basket WHERE car_id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, carId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderBasketId id = new OrderBasketId(
                        rs.getInt("order_id"),
                        rs.getInt("car_id")
                    );
                    
                    OrderBasket ob = new OrderBasket();
                    ob.setId(id);
                    
                    Order order = new Order();
                    order.setOrderId(rs.getInt("order_id"));
                    ob.setOrder(order);
                    
                    Car car = new Car();
                    car.setCarId(rs.getInt("car_id"));
                    ob.setCar(car);
                    
                    result.add(ob);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding order baskets by car id: " + e.getMessage(), e);
        }
        return result;
    }
    
    public long countOrdersForCar(Integer carId) {
        String sql = "SELECT COUNT(*) FROM order_basket WHERE car_id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, carId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting orders for car: " + e.getMessage(), e);
        }
        return 0L;
    }
    
    public List<Car> findCarsInOrder(Integer orderId) {
        List<Car> result = new ArrayList<>();
        String sql = "SELECT c.* FROM car c " +
                     "JOIN order_basket ob ON c.car_id = ob.car_id " +
                     "WHERE ob.order_id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Car car = new Car();
                    car.setCarId(rs.getInt("car_id"));
                    car.setModelName(rs.getString("model_name"));
                    car.setModelYear(rs.getInt("model_year"));
                    car.setWeight(rs.getBigDecimal("weight"));
                    car.setPrice(rs.getBigDecimal("price"));
                    car.setWarehouseId(rs.getInt("warehouse_id"));
                    car.setFactoryId(rs.getInt("factory_id"));
                    result.add(car);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding cars in order: " + e.getMessage(), e);
        }
        return result;
    }
}