# IDR Rate Aggregator

 Spring Boot application that provides comprehensive REST API for accessing financial exchange rate data with a focus on Indonesian Rupiah (IDR). The application aggregates data from the Frankfurter Exchange Rate API and implements advanced architectural patterns for clean, maintainable, and thread-safe financial data processing.

## Architecture & Features

### 1. External API Integration (Frankfurter API)

* **Base URL (Public):** `https://api.frankfurter.app/`.

* Integrates with three distinct Frankfurter API resources:

   1.  `/latest?base=IDR` - Latest exchange rates relative to IDR
   2.  Historical data queries for custom date ranges and currency pairs
   3.  `/currencies` - Complete list of supported currency symbols

### 2. Key Features

* **Multi-endpoint REST API** supporting various exchange rate queries
* **Personalized spread calculation** for USD buy rates based on unique factors
* **In-memory caching** for fast response times on frequently accessed data
* **Thread-safe concurrent access** using ReadWriteLock pattern
* **Production-ready error handling** with comprehensive HTTP status codes
* **Strategy Pattern implementation** for extensible data fetching strategies
* **FactoryBean pattern** for external API client configuration
* **Application startup data initialization** with ApplicationRunner

### 3. Personalized Spread Calculation

The application calculates a unique **USD_BuySpread_IDR** value using a personalized spread factor:

* **GitHub Username:** `dihardmg`
* **Character codes sum:** 832
* **Spread Factor:** `(832 % 1000) / 100000.0 = 0.00832`
* **Formula:** `USD_BuySpread_IDR = (1 / Rate_USD) * (1 + 0.00832)`

## Technical Implementation

The application implements several advanced architectural patterns:

### Design Patterns Used
- **Strategy Pattern:** For extensible data fetching strategies
- **FactoryBean Pattern:** For external API client configuration
- **ApplicationRunner:** For startup data initialization
- **ReadWriteLock:** For thread-safe concurrent data access

### Key Components
- **FinanceController:** REST API endpoints
- **DataFetchService:** Business logic and strategy coordination
- **InMemoryDataStore:** Thread-safe data caching
- **FrankfurterClientFactoryBean:** WebClient configuration
- **DataInitializationRunner:** Startup data loading

## Setup & Run Instructions

### Prerequisites
- Java 25 or higher
- Maven 3.6 or higher

### Running the Application

1. **Clone the repository:**
```bash
$ git clone https://github.com/dihardmg/allo-backend-test

$ cd allo-backend-test
```

2. **Build the project:**
```bash
mvn clean install
```

3. **Run the application:**
```bash
mvn clean spring-boot:run
```

The application will start on `http://localhost:8080`

### Running Tests

1. **Run all tests:**
```bash
mvn test
```

2. **Run specific test categories:**

**Controller Tests (API Endpoint Testing):**
```bash
mvn test -Dtest=FinanceControllerTest
```

**Service Layer Tests:**
```bash
mvn test -Dtest="*ServiceTest"
mvn test -Dtest=DataStoreServiceTest
mvn test -Dtest=CurrencyMetadataServiceTest
```

**Strategy Tests:**
```bash
mvn test -Dtest=SpreadFactorCalculatorTest
mvn test -Dtest=LatestRatesStrategyTest
mvn test -Dtest=SupportedCurrenciesStrategyTest
```

**Integration Tests:**
```bash
mvn test -Dtest=DataInitializationRunnerTest
mvn test -Dtest=ApplicationIntegrationTest
```

## ✅ Core Endpoints Test with CURL

### 1. Health Check Endpoint
Check if the API service is running and ready to accept requests.

```bash
curl -X GET http://localhost:8080/api/finance/data/health
```

**Expected Response:**
```json
{
  "status": "UP",
  "initialized": true
}
```

✅ Service ready state  
✅ Service initializing state

---

### 2. Latest IDR Rates with Spread Calculation
Get current exchange rates with IDR as base currency, including unique USD spread calculation.

```bash
curl -X GET http://localhost:8080/api/finance/data/latest_idr_rates
```

**Expected Response:**
```json
{
  "base": "IDR",
  "date": "2025-11-18",
  "rates": {
    "USD": 0.000064,
    "EUR": 0.000059,
    "SGD": 0.000085,
    "JPY": 0.009726,
    "GBP": 0.000081
  },
  "USD_BuySpread_IDR": 15800.0
}
```

✅ Success response with USD spread  
✅ Service unavailable handling  
✅ Resource not found scenarios  
✅ Internal server error handling

---

### 3. Supported Currencies (Multiple Formats)

#### Enhanced Format (Default)
Get complete currency information with metadata like country, symbol, and decimal places.

