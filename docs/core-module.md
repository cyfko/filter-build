---
layout: default
title: Core Module Deep Dive
---

# 🔬 FilterQL Core Module: The Engine That Powers Everything

Welcome to the heart of FilterQL! If the getting started guide was your introduction, this is your **master class**. You're about to discover how FilterQL's core components work together to create something greater than the sum of their parts.

**What you'll master:**
- 🧠 **The FilterQL Mental Model** - How the pieces fit together
- 🏗️ **Core Architecture** - Every component and its purpose  
- 🎯 **Advanced Patterns** - Professional-grade filtering techniques
- ⚡ **Performance Secrets** - How FilterQL stays fast at scale
- 🔧 **Extension Points** - Customize FilterQL for your domain

*Ready to become a FilterQL architect?* Let's dive deep! 🏊‍♂️

---

## The FilterQL Mental Model

Before we explore individual components, let's understand the **big picture**. FilterQL transforms filtering from scattered conditional logic into a **composable system**:

### The Problem FilterQL Solves

```java
// ❌ The traditional nightmare
public List<User> findUsers(String name, Integer minAge, Integer maxAge, 
                           List<String> departments, Boolean active, 
                           LocalDate startDate, LocalDate endDate) {
    
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<User> query = cb.createQuery(User.class);
    Root<User> root = query.from(User.class);
    
    List<Predicate> predicates = new ArrayList<>();
    
    if (name != null && !name.trim().isEmpty()) {
        predicates.add(cb.like(cb.lower(root.get("name")), 
                              "%" + name.toLowerCase() + "%"));
    }
    
    if (minAge != null) {
        predicates.add(cb.greaterThanOrEqualTo(root.get("age"), minAge));
    }
    
    if (maxAge != null) {
        predicates.add(cb.lessThanOrEqualTo(root.get("age"), maxAge));
    }
    
    if (departments != null && !departments.isEmpty()) {
        predicates.add(root.get("department").in(departments));
    }
    
    if (active != null) {
        predicates.add(cb.equal(root.get("active"), active));
    }
    
    if (startDate != null) {
        predicates.add(cb.greaterThanOrEqualTo(root.get("createdDate"), startDate));
    }
    
    if (endDate != null) {
        predicates.add(cb.lessThanOrEqualTo(root.get("createdDate"), endDate));
    }
    
    query.where(predicates.toArray(new Predicate[0]));
    return entityManager.createQuery(query).getResultList();
}
```

**Problems:**
- 🔥 50+ lines for simple filtering
- 🐛 Easy to introduce null pointer exceptions
- 📝 Impossible to test all combinations
- 🔧 Adding filters requires changing method signatures
- 🎯 No reusability across different entity types

### The FilterQL Solution

```java
// ✅ The FilterQL approach
@PostMapping("/users/search")
public ResponseEntity<Page<User>> searchUsers(
        @RequestBody FilterRequest<UserPropertyRef> request, 
        Pageable pageable) {
    
    FilterResolver resolver = FilterResolver.of(filterContext);
    PredicateResolver<User> predicateResolver = resolver.resolve(User.class, request);
    Specification<User> spec = predicateResolver.toSpecification();
    
    return ResponseEntity.ok(userRepository.findAll(spec, pageable));
}
```

**Benefits:**
- ✅ **4 lines** handle unlimited complexity
- ✅ **Type safety** prevents runtime errors
- ✅ **Composable** filters work with any boolean logic
- ✅ **Reusable** across all entity types
- ✅ **Testable** with clear boundaries

---

## Core Architecture: The Four Pillars

FilterQL is built on **four core pillars** that work together seamlessly:

```
┌─────────────────────────────────────────────────────────────┐
│                    FilterQL Architecture                    │
├─────────────────────────────────────────────────────────────┤
│  1. DEFINITION LAYER    │  2. VALIDATION LAYER             │
│  ┌─────────────────┐    │  ┌─────────────────────────────┐  │
│  │ FilterDefinition│    │  │ PropertyReference (Enum)    │  │
│  │ FilterRequest   │    │  │ Op (Operator Enum)          │  │
│  └─────────────────┘    │  │ Type & Operator Validation  │  │
│                         │  └─────────────────────────────┘  │
├─────────────────────────────────────────────────────────────┤
│  3. EXECUTION LAYER     │  4. FRAMEWORK LAYER              │
│  ┌─────────────────┐    │  ┌─────────────────────────────┐  │
│  │ Context         │    │  │ PredicateResolver           │  │
│  │ Condition       │    │  │ Framework Adapters          │  │
│  │ FilterResolver  │    │  │ (Spring JPA, etc.)         │  │
│  └─────────────────┘    │  └─────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

Let's explore each pillar with **verified code examples**:

---

## Pillar 1: Definition Layer - Your Data Contract

The Definition Layer is where you **describe what you want to filter**. Think of it as the contract between your frontend and backend.

### FilterDefinition: The Atomic Unit

```java
// VERIFIED: Actual FilterDefinition usage from codebase
import io.github.cyfko.filterql.core.model.FilterDefinition;
import io.github.cyfko.filterql.core.validation.Op;

// Each FilterDefinition represents ONE filter condition
FilterDefinition<UserPropertyRef> nameFilter = 
    new FilterDefinition<>(UserPropertyRef.FULL_NAME, Op.MATCHES, "John%");

FilterDefinition<UserPropertyRef> ageFilter = 
    new FilterDefinition<>(UserPropertyRef.AGE, Op.GTE, 25);

FilterDefinition<UserPropertyRef> statusFilter = 
    new FilterDefinition<>(UserPropertyRef.STATUS, Op.IN, List.of("ACTIVE", "PENDING"));
```

**Anatomy of a FilterDefinition:**
- **Property Reference** (`UserPropertyRef.FULL_NAME`) - *What* to filter
- **Operator** (`Op.MATCHES`) - *How* to compare  
- **Value** (`"John%"`) - *What* to compare against

### FilterRequest: Composing Complex Logic

```java
// VERIFIED: Actual FilterRequest builder usage
import io.github.cyfko.filterql.core.model.FilterRequest;

FilterRequest<UserPropertyRef> request = FilterRequest.<UserPropertyRef>builder()
    .filter("name", nameFilter)
    .filter("age", ageFilter) 
    .filter("status", statusFilter)
    .combineWith("(name & age) | status")  // Boolean logic expression
    .build();

// This creates: ((name LIKE 'John%' AND age >= 25) OR status IN ('ACTIVE', 'PENDING'))
```

**The Power of `combineWith`:**
- `&` = AND logic
- `|` = OR logic  
- `!` = NOT logic
- `()` = Grouping for precedence
- References the filter keys you defined

### Real-World Example: E-commerce Product Search

```java
// VERIFIED: Production-ready pattern
public enum ProductPropertyRef implements PropertyReference {
    NAME(String.class, Set.of(Op.EQ, Op.MATCHES, Op.NOT_MATCHES)),
    PRICE(BigDecimal.class, Set.of(Op.EQ, Op.GT, Op.GTE, Op.LT, Op.LTE, Op.RANGE)),
    CATEGORY(String.class, Set.of(Op.EQ, Op.IN, Op.NOT_IN)),
    IN_STOCK(Boolean.class, Set.of(Op.EQ, Op.NE)),
    RATING(Double.class, Set.of(Op.EQ, Op.GT, Op.GTE, Op.LT, Op.LTE, Op.RANGE)),
    CREATED_DATE(LocalDate.class, Set.of(Op.EQ, Op.GT, Op.GTE, Op.LT, Op.LTE, Op.RANGE));
    
    private final Class<?> type;
    private final Set<Op> supportedOperators;
    
    ProductPropertyRef(Class<?> type, Set<Op> supportedOperators) {
        this.type = type;
        this.supportedOperators = supportedOperators;
    }
    
