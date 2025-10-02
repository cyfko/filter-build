# FilterQL Spring Adapter

The FilterQL Spring Adapter provides seamless integration with Spring Data JPA, converting FilterQL conditions into native Spring Specifications with full type safety and performance optimization.

## Overview

The Spring Adapter bridges the framework-agnostic FilterQL Core with Spring Data JPA's powerful querying capabilities, enabling:

- **Native Spring Integration** - Works with existing Spring Data repositories
- **Type-Safe Queries** - Compile-time safety with runtime validation
- **Performance Optimization** - Leverages Spring's query optimization
- **Feature Preservation** - Maintains all Spring Data features (pagination, caching, etc.)
- **JPA Criteria API** - Full access to JPA's advanced querying capabilities

## Quick Setup

### Dependencies

```xml
<dependency>
    <groupId>io.github.cyfko</groupId>
    <artifactId>filterql-core</artifactId>
    <version>3.0.0</version>
</dependency>
<dependency>
    <groupId>io.github.cyfko</groupId>
    <artifactId>filterql-spring</artifactId>
    <version>3.0.0</version>
</dependency>
```

### Basic Configuration

```java
@Configuration
@EnableJpaRepositories
public class FilterQLConfig {
    
    @Bean
    public FilterContext<User, UserPropertyRef> userFilterContext() {
        return new FilterContext<>(
            User.class,
            UserPropertyRef.class,
            this::mapUserProperty
        );
    }
    
    private Object mapUserProperty(FilterDefinition<UserPropertyRef> definition) {
        return switch (definition.ref()) {
            case NAME -> "name";
            case EMAIL -> "email";
            case AGE -> "age";
            case DEPARTMENT_NAME -> "department.name"; // Nested properties
            case ADDRESS_CITY -> "address.city.name";  // Deep nesting
        };
    }
}
```
```

## Core Components

### 1. FilterContext

The main integration point that bridges FilterQL definitions with Spring Data JPA Specifications.

```java
public class FilterContext<E, P extends Enum<P> & PropertyReference> implements Context {
    private final Class<E> entityClass;
    private final Class<P> propertyRefClass;
    private final Function<FilterDefinition<P>, Object> mappingFunction;
    
    // Converts FilterDefinitions to executable Specifications
    // Handles validation and type safety
    // Supports both simple paths and complex custom logic
}
```

**Key Features:**
- Type-safe entity and property reference binding
- Flexible mapping strategies (simple paths vs. custom resolvers)
- Automatic validation and error handling
- Thread-safe operation with immutable design
- Integration with Spring's transaction management

**Construction Example:**
```java
FilterContext<User, UserPropertyRef> context = new FilterContext<>(
    User.class,                    // Target entity
    UserPropertyRef.class,         // Property reference enum
    this::mapUserProperty          // Mapping function
);
```

### 2. FilterCondition

Wraps Spring Specifications while providing FilterQL's boolean combination operations.

```java
public class FilterCondition<T> implements Condition {
    private final Specification<T> specification;
    
    // Implements FilterQL's Condition interface
    // Delegates to Spring Specification for query execution
    // Provides boolean logic operations (and, or, not)
}
```

**Usage Example:**
```java
// Create individual conditions
FilterCondition<User> nameCondition = new FilterCondition<>(nameSpec);
FilterCondition<User> ageCondition = new FilterCondition<>(ageSpec);

// Combine using FilterQL operators
Condition combined = nameCondition.and(ageCondition);
Condition alternative = nameCondition.or(ageCondition);
Condition negated = nameCondition.not();

// Convert back to Spring Specification
PredicateResolver<User> resolver = combined.toPredicateResolver(User.class);
```

### 3. PredicateResolver Integration

Seamlessly converts FilterQL predicates to JPA Criteria API predicates.

```java
// FilterQL PredicateResolver
PredicateResolver<User> resolver = (root, query, criteriaBuilder) -> {
    return criteriaBuilder.equal(root.get("name"), "John");
};

// Direct usage with Spring Specification
Specification<User> specification = (root, query, cb) -> 
    resolver.resolve(root, query, cb);

