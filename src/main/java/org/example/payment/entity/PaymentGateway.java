package org.example.payment.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_gateways")
public class PaymentGateway {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false, unique = true)
    private String name;
    
    @Column(name = "display_name", nullable = false)
    private String displayName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private GatewayType type;
    
    @Column(name = "api_endpoint")
    private String apiEndpoint;
    
    @Column(name = "api_key")
    private String apiKey;
    
    @Column(name = "secret_key")
    private String secretKey;
    
    @Column(name = "merchant_id")
    private String merchantId;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "is_test_mode", nullable = false)
    private Boolean isTestMode = true;
    
    @Column(name = "supported_currencies", columnDefinition = "TEXT")
    private String supportedCurrencies; // JSON array of supported currencies
    
    @Column(name = "transaction_fee_percentage", precision = 5, scale = 4)
    private java.math.BigDecimal transactionFeePercentage;
    
    @Column(name = "fixed_transaction_fee", precision = 10, scale = 2)
    private java.math.BigDecimal fixedTransactionFee;
    
    @Column(name = "min_amount", precision = 10, scale = 2)
    private java.math.BigDecimal minAmount;
    
    @Column(name = "max_amount", precision = 10, scale = 2)
    private java.math.BigDecimal maxAmount;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Enums
    public enum GatewayType {
        STRIPE, PAYPAL, SQUARE, BRAINTREE, AUTHORIZE_NET, RAZORPAY, MOCK
    }
    
    // Constructors
    public PaymentGateway() {
        this.createdAt = LocalDateTime.now();
    }
    
    public PaymentGateway(String name, String displayName, GatewayType type) {
        this();
        this.name = name;
        this.displayName = displayName;
        this.type = type;
    }
    
    // Business methods
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    public boolean isConfigured() {
        return apiKey != null && !apiKey.trim().isEmpty();
    }
    
    public boolean supportsAmount(java.math.BigDecimal amount) {
        if (minAmount != null && amount.compareTo(minAmount) < 0) {
            return false;
        }
        if (maxAmount != null && amount.compareTo(maxAmount) > 0) {
            return false;
        }
        return true;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    public GatewayType getType() {
        return type;
    }
    
    public void setType(GatewayType type) {
        this.type = type;
    }
    
    public String getApiEndpoint() {
        return apiEndpoint;
    }
    
    public void setApiEndpoint(String apiEndpoint) {
        this.apiEndpoint = apiEndpoint;
    }
    
    public String getApiKey() {
        return apiKey;
    }
    
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
    
    public String getSecretKey() {
        return secretKey;
    }
    
    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }
    
    public String getMerchantId() {
        return merchantId;
    }
    
    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public Boolean getIsTestMode() {
        return isTestMode;
    }
    
    public void setIsTestMode(Boolean isTestMode) {
        this.isTestMode = isTestMode;
    }
    
    public String getSupportedCurrencies() {
        return supportedCurrencies;
    }
    
    public void setSupportedCurrencies(String supportedCurrencies) {
        this.supportedCurrencies = supportedCurrencies;
    }
    
    public java.math.BigDecimal getTransactionFeePercentage() {
        return transactionFeePercentage;
    }
    
    public void setTransactionFeePercentage(java.math.BigDecimal transactionFeePercentage) {
        this.transactionFeePercentage = transactionFeePercentage;
    }
    
    public java.math.BigDecimal getFixedTransactionFee() {
        return fixedTransactionFee;
    }
    
    public void setFixedTransactionFee(java.math.BigDecimal fixedTransactionFee) {
        this.fixedTransactionFee = fixedTransactionFee;
    }
    
    public java.math.BigDecimal getMinAmount() {
        return minAmount;
    }
    
    public void setMinAmount(java.math.BigDecimal minAmount) {
        this.minAmount = minAmount;
    }
    
    public java.math.BigDecimal getMaxAmount() {
        return maxAmount;
    }
    
    public void setMaxAmount(java.math.BigDecimal maxAmount) {
        this.maxAmount = maxAmount;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
