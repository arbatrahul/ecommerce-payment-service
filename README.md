# Ecommerce Payment Service

Microservice for payment processing and payment gateway management in the Ecommerce Platform.

## Overview

The Payment Service handles all payment-related operations including:
- Payment processing
- Payment gateway management
- Payment status tracking
- Refund processing
- Payment statistics
- Integration with order service

## Features

- ✅ **Payment Processing**: Process payments through multiple gateways
- ✅ **Payment Gateway Management**: Manage multiple payment gateways
- ✅ **Payment Status Tracking**: Track payment status and history
- ✅ **Refund Processing**: Handle payment refunds
- ✅ **Kafka Integration**: Event-driven payment processing
- ✅ **Service Discovery**: Eureka client integration
- ✅ **MySQL Database**: Persistent payment storage
- ✅ **RESTful API**: Comprehensive REST endpoints
- ✅ **Payment Statistics**: Analytics and reporting

## Quick Start

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher
- MySQL 8.0+ (Port 3309)
- Kafka (Port 9092)
- Eureka Server (Port 8761)

### Database Setup

1. **Create MySQL Database**:
   ```sql
   CREATE DATABASE payment_service_db;
   ```

### Running Locally

1. **Build the project**:
   ```bash
   mvn clean package
   ```

2. **Run the application**:
   ```bash
   java -jar target/payment-service-1.0.0.jar
   ```

3. **Or use Maven**:
   ```bash
   mvn spring-boot:run
   ```

The service will start on `http://localhost:8085`

## API Endpoints

### Payment Endpoints

#### Get Payment by Transaction ID
```http
GET /api/payments/transaction/{transactionId}
```

#### Get Payments by Order ID
```http
GET /api/payments/order/{orderId}
```

#### Get Payments by User ID
```http
GET /api/payments/user/{userId}?page=0&size=10
```

#### Get Failed Payments
```http
GET /api/payments/failed
```

#### Process Refund
```http
POST /api/payments/refund/{transactionId}
Content-Type: application/json

{
  "refundAmount": 99.99,
  "reason": "Customer request"
}
```

#### Get Payment Statistics
```http
GET /api/payments/statistics
```

#### Create Manual Payment
```http
POST /api/payments/manual
Content-Type: application/json

{
  "orderId": 1,
  "userId": 1,
  "amount": 199.99,
  "paymentMethod": "CREDIT_CARD",
  "cardDetails": {
    "cardNumber": "4111111111111111",
    "expiryMonth": 12,
    "expiryYear": 2025,
    "cvv": "123"
  }
}
```

#### Process Payment
```http
POST /api/payments/process/{transactionId}
Content-Type: application/json

{
  "cardNumber": "4111111111111111",
  "expiryMonth": 12,
  "expiryYear": 2025,
  "cvv": "123"
}
```

### Payment Gateway Endpoints

#### Get All Gateways
```http
GET /api/payment-gateways
```

#### Get Active Gateways
```http
GET /api/payment-gateways/active
```

#### Get Active Gateways for Amount
```http
GET /api/payment-gateways/active/amount/{amount}
```

#### Get Gateway by ID
```http
GET /api/payment-gateways/{id}
```

#### Get Gateway by Name
```http
GET /api/payment-gateways/name/{name}
```

#### Create Gateway (Admin)
```http
POST /api/payment-gateways
Content-Type: application/json

{
  "name": "Stripe",
  "description": "Stripe payment gateway",
  "isActive": true,
  "minAmount": 0.01,
  "maxAmount": 10000.00,
  "feePercentage": 2.9,
  "feeFixed": 0.30
}
```

#### Update Gateway (Admin)
```http
PUT /api/payment-gateways/{id}
Content-Type: application/json

{
  "name": "Updated Stripe",
  "isActive": true
}
```

#### Toggle Gateway Status (Admin)
```http
PUT /api/payment-gateways/{id}/toggle
```

