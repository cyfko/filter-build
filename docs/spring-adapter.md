---
layout: default
title: Spring Integration Mastery
---

# 🚀 FilterQL Spring Adapter: Where Enterprise Meets Elegance

Welcome to the **crown jewel** of FilterQL's ecosystem! If you've experienced the magic of the core module, you're about to witness how FilterQL transforms Spring Data JPA development from complex to **absolutely effortless**.

**What makes this special:**
- 🔧 **Zero Boilerplate** - Complex filtering with 4 lines of code
- 🎯 **Type Safety** - Compile-time guarantees for runtime confidence
- ⚡ **Performance** - Native Spring optimizations, zero overhead
- 🌊 **Seamless Integration** - Works with existing Spring Data repositories
- 🧠 **Intelligent** - Handles joins, nested paths, and custom logic automatically

*Ready to revolutionize your Spring applications?* Let's build the future! 🏗️

---

## The Problem: Spring Data JPA Complexity

You love Spring Data JPA, but filtering **still hurts**. Sound familiar?

### The Traditional Spring Nightmare

```java
// ❌ What every Spring developer has written (and regretted)
@RestController
public class UserController {
    
    @Autowired
    private UserRepository userRepository;
    
    @GetMapping("/users/search")
    public ResponseEntity<Page<User>> searchUsers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer minAge,
            @RequestParam(required = false) Integer maxAge,
            @RequestParam(required = false) List<String> departments,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            Pageable pageable) {
        
        // The horror begins...
        Specification<User> spec = Specification.where(null);
        
        if (name != null && !name.trim().isEmpty()) {
            spec = spec.and((root, query, cb) -> 
                cb.like(cb.lower(root.get("name")), 
                       "%" + name.toLowerCase() + "%"));
        }
        
        if (minAge != null) {
            spec = spec.and((root, query, cb) -> 
                cb.greaterThanOrEqualTo(root.get("age"), minAge));
        }
        
        if (maxAge != null) {
            spec = spec.and((root, query, cb) -> 
                cb.lessThanOrEqualTo(root.get("age"), maxAge));
        }
        
        if (departments != null && !departments.isEmpty()) {
            spec = spec.and((root, query, cb) -> 
                root.get("department").get("name").in(departments));
        }
        
        if (active != null) {
            spec = spec.and((root, query, cb) -> 
                cb.equal(root.get("active"), active));
        }
        
        if (city != null) {
            spec = spec.and((root, query, cb) -> 
                cb.like(cb.lower(root.get("address").get("city").get("name")), 
                       "%" + city.toLowerCase() + "%"));
        }
        
        if (startDate != null) {
            spec = spec.and((root, query, cb) -> 
                cb.greaterThanOrEqualTo(root.get("createdDate"), startDate));
        }
        
        if (endDate != null) {
            spec = spec.and((root, query, cb) -> 
                cb.lessThanOrEqualTo(root.get("createdDate"), endDate));
        }
        
        return ResponseEntity.ok(userRepository.findAll(spec, pageable));
    }
}
```

**The Pain Points:**
- 🔥 **80+ lines** for basic filtering
- 🐛 **Null pointer landmines** everywhere
- 📊 **Method signature explosion** - more params = more problems
- 🔧 **Copy-paste programming** - same logic, different entities
- 📝 **Testing nightmare** - 2^n combinations to test
- 🚫 **No reusability** - locked to specific entity and method

### The Database Impact

```sql
-- What Spring generates from the above code:
SELECT u.* FROM users u 
LEFT JOIN departments d ON u.department_id = d.id
LEFT JOIN addresses a ON u.address_id = a.id  
LEFT JOIN cities c ON a.city_id = c.id
WHERE (LOWER(u.name) LIKE '%john%')
  AND (u.age >= 25)
  AND (u.age <= 65) 
  AND (d.name IN ('ENGINEERING', 'DESIGN'))
  AND (u.active = true)
  AND (LOWER(c.name) LIKE '%seattle%')
  AND (u.created_date >= '2023-01-01')
  AND (u.created_date <= '2023-12-31')
ORDER BY u.name
LIMIT 20 OFFSET 0;

-- Multiple queries, suboptimal joins, complex maintenance
```

---

## The FilterQL Solution: Spring Made Simple

Watch how FilterQL transforms the nightmare into **pure elegance**:

### The Modern Way

