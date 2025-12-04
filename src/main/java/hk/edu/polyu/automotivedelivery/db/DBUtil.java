package hk.edu.polyu.automotivedelivery.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtil {
    // CHANGE THESE TO YOUR DATABASE CREDENTIALS
    private static final String URL = "jdbc:mysql://localhost:3306/automotive_delivery_db";
    private static final String USER = "root";
    private static final String PASSWORD = "";  // Your MySQL password here
    
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("MySQL JDBC Driver not found");
        }
    }
    
    public static Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.err.println("Database connection failed!");
            System.err.println("URL: " + URL);
            System.err.println("User: " + USER);
            throw e;
        }
    }
    
    // Test connection
    public static void testConnection() {
        try (Connection conn = getConnection()) {
            System.out.println("✅ Database connection successful!");
        } catch (SQLException e) {
            System.err.println("❌ Database connection failed: " + e.getMessage());
        }
    }
}