// Or via FilterCondition wrapper
FilterCondition<User> condition = new FilterCondition<>(specification);
```

## Mapping Strategies

### 1. Simple Path Mapping

Map property references directly to JPA entity paths:

```java
FilterContext<User, UserPropertyRef> context = new FilterContext<>(
    User.class,
    UserPropertyRef.class,
    definition -> switch (definition.ref()) {
        case NAME -> "name";                    // Simple property
        case EMAIL -> "email";                  // Simple property
        case DEPARTMENT_NAME -> "department.name"; // Nested property
        case ADDRESS_CITY -> "address.city.name";  // Deep nesting
    }
);
```

### 2. Custom Logic with PredicateResolverMapping

For complex business logic that can't be expressed as simple paths:

```java
case FULL_NAME_SEARCH -> new PredicateResolverMapping<User, UserPropertyRef>() {
    @Override
    public PredicateResolver<User> resolve() {
        // Access definition from closure
        String searchTerm = (String) definition.value();
        return (root, query, cb) -> {
            return cb.or(
                cb.like(cb.lower(root.get("firstName")), "%" + searchTerm.toLowerCase() + "%"),
                cb.like(cb.lower(root.get("lastName")), "%" + searchTerm.toLowerCase() + "%"),
                cb.like(cb.lower(root.get("middleName")), "%" + searchTerm.toLowerCase() + "%")
            );
        };
    }
};
```

### 3. Dynamic Mapping

Build mappings at runtime based on conditions:

```java
FilterContext<User, UserPropertyRef> context = new FilterContext<>(
    User.class,
    UserPropertyRef.class,
    definition -> {
        return switch (definition.ref()) {
            case NAME -> {
                // Different behavior based on operator
                if (definition.operator() == Op.MATCHES) {
                    yield new CaseInsensitiveSearchMapping(definition);
                } else {
                    yield "name";
                }
            }
            case STATUS -> "status";
            // ... other cases
        };
    }
);
```

## Service Layer Integration

### 1. Repository Pattern

Integrate FilterQL with Spring Data repositories for clean, testable filtering.

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    // Spring Data provides automatic Specification support
}

@Service
public class UserService {
    private final UserRepository repository;
    private final FilterContext<User, UserPropertyRef> filterContext;
    
    public UserService(UserRepository repository, FilterContext<User, UserPropertyRef> filterContext) {
        this.repository = repository;
        this.filterContext = filterContext;
    }
    
    public Page<User> findUsers(FilterRequest<UserPropertyRef> request, Pageable pageable) {
        try {
            // Parse and resolve filter
            FilterTree<UserPropertyRef> tree = FilterParser.parse(request.getFilter());
            Condition condition = tree.resolve(filterContext);
            
            // Convert to Spring Specification
            Specification<User> spec = toSpecification(condition);
            
            // Execute query with pagination
            return repository.findAll(spec, pageable);
            
        } catch (DSLSyntaxException e) {
            throw new IllegalArgumentException("Invalid filter syntax: " + e.getMessage(), e);
        } catch (FilterValidationException e) {
            throw new IllegalArgumentException("Invalid filter: " + e.getMessage(), e);
        }
    }
    
    public List<User> findUsers(String filterExpression) {
        FilterRequest<UserPropertyRef> request = FilterRequest.of(filterExpression);
        Page<User> page = findUsers(request, Pageable.unpaged());
        return page.getContent();
    }
    
    private Specification<User> toSpecification(Condition condition) {
        if (condition instanceof FilterCondition<User> filterCondition) {
            return filterCondition.getSpecification();
        }
        
        // Handle composite conditions
        PredicateResolver<User> resolver = condition.toPredicateResolver(User.class);
        return (root, query, cb) -> resolver.resolve(root, query, cb);
    }
}
```

### 2. REST Controller Integration

