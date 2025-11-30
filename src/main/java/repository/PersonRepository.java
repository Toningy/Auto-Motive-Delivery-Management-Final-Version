package hk.edu.polyu.automotivedelivery.repository;
import db.DBUtil;
import model.Person;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PersonRepository {

    private Person mapRowToPerson(ResultSet rs) throws SQLException {
        Person p = new Person();
        p.setPersonId(rs.getInt("person_id"));
        p.setName(rs.getString("name"));
        p.setPhoneNumber(rs.getString("phone_number"));
        Date birthdate = rs.getDate("birthdate");
        p.setBirthdate(birthdate);
        p.setEmail(rs.getString("email"));
        return p;
    }

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
            throw new RuntimeException("Error finding person by id", e);
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
            throw new RuntimeException("Error fetching all persons", e);
        }
        return result;
    }
}
