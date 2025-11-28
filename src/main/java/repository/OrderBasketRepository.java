package repository;

import db.DBUtil;
import model.OrderBasket;
import model.OrderBasketId;
import model.Order;
import model.Car;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderBasketRepository {

    private OrderBasket mapRowToOrderBasket(ResultSet rs) throws SQLException {
        int orderId = rs.getInt("order_id");
        int carId = rs.getInt("car_id");

        OrderBasketId id = new OrderBasketId(orderId, carId);
        OrderBasket ob = new OrderBasket();
        ob.setId(id);

        Order order = new Order();
        order.setOrderId(orderId);
        ob.setOrder(order);

        Car car = new Car();
        car.setCarId(carId);
        ob.setCar(car);

        return ob;
    }

    // All baskets for a specific order
    public List<OrderBasket> findByOrderId(Integer orderId) {
        List<OrderBasket> result = new ArrayList<>();
        String sql = "SELECT order_id, car_id FROM order_basket WHERE order_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRowToOrderBasket(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding order baskets by order id", e);
        }
        return result;
    }

    // All baskets for a specific car
    public List<OrderBasket> findByCarId(Integer carId) {
        List<OrderBasket> result = new ArrayList<>();
        String sql = "SELECT order_id, car_id FROM order_basket WHERE car_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, carId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRowToOrderBasket(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding order baskets by car id", e);
        }
        return result;
    }

    // Count how many times a car has been ordered
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
            throw new RuntimeException("Error counting orders for car", e);
        }
        return 0L;
    }

    // Get all cars in a specific order
    public List<Car> findCarsInOrder(Integer orderId) {
        List<Car> result = new ArrayList<>();
        String sql = "SELECT c.car_id, c.model_name, c.model_year, c.weight, c.price, c.warehouse_id, c.factory_id " +
                     "FROM order_basket ob " +
                     "JOIN car c ON ob.car_id = c.car_id " +
                     "WHERE ob.order_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Car c = new Car();
                    c.setCarId(rs.getInt("car_id"));
                    c.setModelName(rs.getString("model_name"));
                    c.setModelYear((Integer) rs.getObject("model_year"));
                    c.setWeight(rs.getBigDecimal("weight"));
                    c.setPrice(rs.getBigDecimal("price"));
                    c.setWarehouseId((Integer) rs.getObject("warehouse_id"));
                    c.setFactoryId((Integer) rs.getObject("factory_id"));
                    result.add(c);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding cars in order", e);
        }
        return result;
    }
}