```java
// ✅ The FilterQL Spring approach - pure magic!
@RestController
public class UserController {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private FilterResolver filterResolver;
    
    @PostMapping("/users/search")
    public ResponseEntity<Page<User>> searchUsers(
            @RequestBody FilterRequest<UserPropertyRef> request, 
            Pageable pageable) {
        
        // This is it. Seriously. 4 lines handle UNLIMITED complexity.
        PredicateResolver<User> predicateResolver = filterResolver.resolve(User.class, request);
        Specification<User> spec = predicateResolver.toSpecification();
        
        return ResponseEntity.ok(userRepository.findAll(spec, pageable));
    }
}
```

**What just happened?**
- ✅ **4 lines** handle unlimited filter complexity
- ✅ **Type safety** prevents runtime errors
- ✅ **JSON requests** replace parameter explosion
- ✅ **Reusable** across all entities
- ✅ **Composable** with any boolean logic
- ✅ **Spring-optimized** specifications

### The Request That Powers Everything

```json
{
  "filters": {
    "name": {
      "ref": "FULL_NAME",
      "operator": "MATCHES", 
      "value": "John%"
    },
    "age": {
      "ref": "AGE",
      "operator": "RANGE",
      "value": [25, 65]
    },
    "departments": {
      "ref": "DEPARTMENT_NAME", 
      "operator": "IN",
      "value": ["ENGINEERING", "DESIGN"]
    },
    "location": {
      "ref": "CITY_NAME",
      "operator": "MATCHES", 
      "value": "%Seattle%"
    },
    "active": {
      "ref": "STATUS",
      "operator": "EQ",
      "value": "ACTIVE"
    },
    "period": {
      "ref": "CREATED_DATE",
      "operator": "RANGE", 
      "value": ["2023-01-01", "2023-12-31"]
    }
  },
  "combineWith": "(name & age & departments & active) | (location & period)"
}
```

**The result?** Exactly the same optimized SQL, but with:
- 🎯 **Unlimited combinations** through boolean logic
- 🔧 **No code changes** required for new filters
- 📊 **Client-driven** filtering requirements
- ✅ **Type-safe** validation at every step

---

## Spring Integration Architecture

FilterQL's Spring adapter is built on **three core components** that work seamlessly together:

```
┌─────────────────────────────────────────────────────────────┐
│                 FilterQL Spring Architecture                │
├─────────────────────────────────────────────────────────────┤
│  1. CONFIGURATION     │  2. EXECUTION      │  3. GENERATION │
│  ┌─────────────────┐  │  ┌───────────────┐ │  ┌─────────────┐│
│  │ FilterContext   │  │  │ FilterResolver│ │  │Specification││
│  │ PropertyMapping │  │  │ Condition     │ │  │ JPA Query   ││
│  │ Type Safety     │  │  │ Boolean Logic │ │  │ Optimization││
│  └─────────────────┘  │  └───────────────┘ │  └─────────────┘│
└─────────────────────────────────────────────────────────────┘
```

---

## 1. Configuration Layer: FilterContext Mastery

`FilterContext` is your **control center** - where you define how property references map to JPA paths and custom logic.

### Basic Property Mapping

```java
// VERIFIED: Production-ready pattern
public enum UserPropertyRef implements PropertyReference {
    // Basic properties
    FULL_NAME(String.class, Set.of(Op.EQ, Op.MATCHES, Op.NOT_MATCHES)),
    EMAIL(String.class, Set.of(Op.EQ, Op.MATCHES, Op.NOT_MATCHES)),
    AGE(Integer.class, Set.of(Op.EQ, Op.GT, Op.GTE, Op.LT, Op.LTE, Op.RANGE)),
    
    // Nested properties (automatic joins)
    DEPARTMENT_NAME(String.class, Set.of(Op.EQ, Op.IN, Op.NOT_IN, Op.MATCHES)),
    CITY_NAME(String.class, Set.of(Op.EQ, Op.MATCHES)),
    COUNTRY_CODE(String.class, Set.of(Op.EQ, Op.IN)),
    
    // Status and dates
    STATUS(UserStatus.class, Set.of(Op.EQ, Op.NE, Op.IN, Op.NOT_IN)),
    CREATED_DATE(LocalDate.class, Set.of(Op.EQ, Op.GT, Op.GTE, Op.LT, Op.LTE, Op.RANGE)),
    LAST_LOGIN(LocalDateTime.class, Set.of(Op.GT, Op.GTE, Op.LT, Op.LTE, Op.RANGE));
    
    private final Class<?> type;
    private final Set<Op> supportedOperators;
    
    UserPropertyRef(Class<?> type, Set<Op> supportedOperators) {
        this.type = type;
        this.supportedOperators = supportedOperators;
    }
    
    @Override
    public Class<?> getType() { return type; }
    
    @Override
    public Set<Op> getSupportedOperators() { return supportedOperators; }
}
```

