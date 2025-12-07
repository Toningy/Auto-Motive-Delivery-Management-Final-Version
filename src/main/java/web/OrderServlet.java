package web;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import db.DBUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Order;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

@WebServlet("/api/orders/*")
public class OrderServlet extends HttpServlet {
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
                List<Order> orders = getAllOrders();
                resp.getWriter().write(gson.toJson(orders));
            } else if (pathInfo.startsWith("/client/")) {
                String clientIdStr = pathInfo.substring("/client/".length());
                Integer clientId = Integer.parseInt(clientIdStr);
                List<Order> orders = getOrdersByClientId(clientId);
                resp.getWriter().write(gson.toJson(orders));
            } else {
                String idStr = pathInfo.substring(1);
                Integer id = Integer.parseInt(idStr);
                Order order = getOrderById(id);

                if (order != null) {
                    resp.getWriter().write(gson.toJson(order));
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write("{\"error\":\"Order not found\"}");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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
            System.out.println("Received order data: " + body);
            
            Type type = new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> orderData = gson.fromJson(body, type);
            
            // Extract values
            Integer clientId = ((Double) orderData.get("clientId")).intValue();
            List<Double> carIdDoubles = (List<Double>) orderData.get("carIds");
            List<Integer> carIds = new ArrayList<>();
            for (Double d : carIdDoubles) {
                carIds.add(d.intValue());
            }
            String deliveryAddress = (String) orderData.get("deliveryAddress");
            String customerName = (String) orderData.get("customerName");
            String customerPhone = (String) orderData.get("customerPhone");
            
            System.out.println("Parsed order data:");
            System.out.println("Client ID: " + clientId);
            System.out.println("Car IDs: " + carIds);
            System.out.println("Address: " + deliveryAddress);
            System.out.println("Name: " + customerName);
            System.out.println("Phone: " + customerPhone);

            // Create order
            Order order = createOrder(clientId, carIds, deliveryAddress, customerName, customerPhone);
            
            if (order != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("order", order);
                response.put("message", "Order created successfully");
                
                resp.getWriter().write(gson.toJson(response));
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\":\"Failed to create order\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private Order createOrder(Integer clientId, List<Integer> carIds, 
                             String deliveryAddress, String customerName, String customerPhone) {
        Connection conn = null;
        PreparedStatement orderStmt = null;
        PreparedStatement basketStmt = null;
        PreparedStatement missionStmt = null;
        PreparedStatement priceStmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false); // Start transaction
            
            // Calculate total price
            BigDecimal totalPrice = BigDecimal.ZERO;
            String priceSql = "SELECT price FROM car WHERE car_id = ?";
            priceStmt = conn.prepareStatement(priceSql);
            
            for (Integer carId : carIds) {
                priceStmt.setInt(1, carId);
                rs = priceStmt.executeQuery();
                if (rs.next()) {
                    totalPrice = totalPrice.add(rs.getBigDecimal("price"));
                }
                rs.close();
            }
            
            // Create order
            String orderSql = "INSERT INTO orders (client_id, order_date, total_price, status) " +
                             "VALUES (?, NOW(), ?, 'PENDING')";
            orderStmt = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS);
            orderStmt.setInt(1, clientId);
            orderStmt.setBigDecimal(2, totalPrice);
            
            int affectedRows = orderStmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating order failed, no rows affected.");
            }
            
            // Get the generated order ID
            int orderId;
            try (ResultSet generatedKeys = orderStmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    orderId = generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating order failed, no ID obtained.");
                }
            }
            
            // Add cars to order basket
            String basketSql = "INSERT INTO order_basket (order_id, car_id) VALUES (?, ?)";
            basketStmt = conn.prepareStatement(basketSql);
            
            for (Integer carId : carIds) {
                basketStmt.setInt(1, orderId);
                basketStmt.setInt(2, carId);
                basketStmt.addBatch();
            }
            basketStmt.executeBatch();
            
            // Create delivery mission
            String missionSql = "INSERT INTO delivery_mission (order_id, delivery_address, customer_name, customer_phone, status, start_date) " +
                               "VALUES (?, ?, ?, ?, 'PENDING', NOW())";
            missionStmt = conn.prepareStatement(missionSql);
            missionStmt.setInt(1, orderId);
            missionStmt.setString(2, deliveryAddress);
            missionStmt.setString(3, customerName);
            missionStmt.setString(4, customerPhone);
            missionStmt.executeUpdate();
            
            // Commit transaction
            conn.commit();
            
            // Create order object to return
            Order order = new Order();
            order.setOrderId(orderId);
            order.setClientId(clientId);
            order.setOrderDate(new java.util.Date());
            order.setTotalAmount(totalPrice);
            order.setStatus("PENDING");
            
            return order;
            
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return null;
        } finally {
            try {
                if (rs != null) rs.close();
                if (priceStmt != null) priceStmt.close();
                if (basketStmt != null) basketStmt.close();
                if (missionStmt != null) missionStmt.close();
                if (orderStmt != null) orderStmt.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT order_id, client_id, order_date, total_price, status FROM orders";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                Order order = new Order();
                order.setOrderId(rs.getInt("order_id"));
                order.setClientId(rs.getInt("client_id"));
                order.setOrderDate(rs.getDate("order_date"));
                order.setTotalAmount(rs.getBigDecimal("total_price"));
                order.setStatus(rs.getString("status"));
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    private List<Order> getOrdersByClientId(Integer clientId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT order_id, client_id, order_date, total_price, status " +
                     "FROM orders WHERE client_id = ? ORDER BY order_date DESC";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, clientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order order = new Order();
                    order.setOrderId(rs.getInt("order_id"));
                    order.setClientId(rs.getInt("client_id"));
                    order.setOrderDate(rs.getDate("order_date"));
                    order.setTotalAmount(rs.getBigDecimal("total_price"));
                    order.setStatus(rs.getString("status"));
                    orders.add(order);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    private Order getOrderById(Integer orderId) {
        String sql = "SELECT order_id, client_id, order_date, total_price, status " +
                     "FROM orders WHERE order_id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Order order = new Order();
                    order.setOrderId(rs.getInt("order_id"));
                    order.setClientId(rs.getInt("client_id"));
                    order.setOrderDate(rs.getDate("order_date"));
                    order.setTotalAmount(rs.getBigDecimal("total_price"));
                    order.setStatus(rs.getString("status"));
                    return order;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        resp.setStatus(HttpServletResponse.SC_OK);
    }
}