package org.example.payment.consumer;

import org.example.payment.entity.Payment;
import org.example.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class PaymentRequestConsumer {

    @Autowired
    private PaymentService paymentService;

    @KafkaListener(topics = "order-events", groupId = "payment-service-group")
    public void handleOrderEvent(@Payload Map<String, Object> orderEvent, 
                               @Header(KafkaHeaders.KEY) String eventType) {
        
        if ("order-created".equals(eventType)) {
            processPaymentRequest(orderEvent);
        }
    }

    private void processPaymentRequest(Map<String, Object> orderEvent) {
        try {
            Long orderId = Long.valueOf(orderEvent.get("orderId").toString());
            Long userId = Long.valueOf(orderEvent.get("userId").toString());
            BigDecimal amount = new BigDecimal(orderEvent.get("totalAmount").toString());
            String paymentMethodStr = orderEvent.get("paymentMethod").toString();
            
            // Parse payment method
            Payment.PaymentMethod paymentMethod = Payment.PaymentMethod.valueOf(paymentMethodStr);
            
            // Extract card details if present
            @SuppressWarnings("unchecked")
            Map<String, Object> cardDetails = (Map<String, Object>) orderEvent.get("cardDetails");
            
            System.out.println("Creating payment for order: " + orderId + 
                             ", amount: $" + amount + 
                             ", method: " + paymentMethod);
            
            // Create payment record
            Payment payment = paymentService.createPayment(orderId, userId, amount, paymentMethod, cardDetails);
            
            // Process the payment
            paymentService.processPayment(payment.getTransactionId(), cardDetails);
            
            System.out.println("Payment processing initiated for transaction: " + payment.getTransactionId());
            
        } catch (Exception e) {
            System.err.println("Error processing payment request: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
