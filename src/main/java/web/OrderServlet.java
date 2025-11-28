package web;

import db.DBUtil;
import model.Order;

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

@WebServlet("/api/orders/*")
public class OrderServlet extends HttpServlet {

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

                if ("pending-delivery".equals(action)) {
                    List<Order> orders = getOrdersWithoutDelivery();
                    resp.getWriter().write(toJson(orders));
                } else {
                    List<Order> orders = getAllOrders();
                    resp.getWriter().write(toJson(orders));
                }

            } else if (pathInfo.startsWith("/client/")) {
                String clientIdStr = pathInfo.substring("/client/".length());
                Integer clientId = Integer.parseInt(clientIdStr);
                List<Order> orders = getByClientId(clientId);
                resp.getWriter().write(toJson(orders));

            } else {
                String idStr = pathInfo.substring(1);
                Integer id = Integer.parseInt(idStr);
                Order order = getById(id);

                if (order != null) {
                    resp.getWriter().write(toJsonSingle(order));
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write("{\"error\":\"Order not found\"}");
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
            Order order = parseOrderFromJson(body);

            Order saved = saveOrder(order);
            resp.getWriter().write(toJsonSingle(saved));

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"" + escape(e.getMessage()) + "\"}");
        }
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    // DAO Methods
    private List<Order> getAllOrders() throws SQLException {
        String sql = "SELECT * FROM `order`";
        List<Order> list = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToOrder(rs));
            }
        }
        return list;
    }

    private List<Order> getByClientId(Integer clientId) throws SQLException {
        String sql = "SELECT * FROM `order` WHERE client_id = ?";
        List<Order> list = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, clientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToOrder(rs));
                }
            }
        }
        return list;
    }

    private List<Order> getOrdersWithoutDelivery() throws SQLException {
        String sql = "SELECT * FROM `order` WHERE order_id NOT IN (SELECT order_id FROM delivery_mission)";
        List<Order> list = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToOrder(rs));
            }
        }
        return list;
    }

    private Order getById(Integer id) throws SQLException {
        String sql = "SELECT * FROM `order` WHERE order_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToOrder(rs);
                }
            }
        }
        return null;
    }

    private Order saveOrder(Order order) throws SQLException {
        String sql = "INSERT INTO `order` (client_id, order_date, total_price) VALUES (?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, order.getClientId());
            ps.setDate(2, new java.sql.Date(order.getOrderDate().getTime()));
            ps.setBigDecimal(3, order.getTotalPrice());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    order.setOrderId(keys.getInt(1));
                }
            }
        }
        return order;
    }

    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setOrderId(rs.getInt("order_id"));
        order.setClientId(rs.getInt("client_id"));
        order.setOrderDate(rs.getDate("order_date"));
        order.setTotalPrice(rs.getBigDecimal("total_price"));
        return order;
    }

    // JSON Methods
    private String toJson(List<Order> orders) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < orders.size(); i++) {
            sb.append(toJsonSingle(orders.get(i)));
            if (i < orders.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private String toJsonSingle(Order o) {
        return "{\"orderId\":" + o.getOrderId() + "," +
                "\"clientId\":" + o.getClientId() + "," +
                "\"orderDate\":\"" + o.getOrderDate() + "\"," +
                "\"totalPrice\":" + o.getTotalPrice() + "}";
    }

    private Order parseOrderFromJson(String json) {
        Order order = new Order();
        json = json.replaceAll("[{}\" ]", "");
        String[] pairs = json.split(",");

        for (String pair : pairs) {
            String[] kv = pair.split(":");
            if (kv.length == 2) {
                String key = kv[0].trim();
                String value = kv[1].trim();

                switch (key) {
                    case "clientId":
                        order.setClientId(Integer.parseInt(value));
                        break;
                    case "orderDate":
                        // Einfaches Parsing - ggf. mit SimpleDateFormat erweitern
                        order.setOrderDate(new java.util.Date());
                        break;
                    case "totalPrice":
                        order.setTotalPrice(new BigDecimal(value));
                        break;
                }
            }
        }
        return order;
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\"", "\\\"").replace("\n", " ");
    }
}