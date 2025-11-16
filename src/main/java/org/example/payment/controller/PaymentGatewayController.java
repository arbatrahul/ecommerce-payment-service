package org.example.payment.controller;

import org.example.payment.entity.PaymentGateway;
import org.example.payment.repository.PaymentGatewayRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/payment-gateways")
@CrossOrigin(origins = "*")
public class PaymentGatewayController {

    @Autowired
    private PaymentGatewayRepository paymentGatewayRepository;

    @GetMapping
    public ResponseEntity<List<PaymentGateway>> getAllGateways() {
        List<PaymentGateway> gateways = paymentGatewayRepository.findAll();
        return ResponseEntity.ok(gateways);
    }

    @GetMapping("/active")
    public ResponseEntity<List<PaymentGateway>> getActiveGateways() {
        List<PaymentGateway> activeGateways = paymentGatewayRepository.findByIsActiveTrue();
        return ResponseEntity.ok(activeGateways);
    }

    @GetMapping("/active/amount/{amount}")
    public ResponseEntity<List<PaymentGateway>> getActiveGatewaysForAmount(@PathVariable BigDecimal amount) {
        List<PaymentGateway> gateways = paymentGatewayRepository.findActiveGatewaysForAmount(amount);
        return ResponseEntity.ok(gateways);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentGateway> getGatewayById(@PathVariable Long id) {
        Optional<PaymentGateway> gateway = paymentGatewayRepository.findById(id);
        return gateway.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<PaymentGateway> getGatewayByName(@PathVariable String name) {
        Optional<PaymentGateway> gateway = paymentGatewayRepository.findByName(name);
        return gateway.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<PaymentGateway> createGateway(@RequestBody PaymentGateway gateway) {
        try {
            PaymentGateway savedGateway = paymentGatewayRepository.save(gateway);
            return ResponseEntity.ok(savedGateway);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentGateway> updateGateway(@PathVariable Long id, @RequestBody PaymentGateway gateway) {
        Optional<PaymentGateway> existingGateway = paymentGatewayRepository.findById(id);
        
        if (existingGateway.isPresent()) {
            gateway.setId(id);
            PaymentGateway updatedGateway = paymentGatewayRepository.save(gateway);
            return ResponseEntity.ok(updatedGateway);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/toggle")
    public ResponseEntity<?> toggleGatewayStatus(@PathVariable Long id) {
        Optional<PaymentGateway> gatewayOpt = paymentGatewayRepository.findById(id);
        
        if (gatewayOpt.isPresent()) {
            PaymentGateway gateway = gatewayOpt.get();
            gateway.setIsActive(!gateway.getIsActive());
            paymentGatewayRepository.save(gateway);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Gateway status updated",
                "isActive", gateway.getIsActive()
            ));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGateway(@PathVariable Long id) {
        if (paymentGatewayRepository.existsById(id)) {
            paymentGatewayRepository.deleteById(id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Gateway deleted successfully"
            ));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/test/{id}")
    public ResponseEntity<?> testGateway(@PathVariable Long id) {
        Optional<PaymentGateway> gatewayOpt = paymentGatewayRepository.findById(id);
        
        if (gatewayOpt.isPresent()) {
            PaymentGateway gateway = gatewayOpt.get();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Gateway connection test",
                "gateway", gateway.getName(),
                "configured", gateway.isConfigured(),
                "active", gateway.getIsActive(),
                "testMode", gateway.getIsTestMode()
            ));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
