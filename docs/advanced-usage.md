---
layout: default
title: Advanced Usage
description: Advanced FilterQL features including custom operators, performance optimization, and extension patterns
---

# Advanced Usage

This guide covers advanced FilterQL features for power users who need custom operators, performance optimization, and extension patterns.

## Custom Operators

### Creating Custom Operators

Extend FilterQL with domain-specific operators:

```java
public enum CustomOperator implements OperatorType {
    SOUNDS_LIKE("SOUNDEX"),
    WITHIN_DISTANCE("DISTANCE"), 
    REGEX_MATCH("REGEX"),
    FULL_TEXT_SEARCH("FTS"),
    DATE_RANGE("DATE_RANGE"),
    JSON_CONTAINS("JSON_CONTAINS");
    
    private final String code;
    
    CustomOperator(String code) {
        this.code = code;
    }
    
    @Override
    public String getCode() {
        return code;
    }
}
```

### Custom Condition Adapters

Implement custom logic for your operators:

```java
public class CustomConditionAdapterBuilder<T, P extends Enum<P> & PropertyRef & PathShape> 
       extends ConditionAdapterBuilder<T, P> {
    
    @Override
    protected Predicate buildPredicate(FilterDefinition<P> filter, 
                                     Root<T> root, 
                                     CriteriaQuery<?> query, 
                                     CriteriaBuilder cb) {
        
        if (filter.getOperator() instanceof CustomOperator) {
            return buildCustomPredicate(filter, root, query, cb);
        }
        
        return super.buildPredicate(filter, root, query, cb);
    }
    
    private Predicate buildCustomPredicate(FilterDefinition<P> filter,
                                         Root<T> root,
                                         CriteriaQuery<?> query,
                                         CriteriaBuilder cb) {
        CustomOperator operator = (CustomOperator) filter.getOperator();
        Path<?> path = resolvePath(root, filter.getProperty().getPath());
        Object value = filter.getValue();
        
        switch (operator) {
            case SOUNDS_LIKE:
                return buildSoundsLikePredicate(path, value, cb);
            case WITHIN_DISTANCE:
                return buildDistancePredicate(path, value, cb);
            case REGEX_MATCH:
                return buildRegexPredicate(path, value, cb);
            case FULL_TEXT_SEARCH:
                return buildFullTextPredicate(path, value, cb);
            case DATE_RANGE:
                return buildDateRangePredicate(path, value, cb);
            case JSON_CONTAINS:
                return buildJsonContainsPredicate(path, value, cb);
            default:
                throw new UnsupportedOperationException("Operator not implemented: " + operator);
        }
    }
    
    private Predicate buildSoundsLikePredicate(Path<?> path, Object value, CriteriaBuilder cb) {
        Expression<String> soundexPath = cb.function("SOUNDEX", String.class, path);
        Expression<String> soundexValue = cb.function("SOUNDEX", String.class, cb.literal(value.toString()));
        return cb.equal(soundexPath, soundexValue);
    }
    
    private Predicate buildDistancePredicate(Path<?> path, Object value, CriteriaBuilder cb) {
        // Assuming value is a DistanceFilter object
        DistanceFilter distanceFilter = (DistanceFilter) value;
        
        // Using PostGIS ST_DWithin function as example
        Expression<Boolean> withinDistance = cb.function("ST_DWithin",
            Boolean.class,
            path,
            cb.literal(distanceFilter.getPoint()),
            cb.literal(distanceFilter.getDistance())
        );
        
        return cb.isTrue(withinDistance);
    }
    
    private Predicate buildRegexPredicate(Path<?> path, Object value, CriteriaBuilder cb) {
        // Database-specific regex function
        Expression<Boolean> regexMatch = cb.function("REGEXP",
            Boolean.class,
            path,
            cb.literal(value.toString())
        );
        
        return cb.isTrue(regexMatch);
    }
    
    private Predicate buildFullTextPredicate(Path<?> path, Object value, CriteriaBuilder cb) {
        // PostgreSQL full-text search example
        Expression<Boolean> ftsMatch = cb.function("to_tsvector",
            Boolean.class,
            path
        );
        
        Expression<Boolean> ftsQuery = cb.function("plainto_tsquery",
            Boolean.class,
            cb.literal(value.toString())
        );
        
        return cb.isTrue(cb.function("@@", Boolean.class, ftsMatch, ftsQuery));
    }
    
    private Predicate buildDateRangePredicate(Path<?> path, Object value, CriteriaBuilder cb) {
        DateRange range = (DateRange) value;
        
        List<Predicate> predicates = new ArrayList<>();
        
        if (range.getStart() != null) {
            predicates.add(cb.greaterThanOrEqualTo((Path<Date>) path, range.getStart()));
        }
        
        if (range.getEnd() != null) {
            predicates.add(cb.lessThanOrEqualTo((Path<Date>) path, range.getEnd()));
        }
        
        return cb.and(predicates.toArray(new Predicate[0]));
    }
    
    private Predicate buildJsonContainsPredicate(Path<?> path, Object value, CriteriaBuilder cb) {
        // PostgreSQL JSON contains example
        Expression<Boolean> jsonContains = cb.function("jsonb_contains",
            Boolean.class,
            path,
            cb.literal(value.toString())
        );
        
        return cb.isTrue(jsonContains);
    }
}
```

