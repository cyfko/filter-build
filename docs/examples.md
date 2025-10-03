---
title: Exemples Pratiques
description: Collection d'exemples réels et patterns d'utilisation FilterQL
sidebar_position: 6
---

# Exemples Pratiques FilterQL

> **Découvrez FilterQL à travers des exemples concrets et des patterns éprouvés**

---

## Vue d'Ensemble

Cette page présente une collection complète d'exemples pratiques couvrant :

- **Exemples Basic** : Premiers pas et concepts fondamentaux
- **Patterns Avancés** : Techniques de filtrage complexes
- **Intégrations Réelles** : Cas d'usage concrets avec Spring Boot
- **API REST** : Endpoints dynamiques avec filtrage
- **Performance** : Optimisations et bonnes pratiques

---

## Exemples Basic

### 1. Filtrage Simple d'Entité

**Entité User** :
```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(unique = true)
    private String email;
    
    private Integer age;
    
    @Enumerated(EnumType.STRING)
    private UserStatus status;
    
    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;
    
    @Column(name = "created_date")
    private LocalDateTime createdDate;
    
    // Constructors, getters, setters...
}

public enum UserStatus {
    ACTIVE, INACTIVE, PENDING, BLOCKED
}
```

**PropertyReference** :
```java
public enum UserPropertyRef implements PropertyReference {
    // Propriétés simples
    NAME(String.class, Set.of(Op.EQ, Op.MATCHES, Op.CONTAINS)),
    EMAIL(String.class, Set.of(Op.EQ, Op.CONTAINS)),
    AGE(Integer.class, Set.of(Op.EQ, Op.GT, Op.LT, Op.GTE, Op.LTE)),
    STATUS(UserStatus.class, Set.of(Op.EQ, Op.IN)),
    CREATED_DATE(LocalDateTime.class, Set.of(Op.EQ, Op.GT, Op.LT, Op.BETWEEN)),
    
    // Propriétés de relation
    DEPARTMENT_NAME(String.class, Set.of(Op.EQ, Op.CONTAINS)),
    DEPARTMENT_CODE(String.class, Set.of(Op.EQ));

    private final Class<?> type;
    private final Set<Op> supportedOperators;

    UserPropertyRef(Class<?> type, Set<Op> supportedOperators) {
        this.type = type;
        this.supportedOperators = Set.copyOf(supportedOperators);
    }

    @Override
    public Class<?> getType() { return type; }

    @Override
    public Set<Op> getSupportedOperators() { return supportedOperators; }
}
```

**Exemples de Filtrage** :

```java
@Service
public class UserService {
    
    private final UserRepository userRepository;
    private final FilterContext<User, UserPropertyRef> filterContext;
    
    /**
     * Recherche utilisateurs par nom exact
     */
    public List<User> findUsersByName(String name) {
        FilterDefinition<UserPropertyRef> nameFilter = new FilterDefinition<>(
            UserPropertyRef.NAME, Op.EQ, name
        );
        
        Condition condition = filterContext.addCondition("nameFilter", nameFilter);
        Specification<User> spec = filterContext.toSpecification(condition);
        
        return userRepository.findAll(spec);
    }
    
    /**
     * Recherche utilisateurs par tranche d'âge
     */
    public List<User> findUsersByAgeRange(int minAge, int maxAge) {
        FilterRequest<UserPropertyRef> request = FilterRequest.<UserPropertyRef>builder()
            .filter("minAge", new FilterDefinition<>(UserPropertyRef.AGE, Op.GTE, minAge))
            .filter("maxAge", new FilterDefinition<>(UserPropertyRef.AGE, Op.LTE, maxAge))
            .combineWith("minAge & maxAge")
            .build();
            
        Condition condition = filterContext.toCondition(request);
        Specification<User> spec = filterContext.toSpecification(condition);
        
        return userRepository.findAll(spec);
    }
    
    /**
     * Recherche utilisateurs avec critères multiples
     */
    public List<User> findUsersWithCriteria(String namePattern, 
                                          Set<UserStatus> statuses, 
                                          String departmentName) {
        FilterRequest.Builder<UserPropertyRef> builder = FilterRequest.<UserPropertyRef>builder();
        StringBuilder expression = new StringBuilder();
        
        // Nom (pattern matching)
        if (namePattern != null && !namePattern.trim().isEmpty()) {
            builder.filter("namePattern", new FilterDefinition<>(
                UserPropertyRef.NAME, Op.CONTAINS, namePattern
            ));
            expression.append("namePattern");
        }
        
        // Status (multiple values)
        if (statuses != null && !statuses.isEmpty()) {
            builder.filter("statusFilter", new FilterDefinition<>(
                UserPropertyRef.STATUS, Op.IN, statuses
            ));
            if (expression.length() > 0) expression.append(" & ");
            expression.append("statusFilter");
        }
        
        // Département
        if (departmentName != null && !departmentName.trim().isEmpty()) {
            builder.filter("deptFilter", new FilterDefinition<>(
                UserPropertyRef.DEPARTMENT_NAME, Op.EQ, departmentName
            ));
            if (expression.length() > 0) expression.append(" & ");
            expression.append("deptFilter");
        }
        
        if (expression.length() == 0) {
            return userRepository.findAll(); // No filters = all results
        }
        
        FilterRequest<UserPropertyRef> request = builder
            .combineWith(expression.toString())
            .build();
            
        Condition condition = filterContext.toCondition(request);
        Specification<User> spec = filterContext.toSpecification(condition);
        
        return userRepository.findAll(spec);
    }
}
```