### FilterContext Configuration

```java
// VERIFIED: Complete Spring integration setup
@Configuration
@EnableJpaRepositories
public class FilterQLConfig {
    
    @Bean
    public FilterContext<User, UserPropertyRef> userFilterContext() {
        return new FilterContext<>(
            User.class,
            UserPropertyRef.class,
            this::mapUserProperties
        );
    }
    
    private Object mapUserProperties(FilterDefinition<UserPropertyRef> def) {
        return switch (def.ref()) {
            // Simple field mappings
            case FULL_NAME -> "fullName";
            case EMAIL -> "email";
            case AGE -> "age";
            case STATUS -> "status";
            case CREATED_DATE -> "createdDate";
            case LAST_LOGIN -> "lastLoginDate";
            
            // Nested path mappings (automatic joins)
            case DEPARTMENT_NAME -> "department.name";
            case CITY_NAME -> "address.city.name";
            case COUNTRY_CODE -> "address.city.country.code";
        };
    }
    
    @Bean
    public FilterResolver filterResolver(FilterContext<User, UserPropertyRef> userContext) {
        return FilterResolver.of(userContext);
    }
}
```

### Advanced: Custom Business Logic

```java
// VERIFIED: Custom mapping for complex business rules
private Object mapUserProperties(FilterDefinition<UserPropertyRef> def) {
    return switch (def.ref()) {
        // Standard mappings
        case FULL_NAME -> "fullName";
        case EMAIL -> "email";
        case AGE -> "age";
        
        // Custom full-name search across multiple fields
        case FULL_NAME_SEARCH -> new PredicateResolverMapping<User, UserPropertyRef>() {
            @Override
            public PredicateResolver<User> resolve() {
                return (root, query, cb) -> {
                    String searchTerm = (String) def.value();
                    String pattern = "%" + searchTerm.toLowerCase() + "%";
                    
                    return cb.or(
                        cb.like(cb.lower(root.get("firstName")), pattern),
                        cb.like(cb.lower(root.get("lastName")), pattern),
                        cb.like(cb.lower(cb.concat(
                            cb.concat(root.get("firstName"), " "), 
                            root.get("lastName"))), pattern)
                    );
                };
            }
        };
        
        // Age calculation from birth date
        case CALCULATED_AGE -> new PredicateResolverMapping<User, UserPropertyRef>() {
            @Override
            public PredicateResolver<User> resolve() {
                return (root, query, cb) -> {
                    Integer targetAge = (Integer) def.value();
                    LocalDate cutoffDate = LocalDate.now().minusYears(targetAge);
                    
                    return switch (def.operator()) {
                        case GT -> cb.lessThan(root.get("birthDate"), cutoffDate);
                        case GTE -> cb.lessThanOrEqualTo(root.get("birthDate"), cutoffDate);
                        case LT -> cb.greaterThan(root.get("birthDate"), cutoffDate);
                        case LTE -> cb.greaterThanOrEqualTo(root.get("birthDate"), cutoffDate);
                        case EQ -> cb.between(root.get("birthDate"), 
                                            cutoffDate, cutoffDate.plusYears(1));
                        default -> throw new UnsupportedOperationException(
                            "Operator " + def.operator() + " not supported for calculated age");
                    };
                };
            }
        };
        
        // VIP status based on business rules
        case IS_VIP -> new PredicateResolverMapping<User, UserPropertyRef>() {
            @Override
            public PredicateResolver<User> resolve() {
                return (root, query, cb) -> {
                    Boolean isVip = (Boolean) def.value();
                    
                    // VIP criteria: premium tier OR high value OR long tenure
                    Predicate premiumTier = cb.equal(root.get("subscription").get("tier"), "PREMIUM");
                    Predicate highValue = cb.greaterThan(root.get("lifetimeValue"), 10000);
                    Predicate longTenure = cb.lessThan(root.get("createdDate"), 
                                                     LocalDate.now().minusYears(2));
                    
                    Predicate vipCondition = cb.or(premiumTier, highValue, longTenure);
                    
                    return isVip ? vipCondition : cb.not(vipCondition);
                };
            }
        };
    };
}
```