### Supporting Classes

```java
public class DistanceFilter {
    private final String point; // WKT format: "POINT(longitude latitude)"
    private final double distance; // in meters
    
    public DistanceFilter(double longitude, double latitude, double distance) {
        this.point = String.format("POINT(%f %f)", longitude, latitude);
        this.distance = distance;
    }
    
    // getters...
}

public class DateRange {
    private final Date start;
    private final Date end;
    
    public DateRange(Date start, Date end) {
        this.start = start;
        this.end = end;
    }
    
    // getters...
}
```

## Performance Optimization

### Query Analysis and Optimization

```java
@Component
public class FilterPerformanceAnalyzer {
    
    private static final Logger logger = LoggerFactory.getLogger(FilterPerformanceAnalyzer.class);
    
    public <T> List<T> executeWithAnalysis(FilterRequest<?> request, 
                                          Supplier<List<T>> queryExecutor) {
        long startTime = System.currentTimeMillis();
        
        try {
            List<T> results = queryExecutor.get();
            long executionTime = System.currentTimeMillis() - startTime;
            
            logPerformanceMetrics(request, results.size(), executionTime);
            
            if (executionTime > 1000) { // Log slow queries
                logger.warn("Slow query detected: {}ms for filter: {}", 
                           executionTime, request.getCombineWith());
            }
            
            return results;
        } catch (Exception e) {
            logger.error("Query execution failed for filter: {}", 
                        request.getCombineWith(), e);
            throw e;
        }
    }
    
    private void logPerformanceMetrics(FilterRequest<?> request, int resultCount, long executionTime) {
        logger.info("Query executed: filter='{}', results={}, time={}ms",
                   request.getCombineWith(), resultCount, executionTime);
    }
}
```

### Caching Strategies

#### Filter Result Caching

```java
@Service
@Transactional(readOnly = true)
public class CachedUserService {
    
    private final UserRepository userRepository;
    private final CacheManager cacheManager;
    
    @Cacheable(value = "user-search", 
               key = "#request.hashCode()", 
               condition = "#request.filters.size() <= 5",
               unless = "#result.size() > 1000")
    public List<User> findUsers(FilterRequest<UserProperty> request) {
        Specification<User> spec = SpecificationBuilder.toSpecification(request);
        return userRepository.findAll(spec);
    }
    
    @CacheEvict(value = "user-search", allEntries = true)
    @EventListener
    public void handleUserChanged(UserChangedEvent event) {
        // Invalidate cache when user data changes
    }
    
    // Cache warming
    @PostConstruct
    public void warmCache() {
        List<FilterRequest<UserProperty>> commonFilters = getCommonFilters();
        commonFilters.forEach(this::findUsers);
    }
    
    private List<FilterRequest<UserProperty>> getCommonFilters() {
        return Arrays.asList(
            // Active users
            FilterRequest.<UserProperty>builder()
                .filter("active", UserProperty.STATUS, Operator.EQUALS, "ACTIVE")
                .combineWith("active")
                .build(),
            
            // Recent users
            FilterRequest.<UserProperty>builder()
                .filter("recent", UserProperty.CREATED_DATE, Operator.GREATER_THAN, 
                        Date.from(Instant.now().minus(30, ChronoUnit.DAYS)))
                .combineWith("recent")
                .build()
        );
    }
}
```