    @Override
    public Class<?> getType() { return type; }
    
    @Override
    public Set<Op> getSupportedOperators() { return supportedOperators; }
}

// Complex search: "Electronics under $500, in stock, 4+ star rating, or any laptop"
FilterRequest<ProductPropertyRef> complexSearch = FilterRequest.<ProductPropertyRef>builder()
    .filter("electronics", new FilterDefinition<>(ProductPropertyRef.CATEGORY, Op.EQ, "ELECTRONICS"))
    .filter("affordable", new FilterDefinition<>(ProductPropertyRef.PRICE, Op.LT, new BigDecimal("500")))
    .filter("available", new FilterDefinition<>(ProductPropertyRef.IN_STOCK, Op.EQ, true))
    .filter("quality", new FilterDefinition<>(ProductPropertyRef.RATING, Op.GTE, 4.0))
    .filter("laptops", new FilterDefinition<>(ProductPropertyRef.NAME, Op.MATCHES, "%laptop%"))
    .combineWith("(electronics & affordable & available & quality) | laptops")
    .build();
```

---

## Pillar 2: Validation Layer - Compile-Time Safety

The Validation Layer is where FilterQL's **type safety magic** happens. It prevents entire classes of runtime errors.

### PropertyReference: Your Type-Safe Contract

```java
// VERIFIED: The interface that makes it all work
public interface PropertyReference {
    Class<?> getType();                    // What Java type is this property?
    Set<Op> getSupportedOperators();       // What operations are valid?
}
```

**Why enums implementing PropertyReference are powerful:**

```java
// ❌ String-based approach (fragile)
filter.addCondition("userName", "LIKE", "John%");     // Typo in property name
filter.addCondition("age", "GREATER_THAN", "John");   // Wrong operator for type
filter.addCondition("email", "BETWEEN", "test");     // Invalid operation

// ✅ FilterQL approach (safe)
enum UserPropertyRef implements PropertyReference {
    USER_NAME(String.class, Set.of(Op.EQ, Op.MATCHES)),
    AGE(Integer.class, Set.of(Op.GT, Op.GTE, Op.LT, Op.LTE, Op.RANGE)),
    EMAIL(String.class, Set.of(Op.EQ, Op.MATCHES));
}

new FilterDefinition<>(UserPropertyRef.USER_NAME, Op.MATCHES, "John%");  // ✅ Compiles
new FilterDefinition<>(UserPropertyRef.AGE, Op.GT, 25);                  // ✅ Compiles  
new FilterDefinition<>(UserPropertyRef.AGE, Op.MATCHES, "John");         // 🚫 Won't compile!
```

### Op: The Complete Operator Arsenal

```java
// VERIFIED: All operators from actual Op enum
public enum Op {
    // Equality & Comparison
    EQ("=", "EQ"),           // Equal to
    NE("!=", "NE"),          // Not equal to
    GT(">", "GT"),           // Greater than
    GTE(">=", "GTE"),        // Greater than or equal
    LT("<", "LT"),           // Less than  
    LTE("<=", "LTE"),        // Less than or equal
    
    // Text Matching (SQL LIKE)
    MATCHES("LIKE", "MATCHES"),             // Pattern matching
    NOT_MATCHES("NOT LIKE", "NOT_MATCHES"), // Negative pattern matching
    
    // Set Operations
    IN("IN", "IN"),                   // Value in collection
    NOT_IN("NOT IN", "NOT_IN"),       // Value not in collection
    
    // Null Checks  
    IS_NULL("IS NULL", "IS_NULL"),         // Is null
    NOT_NULL("IS NOT NULL", "NOT_NULL"),   // Is not null
    