### 2. Configuration FilterContext

```java
@Configuration
public class FilterConfig {
    
    @Bean
    public FilterContext<User, UserPropertyRef> userFilterContext() {
        return new FilterContext<>(
            User.class,
            UserPropertyRef.class,
            this::mapUserProperty
        );
    }
    
    /**
     * Mapping PropertyReference vers chemins JPA
     */
    private Object mapUserProperty(FilterDefinition<UserPropertyRef> definition) {
        return switch (definition.ref()) {
            case NAME -> "name";
            case EMAIL -> "email"; 
            case AGE -> "age";
            case STATUS -> "status";
            case CREATED_DATE -> "createdDate";
            
            // Navigation dans les relations
            case DEPARTMENT_NAME -> "department.name";
            case DEPARTMENT_CODE -> "department.code";
        };
    }
}
```

---

## Patterns Avancés

### 1. Filtrage avec Relations Complexes

**Entités avec Relations** :
```java
@Entity
public class Order {
    @Id
    private Long id;
    
    @ManyToOne
    private Customer customer;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items = new ArrayList<>();
    
    private BigDecimal totalAmount;
    private LocalDateTime orderDate;
    private OrderStatus status;
}

@Entity 
public class Customer {
    @Id
    private Long id;
    private String name;
    private String email;
    
    @ManyToOne
    private Country country;
    
    @OneToMany(mappedBy = "customer")
    private List<Order> orders = new ArrayList<>();
}

@Entity
public class OrderItem {
    @Id
    private Long id;
    
    @ManyToOne
    private Order order;
    
    @ManyToOne
    private Product product;
    
    private Integer quantity;
    private BigDecimal unitPrice;
}
```

**PropertyReference Avancé** :
```java
public enum OrderPropertyRef implements PropertyReference {
    // Propriétés Order
    ID(Long.class, Set.of(Op.EQ, Op.IN)),
    TOTAL_AMOUNT(BigDecimal.class, Set.of(Op.EQ, Op.GT, Op.LT, Op.BETWEEN)),
    ORDER_DATE(LocalDateTime.class, Set.of(Op.EQ, Op.GT, Op.LT, Op.BETWEEN)),
    STATUS(OrderStatus.class, Set.of(Op.EQ, Op.IN)),
    
    // Relations Customer
    CUSTOMER_NAME(String.class, Set.of(Op.EQ, Op.CONTAINS)),
    CUSTOMER_EMAIL(String.class, Set.of(Op.EQ, Op.CONTAINS)),
    CUSTOMER_COUNTRY(String.class, Set.of(Op.EQ)),
    
    // Relations Items/Products  
    PRODUCT_NAME(String.class, Set.of(Op.EQ, Op.CONTAINS)),
    PRODUCT_CATEGORY(String.class, Set.of(Op.EQ, Op.IN)),
    ITEM_QUANTITY(Integer.class, Set.of(Op.GT, Op.LT)),
    
    // Calculs/Aggregations
    HAS_MINIMUM_AMOUNT(BigDecimal.class, Set.of(Op.GT)),
    CONTAINS_PRODUCT(String.class, Set.of(Op.CONTAINS));

    // Implementation PropertyReference...
}
```

**Mapping Relations Complexes** :
```java
@Configuration
public class OrderFilterConfig {
    
    @Bean
    public FilterContext<Order, OrderPropertyRef> orderFilterContext() {
        return new FilterContext<>(
            Order.class,
            OrderPropertyRef.class,
            this::mapOrderProperty
        );
    }
    
    private Object mapOrderProperty(FilterDefinition<OrderPropertyRef> definition) {
        return switch (definition.ref()) {
            // Propriétés directes
            case ID -> "id";
            case TOTAL_AMOUNT -> "totalAmount";
            case ORDER_DATE -> "orderDate";
            case STATUS -> "status";
            
            // Relations simples Customer
            case CUSTOMER_NAME -> "customer.name";
            case CUSTOMER_EMAIL -> "customer.email";
            case CUSTOMER_COUNTRY -> "customer.country.name";
            
            // Relations collections (nécessite EXISTS subquery)
            case PRODUCT_NAME -> new ProductNameSubquery(definition);
            case PRODUCT_CATEGORY -> new ProductCategorySubquery(definition);
            case ITEM_QUANTITY -> new ItemQuantitySubquery(definition);
            
            // Logique métier complexe
            case HAS_MINIMUM_AMOUNT -> new MinimumAmountPredicate(definition);
            case CONTAINS_PRODUCT -> new ContainsProductPredicate(definition);
        };
    }
}
```

