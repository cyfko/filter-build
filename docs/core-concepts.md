# Core Concepts

Understanding FilterQL's core concepts will help you leverage its full power and build more maintainable filtering solutions.

## Architecture Overview

FilterQL is built on a clean, layered architecture:

```
┌─────────────────────────────────────────────────────────┐
│                   Your Application                      │
├─────────────────────────────────────────────────────────┤
│              Framework Adapters                         │
│         ┌─────────────┐    ┌─────────────┐             │
│         │   Spring    │    │     JPA     │             │
│         │   Adapter   │    │   Adapter   │             │
│         └─────────────┘    └─────────────┘             │
├─────────────────────────────────────────────────────────┤
│                  FilterQL Core                          │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐       │
│  │ DSL Parser  │ │ Filter Tree │ │ Conditions  │       │
│  └─────────────┘ └─────────────┘ └─────────────┘       │
└─────────────────────────────────────────────────────────┘
```

## Key Components

### 1. FilterRequest

The `FilterRequest` is your entry point. It contains:

- **Filters**: Individual filter definitions
- **Combine Logic**: DSL expression defining how filters combine

```java
FilterRequest<UserProperty> request = FilterRequest.<UserProperty>builder()
    .filter("name-john", UserProperty.NAME, Operator.EQUALS, "John")
    .filter("age-senior", UserProperty.AGE, Operator.GREATER_THAN, 30)
    .filter("dept-eng", UserProperty.DEPARTMENT, Operator.EQUALS, "Engineering")
    .combineWith("name-john AND (age-senior OR dept-eng)")
    .build();
```

#### Filter Components:
- **ID**: Unique identifier for the filter (`"name-john"`)
- **Property**: What to filter on (`UserProperty.NAME`)
- **Operator**: How to compare (`Operator.EQUALS`)
- **Value**: What to compare against (`"John"`)

### 2. Property References

Properties define what can be filtered. They must implement two interfaces:

```java
public enum UserProperty implements PropertyRef, PathShape {
    NAME("name"),
    EMAIL("email"),
    DEPARTMENT_NAME("department.name"),  // Nested property
    TAGS("tags");                        // Collection property
    
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

#### PropertyRef
Provides the property identifier used in enum comparisons.

#### PathShape
Provides the actual property path for framework-specific resolution.

### 3. Operators

Operators define how values are compared:

```java
public enum Operator {
    EQUALS,                 // field = value
    NOT_EQUALS,            // field != value
    GREATER_THAN,          // field > value
    GREATER_THAN_OR_EQUAL, // field >= value
    LESS_THAN,             // field < value
    LESS_THAN_OR_EQUAL,    // field <= value
    CONTAINS,              // field LIKE '%value%'
    STARTS_WITH,           // field LIKE 'value%'
    ENDS_WITH,             // field LIKE '%value'
    IN,                    // field IN (value1, value2, ...)
    NOT_IN,                // field NOT IN (value1, value2, ...)
    IS_NULL,               // field IS NULL
    IS_NOT_NULL            // field IS NOT NULL
}
```

### 4. DSL Parser

The DSL Parser converts human-readable expressions into executable filter trees:

```java
// Input: "active AND (senior OR manager)"
// Output: FilterTree with logical structure

DSLParser parser = new DSLParser();
FilterTree tree = parser.parse("active AND (senior OR manager)");
```

#### Supported Syntax:
- **Logical Operators**: `AND`, `OR`
- **Grouping**: `()` for precedence
- **Identifiers**: Filter IDs referenced in the expression

### 5. Filter Tree

The `FilterTree` represents the parsed logical structure:

```
Expression: "A AND (B OR C)"

Tree Structure:
       AND
      /   \
     A     OR
          /  \
         B    C
```

### 6. Context Adapters

Context adapters bridge the gap between FilterQL's generic concepts and framework-specific implementations:

```java
// Spring Data example
ContextAdapter<User, UserProperty> context = new ContextAdapter<>(
    new ConditionAdapterBuilder<User, UserProperty>() {}
);

// Add all filters to context
request.getFilters().forEach(context::addCondition);

// Generate final condition
Condition condition = tree.generate(context);
```

## Advanced Concepts

### Type Safety

FilterQL provides compile-time type safety through generic constraints:

```java
// This won't compile - wrong property type
FilterRequest<UserProperty> request = FilterRequest.<OrderProperty>builder() // ❌
    .filter("name", UserProperty.NAME, Operator.EQUALS, "John")
    .build();