```bash
curl -X GET http://localhost:8080/api/finance/data/supported_currencies
```

**Expected Response:**
```json
{
  "currencies": [
    {
      "code": "IDR",
      "name": "Indonesian Rupiah",
      "symbol": "Rp",
      "country": "Indonesia",
      "country_code": "ID",
      "is_base_currency": true,
      "decimal_places": 0,
      "display_name": "Indonesian Rupiah (IDR) - Rp"
    },
    {
      "code": "USD",
      "name": "United States Dollar",
      "symbol": "$",
      "country": "United States",
      "country_code": "US",
      "is_base_currency": false,
      "decimal_places": 2,
      "display_name": "United States Dollar (USD) - $"
    }
  ],
  "metadata": {
    "total_currencies": 31,
    "supported_pairs": 961,
    "base_currencies": ["IDR"],
    "last_updated": "2025-11-18T02:54:14.611987900",
    "version": "2.0.0"
  }
}
```

#### Simple Format (Code Only)
Get just the currency codes for simple integration.

```bash
curl -X GET "http://localhost:8080/api/finance/data/supported_currencies?format=simple"
```

**Expected Response:**
```json
{
  "currencies": ["AUD", "BGN", "BRL", "CAD", "CHF", "CNY", "CZK", "DKK", "EUR", "GBP", "HKD", "HUF", "IDR", "ILS", "INR", "ISK", "JPY", "KRW", "MXN", "MYR", "NOK", "NZD", "PHP", "PLN", "RON", "SEK", "SGD", "THB", "TRY", "USD", "ZAR"]
}
```

✅ Enhanced format with metadata  
✅ Simple format (backward compatible)  
✅ Currency enrichment workflows  
✅ Service availability checks

---

### 4. Historical Data with Custom Parameters

#### IDR to USD Historical Rates
Get historical exchange rates from Indonesian Rupiah to US Dollar.

```bash
curl -X GET "http://localhost:8080/api/finance/data/historical/custom?start=2025-11-03&end=2025-11-30&from=IDR&to=USD"
```

#### USD to IDR Historical Rates
Get historical exchange rates from US Dollar to Indonesian Rupiah.

```bash
curl -X GET "http://localhost:8080/api/finance/data/historical/custom?start=2025-11-01&end=2025-11-30&from=USD&to=IDR"
```

**Expected Response:**
```json
{
  "amount": "1",
  "base": "IDR",
  "start_date": "2025-11-03",
  "end_date": "2025-11-30",
  "rates": {
    "2025-11-30": {
      "USD": 0.000064
    },
    "2025-11-29": {
      "USD": 0.000065
    },
    "2025-11-03": {
      "USD": 0.000066
    }
  }
}
```

✅ Success with valid parameters  
✅ Date format validation (YYYY-MM-DD)  
✅ Currency code validation (ISO 4217)  
✅ Date sorting (newest first)  
✅ External API failure handling  
✅ Missing parameter validation  
✅ Service initialization states

---


**Parameter Validation Testing:**
- ✅ Valid date formats: `2024-01-01`, `2024-12-31`
- ✅ Invalid date formats: `01-01-2024`, `invalid-date`, `2024/01/01`
- ✅ Valid currency codes: `USD`, `EUR`, `JPY`, `IDR`
- ✅ Invalid currency codes: `usd`, `US`, `USDA`, `U1D`, `U-D`

**Error Response Validation:**
- ✅ HTTP Status Codes: 200, 400, 500, 503
- ✅ JSON error structure validation
- ✅ Error message content verification
- ✅ Proper error propagation



#### Quick Test Commands
```bash
# Run all unit tests (fast feedback) - Excluding integration tests
mvn test -Dtest="FinanceControllerTest,DataStoreServiceTest,CurrencyMetadataServiceTest,SpreadFactorCalculatorTest,LatestRatesStrategyTest"

# Run only new comprehensive tests
mvn test -Dtest="FinanceControllerTest,*ServiceTest"

# Run controller tests only
mvn test -Dtest="FinanceControllerTest"

# Run service layer tests only
mvn test -Dtest="DataStoreServiceTest,CurrencyMetadataServiceTest"

# Run all tests including integration tests
mvn test
```

**Data Source Strategy:**
  - Latest rates and supported currencies: Pre-fetched at startup and stored in-memory
  - Historical data: Fetched in real-time from external API
  - Thread-safe concurrent access using ReadWriteLock pattern
  - Concurrency: All endpoints support concurrent requests with thread-safe data access

### Supported Currencies

The API supports the following 31 major world currencies:
AUD, BGN, BRL, CAD, CHF, CNY, CZK, DKK, EUR, GBP, HKD, HUF, IDR, ILS, INR, ISK, JPY, KRW, MXN, MYR, NOK, NZD, PHP, PLN, RON, SEK, SGD, THB, TRY, USD, ZAR

