package hk.edu.polyu.automotive_delivery.repository;

import hk.edu.polyu.automotive_delivery.entity.DeliveryMission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface DeliveryMissionRepository extends JpaRepository<DeliveryMission, Integer> {
    List<DeliveryMission> findByDeliveryMan_StaffId(Integer deliveryManId);
    List<DeliveryMission> findByStatus(String status);
    
    @Query("SELECT dm FROM DeliveryMission dm WHERE dm.missionDate BETWEEN :startDate AND :endDate")
    List<DeliveryMission> findMissionsBetweenDates(Date startDate, Date endDate);
    
    @Query("SELECT dm FROM DeliveryMission dm WHERE dm.deliveryMan.staffId = :deliveryManId AND dm.status = 'pending'")
    List<DeliveryMission> findPendingMissionsByDeliveryMan(Integer deliveryManId);
}