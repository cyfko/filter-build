# FilterQL Core Module Overview

The FilterQL Core module provides the foundation for type-safe, dynamic filtering. It's framework-agnostic and can be adapted to work with any persistence technology.

## Architecture

FilterQL Core follows a layered architecture with clear separation of concerns:

```
┌─────────────────────────────────────────┐
│              DSL Layer                  │
│  (Parser, FilterTree, Boolean Logic)   │
├─────────────────────────────────────────┤
│            Validation Layer             │
│   (PropertyReference, Operators, Op)    │
├─────────────────────────────────────────┤
│             Model Layer                 │
│  (FilterDefinition, FilterRequest)      │
├─────────────────────────────────────────┤
│           Execution Layer               │
│  (Context, Condition, PredicateResolver)│
└─────────────────────────────────────────┘
```

## Core Components

### 1. FilterResolver

The main entry point for resolving filter requests into executable predicates.

```java
FilterResolver resolver = FilterResolver.of(context);
PredicateResolver<User> predicate = resolver.resolve(User.class, filterRequest);
```

**Key Responsibilities:**
- Orchestrates the entire filtering pipeline
- Combines DSL parsing, context management, and predicate resolution
- Provides a clean, high-level API

### 2. Parser & FilterTree

Transforms DSL expressions into executable boolean logic.

```java
Parser parser = new DSLParser();
FilterTree tree = parser.parse("(filter1 & filter2) | !filter3");
```

**Supported DSL Syntax:**
- `&` - AND operator (precedence: 2)
- `|` - OR operator (precedence: 1)  
- `!` - NOT operator (precedence: 3)
- `( )` - Grouping parentheses

**Examples:**
```java
// Simple AND
"nameFilter & ageFilter"

// Complex with precedence
"(active & premium) | vip"

// Negation
"!deleted & (active | pending)"
```

### 3. PropertyReference

Type-safe enum interface for defining filterable properties.

```java
public enum UserPropertyRef implements PropertyReference {
    NAME(String.class, Set.of(Op.EQ, Op.MATCHES, Op.IN)),
    AGE(Integer.class, Set.of(Op.EQ, Op.GT, Op.GTE, Op.LT, Op.LTE, Op.RANGE));
    
    // Implementation details...
}
```

**Benefits:**
- Compile-time safety
- Automatic validation
- Clear API contracts
- IDE auto-completion

### 4. Operators (Op)

Comprehensive set of filtering operators with built-in validation.

| Operator | Symbol | Description | Value Type |
|----------|--------|-------------|------------|
| `EQ` | `=` | Equality | Single value |
| `NE` | `!=` | Not equal | Single value |
| `GT` | `>` | Greater than | Single value |
| `GTE` | `>=` | Greater than or equal | Single value |
| `LT` | `<` | Less than | Single value |
| `LTE` | `<=` | Less than or equal | Single value |
| `MATCHES` | `LIKE` | Pattern matching | Single value (with %) |
| `NOT_MATCHES` | `NOT LIKE` | Negated pattern | Single value (with %) |
| `IN` | `IN` | Set membership | Collection |
| `NOT_IN` | `NOT IN` | Not in set | Collection |
| `IS_NULL` | `IS NULL` | Null check | No value needed |
| `NOT_NULL` | `IS NOT NULL` | Not null check | No value needed |
| `RANGE` | `BETWEEN` | Range check | Collection (exactly 2 values) |
| `NOT_RANGE` | `NOT BETWEEN` | Not in range | Collection (exactly 2 values) |

### 5. FilterDefinition & FilterRequest

Type-safe containers for filter data and logic.

```java
// Single filter definition
FilterDefinition<UserPropertyRef> nameFilter = 
    new FilterDefinition<>(UserPropertyRef.NAME, Op.MATCHES, "John%");

// Complete filter request
FilterRequest<UserPropertyRef> request = FilterRequest.builder()
    .filter("nameFilter", nameFilter)
    .filter("ageFilter", ageFilter)
    .combineWith("nameFilter & ageFilter")
    .build();
```

### 6. Context & Condition

Bridge between filter definitions and executable predicates.

```java
public interface Context {
    Condition addCondition(String filterKey, FilterDefinition<?> definition);
    Condition getCondition(String filterKey);
    <E> PredicateResolver<E> toResolver(Class<E> entityClass, Condition condition);
}
```

## Design Patterns Used

### 1. Builder Pattern

Used throughout for complex object construction:

```java
FilterRequest<UserPropertyRef> request = FilterRequest.<UserPropertyRef>builder()
    .filter("filter1", definition1)
    .filter("filter2", definition2)
    .combineWith("filter1 & filter2")
    .build();
```

### 2. Strategy Pattern

PropertyReference allows different validation strategies:

```java
// Text properties support pattern matching
NAME(String.class, Set.of(Op.EQ, Op.MATCHES, Op.IN))

// Numeric properties support range operations
AGE(Integer.class, Set.of(Op.EQ, Op.GT, Op.GTE, Op.LT, Op.LTE, Op.RANGE))
```

