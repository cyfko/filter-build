# Spring Data Adapter Guide

The FilterQL Spring adapter provides seamless integration with Spring Data JPA, allowing you to convert FilterQL expressions directly into Spring `Specification` objects.

## Quick Setup

### Dependencies

```xml
<dependency>
    <groupId>io.github.cyfko</groupId>
    <artifactId>filterql-spring</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Basic Usage

```java
@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    public List<User> findUsers(FilterRequest<UserProperty> request) {
        Specification<User> spec = SpecificationBuilder.toSpecification(request);
        return userRepository.findAll(spec);
    }
}
```

## Core Components

### SpecificationBuilder

The main entry point for converting FilterQL requests to Spring Specifications:

```java
public class SpecificationBuilder {
    public static <T, P extends Enum<P> & PropertyRef & PathShape> 
           Specification<T> toSpecification(FilterRequest<P> filterRequest) {
        // Converts FilterRequest to Spring Specification
    }
}
```

### Example Usage

```java
// Define your properties
public enum UserProperty implements PropertyRef, PathShape {
    NAME("name"),
    EMAIL("email"),
    AGE("age"),
    DEPARTMENT_NAME("department.name"),
    MANAGER_EMAIL("manager.email");
    
    private final String path;
    
    UserProperty(String path) {
        this.path = path;
    }
    
    @Override
    public String getPath() {
        return path;
    }
}

// Build filter request
FilterRequest<UserProperty> request = FilterRequest.<UserProperty>builder()
    .filter("senior", UserProperty.AGE, Operator.GREATER_THAN, 30)
    .filter("engineering", UserProperty.DEPARTMENT_NAME, Operator.EQUALS, "Engineering")
    .filter("active", UserProperty.STATUS, Operator.EQUALS, "ACTIVE")
    .combineWith("active AND (senior OR engineering)")
    .build();

// Convert to Specification
Specification<User> spec = SpecificationBuilder.toSpecification(request);

// Use with repository
List<User> results = userRepository.findAll(spec);
```

## Advanced Features

### Nested Properties

FilterQL automatically handles nested property navigation:

```java
public enum UserProperty implements PropertyRef, PathShape {
    // Direct properties
    NAME("name"),
    EMAIL("email"),
    
    // Single-level nesting
    DEPARTMENT_NAME("department.name"),
    DEPARTMENT_CODE("department.code"),
    
    // Multi-level nesting
    MANAGER_NAME("manager.name"),
    MANAGER_EMAIL("manager.email"),
    DEPARTMENT_MANAGER_NAME("department.manager.name"),
    
    // Collection navigation
    PROJECT_NAMES("projects.name"),
    SKILL_NAMES("skills.name");
    
    // ... implementation
}
```

#### Generated JPA Joins

FilterQL automatically creates the necessary JPA joins:

```java
// For property "department.manager.name"
// FilterQL generates:
Join<User, Department> deptJoin = root.join("department", JoinType.LEFT);
Join<Department, Manager> managerJoin = deptJoin.join("manager", JoinType.LEFT);
Path<String> namePath = managerJoin.get("name");
```

### Collection Filtering

Handle collection properties elegantly:

```java
public enum UserProperty implements PropertyRef, PathShape {
    SKILLS("skills"),           // Collection of Skill entities
    SKILL_NAMES("skills.name"), // Names of skills
    PROJECT_TAGS("projects.tags"); // Nested collection
    
    // ... implementation
}

// Filter users with specific skills
FilterRequest<UserProperty> request = FilterRequest.<UserProperty>builder()
    .filter("java-skill", UserProperty.SKILL_NAMES, Operator.CONTAINS, "Java")
    .filter("python-skill", UserProperty.SKILL_NAMES, Operator.CONTAINS, "Python")
    .combineWith("java-skill OR python-skill")
    .build();
```

### Complex Specifications

Combine FilterQL with existing Specifications:

```java
@Service
public class UserService {
    
