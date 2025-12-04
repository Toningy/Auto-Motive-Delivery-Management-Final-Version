package hk.edu.polyu.automotivedelivery.service;

import hk.edu.polyu.automotivedelivery.model.Car;
import hk.edu.polyu.automotivedelivery.repository.CarRepository;

import java.util.List;

public class CarService {
    private CarRepository carRepository = new CarRepository();

    public List<Car> getAllCars() {
        return carRepository.findAll();
    }

    public Car getCarById(Integer id) {
        return carRepository.findById(id);
    }

    public List<Car> getAvailableCars() {
        return carRepository.findAvailableCars();
    }

    public List<Car> searchCarsByModel(String model) {
        return carRepository.findByModelNameContaining(model);
    }

    public List<Car> getCarsByWarehouse(Integer warehouseId) {
        return carRepository.findByWarehouseId(warehouseId);
    }

    public List<Car> getCarsByPriceRange(Double minPrice, Double maxPrice) {
        return carRepository.findByPriceBetween(minPrice, maxPrice);
    }
}