# Migration Guide

This guide helps you migrate from other query solutions to FilterQL, providing step-by-step instructions and code examples.

## Table of Contents

1. [Migration from Spring Data Specifications](#migration-from-spring-data-specifications)
2. [Migration from Criteria API](#migration-from-criteria-api)
3. [Migration from QueryDSL](#migration-from-querydsl)
4. [Migration from Custom Query Solutions](#migration-from-custom-query-solutions)
5. [Common Migration Patterns](#common-migration-patterns)
6. [Performance Considerations](#performance-considerations)
7. [Testing During Migration](#testing-during-migration)

## Migration from Spring Data Specifications

### Before: Traditional Specifications

```java
// Old approach: Manual Specification building
@Service
public class UserSearchService {
    
    public Page<User> searchUsers(String name, Integer minAge, UserStatus status, Pageable pageable) {
        return userRepository.findAll(buildSpecification(name, minAge, status), pageable);
    }
    
    private Specification<User> buildSpecification(String name, Integer minAge, UserStatus status) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (name != null && !name.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }
            
            if (minAge != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("age"), minAge));
            }
            
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
```

### After: FilterQL Implementation

```java
// New approach: Declarative FilterQL
@Service
public class UserSearchService {
    
    private final FilterContext<User, UserPropertyRef> filterContext;
    
    public Page<User> searchUsers(FilterRequest<UserPropertyRef> filterRequest, Pageable pageable) {
        FilterResolver resolver = FilterResolver.of(filterContext);
        PredicateResolver<User> predicateResolver = resolver.resolve(User.class, filterRequest);
        
        Specification<User> specification = (root, query, cb) -> 
            predicateResolver.resolve(root, query, cb);
            
        return userRepository.findAll(specification, pageable);
    }
}

// Property definitions
public enum UserPropertyRef implements PropertyReference {
    NAME(String.class, Set.of(Op.EQ, Op.MATCHES)),
    AGE(Integer.class, OperatorUtils.FOR_NUMBER),
    STATUS(UserStatus.class, Set.of(Op.EQ, Op.NE, Op.IN));
    
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

// Filter context configuration
@Configuration
public class FilterConfig {
    
    @Bean
    public FilterContext<User, UserPropertyRef> userFilterContext() {
        return new FilterContext<>(
            User.class,
            UserPropertyRef.class,
            definition -> switch (definition.ref()) {
                case NAME -> "name";
                case AGE -> "age";
                case STATUS -> "status";
            }
        );
    }
}
```

### Migration Steps

1. **Define Property References**
   ```java
   // Step 1: List all filterable properties
   public enum UserPropertyRef implements PropertyReference {
       NAME(String.class, Set.of(Op.EQ, Op.MATCHES)),
       EMAIL(String.class, Set.of(Op.EQ, Op.MATCHES)),
       AGE(Integer.class, OperatorUtils.FOR_NUMBER),
       STATUS(UserStatus.class, Set.of(Op.EQ, Op.IN)),
       DEPARTMENT(String.class, Set.of(Op.EQ, Op.IN));
   }
   ```

2. **Create Filter Context**
   ```java
   // Step 2: Map properties to entity paths
   FilterContext<User, UserPropertyRef> context = new FilterContext<>(
       User.class,
       UserPropertyRef.class,
       definition -> switch (definition.ref()) {
           case NAME -> "name";
           case EMAIL -> "email";
           case AGE -> "age";
           case STATUS -> "status";
           case DEPARTMENT -> "department.name"; // Join navigation
       }
   );
   ```

3. **Replace Service Methods**
   ```java
   // Step 3: Replace parameter-based methods
   // Before
   public Page<User> findUsersByNameAndAge(String name, Integer age, Pageable pageable);
   
   // After
   public Page<User> findUsers(FilterRequest<UserPropertyRef> request, Pageable pageable);
   ```

4. **Update Controllers**
   ```java
   // Step 4: Update REST endpoints
   // Before
   @GetMapping("/users")
   public Page<User> getUsers(
           @RequestParam(required = false) String name,
           @RequestParam(required = false) Integer minAge,
           @RequestParam(required = false) UserStatus status,
           Pageable pageable) {
       return userService.searchUsers(name, minAge, status, pageable);
   }
   
   // After
   @PostMapping("/users/search")
   public Page<User> searchUsers(
           @RequestBody FilterRequest<UserPropertyRef> filterRequest,
           Pageable pageable) {
       return userService.searchUsers(filterRequest, pageable);
   }
   ```

## Migration from Criteria API

### Before: Raw Criteria API

```java
@Repository
public class UserRepositoryImpl implements UserRepositoryCustom {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    public List<User> findUsersByCriteria(UserSearchCriteria criteria) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> query = cb.createQuery(User.class);
        Root<User> root = query.from(User.class);
        
        List<Predicate> predicates = new ArrayList<>();
        
        if (criteria.getName() != null) {
            predicates.add(cb.like(root.get("name"), "%" + criteria.getName() + "%"));
        }
        
        if (criteria.getMinAge() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("age"), criteria.getMinAge()));
        }
        
        if (criteria.getDepartments() != null && !criteria.getDepartments().isEmpty()) {
            Join<User, Department> deptJoin = root.join("department");
            predicates.add(deptJoin.get("name").in(criteria.getDepartments()));
        }
        
        query.where(cb.and(predicates.toArray(new Predicate[0])));
        
        return entityManager.createQuery(query).getResultList();
    }
}
```

### After: FilterQL with Custom Mappings

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
                case AGE -> "age";
                case DEPARTMENTS -> new PredicateResolverMapping<User, UserPropertyRef>() {
                    @Override
                    public PredicateResolver<User> resolve(FilterDefinition<UserPropertyRef> def) {
                        return (root, query, cb) -> {
                            Join<User, Department> deptJoin = root.join("department", JoinType.INNER);
                            return deptJoin.get("name").in((Collection<?>) def.value());
                        };
                    }
                };
            }
        );
    }
}

// Simplified service
@Service
public class UserService {
    
    public List<User> findUsers(FilterRequest<UserPropertyRef> request) {
        FilterResolver resolver = FilterResolver.of(filterContext);
        PredicateResolver<User> predicateResolver = resolver.resolve(User.class, request);
        
        Specification<User> spec = (root, query, cb) -> 
            predicateResolver.resolve(root, query, cb);
            
        return userRepository.findAll(spec);
    }
}
```

## Migration from QueryDSL

### Before: QueryDSL

```java
@Repository
public class UserRepositoryImpl extends QuerydslRepositorySupport implements UserRepositoryCustom {
    
    private final QUser user = QUser.user;
    private final QDepartment department = QDepartment.department;
    
    public UserRepositoryImpl() {
        super(User.class);
    }
    
    public List<User> findUsersWithFilters(String namePattern, List<String> departments, 
                                          Integer minAge, Integer maxAge) {
        JPAQuery<User> query = new JPAQuery<>(getEntityManager());
        
        BooleanBuilder builder = new BooleanBuilder();
        
        if (namePattern != null) {
            builder.and(user.name.containsIgnoreCase(namePattern));
        }
        
        if (departments != null && !departments.isEmpty()) {
            builder.and(user.department.name.in(departments));
        }
        
        if (minAge != null) {
            builder.and(user.age.goe(minAge));
        }
        
        if (maxAge != null) {
            builder.and(user.age.loe(maxAge));
        }
        
        return query.from(user)
                   .leftJoin(user.department, department)
                   .where(builder)
                   .fetch();
    }
}
```

### After: FilterQL Implementation

```java
// Property definitions mirror QueryDSL paths
public enum UserPropertyRef implements PropertyReference {
    NAME_CONTAINS(String.class, Set.of(Op.MATCHES)),
    DEPARTMENT_NAMES(String.class, Set.of(Op.IN)),
    AGE_MIN(Integer.class, Set.of(Op.GTE)),
    AGE_MAX(Integer.class, Set.of(Op.LTE)),
    AGE_RANGE(Integer.class, Set.of(Op.RANGE));
    
    // Implementation...
}

// Mapping configuration
@Bean
public FilterContext<User, UserPropertyRef> userFilterContext() {
    return new FilterContext<>(
        User.class,
        UserPropertyRef.class,
        definition -> switch (definition.ref()) {
            case NAME_CONTAINS -> new PredicateResolverMapping<User, UserPropertyRef>() {
                @Override
                public PredicateResolver<User> resolve(FilterDefinition<UserPropertyRef> def) {
                    return (root, query, cb) -> {
                        String pattern = "%" + ((String) def.value()).toLowerCase() + "%";
                        return cb.like(cb.lower(root.get("name")), pattern);
                    };
                }
            };
            
            case DEPARTMENT_NAMES -> new PredicateResolverMapping<User, UserPropertyRef>() {
                @Override
                public PredicateResolver<User> resolve(FilterDefinition<UserPropertyRef> def) {
                    return (root, query, cb) -> {
                        Join<User, Department> deptJoin = root.join("department", JoinType.LEFT);
                        return deptJoin.get("name").in((Collection<?>) def.value());
                    };
                }
            };
            
            case AGE_MIN -> new PredicateResolverMapping<User, UserPropertyRef>() {
                @Override
                public PredicateResolver<User> resolve(FilterDefinition<UserPropertyRef> def) {
                    return (root, query, cb) -> 
                        cb.greaterThanOrEqualTo(root.get("age"), (Integer) def.value());
                }
            };
            
            case AGE_MAX -> new PredicateResolverMapping<User, UserPropertyRef>() {
                @Override
                public PredicateResolver<User> resolve(FilterDefinition<UserPropertyRef> def) {
                    return (root, query, cb) -> 
                        cb.lessThanOrEqualTo(root.get("age"), (Integer) def.value());
                }
            };
            
            case AGE_RANGE -> new PredicateResolverMapping<User, UserPropertyRef>() {
                @Override
                public PredicateResolver<User> resolve(FilterDefinition<UserPropertyRef> def) {
                    return (root, query, cb) -> {
                        List<Integer> range = (List<Integer>) def.value();
                        return cb.between(root.get("age"), range.get(0), range.get(1));
                    };
                }
            };
        }
    );
}

// Simplified service (same as above)
@Service
public class UserService {
    public List<User> findUsers(FilterRequest<UserPropertyRef> request) {
        // Same FilterQL implementation
    }
}
```

## Migration from Custom Query Solutions

### Before: String-based Dynamic Queries

```java
@Repository
public class UserRepositoryImpl implements UserRepositoryCustom {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    public List<User> findUsersByDynamicQuery(Map<String, Object> filters) {
        StringBuilder jpql = new StringBuilder("SELECT u FROM User u WHERE 1=1");
        Map<String, Object> parameters = new HashMap<>();
        
        if (filters.containsKey("name")) {
            jpql.append(" AND LOWER(u.name) LIKE :name");
            parameters.put("name", "%" + filters.get("name").toString().toLowerCase() + "%");
        }
        
        if (filters.containsKey("age")) {
            jpql.append(" AND u.age >= :age");
            parameters.put("age", filters.get("age"));
        }
        
        if (filters.containsKey("status")) {
            jpql.append(" AND u.status = :status");
            parameters.put("status", filters.get("status"));
        }
        
        Query query = entityManager.createQuery(jpql.toString());
        parameters.forEach(query::setParameter);
        
        return query.getResultList();
    }
}
```

### After: Type-Safe FilterQL

```java
// Replace Map<String, Object> with type-safe FilterRequest
public enum UserPropertyRef implements PropertyReference {
    NAME(String.class, Set.of(Op.EQ, Op.MATCHES)),
    AGE(Integer.class, Set.of(Op.GTE, Op.LTE, Op.RANGE)),
    STATUS(UserStatus.class, Set.of(Op.EQ, Op.IN));
    
    // Standard implementation...
}

@Service
public class UserService {
    
    // Before: Unsafe Map-based filters
    // public List<User> findUsers(Map<String, Object> filters)
    
    // After: Type-safe FilterRequest
    public List<User> findUsers(FilterRequest<UserPropertyRef> request) {
        FilterResolver resolver = FilterResolver.of(filterContext);
        PredicateResolver<User> predicateResolver = resolver.resolve(User.class, request);
        
        Specification<User> spec = (root, query, cb) -> 
            predicateResolver.resolve(root, query, cb);
            
        return userRepository.findAll(spec);
    }
}
```

## Common Migration Patterns

### 1. Parameter Mapping

```java
// Before: Multiple optional parameters
public Page<User> searchUsers(
        String name,
        Integer minAge,
        Integer maxAge,
        List<UserStatus> statuses,
        String departmentName,
        Boolean isActive,
        Pageable pageable) {
    // Complex parameter handling...
}

// After: Single FilterRequest parameter
public Page<User> searchUsers(FilterRequest<UserPropertyRef> request, Pageable pageable) {
    // Simple FilterQL processing...
}

// Usage transformation
// Before:
userService.searchUsers("John", 25, 65, Arrays.asList(ACTIVE, PENDING), "Engineering", true, pageable);

// After:
FilterRequest<UserPropertyRef> request = FilterRequest.<UserPropertyRef>builder()
    .filter("name", new FilterDefinition<>(UserPropertyRef.NAME, Op.MATCHES, "John%"))
    .filter("ageRange", new FilterDefinition<>(UserPropertyRef.AGE, Op.RANGE, Arrays.asList(25, 65)))
    .filter("statuses", new FilterDefinition<>(UserPropertyRef.STATUS, Op.IN, Arrays.asList(ACTIVE, PENDING)))
    .filter("department", new FilterDefinition<>(UserPropertyRef.DEPARTMENT_NAME, Op.EQ, "Engineering"))
    .filter("active", new FilterDefinition<>(UserPropertyRef.IS_ACTIVE, Op.EQ, true))
    .combineWith("name & ageRange & statuses & department & active")
    .build();

userService.searchUsers(request, pageable);
```

### 2. Complex Logic Migration

```java
// Before: Complex conditional logic
public Specification<User> buildComplexSpecification(SearchCriteria criteria) {
    return (root, query, cb) -> {
        List<Predicate> predicates = new ArrayList<>();
        
        // Complex name search
        if (criteria.getNameSearch() != null) {
            String term = criteria.getNameSearch().toLowerCase();
            Predicate firstNameMatch = cb.like(cb.lower(root.get("firstName")), "%" + term + "%");
            Predicate lastNameMatch = cb.like(cb.lower(root.get("lastName")), "%" + term + "%");
            Predicate fullNameMatch = cb.like(
                cb.lower(cb.concat(cb.concat(root.get("firstName"), " "), root.get("lastName"))),
                "%" + term + "%"
            );
            predicates.add(cb.or(firstNameMatch, lastNameMatch, fullNameMatch));
        }
        
        // Age range with defaults
        Integer minAge = criteria.getMinAge() != null ? criteria.getMinAge() : 18;
        Integer maxAge = criteria.getMaxAge() != null ? criteria.getMaxAge() : 65;
        predicates.add(cb.between(root.get("age"), minAge, maxAge));
        
        return cb.and(predicates.toArray(new Predicate[0]));
    };
}

// After: FilterQL with custom mappings
public enum UserPropertyRef implements PropertyReference {
    FULL_NAME_SEARCH(String.class, Set.of(Op.MATCHES)),
    AGE_RANGE_WITH_DEFAULTS(List.class, Set.of(Op.RANGE));
    
    // Implementation...
}

// Custom mapping for complex logic
FilterContext<User, UserPropertyRef> context = new FilterContext<>(
    User.class,
    UserPropertyRef.class,
    definition -> switch (definition.ref()) {
        case FULL_NAME_SEARCH -> new PredicateResolverMapping<User, UserPropertyRef>() {
            @Override
            public PredicateResolver<User> resolve(FilterDefinition<UserPropertyRef> def) {
                return (root, query, cb) -> {
                    String term = ((String) def.value()).toLowerCase();
                    return cb.or(
                        cb.like(cb.lower(root.get("firstName")), "%" + term + "%"),
                        cb.like(cb.lower(root.get("lastName")), "%" + term + "%"),
                        cb.like(cb.lower(cb.concat(
                            cb.concat(root.get("firstName"), " "),
                            root.get("lastName")
                        )), "%" + term + "%")
                    );
                };
            }
        };
        
        case AGE_RANGE_WITH_DEFAULTS -> new PredicateResolverMapping<User, UserPropertyRef>() {
            @Override
            public PredicateResolver<User> resolve(FilterDefinition<UserPropertyRef> def) {
                return (root, query, cb) -> {
                    List<Integer> range = (List<Integer>) def.value();
                    Integer minAge = range.get(0) != null ? range.get(0) : 18;
                    Integer maxAge = range.get(1) != null ? range.get(1) : 65;
                    return cb.between(root.get("age"), minAge, maxAge);
                };
            }
        };
    }
);
```

### 3. Error Handling Migration

```java
// Before: Manual validation
public List<User> searchUsers(String name, Integer age) {
    if (name != null && name.length() > 100) {
        throw new IllegalArgumentException("Name too long");
    }
    if (age != null && (age < 0 || age > 150)) {
        throw new IllegalArgumentException("Invalid age");
    }
    
    // Build query...
}

// After: PropertyReference validation
public enum UserPropertyRef implements PropertyReference {
    NAME(String.class, Set.of(Op.EQ, Op.MATCHES)) {
        @Override
        public boolean validate(Op operator, Object value) {
            if (!super.validate(operator, value)) return false;
            
            if (value instanceof String name && name.length() > 100) {
                throw new FilterValidationException("Name cannot exceed 100 characters");
            }
            return true;
        }
    },
    
    AGE(Integer.class, OperatorUtils.FOR_NUMBER) {
        @Override
        public boolean validate(Op operator, Object value) {
            if (!super.validate(operator, value)) return false;
            
            if (value instanceof Integer age && (age < 0 || age > 150)) {
                throw new FilterValidationException("Age must be between 0 and 150");
            }
            return true;
        }
    };
}
```

## Performance Considerations

### 1. Index Migration

```sql
-- Before: Indexes for individual query methods
CREATE INDEX idx_user_name ON users(name);
CREATE INDEX idx_user_age ON users(age);
CREATE INDEX idx_user_status ON users(status);

-- After: Composite indexes for common filter combinations
CREATE INDEX idx_user_name_status ON users(name, status);
CREATE INDEX idx_user_age_department ON users(age, department_id);
CREATE INDEX idx_user_active_created ON users(is_active, created_date);

-- Analyze actual FilterQL usage patterns
-- Add indexes based on common filter combinations
```

### 2. Query Optimization

```java
// Migration strategy for performance
@Configuration
public class MigrationConfig {
    
    // Phase 1: Dual implementation for comparison
    @Bean
    @Primary
    public UserService filterQLUserService() {
        return new FilterQLUserService(); // New implementation
    }
    
    @Bean
    public UserService legacyUserService() {
        return new LegacyUserService(); // Old implementation
    }
    
    // Performance monitoring
    @Bean
    public PerformanceMonitor performanceMonitor() {
        return new PerformanceMonitor();
    }
}

@Service
public class MigrationUserService {
    
    private final UserService filterQLService;
    private final UserService legacyService;
    private final PerformanceMonitor monitor;
    
    // Feature flag for gradual migration
    @Value("${migration.use-filterql:false}")
    private boolean useFilterQL;
    
    public Page<User> searchUsers(FilterRequest<UserPropertyRef> request, Pageable pageable) {
        if (useFilterQL) {
            return monitor.measure("filterql", () -> 
                filterQLService.searchUsers(request, pageable)
            );
        } else {
            // Convert FilterRequest back to legacy parameters for comparison
            return monitor.measure("legacy", () -> 
                legacyService.searchUsers(convertToLegacyParams(request), pageable)
            );
        }
    }
}
```

## Testing During Migration

### 1. Parallel Testing

```java
@Test
public class MigrationTest {
    
    @Autowired
    private LegacyUserService legacyService;
    
    @Autowired
    private FilterQLUserService filterQLService;
    
    @ParameterizedTest
    @MethodSource("searchScenarios")
    void shouldProduceSameResults(SearchScenario scenario) {
        // Legacy approach
        Page<User> legacyResults = legacyService.searchUsers(
            scenario.getName(),
            scenario.getMinAge(),
            scenario.getMaxAge(),
            scenario.getStatuses(),
            scenario.getPageable()
        );
        
        // FilterQL approach
        FilterRequest<UserPropertyRef> request = scenario.toFilterRequest();
        Page<User> filterQLResults = filterQLService.searchUsers(request, scenario.getPageable());
        
        // Compare results
        assertThat(filterQLResults.getContent())
            .usingElementComparatorIgnoringFields("id") // Ignore auto-generated fields
            .containsExactlyInAnyOrderElementsOf(legacyResults.getContent());
        
        assertThat(filterQLResults.getTotalElements())
            .isEqualTo(legacyResults.getTotalElements());
    }
    
    static Stream<SearchScenario> searchScenarios() {
        return Stream.of(
            new SearchScenario("John", null, null, null),
            new SearchScenario(null, 25, 65, null),
            new SearchScenario("Jane", 30, null, Arrays.asList(UserStatus.ACTIVE)),
            // Add more test scenarios...
        );
    }
}
```

### 2. Performance Regression Testing

```java
@Test
public class PerformanceRegressionTest {
    
    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void filterQLShouldNotBeMuchSlowerThanLegacy() {
        // Setup large dataset
        createLargeDataset(10000);
        
        FilterRequest<UserPropertyRef> request = createComplexFilterRequest();
        
        // Warm up JVM
        for (int i = 0; i < 5; i++) {
            filterQLService.searchUsers(request, PageRequest.of(0, 100));
        }
        
        // Measure performance
        long startTime = System.nanoTime();
        Page<User> results = filterQLService.searchUsers(request, PageRequest.of(0, 100));
        long duration = System.nanoTime() - startTime;
        
        // Should complete within reasonable time
        assertThat(duration).isLessThan(Duration.ofSeconds(2).toNanos());
        assertThat(results).isNotNull();
    }
}
```

## Migration Checklist

### Pre-Migration
- [ ] Analyze existing query patterns
- [ ] Identify all filterable properties
- [ ] Document complex business logic
- [ ] Review security requirements
- [ ] Plan database index strategy

### During Migration
- [ ] Define PropertyReference enums
- [ ] Create FilterContext configurations
- [ ] Implement custom mappings for complex logic
- [ ] Add comprehensive validation
- [ ] Set up parallel testing
- [ ] Monitor performance metrics

### Post-Migration
- [ ] Remove legacy code
- [ ] Update documentation
- [ ] Optimize database indexes
- [ ] Train team on FilterQL patterns
- [ ] Set up monitoring and alerting

### Rollback Plan
- [ ] Feature flags for quick rollback
- [ ] Legacy code preservation
- [ ] Database rollback scripts
- [ ] Performance baseline restoration

## Common Migration Issues

### 1. Case Sensitivity
```java
// Legacy: Case-insensitive by default
WHERE LOWER(name) LIKE LOWER('%john%')

// FilterQL: Explicit case handling needed
case NAME -> new PredicateResolverMapping<User, UserPropertyRef>() {
    @Override
    public PredicateResolver<User> resolve(FilterDefinition<UserPropertyRef> definition) {
        return (root, query, cb) -> {
            if (definition.operator() == Op.MATCHES) {
                String pattern = ((String) definition.value()).toLowerCase();
                return cb.like(cb.lower(root.get("name")), pattern);
            }
            return cb.equal(root.get("name"), definition.value());
        };
    }
};
```

### 2. Null Handling
```java
// Legacy: Manual null checks
if (value != null) {
    predicates.add(cb.equal(root.get("field"), value));
}

// FilterQL: Built-in null validation
public enum UserPropertyRef implements PropertyReference {
    NAME(String.class, Set.of(Op.EQ, Op.MATCHES)) {
        @Override
        public boolean validate(Op operator, Object value) {
            if (value == null) {
                throw new FilterValidationException("Name cannot be null");
            }
            return super.validate(operator, value);
        }
    };
}
```

### 3. Join Strategy Changes
```java
// Legacy: Implicit join behavior
root.get("department").get("name") // May cause issues

// FilterQL: Explicit join control
case DEPARTMENT_NAME -> new PredicateResolverMapping<User, UserPropertyRef>() {
    @Override
    public PredicateResolver<User> resolve(FilterDefinition<UserPropertyRef> definition) {
        return (root, query, cb) -> {
            Join<User, Department> deptJoin = root.join("department", JoinType.LEFT);
            return cb.equal(deptJoin.get("name"), definition.value());
        };
    }
};
```

## Summary

Migration to FilterQL provides:

1. **Type Safety** - Compile-time validation vs runtime errors
2. **Maintainability** - Declarative filters vs imperative query building
3. **Consistency** - Standardized patterns across the application
4. **Testability** - Easier to test filter logic in isolation
5. **Performance** - Optimized query generation and caching

Follow this guide step-by-step, test thoroughly, and migrate incrementally for a successful transition to FilterQL.