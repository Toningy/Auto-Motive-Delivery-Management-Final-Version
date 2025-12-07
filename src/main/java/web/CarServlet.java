package web;

import com.google.gson.Gson;
import db.DBUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Car;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/api/cars")
public class CarServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");

        try {
            // Get all cars
            List<Car> cars = getAllCars();
            
            // Add image URLs to each car
            for (Car car : cars) {
                car.setModelName(car.getModelName() != null ? car.getModelName() : "Unknown Model");
            }
            
            String json = gson.toJson(cars);
            resp.getWriter().write(json);

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"Failed to load cars: " + e.getMessage() + "\"}");
        }
    }

    private List<Car> getAllCars() {
        List<Car> cars = new ArrayList<>();
        String sql = "SELECT car_id, model_name, model_year, weight, price, warehouse_id, factory_id FROM car";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                Car car = new Car();
                car.setCarId(rs.getInt("car_id"));
                car.setModelName(rs.getString("model_name"));
                car.setModelYear(rs.getInt("model_year"));
                car.setWeight(rs.getBigDecimal("weight"));
                car.setPrice(rs.getBigDecimal("price"));
                car.setWarehouseId(rs.getInt("warehouse_id"));
                car.setFactoryId(rs.getInt("factory_id"));
                cars.add(car);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cars;
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        resp.setStatus(HttpServletResponse.SC_OK);
    }
}