### 3. Composite Pattern

Conditions can be combined using boolean logic:

```java
Condition nameCondition = context.getCondition("nameFilter");
Condition ageCondition = context.getCondition("ageFilter");
Condition combined = nameCondition.and(ageCondition);
```

### 4. Template Method Pattern

FilterResolver orchestrates a defined process:

1. Parse DSL expression
2. Populate context with filter definitions
3. Generate condition tree
4. Convert to executable predicate

## Validation System

FilterQL Core includes comprehensive validation at multiple levels:

### Type Validation
```java
// Ensures value type matches property type
UserPropertyRef.AGE.validateOperatorForValue(Op.GT, "invalid"); // Throws exception
UserPropertyRef.AGE.validateOperatorForValue(Op.GT, 25); // OK
```

### Operator Validation
```java
// Ensures operator is supported for property
UserPropertyRef.NAME.validateOperator(Op.RANGE); // Throws exception - RANGE not supported for String
UserPropertyRef.NAME.validateOperator(Op.MATCHES); // OK
```

### Collection Validation
```java
// For IN, NOT_IN operators
UserPropertyRef.STATUS.validateOperatorForValue(Op.IN, List.of(UserStatus.ACTIVE, UserStatus.PENDING)); // OK

// For RANGE, NOT_RANGE operators  
UserPropertyRef.AGE.validateOperatorForValue(Op.RANGE, List.of(18, 65)); // OK
UserPropertyRef.AGE.validateOperatorForValue(Op.RANGE, List.of(18)); // Throws exception - needs exactly 2 values
```

## Error Handling

FilterQL Core provides specific exceptions for different error types:

### DSLSyntaxException
```java
try {
    parser.parse("invalid & & expression");
} catch (DSLSyntaxException e) {
    // Handle syntax errors in DSL
    log.error("Invalid DSL syntax: {}", e.getMessage());
}
```

### FilterValidationException
```java
try {
    propertyRef.validateOperatorForValue(Op.GT, "invalid");
} catch (FilterValidationException e) {
    // Handle validation errors
    log.error("Filter validation failed: {}", e.getMessage());
}
```

## Performance Considerations

### Caching
- Field reflection results are cached in ClassUtils
- Superclass calculations are cached for better performance

### Memory Efficiency
- Immutable objects reduce memory allocation
- Flyweight pattern for operators and validation results

### Lazy Evaluation
- PredicateResolver uses deferred execution
- Conditions are built only when needed

## Extension Points

FilterQL Core is designed for extensibility:

### Custom Operators
```java
// Add new operators by extending Op enum (in future versions)
```

### Custom Validation
```java
public enum CustomPropertyRef implements PropertyReference {
    EMAIL(String.class, Set.of(Op.EQ, Op.MATCHES)) {
        @Override
        public void validateOperatorForValue(Op operator, Object value) {
            super.validateOperatorForValue(operator, value);
            // Add custom email validation
            if (value instanceof String email && !email.contains("@")) {
                throw new FilterValidationException("Invalid email format");
            }
        }
    };
}
```

### Custom Parsers
```java
FilterResolver resolver = FilterResolver.of(new CustomDSLParser(), context);
```

## Best Practices

### PropertyReference Design
```java
public enum UserPropertyRef implements PropertyReference {
    // Use descriptive names
    USER_FULL_NAME(String.class, OperatorUtils.FOR_TEXT),
    
    // Group related operators
    USER_AGE(Integer.class, OperatorUtils.FOR_NUMBER),
    
    // Be specific about supported operations
    USER_STATUS(UserStatus.class, Set.of(Op.EQ, Op.NE, Op.IN));
}
```

### Filter Naming
```java
// Use business-meaningful names
FilterRequest.builder()
    .filter("activeCustomers", activeDefinition)
    .filter("premiumTier", premiumDefinition)
    .filter("recentSignups", recentDefinition)
    .combineWith("activeCustomers & (premiumTier | recentSignups)")
    .build();
```

### Error Handling
```java
public List<User> findUsers(FilterRequest<UserPropertyRef> request) {
    try {
        FilterResolver resolver = FilterResolver.of(context);
        PredicateResolver<User> predicate = resolver.resolve(User.class, request);
        // Execute query...
    } catch (DSLSyntaxException e) {
        // Log and return user-friendly error
        throw new BadRequestException("Invalid filter expression: " + e.getMessage());
    } catch (FilterValidationException e) {
        // Log and return validation error
        throw new BadRequestException("Invalid filter: " + e.getMessage());
    }
}
```

## Next Steps

- **[Learn about Spring Integration](../spring-adapter/overview.md)**
- **[See Core Examples](examples/basic-usage.md)**
- **[Advanced Patterns](examples/advanced-patterns.md)**
- **[API Reference](../api/javadoc/)**