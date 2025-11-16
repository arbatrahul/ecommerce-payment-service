package org.example.payment.controller;

import org.example.payment.entity.Payment;
import org.example.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<Payment> getPaymentByTransactionId(@PathVariable String transactionId) {
        Optional<Payment> payment = paymentService.getPaymentByTransactionId(transactionId);
        return payment.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<Payment>> getPaymentsByOrderId(@PathVariable Long orderId) {
        List<Payment> payments = paymentService.getPaymentsByOrderId(orderId);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<Payment>> getPaymentsByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Payment> payments = paymentService.getPaymentsByUserId(userId, pageable);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/failed")
    public ResponseEntity<List<Payment>> getFailedPayments() {
        List<Payment> failedPayments = paymentService.getFailedPayments();
        return ResponseEntity.ok(failedPayments);
    }

    @PostMapping("/refund/{transactionId}")
    public ResponseEntity<?> refundPayment(
            @PathVariable String transactionId,
            @RequestBody RefundRequest refundRequest) {
        
        try {
            Payment refundedPayment = paymentService.refundPayment(
                transactionId, 
                refundRequest.getRefundAmount(), 
                refundRequest.getReason()
            );
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Refund processed successfully",
                "payment", refundedPayment
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Refund failed: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getPaymentStatistics() {
        Map<String, Object> statistics = paymentService.getPaymentStatistics();
        return ResponseEntity.ok(statistics);
    }

    @PostMapping("/manual")
    public ResponseEntity<?> createManualPayment(@RequestBody ManualPaymentRequest request) {
        try {
            Payment payment = paymentService.createPayment(
                request.getOrderId(),
                request.getUserId(),
                request.getAmount(),
                request.getPaymentMethod(),
                request.getCardDetails()
            );
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Payment created successfully",
                "transactionId", payment.getTransactionId(),
                "payment", payment
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Payment creation failed: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/process/{transactionId}")
    public ResponseEntity<?> processPayment(
            @PathVariable String transactionId,
            @RequestBody Map<String, Object> cardDetails) {
        
        try {
            Payment processedPayment = paymentService.processPayment(transactionId, cardDetails);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Payment processed successfully",
                "payment", processedPayment
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Payment processing failed: " + e.getMessage()
            ));
        }
    }

    // DTOs
    public static class RefundRequest {
        private BigDecimal refundAmount;
        private String reason;

        public BigDecimal getRefundAmount() {
            return refundAmount;
        }

        public void setRefundAmount(BigDecimal refundAmount) {
            this.refundAmount = refundAmount;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }

    public static class ManualPaymentRequest {
        private Long orderId;
        private Long userId;
        private BigDecimal amount;
        private Payment.PaymentMethod paymentMethod;
        private Map<String, Object> cardDetails;

        public Long getOrderId() {
            return orderId;
        }

        public void setOrderId(Long orderId) {
            this.orderId = orderId;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public Payment.PaymentMethod getPaymentMethod() {
            return paymentMethod;
        }

        public void setPaymentMethod(Payment.PaymentMethod paymentMethod) {
            this.paymentMethod = paymentMethod;
        }

        public Map<String, Object> getCardDetails() {
            return cardDetails;
        }

        public void setCardDetails(Map<String, Object> cardDetails) {
            this.cardDetails = cardDetails;
        }
    }
}