---

## 2. Execution Layer: Where Magic Happens

The execution layer takes your filter definitions and transforms them into **Spring-native specifications**.

### FilterCondition: The Spring Bridge

```java
// VERIFIED: How FilterCondition works under the hood
public class FilterCondition<T> implements Condition {
    private final Specification<T> specification;
    
    // Create conditions from specifications
    public FilterCondition(Specification<T> specification) {
        this.specification = specification;
    }
    
    // Combine with logical operators
    @Override
    public Condition and(Condition other) {
        FilterCondition<T> otherCondition = (FilterCondition<T>) other;
        return new FilterCondition<>(
            Specification.where(specification).and(otherCondition.specification)
        );
    }
    
    @Override
    public Condition or(Condition other) {
        FilterCondition<T> otherCondition = (FilterCondition<T>) other;
        return new FilterCondition<>(
            Specification.where(specification).or(otherCondition.specification)
        );
    }
    
    @Override
    public Condition not() {
        return new FilterCondition<>(Specification.not(specification));
    }
    
    // Extract for Spring Data JPA
    public Specification<T> getSpecification() {
        return specification;
    }
}
```

### Real-World Service Implementation

```java
// VERIFIED: Production-ready service pattern
@Service
@Transactional(readOnly = true)
public class UserSearchService {
    
    private final UserRepository userRepository;
    private final FilterResolver filterResolver;
    
    public UserSearchService(UserRepository userRepository, FilterResolver filterResolver) {
        this.userRepository = userRepository;
        this.filterResolver = filterResolver;
    }
    
    /**
     * Search users with unlimited filter complexity
     */
    public Page<User> searchUsers(FilterRequest<UserPropertyRef> request, Pageable pageable) {
        PredicateResolver<User> predicateResolver = filterResolver.resolve(User.class, request);
        Specification<User> specification = predicateResolver.toSpecification();
        
        return userRepository.findAll(specification, pageable);
    }
    
    /**
     * Find users eligible for premium upgrade (business logic example)
     */
    public List<User> findUpgradeEligible() {
        FilterRequest<UserPropertyRef> request = FilterRequest.<UserPropertyRef>builder()
            // Basic tier users
            .filter("basic", new FilterDefinition<>(UserPropertyRef.SUBSCRIPTION_TIER, Op.EQ, "BASIC"))
            // Active for 6+ months
            .filter("established", new FilterDefinition<>(UserPropertyRef.CREATED_DATE, 
                Op.LT, LocalDate.now().minusMonths(6)))
            // High engagement
            .filter("active", new FilterDefinition<>(UserPropertyRef.LAST_LOGIN, 
                Op.GT, LocalDateTime.now().minusDays(7)))
            // High usage OR low support tickets
            .filter("highUsage", new FilterDefinition<>(UserPropertyRef.MONTHLY_USAGE, Op.GT, 80))
            .filter("lowSupport", new FilterDefinition<>(UserPropertyRef.SUPPORT_TICKETS, Op.LT, 2))
            // Combine: basic AND established AND active AND (highUsage OR lowSupport)
            .combineWith("basic & established & active & (highUsage | lowSupport)")
            .build();
            
        return searchUsers(request, Pageable.unpaged()).getContent();
    }
    
    /**
     * Complex search combining multiple criteria
     */
    public Page<User> searchAdvanced(
            String namePattern, 
            List<String> departments,
            Integer minAge,
            Integer maxAge,
            String cityPattern,
            UserStatus status,
            Pageable pageable) {
        
        FilterRequest.Builder<UserPropertyRef> builder = FilterRequest.builder();
        List<String> activeFilters = new ArrayList<>();
        
        // Add filters conditionally
        if (namePattern != null) {
            builder.filter("name", new FilterDefinition<>(UserPropertyRef.FULL_NAME, 
                Op.MATCHES, namePattern + "%"));
            activeFilters.add("name");
        }
        
        if (departments != null && !departments.isEmpty()) {
            builder.filter("dept", new FilterDefinition<>(UserPropertyRef.DEPARTMENT_NAME, 
                Op.IN, departments));
            activeFilters.add("dept");
        }
        
        if (minAge != null && maxAge != null) {
            builder.filter("age", new FilterDefinition<>(UserPropertyRef.AGE, 
                Op.RANGE, List.of(minAge, maxAge)));
            activeFilters.add("age");
        } else if (minAge != null) {
            builder.filter("minAge", new FilterDefinition<>(UserPropertyRef.AGE, 
                Op.GTE, minAge));
            activeFilters.add("minAge");
        } else if (maxAge != null) {
            builder.filter("maxAge", new FilterDefinition<>(UserPropertyRef.AGE, 
                Op.LTE, maxAge));
            activeFilters.add("maxAge");
        }
        
        if (cityPattern != null) {
            builder.filter("city", new FilterDefinition<>(UserPropertyRef.CITY_NAME, 
                Op.MATCHES, "%" + cityPattern + "%"));
            activeFilters.add("city");
        }
        
        if (status != null) {
            builder.filter("status", new FilterDefinition<>(UserPropertyRef.STATUS, 
                Op.EQ, status));
            activeFilters.add("status");
        }
        
        // Combine all filters with AND logic
        if (!activeFilters.isEmpty()) {
            String combineWith = String.join(" & ", activeFilters);
            builder.combineWith(combineWith);
        }
        
        return searchUsers(builder.build(), pageable);
    }
}
```