#### Delete Gateway (Admin)
```http
DELETE /api/payment-gateways/{id}
```

#### Test Gateway Connection (Admin)
```http
GET /api/payment-gateways/test/{id}
```

## Configuration

### Application Configuration (application.yml)

```yaml
server:
  port: 8085

spring:
  application:
    name: payment-service
  datasource:
    url: jdbc:mysql://localhost:3309/payment_service_db
    username: root
    password: password
  kafka:
    bootstrap-servers: localhost:9092
```

### Payment Methods

- `CREDIT_CARD` - Credit card payment
- `DEBIT_CARD` - Debit card payment
- `PAYPAL` - PayPal payment
- `BANK_TRANSFER` - Bank transfer
- `WALLET` - Digital wallet payment

### Payment Statuses

- `PENDING` - Payment pending
- `PROCESSING` - Payment being processed
- `COMPLETED` - Payment completed
- `FAILED` - Payment failed
- `REFUNDED` - Payment refunded
- `CANCELLED` - Payment cancelled

## Usage Examples

### Get Payment by Transaction ID

```bash
curl http://localhost:8085/api/payments/transaction/TXN-20240101-001
```

### Get Payments by Order ID

```bash
curl http://localhost:8085/api/payments/order/1
```

### Process Refund

```bash
curl -X POST http://localhost:8085/api/payments/refund/TXN-20240101-001 \
  -H "Content-Type: application/json" \
  -d '{
    "refundAmount": 99.99,
    "reason": "Customer request"
  }'
```

### Create Manual Payment

```bash
curl -X POST http://localhost:8085/api/payments/manual \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": 1,
    "userId": 1,
    "amount": 199.99,
    "paymentMethod": "CREDIT_CARD",
    "cardDetails": {
      "cardNumber": "4111111111111111",
      "expiryMonth": 12,
      "expiryYear": 2025,
      "cvv": "123"
    }
  }'
```

### Get Active Payment Gateways

```bash
curl http://localhost:8085/api/payment-gateways/active
```

## Architecture

### Payment Processing Flow

1. **Payment Request** → Receive payment request from Kafka or API
2. **Gateway Selection** → Select appropriate payment gateway
3. **Payment Processing** → Process payment through gateway
4. **Status Update** → Update payment status
5. **Event Publishing** → Publish payment event to Kafka
6. **Order Update** → Notify Order Service of payment status

### Refund Processing Flow

1. **Refund Request** → Receive refund request
2. **Payment Validation** → Validate original payment
3. **Refund Processing** → Process refund through gateway
4. **Status Update** → Update payment status to REFUNDED
5. **Event Publishing** → Publish refund event

### Components

1. **PaymentController**: REST endpoints for payments
2. **PaymentGatewayController**: REST endpoints for gateways
3. **PaymentService**: Payment processing logic
4. **PaymentGatewayService**: Gateway management
5. **PaymentRepository**: Database operations
6. **PaymentRequestConsumer**: Kafka consumer for payment requests
7. **DataInitializationService**: Initialize default gateways

## Kafka Integration

### Topics Consumed

- `payment-requests`: Payment processing requests from Order Service

### Topics Produced

- `payment-events`: Payment status updates

### Event Types

- `PAYMENT_REQUESTED` → Payment request received
- `PAYMENT_PROCESSING` → Payment being processed
- `PAYMENT_COMPLETED` → Payment successful
- `PAYMENT_FAILED` → Payment failed
- `PAYMENT_REFUNDED` → Payment refunded

## Database Schema

### Payment Entity

- `id` - Primary key
- `transactionId` - Unique transaction ID
- `orderId` - Order ID
- `userId` - User ID
- `amount` - Payment amount
- `paymentMethod` - Payment method
- `status` - Payment status
- `gatewayId` - Payment gateway ID
- `gatewayTransactionId` - Gateway transaction ID
- `cardDetails` - Card details (encrypted)
- `createdAt` - Creation timestamp
- `updatedAt` - Update timestamp

