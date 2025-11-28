package hk.edu.polyu.automotive_delivery.entity;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "person")
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "person_id")
    private Integer personId;
    
    @Column(name = "name", length = 50, nullable = false)
    private String name;
    
    @Column(name = "phone_number", length = 25)
    private String phoneNumber;
    
    @Column(name = "birthdate")
    @Temporal(TemporalType.DATE)
    private Date birthdate;
    
    @Column(name = "email", length = 50)
    private String email;
    
    public Person() {}
    
    public Person(String name, String phoneNumber, Date birthdate, String email) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.birthdate = birthdate;
        this.email = email;
    }
    
    // Getters and Setters
    public Integer getPersonId() { return personId; }
    public void setPersonId(Integer personId) { this.personId = personId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public Date getBirthdate() { return birthdate; }
    public void setBirthdate(Date birthdate) { this.birthdate = birthdate; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}