Create filtering endpoints with proper error handling and validation.

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    @GetMapping
    public ResponseEntity<Page<UserDto>> getUsers(
            @RequestParam(required = false) String filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {
        
        try {
            // Validate pagination parameters
            if (page < 0) page = 0;
            if (size < 1 || size > 100) size = 20;
            
            // Create pageable
            Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) 
                ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
            
            // Build filter request
            FilterRequest<UserPropertyRef> request = filter != null 
                ? FilterRequest.of(filter) 
                : FilterRequest.empty();
            
            // Execute query
            Page<User> users = userService.findUsers(request, pageable);
            Page<UserDto> dtos = users.map(this::toDto);
            
            return ResponseEntity.ok(dtos);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .header("X-Error-Message", e.getMessage())
                .build();
        }
    }
    
    @PostMapping("/search")
    public ResponseEntity<List<UserDto>> searchUsers(@RequestBody SearchRequest request) {
        try {
            // Validate request
            if (request.getFilter() == null || request.getFilter().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .header("X-Error-Message", "Filter expression is required")
                    .build();
            }
            
            // Execute search
            List<User> users = userService.findUsers(request.getFilter());
            List<UserDto> dtos = users.stream().map(this::toDto).toList();
            
            return ResponseEntity.ok(dtos);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .header("X-Error-Message", e.getMessage())
                .build();
        }
    }
    
    private UserDto toDto(User user) {
        return UserDto.builder()
            .id(user.getId())
            .name(user.getName())
            .email(user.getEmail())
            .age(user.getAge())
            .status(user.getStatus())
            .createdAt(user.getCreatedAt())
            .build();
    }
}

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SearchRequest(
    @NotBlank String filter,
    Integer limit,
    List<String> sortBy
) {}
```

### 3. Service Layer Best Practices

```java
@Service
@Transactional(readOnly = true)
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    private final UserRepository repository;
    private final FilterContext<User, UserPropertyRef> filterContext;
    private final FilterParser<UserPropertyRef> parser;
    
    public UserService(
            UserRepository repository, 
            FilterContext<User, UserPropertyRef> filterContext) {
        this.repository = repository;
        this.filterContext = filterContext;
        this.parser = new FilterParser<>(UserPropertyRef.class);
    }
    
    public Page<User> findUsers(String filterExpression, Pageable pageable) {
        logger.debug("Finding users with filter: {}", filterExpression);
        
        try {
            // Pre-validate filter expression
            if (filterExpression != null && filterExpression.length() > 1000) {
                throw new IllegalArgumentException("Filter expression too long (max 1000 characters)");
            }
            
            FilterRequest<UserPropertyRef> request = filterExpression != null 
                ? FilterRequest.of(filterExpression) 
                : FilterRequest.empty();
                
            return findUsers(request, pageable);
            
        } catch (Exception e) {
            logger.warn("Filter query failed: {}", e.getMessage());
            throw e;
        }
    }
    
    public Page<User> findUsers(FilterRequest<UserPropertyRef> request, Pageable pageable) {
        if (request.isEmpty()) {
            return repository.findAll(pageable);
        }
        
        try {
            FilterTree<UserPropertyRef> tree = parser.parse(request.getFilter());
            Condition condition = tree.resolve(filterContext);
            Specification<User> spec = toSpecification(condition);
            
            return repository.findAll(spec, pageable);
            
        } catch (DSLSyntaxException e) {
            logger.debug("DSL syntax error: {}", e.getMessage());
            throw new InvalidFilterException("Invalid filter syntax: " + e.getMessage(), e);
        } catch (FilterValidationException e) {
            logger.debug("Filter validation error: {}", e.getMessage());
            throw new InvalidFilterException("Invalid filter: " + e.getMessage(), e);
        }
    }
    
    // Cache frequently used specifications
    @Cacheable(value = "userSpecs", key = "#filterExpression")
    public Specification<User> getSpecification(String filterExpression) {
        FilterRequest<UserPropertyRef> request = FilterRequest.of(filterExpression);
        FilterTree<UserPropertyRef> tree = parser.parse(request.getFilter());
        Condition condition = tree.resolve(filterContext);
        return toSpecification(condition);
    }
    
    private Specification<User> toSpecification(Condition condition) {
        if (condition instanceof FilterCondition<User> filterCondition) {
            return filterCondition.getSpecification();
        }
        
        PredicateResolver<User> resolver = condition.toPredicateResolver(User.class);
        return (root, query, cb) -> resolver.resolve(root, query, cb);
    }
}

