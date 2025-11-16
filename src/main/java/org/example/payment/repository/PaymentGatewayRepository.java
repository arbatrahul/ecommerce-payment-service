package org.example.payment.repository;

import org.example.payment.entity.PaymentGateway;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentGatewayRepository extends JpaRepository<PaymentGateway, Long> {
    
    Optional<PaymentGateway> findByName(String name);
    
    List<PaymentGateway> findByIsActiveTrue();
    
    List<PaymentGateway> findByType(PaymentGateway.GatewayType type);
    
    List<PaymentGateway> findByIsActiveTrueAndIsTestMode(Boolean isTestMode);
    
    @Query("SELECT pg FROM PaymentGateway pg WHERE pg.isActive = true AND " +
           "(pg.minAmount IS NULL OR pg.minAmount <= :amount) AND " +
           "(pg.maxAmount IS NULL OR pg.maxAmount >= :amount)")
    List<PaymentGateway> findActiveGatewaysForAmount(@Param("amount") BigDecimal amount);
    
    @Query("SELECT pg FROM PaymentGateway pg WHERE pg.isActive = true AND pg.type = :type AND " +
           "(pg.minAmount IS NULL OR pg.minAmount <= :amount) AND " +
           "(pg.maxAmount IS NULL OR pg.maxAmount >= :amount)")
    List<PaymentGateway> findActiveGatewaysByTypeForAmount(@Param("type") PaymentGateway.GatewayType type, 
                                                          @Param("amount") BigDecimal amount);
}
