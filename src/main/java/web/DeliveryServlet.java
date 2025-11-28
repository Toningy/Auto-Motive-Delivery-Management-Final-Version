package web;

import db.DBUtil;
import model.DeliveryMission;

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
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/api/delivery/missions/*")
public class DeliveryServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");

        String pathInfo = req.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                List<DeliveryMission> missions = getAllMissions();
                resp.getWriter().write(toJson(missions));

            } else if (pathInfo.startsWith("/delivery-man/")) {
                String[] parts = pathInfo.substring("/delivery-man/".length()).split("/");
                Integer deliveryManId = Integer.parseInt(parts[0]);

                if (parts.length > 1 && "pending".equals(parts[1])) {
                    List<DeliveryMission> missions = getPendingByDeliveryMan(deliveryManId);
                    resp.getWriter().write(toJson(missions));
                } else {
                    List<DeliveryMission> missions = getByDeliveryMan(deliveryManId);
                    resp.getWriter().write(toJson(missions));
                }

            } else if (pathInfo.startsWith("/status/")) {
                String status = pathInfo.substring("/status/".length());
                List<DeliveryMission> missions = getByStatus(status);
                resp.getWriter().write(toJson(missions));

            } else {
                String idStr = pathInfo.substring(1);
                Integer id = Integer.parseInt(idStr);
                DeliveryMission mission = getById(id);

                if (mission != null) {
                    resp.getWriter().write(toJsonSingle(mission));
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write("{\"error\":\"Mission not found\"}");
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
            DeliveryMission mission = parseMissionFromJson(body);

            DeliveryMission saved = saveMission(mission);
            resp.getWriter().write(toJsonSingle(saved));

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"" + escape(e.getMessage()) + "\"}");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");

        String pathInfo = req.getPathInfo();

        try {
            if (pathInfo != null && pathInfo.contains("/status")) {
                String idStr = pathInfo.substring(1, pathInfo.indexOf("/status"));
                Integer id = Integer.parseInt(idStr);
                String status = req.getParameter("status");

                DeliveryMission updated = updateStatus(id, status);
                if (updated != null) {
                    resp.getWriter().write(toJsonSingle(updated));
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write("{\"error\":\"Mission not found\"}");
                }
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"" + escape(e.getMessage()) + "\"}");
        }
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    // DAO Methods
    private List<DeliveryMission> getAllMissions() throws SQLException {
        String sql = "SELECT * FROM delivery_mission";
        List<DeliveryMission> list = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToMission(rs));
            }
        }
        return list;
    }

    private List<DeliveryMission> getByDeliveryMan(Integer deliveryManId) throws SQLException {
        String sql = "SELECT * FROM delivery_mission WHERE delivery_man_id = ?";
        List<DeliveryMission> list = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, deliveryManId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToMission(rs));
                }
            }
        }
        return list;
    }

    private List<DeliveryMission> getPendingByDeliveryMan(Integer deliveryManId) throws SQLException {
        String sql = "SELECT * FROM delivery_mission WHERE delivery_man_id = ? AND status = 'pending'";
        List<DeliveryMission> list = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, deliveryManId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToMission(rs));
                }
            }
        }
        return list;
    }

    private List<DeliveryMission> getByStatus(String status) throws SQLException {
        String sql = "SELECT * FROM delivery_mission WHERE status = ?";
        List<DeliveryMission> list = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToMission(rs));
                }
            }
        }
        return list;
    }

    private DeliveryMission getById(Integer id) throws SQLException {
        String sql = "SELECT * FROM delivery_mission WHERE mission_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToMission(rs);
                }
            }
        }
        return null;
    }

    private DeliveryMission saveMission(DeliveryMission mission) throws SQLException {
        String sql = "INSERT INTO delivery_mission (order_id, delivery_man_id, vehicle_id, status, start_date, end_date) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, mission.getOrderId());
            ps.setInt(2, mission.getDeliveryManId());
            ps.setInt(3, mission.getVehicleId());
            ps.setString(4, mission.getStatus());
            ps.setDate(5, mission.getStartDate() != null ? new java.sql.Date(mission.getStartDate().getTime()) : null);
            ps.setDate(6, mission.getEndDate() != null ? new java.sql.Date(mission.getEndDate().getTime()) : null);

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    mission.setMissionId(keys.getInt(1));
                }
            }
        }
        return mission;
    }

    private DeliveryMission updateStatus(Integer id, String status) throws SQLException {
        String sql = "UPDATE delivery_mission SET status = ? WHERE mission_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, id);

            int updated = ps.executeUpdate();
            if (updated > 0) {
                return getById(id);
            }
        }
        return null;
    }

    private DeliveryMission mapResultSetToMission(ResultSet rs) throws SQLException {
        DeliveryMission mission = new DeliveryMission();
        mission.setMissionId(rs.getInt("mission_id"));
        mission.setOrderId(rs.getInt("order_id"));
        mission.setDeliveryManId(rs.getInt("delivery_man_id"));
        mission.setVehicleId(rs.getInt("vehicle_id"));
        mission.setStatus(rs.getString("status"));
        mission.setStartDate(rs.getDate("start_date"));
        mission.setEndDate(rs.getDate("end_date"));
        return mission;
    }

    // JSON Methods
    private String toJson(List<DeliveryMission> missions) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < missions.size(); i++) {
            sb.append(toJsonSingle(missions.get(i)));
            if (i < missions.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private String toJsonSingle(DeliveryMission m) {
        return "{\"missionId\":" + m.getMissionId() + "," +
                "\"orderId\":" + m.getOrderId() + "," +
                "\"deliveryManId\":" + m.getDeliveryManId() + "," +
                "\"vehicleId\":" + m.getVehicleId() + "," +
                "\"status\":\"" + escape(m.getStatus()) + "\"," +
                "\"startDate\":\"" + m.getStartDate() + "\"," +
                "\"endDate\":\"" + m.getEndDate() + "\"}";
    }

    private DeliveryMission parseMissionFromJson(String json) {
        DeliveryMission mission = new DeliveryMission();
        // Simple parsing - adapt based on your DeliveryMission fields
        return mission;
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\"", "\\\"").replace("\n", " ");
    }
}