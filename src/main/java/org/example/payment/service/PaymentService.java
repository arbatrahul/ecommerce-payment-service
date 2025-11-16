package org.example.payment.service;

import org.example.payment.entity.Payment;
import org.example.payment.entity.PaymentGateway;
import org.example.payment.repository.PaymentRepository;
import org.example.payment.repository.PaymentGatewayRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private PaymentGatewayRepository paymentGatewayRepository;
    
    @Autowired
    private PaymentGatewayService paymentGatewayService;
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public Payment createPayment(Long orderId, Long userId, BigDecimal amount, 
                               Payment.PaymentMethod paymentMethod, Map<String, Object> cardDetails) {
        
        // Find suitable payment gateway
        PaymentGateway gateway = findSuitableGateway(amount, paymentMethod);
        if (gateway == null) {
            throw new RuntimeException("No suitable payment gateway found for amount: " + amount);
        }
        
        // Generate transaction ID
        String transactionId = generateTransactionId();
        
        // Create payment record
        Payment payment = new Payment(transactionId, orderId, userId, amount, paymentMethod, gateway.getId());
        
        // Extract card details for logging (last 4 digits only)
        if (cardDetails != null && cardDetails.containsKey("cardNumber")) {
            String cardNumber = cardDetails.get("cardNumber").toString();
            payment.setCardLastFour(cardNumber.substring(Math.max(0, cardNumber.length() - 4)));
            payment.setCardType(determineCardType(cardNumber));
        }
        
        Payment savedPayment = paymentRepository.save(payment);
        
        // Send payment created event
        kafkaTemplate.send("payment-events", "payment-created", 
            createPaymentEvent(savedPayment, "PAYMENT_CREATED"));
        
        return savedPayment;
    }

    public Payment processPayment(String transactionId, Map<String, Object> cardDetails) {
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + transactionId));
        
        if (payment.getStatus() != Payment.PaymentStatus.PENDING) {
            throw new RuntimeException("Payment is not in pending status: " + payment.getStatus());
        }
        
        // Mark as processing
        payment.markAsProcessing();
        paymentRepository.save(payment);
        
        try {
            // Get payment gateway
            PaymentGateway gateway = paymentGatewayRepository.findById(payment.getGatewayId())
                    .orElseThrow(() -> new RuntimeException("Payment gateway not found"));
            
            // Process payment through gateway
            PaymentGatewayService.PaymentResult result = paymentGatewayService.processPayment(
                gateway, payment, cardDetails);
            
            if (result.isSuccess()) {
                // Mark as completed
                payment.markAsCompleted(result.getGatewayTransactionId(), result.getGatewayResponse());
                
                // Send payment completed event
                kafkaTemplate.send("payment-events", "payment-completed", 
                    createPaymentEvent(payment, "PAYMENT_COMPLETED"));
                
            } else {
                // Mark as failed
                payment.markAsFailed(result.getFailureReason(), result.getGatewayResponse());
                
                // Send payment failed event
                kafkaTemplate.send("payment-events", "payment-failed", 
                    createPaymentEvent(payment, "PAYMENT_FAILED"));
            }
            
        } catch (Exception e) {
            // Mark as failed due to exception
            payment.markAsFailed("Payment processing error: " + e.getMessage(), null);
            
            // Send payment failed event
            kafkaTemplate.send("payment-events", "payment-failed", 
                createPaymentEvent(payment, "PAYMENT_FAILED"));
        }
        
        return paymentRepository.save(payment);
    }

    public Payment refundPayment(String transactionId, BigDecimal refundAmount, String reason) {
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + transactionId));
        
        if (payment.getStatus() != Payment.PaymentStatus.COMPLETED) {
            throw new RuntimeException("Payment is not in completed status: " + payment.getStatus());
        }
        
        if (refundAmount.compareTo(payment.getAmount()) > 0) {
            throw new RuntimeException("Refund amount cannot exceed payment amount");
        }
        
        try {
            // Get payment gateway
            PaymentGateway gateway = paymentGatewayRepository.findById(payment.getGatewayId())
                    .orElseThrow(() -> new RuntimeException("Payment gateway not found"));
            
            // Process refund through gateway
            PaymentGatewayService.RefundResult result = paymentGatewayService.processRefund(
                gateway, payment, refundAmount, reason);
            
            if (result.isSuccess()) {
                // Update payment status
                if (refundAmount.compareTo(payment.getAmount()) == 0) {
                    payment.setStatus(Payment.PaymentStatus.REFUNDED);
                } else {
                    payment.setStatus(Payment.PaymentStatus.PARTIALLY_REFUNDED);
                }
                payment.setUpdatedAt(LocalDateTime.now());
                
                // Send refund completed event
                kafkaTemplate.send("payment-events", "refund-completed", 
                    createRefundEvent(payment, refundAmount, reason));
                
            } else {
                throw new RuntimeException("Refund failed: " + result.getFailureReason());
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Refund processing error: " + e.getMessage());
        }
        
        return paymentRepository.save(payment);
    }

    public Optional<Payment> getPaymentByTransactionId(String transactionId) {
        return paymentRepository.findByTransactionId(transactionId);
    }

    public List<Payment> getPaymentsByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    public Page<Payment> getPaymentsByUserId(Long userId, Pageable pageable) {
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    public List<Payment> getFailedPayments() {
        return paymentRepository.findByStatus(Payment.PaymentStatus.FAILED);
    }

    public Map<String, Object> getPaymentStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);
        LocalDateTime last7Days = LocalDateTime.now().minusDays(7);
        LocalDateTime last30Days = LocalDateTime.now().minusDays(30);
        
        // Payment counts by status
        stats.put("completed_last_24h", paymentRepository.countByStatusAndProcessedAtAfter(
            Payment.PaymentStatus.COMPLETED, last24Hours));
        stats.put("failed_last_24h", paymentRepository.countByStatusAndProcessedAtAfter(
            Payment.PaymentStatus.FAILED, last24Hours));
        
        stats.put("completed_last_7d", paymentRepository.countByStatusAndProcessedAtAfter(
            Payment.PaymentStatus.COMPLETED, last7Days));
        stats.put("failed_last_7d", paymentRepository.countByStatusAndProcessedAtAfter(
            Payment.PaymentStatus.FAILED, last7Days));
        
        // Total amounts
        stats.put("total_amount_last_30d", paymentRepository.getTotalCompletedPaymentsAmount(last30Days));
        
        // Payment method statistics
        List<Object[]> methodStats = paymentRepository.getPaymentMethodStats(last30Days);
        Map<String, Long> methodStatsMap = new HashMap<>();
        for (Object[] stat : methodStats) {
            methodStatsMap.put(stat[0].toString(), (Long) stat[1]);
        }
        stats.put("payment_method_stats_last_30d", methodStatsMap);
        
        // Gateway statistics
        List<Object[]> gatewayStats = paymentRepository.getGatewayStats(last30Days);
        Map<String, Long> gatewayStatsMap = new HashMap<>();
        for (Object[] stat : gatewayStats) {
            gatewayStatsMap.put(stat[0].toString(), (Long) stat[1]);
        }
        stats.put("gateway_stats_last_30d", gatewayStatsMap);
        
        return stats;
    }

    private PaymentGateway findSuitableGateway(BigDecimal amount, Payment.PaymentMethod paymentMethod) {
        // For now, return the first active gateway that supports the amount
        List<PaymentGateway> gateways = paymentGatewayRepository.findActiveGatewaysForAmount(amount);
        return gateways.isEmpty() ? null : gateways.get(0);
    }

    private String generateTransactionId() {
        return "TXN-" + System.currentTimeMillis() + "-" + 
               String.valueOf((int)(Math.random() * 10000)).substring(0, 4);
    }

    private String determineCardType(String cardNumber) {
        if (cardNumber.startsWith("4")) return "VISA";
        if (cardNumber.startsWith("5")) return "MASTERCARD";
        if (cardNumber.startsWith("3")) return "AMEX";
        return "UNKNOWN";
    }

    private Map<String, Object> createPaymentEvent(Payment payment, String eventType) {
        Map<String, Object> event = new HashMap<>();
        event.put("transactionId", payment.getTransactionId());
        event.put("orderId", payment.getOrderId());
        event.put("userId", payment.getUserId());
        event.put("amount", payment.getAmount());
        event.put("paymentMethod", payment.getPaymentMethod().toString());
        event.put("status", payment.getStatus().toString());
        event.put("eventType", eventType);
        event.put("timestamp", LocalDateTime.now());
        
        if (payment.getFailureReason() != null) {
            event.put("failureReason", payment.getFailureReason());
        }
        
        return event;
    }

    private Map<String, Object> createRefundEvent(Payment payment, BigDecimal refundAmount, String reason) {
        Map<String, Object> event = new HashMap<>();
        event.put("transactionId", payment.getTransactionId());
        event.put("orderId", payment.getOrderId());
        event.put("userId", payment.getUserId());
        event.put("originalAmount", payment.getAmount());
        event.put("refundAmount", refundAmount);
        event.put("reason", reason);
        event.put("eventType", "REFUND_COMPLETED");
        event.put("timestamp", LocalDateTime.now());
        
        return event;
    }
}