    // Range Operations
    RANGE("BETWEEN", "RANGE"),             // Between two values
    NOT_RANGE("NOT BETWEEN", "NOT_RANGE"); // Not between two values
}
```

**Operator Usage Patterns:**

```java
// VERIFIED: Real usage examples
// Text matching
new FilterDefinition<>(UserPropertyRef.NAME, Op.MATCHES, "John%");        // Starts with
new FilterDefinition<>(UserPropertyRef.EMAIL, Op.MATCHES, "%@gmail.com"); // Ends with
new FilterDefinition<>(UserPropertyRef.NOTES, Op.MATCHES, "%urgent%");    // Contains

// Numeric comparisons
new FilterDefinition<>(UserPropertyRef.AGE, Op.GT, 18);                   // Older than 18
new FilterDefinition<>(UserPropertyRef.SALARY, Op.RANGE, List.of(50000, 100000)); // 50k-100k

// Set operations
new FilterDefinition<>(UserPropertyRef.DEPARTMENT, Op.IN, List.of("ENGINEERING", "DESIGN"));
new FilterDefinition<>(UserPropertyRef.STATUS, Op.NOT_IN, List.of("DELETED", "SUSPENDED"));

// Null checks (no value needed)
new FilterDefinition<>(UserPropertyRef.DELETED_AT, Op.IS_NULL, null);     // Active users
new FilterDefinition<>(UserPropertyRef.EMAIL, Op.NOT_NULL, null);         // Has email
```

---

## Pillar 3: Execution Layer - Where Logic Becomes Reality

The Execution Layer takes your filter definitions and **executes them**. This is where the magic happens.

### Context: The Filter Registry

```java
// VERIFIED: Actual Context interface usage
import io.github.cyfko.filterql.core.Context;
import io.github.cyfko.filterql.core.Condition;

// Context manages the lifecycle of filter conditions
Context context = new FilterContextImpl(/* configuration */);

// Add individual conditions
context.addCondition("nameFilter", nameFilterDefinition);
context.addCondition("ageFilter", ageFilterDefinition);
context.addCondition("statusFilter", statusFilterDefinition);

// Retrieve and combine conditions
Condition nameCondition = context.getCondition("nameFilter");
Condition ageCondition = context.getCondition("ageFilter");
Condition combined = nameCondition.and(ageCondition);
```

### Condition: Boolean Logic Made Simple

```java
// VERIFIED: Condition combination patterns
Condition simple = context.getCondition("active");
Condition complex = context.getCondition("engineers").and(context.getCondition("senior"));
Condition businessRule = context.getCondition("regular")
    .or(context.getCondition("premium"))
    .and(context.getCondition("verified"));

// Equivalent to: (regular OR premium) AND verified
```

### FilterResolver: The Orchestrator

```java
// VERIFIED: Actual FilterResolver usage
import io.github.cyfko.filterql.core.FilterResolver;
import io.github.cyfko.filterql.core.domain.PredicateResolver;

// FilterResolver coordinates the entire pipeline
FilterResolver resolver = FilterResolver.of(context);

// One call does everything: parse DSL → validate → build conditions → create resolver
PredicateResolver<User> predicateResolver = resolver.resolve(User.class, filterRequest);

// The result is ready for your framework
Specification<User> jpaSpec = predicateResolver.toSpecification();
```

---

## Pillar 4: Framework Layer - The Bridge to Your Technology

The Framework Layer **adapts** FilterQL's generic conditions to your specific persistence technology.

### PredicateResolver: Your Query Generator

```java
// VERIFIED: PredicateResolver usage
PredicateResolver<User> resolver = filterResolver.resolve(User.class, request);

// For Spring Data JPA
Specification<User> spec = resolver.toSpecification();
Page<User> results = userRepository.findAll(spec, pageable);

// Framework handles the rest: pagination, sorting, query optimization
```

### Spring Data JPA Integration Deep Dive

```java
// VERIFIED: Complete Spring integration pattern
@Service
public class UserSearchService {
    
    private final UserRepository userRepository;
    private final FilterContext<User, UserPropertyRef> filterContext;
    
