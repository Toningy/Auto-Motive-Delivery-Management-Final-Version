package hk.edu.polyu.automotivedelivery.web;
import hk.edu.polyu.automotivedelivery.db.DBUtil;
import hk.edu.polyu.automotivedelivery.model.Client;
import hk.edu.polyu.automotivedelivery.model.Person;

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
import java.util.ArrayList;
import java.util.List;

@WebServlet("/api/clients/*")
public class ClientServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");

        String pathInfo = req.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                List<Client> clients = getAllClients();
                resp.getWriter().write(toJson(clients));

            } else {
                String idStr = pathInfo.substring(1);
                Integer id = Integer.parseInt(idStr);
                Client client = getById(id);

                if (client != null) {
                    resp.getWriter().write(toJsonSingle(client));
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write("{\"error\":\"Client not found\"}");
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
        resp.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    // DAO Methods
    private List<Client> getAllClients() throws SQLException {
        String sql = "SELECT * FROM client";
        List<Client> list = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToClient(rs));
            }
        }
        return list;
    }

    private Client getById(Integer id) throws SQLException {
        String sql = "SELECT * FROM client WHERE client_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToClient(rs);
                }
            }
        }
        return null;
    }

    private Client mapResultSetToClient(ResultSet rs) throws SQLException {
        Client client = new Client();
        client.setClientId(rs.getInt("client_id"));

        // Person-Objekt erstellen und befüllen
        Person person = new Person();
        person.setName(rs.getString("name"));
        person.setEmail(rs.getString("email"));
        person.setPhoneNumber(rs.getString("phone"));
        person.setAddress(rs.getString("address"));
        client.setPerson(person);

        // Adressen am Client
        client.setShippingAddress(rs.getString("shipping_address"));
        client.setBillingAddress(rs.getString("billing_address"));

        return client;
    }

    // JSON Methods
    private String toJson(List<Client> clients) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < clients.size(); i++) {
            sb.append(toJsonSingle(clients.get(i)));
            if (i < clients.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private String toJsonSingle(Client c) {
        return "{"
                + "\"clientId\":" + c.getClientId() + ","
                + "\"name\":\"" + escape(c.getPerson().getName()) + "\","
                + "\"email\":\"" + escape(c.getPerson().getEmail()) + "\","
                + "\"phone\":\"" + escape(c.getPerson().getPhoneNumber()) + "\","
                + "\"address\":\"" + escape(c.getPerson().getAddress()) + "\","
                + "\"shippingAddress\":\"" + escape(c.getShippingAddress()) + "\","
                + "\"billingAddress\":\"" + escape(c.getBillingAddress()) + "\""
                + "}";
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\"", "\\\"").replace("\n", " ");
    }
}