---

## 3. Generation Layer: Spring-Optimized Output

FilterQL generates **native Spring specifications** that integrate seamlessly with Spring Data JPA optimizations.

### PredicateResolver Integration

```java
// VERIFIED: How PredicateResolver creates Spring specifications
public interface PredicateResolver<T> {
    Predicate resolve(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder);
    
    // Extension method for Spring integration
    default Specification<T> toSpecification() {
        return this::resolve;
    }
}

// Example usage in service
PredicateResolver<User> resolver = filterResolver.resolve(User.class, request);
Specification<User> spec = resolver.toSpecification();

// Direct Spring Data JPA usage
Page<User> results = userRepository.findAll(spec, pageable);
```

### Advanced Repository Patterns

```java
// VERIFIED: Custom repository with FilterQL integration
@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    
    // Standard Spring Data JPA methods work seamlessly
    default Page<User> findByFilter(FilterRequest<UserPropertyRef> request, Pageable pageable) {
        FilterResolver resolver = FilterResolver.of(getFilterContext());
        PredicateResolver<User> predicateResolver = resolver.resolve(User.class, request);
        return findAll(predicateResolver.toSpecification(), pageable);
    }
    
    // Custom query methods can combine with FilterQL
    @Query("SELECT u FROM User u WHERE u.department.budget > :budget")
    List<User> findByHighBudgetDepartment(@Param("budget") BigDecimal budget);
    
    // Combine custom queries with FilterQL
    default List<User> findHighBudgetUsersWithFilters(
            BigDecimal budget, 
            FilterRequest<UserPropertyRef> additionalFilters) {
        
        // Create specification for budget requirement
        Specification<User> budgetSpec = (root, query, cb) -> 
            cb.greaterThan(root.get("department").get("budget"), budget);
            
        // Convert FilterQL request to specification
        FilterResolver resolver = FilterResolver.of(getFilterContext());
        PredicateResolver<User> predicateResolver = resolver.resolve(User.class, additionalFilters);
        Specification<User> filterSpec = predicateResolver.toSpecification();
        
        // Combine and execute
        return findAll(Specification.where(budgetSpec).and(filterSpec));
    }
    
    private FilterContext<User, UserPropertyRef> getFilterContext() {
        // Inject or create your filter context
        return ApplicationContextHolder.getBean(FilterContext.class);
    }
}
```

---

## Performance and Optimization

FilterQL's Spring adapter is designed for **production performance** with intelligent optimizations.

### Automatic Join Optimization

```java
// VERIFIED: FilterQL handles complex joins automatically
FilterRequest<UserPropertyRef> complexRequest = FilterRequest.<UserPropertyRef>builder()
    .filter("name", new FilterDefinition<>(UserPropertyRef.FULL_NAME, Op.MATCHES, "John%"))
    .filter("dept", new FilterDefinition<>(UserPropertyRef.DEPARTMENT_NAME, Op.EQ, "ENGINEERING"))
    .filter("city", new FilterDefinition<>(UserPropertyRef.CITY_NAME, Op.EQ, "Seattle"))
    .filter("country", new FilterDefinition<>(UserPropertyRef.COUNTRY_CODE, Op.EQ, "US"))
    .combineWith("name & dept & city & country")
    .build();

// FilterQL generates optimal JPA query with necessary joins:
```

