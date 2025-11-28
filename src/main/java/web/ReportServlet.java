package web;

import db.DBUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

@WebServlet("/api/reports/*")
public class ReportServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");

        String pathInfo = req.getPathInfo();

        try {
            if ("/revenue".equals(pathInfo)) {
                String startDateStr = req.getParameter("startDate");
                String endDateStr = req.getParameter("endDate");

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date startDate = sdf.parse(startDateStr);
                Date endDate = sdf.parse(endDateStr);

                Double revenue = getTotalRevenue(startDate, endDate);
                Long totalOrders = getTotalOrders(startDate, endDate);
                Long completedMissions = getCompletedMissions(startDate, endDate);

                Double avgOrderValue = totalOrders > 0 ? revenue / totalOrders : 0.0;

                String json = "{\"startDate\":\"" + startDateStr + "\"," +
                        "\"endDate\":\"" + endDateStr + "\"," +
                        "\"totalRevenue\":" + revenue + "," +
                        "\"totalOrders\":" + totalOrders + "," +
                        "\"completedMissions\":" + completedMissions + "," +
                        "\"averageOrderValue\":" + avgOrderValue + "}";

                resp.getWriter().write(json);

            } else if ("/delivery-performance".equals(pathInfo)) {
                // Dummy-Daten – kannst du später mit echten SQL-Queries ersetzen
                String json = "{\"totalMissions\":150," +
                        "\"completedMissions\":120," +
                        "\"successRate\":80.0," +
                        "\"averageDeliveryTime\":\"2.5 days\"}";
                resp.getWriter().write(json);
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\":\"Unknown report endpoint\"}");
            }

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"" + escape(e.getMessage()) + "\"}");
        }
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    // DAO Methods
    private Double getTotalRevenue(Date startDate, Date endDate) throws SQLException {
        String sql = "SELECT SUM(total_price) FROM `order` WHERE order_date BETWEEN ? AND ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, new java.sql.Date(startDate.getTime()));
            ps.setDate(2, new java.sql.Date(endDate.getTime()));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        }
        return 0.0;
    }

    private Long getTotalOrders(Date startDate, Date endDate) throws SQLException {
        String sql = "SELECT COUNT(*) FROM `order` WHERE order_date BETWEEN ? AND ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, new java.sql.Date(startDate.getTime()));
            ps.setDate(2, new java.sql.Date(endDate.getTime()));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        return 0L;
    }

    private Long getCompletedMissions(Date startDate, Date endDate) throws SQLException {
        String sql = "SELECT COUNT(*) FROM delivery_mission " +
                "WHERE status = 'completed' AND end_date BETWEEN ? AND ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, new java.sql.Date(startDate.getTime()));
            ps.setDate(2, new java.sql.Date(endDate.getTime()));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        return 0L;
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\"", "\\\"").replace("\n", " ");
    }
}