// Custom exception for better error handling
public class InvalidFilterException extends RuntimeException {
    public InvalidFilterException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

## Advanced Examples

### 1. Complex Entity Relationships

Handle deep entity relationships with optimized queries.

```java
// Multi-level entity relationships
@Entity
public class Order {
    @Id private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;
    
    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    private List<OrderItem> items;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipping_address_id")
    private Address shippingAddress;
}

@Entity
public class Customer {
    @Id private Long id;
    private String firstName;
    private String lastName;
    private String email;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "billing_address_id")
    private Address billingAddress;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;
}

// Property reference enum for complex relationships
public enum OrderPropertyRef implements PropertyReference {
    ORDER_ID,
    ORDER_DATE,
    ORDER_STATUS,
    CUSTOMER_FIRST_NAME,
    CUSTOMER_LAST_NAME,
    CUSTOMER_EMAIL,
    CUSTOMER_COMPANY_NAME,
    SHIPPING_CITY,
    SHIPPING_COUNTRY,
    ITEM_PRODUCT_NAME,
    ITEM_QUANTITY,
    TOTAL_AMOUNT
}

// FilterContext with optimized join handling
@Configuration
public class OrderFilterConfig {
    
    @Bean
    public FilterContext<Order, OrderPropertyRef> orderFilterContext() {
        return new FilterContext<>(
            Order.class,
            OrderPropertyRef.class,
            definition -> switch (definition.ref()) {
                case ORDER_ID -> "id";
                case ORDER_DATE -> "orderDate";
                case ORDER_STATUS -> "status";
                
                // Customer properties with join optimization
                case CUSTOMER_FIRST_NAME -> "customer.firstName";
                case CUSTOMER_LAST_NAME -> "customer.lastName";
                case CUSTOMER_EMAIL -> "customer.email";
                case CUSTOMER_COMPANY_NAME -> "customer.company.name";
                
                // Address properties
                case SHIPPING_CITY -> "shippingAddress.city";
                case SHIPPING_COUNTRY -> "shippingAddress.country";
                
                // Complex aggregation for order items
                case ITEM_PRODUCT_NAME -> new PredicateResolverMapping<Order, OrderPropertyRef>() {
                    @Override
                    public PredicateResolver<Order> resolve() {
                        String productName = (String) definition.value();
                        return (root, query, cb) -> {
                            // Create subquery for items
                            Subquery<Long> subquery = query.subquery(Long.class);
                            Root<OrderItem> itemRoot = subquery.from(OrderItem.class);
                            
                            subquery.select(itemRoot.get("order").get("id"))
                                   .where(cb.like(cb.lower(itemRoot.get("product").get("name")), 
                                                 "%" + productName.toLowerCase() + "%"));
                            
                            return cb.in(root.get("id")).value(subquery);
                        };
                    }
                };
                
                case TOTAL_AMOUNT -> new PredicateResolverMapping<Order, OrderPropertyRef>() {
                    @Override
                    public PredicateResolver<Order> resolve() {
                        BigDecimal amount = (BigDecimal) definition.value();
                        return (root, query, cb) -> {
                            // Subquery to calculate total
                            Subquery<BigDecimal> totalSubquery = query.subquery(BigDecimal.class);
                            Root<OrderItem> itemRoot = totalSubquery.from(OrderItem.class);
                            
                            totalSubquery.select(cb.sum(
                                cb.prod(itemRoot.get("quantity"), itemRoot.get("unitPrice"))
                            )).where(cb.equal(itemRoot.get("order"), root));
                            
                            return switch (definition.operator()) {
                                case EQ -> cb.equal(totalSubquery, amount);
                                case GT -> cb.greaterThan(totalSubquery, amount);
                                case LT -> cb.lessThan(totalSubquery, amount);
                                case GTE -> cb.greaterThanOrEqualTo(totalSubquery, amount);
                                case LTE -> cb.lessThanOrEqualTo(totalSubquery, amount);
                                default -> throw new FilterValidationException(
                                    "Unsupported operator for TOTAL_AMOUNT: " + definition.operator()
                                );
                            };
                        };
                    }
                };
            }
        );
    }
}
```

### 2. Dynamic Multi-Entity Search

Create a unified search service across multiple entity types.

```java
@Service
public class UnifiedSearchService {
    
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    
    private final FilterContext<User, UserPropertyRef> userContext;
    private final FilterContext<Order, OrderPropertyRef> orderContext;
    private final FilterContext<Product, ProductPropertyRef> productContext;
    
    public UnifiedSearchService(
            UserRepository userRepository,
            OrderRepository orderRepository,
            ProductRepository productRepository,
            FilterContext<User, UserPropertyRef> userContext,
            FilterContext<Order, OrderPropertyRef> orderContext,
            FilterContext<Product, ProductPropertyRef> productContext) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userContext = userContext;
        this.orderContext = orderContext;
        this.productContext = productContext;
    }
    
    @Data
    public static class UnifiedSearchResult {
        private List<User> users = new ArrayList<>();
        private List<Order> orders = new ArrayList<>();
        private List<Product> products = new ArrayList<>();
        private long totalResults;
    }
    
    public UnifiedSearchResult search(String query, List<String> entityTypes, Pageable pageable) {
        UnifiedSearchResult result = new UnifiedSearchResult();
        
        if (entityTypes.contains("users")) {
            result.users = searchUsers(query, pageable).getContent();
        }
        
        if (entityTypes.contains("orders")) {
            result.orders = searchOrders(query, pageable).getContent();
        }
        
        if (entityTypes.contains("products")) {
            result.products = searchProducts(query, pageable).getContent();
        }
        
        result.totalResults = result.users.size() + result.orders.size() + result.products.size();
        return result;
    }
    
    private Page<User> searchUsers(String query, Pageable pageable) {
        try {
            String filterExpression = String.format(
                "name contains '%s' or email contains '%s'", 
                query, query
            );
            FilterRequest<UserPropertyRef> request = FilterRequest.of(filterExpression);
            return findUsers(request, pageable);
        } catch (Exception e) {
            return Page.empty(pageable);
        }
    }
    
    private Page<Order> searchOrders(String query, Pageable pageable) {
        try {
            // Search by customer name or order ID
            String filterExpression = String.format(
                "customerFirstName contains '%s' or customerLastName contains '%s'", 
                query, query
            );
            FilterRequest<OrderPropertyRef> request = FilterRequest.of(filterExpression);
            return findOrders(request, pageable);
        } catch (Exception e) {
            return Page.empty(pageable);
        }
    }
    
    private Page<Product> searchProducts(String query, Pageable pageable) {
        try {
            String filterExpression = String.format(
                "name contains '%s' or description contains '%s'", 
                query, query
            );
            FilterRequest<ProductPropertyRef> request = FilterRequest.of(filterExpression);
            return findProducts(request, pageable);
        } catch (Exception e) {
            return Page.empty(pageable);
        }
    }
}
```

### 3. Advanced REST Controller with Faceted Search

```java
@RestController
@RequestMapping("/api/search")
public class AdvancedSearchController {
    
    private final UnifiedSearchService searchService;
    private final FacetService facetService;
    
    public AdvancedSearchController(
            UnifiedSearchService searchService,
            FacetService facetService) {
        this.searchService = searchService;
        this.facetService = facetService;
    }
    
    @PostMapping("/advanced")
    public ResponseEntity<AdvancedSearchResponse> advancedSearch(
            @RequestBody @Valid AdvancedSearchRequest request) {
        
        try {
            // Validate filter expression
            validateFilterExpression(request.getFilter());
            
            // Execute search with filters
            Pageable pageable = PageRequest.of(
                request.getPage(), 
                Math.min(request.getSize(), 100),  // Max 100 results
                parseSort(request.getSort())
            );
            
            UnifiedSearchService.UnifiedSearchResult results = 
                searchService.search(request.getQuery(), request.getEntityTypes(), pageable);
            
            // Generate facets for filtering
            Map<String, List<FacetValue>> facets = Collections.emptyMap();
            if (request.isIncludeFacets()) {
                facets = facetService.generateFacets(request, results);
            }
            
            // Build response
            AdvancedSearchResponse response = AdvancedSearchResponse.builder()
                .results(results)
                .facets(facets)
                .totalResults(results.getTotalResults())
                .executionTimeMs(System.currentTimeMillis() - startTime)
                .build();
                
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(AdvancedSearchResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Search failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(AdvancedSearchResponse.error("Search temporarily unavailable"));
        }
    }
    
    @GetMapping("/suggest")
    public ResponseEntity<List<SearchSuggestion>> getSuggestions(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit) {
        
        if (query.length() < 2) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        
        List<SearchSuggestion> suggestions = searchService.getSuggestions(query, limit);
        return ResponseEntity.ok(suggestions);
    }
    
    private void validateFilterExpression(String filter) {
        if (filter != null && filter.length() > 2000) {
            throw new IllegalArgumentException("Filter expression too long (max 2000 characters)");
        }
        
        // Additional validation rules
        if (filter != null && filter.contains("__dangerous__")) {
            throw new IllegalArgumentException("Invalid filter expression");
        }
    }
    
    private Sort parseSort(List<String> sortFields) {
        if (sortFields == null || sortFields.isEmpty()) {
            return Sort.by("id").ascending();
        }
        
        List<Sort.Order> orders = sortFields.stream()
            .map(field -> {
                if (field.startsWith("-")) {
                    return Sort.Order.desc(field.substring(1));
                } else {
                    return Sort.Order.asc(field);
                }
            })
            .toList();
            
        return Sort.by(orders);
    }
}

@Data
@Builder
public class AdvancedSearchRequest {
    @NotBlank
    private String query;
    
    private String filter;
    
    @Valid
    private List<String> entityTypes = List.of("users", "orders", "products");
    
    @Min(0)
    private int page = 0;
    
    @Min(1) @Max(100)
    private int size = 20;
    
    private List<String> sort = new ArrayList<>();
    
    private boolean includeFacets = false;
}

@Data
@Builder
public class AdvancedSearchResponse {
    private UnifiedSearchService.UnifiedSearchResult results;
    private Map<String, List<FacetValue>> facets;
    private long totalResults;
    private long executionTimeMs;
    private String error;
    
    public static AdvancedSearchResponse error(String message) {
        return AdvancedSearchResponse.builder()
            .error(message)
            .totalResults(0)
            .build();
    }
}
```
    public ResponseEntity<Page<User>> searchUsers(
            @Valid @RequestBody FilterRequest<UserPropertyRef> filterRequest,
            @PageableDefault(size = 20) Pageable pageable) {
        
        try {
            Page<User> users = userService.findUsers(filterRequest, pageable);
            return ResponseEntity.ok(users);
        } catch (DSLSyntaxException e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Invalid filter syntax: " + e.getMessage()));
        } catch (FilterValidationException e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Invalid filter: " + e.getMessage()));
        }
    }
}
```

### Complex Business Logic

```java
public enum OrderPropertyRef implements PropertyReference {
    CUSTOMER_NAME(String.class, Set.of(Op.EQ, Op.MATCHES)),
    TOTAL_AMOUNT(BigDecimal.class, Set.of(Op.EQ, Op.GT, Op.GTE, Op.LT, Op.LTE, Op.RANGE)),
    STATUS(OrderStatus.class, Set.of(Op.EQ, Op.NE, Op.IN)),
    URGENT_ORDERS(Boolean.class, Set.of(Op.EQ)),           // Custom business logic
    HIGH_VALUE_CUSTOMER(Boolean.class, Set.of(Op.EQ));    // Custom business logic