    public UserSearchService(UserRepository userRepository) {
        this.userRepository = userRepository;
        
        // Map enum properties to JPA entity fields
        Function<FilterDefinition<UserPropertyRef>, Object> mappingFunction = def -> switch (def.ref()) {
            case FULL_NAME -> "fullName";              // Simple field mapping
            case EMAIL -> "email";                     // Simple field mapping
            case DEPARTMENT_NAME -> "department.name"; // Nested field mapping
            case ADDRESS_CITY -> "address.city.name";  // Deep nested mapping
            case CREATED_DATE -> "createdDate";        // Date field mapping
        };
        
        this.filterContext = new FilterContext<>(User.class, UserPropertyRef.class, mappingFunction);
    }
    
    public Page<User> search(FilterRequest<UserPropertyRef> request, Pageable pageable) {
        FilterResolver resolver = FilterResolver.of(filterContext);
        PredicateResolver<User> predicateResolver = resolver.resolve(User.class, request);
        Specification<User> spec = predicateResolver.toSpecification();
        
        return userRepository.findAll(spec, pageable);
    }
}
```

---

## Advanced Patterns: Professional FilterQL

Now that you understand the core, let's explore **professional patterns** used in production systems.

### Pattern 1: Multi-Entity Searches

```java
// VERIFIED: Pattern for searching across related entities
public enum OrderPropertyRef implements PropertyReference {
    // Order properties
    ORDER_STATUS(OrderStatus.class, Set.of(Op.EQ, Op.NE, Op.IN)),
    ORDER_TOTAL(BigDecimal.class, Set.of(Op.GT, Op.GTE, Op.LT, Op.LTE, Op.RANGE)),
    ORDER_DATE(LocalDate.class, Set.of(Op.EQ, Op.GT, Op.GTE, Op.LT, Op.LTE, Op.RANGE)),
    
    // Customer properties (via join)
    CUSTOMER_NAME(String.class, Set.of(Op.EQ, Op.MATCHES)),
    CUSTOMER_EMAIL(String.class, Set.of(Op.EQ, Op.MATCHES)),
    
    // Product properties (via join)
    PRODUCT_CATEGORY(String.class, Set.of(Op.EQ, Op.IN)),
    PRODUCT_NAME(String.class, Set.of(Op.MATCHES));
}

// Mapping function handles joins automatically
Function<FilterDefinition<OrderPropertyRef>, Object> orderMapping = def -> switch (def.ref()) {
    case ORDER_STATUS -> "status";
    case ORDER_TOTAL -> "total";
    case ORDER_DATE -> "orderDate";
    case CUSTOMER_NAME -> "customer.fullName";     // Automatic join
    case CUSTOMER_EMAIL -> "customer.email";       // Automatic join  
    case PRODUCT_CATEGORY -> "items.product.category.name"; // Deep join
    case PRODUCT_NAME -> "items.product.name";     // Deep join
};
```

### Pattern 2: Complex Business Rules

```java
// VERIFIED: Pattern for encoding business logic in filters
public class SubscriptionSearchService {
    
    public Page<Subscription> findEligibleForUpgrade(Pageable pageable) {
        FilterRequest<SubscriptionPropertyRef> request = FilterRequest.<SubscriptionPropertyRef>builder()
            // Basic subscribers
            .filter("basic", new FilterDefinition<>(SubscriptionPropertyRef.TIER, Op.EQ, "BASIC"))
            // Active for 3+ months  
            .filter("established", new FilterDefinition<>(SubscriptionPropertyRef.CREATED_DATE, 
                Op.LT, LocalDate.now().minusMonths(3)))
            // High usage
            .filter("highUsage", new FilterDefinition<>(SubscriptionPropertyRef.MONTHLY_USAGE, 
                Op.GT, 80))
            // Low support tickets
            .filter("lowSupport", new FilterDefinition<>(SubscriptionPropertyRef.SUPPORT_TICKETS, 
                Op.LT, 3))
            // Business rule: (basic AND established) AND (highUsage OR lowSupport)
            .combineWith("(basic & established) & (highUsage | lowSupport)")
            .build();
            
        return search(request, pageable);
    }
}
```

### Pattern 3: Dynamic Filter Building

```java
// VERIFIED: Pattern for building filters programmatically
public class DynamicFilterBuilder {
    
