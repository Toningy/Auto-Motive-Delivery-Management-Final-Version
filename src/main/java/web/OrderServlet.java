package web;

import model.Order;
import service.OrderService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

@WebServlet("/api/orders/*")
public class OrderServlet extends HttpServlet {
    private OrderService orderService = new OrderService();
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
                List<Order> orders = orderService.getAllOrders();
                resp.getWriter().write(gson.toJson(orders));
            } else if (pathInfo.startsWith("/client/")) {
                String clientIdStr = pathInfo.substring("/client/".length());
                Integer clientId = Integer.parseInt(clientIdStr);
                List<Order> orders = orderService.getClientOrders(clientId);
                resp.getWriter().write(gson.toJson(orders));
            } else {
                String idStr = pathInfo.substring(1);
                Integer id = Integer.parseInt(idStr);
                Order order = orderService.getAllOrders().stream()
                    .filter(o -> o.getOrderId().equals(id))
                    .findFirst()
                    .orElse(null);

                if (order != null) {
                    resp.getWriter().write(gson.toJson(order));
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write("{\"error\":\"Order not found\"}");
                }
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
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
            Type type = new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> orderData = gson.fromJson(body, type);

            Order order = orderService.createOrder(orderData);
            resp.getWriter().write(gson.toJson(order));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        resp.setStatus(HttpServletResponse.SC_OK);
    }
}