package web;

import model.DeliveryMission;
import service.DeliveryService;
import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/api/delivery/*")
public class DeliveryServlet extends HttpServlet {
    private DeliveryService deliveryService = new DeliveryService();
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
                List<DeliveryMission> missions = deliveryService.getAllMissions();
                resp.getWriter().write(gson.toJson(missions));
            } else if (pathInfo.startsWith("/missions/delivery-man/")) {
                String deliveryManIdStr = pathInfo.substring("/missions/delivery-man/".length());
                Integer deliveryManId = Integer.parseInt(deliveryManIdStr);
                List<DeliveryMission> missions = deliveryService.getMissionsByDeliveryMan(deliveryManId);
                resp.getWriter().write(gson.toJson(missions));
            } else if (pathInfo.startsWith("/missions/pending")) {
                List<DeliveryMission> missions = deliveryService.getPendingMissions();
                resp.getWriter().write(gson.toJson(missions));
            } else if (pathInfo.startsWith("/missions/status/")) {
                String status = pathInfo.substring("/missions/status/".length());
                List<DeliveryMission> missions = deliveryService.getMissionsByStatus(status);
                resp.getWriter().write(gson.toJson(missions));
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
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
            if (pathInfo != null && pathInfo.startsWith("/missions/") && pathInfo.contains("/status")) {
                String[] parts = pathInfo.split("/");
                Integer missionId = Integer.parseInt(parts[2]);
                String status = req.getParameter("status");

                DeliveryMission mission = deliveryService.updateMissionStatus(missionId, status);
                if (mission != null) {
                    resp.getWriter().write(gson.toJson(mission));
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write("{\"error\":\"Mission not found\"}");
                }
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, PUT, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        resp.setStatus(HttpServletResponse.SC_OK);
    }
}