package model;

import java.util.Date;

public class DeliveryMission {
    private Integer missionId;

    private Integer orderId;          // FK auf Order
    private Integer deliveryManId;    // FK auf DeliveryMan (Staff)
    private Integer vehicleId;        // FK auf DeliveryVehicle

    private String status;
    private Date startDate;
    private Date endDate;

    // Optional: alte Felder, wenn du sie noch brauchst
    // private Date missionDate;
    // private DeliveryMan deliveryMan;
    // private Manager manager;

    public DeliveryMission() {}

    public DeliveryMission(Integer orderId, Integer deliveryManId, Integer vehicleId,
                           String status, Date startDate, Date endDate) {
        this.orderId = orderId;
        this.deliveryManId = deliveryManId;
        this.vehicleId = vehicleId;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Integer getMissionId() { return missionId; }
    public void setMissionId(Integer missionId) { this.missionId = missionId; }

    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }

    public Integer getDeliveryManId() { return deliveryManId; }
    public void setDeliveryManId(Integer deliveryManId) { this.deliveryManId = deliveryManId; }

    public Integer getVehicleId() { return vehicleId; }
    public void setVehicleId(Integer vehicleId) { this.vehicleId = vehicleId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }

    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }
}