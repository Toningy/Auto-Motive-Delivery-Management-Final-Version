package hk.edu.polyu.automotivedelivery.web;

import hk.edu.polyu.automotivedelivery.model.Car;
import hk.edu.polyu.automotivedelivery.service.CarService;
import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/api/cars/*")
public class CarServlet extends HttpServlet {
    private CarService carService = new CarService();
    private Gson gson = new Gson();

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
                    List<Car> cars = carService.getAvailableCars();
                    resp.getWriter().write(gson.toJson(cars));
                } else if (req.getParameter("model") != null) {
                    String model = req.getParameter("model");
                    List<Car> cars = carService.searchCarsByModel(model);
                    resp.getWriter().write(gson.toJson(cars));
                } else if (req.getParameter("minPrice") != null && req.getParameter("maxPrice") != null) {
                    Double minPrice = Double.parseDouble(req.getParameter("minPrice"));
                    Double maxPrice = Double.parseDouble(req.getParameter("maxPrice"));
                    List<Car> cars = carService.getCarsByPriceRange(minPrice, maxPrice);
                    resp.getWriter().write(gson.toJson(cars));
                } else {
                    List<Car> cars = carService.getAllCars();
                    resp.getWriter().write(gson.toJson(cars));
                }
            } else if (pathInfo.startsWith("/warehouse/")) {
                String warehouseIdStr = pathInfo.substring("/warehouse/".length());
                Integer warehouseId = Integer.parseInt(warehouseIdStr);
                List<Car> cars = carService.getCarsByWarehouse(warehouseId);
                resp.getWriter().write(gson.toJson(cars));
            } else {
                String idStr = pathInfo.substring(1);
                Integer id = Integer.parseInt(idStr);
                Car car = carService.getCarById(id);

                if (car != null) {
                    resp.getWriter().write(gson.toJson(car));
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write("{\"error\":\"Car not found\"}");
                }
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        resp.setStatus(HttpServletResponse.SC_OK);
    }
}