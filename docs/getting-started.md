# Getting Started with FilterQL

## What is FilterQL?

FilterQL is a Java library that simplifies dynamic query filtering by providing an intuitive Domain Specific Language (DSL). Instead of writing complex QueryBuilder chains or Criteria API code, you can express your filtering logic in natural, readable expressions.

## Prerequisites

- Java 21 or higher
- Maven or Gradle build tool
- Spring Boot 3.x (for Spring adapter)
- JPA 3.x (for JPA adapter)

## Installation

### Maven

```xml
<dependencies>
    <!-- Core library (required) -->
    <dependency>
        <groupId>io.github.cyfko</groupId>
        <artifactId>filterql-core</artifactId>
        <version>1.2.0</version>
    </dependency>
    
    <!-- Choose your adapter -->
    
    <!-- For JPA/Hibernate projects -->
    <dependency>
        <groupId>io.github.cyfko</groupId>
        <artifactId>filterql-adapter-jpa</artifactId>
        <version>1.0.1</version>
    </dependency>
    
    <!-- For Spring Data projects -->
    <dependency>
        <groupId>io.github.cyfko</groupId>
        <artifactId>filterql-spring</artifactId>
        <version>1.0.0</version>
    </dependency>
</dependencies>
```

### Gradle

```groovy
dependencies {
    // Core library (required)
    implementation 'io.github.cyfko:filterql-core:1.2.0'
    
    // Choose your adapter
    
    // For JPA/Hibernate projects
    implementation 'io.github.cyfko:filterql-adapter-jpa:1.0.1'
    
    // For Spring Data projects
    implementation 'io.github.cyfko:filterql-spring:1.0.0'
}
```

## Your First Filter

Let's create your first filter with FilterQL. We'll use a simple `User` entity as an example.

### 1. Define Your Entity

```java
@Entity
public class User {
    @Id
    private Long id;
    private String name;
    private String email;
    private Integer age;
    private String status;
    
    @ManyToOne
    private Department department;
    
    // getters and setters...
}

@Entity
public class Department {
    @Id
    private Long id;
    private String name;
    
    // getters and setters...
}
```

### 2. Create Property Enum

Define which properties can be filtered:

```java
public enum UserProperty implements PropertyRef, PathShape {
    NAME("name"),
    EMAIL("email"),
    AGE("age"),
    STATUS("status"),
    DEPARTMENT_NAME("department.name");  // Nested property
    
    private final String path;
    
    UserProperty(String path) {
        this.path = path;
    }
    
    @Override
    public String getPath() {
        return path;
    }
}
```

### 3. Build Your First Filter

```java
// Find all active users named John
FilterRequest<UserProperty> request = FilterRequest.<UserProperty>builder()
    .filter("name-filter", UserProperty.NAME, Operator.EQUALS, "John")
    .filter("status-filter", UserProperty.STATUS, Operator.EQUALS, "ACTIVE")
    .combineWith("name-filter AND status-filter")
    .build();
```

### 4. Execute the Filter

#### With Spring Data:

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

#### With JPA (EntityManager):

```java
@Repository
public class UserDao {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    public List<User> findUsers(FilterRequest<UserProperty> request) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> cq = cb.createQuery(User.class);
        Root<User> root = cq.from(User.class);
        
        // Convert FilterRequest to JPA Predicate
        DSLParser parser = new DSLParser();
        FilterTree tree = parser.parse(request.getCombineWith());
        
        ContextAdapter<User, UserProperty> context = new ContextAdapter<>(
            new ConditionAdapterBuilder<User, UserProperty>() {}
        );
        request.getFilters().forEach(context::addCondition);
        
        Condition condition = tree.generate(context);
        Predicate predicate = ((ConditionAdapter<User>) condition)
            .toPredicate(root, cq, cb);
        
        cq.where(predicate);
        return entityManager.createQuery(cq).getResultList();
    }
}
```

## Available Operators

FilterQL supports these operators out of the box:

- `Operator.EQUALS` - Exact match
- `Operator.NOT_EQUALS` - Not equal
- `Operator.GREATER_THAN` - Greater than
- `Operator.GREATER_THAN_OR_EQUAL` - Greater than or equal
- `Operator.LESS_THAN` - Less than
- `Operator.LESS_THAN_OR_EQUAL` - Less than or equal
- `Operator.CONTAINS` - String contains (case-insensitive)
- `Operator.STARTS_WITH` - String starts with
- `Operator.ENDS_WITH` - String ends with
- `Operator.IN` - Value in list
- `Operator.NOT_IN` - Value not in list
- `Operator.IS_NULL` - Is null
- `Operator.IS_NOT_NULL` - Is not null

## DSL Expressions

Combine your filters with logical operators:

- `AND` - Both conditions must be true
- `OR` - Either condition must be true
- `()` - Grouping for complex logic

### Examples:

```java
// Simple AND
"filter1 AND filter2"

// Simple OR
"filter1 OR filter2"

// Complex grouping
"active AND (senior OR manager)"

// Multiple levels
"department AND (senior OR (lead AND experienced))"
```

## Next Steps

Now that you have the basics, explore these advanced topics:

- [Core Concepts](core-concepts.md) - Deep dive into FilterQL architecture
- [JPA Adapter Guide](jpa-adapter.md) - Advanced JPA usage
- [Spring Data Guide](spring-adapter.md) - Spring-specific features
- [Advanced Usage](advanced-usage.md) - Custom operators and complex scenarios

## Common Patterns

### REST API Integration

```java
@RestController
public class UserController {
    
    @PostMapping("/users/search")
    public List<User> searchUsers(@RequestBody FilterRequest<UserProperty> request) {
        Specification<User> spec = SpecificationBuilder.toSpecification(request);
        return userRepository.findAll(spec);
    }
}
```

### Dynamic Query Building

```java
public FilterRequest<UserProperty> buildDynamicFilter(
    String name, Integer minAge, String department) {
    
    FilterRequest.Builder<UserProperty> builder = FilterRequest.builder();
    List<String> conditions = new ArrayList<>();
    
    if (name != null) {
        builder.filter("name", UserProperty.NAME, Operator.CONTAINS, name);
        conditions.add("name");
    }
    
    if (minAge != null) {
        builder.filter("age", UserProperty.AGE, Operator.GREATER_THAN_OR_EQUAL, minAge);
        conditions.add("age");
    }
    
    if (department != null) {
        builder.filter("dept", UserProperty.DEPARTMENT_NAME, Operator.EQUALS, department);
        conditions.add("dept");
    }
    
    return conditions.isEmpty() ? null : 
        builder.combineWith(String.join(" AND ", conditions)).build();
}
```

Ready to dive deeper? Continue with [Core Concepts](core-concepts.md)!