**Custom PredicateResolver** :
```java
/**
 * Resolver pour filtrer commandes contenant un produit spécifique
 */
public class ContainsProductPredicate implements PredicateResolver<Order> {
    
    private final FilterDefinition<OrderPropertyRef> definition;
    
    public ContainsProductPredicate(FilterDefinition<OrderPropertyRef> definition) {
        this.definition = definition;
    }
    
    @Override
    public jakarta.persistence.criteria.Predicate resolve(Root<Order> root, 
                                                         CriteriaQuery<?> query, 
                                                         CriteriaBuilder cb) {
        String productName = (String) definition.value();
        
        // Subquery EXISTS pour vérifier si Order contient Product
        Subquery<Long> subquery = query.subquery(Long.class);
        Root<OrderItem> itemRoot = subquery.from(OrderItem.class);
        
        subquery.select(itemRoot.get("id"))
               .where(
                   cb.and(
                       cb.equal(itemRoot.get("order"), root),
                       cb.like(
                           cb.lower(itemRoot.get("product").get("name")),
                           "%" + productName.toLowerCase() + "%"
                       )
                   )
               );
        
        return cb.exists(subquery);
    }
}
```

### 2. Filtrage avec Aggregations

**Recherche Commandes par Montant Total** :
```java
public class MinimumAmountPredicate implements PredicateResolver<Order> {
    
    private final FilterDefinition<OrderPropertyRef> definition;
    
    @Override
    public jakarta.persistence.criteria.Predicate resolve(Root<Order> root, 
                                                         CriteriaQuery<?> query, 
                                                         CriteriaBuilder cb) {
        BigDecimal minimumAmount = (BigDecimal) definition.value();
        
        // Subquery pour calculer total items
        Subquery<BigDecimal> subquery = query.subquery(BigDecimal.class);
        Root<OrderItem> itemRoot = subquery.from(OrderItem.class);
        
        // SUM(quantity * unitPrice)
        Expression<BigDecimal> itemTotal = cb.prod(
            itemRoot.get("quantity").as(BigDecimal.class),
            itemRoot.get("unitPrice")
        );
        
        subquery.select(cb.sum(itemTotal))
               .where(cb.equal(itemRoot.get("order"), root));
        
        return switch (definition.operator()) {
            case GT -> cb.gt(subquery, minimumAmount);
            case GTE -> cb.ge(subquery, minimumAmount);
            case LT -> cb.lt(subquery, minimumAmount);
            case LTE -> cb.le(subquery, minimumAmount);
            case EQ -> cb.equal(subquery, minimumAmount);
            default -> throw new UnsupportedOperationException(
                "Operator " + definition.operator() + " not supported for aggregation"
            );
        };
    }
}
```

---

## Intégrations Réelles

### 1. API REST avec Filtrage Dynamique

**Controller REST** :
```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    private final UserService userService;
    
    /**
     * Endpoint avec filtrage par query parameters
     */
    @GetMapping
    public ResponseEntity<Page<UserDTO>> getUsers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Integer minAge,
            @RequestParam(required = false) Integer maxAge,
            @RequestParam(required = false) Set<UserStatus> status,
            @RequestParam(required = false) String department,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        // Construction dynamique FilterRequest
        FilterRequest<UserPropertyRef> filterRequest = buildUserFilter(
            name, email, minAge, maxAge, status, department
        );
        
        // Pagination et tri
        Sort sort = Sort.by(
            "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC,
            sortBy
        );
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // Exécution recherche
        Page<User> users = userService.findUsers(filterRequest, pageable);
        Page<UserDTO> userDTOs = users.map(this::toDTO);
        
        return ResponseEntity.ok(userDTOs);
    }
    
    /**
     * Construction dynamique FilterRequest depuis parameters
     */
    private FilterRequest<UserPropertyRef> buildUserFilter(String name, 
                                                          String email,
                                                          Integer minAge, 
                                                          Integer maxAge,
                                                          Set<UserStatus> status, 
                                                          String department) {
        FilterRequest.Builder<UserPropertyRef> builder = FilterRequest.<UserPropertyRef>builder();
        List<String> conditions = new ArrayList<>();
        
        if (name != null && !name.trim().isEmpty()) {
            builder.filter("name", new FilterDefinition<>(
                UserPropertyRef.NAME, Op.CONTAINS, name.trim()
            ));
            conditions.add("name");
        }
        
        if (email != null && !email.trim().isEmpty()) {
            builder.filter("email", new FilterDefinition<>(
                UserPropertyRef.EMAIL, Op.CONTAINS, email.trim()
            ));
            conditions.add("email");
        }
        
        if (minAge != null) {
            builder.filter("minAge", new FilterDefinition<>(
                UserPropertyRef.AGE, Op.GTE, minAge
            ));
            conditions.add("minAge");
        }
        
        if (maxAge != null) {
            builder.filter("maxAge", new FilterDefinition<>(
                UserPropertyRef.AGE, Op.LTE, maxAge
            ));
            conditions.add("maxAge");
        }
        
        if (status != null && !status.isEmpty()) {
            builder.filter("status", new FilterDefinition<>(
                UserPropertyRef.STATUS, Op.IN, status
            ));
            conditions.add("status");
        }
        
        if (department != null && !department.trim().isEmpty()) {
            builder.filter("department", new FilterDefinition<>(
                UserPropertyRef.DEPARTMENT_NAME, Op.EQ, department.trim()
            ));
            conditions.add("department");
        }
        
        // Combinaison AND de tous les filtres
        if (!conditions.isEmpty()) {
            String expression = String.join(" & ", conditions);
            builder.combineWith(expression);
        }
        
        return builder.build();
    }
    
    /**
     * Endpoint avec FilterRequest en POST body
     */
    @PostMapping("/search")
    public ResponseEntity<List<UserDTO>> searchUsers(
            @RequestBody @Valid UserSearchRequest searchRequest) {
        
        FilterRequest<UserPropertyRef> filterRequest = searchRequest.toFilterRequest();
        List<User> users = userService.findUsers(filterRequest);
        List<UserDTO> userDTOs = users.stream().map(this::toDTO).toList();
        
        return ResponseEntity.ok(userDTOs);
    }
    
    private UserDTO toDTO(User user) {
        return new UserDTO(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getAge(),
            user.getStatus(),
            user.getDepartment() != null ? user.getDepartment().getName() : null,
            user.getCreatedDate()
        );
    }
}
```

