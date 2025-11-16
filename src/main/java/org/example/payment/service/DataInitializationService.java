package org.example.payment.service;

import org.example.payment.entity.PaymentGateway;
import org.example.payment.repository.PaymentGatewayRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class DataInitializationService implements CommandLineRunner {

    @Autowired
    private PaymentGatewayRepository paymentGatewayRepository;

    @Override
    public void run(String... args) throws Exception {
        initializePaymentGateways();
    }

    private void initializePaymentGateways() {
        if (paymentGatewayRepository.count() == 0) {
            System.out.println("Initializing payment gateways...");

            // Stripe Gateway
            PaymentGateway stripe = new PaymentGateway("stripe", "Stripe", PaymentGateway.GatewayType.STRIPE);
            stripe.setApiEndpoint("https://api.stripe.com/v1");
            stripe.setApiKey("sk_test_stripe_key_here");
            stripe.setSecretKey("stripe_webhook_secret_here");
            stripe.setSupportedCurrencies("[\"USD\", \"EUR\", \"GBP\", \"CAD\"]");
            stripe.setTransactionFeePercentage(new BigDecimal("2.9"));
            stripe.setFixedTransactionFee(new BigDecimal("0.30"));
            stripe.setMinAmount(new BigDecimal("0.50"));
            stripe.setMaxAmount(new BigDecimal("999999.99"));
            stripe.setIsActive(true);
            stripe.setIsTestMode(true);
            paymentGatewayRepository.save(stripe);

            // PayPal Gateway
            PaymentGateway paypal = new PaymentGateway("paypal", "PayPal", PaymentGateway.GatewayType.PAYPAL);
            paypal.setApiEndpoint("https://api.sandbox.paypal.com");
            paypal.setApiKey("paypal_client_id_here");
            paypal.setSecretKey("paypal_client_secret_here");
            paypal.setSupportedCurrencies("[\"USD\", \"EUR\", \"GBP\", \"CAD\", \"AUD\"]");
            paypal.setTransactionFeePercentage(new BigDecimal("3.49"));
            paypal.setFixedTransactionFee(new BigDecimal("0.49"));
            paypal.setMinAmount(new BigDecimal("1.00"));
            paypal.setMaxAmount(new BigDecimal("10000.00"));
            paypal.setIsActive(true);
            paypal.setIsTestMode(true);
            paymentGatewayRepository.save(paypal);

            // Square Gateway
            PaymentGateway square = new PaymentGateway("square", "Square", PaymentGateway.GatewayType.SQUARE);
            square.setApiEndpoint("https://connect.squareupsandbox.com");
            square.setApiKey("square_access_token_here");
            square.setSecretKey("square_webhook_signature_key_here");
            square.setSupportedCurrencies("[\"USD\", \"CAD\", \"GBP\", \"AUD\", \"JPY\"]");
            square.setTransactionFeePercentage(new BigDecimal("2.6"));
            square.setFixedTransactionFee(new BigDecimal("0.10"));
            square.setMinAmount(new BigDecimal("1.00"));
            square.setMaxAmount(new BigDecimal("50000.00"));
            square.setIsActive(true);
            square.setIsTestMode(true);
            paymentGatewayRepository.save(square);

            // Mock Gateway (for testing)
            PaymentGateway mock = new PaymentGateway("mock", "Mock Gateway", PaymentGateway.GatewayType.MOCK);
            mock.setApiEndpoint("http://localhost:8085/mock");
            mock.setApiKey("mock_api_key");
            mock.setSecretKey("mock_secret_key");
            mock.setSupportedCurrencies("[\"USD\", \"EUR\", \"GBP\"]");
            mock.setTransactionFeePercentage(new BigDecimal("0.0"));
            mock.setFixedTransactionFee(new BigDecimal("0.0"));
            mock.setMinAmount(new BigDecimal("0.01"));
            mock.setMaxAmount(new BigDecimal("1000000.00"));
            mock.setIsActive(true);
            mock.setIsTestMode(true);
            paymentGatewayRepository.save(mock);

            System.out.println("Payment gateways initialized successfully!");
            System.out.println("Available gateways: Stripe, PayPal, Square, Mock");
        }
    }
}
