package hk.edu.polyu.automotivedelivery.service;

import hk.edu.polyu.automotivedelivery.db.DBUtil;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;

public class AuthService {
    
    public boolean authenticate(String email, String password) {
        String sql = "SELECT password FROM users WHERE email = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String hashedPassword = rs.getString("password");
                    // For demo, accept plain password or hashed
                    return password.equals(hashedPassword) || 
                           BCrypt.checkpw(password, hashedPassword);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean register(String name, String email, String password, String role) {
        String sql = "INSERT INTO users (email, password, name, role) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            // Hash the password
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
            
            ps.setString(1, email);
            ps.setString(2, hashedPassword);
            ps.setString(3, name);
            ps.setString(4, role);
            
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public User getUserByEmail(String email) {
        String sql = "SELECT id, email, name, role FROM users WHERE email = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setEmail(rs.getString("email"));
                    user.setName(rs.getString("name"));
                    user.setRole(rs.getString("role"));
                    return user;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // Inner class for user data
    public static class User {
        private Integer id;
        private String email;
        private String name;
        private String role;
        
        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }
}