    // Implementation...
}

// Mapping function with business logic
FilterContext<Order, OrderPropertyRef> orderContext = new FilterContext<>(
    Order.class,
    OrderPropertyRef.class,
    definition -> switch (definition.ref()) {
        case CUSTOMER_NAME -> "customer.name";
        case TOTAL_AMOUNT -> "totalAmount";
        case STATUS -> "status";
        
        case URGENT_ORDERS -> new PredicateResolverMapping<Order, OrderPropertyRef>() {
            @Override
            public PredicateResolver<Order> resolve(FilterDefinition<OrderPropertyRef> def) {
                return (root, query, cb) -> {
                    boolean showUrgent = (Boolean) def.value();
                    if (showUrgent) {
                        // Urgent = high priority OR due within 24 hours
                        return cb.or(
                            cb.equal(root.get("priority"), Priority.HIGH),
                            cb.lessThan(root.get("dueDate"), LocalDateTime.now().plusHours(24))
                        );
                    } else {
                        // Non-urgent orders
                        return cb.and(
                            cb.notEqual(root.get("priority"), Priority.HIGH),
                            cb.greaterThanOrEqualTo(root.get("dueDate"), LocalDateTime.now().plusHours(24))
                        );
                    }
                };
            }
        };
        
        case HIGH_VALUE_CUSTOMER -> new PredicateResolverMapping<Order, OrderPropertyRef>() {
            @Override
            public PredicateResolver<Order> resolve(FilterDefinition<OrderPropertyRef> def) {
                return (root, query, cb) -> {
                    boolean highValue = (Boolean) def.value();
                    if (highValue) {
                        // Subquery to find customers with total orders > $10,000
                        Subquery<BigDecimal> subquery = query.subquery(BigDecimal.class);
                        Root<Order> subRoot = subquery.from(Order.class);
                        subquery.select(cb.sum(subRoot.get("totalAmount")))
                               .where(cb.equal(subRoot.get("customer"), root.get("customer")));
                               
                        return cb.greaterThan(subquery, new BigDecimal("10000"));
                    } else {
                        // Regular customers
                        Subquery<BigDecimal> subquery = query.subquery(BigDecimal.class);
                        Root<Order> subRoot = subquery.from(Order.class);
                        subquery.select(cb.sum(subRoot.get("totalAmount")))
                               .where(cb.equal(subRoot.get("customer"), root.get("customer")));
                               
                        return cb.lessThanOrEqualTo(subquery, new BigDecimal("10000"));
                    }
                };
            }
        };
    }
);
```

## Performance Optimization

### Query Optimization

```java
// Use JOIN FETCH for lazy associations
case DEPARTMENT_EMPLOYEES -> new PredicateResolverMapping<User, UserPropertyRef>() {
    @Override
    public PredicateResolver<User> resolve(FilterDefinition<UserPropertyRef> definition) {
        return (root, query, cb) -> {
            // Add fetch join to avoid N+1 queries
            if (query.getResultType() == User.class) {
                root.fetch("department", JoinType.LEFT);
            }
            
            return cb.equal(root.get("department").get("name"), definition.value());
        };
    }
};
```

### Caching Integration

```java
@Service
@Transactional(readOnly = true)
public class CachedUserService {
    
