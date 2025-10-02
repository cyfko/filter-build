# FilterQL Best Practices

This guide provides comprehensive best practices for implementing robust, maintainable, and efficient FilterQL solutions.

## Table of Contents

1. [Property Reference Design](#property-reference-design)
2. [Mapping Strategy](#mapping-strategy)
3. [Performance Optimization](#performance-optimization)
4. [Error Handling](#error-handling)
5. [Security Considerations](#security-considerations)
6. [Testing Strategy](#testing-strategy)
7. [Code Organization](#code-organization)
8. [Common Pitfalls](#common-pitfalls)

## Property Reference Design

### 1. Naming Conventions

```java
public enum UserPropertyRef implements PropertyReference {
    // ✅ Good: Clear, business-meaningful names
    PERSONAL_FIRST_NAME(String.class, OperatorUtils.FOR_TEXT),
    PERSONAL_LAST_NAME(String.class, OperatorUtils.FOR_TEXT),
    CONTACT_EMAIL(String.class, OperatorUtils.FOR_TEXT),
    ACCOUNT_STATUS(UserStatus.class, Set.of(Op.EQ, Op.NE, Op.IN)),
    
    // ❌ Avoid: Technical field names exposed to business users
    // USER_TBL_FNAME, USR_EMAIL, STATUS_CD
    
    // ✅ Good: Logical grouping with prefixes
    ADDRESS_STREET(String.class, OperatorUtils.FOR_TEXT),
    ADDRESS_CITY(String.class, OperatorUtils.FOR_TEXT),
    ADDRESS_ZIP_CODE(String.class, OperatorUtils.FOR_TEXT),
    
    // ✅ Good: Business logic properties
    RECENT_ACTIVITY(Boolean.class, Set.of(Op.EQ)),
    HIGH_VALUE_CUSTOMER(Boolean.class, Set.of(Op.EQ)),
    OVERDUE_PAYMENT(Boolean.class, Set.of(Op.EQ));
}
```

### 2. Operator Restrictions

```java
public enum ProductPropertyRef implements PropertyReference {
    // ✅ Good: Thoughtful operator restrictions
    NAME(String.class, Set.of(Op.EQ, Op.MATCHES)),           // Text search only
    CATEGORY(ProductCategory.class, Set.of(Op.EQ, Op.IN)),   // Exact matches only
    PRICE(BigDecimal.class, OperatorUtils.FOR_NUMBER),       // Full numeric operations
    CREATION_DATE(LocalDateTime.class, OperatorUtils.FOR_DATE), // Date range operations
    
    // ✅ Good: Boolean properties with EQ only
    IS_ACTIVE(Boolean.class, Set.of(Op.EQ)),
    IS_FEATURED(Boolean.class, Set.of(Op.EQ)),
    
    // ❌ Avoid: Overly permissive operators
    // STATUS(String.class, OperatorUtils.ALL_OPERATORS) // Too broad
    
    // ✅ Good: Custom business logic with appropriate operators
    LOW_STOCK_ALERT(Boolean.class, Set.of(Op.EQ)) {
        @Override
        public Set<Op> supportedOperators() {
            return Set.of(Op.EQ);
        }
        
        @Override
        public boolean validate(Op operator, Object value) {
            return value instanceof Boolean && operator == Op.EQ;
        }
    };
}
```

### 3. Type Safety

```java
public enum OrderPropertyRef implements PropertyReference {
    private final Class<?> type;
    private final Set<Op> operators;
    
    // ✅ Good: Strong typing with validation
    TOTAL_AMOUNT(BigDecimal.class, OperatorUtils.FOR_NUMBER) {
        @Override
        public boolean validate(Op operator, Object value) {
            if (!super.validate(operator, value)) return false;
            
            // Additional business validation
            if (value instanceof BigDecimal amount) {
                return amount.compareTo(BigDecimal.ZERO) >= 0; // No negative amounts
            }
            return false;
        }
    },
    
    ORDER_DATE(LocalDateTime.class, OperatorUtils.FOR_DATE) {
        @Override
        public boolean validate(Op operator, Object value) {
            if (!super.validate(operator, value)) return false;
            
            if (operator == Op.RANGE && value instanceof List<?> range) {
                if (range.size() != 2) return false;
                
                LocalDateTime start = (LocalDateTime) range.get(0);
                LocalDateTime end = (LocalDateTime) range.get(1);
                
                // Business rule: Date range cannot exceed 1 year
                return Duration.between(start, end).toDays() <= 365;
            }
            return true;
        }
    };
}
```

## Mapping Strategy

### 1. Simple Path Mapping (Preferred)

```java
// ✅ Good: Simple, maintainable mappings
FilterContext<User, UserPropertyRef> context = new FilterContext<>(
    User.class,
    UserPropertyRef.class,
    definition -> switch (definition.ref()) {
        case PERSONAL_NAME -> "name";
        case CONTACT_EMAIL -> "email";
        case DEPARTMENT_NAME -> "department.name";        // Simple join
        case ADDRESS_CITY -> "address.city.name";        // Nested navigation
    }
);
```

### 2. Custom Logic Organization

```java
// ✅ Good: Extract complex logic to separate methods
public class UserFilterMappings {
    
    public static Object mapUserProperty(FilterDefinition<UserPropertyRef> definition) {
        return switch (definition.ref()) {
            case PERSONAL_NAME -> "name";
            case CONTACT_EMAIL -> "email";
            
            // Complex business logic
            case RECENT_ACTIVITY -> createRecentActivityMapping();
            case HIGH_VALUE_CUSTOMER -> createHighValueMapping();
            case FULL_NAME_SEARCH -> createFullNameSearchMapping();
            
            default -> throw new UnsupportedOperationException(
                "Mapping not implemented for: " + definition.ref()
            );
        };
    }
    
    private static PredicateResolverMapping<User, UserPropertyRef> createRecentActivityMapping() {
        return definition -> (root, query, cb) -> {
            boolean hasRecentActivity = (Boolean) definition.value();
            LocalDateTime threshold = LocalDateTime.now().minusDays(30);
            
            if (hasRecentActivity) {
                return cb.greaterThan(root.get("lastActivityDate"), threshold);
            } else {
                return cb.or(
                    cb.isNull(root.get("lastActivityDate")),
                    cb.lessThanOrEqualTo(root.get("lastActivityDate"), threshold)
                );
            }
        };
    }
    
    // ✅ Good: Reusable mapping logic
    private static PredicateResolverMapping<User, UserPropertyRef> createFullNameSearchMapping() {
        return definition -> (root, query, cb) -> {
            String searchTerm = ((String) definition.value()).toLowerCase();
            return cb.or(
                cb.like(cb.lower(root.get("firstName")), "%" + searchTerm + "%"),
                cb.like(cb.lower(root.get("lastName")), "%" + searchTerm + "%"),
                cb.like(cb.lower(
                    cb.concat(
                        cb.concat(root.get("firstName"), " "),
                        root.get("lastName")
                    )
                ), "%" + searchTerm + "%")
            );
        };
    }
}
```

### 3. Context Configuration

```java
// ✅ Good: Centralized configuration with validation
@Configuration
public class FilterConfig {
    
    @Bean
    @Primary
    public FilterContext<User, UserPropertyRef> userFilterContext() {
        FilterContext<User, UserPropertyRef> context = new FilterContext<>(
            User.class,
            UserPropertyRef.class,
            UserFilterMappings::mapUserProperty
        );
        
        // Add validation
        context.setValidationEnabled(true);
        return context;
    }
    
    @Bean
    public FilterContext<Order, OrderPropertyRef> orderFilterContext() {
        return new FilterContext<>(
            Order.class,
            OrderPropertyRef.class,
            OrderFilterMappings::mapOrderProperty
        );
    }
    
    // ✅ Good: Environment-specific configurations
    @Bean
    @Profile("development")
    public FilterValidator developmentValidator() {
        return new FilterValidator() {
            @Override
            public void validate(FilterRequest<?> request) {
                // Relaxed validation for development
                super.validate(request);
            }
        };
    }
    
    @Bean
    @Profile("production")
    public FilterValidator productionValidator() {
        return new FilterValidator() {
            @Override
            public void validate(FilterRequest<?> request) {
                // Strict validation for production
                super.validate(request);
                
                // Additional security checks
                SecurityValidation.checkFilterAccess(request);
            }
        };
    }
}
```

## Performance Optimization

### 1. Query Optimization

```java
// ✅ Good: Use JOINs instead of subqueries when possible
case DEPARTMENT_USER_COUNT -> new PredicateResolverMapping<User, UserPropertyRef>() {
    @Override
    public PredicateResolver<User> resolve(FilterDefinition<UserPropertyRef> definition) {
        return (root, query, cb) -> {
            // JOIN is more efficient than subquery
            Join<User, Department> deptJoin = root.join("department");
            Join<Department, User> userJoin = deptJoin.join("users");
            
            query.groupBy(root.get("id"));
            query.having(cb.greaterThan(cb.count(userJoin), (Integer) definition.value()));
            
            return cb.conjunction(); // Always true, filtering is done by HAVING
        };
    }
};

// ✅ Good: Add fetch joins to avoid N+1 queries
case DEPARTMENT_WITH_MANAGER -> new PredicateResolverMapping<User, UserPropertyRef>() {
    @Override
    public PredicateResolver<User> resolve(FilterDefinition<UserPropertyRef> definition) {
        return (root, query, cb) -> {
            // Fetch related data to avoid lazy loading
            if (query.getResultType() == User.class) {
                root.fetch("department", JoinType.LEFT);
                root.fetch("department").fetch("manager", JoinType.LEFT);
            }
            
            return cb.isNotNull(root.get("department").get("manager"));
        };
    }
};
```

### 2. Caching Strategy

```java
// ✅ Good: Cache expensive operations
@Service
@Transactional(readOnly = true)
public class CachedUserService {
    
    private final UserRepository userRepository;
    private final FilterContext<User, UserPropertyRef> filterContext;
    
    // Cache filter results based on request hash
    @Cacheable(
        value = "userSearchResults",
        key = "#filterRequest.hashCode() + '_' + #pageable.hashCode()",
        condition = "#filterRequest.filters().size() <= 5" // Only cache simple queries
    )
    public Page<User> findUsers(FilterRequest<UserPropertyRef> filterRequest, Pageable pageable) {
        return executeFilter(filterRequest, pageable);
    }
    
    // Cache filter counts separately
    @Cacheable(
        value = "userSearchCounts",
        key = "#filterRequest.hashCode()",
        condition = "#filterRequest.filters().size() <= 3"
    )
    public long countUsers(FilterRequest<UserPropertyRef> filterRequest) {
        FilterResolver resolver = FilterResolver.of(filterContext);
        PredicateResolver<User> predicateResolver = resolver.resolve(User.class, filterRequest);
        
        Specification<User> spec = (root, query, cb) -> 
            predicateResolver.resolve(root, query, cb);
            
        return userRepository.count(spec);
    }
}
```

### 3. Database Indexing

```sql
-- ✅ Good: Create indexes for commonly filtered properties
CREATE INDEX idx_user_name ON users(name);
CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_user_status ON users(status);
CREATE INDEX idx_user_created_date ON users(created_date);

-- ✅ Good: Composite indexes for common filter combinations
CREATE INDEX idx_user_status_created ON users(status, created_date);
CREATE INDEX idx_user_dept_name ON users(department_id, name);

-- ✅ Good: Partial indexes for boolean filters
CREATE INDEX idx_user_active ON users(id) WHERE is_active = true;
CREATE INDEX idx_user_featured ON users(id) WHERE is_featured = true;
```

## Error Handling

### 1. Validation Strategy

```java
// ✅ Good: Comprehensive validation with specific errors
@Component
public class FilterRequestValidator {
    
    private final SecurityContext securityContext;
    
    public void validate(FilterRequest<UserPropertyRef> request) {
        validateSecurity(request);
        validateBusinessRules(request);
        validatePerformance(request);
    }
    
    private void validateSecurity(FilterRequest<UserPropertyRef> request) {
        Authentication auth = securityContext.getAuthentication();
        Set<String> userRoles = auth.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toSet());
        
        request.filters().values().forEach(definition -> {
            switch (definition.ref()) {
                case SALARY_AMOUNT -> {
                    if (!userRoles.contains("ROLE_HR") && !userRoles.contains("ROLE_ADMIN")) {
                        throw new FilterValidationException(
                            "Access denied: Insufficient privileges to filter by salary"
                        );
                    }
                }
                case PERSONAL_SSN -> {
                    if (!userRoles.contains("ROLE_ADMIN")) {
                        throw new FilterValidationException(
                            "Access denied: SSN filtering requires admin privileges"
                        );
                    }
                }
            }
        });
    }
    
    private void validateBusinessRules(FilterRequest<UserPropertyRef> request) {
        request.filters().values().forEach(definition -> {
            switch (definition.ref()) {
                case AGE -> {
                    if (definition.operator() == Op.RANGE) {
                        List<Integer> range = (List<Integer>) definition.value();
                        if (range.get(1) - range.get(0) > 50) {
                            throw new FilterValidationException(
                                "Age range cannot exceed 50 years"
                            );
                        }
                    }
                }
                case CREATION_DATE -> {
                    if (definition.operator() == Op.RANGE) {
                        List<LocalDateTime> dates = (List<LocalDateTime>) definition.value();
                        Duration duration = Duration.between(dates.get(0), dates.get(1));
                        if (duration.toDays() > 365) {
                            throw new FilterValidationException(
                                "Date range cannot exceed 1 year"
                            );
                        }
                    }
                }
            }
        });
    }
    
    private void validatePerformance(FilterRequest<UserPropertyRef> request) {
        // Limit number of filters to prevent performance issues
        if (request.filters().size() > 10) {
            throw new FilterValidationException(
                "Too many filters: Maximum 10 filters allowed"
            );
        }
        
        // Check for potentially expensive operations
        long expensiveFilters = request.filters().values().stream()
            .filter(def -> def.operator() == Op.MATCHES)
            .count();
            
        if (expensiveFilters > 3) {
            throw new FilterValidationException(
                "Too many text search filters: Maximum 3 MATCHES operations allowed"
            );
        }
    }
}
```

### 2. Exception Handling

```java
// ✅ Good: Specific exception types with detailed information
@ControllerAdvice
public class FilterExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(FilterExceptionHandler.class);
    
    @ExceptionHandler(DSLSyntaxException.class)
    public ResponseEntity<ErrorResponse> handleDSLSyntax(DSLSyntaxException e) {
        logger.warn("Invalid filter syntax: {}", e.getMessage());
        
        return ResponseEntity.badRequest()
            .body(ErrorResponse.builder()
                .code("INVALID_FILTER_SYNTAX")
                .message("The filter syntax is invalid")
                .details(e.getMessage())
                .timestamp(Instant.now())
                .build());
    }
    
    @ExceptionHandler(FilterValidationException.class)
    public ResponseEntity<ErrorResponse> handleFilterValidation(FilterValidationException e) {
        logger.warn("Filter validation failed: {}", e.getMessage());
        
        return ResponseEntity.badRequest()
            .body(ErrorResponse.builder()
                .code("INVALID_FILTER")
                .message("The filter request is invalid")
                .details(e.getMessage())
                .timestamp(Instant.now())
                .build());
    }
    
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException e) {
        logger.warn("Access denied for filter operation: {}", e.getMessage());
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ErrorResponse.builder()
                .code("ACCESS_DENIED")
                .message("Access denied")
                .details("You don't have permission to use this filter")
                .timestamp(Instant.now())
                .build());
    }
}

// ✅ Good: Structured error response
@Data
@Builder
public class ErrorResponse {
    private String code;
    private String message;
    private String details;
    private Instant timestamp;
    private Map<String, Object> metadata;
}
```

## Security Considerations

### 1. Field-Level Security

```java
// ✅ Good: Role-based field access control
@Component
public class SecurityAwareFilterContext {
    
    private final FilterContext<User, UserPropertyRef> delegate;
    private final SecurityService securityService;
    
    public FilterContext<User, UserPropertyRef> createSecureContext() {
        return new FilterContext<>(
            User.class,
            UserPropertyRef.class,
            definition -> {
                // Check access before processing
                securityService.checkFieldAccess(definition.ref());
                return mapUserProperty(definition);
            }
        );
    }
    
    private Object mapUserProperty(FilterDefinition<UserPropertyRef> definition) {
        return switch (definition.ref()) {
            case PERSONAL_NAME -> "name";
            case CONTACT_EMAIL -> {
                // Mask email for non-admin users
                if (!securityService.hasRole("ADMIN")) {
                    yield createEmailMaskingMapping();
                }
                yield "email";
            }
            // Other mappings...
        };
    }
}

@Service
public class SecurityService {
    
    private static final Map<UserPropertyRef, Set<String>> FIELD_ACCESS_RULES = Map.of(
        UserPropertyRef.SALARY_AMOUNT, Set.of("ROLE_HR", "ROLE_ADMIN"),
        UserPropertyRef.PERSONAL_SSN, Set.of("ROLE_ADMIN"),
        UserPropertyRef.PERFORMANCE_RATING, Set.of("ROLE_MANAGER", "ROLE_HR", "ROLE_ADMIN")
    );
    
    public void checkFieldAccess(UserPropertyRef field) {
        Set<String> requiredRoles = FIELD_ACCESS_RULES.get(field);
        if (requiredRoles != null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean hasAccess = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(requiredRoles::contains);
                
            if (!hasAccess) {
                throw new AccessDeniedException(
                    "Insufficient privileges to access field: " + field
                );
            }
        }
    }
    
    public boolean hasRole(String role) {
        return SecurityContextHolder.getContext().getAuthentication()
            .getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_" + role));
    }
}
```

### 2. Input Sanitization

```java
// ✅ Good: Sanitize user input
@Component
public class FilterInputSanitizer {
    
    private static final Pattern SAFE_TEXT_PATTERN = Pattern.compile("^[a-zA-Z0-9\\s\\-_@.]*$");
    private static final int MAX_TEXT_LENGTH = 100;
    
    public FilterRequest<UserPropertyRef> sanitize(FilterRequest<UserPropertyRef> request) {
        Map<String, FilterDefinition<UserPropertyRef>> sanitizedFilters = new HashMap<>();
        
        request.filters().forEach((key, definition) -> {
            FilterDefinition<UserPropertyRef> sanitized = sanitizeDefinition(definition);
            sanitizedFilters.put(key, sanitized);
        });
        
        return FilterRequest.<UserPropertyRef>builder()
            .filters(sanitizedFilters)
            .combineWith(sanitizeCombination(request.combineWith()))
            .build();
    }
    
    private FilterDefinition<UserPropertyRef> sanitizeDefinition(FilterDefinition<UserPropertyRef> definition) {
        Object sanitizedValue = sanitizeValue(definition.value(), definition.ref().type());
        
        return new FilterDefinition<>(
            definition.ref(),
            definition.operator(),
            sanitizedValue
        );
    }
    
    private Object sanitizeValue(Object value, Class<?> expectedType) {
        if (value == null) return null;
        
        if (expectedType == String.class && value instanceof String str) {
            // Remove potentially dangerous characters
            String sanitized = str.replaceAll("[<>\"'&]", "");
            
            // Limit length
            if (sanitized.length() > MAX_TEXT_LENGTH) {
                sanitized = sanitized.substring(0, MAX_TEXT_LENGTH);
            }
            
            // Validate pattern
            if (!SAFE_TEXT_PATTERN.matcher(sanitized).matches()) {
                throw new FilterValidationException("Invalid characters in text filter");
            }
            
            return sanitized;
        }
        
        return value; // Other types handled by FilterQL validation
    }
    
    private String sanitizeCombination(String combination) {
        if (combination == null) return null;
        
        // Only allow safe combination operators
        return combination.replaceAll("[^a-zA-Z0-9\\s&|()!]", "");
    }
}
```

## Testing Strategy

### 1. Unit Testing

```java
// ✅ Good: Comprehensive unit tests
@ExtendWith(MockitoExtension.class)
class FilterContextTest {
    
    private FilterContext<User, UserPropertyRef> context;
    
    @BeforeEach
    void setUp() {
        context = new FilterContext<>(
            User.class,
            UserPropertyRef.class,
            UserFilterMappings::mapUserProperty
        );
    }
    
    @Test
    void shouldCreateSimpleCondition() {
        // Given
        FilterDefinition<UserPropertyRef> definition = 
            new FilterDefinition<>(UserPropertyRef.PERSONAL_NAME, Op.EQ, "John");
        
        // When
        Condition condition = context.addCondition("nameFilter", definition);
        
        // Then
        assertThat(condition).isNotNull();
        assertThat(context.getCondition("nameFilter")).isSameAs(condition);
    }
    
    @ParameterizedTest
    @EnumSource(UserPropertyRef.class)
    void shouldSupportAllDefinedProperties(UserPropertyRef property) {
        // Given
        Object testValue = createValidTestValue(property);
        Op testOperator = property.supportedOperators().iterator().next();
        FilterDefinition<UserPropertyRef> definition = 
            new FilterDefinition<>(property, testOperator, testValue);
        
        // When & Then
        assertDoesNotThrow(() -> {
            context.addCondition("test_" + property.name(), definition);
        });
    }
    
    @Test
    void shouldRejectInvalidOperatorForProperty() {
        // Given
        FilterDefinition<UserPropertyRef> definition = 
            new FilterDefinition<>(UserPropertyRef.AGE, Op.MATCHES, "invalid");
        
        // When & Then
        assertThatThrownBy(() -> context.addCondition("ageFilter", definition))
            .isInstanceOf(FilterValidationException.class)
            .hasMessageContaining("not supported");
    }
    
    private Object createValidTestValue(UserPropertyRef property) {
        return switch (property) {
            case PERSONAL_NAME, CONTACT_EMAIL -> "test";
            case AGE -> 25;
            case SALARY_AMOUNT -> new BigDecimal("50000");
            case CREATION_DATE -> LocalDateTime.now();
            case IS_ACTIVE -> true;
            case ACCOUNT_STATUS -> UserStatus.ACTIVE;
        };
    }
}
```

### 2. Integration Testing

```java
// ✅ Good: Test real database interactions
@SpringBootTest
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.datasource.url=jdbc:h2:mem:testdb"
})
@Transactional
class UserFilterIntegrationTest {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private FilterContext<User, UserPropertyRef> filterContext;
    
    @BeforeEach
    void setUp() {
        // Create test data
        List<User> users = List.of(
            createUser("John", "Doe", "john@example.com", 25, UserStatus.ACTIVE),
            createUser("Jane", "Smith", "jane@example.com", 30, UserStatus.ACTIVE),
            createUser("Bob", "Wilson", "bob@example.com", 35, UserStatus.INACTIVE)
        );
        userRepository.saveAll(users);
    }
    
    @Test
    void shouldFilterBySimpleCondition() {
        // Given
        FilterRequest<UserPropertyRef> request = FilterRequest.<UserPropertyRef>builder()
            .filter("activeUsers", new FilterDefinition<>(UserPropertyRef.ACCOUNT_STATUS, Op.EQ, UserStatus.ACTIVE))
            .build();
        
        // When
        List<User> results = executeFilter(request);
        
        // Then
        assertThat(results).hasSize(2);
        assertThat(results).allSatisfy(user -> 
            assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE)
        );
    }
    
    @Test
    void shouldFilterByComplexCondition() {
        // Given
        FilterRequest<UserPropertyRef> request = FilterRequest.<UserPropertyRef>builder()
            .filter("nameFilter", new FilterDefinition<>(UserPropertyRef.PERSONAL_NAME, Op.MATCHES, "J%"))
            .filter("ageFilter", new FilterDefinition<>(UserPropertyRef.AGE, Op.GT, 25))
            .combineWith("nameFilter & ageFilter")
            .build();
        
        // When
        List<User> results = executeFilter(request);
        
        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getFirstName()).isEqualTo("Jane");
    }
    
    private List<User> executeFilter(FilterRequest<UserPropertyRef> request) {
        FilterResolver resolver = FilterResolver.of(filterContext);
        PredicateResolver<User> predicateResolver = resolver.resolve(User.class, request);
        
        Specification<User> spec = (root, query, cb) -> 
            predicateResolver.resolve(root, query, cb);
            
        return userRepository.findAll(spec);
    }
}
```

### 3. Performance Testing

```java
// ✅ Good: Performance benchmarks
@Component
public class FilterPerformanceTest {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private FilterContext<User, UserPropertyRef> filterContext;
    
    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void shouldExecuteSimpleFilterWithinTimeout() {
        // Test with large dataset
        createLargeDataset(10000);
        
        FilterRequest<UserPropertyRef> request = FilterRequest.<UserPropertyRef>builder()
            .filter("statusFilter", new FilterDefinition<>(UserPropertyRef.ACCOUNT_STATUS, Op.EQ, UserStatus.ACTIVE))
            .build();
        
        // Should complete within 5 seconds
        List<User> results = executeFilter(request);
        assertThat(results).isNotEmpty();
    }
    
    @Test
    void shouldScaleLinearlyWithDataSize() {
        // Test performance scaling
        Map<Integer, Long> timings = new HashMap<>();
        
        for (int size : Arrays.asList(1000, 5000, 10000)) {
            createLargeDataset(size);
            
            long startTime = System.currentTimeMillis();
            executeFilter(createTestFilter());
            long duration = System.currentTimeMillis() - startTime;
            
            timings.put(size, duration);
        }
        
        // Verify reasonable scaling
        assertThat(timings.get(10000)).isLessThan(timings.get(1000) * 15); // Should not be more than 15x slower
    }
}
```

## Code Organization

### 1. Package Structure

```
com.example.filters/
├── config/
│   ├── FilterConfig.java
│   └── SecurityFilterConfig.java
├── context/
│   ├── UserFilterContext.java
│   ├── OrderFilterContext.java
│   └── mapping/
│       ├── UserFilterMappings.java
│       ├── OrderFilterMappings.java
│       └── SecurityAwareMappings.java
├── properties/
│   ├── UserPropertyRef.java
│   ├── OrderPropertyRef.java
│   └── common/
│       └── OperatorUtils.java
├── service/
│   ├── FilterService.java
│   ├── UserSearchService.java
│   └── validation/
│       ├── FilterValidator.java
│       ├── SecurityValidator.java
│       └── BusinessRuleValidator.java
├── controller/
│   ├── UserSearchController.java
│   └── exception/
│       └── FilterExceptionHandler.java
└── util/
    ├── FilterTestUtils.java
    └── TestDataBuilder.java
```

### 2. Service Layer Design

```java
// ✅ Good: Layered service design
@Service
@Transactional(readOnly = true)
public class UserSearchService {
    
    private final UserRepository userRepository;
    private final FilterContext<User, UserPropertyRef> filterContext;
    private final FilterValidator validator;
    private final SecurityService securityService;
    
    public UserSearchService(
            UserRepository userRepository,
            FilterContext<User, UserPropertyRef> filterContext,
            FilterValidator validator,
            SecurityService securityService) {
        this.userRepository = userRepository;
        this.filterContext = filterContext;
        this.validator = validator;
        this.securityService = securityService;
    }
    
    // Main search method with full features
    public Page<User> searchUsers(FilterRequest<UserPropertyRef> request, Pageable pageable) {
        // 1. Security check
        securityService.checkAccess(request);
        
        // 2. Validate request
        validator.validate(request);
        
        // 3. Build and execute query
        Specification<User> specification = buildSpecification(request);
        
        return userRepository.findAll(specification, pageable);
    }
    
    // Simplified search for internal use
    public List<User> findUsers(FilterRequest<UserPropertyRef> request) {
        validator.validate(request);
        Specification<User> specification = buildSpecification(request);
        return userRepository.findAll(specification);
    }
    
    // Count only (for pagination info)
    public long countUsers(FilterRequest<UserPropertyRef> request) {
        validator.validate(request);
        Specification<User> specification = buildSpecification(request);
        return userRepository.count(specification);
    }
    
    private Specification<User> buildSpecification(FilterRequest<UserPropertyRef> request) {
        FilterResolver resolver = FilterResolver.of(filterContext);
        PredicateResolver<User> predicateResolver = resolver.resolve(User.class, request);
        
        return (root, query, cb) -> predicateResolver.resolve(root, query, cb);
    }
}
```

## Common Pitfalls

### 1. Incorrect Operator Usage

```java
// ❌ Wrong: Using MATCHES with wrong syntax
FilterDefinition<UserPropertyRef> wrongMatches = 
    new FilterDefinition<>(UserPropertyRef.NAME, Op.MATCHES, "*John*"); // Wrong wildcard

// ✅ Correct: SQL LIKE syntax
FilterDefinition<UserPropertyRef> correctMatches = 
    new FilterDefinition<>(UserPropertyRef.NAME, Op.MATCHES, "%John%");

// ❌ Wrong: Using EQ with collection
FilterDefinition<UserPropertyRef> wrongIn = 
    new FilterDefinition<>(UserPropertyRef.STATUS, Op.EQ, Arrays.asList(Status.ACTIVE, Status.PENDING));

// ✅ Correct: Using IN with collection
FilterDefinition<UserPropertyRef> correctIn = 
    new FilterDefinition<>(UserPropertyRef.STATUS, Op.IN, Arrays.asList(Status.ACTIVE, Status.PENDING));
```

### 2. Performance Issues

```java
// ❌ Wrong: N+1 query problem
case DEPARTMENT_NAME -> "department.name"; // Will cause lazy loading

// ✅ Correct: Use fetch join
case DEPARTMENT_NAME -> new PredicateResolverMapping<User, UserPropertyRef>() {
    @Override
    public PredicateResolver<User> resolve(FilterDefinition<UserPropertyRef> definition) {
        return (root, query, cb) -> {
            if (query.getResultType() == User.class) {
                root.fetch("department", JoinType.LEFT);
            }
            return cb.equal(root.get("department").get("name"), definition.value());
        };
    }
};

// ❌ Wrong: Complex subquery in every filter
case RECENT_ORDERS -> new PredicateResolverMapping<User, UserPropertyRef>() {
    @Override
    public PredicateResolver<User> resolve(FilterDefinition<UserPropertyRef> definition) {
        return (root, query, cb) -> {
            // This subquery executes for every user!
            Subquery<Long> subquery = query.subquery(Long.class);
            // ... expensive subquery logic
        };
    }
};

// ✅ Correct: Use efficient EXISTS or JOIN
case RECENT_ORDERS -> new PredicateResolverMapping<User, UserPropertyRef>() {
    @Override
    public PredicateResolver<User> resolve(FilterDefinition<UserPropertyRef> definition) {
        return (root, query, cb) -> {
            Subquery<Integer> subquery = query.subquery(Integer.class);
            Root<Order> orderRoot = subquery.from(Order.class);
            subquery.select(cb.literal(1))
                   .where(
                       cb.equal(orderRoot.get("user"), root),
                       cb.greaterThan(orderRoot.get("createdDate"), LocalDateTime.now().minusDays(30))
                   );
            return cb.exists(subquery);
        };
    }
};
```

### 3. Security Vulnerabilities

```java
// ❌ Wrong: No access control
public enum UserPropertyRef implements PropertyReference {
    SALARY(BigDecimal.class, OperatorUtils.FOR_NUMBER),    // Sensitive data exposed!
    SSN(String.class, Set.of(Op.EQ));                      // PII exposed!
}

// ✅ Correct: Implement access control
public enum UserPropertyRef implements PropertyReference {
    SALARY(BigDecimal.class, OperatorUtils.FOR_NUMBER) {
        @Override
        public boolean validate(Op operator, Object value) {
            // Check access permissions
            SecurityService.checkSalaryAccess();
            return super.validate(operator, value);
        }
    };
}

// ❌ Wrong: Direct value insertion
case NAME -> (root, query, cb) -> 
    cb.equal(root.get("name"), definition.value()); // Potential injection

// ✅ Correct: Parameterized queries (JPA handles this)
case NAME -> "name"; // Let FilterQL handle parameterization
```

### 4. Testing Mistakes

```java
// ❌ Wrong: Not testing edge cases
@Test
void shouldFilterUsers() {
    FilterRequest<UserPropertyRef> request = FilterRequest.<UserPropertyRef>builder()
        .filter("name", new FilterDefinition<>(UserPropertyRef.NAME, Op.EQ, "John"))
        .build();
    
    List<User> results = userService.findUsers(request);
    assertThat(results).hasSize(1); // Only tests happy path!
}

// ✅ Correct: Comprehensive testing
@Test
void shouldFilterUsers() {
    // Test data setup
    userRepository.saveAll(Arrays.asList(
        createUser("John", "Doe"),
        createUser("john", "smith"),     // Test case sensitivity
        createUser("Johnny", "Wilson"),  // Test partial matches
        createUser("Jane", "Doe")        // Test non-matches
    ));
    
    // Test exact match
    FilterRequest<UserPropertyRef> exactMatch = FilterRequest.<UserPropertyRef>builder()
        .filter("name", new FilterDefinition<>(UserPropertyRef.NAME, Op.EQ, "John"))
        .build();
    
    List<User> exactResults = userService.findUsers(exactMatch);
    assertThat(exactResults).hasSize(1);
    assertThat(exactResults.get(0).getFirstName()).isEqualTo("John");
    
    // Test pattern match
    FilterRequest<UserPropertyRef> patternMatch = FilterRequest.<UserPropertyRef>builder()
        .filter("name", new FilterDefinition<>(UserPropertyRef.NAME, Op.MATCHES, "John%"))
        .build();
    
    List<User> patternResults = userService.findUsers(patternMatch);
    assertThat(patternResults).hasSize(2); // John and Johnny
    
    // Test no matches
    FilterRequest<UserPropertyRef> noMatch = FilterRequest.<UserPropertyRef>builder()
        .filter("name", new FilterDefinition<>(UserPropertyRef.NAME, Op.EQ, "NonExistent"))
        .build();
    
    List<User> noResults = userService.findUsers(noMatch);
    assertThat(noResults).isEmpty();
}
```

## Summary

Following these best practices will help you build robust, maintainable, and secure FilterQL implementations:

1. **Design clear, business-meaningful property references**
2. **Use simple path mappings when possible**
3. **Implement comprehensive validation and security**
4. **Optimize for performance from the start**
5. **Write thorough tests covering edge cases**
6. **Organize code in logical layers**
7. **Handle errors gracefully with specific messages**
8. **Consider security implications for all filter operations**

Remember: FilterQL is a powerful tool, but with power comes responsibility. Always validate inputs, secure sensitive data, and test thoroughly.