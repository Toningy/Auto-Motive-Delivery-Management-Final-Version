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
    private Gson gson = new Gson();
    
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
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
    
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
        String sql = "SELECT id, email, name, role, password FROM users WHERE email = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("password");
                    
                    // For demo, use simple password comparison
                    if (password.equals(storedPassword)) {
                        Map<String, Object> response = new HashMap<>();
                        response.put("success", true);
                        
                        Map<String, Object> user = new HashMap<>();
                        user.put("id", rs.getInt("id"));
                        user.put("email", rs.getString("email"));
                        user.put("name", rs.getString("name"));
                        user.put("role", rs.getString("role"));
                        
                        response.put("user", user);
                        
                        // Create session
                        HttpSession session = req.getSession(true);
                        session.setAttribute("user", user);
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
        
        // Check if email already exists
        String checkSql = "SELECT id FROM users WHERE email = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(checkSql)) {
            
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write("{\"error\":\"Email already registered\"}");
                    return;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"Registration failed\"}");
            return;
        }
        
        // Insert new user
        String insertSql = "INSERT INTO users (email, password, name, role) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setString(1, email);
            ps.setString(2, password); // In production, hash this!
            ps.setString(3, name);
            ps.setString(4, role != null ? role : "CLIENT");
            
            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Registration successful");
                
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        response.put("userId", keys.getInt(1));
                    }
                }
                
                resp.getWriter().write(gson.toJson(response));
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\":\"Registration failed\"}");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"Registration failed\"}");
        }
    }
    
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