    @Cacheable(value = "userSearchResults", key = "#filterRequest.hashCode() + '_' + #pageable.hashCode()")
    public Page<User> findUsers(FilterRequest<UserPropertyRef> filterRequest, Pageable pageable) {
        // FilterQL query execution
        // Results are automatically cached by Spring
    }
}
```

## Error Handling & Validation

### Custom Validation

```java
@Component
public class FilterRequestValidator {
    
    public void validate(FilterRequest<UserPropertyRef> request) {
        // Business rule: can't filter by sensitive data for non-admin users
        if (!SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            
            request.filters().values().forEach(definition -> {
                if (definition.ref() == UserPropertyRef.SALARY) {
                    throw new FilterValidationException("Access denied: Cannot filter by salary");
                }
            });
        }
        
        // Business rule: Date ranges must be reasonable
        request.filters().values().forEach(definition -> {
            if (definition.ref() == UserPropertyRef.CREATED_AT && 
                definition.operator() == Op.RANGE) {
                
                List<LocalDateTime> dates = (List<LocalDateTime>) definition.value();
                Duration range = Duration.between(dates.get(0), dates.get(1));
                
                if (range.toDays() > 365) {
                    throw new FilterValidationException("Date range cannot exceed 1 year");
                }
            }
        });
    }
}
```

### Exception Handling

```java
@ControllerAdvice
public class FilterExceptionHandler {
    
