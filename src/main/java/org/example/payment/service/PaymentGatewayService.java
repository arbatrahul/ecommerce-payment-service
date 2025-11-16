package org.example.payment.service;

import org.example.payment.entity.Payment;
import org.example.payment.entity.PaymentGateway;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Random;

@Service
public class PaymentGatewayService {

    private final Random random = new Random();

    public PaymentResult processPayment(PaymentGateway gateway, Payment payment, Map<String, Object> cardDetails) {
        
        System.out.println("Processing payment through gateway: " + gateway.getDisplayName() + 
                          " for amount: $" + payment.getAmount());
        
        try {
            // Simulate payment processing based on gateway type
            switch (gateway.getType()) {
                case STRIPE:
                    return processStripePayment(payment, cardDetails);
                case PAYPAL:
                    return processPayPalPayment(payment, cardDetails);
                case SQUARE:
                    return processSquarePayment(payment, cardDetails);
                case MOCK:
                default:
                    return processMockPayment(payment, cardDetails);
            }
            
        } catch (Exception e) {
            return new PaymentResult(false, null, 
                "Gateway processing error: " + e.getMessage(), 
                "Exception occurred during payment processing");
        }
    }

    public RefundResult processRefund(PaymentGateway gateway, Payment payment, 
                                    BigDecimal refundAmount, String reason) {
        
        System.out.println("Processing refund through gateway: " + gateway.getDisplayName() + 
                          " for amount: $" + refundAmount);
        
        try {
            // Simulate refund processing based on gateway type
            switch (gateway.getType()) {
                case STRIPE:
                    return processStripeRefund(payment, refundAmount, reason);
                case PAYPAL:
                    return processPayPalRefund(payment, refundAmount, reason);
                case SQUARE:
                    return processSquareRefund(payment, refundAmount, reason);
                case MOCK:
                default:
                    return processMockRefund(payment, refundAmount, reason);
            }
            
        } catch (Exception e) {
            return new RefundResult(false, null, 
                "Gateway refund error: " + e.getMessage(), 
                "Exception occurred during refund processing");
        }
    }

    private PaymentResult processStripePayment(Payment payment, Map<String, Object> cardDetails) {
        // Simulate Stripe API call
        simulateProcessingDelay(1000, 3000);
        
        // Simulate 95% success rate for Stripe
        boolean success = random.nextDouble() < 0.95;
        
        if (success) {
            String gatewayTxnId = "stripe_" + System.currentTimeMillis();
            String response = "{'id': '" + gatewayTxnId + "', 'status': 'succeeded', 'amount': " + 
                            payment.getAmount().multiply(new BigDecimal("100")).intValue() + "}";
            
            return new PaymentResult(true, gatewayTxnId, null, response);
        } else {
            String response = "{'error': {'type': 'card_error', 'code': 'card_declined', 'message': 'Your card was declined.'}}";
            return new PaymentResult(false, null, "Card declined by Stripe", response);
        }
    }

    private PaymentResult processPayPalPayment(Payment payment, Map<String, Object> cardDetails) {
        // Simulate PayPal API call
        simulateProcessingDelay(2000, 4000);
        
        // Simulate 92% success rate for PayPal
        boolean success = random.nextDouble() < 0.92;
        
        if (success) {
            String gatewayTxnId = "paypal_" + System.currentTimeMillis();
            String response = "{'id': '" + gatewayTxnId + "', 'state': 'approved', 'amount': {'total': '" + 
                            payment.getAmount() + "', 'currency': 'USD'}}";
            
            return new PaymentResult(true, gatewayTxnId, null, response);
        } else {
            String response = "{'name': 'PAYMENT_DENIED', 'message': 'The request was denied by PayPal.'}";
            return new PaymentResult(false, null, "Payment denied by PayPal", response);
        }
    }

    private PaymentResult processSquarePayment(Payment payment, Map<String, Object> cardDetails) {
        // Simulate Square API call
        simulateProcessingDelay(1500, 3500);
        
        // Simulate 90% success rate for Square
        boolean success = random.nextDouble() < 0.90;
        
        if (success) {
            String gatewayTxnId = "square_" + System.currentTimeMillis();
            String response = "{'payment': {'id': '" + gatewayTxnId + "', 'status': 'COMPLETED', 'amount_money': {'amount': " + 
                            payment.getAmount().multiply(new BigDecimal("100")).intValue() + ", 'currency': 'USD'}}}";
            
            return new PaymentResult(true, gatewayTxnId, null, response);
        } else {
            String response = "{'errors': [{'category': 'PAYMENT_METHOD_ERROR', 'code': 'CARD_DECLINED', 'detail': 'Card declined.'}]}";
            return new PaymentResult(false, null, "Card declined by Square", response);
        }
    }

