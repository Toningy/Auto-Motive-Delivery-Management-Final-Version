package hk.edu.polyu.automotive_delivery.repository;

import hk.edu.polyu.automotive_delivery.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findByClient_ClientId(Integer clientId);
    
    @Query("SELECT o FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate")
    List<Order> findOrdersBetweenDates(Date startDate, Date endDate);
    
    @Query("SELECT o FROM Order o WHERE o.deliveryMission IS NULL")
    List<Order> findOrdersWithoutDeliveryMission();
}