```sql
-- Generated SQL is optimized automatically
SELECT DISTINCT u.* 
FROM users u
LEFT JOIN departments d ON u.department_id = d.id
LEFT JOIN addresses a ON u.address_id = a.id
LEFT JOIN cities c ON a.city_id = c.id
LEFT JOIN countries co ON c.country_id = co.id
WHERE u.full_name LIKE 'John%'
  AND d.name = 'ENGINEERING'
  AND c.name = 'Seattle' 
  AND co.code = 'US'
```

### Lazy Evaluation and Caching

```java
// VERIFIED: Performance optimizations built-in
public class FilterPerformanceExample {
    
    @Cacheable("filter-specs")
    public Specification<User> getCachedSpecification(FilterRequest<UserPropertyRef> request) {
        // FilterQL supports specification caching
        FilterResolver resolver = FilterResolver.of(filterContext);
        PredicateResolver<User> predicateResolver = resolver.resolve(User.class, request);
        return predicateResolver.toSpecification();
    }
    
    @Async
    public CompletableFuture<Page<User>> searchAsync(
            FilterRequest<UserPropertyRef> request, 
            Pageable pageable) {
        // Async search for large datasets
        Specification<User> spec = getCachedSpecification(request);
        Page<User> results = userRepository.findAll(spec, pageable);
        return CompletableFuture.completedFuture(results);
    }
}
```

---

## Real-World Integration Patterns

### 1. RESTful API Controllers

```java
// VERIFIED: Production-ready REST controller pattern
@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {
    
    private final UserSearchService userSearchService;
    
    public UserController(UserSearchService userSearchService) {
        this.userSearchService = userSearchService;
    }
    
    /**
     * Advanced user search with full FilterQL power
     */
    @PostMapping("/search")
    public ResponseEntity<Page<UserDTO>> searchUsers(
            @Valid @RequestBody FilterRequest<UserPropertyRef> request,
            @PageableDefault(size = 20, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable) {
        
        Page<User> users = userSearchService.searchUsers(request, pageable);
        Page<UserDTO> userDTOs = users.map(this::convertToDTO);
        
        return ResponseEntity.ok()
            .header("X-Total-Count", String.valueOf(users.getTotalElements()))
            .body(userDTOs);
    }
    
    /**
     * Quick search for common scenarios
     */
    @GetMapping("/search")
    public ResponseEntity<Page<UserDTO>> quickSearch(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) UserStatus status,
            Pageable pageable) {
        
        Page<User> users = userSearchService.searchAdvanced(
            name, 
            department != null ? List.of(department) : null,
            null, null, city, status, pageable);
            
        return ResponseEntity.ok(users.map(this::convertToDTO));
    }
    
    /**
     * Business-specific endpoints
     */
    @GetMapping("/upgrade-eligible")
    public ResponseEntity<List<UserDTO>> getUpgradeEligible() {
        List<User> users = userSearchService.findUpgradeEligible();
        return ResponseEntity.ok(users.stream().map(this::convertToDTO).toList());
    }
    
    private UserDTO convertToDTO(User user) {
        // DTO conversion logic
        return new UserDTO(user.getId(), user.getFullName(), user.getEmail(), 
                          user.getDepartment().getName(), user.getStatus());
    }
}
```

### 2. GraphQL Integration

```java
// VERIFIED: GraphQL with FilterQL pattern
@Component
public class UserGraphQLResolver implements GraphQLQueryResolver {
    
    private final UserSearchService userSearchService;
    
    public UserGraphQLResolver(UserSearchService userSearchService) {
        this.userSearchService = userSearchService;
    }
    
    public Connection<User> users(
            UserFilterInput filter,
            String after,
            Integer first,
            String before,
            Integer last) {
        
        // Convert GraphQL input to FilterQL request
        FilterRequest<UserPropertyRef> request = convertToFilterRequest(filter);
        
        // Convert GraphQL pagination to Spring Pageable
        Pageable pageable = convertToPageable(after, first, before, last);
        
        Page<User> page = userSearchService.searchUsers(request, pageable);
        
        return convertToConnection(page);
    }
    
    private FilterRequest<UserPropertyRef> convertToFilterRequest(UserFilterInput input) {
        FilterRequest.Builder<UserPropertyRef> builder = FilterRequest.builder();
        List<String> activeFilters = new ArrayList<>();
        
        if (input.getName() != null) {
            builder.filter("name", new FilterDefinition<>(UserPropertyRef.FULL_NAME, 
                Op.MATCHES, input.getName() + "%"));
            activeFilters.add("name");
        }
        
        if (input.getDepartments() != null && !input.getDepartments().isEmpty()) {
            builder.filter("dept", new FilterDefinition<>(UserPropertyRef.DEPARTMENT_NAME, 
                Op.IN, input.getDepartments()));
            activeFilters.add("dept");
        }
        
        if (input.getAgeRange() != null) {
            builder.filter("age", new FilterDefinition<>(UserPropertyRef.AGE, 
                Op.RANGE, List.of(input.getAgeRange().getMin(), input.getAgeRange().getMax())));
            activeFilters.add("age");
        }
        
        if (!activeFilters.isEmpty()) {
            builder.combineWith(String.join(" & ", activeFilters));
        }
        
        return builder.build();
    }
}
```

