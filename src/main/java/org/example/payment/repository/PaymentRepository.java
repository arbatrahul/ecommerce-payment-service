package org.example.payment.repository;

import org.example.payment.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    Optional<Payment> findByTransactionId(String transactionId);
    
    List<Payment> findByOrderId(Long orderId);
    
    Page<Payment> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    List<Payment> findByStatus(Payment.PaymentStatus status);
    
    List<Payment> findByPaymentMethod(Payment.PaymentMethod paymentMethod);
    
    List<Payment> findByGatewayId(Long gatewayId);
    
    @Query("SELECT p FROM Payment p WHERE p.status = :status AND p.createdAt < :beforeTime")
    List<Payment> findByStatusAndCreatedAtBefore(@Param("status") Payment.PaymentStatus status, 
                                                @Param("beforeTime") LocalDateTime beforeTime);
    
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'COMPLETED' AND p.processedAt >= :fromDate")
    BigDecimal getTotalCompletedPaymentsAmount(@Param("fromDate") LocalDateTime fromDate);
    
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = :status AND p.processedAt >= :fromDate")
    Long countByStatusAndProcessedAtAfter(@Param("status") Payment.PaymentStatus status, 
                                         @Param("fromDate") LocalDateTime fromDate);
    
    @Query("SELECT p.paymentMethod, COUNT(p) FROM Payment p WHERE p.status = 'COMPLETED' AND p.processedAt >= :fromDate GROUP BY p.paymentMethod")
    List<Object[]> getPaymentMethodStats(@Param("fromDate") LocalDateTime fromDate);
    
    @Query("SELECT p.gatewayId, COUNT(p) FROM Payment p WHERE p.status = 'COMPLETED' AND p.processedAt >= :fromDate GROUP BY p.gatewayId")
    List<Object[]> getGatewayStats(@Param("fromDate") LocalDateTime fromDate);
    
    List<Payment> findByGatewayTransactionId(String gatewayTransactionId);
}
