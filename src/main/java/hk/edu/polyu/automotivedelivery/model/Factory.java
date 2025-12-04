package hk.edu.polyu.automotivedelivery.model;


public class Factory {
    private Integer factoryId;
    
    private String location;
    
    public Factory() {}
    
    public Factory(String location) {
        this.location = location;
    }
    
    // Getters and Setters
    public Integer getFactoryId() { return factoryId; }
    public void setFactoryId(Integer factoryId) { this.factoryId = factoryId; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}