**DTO pour Recherche Avancée** :
```java
@JsonDeserialize(builder = UserSearchRequest.Builder.class)
public record UserSearchRequest(
    @Nullable String namePattern,
    @Nullable String emailPattern,
    @Nullable Integer minAge,
    @Nullable Integer maxAge,
    @Nullable Set<UserStatus> statuses,
    @Nullable String departmentName,
    @Nullable LocalDateTime createdAfter,
    @Nullable LocalDateTime createdBefore,
    @NotNull LogicalOperator operator // AND, OR
) {
    
    public enum LogicalOperator { AND, OR }
    
    /**
     * Conversion vers FilterRequest FilterQL
     */
    public FilterRequest<UserPropertyRef> toFilterRequest() {
        FilterRequest.Builder<UserPropertyRef> builder = FilterRequest.<UserPropertyRef>builder();
        List<String> conditions = new ArrayList<>();
        
        if (namePattern != null) {
            builder.filter("namePattern", new FilterDefinition<>(
                UserPropertyRef.NAME, Op.CONTAINS, namePattern
            ));
            conditions.add("namePattern");
        }
        
        if (emailPattern != null) {
            builder.filter("emailPattern", new FilterDefinition<>(
                UserPropertyRef.EMAIL, Op.CONTAINS, emailPattern
            ));
            conditions.add("emailPattern");
        }
        
        if (minAge != null) {
            builder.filter("minAge", new FilterDefinition<>(
                UserPropertyRef.AGE, Op.GTE, minAge
            ));
            conditions.add("minAge");
        }
        
        if (maxAge != null) {
            builder.filter("maxAge", new FilterDefinition<>(
                UserPropertyRef.AGE, Op.LTE, maxAge
            ));
            conditions.add("maxAge");
        }
        
        if (statuses != null && !statuses.isEmpty()) {
            builder.filter("statuses", new FilterDefinition<>(
                UserPropertyRef.STATUS, Op.IN, statuses
            ));
            conditions.add("statuses");
        }
        
        if (departmentName != null) {
            builder.filter("department", new FilterDefinition<>(
                UserPropertyRef.DEPARTMENT_NAME, Op.EQ, departmentName
            ));
            conditions.add("department");
        }
        
        if (createdAfter != null) {
            builder.filter("createdAfter", new FilterDefinition<>(
                UserPropertyRef.CREATED_DATE, Op.GTE, createdAfter
            ));
            conditions.add("createdAfter");
        }
        
        if (createdBefore != null) {
            builder.filter("createdBefore", new FilterDefinition<>(
                UserPropertyRef.CREATED_DATE, Op.LTE, createdBefore
            ));
            conditions.add("createdBefore");
        }
        
        if (!conditions.isEmpty()) {
            String separator = operator == LogicalOperator.AND ? " & " : " | ";
            String expression = String.join(separator, conditions);
            builder.combineWith(expression);
        }
        
        return builder.build();
    }
    
    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {
        // Jackson builder for deserialization
    }
}
```

### 2. Service avec Cache et Performance

```java
@Service
@Transactional(readOnly = true)
public class UserService {
    
    private final UserRepository userRepository;
    private final FilterContext<User, UserPropertyRef> filterContext;
    private final RedisTemplate<String, Object> redisTemplate;
    
    /**
     * Recherche avec cache Redis
     */
    @Cacheable(value = "user-filters", key = "#filterRequest.hashCode()")
    public List<User> findUsers(FilterRequest<UserPropertyRef> filterRequest) {
        if (filterRequest.filters().isEmpty()) {
            return userRepository.findAll();
        }
        
        Condition condition = filterContext.toCondition(filterRequest);
        Specification<User> spec = filterContext.toSpecification(condition);
        
        return userRepository.findAll(spec);
    }
    
    /**
     * Recherche paginée avec cache
     */
    @Cacheable(value = "user-pages", 
               key = "#filterRequest.hashCode() + '_' + #pageable.hashCode()")
    public Page<User> findUsers(FilterRequest<UserPropertyRef> filterRequest, 
                               Pageable pageable) {
        if (filterRequest.filters().isEmpty()) {
            return userRepository.findAll(pageable);
        }
        
        Condition condition = filterContext.toCondition(filterRequest);
        Specification<User> spec = filterContext.toSpecification(condition);
        
        return userRepository.findAll(spec, pageable);
    }
    
    /**
     * Comptage avec cache
     */
    @Cacheable(value = "user-counts", key = "#filterRequest.hashCode()")
    public long countUsers(FilterRequest<UserPropertyRef> filterRequest) {
        if (filterRequest.filters().isEmpty()) {
            return userRepository.count();
        }
        
        Condition condition = filterContext.toCondition(filterRequest);
        Specification<User> spec = filterContext.toSpecification(condition);
        
        return userRepository.count(spec);
    }
    
    /**
     * Recherche avec hints JPA pour performance
     */
    public List<User> findUsersOptimized(FilterRequest<UserPropertyRef> filterRequest) {
        if (filterRequest.filters().isEmpty()) {
            return userRepository.findAllWithDepartment(); // Custom query avec JOIN
        }
        
        Condition condition = filterContext.toCondition(filterRequest);
        Specification<User> spec = filterContext.toSpecification(condition);
        
        // Application query hints pour performance
        return userRepository.findAll(spec, hint -> {
            hint.put("javax.persistence.fetchgraph", "User.withDepartment");
            hint.put("org.hibernate.readOnly", true);
        });
    }
}
```