    @ExceptionHandler(DSLSyntaxException.class)
    public ResponseEntity<ErrorResponse> handleDSLSyntax(DSLSyntaxException e) {
        return ResponseEntity.badRequest()
            .body(new ErrorResponse("INVALID_FILTER_SYNTAX", e.getMessage()));
    }
    
    @ExceptionHandler(FilterValidationException.class)
    public ResponseEntity<ErrorResponse> handleFilterValidation(FilterValidationException e) {
        return ResponseEntity.badRequest()
            .body(new ErrorResponse("INVALID_FILTER", e.getMessage()));
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        if (e.getMessage().contains("not found")) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("FILTER_NOT_FOUND", e.getMessage()));
        }
        return ResponseEntity.badRequest()
            .body(new ErrorResponse("INVALID_FILTER_REQUEST", e.getMessage()));
    }
}
```

## Testing

### Unit Testing FilterContext

```java
@ExtendWith(MockitoExtension.class)
class FilterContextTest {
    
    private FilterContext<User, UserPropertyRef> context;
    
    @BeforeEach
    void setUp() {
        context = new FilterContext<>(
            User.class,
            UserPropertyRef.class,
            definition -> switch (definition.ref()) {
                case NAME -> "name";
                case AGE -> "age";
            }
        );
    }
    
    @Test
    void shouldCreateConditionForSimpleFilter() {
        FilterDefinition<UserPropertyRef> definition = 
            new FilterDefinition<>(UserPropertyRef.NAME, Op.EQ, "John");
            
        Condition condition = context.addCondition("nameFilter", definition);
        
        assertThat(condition).isNotNull();
        assertThat(context.getCondition("nameFilter")).isSameAs(condition);
    }
    
    @Test
    void shouldThrowExceptionForInvalidFilter() {
        FilterDefinition<UserPropertyRef> definition = 
            new FilterDefinition<>(UserPropertyRef.AGE, Op.MATCHES, "invalid"); // MATCHES not supported for Integer
            
        assertThatThrownBy(() -> context.addCondition("ageFilter", definition))
            .isInstanceOf(FilterValidationException.class)
            .hasMessageContaining("not supported");
    }
}
```

### Integration Testing

```java
@SpringBootTest
@Transactional
class UserFilterIntegrationTest {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private FilterContext<User, UserPropertyRef> filterContext;
    