    public List<User> findActiveUsersWithFilters(FilterRequest<UserProperty> request) {
        // Base specification for active users
        Specification<User> activeSpec = (root, query, cb) -> 
            cb.equal(root.get("status"), "ACTIVE");
        
        // FilterQL specification
        Specification<User> filterSpec = SpecificationBuilder.toSpecification(request);
        
        // Combine specifications
        Specification<User> combinedSpec = activeSpec.and(filterSpec);
        
        return userRepository.findAll(combinedSpec);
    }
}
```

## REST API Integration

### Simple REST Endpoint

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/search")
    public ResponseEntity<List<User>> searchUsers(
            @RequestBody FilterRequest<UserProperty> request) {
        try {
            List<User> users = userService.findUsers(request);
            return ResponseEntity.ok(users);
        } catch (DSLSyntaxException | FilterValidationException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
```

### Request/Response DTOs

```java
public class UserSearchRequest {
    private List<FilterDefinition> filters;
    private String combineWith;
    
    // Convert to FilterRequest
    public FilterRequest<UserProperty> toFilterRequest() {
        FilterRequest.Builder<UserProperty> builder = FilterRequest.builder();
        
        filters.forEach(filter -> {
            UserProperty property = UserProperty.valueOf(filter.getProperty());
            Operator operator = Operator.valueOf(filter.getOperator());
            builder.filter(filter.getId(), property, operator, filter.getValue());
        });
        
        return builder.combineWith(combineWith).build();
    }
}

public class FilterDefinition {
    private String id;
    private String property;
    private String operator;
    private Object value;
    
    // getters and setters...
}
```

### Dynamic Query Building

```java
@GetMapping("/search")
public ResponseEntity<List<User>> searchUsers(
        @RequestParam(required = false) String name,
        @RequestParam(required = false) Integer minAge,
        @RequestParam(required = false) String department,
        @RequestParam(required = false) String status) {
    
    FilterRequest.Builder<UserProperty> builder = FilterRequest.builder();
    List<String> conditions = new ArrayList<>();
    
    if (name != null && !name.trim().isEmpty()) {
        builder.filter("name", UserProperty.NAME, Operator.CONTAINS, name);
        conditions.add("name");
    }
    
    if (minAge != null) {
        builder.filter("age", UserProperty.AGE, Operator.GREATER_THAN_OR_EQUAL, minAge);
        conditions.add("age");
    }
    
    if (department != null && !department.trim().isEmpty()) {
        builder.filter("dept", UserProperty.DEPARTMENT_NAME, Operator.EQUALS, department);
        conditions.add("dept");
    }
    
    if (status != null && !status.trim().isEmpty()) {
        builder.filter("status", UserProperty.STATUS, Operator.EQUALS, status);
        conditions.add("status");
    }
    
    if (conditions.isEmpty()) {
        return ResponseEntity.ok(userRepository.findAll());
    }
    
    FilterRequest<UserProperty> request = builder
        .combineWith(String.join(" AND ", conditions))
        .build();
    
    Specification<User> spec = SpecificationBuilder.toSpecification(request);
    return ResponseEntity.ok(userRepository.findAll(spec));
}
```

## Pagination and Sorting

FilterQL works seamlessly with Spring Data pagination:

```java
@Service
public class UserService {
    
    public Page<User> findUsers(FilterRequest<UserProperty> request, Pageable pageable) {
        Specification<User> spec = SpecificationBuilder.toSpecification(request);
        return userRepository.findAll(spec, pageable);
    }
}

@RestController
public class UserController {
    
    @PostMapping("/search")
    public ResponseEntity<Page<User>> searchUsers(
            @RequestBody FilterRequest<UserProperty> request,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        
        Page<User> users = userService.findUsers(request, pageable);
        return ResponseEntity.ok(users);
    }
}
```

## Error Handling

### Global Exception Handler

