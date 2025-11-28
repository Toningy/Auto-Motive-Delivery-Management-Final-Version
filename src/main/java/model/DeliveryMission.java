package model;

import java.util.Date;

public class DeliveryMission {
    private Integer missionId;
    
    private Date missionDate;
    
    private String status;
    
    private DeliveryMan deliveryMan;
    
    private Manager manager;
    
    public DeliveryMission() {}
    
    public DeliveryMission(Date missionDate, String status) {
        this.missionDate = missionDate;
        this.status = status;
    }
    
    // Getters and Setters
    public Integer getMissionId() { return missionId; }
    public void setMissionId(Integer missionId) { this.missionId = missionId; }
    public Date getMissionDate() { return missionDate; }
    public void setMissionDate(Date missionDate) { this.missionDate = missionDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public DeliveryMan getDeliveryMan() { return deliveryMan; }
    public void setDeliveryMan(DeliveryMan deliveryMan) { this.deliveryMan = deliveryMan; }
    public Manager getManager() { return manager; }
    public void setManager(Manager manager) { this.manager = manager; }
}