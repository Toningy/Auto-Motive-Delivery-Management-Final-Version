package hk.edu.polyu.automotivedelivery.repository;

import hk.edu.polyu.automotivedelivery.db.DBUtil;
import hk.edu.polyu.automotivedelivery.model.Client;
import hk.edu.polyu.automotivedelivery.model.Person;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ClientRepository {
    
    public Client findById(Integer clientId) {
        String sql = "SELECT c.client_id, c.shipping_address, c.billing_address, " +
                     "p.name, p.email, p.phone_number, p.birthdate " +
                     "FROM client c " +
                     "JOIN person p ON c.client_id = p.person_id " +
                     "WHERE c.client_id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, clientId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToClient(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding client by id: " + e.getMessage(), e);
        }
        return null;
    }
    
    public List<Client> findAll() {
        List<Client> result = new ArrayList<>();
        String sql = "SELECT c.client_id, c.shipping_address, c.billing_address, " +
                     "p.name, p.email, p.phone_number, p.birthdate " +
                     "FROM client c " +
                     "JOIN person p ON c.client_id = p.person_id";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                result.add(mapRowToClient(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching all clients: " + e.getMessage(), e);
        }
        return result;
    }
    
    private Client mapRowToClient(ResultSet rs) throws SQLException {
        Client client = new Client();
        client.setClientId(rs.getInt("client_id"));
        client.setShippingAddress(rs.getString("shipping_address"));
        client.setBillingAddress(rs.getString("billing_address"));
        
        Person person = new Person();
        person.setPersonId(rs.getInt("client_id"));
        person.setName(rs.getString("name"));
        person.setEmail(rs.getString("email"));
        person.setPhoneNumber(rs.getString("phone_number"));
        person.setBirthdate(rs.getDate("birthdate"));
        
        client.setPerson(person);
        return client;
    }
}