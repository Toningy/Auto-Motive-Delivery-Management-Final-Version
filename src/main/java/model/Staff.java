package hk.edu.polyu.automotive_delivery.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "staff")
public class Staff {
    @Id
    @Column(name = "staff_id")
    private Integer staffId;
    
    @OneToOne
    @MapsId
    @JoinColumn(name = "staff_id")
    private Person person;
    
    @Column(name = "salary", precision = 10, scale = 2)
    private BigDecimal salary;
    
    @Column(name = "working_email", length = 50)
    private String workingEmail;
    
    @Column(name = "taxation_number", length = 20)
    private String taxationNumber;
    
    public Staff() {}
    
    public Staff(Person person, BigDecimal salary, String workingEmail, String taxationNumber) {
        this.person = person;
        this.salary = salary;
        this.workingEmail = workingEmail;
        this.taxationNumber = taxationNumber;
    }
    
    // Getters and Setters
    public Integer getStaffId() { return staffId; }
    public void setStaffId(Integer staffId) { this.staffId = staffId; }
    public Person getPerson() { return person; }
    public void setPerson(Person person) { this.person = person; }
    public BigDecimal getSalary() { return salary; }
    public void setSalary(BigDecimal salary) { this.salary = salary; }
    public String getWorkingEmail() { return workingEmail; }
    public void setWorkingEmail(String workingEmail) { this.workingEmail = workingEmail; }
    public String getTaxationNumber() { return taxationNumber; }
    public void setTaxationNumber(String taxationNumber) { this.taxationNumber = taxationNumber; }
}