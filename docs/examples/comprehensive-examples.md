# FilterQL Examples

This section provides comprehensive, real-world examples demonstrating FilterQL's capabilities across different use cases.

## Table of Contents

1. [Basic Filtering Examples](#basic-filtering-examples)
2. [Advanced Query Patterns](#advanced-query-patterns)
3. [Real-World Use Cases](#real-world-use-cases)
4. [Performance Optimization Examples](#performance-optimization-examples)
5. [Integration Examples](#integration-examples)

## Basic Filtering Examples

### Simple Property Filtering

```java
// Entity definition
@Entity
public class Product {
    @Id
    private Long id;
    private String name;
    private BigDecimal price;
    private ProductCategory category;
    private LocalDateTime createdDate;
    private boolean active;
    
    // getters/setters...
}

// Property references
public enum ProductPropertyRef implements PropertyReference {
    NAME(String.class, Set.of(Op.EQ, Op.MATCHES)),
    PRICE(BigDecimal.class, OperatorUtils.FOR_NUMBER),
    CATEGORY(ProductCategory.class, Set.of(Op.EQ, Op.IN)),
    CREATED_DATE(LocalDateTime.class, OperatorUtils.FOR_DATE),
    IS_ACTIVE(Boolean.class, Set.of(Op.EQ));
    
    private final Class<?> type;
    private final Set<Op> operators;
    
    ProductPropertyRef(Class<?> type, Set<Op> operators) {
        this.type = type;
        this.operators = operators;
    }
    
    @Override
    public Class<?> type() { return type; }
    
    @Override
    public Set<Op> supportedOperators() { return operators; }
}

// Basic filtering examples
public class ProductFilterExamples {
    
    // Example 1: Find products by exact name
    public FilterRequest<ProductPropertyRef> findProductByName(String name) {
        return FilterRequest.<ProductPropertyRef>builder()
            .filter("nameFilter", new FilterDefinition<>(ProductPropertyRef.NAME, Op.EQ, name))
            .build();
    }
    
    // Example 2: Find products with name containing text
    public FilterRequest<ProductPropertyRef> findProductsContaining(String text) {
        return FilterRequest.<ProductPropertyRef>builder()
            .filter("nameSearch", new FilterDefinition<>(ProductPropertyRef.NAME, Op.MATCHES, "%" + text + "%"))
            .build();
    }
    
    // Example 3: Find products in price range
    public FilterRequest<ProductPropertyRef> findProductsInPriceRange(BigDecimal min, BigDecimal max) {
        return FilterRequest.<ProductPropertyRef>builder()
            .filter("priceRange", new FilterDefinition<>(ProductPropertyRef.PRICE, Op.RANGE, Arrays.asList(min, max)))
            .build();
    }
    
    // Example 4: Find products by multiple categories
    public FilterRequest<ProductPropertyRef> findProductsByCategories(List<ProductCategory> categories) {
        return FilterRequest.<ProductPropertyRef>builder()
            .filter("categories", new FilterDefinition<>(ProductPropertyRef.CATEGORY, Op.IN, categories))
            .build();
    }
    
    // Example 5: Find active products only
    public FilterRequest<ProductPropertyRef> findActiveProducts() {
        return FilterRequest.<ProductPropertyRef>builder()
            .filter("activeOnly", new FilterDefinition<>(ProductPropertyRef.IS_ACTIVE, Op.EQ, true))
            .build();
    }
}
```

### Complex Boolean Logic

```java
public class ComplexFilterExamples {
    
    // Example 1: AND combination - Active products in specific categories with price range
    public FilterRequest<ProductPropertyRef> findActiveProductsInCategoryWithPriceRange(
            List<ProductCategory> categories, BigDecimal minPrice, BigDecimal maxPrice) {
        
        return FilterRequest.<ProductPropertyRef>builder()
            .filter("active", new FilterDefinition<>(ProductPropertyRef.IS_ACTIVE, Op.EQ, true))
            .filter("categories", new FilterDefinition<>(ProductPropertyRef.CATEGORY, Op.IN, categories))
            .filter("priceRange", new FilterDefinition<>(ProductPropertyRef.PRICE, Op.RANGE, Arrays.asList(minPrice, maxPrice)))
            .combineWith("active & categories & priceRange")
            .build();
    }
    
    // Example 2: OR combination - Products matching name OR in specific categories
    public FilterRequest<ProductPropertyRef> findProductsByNameOrCategory(
            String namePattern, List<ProductCategory> categories) {
        
        return FilterRequest.<ProductPropertyRef>builder()
            .filter("nameSearch", new FilterDefinition<>(ProductPropertyRef.NAME, Op.MATCHES, namePattern))
            .filter("categories", new FilterDefinition<>(ProductPropertyRef.CATEGORY, Op.IN, categories))
            .combineWith("nameSearch | categories")
            .build();
    }
    
    // Example 3: Complex nested logic - (cheap OR expensive) AND active
    public FilterRequest<ProductPropertyRef> findBudgetOrPremiumActiveProducts() {
        return FilterRequest.<ProductPropertyRef>builder()
            .filter("cheap", new FilterDefinition<>(ProductPropertyRef.PRICE, Op.LT, new BigDecimal("50")))
            .filter("expensive", new FilterDefinition<>(ProductPropertyRef.PRICE, Op.GT, new BigDecimal("500")))
            .filter("active", new FilterDefinition<>(ProductPropertyRef.IS_ACTIVE, Op.EQ, true))
            .combineWith("(cheap | expensive) & active")
            .build();
    }
    
    // Example 4: NOT logic - Active products NOT in specific categories
    public FilterRequest<ProductPropertyRef> findActiveProductsNotInCategories(List<ProductCategory> excludedCategories) {
        return FilterRequest.<ProductPropertyRef>builder()
            .filter("active", new FilterDefinition<>(ProductPropertyRef.IS_ACTIVE, Op.EQ, true))
            .filter("excludedCategories", new FilterDefinition<>(ProductPropertyRef.CATEGORY, Op.IN, excludedCategories))
            .combineWith("active & !excludedCategories")
            .build();
    }
}
```

## Advanced Query Patterns

### Custom Business Logic

```java
// Advanced property references with business logic
public enum OrderPropertyRef implements PropertyReference {
    // Standard properties
    ORDER_DATE(LocalDateTime.class, OperatorUtils.FOR_DATE),
    TOTAL_AMOUNT(BigDecimal.class, OperatorUtils.FOR_NUMBER),
    STATUS(OrderStatus.class, Set.of(Op.EQ, Op.IN)),
    
    // Business logic properties
    IS_URGENT(Boolean.class, Set.of(Op.EQ)),
    IS_HIGH_VALUE(Boolean.class, Set.of(Op.EQ)),
    CUSTOMER_TIER(CustomerTier.class, Set.of(Op.EQ, Op.IN)),
    HAS_DISCOUNT(Boolean.class, Set.of(Op.EQ)),
    FULFILLMENT_RISK(RiskLevel.class, Set.of(Op.EQ, Op.IN));
    
    // Implementation details...
    
    private final Class<?> type;
    private final Set<Op> operators;
    
    OrderPropertyRef(Class<?> type, Set<Op> operators) {
        this.type = type;
        this.operators = operators;
    }
    
    @Override
    public Class<?> type() { return type; }
    
    @Override
    public Set<Op> supportedOperators() { return operators; }
}

// Custom mapping configuration
@Configuration
public class OrderFilterConfig {
    
    @Bean
    public FilterContext<Order, OrderPropertyRef> orderFilterContext() {
        return new FilterContext<>(
            Order.class,
            OrderPropertyRef.class,
            definition -> switch (definition.ref()) {
                // Simple mappings
                case ORDER_DATE -> "orderDate";
                case TOTAL_AMOUNT -> "totalAmount";
                case STATUS -> "status";
                
                // Complex business logic mappings
                case IS_URGENT -> createUrgentOrderMapping();
                case IS_HIGH_VALUE -> createHighValueMapping();
                case CUSTOMER_TIER -> createCustomerTierMapping();
                case HAS_DISCOUNT -> createDiscountMapping();
                case FULFILLMENT_RISK -> createRiskMapping();
            }
        );
    }
    
    private static PredicateResolverMapping<Order, OrderPropertyRef> createUrgentOrderMapping() {
        return definition -> (root, query, cb) -> {
            boolean isUrgent = (Boolean) definition.value();
            LocalDateTime urgentThreshold = LocalDateTime.now().plusHours(24);
            
            if (isUrgent) {
                // Urgent: High priority OR due within 24 hours OR express shipping
                return cb.or(
                    cb.equal(root.get("priority"), Priority.HIGH),
                    cb.lessThan(root.get("dueDate"), urgentThreshold),
                    cb.equal(root.get("shippingMethod"), ShippingMethod.EXPRESS)
                );
            } else {
                // Not urgent
                return cb.and(
                    cb.notEqual(root.get("priority"), Priority.HIGH),
                    cb.greaterThanOrEqualTo(root.get("dueDate"), urgentThreshold),
                    cb.notEqual(root.get("shippingMethod"), ShippingMethod.EXPRESS)
                );
            }
        };
    }
    
    private static PredicateResolverMapping<Order, OrderPropertyRef> createHighValueMapping() {
        return definition -> (root, query, cb) -> {
            boolean isHighValue = (Boolean) definition.value();
            BigDecimal highValueThreshold = new BigDecimal("1000");
            
            if (isHighValue) {
                return cb.greaterThan(root.get("totalAmount"), highValueThreshold);
            } else {
                return cb.lessThanOrEqualTo(root.get("totalAmount"), highValueThreshold);
            }
        };
    }
    
    private static PredicateResolverMapping<Order, OrderPropertyRef> createCustomerTierMapping() {
        return definition -> (root, query, cb) -> {
            // Join with customer to get tier information
            Join<Order, Customer> customerJoin = root.join("customer", JoinType.INNER);
            return cb.equal(customerJoin.get("tier"), definition.value());
        };
    }
    
    private static PredicateResolverMapping<Order, OrderPropertyRef> createDiscountMapping() {
        return definition -> (root, query, cb) -> {
            boolean hasDiscount = (Boolean) definition.value();
            
            if (hasDiscount) {
                // Has any discount applied
                return cb.or(
                    cb.isNotNull(root.get("discountCode")),
                    cb.greaterThan(root.get("discountAmount"), BigDecimal.ZERO),
                    cb.greaterThan(root.get("couponValue"), BigDecimal.ZERO)
                );
            } else {
                // No discounts
                return cb.and(
                    cb.isNull(root.get("discountCode")),
                    cb.or(
                        cb.isNull(root.get("discountAmount")),
                        cb.equal(root.get("discountAmount"), BigDecimal.ZERO)
                    ),
                    cb.or(
                        cb.isNull(root.get("couponValue")),
                        cb.equal(root.get("couponValue"), BigDecimal.ZERO)
                    )
                );
            }
        };
    }
    
    private static PredicateResolverMapping<Order, OrderPropertyRef> createRiskMapping() {
        return definition -> (root, query, cb) -> {
            RiskLevel riskLevel = (RiskLevel) definition.value();
            
            // Complex risk calculation based on multiple factors
            return switch (riskLevel) {
                case LOW -> cb.and(
                    cb.lessThan(root.get("totalAmount"), new BigDecimal("500")),
                    cb.equal(root.get("customer").get("creditRating"), CreditRating.EXCELLENT),
                    cb.equal(root.get("paymentMethod"), PaymentMethod.CREDIT_CARD)
                );
                
                case MEDIUM -> cb.or(
                    cb.between(root.get("totalAmount"), new BigDecimal("500"), new BigDecimal("2000")),
                    cb.equal(root.get("customer").get("creditRating"), CreditRating.GOOD),
                    cb.equal(root.get("paymentMethod"), PaymentMethod.BANK_TRANSFER)
                );
                
                case HIGH -> cb.or(
                    cb.greaterThan(root.get("totalAmount"), new BigDecimal("2000")),
                    cb.in(root.get("customer").get("creditRating")).value(CreditRating.FAIR, CreditRating.POOR),
                    cb.equal(root.get("paymentMethod"), PaymentMethod.CHECK),
                    cb.notEqual(root.get("shippingAddress").get("country"), "US")
                );
            };
        };
    }
}

// Usage examples with business logic
public class BusinessLogicFilterExamples {
    
    // Example 1: Find urgent high-value orders
    public FilterRequest<OrderPropertyRef> findUrgentHighValueOrders() {
        return FilterRequest.<OrderPropertyRef>builder()
            .filter("urgent", new FilterDefinition<>(OrderPropertyRef.IS_URGENT, Op.EQ, true))
            .filter("highValue", new FilterDefinition<>(OrderPropertyRef.IS_HIGH_VALUE, Op.EQ, true))
            .combineWith("urgent & highValue")
            .build();
    }
    
    // Example 2: Find risky orders for review
    public FilterRequest<OrderPropertyRef> findOrdersForRiskReview() {
        return FilterRequest.<OrderPropertyRef>builder()
            .filter("highRisk", new FilterDefinition<>(OrderPropertyRef.FULFILLMENT_RISK, Op.EQ, RiskLevel.HIGH))
            .filter("recentOrders", new FilterDefinition<>(OrderPropertyRef.ORDER_DATE, Op.GT, LocalDateTime.now().minusDays(7)))
            .combineWith("highRisk & recentOrders")
            .build();
    }
    
    // Example 3: Customer tier analysis
    public FilterRequest<OrderPropertyRef> findPremiumCustomerOrders(BigDecimal minAmount) {
        return FilterRequest.<OrderPropertyRef>builder()
            .filter("premiumTier", new FilterDefinition<>(OrderPropertyRef.CUSTOMER_TIER, Op.IN, 
                Arrays.asList(CustomerTier.GOLD, CustomerTier.PLATINUM)))
            .filter("significantAmount", new FilterDefinition<>(OrderPropertyRef.TOTAL_AMOUNT, Op.GTE, minAmount))
            .filter("hasDiscount", new FilterDefinition<>(OrderPropertyRef.HAS_DISCOUNT, Op.EQ, true))
            .combineWith("premiumTier & significantAmount & hasDiscount")
            .build();
    }
}
```

### Subqueries and Aggregations

```java
// Advanced aggregation examples
public enum CustomerPropertyRef implements PropertyReference {
    // Standard properties
    NAME(String.class, Set.of(Op.EQ, Op.MATCHES)),
    EMAIL(String.class, Set.of(Op.EQ, Op.MATCHES)),
    REGISTRATION_DATE(LocalDateTime.class, OperatorUtils.FOR_DATE),
    
    // Aggregation-based properties
    TOTAL_ORDER_VALUE(BigDecimal.class, OperatorUtils.FOR_NUMBER),
    ORDER_COUNT(Integer.class, OperatorUtils.FOR_NUMBER),
    AVERAGE_ORDER_VALUE(BigDecimal.class, OperatorUtils.FOR_NUMBER),
    LAST_ORDER_DATE(LocalDateTime.class, OperatorUtils.FOR_DATE),
    HAS_RECENT_ORDERS(Boolean.class, Set.of(Op.EQ)),
    MOST_FREQUENT_CATEGORY(ProductCategory.class, Set.of(Op.EQ, Op.IN));
    
    // Implementation...
}

@Configuration
public class CustomerAggregationConfig {
    
    @Bean
    public FilterContext<Customer, CustomerPropertyRef> customerFilterContext() {
        return new FilterContext<>(
            Customer.class,
            CustomerPropertyRef.class,
            definition -> switch (definition.ref()) {
                case NAME -> "name";
                case EMAIL -> "email";
                case REGISTRATION_DATE -> "registrationDate";
                
                // Aggregation mappings
                case TOTAL_ORDER_VALUE -> createTotalOrderValueMapping();
                case ORDER_COUNT -> createOrderCountMapping();
                case AVERAGE_ORDER_VALUE -> createAverageOrderValueMapping();
                case LAST_ORDER_DATE -> createLastOrderDateMapping();
                case HAS_RECENT_ORDERS -> createRecentOrdersMapping();
                case MOST_FREQUENT_CATEGORY -> createMostFrequentCategoryMapping();
            }
        );
    }
    
    private static PredicateResolverMapping<Customer, CustomerPropertyRef> createTotalOrderValueMapping() {
        return definition -> (root, query, cb) -> {
            // Subquery to calculate total order value
            Subquery<BigDecimal> subquery = query.subquery(BigDecimal.class);
            Root<Order> orderRoot = subquery.from(Order.class);
            
            subquery.select(cb.coalesce(cb.sum(orderRoot.get("totalAmount")), BigDecimal.ZERO))
                   .where(cb.equal(orderRoot.get("customer"), root));
            
            BigDecimal targetValue = (BigDecimal) definition.value();
            
            return switch (definition.operator()) {
                case EQ -> cb.equal(subquery, targetValue);
                case GT -> cb.greaterThan(subquery, targetValue);
                case GTE -> cb.greaterThanOrEqualTo(subquery, targetValue);
                case LT -> cb.lessThan(subquery, targetValue);
                case LTE -> cb.lessThanOrEqualTo(subquery, targetValue);
                case RANGE -> {
                    List<BigDecimal> range = (List<BigDecimal>) definition.value();
                    yield cb.between(subquery, range.get(0), range.get(1));
                }
                default -> throw new UnsupportedOperationException("Operator not supported: " + definition.operator());
            };
        };
    }
    
    private static PredicateResolverMapping<Customer, CustomerPropertyRef> createOrderCountMapping() {
        return definition -> (root, query, cb) -> {
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<Order> orderRoot = subquery.from(Order.class);
            
            subquery.select(cb.count(orderRoot))
                   .where(cb.equal(orderRoot.get("customer"), root));
            
            Integer targetCount = (Integer) definition.value();
            
            return switch (definition.operator()) {
                case EQ -> cb.equal(subquery, targetCount.longValue());
                case GT -> cb.greaterThan(subquery, targetCount.longValue());
                case GTE -> cb.greaterThanOrEqualTo(subquery, targetCount.longValue());
                case LT -> cb.lessThan(subquery, targetCount.longValue());
                case LTE -> cb.lessThanOrEqualTo(subquery, targetCount.longValue());
                default -> throw new UnsupportedOperationException("Operator not supported: " + definition.operator());
            };
        };
    }
    
    private static PredicateResolverMapping<Customer, CustomerPropertyRef> createRecentOrdersMapping() {
        return definition -> (root, query, cb) -> {
            boolean hasRecentOrders = (Boolean) definition.value();
            LocalDateTime threshold = LocalDateTime.now().minusDays(30);
            
            Subquery<Integer> subquery = query.subquery(Integer.class);
            Root<Order> orderRoot = subquery.from(Order.class);
            
            subquery.select(cb.literal(1))
                   .where(
                       cb.equal(orderRoot.get("customer"), root),
                       cb.greaterThan(orderRoot.get("orderDate"), threshold)
                   );
            
            if (hasRecentOrders) {
                return cb.exists(subquery);
            } else {
                return cb.not(cb.exists(subquery));
            }
        };
    }
}
```

## Real-World Use Cases

### E-commerce Product Search

```java
@RestController
@RequestMapping("/api/products")
public class ProductSearchController {
    
    private final ProductService productService;
    
    @PostMapping("/search")
    public ResponseEntity<Page<ProductDTO>> searchProducts(
            @RequestBody ProductSearchRequest searchRequest,
            @PageableDefault(size = 20) Pageable pageable) {
        
        FilterRequest<ProductPropertyRef> filterRequest = buildFilterRequest(searchRequest);
        Page<Product> products = productService.searchProducts(filterRequest, pageable);
        
        return ResponseEntity.ok(products.map(this::toDTO));
    }
    
    private FilterRequest<ProductPropertyRef> buildFilterRequest(ProductSearchRequest searchRequest) {
        FilterRequest.Builder<ProductPropertyRef> builder = FilterRequest.builder();
        List<String> conditions = new ArrayList<>();
        
        // Text search
        if (searchRequest.getQuery() != null && !searchRequest.getQuery().isEmpty()) {
            builder.filter("textSearch", new FilterDefinition<>(
                ProductPropertyRef.NAME, Op.MATCHES, "%" + searchRequest.getQuery() + "%"
            ));
            conditions.add("textSearch");
        }
        
        // Category filter
        if (searchRequest.getCategories() != null && !searchRequest.getCategories().isEmpty()) {
            builder.filter("categories", new FilterDefinition<>(
                ProductPropertyRef.CATEGORY, Op.IN, searchRequest.getCategories()
            ));
            conditions.add("categories");
        }
        
        // Price range
        if (searchRequest.getMinPrice() != null || searchRequest.getMaxPrice() != null) {
            BigDecimal min = searchRequest.getMinPrice() != null ? searchRequest.getMinPrice() : BigDecimal.ZERO;
            BigDecimal max = searchRequest.getMaxPrice() != null ? searchRequest.getMaxPrice() : new BigDecimal("999999");
            
            builder.filter("priceRange", new FilterDefinition<>(
                ProductPropertyRef.PRICE, Op.RANGE, Arrays.asList(min, max)
            ));
            conditions.add("priceRange");
        }
        
        // Brand filter
        if (searchRequest.getBrands() != null && !searchRequest.getBrands().isEmpty()) {
            builder.filter("brands", new FilterDefinition<>(
                ProductPropertyRef.BRAND, Op.IN, searchRequest.getBrands()
            ));
            conditions.add("brands");
        }
        
        // Availability
        if (searchRequest.getInStock() != null && searchRequest.getInStock()) {
            builder.filter("inStock", new FilterDefinition<>(
                ProductPropertyRef.STOCK_QUANTITY, Op.GT, 0
            ));
            conditions.add("inStock");
        }
        
        // Rating filter
        if (searchRequest.getMinRating() != null) {
            builder.filter("rating", new FilterDefinition<>(
                ProductPropertyRef.AVERAGE_RATING, Op.GTE, searchRequest.getMinRating()
            ));
            conditions.add("rating");
        }
        
        // On sale
        if (searchRequest.getOnSale() != null && searchRequest.getOnSale()) {
            builder.filter("onSale", new FilterDefinition<>(
                ProductPropertyRef.HAS_DISCOUNT, Op.EQ, true
            ));
            conditions.add("onSale");
        }
        
        // Combine all conditions with AND
        if (!conditions.isEmpty()) {
            builder.combineWith(String.join(" & ", conditions));
        }
        
        return builder.build();
    }
}
```

### HR Employee Management

```java
@Service
public class EmployeeSearchService {
    
    public Page<Employee> searchEmployees(EmployeeSearchCriteria criteria, Pageable pageable) {
        FilterRequest<EmployeePropertyRef> filterRequest = buildEmployeeFilter(criteria);
        return employeeRepository.searchEmployees(filterRequest, pageable);
    }
    
    private FilterRequest<EmployeePropertyRef> buildEmployeeFilter(EmployeeSearchCriteria criteria) {
        FilterRequest.Builder<EmployeePropertyRef> builder = FilterRequest.builder();
        List<String> conditions = new ArrayList<>();
        
        // Basic filters
        if (criteria.getName() != null) {
            builder.filter("name", new FilterDefinition<>(
                EmployeePropertyRef.FULL_NAME_SEARCH, Op.MATCHES, criteria.getName()
            ));
            conditions.add("name");
        }
        
        if (criteria.getDepartments() != null && !criteria.getDepartments().isEmpty()) {
            builder.filter("departments", new FilterDefinition<>(
                EmployeePropertyRef.DEPARTMENT, Op.IN, criteria.getDepartments()
            ));
            conditions.add("departments");
        }
        
        if (criteria.getPositions() != null && !criteria.getPositions().isEmpty()) {
            builder.filter("positions", new FilterDefinition<>(
                EmployeePropertyRef.POSITION, Op.IN, criteria.getPositions()
            ));
            conditions.add("positions");
        }
        
        // Employment status
        if (criteria.getEmploymentStatus() != null) {
            builder.filter("status", new FilterDefinition<>(
                EmployeePropertyRef.EMPLOYMENT_STATUS, Op.EQ, criteria.getEmploymentStatus()
            ));
            conditions.add("status");
        }
        
        // Hire date range
        if (criteria.getHireDateFrom() != null || criteria.getHireDateTo() != null) {
            LocalDateTime from = criteria.getHireDateFrom() != null ? 
                criteria.getHireDateFrom() : LocalDateTime.of(1900, 1, 1, 0, 0);
            LocalDateTime to = criteria.getHireDateTo() != null ? 
                criteria.getHireDateTo() : LocalDateTime.now();
                
            builder.filter("hireDate", new FilterDefinition<>(
                EmployeePropertyRef.HIRE_DATE, Op.RANGE, Arrays.asList(from, to)
            ));
            conditions.add("hireDate");
        }
        
        // Salary range
        if (criteria.getMinSalary() != null || criteria.getMaxSalary() != null) {
            BigDecimal min = criteria.getMinSalary() != null ? criteria.getMinSalary() : BigDecimal.ZERO;
            BigDecimal max = criteria.getMaxSalary() != null ? criteria.getMaxSalary() : new BigDecimal("1000000");
            
            builder.filter("salary", new FilterDefinition<>(
                EmployeePropertyRef.SALARY, Op.RANGE, Arrays.asList(min, max)
            ));
            conditions.add("salary");
        }
        
        // Skills
        if (criteria.getRequiredSkills() != null && !criteria.getRequiredSkills().isEmpty()) {
            builder.filter("skills", new FilterDefinition<>(
                EmployeePropertyRef.HAS_SKILLS, Op.EQ, criteria.getRequiredSkills()
            ));
            conditions.add("skills");
        }
        
        // Performance rating
        if (criteria.getMinPerformanceRating() != null) {
            builder.filter("performance", new FilterDefinition<>(
                EmployeePropertyRef.PERFORMANCE_RATING, Op.GTE, criteria.getMinPerformanceRating()
            ));
            conditions.add("performance");
        }
        
        // Manager filter
        if (criteria.getManagerId() != null) {
            builder.filter("manager", new FilterDefinition<>(
                EmployeePropertyRef.MANAGER_ID, Op.EQ, criteria.getManagerId()
            ));
            conditions.add("manager");
        }
        
        // Remote work capability
        if (criteria.getRemoteCapable() != null) {
            builder.filter("remote", new FilterDefinition<>(
                EmployeePropertyRef.REMOTE_CAPABLE, Op.EQ, criteria.getRemoteCapable()
            ));
            conditions.add("remote");
        }
        
        // Combine conditions
        if (!conditions.isEmpty()) {
            builder.combineWith(String.join(" & ", conditions));
        }
        
        return builder.build();
    }
    
    // Specialized search methods
    public List<Employee> findEligibleForPromotion() {
        return searchEmployees(FilterRequest.<EmployeePropertyRef>builder()
            .filter("tenure", new FilterDefinition<>(EmployeePropertyRef.TENURE_YEARS, Op.GTE, 2))
            .filter("performance", new FilterDefinition<>(EmployeePropertyRef.PERFORMANCE_RATING, Op.GTE, 4.0))
            .filter("status", new FilterDefinition<>(EmployeePropertyRef.EMPLOYMENT_STATUS, Op.EQ, EmploymentStatus.ACTIVE))
            .combineWith("tenure & performance & status")
            .build(), Pageable.unpaged()).getContent();
    }
    
    public List<Employee> findEmployeesNeedingReview() {
        LocalDateTime reviewThreshold = LocalDateTime.now().minusMonths(12);
        
        return searchEmployees(FilterRequest.<EmployeePropertyRef>builder()
            .filter("lastReview", new FilterDefinition<>(EmployeePropertyRef.LAST_REVIEW_DATE, Op.LT, reviewThreshold))
            .filter("status", new FilterDefinition<>(EmployeePropertyRef.EMPLOYMENT_STATUS, Op.EQ, EmploymentStatus.ACTIVE))
            .combineWith("lastReview & status")
            .build(), Pageable.unpaged()).getContent();
    }
    
    public List<Employee> findHighPerformersInDepartment(String department) {
        return searchEmployees(FilterRequest.<EmployeePropertyRef>builder()
            .filter("department", new FilterDefinition<>(EmployeePropertyRef.DEPARTMENT, Op.EQ, department))
            .filter("performance", new FilterDefinition<>(EmployeePropertyRef.PERFORMANCE_RATING, Op.GTE, 4.5))
            .filter("status", new FilterDefinition<>(EmployeePropertyRef.EMPLOYMENT_STATUS, Op.EQ, EmploymentStatus.ACTIVE))
            .combineWith("department & performance & status")
            .build(), Pageable.unpaged()).getContent();
    }
}
```

### Financial Transaction Analysis

```java
@Service
public class TransactionAnalysisService {
    
    // Fraud detection filters
    public List<Transaction> findSuspiciousTransactions() {
        return transactionRepository.searchTransactions(
            FilterRequest.<TransactionPropertyRef>builder()
                .filter("highAmount", new FilterDefinition<>(TransactionPropertyRef.AMOUNT, Op.GT, new BigDecimal("10000")))
                .filter("offHours", new FilterDefinition<>(TransactionPropertyRef.IS_OFF_HOURS, Op.EQ, true))
                .filter("foreignLocation", new FilterDefinition<>(TransactionPropertyRef.IS_FOREIGN_LOCATION, Op.EQ, true))
                .filter("recent", new FilterDefinition<>(TransactionPropertyRef.TRANSACTION_DATE, Op.GT, LocalDateTime.now().minusHours(24)))
                .combineWith("(highAmount & offHours) | (highAmount & foreignLocation) | (offHours & foreignLocation & recent)")
                .build(),
            Pageable.unpaged()
        ).getContent();
    }
    
    // Compliance reporting
    public Page<Transaction> findTransactionsForCompliance(ComplianceReportRequest request, Pageable pageable) {
        FilterRequest.Builder<TransactionPropertyRef> builder = FilterRequest.builder();
        List<String> conditions = new ArrayList<>();
        
        // Date range (required for compliance)
        builder.filter("dateRange", new FilterDefinition<>(
            TransactionPropertyRef.TRANSACTION_DATE, Op.RANGE, 
            Arrays.asList(request.getStartDate(), request.getEndDate())
        ));
        conditions.add("dateRange");
        
        // Amount threshold
        if (request.getMinAmount() != null) {
            builder.filter("minAmount", new FilterDefinition<>(
                TransactionPropertyRef.AMOUNT, Op.GTE, request.getMinAmount()
            ));
            conditions.add("minAmount");
        }
        
        // Transaction types
        if (request.getTransactionTypes() != null && !request.getTransactionTypes().isEmpty()) {
            builder.filter("types", new FilterDefinition<>(
                TransactionPropertyRef.TYPE, Op.IN, request.getTransactionTypes()
            ));
            conditions.add("types");
        }
        
        // Customer risk levels
        if (request.getRiskLevels() != null && !request.getRiskLevels().isEmpty()) {
            builder.filter("riskLevels", new FilterDefinition<>(
                TransactionPropertyRef.CUSTOMER_RISK_LEVEL, Op.IN, request.getRiskLevels()
            ));
            conditions.add("riskLevels");
        }
        
        // Cash transactions
        if (request.getCashOnly() != null && request.getCashOnly()) {
            builder.filter("cashOnly", new FilterDefinition<>(
                TransactionPropertyRef.IS_CASH_TRANSACTION, Op.EQ, true
            ));
            conditions.add("cashOnly");
        }
        
        // International transfers
        if (request.getInternationalOnly() != null && request.getInternationalOnly()) {
            builder.filter("international", new FilterDefinition<>(
                TransactionPropertyRef.IS_INTERNATIONAL, Op.EQ, true
            ));
            conditions.add("international");
        }
        
        builder.combineWith(String.join(" & ", conditions));
        
        return transactionRepository.searchTransactions(builder.build(), pageable);
    }
    
    // Customer spending analysis
    public CustomerSpendingReport analyzeCustomerSpending(Long customerId, LocalDateTime startDate, LocalDateTime endDate) {
        // Total spending
        List<Transaction> allTransactions = transactionRepository.searchTransactions(
            FilterRequest.<TransactionPropertyRef>builder()
                .filter("customer", new FilterDefinition<>(TransactionPropertyRef.CUSTOMER_ID, Op.EQ, customerId))
                .filter("dateRange", new FilterDefinition<>(TransactionPropertyRef.TRANSACTION_DATE, Op.RANGE, Arrays.asList(startDate, endDate)))
                .filter("completed", new FilterDefinition<>(TransactionPropertyRef.STATUS, Op.EQ, TransactionStatus.COMPLETED))
                .combineWith("customer & dateRange & completed")
                .build(),
            Pageable.unpaged()
        ).getContent();
        
        // Large transactions (over $1000)
        List<Transaction> largeTransactions = transactionRepository.searchTransactions(
            FilterRequest.<TransactionPropertyRef>builder()
                .filter("customer", new FilterDefinition<>(TransactionPropertyRef.CUSTOMER_ID, Op.EQ, customerId))
                .filter("dateRange", new FilterDefinition<>(TransactionPropertyRef.TRANSACTION_DATE, Op.RANGE, Arrays.asList(startDate, endDate)))
                .filter("largeAmount", new FilterDefinition<>(TransactionPropertyRef.AMOUNT, Op.GT, new BigDecimal("1000")))
                .filter("completed", new FilterDefinition<>(TransactionPropertyRef.STATUS, Op.EQ, TransactionStatus.COMPLETED))
                .combineWith("customer & dateRange & largeAmount & completed")
                .build(),
            Pageable.unpaged()
        ).getContent();
        
        // International transactions
        List<Transaction> internationalTransactions = transactionRepository.searchTransactions(
            FilterRequest.<TransactionPropertyRef>builder()
                .filter("customer", new FilterDefinition<>(TransactionPropertyRef.CUSTOMER_ID, Op.EQ, customerId))
                .filter("dateRange", new FilterDefinition<>(TransactionPropertyRef.TRANSACTION_DATE, Op.RANGE, Arrays.asList(startDate, endDate)))
                .filter("international", new FilterDefinition<>(TransactionPropertyRef.IS_INTERNATIONAL, Op.EQ, true))
                .filter("completed", new FilterDefinition<>(TransactionPropertyRef.STATUS, Op.EQ, TransactionStatus.COMPLETED))
                .combineWith("customer & dateRange & international & completed")
                .build(),
            Pageable.unpaged()
        ).getContent();
        
        return CustomerSpendingReport.builder()
            .customerId(customerId)
            .totalTransactions(allTransactions.size())
            .totalAmount(allTransactions.stream().map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add))
            .largeTransactionCount(largeTransactions.size())
            .internationalTransactionCount(internationalTransactions.size())
            .averageTransactionAmount(calculateAverage(allTransactions))
            .build();
    }
}
```

## Performance Optimization Examples

### Query Optimization with Fetch Joins

```java
// Optimized mappings that prevent N+1 queries
@Configuration
public class OptimizedFilterConfig {
    
    @Bean
    public FilterContext<Order, OrderPropertyRef> optimizedOrderContext() {
        return new FilterContext<>(
            Order.class,
            OrderPropertyRef.class,
            definition -> switch (definition.ref()) {
                // Simple properties - no optimization needed
                case ORDER_DATE -> "orderDate";
                case TOTAL_AMOUNT -> "totalAmount";
                case STATUS -> "status";
                
                // Optimized join properties
                case CUSTOMER_NAME -> createOptimizedCustomerNameMapping();
                case CUSTOMER_EMAIL -> createOptimizedCustomerEmailMapping();
                case PRODUCT_CATEGORIES -> createOptimizedProductCategoryMapping();
                case SHIPPING_ADDRESS -> createOptimizedShippingAddressMapping();
            }
        );
    }
    
    private static PredicateResolverMapping<Order, OrderPropertyRef> createOptimizedCustomerNameMapping() {
        return definition -> (root, query, cb) -> {
            // Add fetch join to prevent N+1 queries when loading results
            if (query.getResultType() == Order.class) {
                root.fetch("customer", JoinType.LEFT);
            }
            
            Join<Order, Customer> customerJoin = root.join("customer", JoinType.LEFT);
            return cb.equal(customerJoin.get("name"), definition.value());
        };
    }
    
    private static PredicateResolverMapping<Order, OrderPropertyRef> createOptimizedProductCategoryMapping() {
        return definition -> (root, query, cb) -> {
            // Complex join optimization - fetch all related entities
            if (query.getResultType() == Order.class) {
                Fetch<Order, OrderItem> itemsFetch = root.fetch("orderItems", JoinType.LEFT);
                itemsFetch.fetch("product", JoinType.LEFT);
            }
            
            Join<Order, OrderItem> itemsJoin = root.join("orderItems", JoinType.LEFT);
            Join<OrderItem, Product> productJoin = itemsJoin.join("product", JoinType.LEFT);
            
            return productJoin.get("category").in((Collection<?>) definition.value());
        };
    }
}

// Performance monitoring service
@Service
public class PerformanceOptimizedOrderService {
    
    private final OrderRepository orderRepository;
    private final FilterContext<Order, OrderPropertyRef> filterContext;
    private final PerformanceMonitor performanceMonitor;
    
    @Cacheable(value = "orderSearchResults", key = "#filterRequest.hashCode() + '_' + #pageable.hashCode()")
    public Page<Order> searchOrdersWithCaching(FilterRequest<OrderPropertyRef> filterRequest, Pageable pageable) {
        return performanceMonitor.measure("order-search", () -> {
            FilterResolver resolver = FilterResolver.of(filterContext);
            PredicateResolver<Order> predicateResolver = resolver.resolve(Order.class, filterRequest);
            
            Specification<Order> specification = (root, query, cb) -> 
                predicateResolver.resolve(root, query, cb);
            
            return orderRepository.findAll(specification, pageable);
        });
    }
    
    // Bulk operations for better performance
    @Transactional(readOnly = true)
    public List<OrderSummary> getOrderSummaries(FilterRequest<OrderPropertyRef> filterRequest) {
        // Use projection to fetch only required fields
        FilterResolver resolver = FilterResolver.of(filterContext);
        PredicateResolver<Order> predicateResolver = resolver.resolve(Order.class, filterRequest);
        
        return entityManager.createQuery(
            "SELECT new com.example.dto.OrderSummary(o.id, o.orderDate, o.totalAmount, o.status, c.name) " +
            "FROM Order o LEFT JOIN o.customer c WHERE " + buildWhereClause(predicateResolver),
            OrderSummary.class
        ).getResultList();
    }
}
```

### Database Index Recommendations

```sql
-- Indexes based on common FilterQL usage patterns

-- Single property indexes
CREATE INDEX idx_order_date ON orders(order_date);
CREATE INDEX idx_order_amount ON orders(total_amount);
CREATE INDEX idx_order_status ON orders(status);
CREATE INDEX idx_customer_name ON customers(name);
CREATE INDEX idx_product_category ON products(category);

-- Composite indexes for common filter combinations
CREATE INDEX idx_order_status_date ON orders(status, order_date);
CREATE INDEX idx_order_customer_date ON orders(customer_id, order_date);
CREATE INDEX idx_order_amount_status ON orders(total_amount, status);

-- Partial indexes for boolean filters
CREATE INDEX idx_orders_active ON orders(id) WHERE status = 'ACTIVE';
CREATE INDEX idx_customers_verified ON customers(id) WHERE is_verified = true;

-- Functional indexes for case-insensitive searches
CREATE INDEX idx_customer_name_lower ON customers(LOWER(name));
CREATE INDEX idx_product_name_lower ON products(LOWER(name));

-- Covering indexes for common projections
CREATE INDEX idx_order_summary_covering ON orders(id, order_date, total_amount, status) 
    INCLUDE (customer_id);
```

## Integration Examples

### REST API Integration

```java
@RestController
@RequestMapping("/api/orders")
@Validated
public class OrderController {
    
    private final OrderService orderService;
    private final FilterRequestValidator validator;
    
    @PostMapping("/search")
    public ResponseEntity<PagedResponse<OrderDTO>> searchOrders(
            @Valid @RequestBody OrderSearchRequest searchRequest,
            @PageableDefault(size = 20, sort = "orderDate", direction = Sort.Direction.DESC) Pageable pageable) {
        
        try {
            // Convert search request to FilterQL
            FilterRequest<OrderPropertyRef> filterRequest = convertToFilterRequest(searchRequest);
            
            // Validate the filter request
            validator.validate(filterRequest);
            
            // Execute search
            Page<Order> orders = orderService.searchOrders(filterRequest, pageable);
            
            // Convert to DTOs
            PagedResponse<OrderDTO> response = PagedResponse.<OrderDTO>builder()
                .content(orders.getContent().stream().map(this::toDTO).collect(Collectors.toList()))
                .page(orders.getNumber())
                .size(orders.getSize())
                .totalElements(orders.getTotalElements())
                .totalPages(orders.getTotalPages())
                .build();
            
            return ResponseEntity.ok(response);
            
        } catch (FilterValidationException e) {
            return ResponseEntity.badRequest()
                .body(PagedResponse.<OrderDTO>builder()
                    .error("INVALID_FILTER")
                    .message(e.getMessage())
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(PagedResponse.<OrderDTO>builder()
                    .error("SEARCH_ERROR")
                    .message("An error occurred while searching orders")
                    .build());
        }
    }
    
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportOrders(
            @Valid OrderSearchRequest searchRequest,
            @RequestParam(defaultValue = "CSV") ExportFormat format) {
        
        FilterRequest<OrderPropertyRef> filterRequest = convertToFilterRequest(searchRequest);
        List<Order> orders = orderService.searchAllOrders(filterRequest);
        
        byte[] exportData = switch (format) {
            case CSV -> exportService.exportToCSV(orders);
            case EXCEL -> exportService.exportToExcel(orders);
            case PDF -> exportService.exportToPDF(orders);
        };
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(getContentType(format));
        headers.setContentDispositionFormData("attachment", "orders." + format.name().toLowerCase());
        
        return ResponseEntity.ok()
            .headers(headers)
            .body(exportData);
    }
    
    // Advanced search with suggestions
    @PostMapping("/search/suggestions")
    public ResponseEntity<SearchSuggestions> getSearchSuggestions(
            @RequestBody PartialSearchRequest partialRequest) {
        
        SearchSuggestions suggestions = SearchSuggestions.builder()
            .customerSuggestions(orderService.suggestCustomers(partialRequest.getCustomerQuery()))
            .productSuggestions(orderService.suggestProducts(partialRequest.getProductQuery()))
            .categorySuggestions(orderService.suggestCategories(partialRequest.getCategoryQuery()))
            .build();
        
        return ResponseEntity.ok(suggestions);
    }
    
    private FilterRequest<OrderPropertyRef> convertToFilterRequest(OrderSearchRequest request) {
        FilterRequest.Builder<OrderPropertyRef> builder = FilterRequest.builder();
        List<String> conditions = new ArrayList<>();
        
        // ... conversion logic as shown in previous examples
        
        return builder.build();
    }
}

// Request/Response DTOs
@Data
@Builder
public class OrderSearchRequest {
    @Size(max = 100)
    private String customerName;
    
    @Past
    private LocalDateTime startDate;
    
    @Future
    private LocalDateTime endDate;
    
    @DecimalMin("0.01")
    private BigDecimal minAmount;
    
    @DecimalMax("1000000")
    private BigDecimal maxAmount;
    
    private List<OrderStatus> statuses;
    private List<String> productCategories;
    private Boolean hasDiscount;
    private PaymentMethod paymentMethod;
}

@Data
@Builder
public class PagedResponse<T> {
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private String error;
    private String message;
}
```

### GraphQL Integration

```java
@Component
public class OrderQueryResolver implements GraphQLQueryResolver {
    
    private final OrderService orderService;
    
    public Page<Order> searchOrders(
            OrderFilterInput filter,
            @DefaultValue("0") int page,
            @DefaultValue("20") int size,
            @DefaultValue("orderDate") String sortBy,
            @DefaultValue("DESC") String sortDirection) {
        
        FilterRequest<OrderPropertyRef> filterRequest = convertGraphQLFilter(filter);
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return orderService.searchOrders(filterRequest, pageable);
    }
    
    private FilterRequest<OrderPropertyRef> convertGraphQLFilter(OrderFilterInput filter) {
        if (filter == null) {
            return FilterRequest.<OrderPropertyRef>builder().build();
        }
        
        FilterRequest.Builder<OrderPropertyRef> builder = FilterRequest.builder();
        List<String> conditions = new ArrayList<>();
        
        if (filter.getCustomerName() != null) {
            builder.filter("customerName", new FilterDefinition<>(
                OrderPropertyRef.CUSTOMER_NAME, Op.MATCHES, "%" + filter.getCustomerName() + "%"
            ));
            conditions.add("customerName");
        }
        
        if (filter.getDateRange() != null) {
            builder.filter("dateRange", new FilterDefinition<>(
                OrderPropertyRef.ORDER_DATE, Op.RANGE, 
                Arrays.asList(filter.getDateRange().getStart(), filter.getDateRange().getEnd())
            ));
            conditions.add("dateRange");
        }
        
        if (filter.getAmountRange() != null) {
            builder.filter("amountRange", new FilterDefinition<>(
                OrderPropertyRef.TOTAL_AMOUNT, Op.RANGE,
                Arrays.asList(filter.getAmountRange().getMin(), filter.getAmountRange().getMax())
            ));
            conditions.add("amountRange");
        }
        
        if (filter.getStatuses() != null && !filter.getStatuses().isEmpty()) {
            builder.filter("statuses", new FilterDefinition<>(
                OrderPropertyRef.STATUS, Op.IN, filter.getStatuses()
            ));
            conditions.add("statuses");
        }
        
        if (!conditions.isEmpty()) {
            builder.combineWith(String.join(" & ", conditions));
        }
        
        return builder.build();
    }
}

// GraphQL Schema (orders.graphqls)
type Query {
    searchOrders(
        filter: OrderFilterInput
        page: Int = 0
        size: Int = 20
        sortBy: String = "orderDate"
        sortDirection: String = "DESC"
    ): OrderConnection
}

input OrderFilterInput {
    customerName: String
    dateRange: DateRangeInput
    amountRange: AmountRangeInput
    statuses: [OrderStatus!]
    hasDiscount: Boolean
    paymentMethod: PaymentMethod
}

input DateRangeInput {
    start: DateTime!
    end: DateTime!
}

input AmountRangeInput {
    min: BigDecimal!
    max: BigDecimal!
}

type OrderConnection {
    content: [Order!]!
    page: Int!
    size: Int!
    totalElements: Long!
    totalPages: Int!
}
```

These examples demonstrate FilterQL's versatility across different scenarios, from simple property filtering to complex business logic, aggregations, and various integration patterns. The key is to design your PropertyReference enums thoughtfully and leverage FilterQL's mapping capabilities to handle complex requirements while maintaining type safety and performance.