#### Parsed Expression Caching

```java
@Component
public class FilterTreeCache {
    
    private final LoadingCache<String, FilterTree> cache;
    private final DSLParser parser;
    
    public FilterTreeCache() {
        this.parser = new DSLParser();
        this.cache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(1, TimeUnit.HOURS)
            .recordStats()
            .build(this::parseExpression);
    }
    
    public FilterTree getFilterTree(String expression) {
        try {
            return cache.get(expression);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse expression: " + expression, e);
        }
    }
    
    private FilterTree parseExpression(String expression) throws DSLSyntaxException {
        return parser.parse(expression);
    }
    
    @EventListener
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void logCacheStats() {
        CacheStats stats = cache.stats();
        if (stats.requestCount() > 0) {
            logger.info("Filter cache stats - Hits: {}, Misses: {}, Hit Rate: {:.2f}%",
                       stats.hitCount(), 
                       stats.missCount(), 
                       stats.hitRate() * 100);
        }
    }
}
```

### Database Optimization

#### Index Recommendations

```java
@Component
public class FilterIndexAnalyzer {
    
    public List<String> analyzeIndexNeeds(List<FilterRequest<?>> commonFilters) {
        Map<String, Integer> propertyUsage = new HashMap<>();
        
        for (FilterRequest<?> request : commonFilters) {
            for (FilterDefinition<?> filter : request.getFilters()) {
                String property = filter.getProperty().getPath();
                propertyUsage.merge(property, 1, Integer::sum);
            }
        }
        
        return propertyUsage.entrySet().stream()
            .filter(entry -> entry.getValue() >= 3) // Used in 3+ filters
            .map(entry -> generateIndexRecommendation(entry.getKey()))
            .collect(Collectors.toList());
    }
    
    private String generateIndexRecommendation(String propertyPath) {
        String[] pathParts = propertyPath.split("\\.");
        
        if (pathParts.length == 1) {
            return String.format("CREATE INDEX idx_%s ON %s (%s);", 
                                pathParts[0], getTableName(), pathParts[0]);
        } else {
            // For nested properties, suggest covering index
            return String.format("CREATE INDEX idx_%s ON %s (%s) INCLUDE (%s);",
                                String.join("_", pathParts),
                                getTableName(),
                                pathParts[0] + "_id",
                                String.join(", ", Arrays.copyOfRange(pathParts, 1, pathParts.length)));
        }
    }
    
    private String getTableName() {
        // Derive table name from entity
        return "users"; // Simplified
    }
}
```

## Extension Patterns

### Custom Property Types

```java
// Enum with validation
public enum ValidatedUserProperty implements PropertyRef, PathShape {
    NAME("name", String.class, Arrays.asList(Operator.EQUALS, Operator.CONTAINS)),
    EMAIL("email", String.class, Arrays.asList(Operator.EQUALS, Operator.CONTAINS)),
    AGE("age", Integer.class, Arrays.asList(Operator.EQUALS, Operator.GREATER_THAN, Operator.LESS_THAN)),
    LOCATION("location", String.class, Arrays.asList(CustomOperator.WITHIN_DISTANCE));
    
    private final String path;
    private final Class<?> type;
    private final List<OperatorType> allowedOperators;
    
    ValidatedUserProperty(String path, Class<?> type, List<OperatorType> allowedOperators) {
        this.path = path;
        this.type = type;
        this.allowedOperators = allowedOperators;
    }
    
    @Override
    public String getPath() {
        return path;
    }
    
    public Class<?> getType() {
        return type;
    }
    
    public boolean isOperatorAllowed(OperatorType operator) {
        return allowedOperators.contains(operator);
    }
    
    public void validateValue(Object value) {
        if (value != null && !type.isInstance(value)) {
            throw new IllegalArgumentException(
                String.format("Invalid value type for property %s. Expected %s, got %s",
                             name(), type.getSimpleName(), value.getClass().getSimpleName()));
        }
    }
}
```

### Filter Request Validation

