package hk.edu.polyu.automotive_delivery.repository;

import hk.edu.polyu.automotive_delivery.entity.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarRepository extends JpaRepository<Car, Integer> {
    List<Car> findByModelNameContaining(String modelName);
    List<Car> findByWarehouse_WarehouseId(Integer warehouseId);
    List<Car> findByPriceBetween(Double minPrice, Double maxPrice);
    
    @Query("SELECT c FROM Car c WHERE c.carId NOT IN (SELECT ob.car.carId FROM OrderBasket ob)")
    List<Car> findAvailableCars();
    
    // CHANGED: c.year -> c.modelYear
    @Query("SELECT c FROM Car c WHERE c.modelYear >= :minYear")
    List<Car> findCarsByMinYear(Integer minYear);
}