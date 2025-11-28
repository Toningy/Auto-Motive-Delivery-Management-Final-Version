package hk.edu.polyu.automotive_delivery.entity;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "delivery_mission")
public class DeliveryMission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mission_id")
    private Integer missionId;
    
    @Column(name = "mission_date")
    @Temporal(TemporalType.DATE)
    private Date missionDate;
    
    @Column(name = "status", length = 20)
    private String status;
    
    @ManyToOne
    @JoinColumn(name = "delivery_man_id")
    private DeliveryMan deliveryMan;
    
    @ManyToOne
    @JoinColumn(name = "manager_id")
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