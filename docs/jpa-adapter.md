# JPA Adapter Guide

The FilterQL JPA adapter provides direct integration with the JPA Criteria API, giving you fine-grained control over query generation while maintaining FilterQL's simplicity.

## Quick Setup

### Dependencies

```xml
<dependency>
    <groupId>io.github.cyfko</groupId>
    <artifactId>filterql-adapter-jpa</artifactId>
    <version>1.0.1</version>
</dependency>
```

### Basic Usage

#### Simple Approach with BasicFilterExecutor

For most use cases, use the simplified `BasicFilterExecutor`:

```java
@Repository
public class UserDao {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    public List<User> findUsers(FilterRequest<UserProperty> request) {
        BasicFilterExecutor executor = new BasicFilterExecutor(entityManager);
        return executor.findAll(User.class, request);
    }
    
    // With pagination
    public List<User> findUsers(FilterRequest<UserProperty> request, int page, int size) {
        BasicFilterExecutor executor = new BasicFilterExecutor(entityManager);
        return executor.findAll(User.class, request, page * size, size);
    }
    
    // Get count
    public long countUsers(FilterRequest<UserProperty> request) {
        BasicFilterExecutor executor = new BasicFilterExecutor(entityManager);
        return executor.count(User.class, request);
    }
}
```

#### Advanced Approach with Manual Control

For advanced use cases where you need full control over the query:

```java
@Repository
public class AdvancedUserDao {
    
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

## Core Components

### BasicFilterExecutor

The `BasicFilterExecutor` is the simplest way to execute FilterQL requests with JPA:

```java
public class BasicFilterExecutor<T, P extends Enum<P> & PropertyRef & PathShape> {
    
    private final EntityManager entityManager;
    private final Class<T> entityClass;
    
    public BasicFilterExecutor(EntityManager entityManager, Class<T> entityClass) {
        this.entityManager = entityManager;
        this.entityClass = entityClass;
    }
    
    // Execute filter and return results
    public <E, P extends Enum<P> & PropertyRef> List<E> findAll(Class<E> entityClass, FilterRequest<P> request) {
        return findAll(entityClass, request, null, null);
    }
    
    // Find with pagination
    public <E, P extends Enum<P> & PropertyRef> List<E> findAll(Class<E> entityClass, FilterRequest<P> request, Integer offset, Integer limit) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<E> cq = cb.createQuery(entityClass);
        Root<E> root = cq.from(entityClass);
        
        if (request != null && !request.getFilters().isEmpty()) {
            Predicate predicate = buildPredicate(request, root, cq, cb);
            cq.where(predicate);
        }
        
        TypedQuery<E> query = entityManager.createQuery(cq);
        
        if (offset != null) {
            query.setFirstResult(offset);
        }
        if (limit != null) {
            query.setMaxResults(limit);
        }
        
        return query.getResultList();
    }
    
    // Count matching entities
    public <E, P extends Enum<P> & PropertyRef> long count(Class<E> entityClass, FilterRequest<P> request) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<E> root = cq.from(entityClass);
        
        cq.select(cb.count(root));
        
        if (request != null && !request.getFilters().isEmpty()) {
            Predicate predicate = buildPredicate(request, root, cq, cb);
            cq.where(predicate);
        }
        
        return entityManager.createQuery(cq).getSingleResult();
    }
    
    private <E, P extends Enum<P> & PropertyRef> Predicate buildPredicate(FilterRequest<P> request, Root<E> root, 
                                   CriteriaQuery<?> cq, CriteriaBuilder cb) {
        DSLParser parser = new DSLParser();
        FilterTree tree = parser.parse(request.getCombineWith());
        
        ContextAdapter<E, P> context = new ContextAdapter<>(
            new ConditionAdapterBuilder<E, P>() {}
        );
        
        request.getFilters().forEach(context::addCondition);
        Condition condition = tree.generate(context);
        
        return ((ConditionAdapter<E>) condition).toPredicate(root, cq, cb);
    }
}
```

### ConditionAdapter

The `ConditionAdapter` bridges FilterQL conditions to JPA predicates:

```java
public class ConditionAdapter<T> implements Condition {
    
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        // Converts condition to JPA Predicate
    }
}
```

### ContextAdapter

Manages the conversion context and builds condition adapters:

```java
ContextAdapter<User, UserProperty> context = new ContextAdapter<>(
    new ConditionAdapterBuilder<User, UserProperty>() {}
);
```

## Advanced Features

### Custom Query Building

Build complex queries with full JPA control:

```java
@Repository
public class AdvancedUserDao {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    public List<UserSummary> findUserSummaries(FilterRequest<UserProperty> request) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<UserSummary> cq = cb.createQuery(UserSummary.class);
        Root<User> root = cq.from(User.class);
        