---

## Patterns de Performance

### 1. Optimisation Requêtes JPA

**Repository avec Query Hints** :
```java
@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    
    /**
     * Query avec JOIN explicite pour éviter N+1
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.department WHERE u.status = :status")
    List<User> findByStatusWithDepartment(@Param("status") UserStatus status);
    
    /**
     * Query optimisée avec Entity Graph
     */
    @EntityGraph(attributePaths = {"department", "orders"})
    @Query("SELECT u FROM User u WHERE u.createdDate >= :since")
    List<User> findRecentUsersWithRelations(@Param("since") LocalDateTime since);
    
    /**
     * Projection DTO pour éviter hydratation complète
     */
    @Query("SELECT new com.example.dto.UserSummaryDTO(u.id, u.name, u.email, d.name) " +
           "FROM User u LEFT JOIN u.department d")
    List<UserSummaryDTO> findAllSummaries();
}
```

**Custom Repository avec Optimisations** :
```java
@Repository
public class OptimizedUserRepository {
    
    private final EntityManager entityManager;
    private final FilterContext<User, UserPropertyRef> filterContext;
    
    /**
     * Recherche avec batch fetch et hints
     */
    public List<User> findUsersOptimized(FilterRequest<UserPropertyRef> filterRequest) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> query = cb.createQuery(User.class);
        Root<User> root = query.from(User.class);
        
        // Fetch joins pour éviter N+1
        root.fetch("department", JoinType.LEFT);
        
        // Application filtres FilterQL
        if (!filterRequest.filters().isEmpty()) {
            Condition condition = filterContext.toCondition(filterRequest);
            PredicateResolver<User> resolver = filterContext.toResolver(User.class, condition);
            jakarta.persistence.criteria.Predicate predicate = resolver.resolve(root, query, cb);
            query.where(predicate);
        }
        
        // Configuration TypedQuery avec hints
        TypedQuery<User> typedQuery = entityManager.createQuery(query);
        typedQuery.setHint(QueryHints.HINT_PASS_DISTINCT_THROUGH, false);
        typedQuery.setHint(QueryHints.HINT_BATCH_SIZE, 100);
        typedQuery.setHint(QueryHints.HINT_READ_ONLY, true);
        
        return typedQuery.getResultList();
    }
    
    /**
     * Comptage optimisé sans fetch
     */
    public long countUsersOptimized(FilterRequest<UserPropertyRef> filterRequest) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<User> root = query.from(User.class);
        
        query.select(cb.count(root));
        
        if (!filterRequest.filters().isEmpty()) {
            Condition condition = filterContext.toCondition(filterRequest);
            PredicateResolver<User> resolver = filterContext.toResolver(User.class, condition);
            jakarta.persistence.criteria.Predicate predicate = resolver.resolve(root, query, cb);
            query.where(predicate);
        }
        
        return entityManager.createQuery(query).getSingleResult();
    }
}
```

### 2. Cache Strategy

**Configuration Cache Redis** :
```java
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheManager.Builder builder = RedisCacheManager
            .RedisCacheManagerBuilder
            .fromConnectionFactory(connectionFactory)
            .cacheDefaults(cacheConfiguration());
            
        return builder.build();
    }
    
    private RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
            .serializeKeysWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer()))
            .entryTtl(Duration.ofMinutes(30))
            .disableCachingNullValues();
    }
    
    /**
     * Cache key generator custom pour FilterRequest
     */
    @Bean
    public KeyGenerator filterRequestKeyGenerator() {
        return (target, method, params) -> {
            StringBuilder key = new StringBuilder();
            key.append(method.getName());
            
            for (Object param : params) {
                if (param instanceof FilterRequest<?> filterRequest) {
                    key.append("_").append(filterRequest.hashCode());
                } else if (param instanceof Pageable pageable) {
                    key.append("_page").append(pageable.getPageNumber())
                       .append("_size").append(pageable.getPageSize())
                       .append("_sort").append(pageable.getSort().toString());
                }
            }
            
            return key.toString();
        };
    }
}
```