    private PaymentResult processMockPayment(Payment payment, Map<String, Object> cardDetails) {
        // Simulate processing delay
        simulateProcessingDelay(500, 2000);
        
        // Simulate 88% success rate for mock gateway
        boolean success = random.nextDouble() < 0.88;
        
        if (success) {
            String gatewayTxnId = "mock_" + System.currentTimeMillis();
            String response = "{'transaction_id': '" + gatewayTxnId + "', 'status': 'success', 'amount': " + 
                            payment.getAmount() + ", 'currency': 'USD'}";
            
            return new PaymentResult(true, gatewayTxnId, null, response);
        } else {
            String[] failureReasons = {
                "Insufficient funds",
                "Card expired",
                "Invalid CVV",
                "Card blocked",
                "Network timeout"
            };
            String reason = failureReasons[random.nextInt(failureReasons.length)];
            String response = "{'status': 'failed', 'error': '" + reason + "'}";
            
            return new PaymentResult(false, null, reason, response);
        }
    }

    private RefundResult processStripeRefund(Payment payment, BigDecimal refundAmount, String reason) {
        simulateProcessingDelay(1000, 2000);
        
        // Simulate 98% success rate for refunds
        boolean success = random.nextDouble() < 0.98;
        
        if (success) {
            String refundId = "stripe_refund_" + System.currentTimeMillis();
            String response = "{'id': '" + refundId + "', 'status': 'succeeded', 'amount': " + 
                            refundAmount.multiply(new BigDecimal("100")).intValue() + "}";
            
            return new RefundResult(true, refundId, null, response);
        } else {
            String response = "{'error': {'type': 'invalid_request_error', 'message': 'Refund failed.'}}";
            return new RefundResult(false, null, "Refund failed", response);
        }
    }

    private RefundResult processPayPalRefund(Payment payment, BigDecimal refundAmount, String reason) {
        simulateProcessingDelay(1500, 3000);
        
        boolean success = random.nextDouble() < 0.96;
        
        if (success) {
            String refundId = "paypal_refund_" + System.currentTimeMillis();
            String response = "{'id': '" + refundId + "', 'state': 'completed', 'amount': {'total': '" + 
                            refundAmount + "', 'currency': 'USD'}}";
            
            return new RefundResult(true, refundId, null, response);
        } else {
            String response = "{'name': 'REFUND_NOT_ALLOWED', 'message': 'Refund not allowed for this transaction.'}";
            return new RefundResult(false, null, "Refund not allowed", response);
        }
    }

    private RefundResult processSquareRefund(Payment payment, BigDecimal refundAmount, String reason) {
        simulateProcessingDelay(1000, 2500);
        
        boolean success = random.nextDouble() < 0.94;
        
        if (success) {
            String refundId = "square_refund_" + System.currentTimeMillis();
            String response = "{'refund': {'id': '" + refundId + "', 'status': 'COMPLETED', 'amount_money': {'amount': " + 
                            refundAmount.multiply(new BigDecimal("100")).intValue() + ", 'currency': 'USD'}}}";
            
            return new RefundResult(true, refundId, null, response);
        } else {
            String response = "{'errors': [{'category': 'REFUND_ERROR', 'code': 'REFUND_DECLINED', 'detail': 'Refund declined.'}]}";
            return new RefundResult(false, null, "Refund declined", response);
        }
    }

    private RefundResult processMockRefund(Payment payment, BigDecimal refundAmount, String reason) {
        simulateProcessingDelay(500, 1500);
        
        boolean success = random.nextDouble() < 0.92;
        
        if (success) {
            String refundId = "mock_refund_" + System.currentTimeMillis();
            String response = "{'refund_id': '" + refundId + "', 'status': 'success', 'amount': " + 
                            refundAmount + ", 'currency': 'USD'}";
            
            return new RefundResult(true, refundId, null, response);
        } else {
            String response = "{'status': 'failed', 'error': 'Refund processing failed'}";
            return new RefundResult(false, null, "Refund processing failed", response);
        }
    }

    private void simulateProcessingDelay(int minMs, int maxMs) {
        try {
            int delay = minMs + random.nextInt(maxMs - minMs);
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // Result classes
    public static class PaymentResult {
        private final boolean success;
        private final String gatewayTransactionId;
        private final String failureReason;
        private final String gatewayResponse;

        public PaymentResult(boolean success, String gatewayTransactionId, String failureReason, String gatewayResponse) {
            this.success = success;
            this.gatewayTransactionId = gatewayTransactionId;
            this.failureReason = failureReason;
            this.gatewayResponse = gatewayResponse;
        }

        public boolean isSuccess() { return success; }
        public String getGatewayTransactionId() { return gatewayTransactionId; }
        public String getFailureReason() { return failureReason; }
        public String getGatewayResponse() { return gatewayResponse; }
    }

    public static class RefundResult {
        private final boolean success;
        private final String refundId;
        private final String failureReason;
        private final String gatewayResponse;

        public RefundResult(boolean success, String refundId, String failureReason, String gatewayResponse) {
            this.success = success;
            this.refundId = refundId;
            this.failureReason = failureReason;
            this.gatewayResponse = gatewayResponse;
        }

        public boolean isSuccess() { return success; }
        public String getRefundId() { return refundId; }
        public String getFailureReason() { return failureReason; }
        public String getGatewayResponse() { return gatewayResponse; }
    }
}
