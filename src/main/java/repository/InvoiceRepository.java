package hk.edu.polyu.automotive_delivery.repository;

import hk.edu.polyu.automotive_delivery.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Integer> {
    List<Invoice> findByClient_ClientId(Integer clientId);
    List<Invoice> findByPaymentStatus(String paymentStatus);
    
    @Query("SELECT i FROM Invoice i WHERE i.amount > :minAmount")
    List<Invoice> findInvoicesAboveAmount(Double minAmount);
}