**Service avec Cache Eviction** :
```java
@Service
public class UserCacheService {
    
    /**
     * Invalidation cache après modification
     */
    @CacheEvict(value = {"user-filters", "user-pages", "user-counts"}, allEntries = true)
    public User saveUser(User user) {
        return userRepository.save(user);
    }
    
    @CacheEvict(value = {"user-filters", "user-pages", "user-counts"}, allEntries = true)
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }
    
    /**
     * Warm up cache avec requêtes communes
     */
    @EventListener(ApplicationReadyEvent.class)
    public void warmUpCache() {
        // Cache requêtes fréquentes
        List<FilterRequest<UserPropertyRef>> commonRequests = Arrays.asList(
            // Utilisateurs actifs
            FilterRequest.<UserPropertyRef>builder()
                .filter("active", new FilterDefinition<>(UserPropertyRef.STATUS, Op.EQ, UserStatus.ACTIVE))
                .combineWith("active")
                .build(),
                
            // Utilisateurs récents (derniers 30 jours)
            FilterRequest.<UserPropertyRef>builder()
                .filter("recent", new FilterDefinition<>(
                    UserPropertyRef.CREATED_DATE, 
                    Op.GTE, 
                    LocalDateTime.now().minusDays(30)
                ))
                .combineWith("recent")
                .build()
        );
        
        commonRequests.forEach(this::preloadCache);
    }
    
    private void preloadCache(FilterRequest<UserPropertyRef> request) {
        try {
            userService.findUsers(request);
            userService.countUsers(request);
        } catch (Exception e) {
            log.warn("Failed to preload cache for request: {}", request, e);
        }
    }
}
```

---

## Patterns de Test

