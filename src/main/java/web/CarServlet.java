package web;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Car;
import service.CarService;

import java.io.IOException;
import java.util.List;

@WebServlet("/api/cars")
public class CarServlet extends HttpServlet {

    private final CarService carService = new CarService();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");

        try {
            // optional: ?action=available für nur verfügbare Autos
            String action = req.getParameter("action");
            List<Car> cars;

            if ("available".equalsIgnoreCase(action)) {
                cars = carService.getAvailableCars();   // falls du diese Methode hast
            } else {
                cars = carService.getAllCars();         // Standard: alle Autos
            }

            String json = gson.toJson(cars);
            resp.getWriter().write(json);

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"Failed to load cars\"}");
        }
    }
}