### Personalization Note

**GitHub Username:** `dihardmg`
**Spread Factor Calculation:**
- GitHub username: `dihardmg`
- Character codes: `d(100) + i(105) + h(104) + a(97) + r(114) + d(100) + m(109) + g(103) = 832`
- Spread Factor: `(832 % 1000) / 100000.0 = 0.00832`

The unique spread factor used for USD_BuySpread_IDR calculation is **0.00832**.

## API Testing & Development

### REST Client Testing

We provide comprehensive REST client testing through `testing.http` file with 50+ test scenarios. The file contains detailed test cases for all endpoints including success scenarios, error handling, and edge cases.

#### Basic Endpoint Testing

```bash
# Health Check
GET http://localhost:8080/api/finance/data/health

# Enhanced Currencies (default format)
GET http://localhost:8080/api/finance/data/supported_currencies

# Simple Currencies (backward compatible format)
GET http://localhost:8080/api/finance/data/supported_currencies?format=simple

# Latest IDR Rates with Spread
GET http://localhost:8080/api/finance/data/latest_idr_rates

# Custom Historical Data
GET http://localhost:8080/api/finance/data/historical/custom?start=2024-01-01&end=2024-01-05&from=IDR&to=USD
```

#### Advanced Testing Scenarios

The `testing.http` file includes comprehensive test cases for:

1. **Historical Data Variations:**
   - Different date ranges (single day, week, month)
   - Various currency pairs
   - Edge cases (invalid dates, future dates)

2. **Error Condition Testing:**
   - Invalid resource types
   - Malformed date formats
   - Invalid currency codes
   - Missing required parameters

3. **Performance Testing:**
   - Concurrent requests
   - Large date range queries
   - Response time validation

4. **Data Validation:**
   - Response structure verification
   - Data type validation
   - Business logic verification (spread calculation)


##  Architectural Rationale

This section contains a detailed explanation answering the following questions:

### 1. Polymorphism Justification

The Strategy Pattern was used over a simpler conditional block in the service layer for handling the multi-resource endpoint for several key reasons:


**Maintainability:** Each strategy encapsulates its own logic, making the codebase more modular and easier to maintain. Testing becomes simpler as each strategy can be unit tested independently. The separation of concerns ensures that changes to one resource type don't affect others.

**Dependency Injection:** Spring's dependency injection naturally works with the Strategy Pattern, allowing for clean configuration and automatic wiring of strategy implementations. The controller remains clean and focused on HTTP concerns rather than business logic.

### 2. Client Factory

Using a `FactoryBean` to construct the external API client provides several specific benefits over a standard `@Bean` method:

**Lazy Initialization:** FactoryBean supports lazy initialization, which can be beneficial for resources that might not be needed immediately or have expensive setup costs.

**Complex Configuration:** FactoryBean allows for more complex object construction logic, including conditional bean creation, complex dependency wiring, and runtime configuration decisions that would be cumbersome in a simple @Bean method.

**External Configuration Integration:** The FactoryBean pattern provides a clean way to integrate with external configuration properties, allowing the client configuration to be externalized while maintaining a clean separation of concerns.

**Lifecycle Management:** FactoryBean gives more control over the bean lifecycle, allowing for custom initialization and destruction logic that might be needed for HTTP clients.

**Testability:** FactoryBean makes it easier to mock or replace the client in tests by providing a clear abstraction point for client creation.

### 3. Startup Runner Choice

Using an `ApplicationRunner` for initial data ingestion over a `@PostConstruct` method was chosen for these specific reasons:

**Application Lifecycle:** ApplicationRunner runs after the application context is fully initialized, ensuring all beans (including the WebClient, strategies, and data store) are available. @PostConstruct runs during bean initialization, which might cause ordering issues with other beans.

**Error Handling:** ApplicationRunner provides better error handling capabilities. If data loading fails, we can fail fast and prevent the application from starting in an inconsistent state. @PostConstruct errors can be harder to handle gracefully.

**Dependency Resolution:** ApplicationRunner has access to the fully constructed application context, eliminating potential circular dependency issues that can occur with @PostConstruct methods.

**Asynchronous Operations:** ApplicationRunner works naturally with reactive programming patterns (Mono/Flux), making it ideal for our WebClient-based approach. @PostConstruct methods are synchronous and don't handle reactive streams well.

**Timeout Control:** ApplicationRunner allows for proper timeout handling of external API calls, ensuring the application doesn't hang indefinitely if the external service is unavailable.


**Built with ❤️ by dihardmg@gmail.com**