### 1. Tests Unitaires FilterQL

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private FilterContext<User, UserPropertyRef> filterContext;
    
    @InjectMocks
    private UserService userService;
    
    @Test
    void shouldFindUsersByName() {
        // Given
        String searchName = "John";
        FilterDefinition<UserPropertyRef> nameFilter = new FilterDefinition<>(
            UserPropertyRef.NAME, Op.EQ, searchName
        );
        
        Condition mockCondition = mock(Condition.class);
        Specification<User> mockSpec = mock(Specification.class);
        List<User> expectedUsers = Arrays.asList(createTestUser("John Doe"));
        
        when(filterContext.addCondition("nameFilter", nameFilter)).thenReturn(mockCondition);
        when(filterContext.toSpecification(mockCondition)).thenReturn(mockSpec);
        when(userRepository.findAll(mockSpec)).thenReturn(expectedUsers);
        
        // When
        List<User> result = userService.findUsersByName(searchName);
        
        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("John Doe");
        
        verify(filterContext).addCondition("nameFilter", nameFilter);
        verify(filterContext).toSpecification(mockCondition);
        verify(userRepository).findAll(mockSpec);
    }
    
    @Test
    void shouldFindUsersByAgeRange() {
        // Given
        int minAge = 25, maxAge = 35;
        FilterRequest<UserPropertyRef> expectedRequest = FilterRequest.<UserPropertyRef>builder()
            .filter("minAge", new FilterDefinition<>(UserPropertyRef.AGE, Op.GTE, minAge))
            .filter("maxAge", new FilterDefinition<>(UserPropertyRef.AGE, Op.LTE, maxAge))
            .combineWith("minAge & maxAge")
            .build();
            
        Condition mockCondition = mock(Condition.class);
        Specification<User> mockSpec = mock(Specification.class);
        List<User> expectedUsers = Arrays.asList(createTestUser("Jane Doe", 30));
        
        when(filterContext.toCondition(any(FilterRequest.class))).thenReturn(mockCondition);
        when(filterContext.toSpecification(mockCondition)).thenReturn(mockSpec);
        when(userRepository.findAll(mockSpec)).thenReturn(expectedUsers);
        
        // When
        List<User> result = userService.findUsersByAgeRange(minAge, maxAge);
        
        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAge()).isEqualTo(30);
        
        ArgumentCaptor<FilterRequest> requestCaptor = ArgumentCaptor.forClass(FilterRequest.class);
        verify(filterContext).toCondition(requestCaptor.capture());
        
        FilterRequest<UserPropertyRef> capturedRequest = requestCaptor.getValue();
        assertThat(capturedRequest.filters()).hasSize(2);
        assertThat(capturedRequest.combineWith()).isEqualTo("minAge & maxAge");
    }
    
    private User createTestUser(String name) {
        return createTestUser(name, 25);
    }
    
    private User createTestUser(String name, Integer age) {
        User user = new User();
        user.setName(name);
        user.setAge(age);
        user.setEmail(name.toLowerCase().replace(" ", ".") + "@example.com");
        user.setStatus(UserStatus.ACTIVE);
        return user;
    }
}
```

### 2. Tests d'Intégration Spring

```java
@SpringBootTest
@DataJpaTest
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "logging.level.org.hibernate.SQL=DEBUG"
})
class UserFilterIntegrationTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private FilterContext<User, UserPropertyRef> filterContext;
    
    @BeforeEach
    void setUp() {
        // Test data setup
        Department engineering = new Department("Engineering", "ENG");
        Department sales = new Department("Sales", "SALES");
        entityManager.persistAndFlush(engineering);
        entityManager.persistAndFlush(sales);
        
        User user1 = new User("John Doe", "john@example.com", 30, UserStatus.ACTIVE, engineering);
        User user2 = new User("Jane Smith", "jane@example.com", 25, UserStatus.ACTIVE, sales);
        User user3 = new User("Bob Johnson", "bob@example.com", 35, UserStatus.INACTIVE, engineering);
        
        entityManager.persistAndFlush(user1);
        entityManager.persistAndFlush(user2);
        entityManager.persistAndFlush(user3);
        entityManager.clear();
    }
    
    @Test
    void shouldFilterByNameContains() {
        // Given
        FilterDefinition<UserPropertyRef> nameFilter = new FilterDefinition<>(
            UserPropertyRef.NAME, Op.CONTAINS, "john"
        );
        
        // When
        Condition condition = filterContext.addCondition("nameFilter", nameFilter);
        Specification<User> spec = filterContext.toSpecification(condition);
        List<User> result = userRepository.findAll(spec);
        
        // Then
        assertThat(result).hasSize(2); // John Doe, Bob Johnson
        assertThat(result).extracting(User::getName)
                         .containsExactlyInAnyOrder("John Doe", "Bob Johnson");
    }
    
    @Test
    void shouldFilterByAgeRange() {
        // Given
        FilterRequest<UserPropertyRef> request = FilterRequest.<UserPropertyRef>builder()
            .filter("minAge", new FilterDefinition<>(UserPropertyRef.AGE, Op.GTE, 30))
            .filter("maxAge", new FilterDefinition<>(UserPropertyRef.AGE, Op.LTE, 35))
            .combineWith("minAge & maxAge")
            .build();
        
        // When
        Condition condition = filterContext.toCondition(request);
        Specification<User> spec = filterContext.toSpecification(condition);
        List<User> result = userRepository.findAll(spec);
        
        // Then
        assertThat(result).hasSize(2); // John (30), Bob (35)
        assertThat(result).extracting(User::getAge)
                         .containsExactlyInAnyOrder(30, 35);
    }
    
    @Test
    void shouldFilterByDepartmentName() {
        // Given
        FilterDefinition<UserPropertyRef> deptFilter = new FilterDefinition<>(
            UserPropertyRef.DEPARTMENT_NAME, Op.EQ, "Engineering"
        );
        
        // When
        Condition condition = filterContext.addCondition("deptFilter", deptFilter);
        Specification<User> spec = filterContext.toSpecification(condition);
        List<User> result = userRepository.findAll(spec);
        
        // Then
        assertThat(result).hasSize(2); // John, Bob
        assertThat(result).extracting(user -> user.getDepartment().getName())
                         .containsOnly("Engineering");
    }
    
    @Test
    void shouldCombineMultipleFilters() {
        // Given - Users actifs du département Engineering
        FilterRequest<UserPropertyRef> request = FilterRequest.<UserPropertyRef>builder()
            .filter("status", new FilterDefinition<>(UserPropertyRef.STATUS, Op.EQ, UserStatus.ACTIVE))
            .filter("dept", new FilterDefinition<>(UserPropertyRef.DEPARTMENT_NAME, Op.EQ, "Engineering"))
            .combineWith("status & dept")
            .build();
        
        // When
        Condition condition = filterContext.toCondition(request);
        Specification<User> spec = filterContext.toSpecification(condition);
        List<User> result = userRepository.findAll(spec);
        
        // Then
        assertThat(result).hasSize(1); // Only John (Bob is INACTIVE)
        assertThat(result.get(0).getName()).isEqualTo("John Doe");
        assertThat(result.get(0).getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(result.get(0).getDepartment().getName()).isEqualTo("Engineering");
    }
}
```

### 3. Tests Performance

```java
@SpringBootTest
@EnableTestContainers
class UserFilterPerformanceTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private FilterContext<User, UserPropertyRef> filterContext;
    
    @BeforeEach
    void setUp() {
        // Création dataset large pour tests performance
        createLargeDataset();
    }
    
    @Test
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    void shouldPerformFastFilteringOnLargeDataset() {
        // Given
        FilterRequest<UserPropertyRef> request = FilterRequest.<UserPropertyRef>builder()
            .filter("status", new FilterDefinition<>(UserPropertyRef.STATUS, Op.EQ, UserStatus.ACTIVE))
            .filter("minAge", new FilterDefinition<>(UserPropertyRef.AGE, Op.GTE, 25))
            .combineWith("status & minAge")
            .build();
        
        // When
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        
        Condition condition = filterContext.toCondition(request);
        Specification<User> spec = filterContext.toSpecification(condition);
        List<User> result = userRepository.findAll(spec);
        
        stopWatch.stop();
        
        // Then
        assertThat(result).isNotEmpty();
        assertThat(stopWatch.getTotalTimeMillis()).isLessThan(2000); // < 2 seconds
        
        log.info("Query executed in {} ms, returned {} results", 
                stopWatch.getTotalTimeMillis(), result.size());
    }
    
    private void createLargeDataset() {
        List<Department> departments = createDepartments();
        List<User> users = new ArrayList<>();
        
        for (int i = 0; i < 10000; i++) {
            User user = new User(
                "User " + i,
                "user" + i + "@example.com",
                20 + (i % 40), // Age 20-59
                i % 3 == 0 ? UserStatus.ACTIVE : UserStatus.INACTIVE,
                departments.get(i % departments.size())
            );
            users.add(user);
            
            // Batch insert pour performance
            if (i % 100 == 0) {
                userRepository.saveAll(users);
                users.clear();
            }
        }
        
        if (!users.isEmpty()) {
            userRepository.saveAll(users);
        }
    }
    
    private List<Department> createDepartments() {
        List<Department> departments = Arrays.asList(
            new Department("Engineering", "ENG"),
            new Department("Sales", "SALES"),
            new Department("Marketing", "MKT"),
            new Department("HR", "HR"),
            new Department("Finance", "FIN")
        );
        
        return departmentRepository.saveAll(departments);
    }
}
```

---

## Bonnes Pratiques

### 1. Structure PropertyReference

```java
// ✅ BIEN - Groupement logique et validation
public enum ProductPropertyRef implements PropertyReference {
    // Propriétés de base
    ID(Long.class, Set.of(Op.EQ, Op.IN)),
    NAME(String.class, Set.of(Op.EQ, Op.CONTAINS, Op.MATCHES)),
    DESCRIPTION(String.class, Set.of(Op.CONTAINS)),
    