    @Test
    void shouldFilterUsersByNameAndAge() {
        // Given
        userRepository.saveAll(List.of(
            new User("John Doe", "john@example.com", 25),
            new User("Jane Smith", "jane@example.com", 30),
            new User("John Wilson", "wilson@example.com", 35)
        ));
        
        FilterRequest<UserPropertyRef> request = FilterRequest.<UserPropertyRef>builder()
            .filter("nameFilter", new FilterDefinition<>(UserPropertyRef.NAME, Op.MATCHES, "John%"))
            .filter("ageFilter", new FilterDefinition<>(UserPropertyRef.AGE, Op.GT, 30))
            .combineWith("nameFilter & ageFilter")
            .build();
        
        // When
        FilterResolver resolver = FilterResolver.of(filterContext);
        PredicateResolver<User> predicateResolver = resolver.resolve(User.class, request);
        
        Specification<User> spec = (root, query, cb) -> 
            predicateResolver.resolve(root, query, cb);
            
        List<User> results = userRepository.findAll(spec);
        
        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("John Wilson");
    }
}
```

## Best Practices

### 1. Property Reference Design

```java
public enum UserPropertyRef implements PropertyReference {
    // Group logically related properties
    PERSONAL_NAME(String.class, OperatorUtils.FOR_TEXT),
    PERSONAL_EMAIL(String.class, OperatorUtils.FOR_TEXT),
    PERSONAL_AGE(Integer.class, OperatorUtils.FOR_NUMBER),
    
    // Use business-meaningful names
    ACCOUNT_STATUS(UserStatus.class, Set.of(Op.EQ, Op.NE, Op.IN)),
    ACCOUNT_CREATED_DATE(LocalDateTime.class, OperatorUtils.FOR_DATE),
    
    // Custom business logic properties
    RECENT_LOGIN(Boolean.class, Set.of(Op.EQ)),
    HIGH_ACTIVITY_USER(Boolean.class, Set.of(Op.EQ));
}
```

### 2. Mapping Function Organization

```java
// Extract complex mappings to separate methods
private static Object mapUserProperty(FilterDefinition<UserPropertyRef> definition) {
    return switch (definition.ref()) {
        case PERSONAL_NAME -> "name";
        case PERSONAL_EMAIL -> "email";
        case RECENT_LOGIN -> createRecentLoginMapping();
        case HIGH_ACTIVITY_USER -> createHighActivityMapping();
        default -> throw new UnsupportedOperationException("Unsupported property: " + definition.ref());
    };
}

private static PredicateResolverMapping<User, UserPropertyRef> createRecentLoginMapping() {
    return definition -> (root, query, cb) -> {
        boolean recentLogin = (Boolean) definition.value();
        LocalDateTime threshold = LocalDateTime.now().minusDays(7);
        
        if (recentLogin) {
            return cb.greaterThan(root.get("lastLoginDate"), threshold);
        } else {
            return cb.or(
                cb.isNull(root.get("lastLoginDate")),
                cb.lessThanOrEqualTo(root.get("lastLoginDate"), threshold)
            );
        }
    };
}
```

### 3. Service Layer Design

```java
@Service
@Transactional(readOnly = true)
public class UserSearchService {
    
    private final UserRepository userRepository;
    private final FilterContext<User, UserPropertyRef> filterContext;
    private final FilterRequestValidator validator;
    
    // Constructor injection...
    
    public Page<User> searchUsers(FilterRequest<UserPropertyRef> request, Pageable pageable) {
        // 1. Validate request
        validator.validate(request);
        
        // 2. Build specification
        Specification<User> specification = buildSpecification(request);
        
        // 3. Execute query with pagination
        return userRepository.findAll(specification, pageable);
    }
    
    public List<User> searchUsers(FilterRequest<UserPropertyRef> request) {
        validator.validate(request);
        Specification<User> specification = buildSpecification(request);
        return userRepository.findAll(specification);
    }
    
    private Specification<User> buildSpecification(FilterRequest<UserPropertyRef> request) {
        FilterResolver resolver = FilterResolver.of(filterContext);
        PredicateResolver<User> predicateResolver = resolver.resolve(User.class, request);
        
        return (root, query, cb) -> predicateResolver.resolve(root, query, cb);
    }
}
```

## Next Steps

- **[See Spring Examples](examples/repository-integration.md)**
- **[Performance Tuning Guide](performance-tuning.md)**
- **[Security Considerations](security.md)**
- **[Migration from Specifications](migration-from-specifications.md)**