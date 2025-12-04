package service;

import model.DeliveryMission;
import repository.DeliveryMissionRepository;

import java.util.List;

public class DeliveryService {
    private DeliveryMissionRepository deliveryMissionRepository = new DeliveryMissionRepository();

    public List<DeliveryMission> getAllMissions() {
        return deliveryMissionRepository.findAll();
    }

    public List<DeliveryMission> getMissionsByDeliveryMan(Integer deliveryManId) {
        return deliveryMissionRepository.findByDeliveryManId(deliveryManId);
    }

    public List<DeliveryMission> getMissionsByStatus(String status) {
        return deliveryMissionRepository.findByStatus(status);
    }

    public List<DeliveryMission> getPendingMissions() {
        return deliveryMissionRepository.findByStatus("PENDING");
    }

    public DeliveryMission updateMissionStatus(Integer missionId, String status) {
        return deliveryMissionRepository.updateStatus(missionId, status);
    }
}