    // Propriétés numériques
    PRICE(BigDecimal.class, Set.of(Op.EQ, Op.GT, Op.LT, Op.BETWEEN)),
    STOCK_QUANTITY(Integer.class, Set.of(Op.EQ, Op.GT, Op.LT)),
    
    // Propriétés enum
    STATUS(ProductStatus.class, Set.of(Op.EQ, Op.IN)),
    CATEGORY(ProductCategory.class, Set.of(Op.EQ, Op.IN)),
    
    // Propriétés date
    CREATED_DATE(LocalDateTime.class, Set.of(Op.EQ, Op.GT, Op.LT, Op.BETWEEN)),
    UPDATED_DATE(LocalDateTime.class, Set.of(Op.GT, Op.LT)),
    
    // Relations
    SUPPLIER_NAME(String.class, Set.of(Op.EQ, Op.CONTAINS)),
    CATEGORY_NAME(String.class, Set.of(Op.EQ, Op.CONTAINS)),
    
    // Propriétés calculées/custom
    IS_IN_STOCK(Boolean.class, Set.of(Op.EQ)),
    HAS_DISCOUNT(Boolean.class, Set.of(Op.EQ));
    
    // Implementation standard...
}

// ❌ ÉVITER - Trop permissif
public enum BadPropertyRef implements PropertyReference {
    NAME(String.class, Set.of(Op.values())), // Tous opérateurs = danger
    PRICE(Object.class, Set.of(Op.EQ));      // Type Object = pas de type safety
}
```

### 2. Gestion d'Erreurs

```java
@ControllerAdvice
public class FilterQLExceptionHandler {
    
    @ExceptionHandler(FilterValidationException.class)
    public ResponseEntity<ErrorResponse> handleFilterValidation(FilterValidationException ex) {
        ErrorResponse error = new ErrorResponse(
            "FILTER_VALIDATION_ERROR",
            ex.getMessage(),
            extractValidationDetails(ex)
        );
        return ResponseEntity.badRequest().body(error);
    }
    
    @ExceptionHandler(DSLSyntaxException.class)
    public ResponseEntity<ErrorResponse> handleDSLSyntax(DSLSyntaxException ex) {
        ErrorResponse error = new ErrorResponse(
            "DSL_SYNTAX_ERROR", 
            "Invalid filter expression: " + ex.getMessage(),
            Map.of("expression", ex.getExpression(), "position", ex.getPosition())
        );
        return ResponseEntity.badRequest().body(error);
    }
    
    private Map<String, Object> extractValidationDetails(FilterValidationException ex) {
        return Map.of(
            "property", ex.getPropertyReference().name(),
            "operator", ex.getOperator().name(),
            "value", ex.getValue(),
            "allowedOperators", ex.getPropertyReference().getSupportedOperators()
        );
    }
}
```

### 3. Monitoring et Métriques

```java
@Service
public class FilterQLMetricsService {
    
    private final MeterRegistry meterRegistry;
    private final Counter filterRequestsCounter;
    private final Timer filterExecutionTimer;
    
    public FilterQLMetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.filterRequestsCounter = Counter.builder("filterql.requests")
                .description("Number of filter requests")
                .register(meterRegistry);
        this.filterExecutionTimer = Timer.builder("filterql.execution.time")
                .description("Filter execution time")
                .register(meterRegistry);
    }
    
    public <T> List<T> executeWithMetrics(String operation, Supplier<List<T>> filterOperation) {
        filterRequestsCounter.increment(Tags.of("operation", operation));
        
        return filterExecutionTimer.recordCallable(() -> {
            try {
                return filterOperation.get();
            } catch (Exception e) {
                meterRegistry.counter("filterql.errors", 
                    "operation", operation,
                    "error", e.getClass().getSimpleName())
                    .increment();
                throw e;
            }
        });
    }
}
```

---

## Next Steps

### Après Maîtrise des Exemples

1. **[Architecture Deep Dive](./architecture.md)** : Compréhension interne FilterQL
2. **[Contributing Guide](./contributing.md)** : Contribuer au projet et optimisations
3. **[Migration Guide](./changelog.md)** : Mise à jour versions

### Ressources Complémentaires

- **[GitHub Repository](https://github.com/cyfko/filter-build)** : Code source et issues
- **[Spring Data JPA Docs](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)** : Specifications et Criteria API
- **[Jakarta Persistence](https://jakarta.ee/specifications/persistence/)** : Standards JPA

---

*Besoin d'aide sur un exemple spécifique ? Consultez les [issues GitHub](https://github.com/cyfko/filter-build/issues) ou créez une nouvelle discussion !*