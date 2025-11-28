package hk.edu.polyu.automotive_delivery.repository;

import hk.edu.polyu.automotive_delivery.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClientRepository extends JpaRepository<Client, Integer> {
    
    // Find clients by shipping address containing specific text
    List<Client> findByShippingAddressContaining(String address);
    
    // Find clients with pending invoices
    @Query("SELECT DISTINCT c FROM Client c JOIN c.invoices i WHERE i.paymentStatus = 'pending'")
    List<Client> findClientsWithPendingInvoices();
    
    // Find clients who have made orders in a specific date range
    @Query("SELECT DISTINCT c FROM Client c JOIN c.orders o WHERE o.orderDate BETWEEN :startDate AND :endDate")
    List<Client> findClientsWithOrdersBetweenDates(java.util.Date startDate, java.util.Date endDate);
    
    // Count total clients
    @Query("SELECT COUNT(c) FROM Client c")
    Long countTotalClients();
}