```java
@Component
public class FilterRequestValidator<P extends Enum<P> & PropertyRef & PathShape> {
    
    public ValidationResult validate(FilterRequest<P> request) {
        ValidationResult result = new ValidationResult();
        
        // Validate each filter
        for (FilterDefinition<P> filter : request.getFilters()) {
            validateFilter(filter, result);
        }
        
        // Validate DSL expression
        validateExpression(request.getCombineWith(), request.getFilters(), result);
        
        return result;
    }
    
    private void validateFilter(FilterDefinition<P> filter, ValidationResult result) {
        P property = filter.getProperty();
        
        // Type validation
        if (property instanceof ValidatedUserProperty) {
            ValidatedUserProperty validatedProp = (ValidatedUserProperty) property;
            
            // Check operator compatibility
            if (!validatedProp.isOperatorAllowed(filter.getOperator())) {
                result.addError(String.format(
                    "Operator %s not allowed for property %s",
                    filter.getOperator(), property.name()));
            }
            
            // Check value type
            try {
                validatedProp.validateValue(filter.getValue());
            } catch (IllegalArgumentException e) {
                result.addError(e.getMessage());
            }
        }
        
        // Business rule validation
        validateBusinessRules(filter, result);
    }
    
    private void validateBusinessRules(FilterDefinition<P> filter, ValidationResult result) {
        // Example: Age must be positive
        if ("age".equals(filter.getProperty().getPath()) && 
            filter.getValue() instanceof Integer) {
            Integer age = (Integer) filter.getValue();
            if (age < 0 || age > 150) {
                result.addError("Age must be between 0 and 150");
            }
        }
        
        // Example: Email format validation
        if ("email".equals(filter.getProperty().getPath()) && 
            filter.getOperator() == Operator.EQUALS) {
            String email = (String) filter.getValue();
            if (!isValidEmail(email)) {
                result.addError("Invalid email format: " + email);
            }
        }
    }
    
    private void validateExpression(String expression, 
                                  List<FilterDefinition<P>> filters, 
                                  ValidationResult result) {
        try {
            DSLParser parser = new DSLParser();
            FilterTree tree = parser.parse(expression);
            
            // Check that all referenced filters exist
            Set<String> referencedIds = extractReferencedIds(tree);
            Set<String> definedIds = filters.stream()
                .map(FilterDefinition::getId)
                .collect(Collectors.toSet());
            
            for (String referencedId : referencedIds) {
                if (!definedIds.contains(referencedId)) {
                    result.addError("Referenced filter not defined: " + referencedId);
                }
            }
            
        } catch (DSLSyntaxException e) {
            result.addError("Invalid DSL expression: " + e.getMessage());
        }
    }
    
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
    
    private Set<String> extractReferencedIds(FilterTree tree) {
        // Implementation to extract all referenced filter IDs from the tree
        // This would traverse the tree and collect leaf node identifiers
        return new HashSet<>(); // Simplified
    }
}

public class ValidationResult {
    private final List<String> errors = new ArrayList<>();
    private final List<String> warnings = new ArrayList<>();
    
    public void addError(String error) {
        errors.add(error);
    }
    
    public void addWarning(String warning) {
        warnings.add(warning);
    }
    
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
    
    public List<String> getErrors() {
        return Collections.unmodifiableList(errors);
    }
    
    public List<String> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }
}
```

### Framework Integration Helpers

#### Spring Boot Auto-Configuration

```java
@Configuration
@ConditionalOnClass({FilterRequest.class, JpaRepository.class})
@EnableConfigurationProperties(FilterQLProperties.class)
public class FilterQLAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public FilterTreeCache filterTreeCache(FilterQLProperties properties) {
        return new FilterTreeCache(properties.getCache());
    }
    
    @Bean
    @ConditionalOnMissingBean
    public FilterPerformanceAnalyzer filterPerformanceAnalyzer() {
        return new FilterPerformanceAnalyzer();
    }
    
    @Bean
    @ConditionalOnProperty(prefix = "filterql", name = "validation.enabled", havingValue = "true")
    public FilterRequestValidator filterRequestValidator() {
        return new FilterRequestValidator();
    }
    
    @Configuration
    @ConditionalOnWebMvc
    static class WebMvcConfiguration {
        
        @Bean
        public FilterRequestArgumentResolver filterRequestArgumentResolver() {
            return new FilterRequestArgumentResolver();
        }
        
        @Bean
        public WebMvcConfigurer filterQLWebMvcConfigurer(
                FilterRequestArgumentResolver resolver) {
            return new WebMvcConfigurer() {
                @Override
                public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
                    resolvers.add(resolver);
                }
            };
        }
    }
}

@ConfigurationProperties(prefix = "filterql")
public class FilterQLProperties {
    
    private Cache cache = new Cache();
    private Validation validation = new Validation();
    private Performance performance = new Performance();
    
    public static class Cache {
        private int maxSize = 1000;
        private Duration expireAfterWrite = Duration.ofHours(1);
        
        // getters and setters
    }
    
    public static class Validation {
        private boolean enabled = true;
        private boolean strictMode = false;
        
        // getters and setters
    }
    
    public static class Performance {
        private Duration slowQueryThreshold = Duration.ofSeconds(1);
        private boolean logSlowQueries = true;
        
        // getters and setters
    }
    
    // getters and setters for main properties
}
```

