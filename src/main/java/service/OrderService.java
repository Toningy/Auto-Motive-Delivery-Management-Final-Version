package service;

import model.*;
import repository.*;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.*;

public class OrderService {
    private OrderRepository orderRepository = new OrderRepository();
    private OrderBasketRepository orderBasketRepository = new OrderBasketRepository();
    private CarRepository carRepository = new CarRepository();
    private DeliveryMissionRepository deliveryMissionRepository = new DeliveryMissionRepository();

    public Order createOrder(Map<String, Object> orderData) throws SQLException {
        Integer clientId = Integer.parseInt(orderData.get("clientId").toString());
        List<Integer> carIds = (List<Integer>) orderData.get("carIds");
        String deliveryAddress = (String) orderData.get("deliveryAddress");
        String customerName = (String) orderData.get("customerName");
        String customerPhone = (String) orderData.get("customerPhone");

        // Calculate total
        BigDecimal totalAmount = calculateOrderTotal(carIds);

        // Create order
        Order order = new Order(clientId, new Date(), totalAmount);
        Order savedOrder = orderRepository.save(order);

        // Add cars to order basket
        for (Integer carId : carIds) {
            OrderBasket orderBasket = new OrderBasket();
            orderBasket.setOrder(savedOrder);
            
            Car car = new Car();
            car.setCarId(carId);
            orderBasket.setCar(car);
            
            orderBasketRepository.save(orderBasket);
        }

        // Create delivery mission
        DeliveryMission mission = new DeliveryMission(
            savedOrder.getOrderId(), 
            deliveryAddress, 
            customerName, 
            customerPhone
        );
        deliveryMissionRepository.save(mission);

        return savedOrder;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public List<Order> getClientOrders(Integer clientId) {
        return orderRepository.findByClientId(clientId);
    }

    private BigDecimal calculateOrderTotal(List<Integer> carIds) {
        BigDecimal total = BigDecimal.ZERO;
        for (Integer carId : carIds) {
            Car car = carRepository.findById(carId);
            if (car != null) {
                total = total.add(car.getPrice());
            }
        }
        return total;
    }
}