        // Custom projection
        cq.select(cb.construct(UserSummary.class,
            root.get("name"),
            root.get("email"),
            root.get("department").get("name")
        ));
        
        // Apply FilterQL predicate
        if (request != null && !request.getFilters().isEmpty()) {
            Predicate filterPredicate = buildFilterPredicate(request, root, cq, cb);
            cq.where(filterPredicate);
        }
        
        // Custom ordering
        cq.orderBy(cb.asc(root.get("name")));
        
        return entityManager.createQuery(cq).getResultList();
    }
    
    private Predicate buildFilterPredicate(FilterRequest<UserProperty> request,
                                         Root<User> root, 
                                         CriteriaQuery<?> cq, 
                                         CriteriaBuilder cb) {
        DSLParser parser = new DSLParser();
        FilterTree tree = parser.parse(request.getCombineWith());
        
        ContextAdapter<User, UserProperty> context = new ContextAdapter<>(
            new ConditionAdapterBuilder<User, UserProperty>() {}
        );
        
        request.getFilters().forEach(context::addCondition);
        Condition condition = tree.generate(context);
        
        return ((ConditionAdapter<User>) condition).toPredicate(root, cq, cb);
    }
}
```

### Nested Entity Handling

FilterQL automatically handles complex entity relationships:

```java
// Entity structure
@Entity
public class User {
    @Id
    private Long id;
    private String name;
    
    @ManyToOne(fetch = FetchType.LAZY)
    private Department department;
    
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Project> projects;
}

@Entity
public class Department {
    @Id
    private Long id;
    private String name;
    
    @ManyToOne(fetch = FetchType.LAZY)
    private Manager manager;
}

@Entity
public class Project {
    @Id
    private Long id;
    private String name;
    private String status;
    
    @ManyToOne
    private User user;
}

// Property definitions with deep nesting
public enum UserProperty implements PropertyRef, PathShape {
    NAME("name"),
    DEPARTMENT_NAME("department.name"),
    MANAGER_NAME("department.manager.name"),
    MANAGER_EMAIL("department.manager.email"),
    PROJECT_NAMES("projects.name"),
    ACTIVE_PROJECT_COUNT("projects.status"); // For custom handling
    
    // ... implementation
}
```

### Collection Queries

Handle collection filtering with EXISTS subqueries:

```java
@Repository
public class UserDao {
    
    public List<User> findUsersWithProjectStatus(String status) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> cq = cb.createQuery(User.class);
        Root<User> root = cq.from(User.class);
        
        // Subquery for projects with specific status
        Subquery<Long> subquery = cq.subquery(Long.class);
        Root<Project> projectRoot = subquery.from(Project.class);
        subquery.select(projectRoot.get("id"))
               .where(cb.and(
                   cb.equal(projectRoot.get("user"), root),
                   cb.equal(projectRoot.get("status"), status)
               ));
        
        cq.where(cb.exists(subquery));
        
        return entityManager.createQuery(cq).getResultList();
    }
    
    // With FilterQL
    public List<User> findUsersWithFilteredProjects(FilterRequest<UserProperty> request) {
        // FilterQL automatically generates appropriate joins/subqueries
        // based on the property paths
        
        FilterRequest<UserProperty> projectFilter = FilterRequest.<UserProperty>builder()
            .filter("active-projects", UserProperty.PROJECT_STATUS, Operator.EQUALS, "ACTIVE")
            .filter("recent-projects", UserProperty.PROJECT_CREATED, Operator.GREATER_THAN, 
                    Date.from(Instant.now().minus(30, ChronoUnit.DAYS)))
            .combineWith("active-projects AND recent-projects")
            .build();
        
        return findUsers(projectFilter);
    }
}
```

### Custom Operators

Extend FilterQL with custom operators:

```java
public enum CustomOperator implements OperatorType {
    SOUNDS_LIKE("SOUNDEX"),
    WITHIN_DISTANCE("DISTANCE"),
    FULL_TEXT_SEARCH("FTS");
    
    private final String code;
    
    CustomOperator(String code) {
        this.code = code;
    }
    
