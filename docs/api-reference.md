# FilterQL API Reference

Complete API reference documentation for FilterQL 2.0.0

## Table of Contents

1. [Core Module API](#core-module-api)
2. [Spring Adapter API](#spring-adapter-api)
3. [Configuration Reference](#configuration-reference)
4. [Exception Handling](#exception-handling)
5. [Utility Classes](#utility-classes)

## Core Module API

### FilterResolver

Main facade for resolving filter requests into executable predicates.

```java
public final class FilterResolver {
    
    /**
     * Creates a FilterResolver instance with the provided context.
     * 
     * @param context The filter context containing mapping logic
     * @return A new FilterResolver instance
     * @throws NullPointerException if context is null
     */
    public static FilterResolver of(Context context)
    
    /**
     * Resolves a filter request into a predicate resolver for the specified entity type.
     * 
     * @param <E> The entity type
     * @param entityType The class of the entity being filtered
     * @param filterRequest The filter request containing conditions and combination logic
     * @return A PredicateResolver that can generate JPA predicates
     * @throws FilterValidationException if filter validation fails
     * @throws DSLSyntaxException if combination syntax is invalid
     */
    public <E> PredicateResolver<E> resolve(Class<E> entityType, FilterRequest<?> filterRequest)
}
```

### FilterRequest

Represents a complete filter request with conditions and combination logic.

```java
public final class FilterRequest<P extends Enum<P> & PropertyReference> {
    
    /**
     * Returns the map of filter definitions keyed by their identifiers.
     */
    public Map<String, FilterDefinition<P>> filters()
    
    /**
     * Returns the combination expression for joining filters.
     */
    public String combineWith()
    
    /**
     * Creates a new builder instance.
     */
    public static <P extends Enum<P> & PropertyReference> Builder<P> builder()
    
    public static class Builder<P extends Enum<P> & PropertyReference> {
        
        /**
         * Adds a filter definition with the specified identifier.
         * 
         * @param id Unique identifier for this filter
         * @param definition The filter definition
         * @return This builder instance
         */
        public Builder<P> filter(String id, FilterDefinition<P> definition)
        
        /**
         * Sets the combination expression for joining filters.
         * 
         * @param expression Boolean expression using filter IDs and operators (&, |, !, parentheses)
         * @return This builder instance
         */
        public Builder<P> combineWith(String expression)
        
        /**
         * Builds the FilterRequest instance.
         * 
         * @return A new FilterRequest
         * @throws IllegalStateException if no filters are defined
         */
        public FilterRequest<P> build()
    }
}
```

### FilterDefinition

Defines a single filter condition with property, operator, and value.

```java
public record FilterDefinition<P extends Enum<P> & PropertyReference>(
    P ref,          // Property reference
    Op operator,    // Comparison operator
    Object value    // Filter value
) {
    
    /**
     * Creates a new FilterDefinition.
     * 
     * @param ref The property reference
     * @param operator The comparison operator
     * @param value The filter value
     * @throws NullPointerException if any parameter is null
     * @throws FilterValidationException if operator is not supported for property
     */
    public FilterDefinition(P ref, Op operator, Object value)
}
```

### PropertyReference

Interface that must be implemented by property reference enums.

```java
public interface PropertyReference {
    
    /**
     * Returns the Java type of this property.
     * 
     * @return The property's type
     */
    Class<?> type();
    
    /**
     * Returns the set of operators supported by this property.
     * 
     * @return Set of supported operators
     */
    Set<Op> supportedOperators();
    
    /**
     * Validates if the given operator and value are valid for this property.
     * Default implementation checks operator support and basic type compatibility.
     * 
     * @param operator The operator to validate
     * @param value The value to validate
     * @return true if valid, false otherwise
     * @throws FilterValidationException for validation errors
     */
    default boolean validate(Op operator, Object value) {
        // Default validation logic
        if (!supportedOperators().contains(operator)) {
            throw new FilterValidationException(
                "Operator " + operator + " not supported for property " + this
            );
        }
        
        if (value == null) {
            throw new FilterValidationException(
                "Value cannot be null for property " + this
            );
        }
        
        return validateType(value) && validateOperatorValue(operator, value);
    }
    
    /**
     * Validates the value type against the property's expected type.
     */
    private boolean validateType(Object value);
    
    /**
     * Validates operator-specific value requirements.
     */
    private boolean validateOperatorValue(Op operator, Object value);
}
```

### Op (Operators)

Enumeration of all supported filter operators.

```java
public enum Op {
    
    // Equality operators
    EQ,     // Equal to
    NE,     // Not equal to
    
    // Comparison operators
    GT,     // Greater than
    GTE,    // Greater than or equal to
    LT,     // Less than
    LTE,    // Less than or equal to
    
    // Pattern matching
    MATCHES, // SQL LIKE pattern matching
    
    // Collection operators
    IN,     // Value is in collection
    
    // Range operators
    RANGE;  // Value is within range (requires List<T> with exactly 2 elements)
    
    /**
     * Returns true if this operator requires a collection value.
     */
    public boolean requiresCollection() {
        return this == IN || this == RANGE;
    }
    
    /**
     * Returns true if this operator is applicable to the given type.
     */
    public boolean isApplicableTo(Class<?> type) {
        return switch (this) {
            case EQ, NE, IN -> true; // Universal operators
            case GT, GTE, LT, LTE, RANGE -> Comparable.class.isAssignableFrom(type);
            case MATCHES -> String.class.isAssignableFrom(type);
        };
    }
}
```

### Context

Interface for providing filter context and mapping logic.

```java
public interface Context {
    
    /**
     * Adds a filter condition to the context.
     * 
     * @param id Unique identifier for the condition
     * @param definition The filter definition
     * @return The created Condition
     * @throws FilterValidationException if definition is invalid
     */
    Condition addCondition(String id, FilterDefinition<?> definition);
    
    /**
     * Retrieves a condition by its identifier.
     * 
     * @param id The condition identifier
     * @return The condition, or null if not found
     */
    Condition getCondition(String id);
    
    /**
     * Returns all conditions in this context.
     * 
     * @return Map of condition ID to Condition
     */
    Map<String, Condition> getConditions();
    
    /**
     * Clears all conditions from this context.
     */
    void clear();
}
```

### Condition

Interface representing a filter condition that can be combined with boolean logic.

```java
public interface Condition {
    
    /**
     * Combines this condition with another using AND logic.
     * 
     * @param other The condition to combine with
     * @return A new combined condition
     */
    Condition and(Condition other);
    
    /**
     * Combines this condition with another using OR logic.
     * 
     * @param other The condition to combine with
     * @return A new combined condition
     */
    Condition or(Condition other);
    
    /**
     * Returns the negation of this condition.
     * 
     * @return A new negated condition
     */
    Condition not();
}
```

### PredicateResolver

Functional interface for resolving conditions into JPA predicates.

```java
@FunctionalInterface
public interface PredicateResolver<E> {
    
    /**
     * Resolves this filter into a JPA Predicate.
     * 
     * @param root The JPA root entity
     * @param query The JPA criteria query
     * @param cb The JPA criteria builder
     * @return A JPA Predicate representing this filter
     */
    Predicate resolve(Root<E> root, CriteriaQuery<?> query, CriteriaBuilder cb);
}
```

## Spring Adapter API

### FilterContext

Spring-specific implementation of Context that maps properties to JPA paths or custom logic.

```java
public class FilterContext<E, P extends Enum<P> & PropertyReference> implements Context {
    
    /**
     * Creates a new FilterContext.
     * 
     * @param entityType The JPA entity type
     * @param propertyType The property reference enum type
     * @param mappingFunction Function that maps property definitions to paths or custom resolvers
     */
    public FilterContext(
        Class<E> entityType,
        Class<P> propertyType,
        Function<FilterDefinition<P>, Object> mappingFunction
    )
    
    /**
     * Returns the entity type for this context.
     */
    public Class<E> getEntityType()
    
    /**
     * Returns the property reference type for this context.
     */
    public Class<P> getPropertyType()
    
    /**
     * Enables or disables validation for this context.
     * 
     * @param enabled true to enable validation, false to disable
     */
    public void setValidationEnabled(boolean enabled)
    
    /**
     * Returns whether validation is enabled.
     */
    public boolean isValidationEnabled()
}
```

### FilterCondition

Spring-specific implementation of Condition that wraps JPA Specifications.

```java
public class FilterCondition<E> implements Condition {
    
    /**
     * Creates a FilterCondition from a Spring Specification.
     * 
     * @param specification The JPA Specification
     */
    public FilterCondition(Specification<E> specification)
    
    /**
     * Returns the underlying Specification.
     */
    public Specification<E> getSpecification()
    
    /**
     * Converts this condition to a PredicateResolver.
     */
    public PredicateResolver<E> toPredicateResolver()
}
```

### PredicateResolverMapping

Interface for providing custom predicate resolution logic.

```java
public interface PredicateResolverMapping<E, P extends Enum<P> & PropertyReference> {
    
    /**
     * Resolves a filter definition into a PredicateResolver.
     * 
     * @param definition The filter definition to resolve
     * @return A PredicateResolver for this definition
     */
    PredicateResolver<E> resolve(FilterDefinition<P> definition);
}
```

## Configuration Reference

### OperatorUtils

Utility class providing common operator sets for different data types.

```java
public final class OperatorUtils {
    
    /**
     * All operators supported by FilterQL.
     */
    public static final Set<Op> ALL_OPERATORS = Set.of(Op.values());
    
    /**
     * Operators suitable for text/string properties.
     */
    public static final Set<Op> FOR_TEXT = Set.of(Op.EQ, Op.NE, Op.MATCHES, Op.IN);
    
    /**
     * Operators suitable for numeric properties.
     */
    public static final Set<Op> FOR_NUMBER = Set.of(Op.EQ, Op.NE, Op.GT, Op.GTE, Op.LT, Op.LTE, Op.RANGE, Op.IN);
    
    /**
     * Operators suitable for date/time properties.
     */
    public static final Set<Op> FOR_DATE = Set.of(Op.EQ, Op.NE, Op.GT, Op.GTE, Op.LT, Op.LTE, Op.RANGE, Op.IN);
    
    /**
     * Operators suitable for boolean properties.
     */
    public static final Set<Op> FOR_BOOLEAN = Set.of(Op.EQ, Op.NE);
    
    /**
     * Operators suitable for enum properties.
     */
    public static final Set<Op> FOR_ENUM = Set.of(Op.EQ, Op.NE, Op.IN);
}
```

### Common Property Reference Patterns

```java
// Example: User property references
public enum UserPropertyRef implements PropertyReference {
    // Text properties
    NAME(String.class, OperatorUtils.FOR_TEXT),
    EMAIL(String.class, OperatorUtils.FOR_TEXT),
    
    // Numeric properties
    AGE(Integer.class, OperatorUtils.FOR_NUMBER),
    SALARY(BigDecimal.class, OperatorUtils.FOR_NUMBER),
    
    // Date properties
    HIRE_DATE(LocalDateTime.class, OperatorUtils.FOR_DATE),
    BIRTH_DATE(LocalDate.class, OperatorUtils.FOR_DATE),
    
    // Boolean properties
    IS_ACTIVE(Boolean.class, OperatorUtils.FOR_BOOLEAN),
    IS_VERIFIED(Boolean.class, OperatorUtils.FOR_BOOLEAN),
    
    // Enum properties
    STATUS(UserStatus.class, OperatorUtils.FOR_ENUM),
    ROLE(UserRole.class, OperatorUtils.FOR_ENUM),
    
    // Custom validation example
    PHONE_NUMBER(String.class, Set.of(Op.EQ, Op.MATCHES)) {
        @Override
        public boolean validate(Op operator, Object value) {
            if (!super.validate(operator, value)) return false;
            
            if (value instanceof String phone) {
                // Custom phone number validation
                return phone.matches("^[+]?[1-9]\\d{1,14}$");
            }
            return false;
        }
    };
    
    private final Class<?> type;
    private final Set<Op> operators;
    
    UserPropertyRef(Class<?> type, Set<Op> operators) {
        this.type = type;
        this.operators = operators;
    }
    
    @Override
    public Class<?> type() { return type; }
    
    @Override
    public Set<Op> supportedOperators() { return operators; }
}
```

### FilterContext Configuration Patterns

```java
@Configuration
public class FilterConfiguration {
    
    // Simple path mapping
    @Bean
    public FilterContext<User, UserPropertyRef> userFilterContext() {
        return new FilterContext<>(
            User.class,
            UserPropertyRef.class,
            definition -> switch (definition.ref()) {
                case NAME -> "name";
                case EMAIL -> "email";
                case AGE -> "age";
                case HIRE_DATE -> "hireDate";
                case DEPARTMENT_NAME -> "department.name"; // Join navigation
            }
        );
    }
    
    // Mixed mapping with custom logic
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
                
                // Custom business logic
                case IS_URGENT -> createUrgentOrderMapping();
                case CUSTOMER_TIER -> createCustomerTierMapping();
                
                default -> throw new UnsupportedOperationException(
                    "Property not supported: " + definition.ref()
                );
            }
        );
    }
    
    private static PredicateResolverMapping<Order, OrderPropertyRef> createUrgentOrderMapping() {
        return definition -> (root, query, cb) -> {
            boolean isUrgent = (Boolean) definition.value();
            LocalDateTime threshold = LocalDateTime.now().plusDays(1);
            
            if (isUrgent) {
                return cb.or(
                    cb.equal(root.get("priority"), Priority.HIGH),
                    cb.lessThan(root.get("dueDate"), threshold)
                );
            } else {
                return cb.and(
                    cb.notEqual(root.get("priority"), Priority.HIGH),
                    cb.greaterThanOrEqualTo(root.get("dueDate"), threshold)
                );
            }
        };
    }
}
```

## Exception Handling

### FilterValidationException

Thrown when filter validation fails.

```java
public class FilterValidationException extends RuntimeException {
    
    /**
     * Creates an exception with the specified message.
     */
    public FilterValidationException(String message)
    
    /**
     * Creates an exception with message and cause.
     */
    public FilterValidationException(String message, Throwable cause)
    
    /**
     * Returns the property reference that caused the validation failure, if available.
     */
    public Optional<PropertyReference> getPropertyReference()
    
    /**
     * Returns the operator that caused the validation failure, if available.
     */
    public Optional<Op> getOperator()
    
    /**
     * Returns the value that caused the validation failure, if available.
     */
    public Optional<Object> getValue()
}
```

### DSLSyntaxException

Thrown when filter combination syntax is invalid.

```java
public class DSLSyntaxException extends RuntimeException {
    
    /**
     * Creates an exception with the specified message.
     */
    public DSLSyntaxException(String message)
    
    /**
     * Creates an exception with message and cause.
     */
    public DSLSyntaxException(String message, Throwable cause)
    
    /**
     * Returns the invalid expression that caused the error.
     */
    public String getInvalidExpression()
    
    /**
     * Returns the position in the expression where the error occurred, if available.
     */
    public OptionalInt getErrorPosition()
}
```

### Exception Handling Best Practices

```java
@ControllerAdvice
public class FilterExceptionHandler {
    
    @ExceptionHandler(FilterValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(FilterValidationException e) {
        ErrorResponse response = ErrorResponse.builder()
            .code("FILTER_VALIDATION_ERROR")
            .message("Filter validation failed")
            .details(e.getMessage())
            .timestamp(Instant.now())
            .build();
            
        if (e.getPropertyReference().isPresent()) {
            response.addMetadata("property", e.getPropertyReference().get().toString());
        }
        if (e.getOperator().isPresent()) {
            response.addMetadata("operator", e.getOperator().get().toString());
        }
        
        return ResponseEntity.badRequest().body(response);
    }
    
    @ExceptionHandler(DSLSyntaxException.class)
    public ResponseEntity<ErrorResponse> handleDSLSyntax(DSLSyntaxException e) {
        ErrorResponse response = ErrorResponse.builder()
            .code("FILTER_SYNTAX_ERROR")
            .message("Filter combination syntax is invalid")
            .details(e.getMessage())
            .timestamp(Instant.now())
            .build();
            
        response.addMetadata("expression", e.getInvalidExpression());
        if (e.getErrorPosition().isPresent()) {
            response.addMetadata("errorPosition", e.getErrorPosition().getAsInt());
        }
        
        return ResponseEntity.badRequest().body(response);
    }
}
```

## Utility Classes

### FilterTree

Internal class representing the parsed filter combination tree.

```java
public abstract class FilterTree {
    
    /**
     * Evaluates this tree node to produce a Condition.
     * 
     * @param context The filter context
     * @return The evaluated Condition
     */
    public abstract Condition evaluate(Context context);
    
    /**
     * Parses a combination expression into a FilterTree.
     * 
     * @param expression The boolean expression
     * @return The parsed tree
     * @throws DSLSyntaxException if syntax is invalid
     */
    public static FilterTree parse(String expression);
}
```

### Parser

Internal parser for filter combination expressions.

```java
public final class Parser {
    
    /**
     * Parses a filter combination expression.
     * 
     * @param expression The expression to parse
     * @return The parsed FilterTree
     * @throws DSLSyntaxException if parsing fails
     */
    public static FilterTree parse(String expression);
    
    /**
     * Validates an expression syntax without parsing.
     * 
     * @param expression The expression to validate
     * @return true if syntax is valid
     */
    public static boolean isValidSyntax(String expression);
}
```

### ClassUtils

Utility methods for type handling and validation.

```java
public final class ClassUtils {
    
    /**
     * Checks if a value is compatible with the expected type.
     * 
     * @param value The value to check
     * @param expectedType The expected type
     * @return true if compatible
     */
    public static boolean isCompatibleType(Object value, Class<?> expectedType);
    
    /**
     * Checks if a type is a numeric type supported by FilterQL.
     * 
     * @param type The type to check
     * @return true if numeric
     */
    public static boolean isNumericType(Class<?> type);
    
    /**
     * Checks if a type is a date/time type supported by FilterQL.
     * 
     * @param type The type to check
     * @return true if date/time
     */
    public static boolean isDateTimeType(Class<?> type);
    
    /**
     * Converts a value to the specified type if possible.
     * 
     * @param value The value to convert
     * @param targetType The target type
     * @return The converted value
     * @throws IllegalArgumentException if conversion is not possible
     */
    public static <T> T convertValue(Object value, Class<T> targetType);
}
```

## Type Safety Guidelines

### Compile-Time Safety

FilterQL provides strong compile-time type safety through:

1. **Enum-based Property References**: All filterable properties must be defined as enum constants
2. **Generic Type Parameters**: FilterRequest, FilterContext, and related classes are parameterized
3. **Operator Validation**: Each property defines its supported operators at compile time

### Runtime Validation

Additional runtime validation includes:

1. **Value Type Checking**: Values are validated against property types
2. **Operator Compatibility**: Operators are checked against property definitions
3. **Collection Value Validation**: IN and RANGE operators validate collection contents
4. **Custom Validation**: PropertyReference.validate() allows custom business rules

### Best Practices for Type Safety

```java
// ✅ Good: Strong typing with validation
public enum ProductPropertyRef implements PropertyReference {
    PRICE(BigDecimal.class, OperatorUtils.FOR_NUMBER) {
        @Override
        public boolean validate(Op operator, Object value) {
            if (!super.validate(operator, value)) return false;
            
            // Custom validation: price must be positive
            if (value instanceof BigDecimal price && price.compareTo(BigDecimal.ZERO) <= 0) {
                throw new FilterValidationException("Price must be positive");
            }
            return true;
        }
    },
    
    CATEGORY(ProductCategory.class, OperatorUtils.FOR_ENUM),
    
    NAME(String.class, OperatorUtils.FOR_TEXT) {
        @Override
        public boolean validate(Op operator, Object value) {
            if (!super.validate(operator, value)) return false;
            
            // Custom validation: name length limits
            if (value instanceof String name && name.length() > 100) {
                throw new FilterValidationException("Name cannot exceed 100 characters");
            }
            return true;
        }
    };
}

// ✅ Good: Type-safe filter building
FilterRequest<ProductPropertyRef> request = FilterRequest.<ProductPropertyRef>builder()
    .filter("price", new FilterDefinition<>(ProductPropertyRef.PRICE, Op.RANGE, 
        Arrays.asList(new BigDecimal("10"), new BigDecimal("100"))))
    .filter("category", new FilterDefinition<>(ProductPropertyRef.CATEGORY, Op.IN,
        Arrays.asList(ProductCategory.ELECTRONICS, ProductCategory.BOOKS)))
    .combineWith("price & category")
    .build();

// ❌ Bad: Would cause compile-time error
// FilterDefinition<ProductPropertyRef> invalid = 
//     new FilterDefinition<>(ProductPropertyRef.PRICE, Op.MATCHES, "invalid"); // Wrong operator for numeric type
```

## Performance Considerations

### Query Optimization

1. **Use Fetch Joins**: Prevent N+1 queries with proper join strategies
2. **Index Strategy**: Create database indexes based on filter usage patterns
3. **Projection Queries**: Use DTOs or projections for large result sets
4. **Caching**: Cache frequently used filter results

### Memory Management

1. **Stateless Design**: FilterResolver and Context are thread-safe
2. **Context Reuse**: Reuse FilterContext instances across requests
3. **Lazy Evaluation**: Conditions are evaluated only when needed

### Best Practices

```java
// ✅ Good: Optimized for performance
@Configuration
public class PerformanceOptimizedConfig {
    
    @Bean
    @Scope("singleton") // Reuse context instances
    public FilterContext<User, UserPropertyRef> userFilterContext() {
        return new FilterContext<>(
            User.class,
            UserPropertyRef.class,
            definition -> switch (definition.ref()) {
                case NAME -> "name";
                case DEPARTMENT_NAME -> createOptimizedDepartmentMapping(); // With fetch joins
            }
        );
    }
    
    private static PredicateResolverMapping<User, UserPropertyRef> createOptimizedDepartmentMapping() {
        return definition -> (root, query, cb) -> {
            // Add fetch join to prevent N+1 queries
            if (query.getResultType() == User.class) {
                root.fetch("department", JoinType.LEFT);
            }
            
            Join<User, Department> deptJoin = root.join("department", JoinType.LEFT);
            return cb.equal(deptJoin.get("name"), definition.value());
        };
    }
}

// ✅ Good: Caching strategy
@Service
public class CachedFilterService {
    
    @Cacheable(value = "filterResults", key = "#request.hashCode()")
    public Page<User> searchUsers(FilterRequest<UserPropertyRef> request, Pageable pageable) {
        // FilterQL processing...
    }
}
```

This API reference provides complete documentation for all public interfaces and classes in FilterQL, along with configuration patterns, best practices, and performance considerations.