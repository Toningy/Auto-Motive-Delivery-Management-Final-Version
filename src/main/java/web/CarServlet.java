package web;

import db.DBUtil;
import model.Car;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/api/cars/*")
public class CarServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");

        String pathInfo = req.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                String action = req.getParameter("action");

                if ("available".equals(action)) {
                    List<Car> cars = getAvailableCars();
                    resp.getWriter().write(toJson(cars));

                } else if ("search".equals(action)) {
                    String model = req.getParameter("model");
                    List<Car> cars = searchByModel(model);
                    resp.getWriter().write(toJson(cars));

                } else if ("price-range".equals(action)) {
                    BigDecimal minPrice = new BigDecimal(req.getParameter("minPrice"));
                    BigDecimal maxPrice = new BigDecimal(req.getParameter("maxPrice"));
                    List<Car> cars = getByPriceRange(minPrice, maxPrice);
                    resp.getWriter().write(toJson(cars));

                } else {
                    List<Car> cars = getAllCars();
                    resp.getWriter().write(toJson(cars));
                }

            } else if (pathInfo.startsWith("/warehouse/")) {
                String warehouseIdStr = pathInfo.substring("/warehouse/".length());
                Integer warehouseId = Integer.parseInt(warehouseIdStr);
                List<Car> cars = getByWarehouse(warehouseId);
                resp.getWriter().write(toJson(cars));

            } else {
                String idStr = pathInfo.substring(1);
                Integer id = Integer.parseInt(idStr);
                Car car = getById(id);

                if (car != null) {
                    resp.getWriter().write(toJsonSingle(car));
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write("{\"error\":\"Car not found\"}");
                }
            }

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"" + escape(e.getMessage()) + "\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");

        try {
            String body = req.getReader().lines().reduce("", (acc, line) -> acc + line);
            Car car = parseCarFromJson(body);

            Car saved = saveCar(car);
            resp.getWriter().write(toJsonSingle(saved));

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"" + escape(e.getMessage()) + "\"}");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");

        String pathInfo = req.getPathInfo();

        try {
            if (pathInfo != null && pathInfo.length() > 1) {
                String idStr = pathInfo.substring(1);
                Integer id = Integer.parseInt(idStr);
                deleteCar(id);
                resp.getWriter().write("{\"success\":true}");
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\":\"ID required\"}");
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"" + escape(e.getMessage()) + "\"}");
        }
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    // DAO Methods
    private List<Car> getAllCars() throws SQLException {
        String sql = "SELECT * FROM car";
        List<Car> list = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToCar(rs));
            }
        }
        return list;
    }

    private List<Car> getAvailableCars() throws SQLException {
        String sql = "SELECT * FROM car WHERE car_id NOT IN (SELECT car_id FROM order_basket)";
        List<Car> list = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToCar(rs));
            }
        }
        return list;
    }

    private List<Car> searchByModel(String model) throws SQLException {
        String sql = "SELECT * FROM car WHERE model_name LIKE ?";
        List<Car> list = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + model + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToCar(rs));
                }
            }
        }
        return list;
    }

    private List<Car> getByWarehouse(Integer warehouseId) throws SQLException {
        String sql = "SELECT * FROM car WHERE warehouse_id = ?";
        List<Car> list = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, warehouseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToCar(rs));
                }
            }
        }
        return list;
    }

    private List<Car> getByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) throws SQLException {
        String sql = "SELECT * FROM car WHERE price BETWEEN ? AND ?";
        List<Car> list = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBigDecimal(1, minPrice);
            ps.setBigDecimal(2, maxPrice);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToCar(rs));
                }
            }
        }
        return list;
    }

    private Car getById(Integer id) throws SQLException {
        String sql = "SELECT * FROM car WHERE car_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCar(rs);
                }
            }
        }
        return null;
    }

    private Car saveCar(Car car) throws SQLException {
        String sql = "INSERT INTO car (model_name, model_year, price, weight, warehouse_id, factory_id) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, car.getModelName());
            ps.setInt(2, car.getModelYear());
            ps.setBigDecimal(3, car.getPrice());
            ps.setBigDecimal(4, car.getWeight());
            ps.setInt(5, car.getWarehouseId());
            ps.setInt(6, car.getFactoryId());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    car.setCarId(keys.getInt(1));
                }
            }
        }
        return car;
    }

    private void deleteCar(Integer id) throws SQLException {
        String sql = "DELETE FROM car WHERE car_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Car mapResultSetToCar(ResultSet rs) throws SQLException {
        Car car = new Car();
        car.setCarId(rs.getInt("car_id"));
        car.setModelName(rs.getString("model_name"));
        car.setModelYear(rs.getInt("model_year"));
        car.setPrice(rs.getBigDecimal("price"));
        car.setWeight(rs.getBigDecimal("weight"));
        car.setWarehouseId(rs.getInt("warehouse_id"));
        car.setFactoryId(rs.getInt("factory_id"));
        return car;
    }

    // JSON Methods
    private String toJson(List<Car> cars) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < cars.size(); i++) {
            sb.append(toJsonSingle(cars.get(i)));
            if (i < cars.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private String toJsonSingle(Car c) {
        return "{\"carId\":" + c.getCarId() + "," +
                "\"modelName\":\"" + escape(c.getModelName()) + "\"," +
                "\"modelYear\":" + c.getModelYear() + "," +
                "\"price\":" + c.getPrice() + "," +
                "\"weight\":" + c.getWeight() + "," +
                "\"warehouseId\":" + c.getWarehouseId() + "," +
                "\"factoryId\":" + c.getFactoryId() + "}";
    }

    private Car parseCarFromJson(String json) {
        Car car = new Car();
        json = json.replaceAll("[{}\" ]", "");
        String[] pairs = json.split(",");

        for (String pair : pairs) {
            String[] kv = pair.split(":");
            if (kv.length == 2) {
                String key = kv[0].trim();
                String value = kv[1].trim();

                switch (key) {
                    case "modelName":
                        car.setModelName(value);
                        break;
                    case "modelYear":
                        car.setModelYear(Integer.parseInt(value));
                        break;
                    case "price":
                        car.setPrice(new BigDecimal(value));
                        break;
                    case "weight":
                        car.setWeight(new BigDecimal(value));
                        break;
                    case "warehouseId":
                        car.setWarehouseId(Integer.parseInt(value));
                        break;
                    case "factoryId":
                        car.setFactoryId(Integer.parseInt(value));
                        break;
                }
            }
        }
        return car;
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\"", "\\\"").replace("\n", " ");
    }
}