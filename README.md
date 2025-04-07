# Banking Transactions API

A scalable RESTful API for managing banking transactions, built with Spring Boot.

## Features

- Create new user accounts with initial balance
- Transfer funds between accounts
- Retrieve transaction history
- Input validation and error handling
- Persistent data storage with PostgreSQL
- Redis caching for improved performance
- Optimistic and pessimistic locking for concurrent transactions
- Connection pooling and batch processing

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- PostgreSQL 12 or higher
- Redis 6 or higher

## Building the Application

```bash
mvn clean install
```

## Running the Application

1. Start PostgreSQL and create a database named 'banking'
2. Start Redis server
3. Update application.properties with your database and Redis credentials if needed
4. Run the application:
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## API Endpoints

### Create Account
```http
POST /api/accounts
Content-Type: application/json

{
    "accountHolderName": "John Doe",
    "initialBalance": 1000.00
}
```

### Get Account Details
```http
GET /api/accounts/{accountNumber}
```

### Transfer Funds
```http
POST /api/accounts/transfer
Content-Type: application/json

{
    "sourceAccountNumber": "source-account-number",
    "destinationAccountNumber": "destination-account-number",
    "amount": 100.00
}
```

### Get Transaction History
```http
GET /api/accounts/{accountNumber}/transactions
```

## Scalability Features

1. **Database Persistence**
   - PostgreSQL for reliable data storage
   - Connection pooling with HikariCP
   - Batch processing for bulk operations
   - Optimized query performance with indexes

2. **Caching Strategy**
   - Redis caching for frequently accessed data
   - Configurable cache TTL
   - Cache invalidation on updates
   - Distributed caching support

3. **Concurrency Handling**
   - Optimistic locking for account version control
   - Pessimistic locking for transfer operations
   - Transaction isolation levels
   - Thread-safe operations

4. **Performance Optimization**
   - Lazy loading of relationships
   - Pagination support for large datasets
   - Connection pool tuning
   - Server thread pool configuration

5. **Horizontal Scalability**
   - Stateless application design
   - Externalized configuration
   - Distributed caching
   - Load balancer ready

## Error Handling

The API includes comprehensive error handling for various scenarios:
- Invalid input validation
- Insufficient funds
- Account not found
- Same account transfers
- Negative amounts
- Concurrent modification exceptions
- Unexpected errors

## Implementation Details

- Three-layer architecture (Controller, Service, Repository)
- Dependency injection using Spring Framework
- DTOs for request/response handling
- Custom exception handling
- JPA for database operations
- Redis for caching
- Transaction management

## Configuration

The application can be configured through application.properties:
- Database connection settings
- Redis connection settings
- Connection pool size
- Cache TTL
- Server thread pool
- Batch processing settings

## Monitoring and Management

The application supports:
- Health checks
- Performance metrics
- Cache statistics
- Connection pool metrics

## Assumptions

1. Account numbers are automatically generated UUIDs
2. All monetary values are in the same currency
3. PostgreSQL is used for data persistence
4. Redis is used for caching
5. Transactions are ACID compliant 