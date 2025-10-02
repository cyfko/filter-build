# FilterQL Spring Adapter

The Spring Adapter provides seamless integration with Spring Data JPA, converting FilterQL conditions into native Spring Specifications.

## Overview

The Spring Adapter bridges FilterQL's framework-agnostic core with Spring Data JPA's powerful query capabilities, enabling:

- **Native Spring Integration** - Works with existing Spring Data repositories
- **Type-Safe Queries** - Compile-time safety with runtime validation
- **Performance Optimization** - Leverages Spring's query optimization
- **Feature Preservation** - Maintains all Spring Data features (pagination, caching, etc.)

## Quick Setup

### Dependencies

```xml
<dependency>
    <groupId>io.github.cyfko</groupId>
    <artifactId>filterql-core</artifactId>
    <version>2.0.0</version>
</dependency>
<dependency>
    <groupId>io.github.cyfko</groupId>
    <artifactId>filterql-spring</artifactId>
    <version>2.0.0</version>
</dependency>
```

### Basic Configuration

```java
@Configuration
public class FilterConfig {
    
    @Bean
    public FilterContext<User, UserPropertyRef> userFilterContext() {
        return new FilterContext<>(
            User.class,
            UserPropertyRef.class,
            definition -> switch (definition.ref()) {
                case NAME -> "name";
                case EMAIL -> "email";
                case AGE -> "age";
                case DEPARTMENT_NAME -> "department.name"; // Nested properties
            }
        );
    }
}
```

## Core Components

### FilterContext

The main integration point that converts filter definitions to Spring Specifications.

```java
public class FilterContext<E, P extends Enum<P> & PropertyReference> implements Context {
    // Converts FilterDefinitions to Spring Specifications
    // Handles both simple path mappings and custom logic
}
```

**Key Features:**
- Type-safe entity and property reference binding
- Flexible mapping strategies (paths vs. custom logic)
- Automatic validation and error handling
- Thread-safe operation

### FilterCondition

Wraps Spring Specifications while providing FilterQL's boolean combination operations.

```java
FilterCondition<User> nameCondition = new FilterCondition<>(nameSpec);
FilterCondition<User> ageCondition = new FilterCondition<>(ageSpec);

// Combine using FilterQL operators
Condition combined = nameCondition.and(ageCondition);
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

## Advanced Examples

### Repository Integration

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    // Spring Data provides Specification support automatically
}

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private FilterContext<User, UserPropertyRef> filterContext;
    
    public Page<User> findUsers(FilterRequest<UserPropertyRef> filterRequest, Pageable pageable) {
        FilterResolver resolver = FilterResolver.of(filterContext);
        PredicateResolver<User> predicateResolver = resolver.resolve(User.class, filterRequest);
        
        // Convert to Spring Specification
        Specification<User> specification = (root, query, cb) -> 
            predicateResolver.resolve(root, query, cb);
            
        return userRepository.findAll(specification, pageable);
    }
}
```

### Controller with Validation

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/search")
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