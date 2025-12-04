package web;

import service.ReportService;
import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/reports/*")
public class ReportServlet extends HttpServlet {
    private ReportService reportService = new ReportService();
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
                // Return available report types
                Map<String, String> availableReports = new HashMap<>();
                availableReports.put("revenue", "/api/reports/revenue?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD");
                availableReports.put("delivery-performance", "/api/reports/delivery-performance");
                availableReports.put("order-summary", "/api/reports/order-summary");
                availableReports.put("delivery-summary", "/api/reports/delivery-summary");
                
                resp.getWriter().write(gson.toJson(availableReports));
                
            } else if ("/revenue".equals(pathInfo)) {
                handleRevenueReport(req, resp);
                
            } else if ("/delivery-performance".equals(pathInfo)) {
                handleDeliveryPerformanceReport(resp);
                
            } else if ("/order-summary".equals(pathInfo)) {
                handleOrderSummaryReport(resp);
                
            } else if ("/delivery-summary".equals(pathInfo)) {
                handleDeliverySummaryReport(resp);
                
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\":\"Report endpoint not found\"}");
            }

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private void handleRevenueReport(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String startDateStr = req.getParameter("startDate");
        String endDateStr = req.getParameter("endDate");

        if (startDateStr == null || endDateStr == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"startDate and endDate parameters are required\"}");
            return;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date startDate = sdf.parse(startDateStr);
            Date endDate = sdf.parse(endDateStr);

            Map<String, Object> report = reportService.getSalesReport(startDate, endDate);
            resp.getWriter().write(gson.toJson(report));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Invalid date format. Use YYYY-MM-DD\"}");
        }
    }

    private void handleDeliveryPerformanceReport(HttpServletResponse resp) throws IOException {
        try {
            Map<String, Object> performance = reportService.getDeliveryPerformanceReport();
            resp.getWriter().write(gson.toJson(performance));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"Error generating delivery performance report\"}");
        }
    }

    private void handleOrderSummaryReport(HttpServletResponse resp) throws IOException {
        try {
            Map<String, Long> summary = reportService.getOrderStatusSummary();
            resp.getWriter().write(gson.toJson(summary));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"Error generating order summary report\"}");
        }
    }

    private void handleDeliverySummaryReport(HttpServletResponse resp) throws IOException {
        try {
            Map<String, Long> summary = reportService.getDeliveryStatusSummary();
            resp.getWriter().write(gson.toJson(summary));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"Error generating delivery summary report\"}");
        }
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        resp.setStatus(HttpServletResponse.SC_OK);
    }
}