    @Override
    public String getCode() {
        return code;
    }
}

// Custom condition adapter builder
public class CustomConditionAdapterBuilder<T, P extends Enum<P> & PropertyRef & PathShape> 
       extends ConditionAdapterBuilder<T, P> {
    
    @Override
    protected Predicate buildPredicate(FilterDefinition<P> filter, 
                                     Root<T> root, 
                                     CriteriaQuery<?> query, 
                                     CriteriaBuilder cb) {
        
        if (filter.getOperator() instanceof CustomOperator) {
            CustomOperator customOp = (CustomOperator) filter.getOperator();
            
            switch (customOp) {
                case SOUNDS_LIKE:
                    return buildSoundsLikePredicate(filter, root, cb);
                case WITHIN_DISTANCE:
                    return buildDistancePredicate(filter, root, cb);
                case FULL_TEXT_SEARCH:
                    return buildFullTextPredicate(filter, root, cb);
                default:
                    return super.buildPredicate(filter, root, query, cb);
            }
        }
        
        return super.buildPredicate(filter, root, query, cb);
    }
    
    private Predicate buildSoundsLikePredicate(FilterDefinition<P> filter, 
                                             Root<T> root, 
                                             CriteriaBuilder cb) {
        Path<?> path = resolvePath(root, filter.getProperty().getPath());
        String value = (String) filter.getValue();
        
        // Use database-specific SOUNDEX function
        Expression<String> soundexPath = cb.function("SOUNDEX", String.class, path);
        Expression<String> soundexValue = cb.function("SOUNDEX", String.class, cb.literal(value));
        
        return cb.equal(soundexPath, soundexValue);
    }
}
```

## Performance Optimization

### Query Hints

Add JPA query hints for performance tuning:

```java
@Repository
public class OptimizedUserDao {
    
    public List<User> findUsersOptimized(FilterRequest<UserProperty> request) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> cq = cb.createQuery(User.class);
        Root<User> root = cq.from(User.class);
        
        // Apply filters
        Predicate predicate = buildFilterPredicate(request, root, cq, cb);
        cq.where(predicate);
        
        TypedQuery<User> query = entityManager.createQuery(cq);
        
        // Add performance hints
        query.setHint("org.hibernate.cacheable", true);
        query.setHint("org.hibernate.fetchSize", 50);
        query.setHint("org.hibernate.timeout", 30);
        
        return query.getResultList();
    }
}
```

### Fetch Strategies

Control entity loading with explicit fetch joins:

```java
public List<User> findUsersWithDepartments(FilterRequest<UserProperty> request) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<User> cq = cb.createQuery(User.class);
    Root<User> root = cq.from(User.class);
    
    // Explicit fetch join to avoid N+1 queries
    root.fetch("department", JoinType.LEFT);
    
    // Apply filters
    if (request != null && !request.getFilters().isEmpty()) {
        Predicate predicate = buildFilterPredicate(request, root, cq, cb);
        cq.where(predicate);
    }
    
    return entityManager.createQuery(cq).getResultList();
}
```

### Batch Processing

Handle large result sets efficiently:

```java
@Repository
public class BatchUserDao {
    
    public void processUsersInBatches(FilterRequest<UserProperty> request, 
                                    Consumer<List<User>> processor,
                                    int batchSize) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> cq = cb.createQuery(User.class);
        Root<User> root = cq.from(User.class);
        
        if (request != null && !request.getFilters().isEmpty()) {
            Predicate predicate = buildFilterPredicate(request, root, cq, cb);
            cq.where(predicate);
        }
        
        TypedQuery<User> query = entityManager.createQuery(cq);
        
        int offset = 0;
        List<User> batch;
        
        do {
            batch = query.setFirstResult(offset)
                         .setMaxResults(batchSize)
                         .getResultList();
            
            if (!batch.isEmpty()) {
                processor.accept(batch);
                entityManager.clear(); // Clear persistence context
            }
            
            offset += batchSize;
        } while (batch.size() == batchSize);
    }
}
```

## Native Query Integration

Combine FilterQL with native queries when needed:

```java
@Repository
public class HybridUserDao {
    
