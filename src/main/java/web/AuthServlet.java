package web;

import com.google.gson.Gson;
import db.DBUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/auth/*")
public class AuthServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");

        String pathInfo = req.getPathInfo();

        try {
            if ("/login".equals(pathInfo)) {
                handleLogin(req, resp);
            } else if ("/register".equals(pathInfo)) {
                handleRegister(req, resp);
            } else if ("/logout".equals(pathInfo)) {
                handleLogout(req, resp);
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\":\"Endpoint not found\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    // -------------------- LOGIN --------------------
    private void handleLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Map<String, String> body = gson.fromJson(req.getReader(), Map.class);
        String email = body.get("email");
        String password = body.get("password");

        if (email == null || password == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Email and password required\"}");
            return;
        }

        // Check user credentials
        String userSql = "SELECT id, email, name, role, password FROM users WHERE email = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement userPs = conn.prepareStatement(userSql)) {

            userPs.setString(1, email);
            try (ResultSet userRs = userPs.executeQuery()) {
                if (userRs.next()) {
                    String storedPassword = userRs.getString("password");

                    // Check password (plain text for demo)
                    if (storedPassword != null && storedPassword.equals(password)) {
                        
                        // Now get the client_id from person table if user is CLIENT
                        Integer clientId = null;
                        if ("CLIENT".equals(userRs.getString("role"))) {
                            clientId = getClientIdByEmail(email, conn);
                        }
                        
                        Map<String, Object> response = new HashMap<>();
                        response.put("success", true);
                        response.put("id", userRs.getInt("id"));
                        response.put("email", userRs.getString("email"));
                        response.put("name", userRs.getString("name"));
                        response.put("role", userRs.getString("role"));
                        
                        // Add client_id if user is a client
                        if (clientId != null) {
                            response.put("clientId", clientId);
                        }

                        // Create session
                        HttpSession session = req.getSession(true);
                        Map<String, Object> sessionUser = new HashMap<>();
                        sessionUser.put("id", userRs.getInt("id"));
                        sessionUser.put("email", userRs.getString("email"));
                        sessionUser.put("name", userRs.getString("name"));
                        sessionUser.put("role", userRs.getString("role"));
                        if (clientId != null) {
                            sessionUser.put("clientId", clientId);
                        }
                        session.setAttribute("user", sessionUser);
                        session.setMaxInactiveInterval(30 * 60); // 30 minutes

                        resp.getWriter().write(gson.toJson(response));
                        return;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // If we get here, authentication failed
        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        resp.getWriter().write("{\"error\":\"Invalid credentials\"}");
    }

    // Helper method to get client_id from person email
    private Integer getClientIdByEmail(String email, Connection conn) throws SQLException {
        String sql = "SELECT c.client_id FROM client c " +
                     "JOIN person p ON c.client_id = p.person_id " +
                     "WHERE p.email = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("client_id");
                }
            }
        }
        return null;
    }

    // -------------------- REGISTER --------------------
    private void handleRegister(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Map<String, String> body = gson.fromJson(req.getReader(), Map.class);
        String name = body.get("name");
        String email = body.get("email");
        String password = body.get("password");
        String role = body.get("role");

        if (name == null || email == null || password == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"All fields required\"}");
            return;
        }

        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false); // Start transaction
            
            // Check if email already exists
            String checkSql = "SELECT id FROM users WHERE email = ?";
            try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
                checkPs.setString(1, email);
                try (ResultSet rs = checkPs.executeQuery()) {
                    if (rs.next()) {
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        resp.getWriter().write("{\"error\":\"Email already registered\"}");
                        return;
                    }
                }
            }

            // Insert new user
            String insertSql = "INSERT INTO users (email, password, name, role) VALUES (?, ?, ?, ?)";
            Integer userId = null;
            
            try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, email);
                ps.setString(2, password); // Plain text for demo
                ps.setString(3, name);
                ps.setString(4, role != null ? role : "CLIENT");

                int affectedRows = ps.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Creating user failed, no rows affected.");
                }

                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        userId = generatedKeys.getInt(1);
                    } else {
                        throw new SQLException("Creating user failed, no ID obtained.");
                    }
                }
            }

            // If registering as CLIENT, also create person and client records
            if ("CLIENT".equals(role)) {
                // First create person
                String personSql = "INSERT INTO person (name, email) VALUES (?, ?)";
                Integer personId = null;
                
                try (PreparedStatement personPs = conn.prepareStatement(personSql, Statement.RETURN_GENERATED_KEYS)) {
                    personPs.setString(1, name);
                    personPs.setString(2, email);
                    personPs.executeUpdate();
                    
                    try (ResultSet generatedKeys = personPs.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            personId = generatedKeys.getInt(1);
                        }
                    }
                }
                
                // Then create client
                if (personId != null) {
                    String clientSql = "INSERT INTO client (client_id, shipping_address, billing_address) " +
                                      "VALUES (?, 'Default Shipping', 'Default Billing')";
                    try (PreparedStatement clientPs = conn.prepareStatement(clientSql)) {
                        clientPs.setInt(1, personId);
                        clientPs.executeUpdate();
                    }
                }
            }

            conn.commit(); // Commit transaction
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("id", userId);
            response.put("email", email);
            response.put("name", name);
            response.put("role", role != null ? role : "CLIENT");
            
            resp.getWriter().write(gson.toJson(response));
            
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"Registration failed: " + e.getMessage() + "\"}");
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // -------------------- LOGOUT --------------------
    private void handleLogout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Logged out");
        resp.getWriter().write(gson.toJson(response));
    }
}