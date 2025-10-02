# Getting Started with FilterQL

Welcome to FilterQL! This guide will walk you through setting up and using FilterQL in your Java application.

## Prerequisites

- Java 21 or higher
- Maven 3.6+ or Gradle 7+
- Basic understanding of JPA/Hibernate (for Spring adapter)

## Installation

### Option 1: Core Module Only

If you want to use FilterQL with a custom adapter or build your own integration:

**Maven:**
```xml
<dependency>
    <groupId>io.github.cyfko</groupId>
    <artifactId>filterql-core</artifactId>
    <version>2.0.0</version>
</dependency>
```

**Gradle:**
```gradle
implementation 'io.github.cyfko:filterql-core:2.0.0'
```

### Option 2: With Spring Data JPA (Recommended)

For Spring Boot applications using JPA:

**Maven:**
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

**Gradle:**
```gradle
implementation 'io.github.cyfko:filterql-core:2.0.0'
implementation 'io.github.cyfko:filterql-spring:2.0.0'
```

## Quick Start Example

Let's build a complete filtering system for a User entity.

### Step 1: Define Your Entity

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "email")
    private String email;
    
    @Column(name = "age")
    private Integer age;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private UserStatus status;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // constructors, getters, setters...
}

public enum UserStatus {
    ACTIVE, INACTIVE, PENDING, SUSPENDED
}
```

### Step 2: Create Property Reference Enum

```java
import io.github.cyfko.filterql.core.validation.PropertyReference;
import io.github.cyfko.filterql.core.validation.Op;

public enum UserPropertyRef implements PropertyReference {
    NAME(String.class, Set.of(Op.EQ, Op.MATCHES, Op.IN)),
    EMAIL(String.class, Set.of(Op.EQ, Op.MATCHES)),
    AGE(Integer.class, Set.of(Op.EQ, Op.GT, Op.GTE, Op.LT, Op.LTE, Op.RANGE)),
    STATUS(UserStatus.class, Set.of(Op.EQ, Op.NE, Op.IN)),
    CREATED_AT(LocalDateTime.class, Set.of(Op.EQ, Op.GT, Op.GTE, Op.LT, Op.LTE, Op.RANGE));

    private final Class<?> type;
    private final Set<Op> supportedOperators;

    UserPropertyRef(Class<?> type, Set<Op> supportedOperators) {
        this.type = type;
        this.supportedOperators = Set.copyOf(supportedOperators);
    }

    @Override
    public Class<?> getType() {
        return type;
    }

    @Override
    public Set<Op> getSupportedOperators() {
        return supportedOperators;
    }
}
```

### Step 3: Create Filter Context

```java
import io.github.cyfko.filterql.adapter.spring.FilterContext;

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
                case STATUS -> "status";
                case CREATED_AT -> "createdAt";
            }
        );
    }
}
```

### Step 4: Create a Service

```java
import io.github.cyfko.filterql.core.FilterResolver;
import io.github.cyfko.filterql.core.domain.PredicateResolver;
import io.github.cyfko.filterql.core.model.FilterRequest;

@Service
public class UserService {
    
    @Autowired
    private EntityManager entityManager;
    
    @Autowired
    private FilterContext<User, UserPropertyRef> userFilterContext;
    
    public List<User> findUsers(FilterRequest<UserPropertyRef> filterRequest) {
        // Create filter resolver
        FilterResolver resolver = FilterResolver.of(userFilterContext);
        
        // Generate predicate resolver
        PredicateResolver<User> predicateResolver = resolver.resolve(User.class, filterRequest);
        
        // Execute JPA query
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> query = cb.createQuery(User.class);
        Root<User> root = query.from(User.class);
        
        query.where(predicateResolver.resolve(root, query, cb));
        
        return entityManager.createQuery(query).getResultList();
    }
}
```

### Step 5: Create a Controller

```java
import io.github.cyfko.filterql.core.model.FilterDefinition;
import io.github.cyfko.filterql.core.model.FilterRequest;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/search")
    public List<User> searchUsers(@RequestBody FilterRequest<UserPropertyRef> filterRequest) {
        return userService.findUsers(filterRequest);
    }
    
    @GetMapping("/active-adults")
    public List<User> getActiveAdults() {
        FilterRequest<UserPropertyRef> request = FilterRequest.<UserPropertyRef>builder()
            .filter("statusFilter", new FilterDefinition<>(UserPropertyRef.STATUS, Op.EQ, UserStatus.ACTIVE))
            .filter("ageFilter", new FilterDefinition<>(UserPropertyRef.AGE, Op.GTE, 18))
            .combineWith("statusFilter & ageFilter")
            .build();
            
        return userService.findUsers(request);
    }
}
```

### Step 6: Test Your Implementation

**Simple POST request to `/api/users/search`:**

```json
{
  "filters": {
    "nameFilter": {
      "ref": "NAME",
      "operator": "MATCHES",
      "value": "John%"
    },
    "ageFilter": {
      "ref": "AGE",
      "operator": "GTE",
      "value": 25
    },
    "statusFilter": {
      "ref": "STATUS",
      "operator": "EQ",
      "value": "ACTIVE"
    }
  },
  "combineWith": "nameFilter & (ageFilter | statusFilter)"
}
```

This will find all users whose name starts with "John" AND (age >= 25 OR status is ACTIVE).

## What's Next?

Now that you have a basic FilterQL setup running:

1. **[Learn about Core Concepts](../core-module/overview.md)** - Understand the architecture in depth
2. **[Explore Advanced Spring Integration](../spring-adapter/configuration.md)** - Custom mappings and complex scenarios  
3. **[See More Examples](../core-module/examples/basic-usage.md)** - Real-world filtering patterns
4. **[Check the FAQ](../guides/faq.md)** - Common questions and solutions

## Common Patterns

### Range Queries
```java
// Age between 18 and 65
new FilterDefinition<>(UserPropertyRef.AGE, Op.RANGE, List.of(18, 65))
```

### Multiple Values
```java
// Status is ACTIVE or PENDING
new FilterDefinition<>(UserPropertyRef.STATUS, Op.IN, List.of(UserStatus.ACTIVE, UserStatus.PENDING))
```

### Null Checks
```java
// Email is not null
new FilterDefinition<>(UserPropertyRef.EMAIL, Op.NOT_NULL, null)
```

### Complex Boolean Logic
```java
FilterRequest.builder()
    .filter("active", new FilterDefinition<>(UserPropertyRef.STATUS, Op.EQ, UserStatus.ACTIVE))
    .filter("young", new FilterDefinition<>(UserPropertyRef.AGE, Op.LT, 30))
    .filter("hasEmail", new FilterDefinition<>(UserPropertyRef.EMAIL, Op.NOT_NULL, null))
    .combineWith("(active & young) | hasEmail")
    .build();
```

## Troubleshooting

### Common Issues

**1. "Operator X is not supported for property Y"**
- Check that your PropertyReference enum includes the operator in `getSupportedOperators()`

**2. "Property not found" errors**
- Verify your mapping function returns the correct JPA property path
- Ensure entity property names match your mapping

**3. "Type mismatch" errors**
- Ensure the value type matches the property type defined in PropertyReference
- Check for null values where they're not expected

For more help, see our [Troubleshooting Guide](../guides/troubleshooting.md).