// This will compile
FilterRequest<UserProperty> request = FilterRequest.<UserProperty>builder() // ✅
    .filter("name", UserProperty.NAME, Operator.EQUALS, "John")
    .build();
```

### Path Resolution

FilterQL intelligently resolves property paths:

```java
public enum UserProperty implements PropertyRef, PathShape {
    // Simple property
    NAME("name"),                    // → user.name
    
    // Nested property
    DEPARTMENT_NAME("department.name"), // → user.department.name
    
    // Deep nesting
    MANAGER_EMAIL("department.manager.email"), // → user.department.manager.email
    
    // Collection properties
    TAGS("tags"),                    // → user.tags (collection)
    ORDER_ITEMS("orders.items")      // → user.orders.items (nested collection)
}
```

### Expression Validation

FilterQL validates DSL expressions and provides meaningful error messages:

```java
try {
    DSLParser parser = new DSLParser();
    FilterTree tree = parser.parse("invalid AND (unclosed");
} catch (DSLSyntaxException e) {
    // Detailed error with position and suggestion
    System.out.println(e.getMessage());
    // "Syntax error at position 15: Expected ')' to close group"
}
```

### Filter Validation

Filters are validated against the available properties:

```java
try {
    FilterRequest<UserProperty> request = FilterRequest.<UserProperty>builder()
        .filter("invalid", UserProperty.NAME, Operator.EQUALS, "John")
        .filter("missing", UserProperty.AGE, Operator.GREATER_THAN, 25)
        .combineWith("invalid AND nonexistent") // ❌ 'nonexistent' not defined
        .build();
} catch (FilterValidationException e) {
    // Lists all validation errors
    e.getErrors().forEach(System.out::println);
}
```

## Best Practices

### 1. Organize Properties Logically

```java
public enum UserProperty implements PropertyRef, PathShape {
    // Basic info
    ID("id"),
    NAME("name"),
    EMAIL("email"),
    
    // Personal details
    AGE("age"),
    BIRTH_DATE("birthDate"),
    
    // Relationships
    DEPARTMENT_NAME("department.name"),
    MANAGER_NAME("manager.name"),
    
    // Collections
    SKILLS("skills"),
    PROJECTS("projects.name");
    
    // ... implementation
}
```

### 2. Use Meaningful Filter IDs

```java
FilterRequest.<UserProperty>builder()
    .filter("senior-developer", UserProperty.LEVEL, Operator.EQUALS, "SENIOR")
    .filter("engineering-dept", UserProperty.DEPARTMENT, Operator.EQUALS, "Engineering")
    .filter("active-status", UserProperty.STATUS, Operator.EQUALS, "ACTIVE")
    .combineWith("active-status AND (senior-developer OR engineering-dept)")
    .build();
```

### 3. Handle Edge Cases

```java
public List<User> searchUsers(FilterRequest<UserProperty> request) {
    // Handle null/empty requests
    if (request == null || request.getFilters().isEmpty()) {
        return userRepository.findAll();
    }
    
    try {
        Specification<User> spec = SpecificationBuilder.toSpecification(request);
        return userRepository.findAll(spec);
    } catch (DSLSyntaxException | FilterValidationException e) {
        // Log error and return empty or throw appropriate exception
        log.warn("Invalid filter request: {}", e.getMessage());
        throw new InvalidFilterException("Invalid filter criteria", e);
    }
}
```

### 4. Cache Parsed Expressions

For frequently used expressions, consider caching:

```java
@Component
public class FilterCache {
    private final Map<String, FilterTree> cache = new ConcurrentHashMap<>();
    private final DSLParser parser = new DSLParser();
    
    public FilterTree getOrParse(String expression) {
        return cache.computeIfAbsent(expression, expr -> {
            try {
                return parser.parse(expr);
            } catch (DSLSyntaxException e) {
                throw new RuntimeException("Invalid expression: " + expr, e);
            }
        });
    }
}
```

## Next Steps

- [JPA Adapter Guide](jpa-adapter.md) - Learn JPA-specific features
- [Spring Data Guide](spring-adapter.md) - Explore Spring integration
- [Advanced Usage](advanced-usage.md) - Custom operators and extensions