package hk.edu.polyu.automotive_delivery.repository;

import hk.edu.polyu.automotive_delivery.entity.OrderBasket;
import hk.edu.polyu.automotive_delivery.entity.OrderBasketId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderBasketRepository extends JpaRepository<OrderBasket, OrderBasketId> {
    
    // Find all order baskets for a specific order
    List<OrderBasket> findByOrder_OrderId(Integer orderId);
    
    // Find all order baskets containing a specific car
    List<OrderBasket> findByCar_CarId(Integer carId);
    
    // Count how many times a car has been ordered
    @Query("SELECT COUNT(ob) FROM OrderBasket ob WHERE ob.car.carId = :carId")
    Long countOrdersForCar(Integer carId);
    
    // Get all cars in a specific order
    @Query("SELECT ob.car FROM OrderBasket ob WHERE ob.order.orderId = :orderId")
    List<Object[]> findCarsInOrder(Integer orderId);
}