```java
@ControllerAdvice
public class FilterExceptionHandler {
    
    @ExceptionHandler(DSLSyntaxException.class)
    public ResponseEntity<ErrorResponse> handleDSLSyntaxException(DSLSyntaxException e) {
        ErrorResponse error = new ErrorResponse(
            "INVALID_FILTER_SYNTAX",
            "Invalid filter expression: " + e.getMessage()
        );
        return ResponseEntity.badRequest().body(error);
    }
    
    @ExceptionHandler(FilterValidationException.class)
    public ResponseEntity<ErrorResponse> handleFilterValidationException(FilterValidationException e) {
        ErrorResponse error = new ErrorResponse(
            "INVALID_FILTER",
            "Filter validation failed",
            e.getErrors()
        );
        return ResponseEntity.badRequest().body(error);
    }
}

public class ErrorResponse {
    private String code;
    private String message;
    private List<String> details;
    
    // constructors, getters, setters...
}
```

## Performance Considerations

### Query Optimization

```java
@Service
public class OptimizedUserService {
    
    // Use projection to limit returned fields
    public List<UserSummary> findUserSummaries(FilterRequest<UserProperty> request) {
        Specification<User> spec = SpecificationBuilder.toSpecification(request);
        
        return userRepository.findAll(spec).stream()
            .map(user -> new UserSummary(user.getName(), user.getEmail()))
            .collect(Collectors.toList());
    }
    
    // Use exists query for count operations
    public boolean hasUsersMatchingCriteria(FilterRequest<UserProperty> request) {
        Specification<User> spec = SpecificationBuilder.toSpecification(request);
        return userRepository.count(spec) > 0;
    }
}
```

### Caching Strategies

```java
@Service
@Transactional(readOnly = true)
public class CachedUserService {
    
    @Cacheable(value = "user-search", key = "#request.hashCode()")
    public List<User> findUsers(FilterRequest<UserProperty> request) {
        Specification<User> spec = SpecificationBuilder.toSpecification(request);
        return userRepository.findAll(spec);
    }
    
    @CacheEvict(value = "user-search", allEntries = true)
    public void clearSearchCache() {
        // Called when user data changes
    }
}
```

## Testing

### Unit Testing Specifications

```java
@Test
public void testSpecificationGeneration() {
    FilterRequest<UserProperty> request = FilterRequest.<UserProperty>builder()
        .filter("name", UserProperty.NAME, Operator.EQUALS, "John")
        .filter("age", UserProperty.AGE, Operator.GREATER_THAN, 25)
        .combineWith("name AND age")
        .build();
    
    Specification<User> spec = SpecificationBuilder.toSpecification(request);
    
    assertThat(spec).isNotNull();
    
    // Test with mock data
    CriteriaBuilder cb = mock(CriteriaBuilder.class);
    CriteriaQuery<?> query = mock(CriteriaQuery.class);
    Root<User> root = mock(Root.class);
    
    // Verify specification behavior
    Predicate result = spec.toPredicate(root, query, cb);
    assertThat(result).isNotNull();
}
```

### Integration Testing

```java
@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
public class UserSearchIntegrationTest {
    
    @Autowired
    private UserRepository userRepository;
    
    @Test
    public void testComplexUserSearch() {
        // Setup test data
        User john = new User("John", "john@example.com", 30);
        User jane = new User("Jane", "jane@example.com", 25);
        userRepository.saveAll(Arrays.asList(john, jane));
        
        // Create filter request
        FilterRequest<UserProperty> request = FilterRequest.<UserProperty>builder()
            .filter("senior", UserProperty.AGE, Operator.GREATER_THAN, 28)
            .filter("engineering", UserProperty.EMAIL, Operator.CONTAINS, "example.com")
            .combineWith("senior AND engineering")
            .build();
        
        // Execute search
        Specification<User> spec = SpecificationBuilder.toSpecification(request);
        List<User> results = userRepository.findAll(spec);
        
        // Verify results
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("John");
    }
}
```

## Next Steps

- [JPA Adapter Guide](jpa-adapter.md) - Compare with JPA adapter
- [Advanced Usage](advanced-usage.md) - Custom operators and extensions
- [API Reference](api-reference.md) - Complete API documentation