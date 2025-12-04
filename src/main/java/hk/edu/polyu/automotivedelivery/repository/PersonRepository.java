package hk.edu.polyu.automotivedelivery.repository;

import hk.edu.polyu.automotivedelivery.db.DBUtil;
import hk.edu.polyu.automotivedelivery.model.Person;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PersonRepository {
    
    public Person findById(Integer personId) {
        String sql = "SELECT person_id, name, phone_number, birthdate, email FROM person WHERE person_id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, personId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToPerson(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding person by id: " + e.getMessage(), e);
        }
        return null;
    }
    
    public List<Person> findAll() {
        List<Person> result = new ArrayList<>();
        String sql = "SELECT person_id, name, phone_number, birthdate, email FROM person";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                result.add(mapRowToPerson(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching all persons: " + e.getMessage(), e);
        }
        return result;
    }
    
    private Person mapRowToPerson(ResultSet rs) throws SQLException {
        Person person = new Person();
        person.setPersonId(rs.getInt("person_id"));
        person.setName(rs.getString("name"));
        person.setPhoneNumber(rs.getString("phone_number"));
        person.setBirthdate(rs.getDate("birthdate"));
        person.setEmail(rs.getString("email"));
        return person;
    }
}