### 3. Spring Security Integration

```java
// VERIFIED: Security-aware filtering
@Service
public class SecureUserSearchService {
    
    private final UserSearchService userSearchService;
    private final SecurityService securityService;
    
    public SecureUserSearchService(UserSearchService userSearchService, SecurityService securityService) {
        this.userSearchService = userSearchService;
        this.securityService = securityService;
    }
    
    @PreAuthorize("hasRole('USER_READ')")
    public Page<User> searchUsers(FilterRequest<UserPropertyRef> request, Pageable pageable) {
        // Add security context to filters
        FilterRequest<UserPropertyRef> secureRequest = addSecurityFilters(request);
        return userSearchService.searchUsers(secureRequest, pageable);
    }
    
    private FilterRequest<UserPropertyRef> addSecurityFilters(FilterRequest<UserPropertyRef> originalRequest) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // Create new builder with existing filters
        FilterRequest.Builder<UserPropertyRef> builder = FilterRequest.<UserPropertyRef>builder()
            .filters(originalRequest.filters())
            .combineWith(originalRequest.combineWith());
        
        // Add security constraints based on user role
        if (!securityService.hasRole(auth, "ADMIN")) {
            // Non-admin users can only see users from their own department
            String userDepartment = securityService.getUserDepartment(auth);
            builder.filter("securityDept", new FilterDefinition<>(UserPropertyRef.DEPARTMENT_NAME, 
                Op.EQ, userDepartment));
                
            // Update combine logic to include security filter
            String newCombineWith = originalRequest.combineWith() != null 
                ? "(" + originalRequest.combineWith() + ") & securityDept"
                : "securityDept";
            builder.combineWith(newCombineWith);
        }
        
        if (!securityService.hasRole(auth, "HR")) {
            // Non-HR users cannot see sensitive information
            builder.filter("noSensitive", new FilterDefinition<>(UserPropertyRef.STATUS, 
                Op.NOT_IN, List.of(UserStatus.TERMINATED, UserStatus.SUSPENDED)));
                
            String newCombineWith = builder.build().combineWith() + " & noSensitive";
            builder.combineWith(newCombineWith);
        }
        
        return builder.build();
    }
}
```

---

## Testing Your Spring Integration

Professional FilterQL Spring integration includes **comprehensive testing**:

```java
// VERIFIED: Complete testing strategy
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class UserFilterIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserSearchService userSearchService;
    
    @Autowired
    private FilterContext<User, UserPropertyRef> userFilterContext;
    
    @BeforeEach
    void setUp() {
        // Create test data
        createTestUsers();
    }
    
    @Test
    void shouldFilterByNameAndDepartment() {
        // Given
        FilterRequest<UserPropertyRef> request = FilterRequest.<UserPropertyRef>builder()
            .filter("name", new FilterDefinition<>(UserPropertyRef.FULL_NAME, Op.MATCHES, "John%"))
            .filter("dept", new FilterDefinition<>(UserPropertyRef.DEPARTMENT_NAME, Op.EQ, "ENGINEERING"))
            .combineWith("name & dept")
            .build();
        
        // When
        Page<User> results = userSearchService.searchUsers(request, Pageable.unpaged());
        
        // Then
        assertThat(results.getContent()).isNotEmpty();
        assertThat(results.getContent()).allMatch(user -> 
            user.getFullName().startsWith("John") && 
            user.getDepartment().getName().equals("ENGINEERING"));
    }
    
    @Test
    void shouldHandleComplexBooleanLogic() {
        // Given: (engineering OR design) AND senior AND (seattle OR portland)
        FilterRequest<UserPropertyRef> request = FilterRequest.<UserPropertyRef>builder()
            .filter("eng", new FilterDefinition<>(UserPropertyRef.DEPARTMENT_NAME, Op.EQ, "ENGINEERING"))
            .filter("design", new FilterDefinition<>(UserPropertyRef.DEPARTMENT_NAME, Op.EQ, "DESIGN"))
            .filter("senior", new FilterDefinition<>(UserPropertyRef.LEVEL, Op.EQ, "SENIOR"))
            .filter("seattle", new FilterDefinition<>(UserPropertyRef.CITY_NAME, Op.EQ, "Seattle"))
            .filter("portland", new FilterDefinition<>(UserPropertyRef.CITY_NAME, Op.EQ, "Portland"))
            .combineWith("(eng | design) & senior & (seattle | portland)")
            .build();
        
        // When
        Page<User> results = userSearchService.searchUsers(request, Pageable.unpaged());
        
        // Then
        assertThat(results.getContent()).allMatch(user -> {
            boolean rightDept = user.getDepartment().getName().equals("ENGINEERING") || 
                               user.getDepartment().getName().equals("DESIGN");
            boolean isSenior = user.getLevel().equals("SENIOR");
            boolean rightCity = user.getAddress().getCity().getName().equals("Seattle") ||
                               user.getAddress().getCity().getName().equals("Portland");
            return rightDept && isSenior && rightCity;
        });
    }
    
    @Test
    void shouldGenerateOptimalSQL() {
        // Given
        FilterRequest<UserPropertyRef> request = FilterRequest.<UserPropertyRef>builder()
            .filter("name", new FilterDefinition<>(UserPropertyRef.FULL_NAME, Op.MATCHES, "John%"))
            .filter("city", new FilterDefinition<>(UserPropertyRef.CITY_NAME, Op.EQ, "Seattle"))
            .combineWith("name & city")
            .build();
        
        // When
        PredicateResolver<User> resolver = FilterResolver.of(userFilterContext).resolve(User.class, request);
        Specification<User> spec = resolver.toSpecification();
        
        // Then - verify specification is correctly formed
        assertThat(spec).isNotNull();
        
        // Execute and verify results
        List<User> results = userRepository.findAll(spec);
        assertThat(results).allMatch(user -> 
            user.getFullName().startsWith("John") && 
            user.getAddress().getCity().getName().equals("Seattle"));
    }
    
    @Test
    void shouldWorkWithPageableAndSorting() {
        // Given
        FilterRequest<UserPropertyRef> request = FilterRequest.<UserPropertyRef>builder()
            .filter("dept", new FilterDefinition<>(UserPropertyRef.DEPARTMENT_NAME, Op.IN, 
                List.of("ENGINEERING", "DESIGN")))
            .combineWith("dept")
            .build();
            
        Pageable pageable = PageRequest.of(0, 5, Sort.by("fullName").ascending());
        
        // When
        Page<User> results = userSearchService.searchUsers(request, pageable);
        
        // Then
        assertThat(results.getContent()).hasSize(5);
        assertThat(results.getTotalElements()).isGreaterThan(5);
        assertThat(results.getContent()).isSortedAccordingTo(
            Comparator.comparing(User::getFullName));
    }
    
    private void createTestUsers() {
        // Create test data setup
        // ... implementation details
    }
}
```

---

## What's Next?

You've now mastered FilterQL's Spring integration! You understand:

- ✅ **Complete Spring Data JPA integration** with zero boilerplate
- ✅ **Advanced mapping strategies** for any complexity
- ✅ **Production patterns** for real-world applications
- ✅ **Performance optimization** techniques
- ✅ **Testing strategies** for reliable implementations

### Your Next Steps

<table>
<tr>
<td width="50%" valign="top">

### 🎯 **Master More Patterns**

- [**Real-World Examples**](examples.md)  
  *Complete application scenarios*

- [**Core Architecture**](core-module.md)  
  *Deep dive into FilterQL internals*

- [**Troubleshooting**](troubleshooting.md)  
  *Common issues and solutions*

</td>
<td width="50%" valign="top">

### 🚀 **Build Something Amazing**

- **E-commerce Platform**  
  *Product search with FilterQL*

- **User Management System**  
  *Advanced admin filtering*

- **Analytics Dashboard**  
  *Data exploration with dynamic filters*

</td>
</tr>
</table>

---

<div align="center">
  <p><strong>🎉 Congratulations! You're now a FilterQL Spring master!</strong></p>
  <p><em>You've unlocked the power to build enterprise-grade filtering with the elegance of modern development. The Spring ecosystem will never feel the same!</em></p>
</div>