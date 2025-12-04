package model;

import java.math.BigDecimal;

public class Staff {
    private Integer staffId;
    
    private Person person;
    
    private BigDecimal salary;
    
    private String workingEmail;
    
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