### PaymentGateway Entity

- `id` - Primary key
- `name` - Gateway name
- `description` - Gateway description
- `isActive` - Active status
- `minAmount` - Minimum payment amount
- `maxAmount` - Maximum payment amount
- `feePercentage` - Fee percentage
- `feeFixed` - Fixed fee
- `isTestMode` - Test mode flag
- `configuration` - Gateway configuration JSON

## Testing

### Run Tests

```bash
mvn test
```

### Manual Testing

1. Start MySQL, Kafka, Eureka
2. Start Payment Service
3. Create payment gateway
4. Process test payment
5. Verify payment in database
6. Test refund functionality

## Deployment

### Docker

```bash
# Build image
docker build -t ecommerce/payment-service .

# Run container
docker run -p 8085:8085 \
  -e SPRING_PROFILES_ACTIVE=docker \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql-payment:3306/payment_service_db \
  ecommerce/payment-service
```

### Production Considerations

1. **Security**:
   - Encrypt card details
   - Use PCI-DSS compliant storage
   - Secure API endpoints
   - Implement rate limiting
2. **Payment Gateways**:
   - Use production gateway credentials
   - Configure proper error handling
   - Set up webhook endpoints
   - Monitor gateway health
3. **Database**:
   - Encrypt sensitive data
   - Set up proper backups
   - Use connection pooling
   - Archive old payments
4. **Compliance**:
   - Follow PCI-DSS requirements
   - Implement audit logging
   - Secure transaction data
   - Regular security audits

## Troubleshooting

### Common Issues

1. **Payment Gateway Connection Failed**:
   - Verify gateway credentials
   - Check network connectivity
   - Verify gateway configuration

2. **Payment Processing Fails**:
   - Check payment gateway logs
   - Verify card details
   - Check payment limits
   - Review error messages

3. **Kafka Consumer Not Receiving Events**:
   - Verify Kafka connection
   - Check consumer group configuration
   - Verify topic exists

4. **Refund Processing Fails**:
   - Verify original payment exists
   - Check refund amount validity
   - Verify gateway supports refunds

### Logs

```bash
# Enable debug logging
java -jar target/payment-service-1.0.0.jar \
  --logging.level.org.example.payment=DEBUG \
  --logging.level.org.apache.kafka=DEBUG
```

## Dependencies

- Spring Boot 3.2.0
- Spring Data JPA (MySQL)
- Spring Kafka
- Spring Cloud Netflix Eureka Client
- MySQL Connector
- Validation API

## Project Structure

```
src/
├── main/
│   ├── java/org/example/payment/
│   │   ├── PaymentServiceApplication.java
│   │   ├── controller/
│   │   │   ├── PaymentController.java
│   │   │   └── PaymentGatewayController.java
│   │   ├── service/
│   │   │   ├── PaymentService.java
│   │   │   ├── PaymentGatewayService.java
│   │   │   └── DataInitializationService.java
│   │   ├── entity/
│   │   │   ├── Payment.java
│   │   │   └── PaymentGateway.java
│   │   ├── repository/
│   │   │   ├── PaymentRepository.java
│   │   │   └── PaymentGatewayRepository.java
│   │   └── consumer/
│   │       └── PaymentRequestConsumer.java
│   └── resources/
│       └── application.yml
└── test/
```

## Contributing

1. Follow Spring Boot best practices
2. Write comprehensive tests
3. Ensure PCI-DSS compliance
4. Encrypt sensitive data
5. Handle errors gracefully

## Security Notes

⚠️ **Important Security Considerations**:

1. **Card Data**: Never store full card numbers. Use tokenization.
2. **Encryption**: Encrypt all sensitive payment data.
3. **PCI-DSS**: Ensure compliance with PCI-DSS standards.
4. **API Security**: Use HTTPS and authentication.
5. **Audit Logging**: Log all payment operations.

## License

This project is part of the Ecommerce Microservices Platform.