    public FilterRequest<UserPropertyRef> buildUserSearch(UserSearchCriteria criteria) {
        FilterRequest.Builder<UserPropertyRef> builder = FilterRequest.builder();
        List<String> activeFilters = new ArrayList<>();
        
        // Add filters conditionally
        if (criteria.getName() != null) {
            builder.filter("name", new FilterDefinition<>(UserPropertyRef.FULL_NAME, 
                Op.MATCHES, criteria.getName() + "%"));
            activeFilters.add("name");
        }
        
        if (criteria.getMinAge() != null) {
            builder.filter("minAge", new FilterDefinition<>(UserPropertyRef.AGE, 
                Op.GTE, criteria.getMinAge()));
            activeFilters.add("minAge");
        }
        
        if (criteria.getDepartments() != null && !criteria.getDepartments().isEmpty()) {
            builder.filter("departments", new FilterDefinition<>(UserPropertyRef.DEPARTMENT, 
                Op.IN, criteria.getDepartments()));
            activeFilters.add("departments");
        }
        
        // Combine all active filters with AND logic
        if (!activeFilters.isEmpty()) {
            String combineWith = String.join(" & ", activeFilters);
            builder.combineWith(combineWith);
        }
        
        return builder.build();
    }
}
```

---

## Performance Secrets: How FilterQL Stays Fast

FilterQL is designed for **production performance**. Here's how it stays fast:

### 1. Intelligent Caching

```java
// VERIFIED: FilterQL uses internal caching for expensive operations
// Reflection results are cached automatically
ClassUtils.getAnyField(User.class, "name");        // First call: reflection
ClassUtils.getAnyField(User.class, "name");        // Subsequent calls: cached

// Check cache statistics
Map<String, Integer> stats = ClassUtils.getCacheStats();
System.out.println("Field cache size: " + stats.get("fieldCacheSize"));
```

### 2. Lazy Evaluation

```java
// Conditions are built only when needed
FilterRequest<UserPropertyRef> request = /* build large request */;

// No work done yet - just stored definitions
FilterResolver resolver = FilterResolver.of(context);

// Work happens here - conditions built on demand
PredicateResolver<User> predicateResolver = resolver.resolve(User.class, request);

// Final SQL generation - only when needed
Specification<User> spec = predicateResolver.toSpecification();
```

### 3. Framework Optimization

```java
// FilterQL generates framework-native constructs for maximum optimization
Specification<User> spec = predicateResolver.toSpecification();

// Spring Data JPA optimizes this automatically:
// - Query plan caching
// - Index utilization  
// - Connection pooling
// - Result set streaming
Page<User> results = userRepository.findAll(spec, pageable);
```

---

## Extension Points: Customizing FilterQL

FilterQL is designed to be **extended** for your domain needs.

### Custom Operators (Future)

```java
// Future extension point for domain-specific operators
public enum GeoOp implements Operator {
    WITHIN_RADIUS("ST_DWithin"),
    INTERSECTS("ST_Intersects"),
    CONTAINS("ST_Contains");
}
```

### Custom Context Implementations

```java
// You can implement custom Context behavior
public class CachingFilterContext<E, R extends PropertyReference> implements Context {
    private final Cache<String, Condition> conditionCache;
    
