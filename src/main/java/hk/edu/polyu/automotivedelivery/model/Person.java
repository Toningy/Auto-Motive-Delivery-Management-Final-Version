package hk.edu.polyu.automotivedelivery.model;

import java.util.Date;

public class Person {
    private Integer personId;
    
    private String name;
    
    private String phoneNumber;
    
    private Date birthdate;
    
    private String email;

    private String address;
    
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
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}