#### Custom Argument Resolver

```java
public class FilterRequestArgumentResolver implements HandlerMethodArgumentResolver {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return FilterRequest.class.isAssignableFrom(parameter.getParameterType());
    }
    
    @Override
    public FilterRequest<?> resolveArgument(MethodParameter parameter,
                                          ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest,
                                          WebDataBinderFactory binderFactory) throws Exception {
        
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        
        if ("GET".equals(request.getMethod())) {
            return buildFromQueryParameters(webRequest, parameter);
        } else {
            return buildFromRequestBody(request, parameter);
        }
    }
    
    private FilterRequest<?> buildFromQueryParameters(NativeWebRequest webRequest, 
                                                    MethodParameter parameter) {
        // Implementation to build FilterRequest from query parameters
        // This would parse query parameters and build filters dynamically
        return FilterRequest.builder().build(); // Simplified
    }
    
    private FilterRequest<?> buildFromRequestBody(HttpServletRequest request, 
                                                MethodParameter parameter) throws IOException {
        // Read and parse JSON request body
        String body = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);
        return objectMapper.readValue(body, FilterRequest.class);
    }
}
```

## Testing Advanced Features

### Custom Operator Testing

```java
@Test
public void testCustomSoundsLikeOperator() {
    FilterRequest<UserProperty> request = FilterRequest.<UserProperty>builder()
        .filter("sounds-like-john", UserProperty.NAME, CustomOperator.SOUNDS_LIKE, "Jon")
        .combineWith("sounds-like-john")
        .build();
    
    // Test with custom adapter builder that includes SOUNDS_LIKE
    ContextAdapter<User, UserProperty> context = new ContextAdapter<>(
        new CustomConditionAdapterBuilder<>()
    );
    
    request.getFilters().forEach(context::addCondition);
    
    DSLParser parser = new DSLParser();
    FilterTree tree = parser.parse(request.getCombineWith());
    Condition condition = tree.generate(context);
    
    assertThat(condition).isInstanceOf(ConditionAdapter.class);
    
    // Test predicate generation
    CriteriaBuilder cb = mock(CriteriaBuilder.class);
    CriteriaQuery<?> query = mock(CriteriaQuery.class);
    Root<User> root = mock(Root.class);
    
    Predicate predicate = ((ConditionAdapter<User>) condition).toPredicate(root, query, cb);
    assertThat(predicate).isNotNull();
}
```

### Performance Testing

```java
@Test
public void testFilterPerformance() {
    List<FilterRequest<UserProperty>> requests = generateTestRequests(100);
    
    long startTime = System.currentTimeMillis();
    
    for (FilterRequest<UserProperty> request : requests) {
        Specification<User> spec = SpecificationBuilder.toSpecification(request);
        // Simulate query execution
        mockRepository.findAll(spec);
    }
    
    long totalTime = System.currentTimeMillis() - startTime;
    double avgTime = totalTime / (double) requests.size();
    
    assertThat(avgTime).isLessThan(10.0); // Average under 10ms per filter
}
```

## Next Steps

This covers the most advanced features of FilterQL. For additional customization:

1. **Review the source code** to understand internal APIs
2. **Contribute custom operators** back to the project
3. **Share performance optimization patterns** with the community
4. **Extend framework support** for other JPA implementations

For questions about advanced usage, please check the [GitHub Issues](https://github.com/cyfko/filter-build/issues) or start a discussion.