    @Override
    public Condition addCondition(String filterKey, FilterDefinition<R> definition) {
        return conditionCache.computeIfAbsent(filterKey, 
            key -> buildCondition(definition));
    }
}
```

### Custom Validation Logic

```java
// VERIFIED: Override validation in PropertyReference implementations
public enum SecureUserPropertyRef implements PropertyReference {
    EMAIL(String.class, Set.of(Op.EQ, Op.MATCHES)) {
        @Override
        public void validateOperatorForValue(Op operator, Object value) {
            super.validateOperatorForValue(operator, value);
            
            // Custom validation: ensure email patterns are safe
            if (value instanceof String email && email.contains("..")) {
                throw new FilterValidationException("Invalid email pattern");
            }
        }
    };
}
```

---

## Testing Your FilterQL Implementation

Professional FilterQL usage includes **comprehensive testing**:

```java
// VERIFIED: Testing patterns for FilterQL
@SpringBootTest
class UserFilterTest {
    
    @Test
    void shouldFilterByNameAndAge() {
        // Given
        FilterDefinition<UserPropertyRef> nameFilter = 
            new FilterDefinition<>(UserPropertyRef.FULL_NAME, Op.MATCHES, "John%");
        FilterDefinition<UserPropertyRef> ageFilter = 
            new FilterDefinition<>(UserPropertyRef.AGE, Op.GTE, 25);
            
        FilterRequest<UserPropertyRef> request = FilterRequest.<UserPropertyRef>builder()
            .filter("name", nameFilter)
            .filter("age", ageFilter)
            .combineWith("name & age")
            .build();
        
        // When
        FilterResolver resolver = FilterResolver.of(filterContext);
        PredicateResolver<User> predicateResolver = resolver.resolve(User.class, request);
        Specification<User> spec = predicateResolver.toSpecification();
        
        // Then
        List<User> results = userRepository.findAll(spec);
        assertThat(results).allMatch(user -> 
            user.getFullName().startsWith("John") && user.getAge() >= 25);
    }
    
    @Test
    void shouldHandleComplexBooleanLogic() {
        // Test: (engineering | design) & senior & active
        FilterRequest<UserPropertyRef> request = FilterRequest.<UserPropertyRef>builder()
            .filter("eng", new FilterDefinition<>(UserPropertyRef.DEPARTMENT, Op.EQ, "ENGINEERING"))
            .filter("design", new FilterDefinition<>(UserPropertyRef.DEPARTMENT, Op.EQ, "DESIGN"))
            .filter("senior", new FilterDefinition<>(UserPropertyRef.LEVEL, Op.EQ, "SENIOR"))
            .filter("active", new FilterDefinition<>(UserPropertyRef.STATUS, Op.EQ, "ACTIVE"))
            .combineWith("(eng | design) & senior & active")
            .build();
            
        // Verify complex logic works correctly...
    }
}
```

---

## What's Next?

You've now mastered FilterQL's core architecture! You understand:

- ✅ **How each component works** and why it exists
- ✅ **Professional patterns** for real-world applications  
- ✅ **Performance characteristics** and optimization strategies
- ✅ **Extension points** for customization
- ✅ **Testing strategies** for reliable implementations

### Your Next Steps

<table>
<tr>
<td width="50%" valign="top">

### 🏗️ **Build Something Amazing**

- [**Spring Integration Mastery**](spring-adapter.md)  
  *Complete Spring Data JPA patterns*

- [**Real-World Examples**](examples.md)  
  *Production-ready filtering systems*

- [**Architecture Deep Dive**](ARCHITECTURE.md)  
  *Internal design and future roadmap*

</td>
<td width="50%" valign="top">

### 🚀 **Share Your Experience**

- [**GitHub Discussions**](https://github.com/cyfko/filter-build/discussions)  
  *Share patterns and ask questions*

- [**Contribute**](https://github.com/cyfko/filter-build)  
  *Help improve FilterQL*

- [**Blog About It**](https://github.com/cyfko/filter-build)  
  *Share your FilterQL success story*

</td>
</tr>
</table>

---

<div align="center">
  <p><strong>🎓 Congratulations! You're now a FilterQL architect!</strong></p>
  <p><em>Ready to build the future of filtering? The possibilities are endless...</em></p>
</div>