    public List<User> findUsersWithNativeOptimization(
            FilterRequest<UserProperty> request,
            String additionalNativeWhere) {
        
        // Build FilterQL predicate first
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> cq = cb.createQuery(User.class);
        Root<User> root = cq.from(User.class);
        
        Predicate filterPredicate = buildFilterPredicate(request, root, cq, cb);
        
        // Convert to native SQL for complex optimizations
        String jpql = buildJPQL(cq, filterPredicate);
        String nativeSQL = convertToNativeSQL(jpql, additionalNativeWhere);
        
        Query nativeQuery = entityManager.createNativeQuery(nativeSQL, User.class);
        return nativeQuery.getResultList();
    }
    
    private String convertToNativeSQL(String jpql, String additionalWhere) {
        // Implementation depends on your specific database and requirements
        // This is a simplified example
        String sql = jpql.replace("FROM User u", "FROM users u")
                        .replace("u.name", "u.user_name")
                        .replace("u.department.name", "d.dept_name");
        
        if (additionalWhere != null) {
            sql += " AND " + additionalWhere;
        }
        
        return sql;
    }
}
```

## Testing

### Unit Testing

```java
@ExtendWith(MockitoExtension.class)
public class UserDaoTest {
    
    @Mock
    private EntityManager entityManager;
    
    @Mock
    private CriteriaBuilder criteriaBuilder;
    
    @Mock
    private CriteriaQuery<User> criteriaQuery;
    
    @Mock
    private Root<User> root;
    
    @InjectMocks
    private UserDao userDao;
    
    @Test
    public void testFindUsersWithFilter() {
        // Setup mocks
        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(User.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(User.class)).thenReturn(root);
        
        FilterRequest<UserProperty> request = FilterRequest.<UserProperty>builder()
            .filter("name", UserProperty.NAME, Operator.EQUALS, "John")
            .combineWith("name")
            .build();
        
        // Execute
        userDao.findUsers(request);
        
        // Verify interactions
        verify(entityManager).getCriteriaBuilder();
        verify(criteriaQuery).where(any(Predicate.class));
    }
}
```

### Integration Testing

```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserDaoIntegrationTest {
    
    @Autowired
    private TestEntityManager testEntityManager;
    
    @Autowired
    private UserDao userDao;
    
    @Test
    public void testComplexFilterQuery() {
        // Setup test data
        Department engineering = new Department("Engineering");
        testEntityManager.persistAndFlush(engineering);
        
        User john = new User("John", "john@example.com", 30);
        john.setDepartment(engineering);
        testEntityManager.persistAndFlush(john);
        
        User jane = new User("Jane", "jane@example.com", 25);
        testEntityManager.persistAndFlush(jane);
        
        // Create complex filter
        FilterRequest<UserProperty> request = FilterRequest.<UserProperty>builder()
            .filter("senior", UserProperty.AGE, Operator.GREATER_THAN, 28)
            .filter("engineering", UserProperty.DEPARTMENT_NAME, Operator.EQUALS, "Engineering")
            .combineWith("senior AND engineering")
            .build();
        
        // Execute and verify
        List<User> results = userDao.findUsers(request);
        
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("John");
    }
}
```

## Migration from Criteria API

If you're migrating from existing Criteria API code:

### Before (Traditional Criteria API)

```java
public List<User> findUsers(String name, Integer minAge, String department) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<User> cq = cb.createQuery(User.class);
    Root<User> root = cq.from(User.class);
    
    List<Predicate> predicates = new ArrayList<>();
    
    if (name != null) {
        predicates.add(cb.like(cb.lower(root.get("name")), 
                              "%" + name.toLowerCase() + "%"));
    }
    
    if (minAge != null) {
        predicates.add(cb.greaterThanOrEqualTo(root.get("age"), minAge));
    }
    
    if (department != null) {
        Join<User, Department> deptJoin = root.join("department");
        predicates.add(cb.equal(deptJoin.get("name"), department));
    }
    
    if (!predicates.isEmpty()) {
        cq.where(cb.and(predicates.toArray(new Predicate[0])));
    }
    
    return entityManager.createQuery(cq).getResultList();
}
```

### After (FilterQL)

```java
public List<User> findUsers(String name, Integer minAge, String department) {
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
    
    if (conditions.isEmpty()) {
        return entityManager.createQuery("FROM User", User.class).getResultList();
    }
    
    FilterRequest<UserProperty> request = builder
        .combineWith(String.join(" AND ", conditions))
        .build();
    
    return findUsers(request);
}
```

## Next Steps

- [Spring Data Guide](spring-adapter.md) - Compare with Spring adapter
- [Advanced Usage](advanced-usage.md) - Custom operators and extensions
- [API Reference](api-reference.md) - Complete API documentation