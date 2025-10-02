---
layout: page
title: Exemples
description: Exemples pratiques d'utilisation de FilterQL
nav_order: 4
category: examples
permalink: /examples/
show_toc: true
---

# 💡 Exemples FilterQL

Découvrez des exemples concrets d'utilisation de FilterQL dans différents contextes.

## 🛒 E-commerce - Recherche de Produits

{% include alert.html type="tip" title="Cas d'usage populaire" content="L'exemple e-commerce est l'un des cas d'usage les plus courants pour FilterQL." %}

### Configuration de Base

{% include code_block.html title="Entité Product" language="java" file="src/main/java/model/Product.java" %}
```java
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String category;
    
    @Column(nullable = false)
    private BigDecimal price;
    
    @Column(name = "stock_quantity")
    private Integer stockQuantity;
    
    @Column(name = "is_promoted")
    private Boolean isPromoted;
    
    @Column(name = "created_date")
    private LocalDateTime createdDate;
    
    @Column(name = "brand")
    private String brand;
    
    @Column(name = "rating")
    private Double rating;
    
    // Getters, setters...
}
```

### Énumération des Propriétés

{% include code_block.html title="ProductPropertyRef" language="java" file="src/main/java/model/ProductPropertyRef.java" %}
```java
public enum ProductPropertyRef implements PropertyReference {
    ID("id"),
    NAME("name"),
    CATEGORY("category"),
    PRICE("price"),
    STOCK_QUANTITY("stockQuantity"),
    IS_PROMOTED("isPromoted"),
    CREATED_DATE("createdDate"),
    BRAND("brand"),
    RATING("rating");
    
    private final String propertyPath;
    
    ProductPropertyRef(String propertyPath) {
        this.propertyPath = propertyPath;
    }
    
    @Override
    public String getPropertyPath() {
        return propertyPath;
    }
}
```

### Service de Recherche

{% include code_block.html title="ProductSearchService" language="java" file="src/main/java/service/ProductSearchService.java" %}
```java
@Service
@Transactional(readOnly = true)
public class ProductSearchService {
    
    private final ProductRepository productRepository;
    private final FilterResolver<ProductPropertyRef> filterResolver;
    
    public ProductSearchService(ProductRepository productRepository,
                               FilterResolver<ProductPropertyRef> filterResolver) {
        this.productRepository = productRepository;
        this.filterResolver = filterResolver;
    }
    
    public Page<Product> searchProducts(ProductSearchRequest searchRequest, Pageable pageable) {
        FilterRequest<ProductPropertyRef> filterRequest = buildFilterRequest(searchRequest);
        Specification<Product> specification = filterResolver.resolve(filterRequest);
        
        return productRepository.findAll(specification, pageable);
    }
    
    private FilterRequest<ProductPropertyRef> buildFilterRequest(ProductSearchRequest search) {
        FilterRequest.Builder<ProductPropertyRef> builder = FilterRequest.builder();
        
        // Filtrage par nom (recherche textuelle)
        if (StringUtils.hasText(search.getQuery())) {
            builder.filter("name", new FilterDefinition<>(
                ProductPropertyRef.NAME, 
                Op.LIKE, 
                "%" + search.getQuery() + "%"
            ));
        }
        
        // Filtrage par catégories
        if (search.getCategories() != null && !search.getCategories().isEmpty()) {
            builder.filter("categories", new FilterDefinition<>(
                ProductPropertyRef.CATEGORY, 
                Op.IN, 
                search.getCategories()
            ));
        }
        
        // Filtrage par fourchette de prix
        if (search.getMinPrice() != null || search.getMaxPrice() != null) {
            List<BigDecimal> priceRange = Arrays.asList(
                search.getMinPrice() != null ? search.getMinPrice() : BigDecimal.ZERO,
                search.getMaxPrice() != null ? search.getMaxPrice() : new BigDecimal("999999")
            );
            builder.filter("priceRange", new FilterDefinition<>(
                ProductPropertyRef.PRICE, 
                Op.RANGE, 
                priceRange
            ));
        }
        
        // Produits en stock uniquement
        if (search.isInStockOnly()) {
            builder.filter("inStock", new FilterDefinition<>(
                ProductPropertyRef.STOCK_QUANTITY, 
                Op.GT, 
                0
            ));
        }
        
        // Produits en promotion
        if (search.isPromotedOnly()) {
            builder.filter("promoted", new FilterDefinition<>(
                ProductPropertyRef.IS_PROMOTED, 
                Op.EQ, 
                true
            ));
        }
        
        // Filtrage par marques
        if (search.getBrands() != null && !search.getBrands().isEmpty()) {
            builder.filter("brands", new FilterDefinition<>(
                ProductPropertyRef.BRAND, 
                Op.IN, 
                search.getBrands()
            ));
        }
        
        // Note minimale
        if (search.getMinRating() != null) {
            builder.filter("minRating", new FilterDefinition<>(
                ProductPropertyRef.RATING, 
                Op.GTE, 
                search.getMinRating()
            ));
        }
        
        // Combinaison logique complexe
        String combination = buildLogicalCombination(search);
        if (StringUtils.hasText(combination)) {
            builder.combineWith(combination);
        }
        
        return builder.build();
    }
    
    private String buildLogicalCombination(ProductSearchRequest search) {
        List<String> conditions = new ArrayList<>();
        
        if (StringUtils.hasText(search.getQuery())) {
            conditions.add("name");
        }
        if (search.getCategories() != null && !search.getCategories().isEmpty()) {
            conditions.add("categories");
        }
        if (search.getMinPrice() != null || search.getMaxPrice() != null) {
            conditions.add("priceRange");
        }
        if (search.getBrands() != null && !search.getBrands().isEmpty()) {
            conditions.add("brands");
        }
        if (search.getMinRating() != null) {
            conditions.add("minRating");
        }
        
        String baseCondition = String.join(" & ", conditions);
        
        // Conditions optionnelles (OR)
        List<String> optionalConditions = new ArrayList<>();
        if (search.isInStockOnly()) {
            optionalConditions.add("inStock");
        }
        if (search.isPromotedOnly()) {
            optionalConditions.add("promoted");
        }
        
        if (!optionalConditions.isEmpty()) {
            String optionalCombination = String.join(" | ", optionalConditions);
            if (StringUtils.hasText(baseCondition)) {
                return "(" + baseCondition + ") & (" + optionalCombination + ")";
            } else {
                return optionalCombination;
            }
        }
        
        return baseCondition;
    }
}
```

### Contrôleur REST

{% include code_block.html title="ProductController" language="java" file="src/main/java/controller/ProductController.java" %}
```java
@RestController
@RequestMapping("/api/products")
@Validated
public class ProductController {
    
    private final ProductSearchService searchService;
    
    public ProductController(ProductSearchService searchService) {
        this.searchService = searchService;
    }
    
    @GetMapping("/search")
    public ResponseEntity<Page<ProductDTO>> searchProducts(
            @Valid ProductSearchRequest searchRequest,
            @PageableDefault(size = 20, sort = "createdDate", direction = Direction.DESC) Pageable pageable) {
        
        Page<Product> products = searchService.searchProducts(searchRequest, pageable);
        Page<ProductDTO> productDTOs = products.map(this::toDTO);
        
        return ResponseEntity.ok(productDTOs);
    }
    
    @GetMapping("/filters/metadata")
    public ResponseEntity<FilterMetadata> getFilterMetadata() {
        FilterMetadata metadata = FilterMetadata.builder()
            .addProperty("name", "string", "Nom du produit")
            .addProperty("category", "enum", "Catégorie", getCategoryValues())
            .addProperty("price", "range", "Prix", "0", "10000")
            .addProperty("brand", "enum", "Marque", getBrandValues())
            .addProperty("rating", "range", "Note", "0", "5")
            .addProperty("inStock", "boolean", "En stock")
            .addProperty("promoted", "boolean", "En promotion")
            .build();
        
        return ResponseEntity.ok(metadata);
    }
    
    private ProductDTO toDTO(Product product) {
        return ProductDTO.builder()
            .id(product.getId())
            .name(product.getName())
            .category(product.getCategory())
            .price(product.getPrice())
            .stockQuantity(product.getStockQuantity())
            .isPromoted(product.getIsPromoted())
            .brand(product.getBrand())
            .rating(product.getRating())
            .build();
    }
    
    private List<String> getCategoryValues() {
        return Arrays.asList("Electronics", "Clothing", "Books", "Home", "Sports");
    }
    
    private List<String> getBrandValues() {
        return Arrays.asList("Apple", "Samsung", "Nike", "Adidas", "Sony");
    }
}
```

## 👥 Gestion RH - Recherche d'Employés

### Entité Employee

{% include code_block.html title="Employee" language="java" file="src/main/java/model/Employee.java" %}
```java
@Entity
@Table(name = "employees")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "first_name", nullable = false)
    private String firstName;
    
    @Column(name = "last_name", nullable = false)
    private String lastName;
    
    @Column(nullable = false)
    private String department;
    
    @Column(nullable = false)
    private String position;
    
    @Column(name = "hire_date")
    private LocalDate hireDate;
    
    @Column(nullable = false)
    private BigDecimal salary;
    
    @Column(name = "performance_rating")
    private Double performanceRating;
    
    @Column(name = "is_remote")
    private Boolean isRemote;
    
    @Enumerated(EnumType.STRING)
    private EmployeeStatus status;
    
    // Relations
    @ManyToOne
    @JoinColumn(name = "manager_id")
    private Employee manager;
    
    @OneToMany(mappedBy = "manager")
    private List<Employee> subordinates;
    
    // Getters, setters...
}

enum EmployeeStatus {
    ACTIVE, INACTIVE, ON_LEAVE, TERMINATED
}
```

### Service de Recherche RH

{% include code_block.html title="EmployeeSearchService" language="java" file="src/main/java/service/EmployeeSearchService.java" %}
```java
@Service
@Transactional(readOnly = true)
public class EmployeeSearchService {
    
    private final EmployeeRepository employeeRepository;
    private final FilterResolver<EmployeePropertyRef> filterResolver;
    
    public List<Employee> findEmployeesByFilters(EmployeeSearchCriteria criteria) {
        FilterRequest<EmployeePropertyRef> filterRequest = buildEmployeeFilters(criteria);
        Specification<Employee> specification = filterResolver.resolve(filterRequest);
        
        return employeeRepository.findAll(specification);
    }
    
    private FilterRequest<EmployeePropertyRef> buildEmployeeFilters(EmployeeSearchCriteria criteria) {
        FilterRequest.Builder<EmployeePropertyRef> builder = FilterRequest.builder();
        
        // Recherche par nom complet
        if (StringUtils.hasText(criteria.getNameQuery())) {
            builder.filter("firstName", new FilterDefinition<>(
                EmployeePropertyRef.FIRST_NAME, 
                Op.LIKE, 
                "%" + criteria.getNameQuery() + "%"
            ));
            builder.filter("lastName", new FilterDefinition<>(
                EmployeePropertyRef.LAST_NAME, 
                Op.LIKE, 
                "%" + criteria.getNameQuery() + "%"
            ));
        }
        
        // Département(s)
        if (criteria.getDepartments() != null && !criteria.getDepartments().isEmpty()) {
            builder.filter("departments", new FilterDefinition<>(
                EmployeePropertyRef.DEPARTMENT, 
                Op.IN, 
                criteria.getDepartments()
            ));
        }
        
        // Fourchette salariale
        if (criteria.getMinSalary() != null || criteria.getMaxSalary() != null) {
            List<BigDecimal> salaryRange = Arrays.asList(
                criteria.getMinSalary() != null ? criteria.getMinSalary() : BigDecimal.ZERO,
                criteria.getMaxSalary() != null ? criteria.getMaxSalary() : new BigDecimal("1000000")
            );
            builder.filter("salaryRange", new FilterDefinition<>(
                EmployeePropertyRef.SALARY, 
                Op.RANGE, 
                salaryRange
            ));
        }
        
        // Performance minimale
        if (criteria.getMinPerformanceRating() != null) {
            builder.filter("performance", new FilterDefinition<>(
                EmployeePropertyRef.PERFORMANCE_RATING, 
                Op.GTE, 
                criteria.getMinPerformanceRating()
            ));
        }
        
        // Ancienneté
        if (criteria.getMinYearsOfService() != null) {
            LocalDate maxHireDate = LocalDate.now().minusYears(criteria.getMinYearsOfService());
            builder.filter("seniority", new FilterDefinition<>(
                EmployeePropertyRef.HIRE_DATE, 
                Op.LTE, 
                maxHireDate
            ));
        }
        
        // Statut
        if (criteria.getStatuses() != null && !criteria.getStatuses().isEmpty()) {
            builder.filter("statuses", new FilterDefinition<>(
                EmployeePropertyRef.STATUS, 
                Op.IN, 
                criteria.getStatuses()
            ));
        }
        
        // Travail à distance
        if (criteria.getIsRemote() != null) {
            builder.filter("remote", new FilterDefinition<>(
                EmployeePropertyRef.IS_REMOTE, 
                Op.EQ, 
                criteria.getIsRemote()
            ));
        }
        
        // Employés avec équipe
        if (criteria.isManagersOnly()) {
            builder.filter("managers", new FilterDefinition<>(
                EmployeePropertyRef.SUBORDINATES_COUNT, 
                Op.GT, 
                0
            ));
        }
        
        // Combinaison logique
        String combination = buildHRLogicalCombination(criteria);
        if (StringUtils.hasText(combination)) {
            builder.combineWith(combination);
        }
        
        return builder.build();
    }
    
    private String buildHRLogicalCombination(EmployeeSearchCriteria criteria) {
        List<String> conditions = new ArrayList<>();
        
        // Conditions obligatoires
        if (StringUtils.hasText(criteria.getNameQuery())) {
            conditions.add("(firstName | lastName)"); // Recherche sur prénom OU nom
        }
        if (criteria.getDepartments() != null && !criteria.getDepartments().isEmpty()) {
            conditions.add("departments");
        }
        if (criteria.getStatuses() != null && !criteria.getStatuses().isEmpty()) {
            conditions.add("statuses");
        }
        
        String mandatoryConditions = String.join(" & ", conditions);
        
        // Conditions de performance (optionnelles mais importantes)
        List<String> performanceConditions = new ArrayList<>();
        if (criteria.getMinSalary() != null || criteria.getMaxSalary() != null) {
            performanceConditions.add("salaryRange");
        }
        if (criteria.getMinPerformanceRating() != null) {
            performanceConditions.add("performance");
        }
        if (criteria.getMinYearsOfService() != null) {
            performanceConditions.add("seniority");
        }
        
        // Conditions spéciales
        List<String> specialConditions = new ArrayList<>();
        if (criteria.getIsRemote() != null) {
            specialConditions.add("remote");
        }
        if (criteria.isManagersOnly()) {
            specialConditions.add("managers");
        }
        
        // Construction de la logique finale
        StringBuilder finalCondition = new StringBuilder();
        
        if (StringUtils.hasText(mandatoryConditions)) {
            finalCondition.append(mandatoryConditions);
        }
        
        if (!performanceConditions.isEmpty()) {
            String perfCond = String.join(" & ", performanceConditions);
            if (finalCondition.length() > 0) {
                finalCondition.append(" & (").append(perfCond).append(")");
            } else {
                finalCondition.append(perfCond);
            }
        }
        
        if (!specialConditions.isEmpty()) {
            String specCond = String.join(" & ", specialConditions);
            if (finalCondition.length() > 0) {
                finalCondition.append(" & (").append(specCond).append(")");
            } else {
                finalCondition.append(specCond);
            }
        }
        
        return finalCondition.toString();
    }
    
    // Requêtes spécialisées
    public List<Employee> findTopPerformers(String department, int limit) {
        FilterRequest<EmployeePropertyRef> request = FilterRequest.<EmployeePropertyRef>builder()
            .filter("department", new FilterDefinition<>(
                EmployeePropertyRef.DEPARTMENT, Op.EQ, department))
            .filter("activeStatus", new FilterDefinition<>(
                EmployeePropertyRef.STATUS, Op.EQ, EmployeeStatus.ACTIVE))
            .filter("highPerformance", new FilterDefinition<>(
                EmployeePropertyRef.PERFORMANCE_RATING, Op.GTE, 4.0))
            .combineWith("department & activeStatus & highPerformance")
            .build();
        
        Specification<Employee> specification = filterResolver.resolve(request);
        Pageable pageable = PageRequest.of(0, limit, 
            Sort.by(Sort.Direction.DESC, "performanceRating"));
        
        return employeeRepository.findAll(specification, pageable).getContent();
    }
    
    public List<Employee> findEligibleForPromotion() {
        LocalDate twoYearsAgo = LocalDate.now().minusYears(2);
        
        FilterRequest<EmployeePropertyRef> request = FilterRequest.<EmployeePropertyRef>builder()
            .filter("activeStatus", new FilterDefinition<>(
                EmployeePropertyRef.STATUS, Op.EQ, EmployeeStatus.ACTIVE))
            .filter("tenureMin", new FilterDefinition<>(
                EmployeePropertyRef.HIRE_DATE, Op.LTE, twoYearsAgo))
            .filter("highPerformance", new FilterDefinition<>(
                EmployeePropertyRef.PERFORMANCE_RATING, Op.GTE, 4.5))
            .filter("notManager", new FilterDefinition<>(
                EmployeePropertyRef.SUBORDINATES_COUNT, Op.EQ, 0))
            .combineWith("activeStatus & tenureMin & highPerformance & notManager")
            .build();
        
        return employeeRepository.findAll(filterResolver.resolve(request));
    }
}
```

## 📊 Analytique Financière

{% include alert.html type="warning" title="Données sensibles" content="Cet exemple utilise des données financières fictives à des fins de démonstration uniquement." %}

### Service d'Analyse Financière

{% include code_block.html title="FinancialAnalyticsService" language="java" file="src/main/java/service/FinancialAnalyticsService.java" %}
```java
@Service
@Transactional(readOnly = true)
public class FinancialAnalyticsService {
    
    private final TransactionRepository transactionRepository;
    private final FilterResolver<TransactionPropertyRef> filterResolver;
    
    public FinancialReport generateReport(FinancialReportRequest request) {
        List<Transaction> transactions = findTransactions(request);
        
        return FinancialReport.builder()
            .transactions(transactions)
            .totalAmount(calculateTotalAmount(transactions))
            .averageAmount(calculateAverageAmount(transactions))
            .transactionCount(transactions.size())
            .dateRange(request.getDateRange())
            .categories(request.getCategories())
            .build();
    }
    
    private List<Transaction> findTransactions(FinancialReportRequest request) {
        FilterRequest<TransactionPropertyRef> filterRequest = buildFinancialFilters(request);
        Specification<Transaction> specification = filterResolver.resolve(filterRequest);
        
        return transactionRepository.findAll(specification);
    }
    
    private FilterRequest<TransactionPropertyRef> buildFinancialFilters(FinancialReportRequest request) {
        FilterRequest.Builder<TransactionPropertyRef> builder = FilterRequest.builder();
        
        // Période d'analyse
        if (request.getStartDate() != null && request.getEndDate() != null) {
            builder.filter("dateRange", new FilterDefinition<>(
                TransactionPropertyRef.TRANSACTION_DATE, 
                Op.RANGE, 
                Arrays.asList(request.getStartDate(), request.getEndDate())
            ));
        }
        
        // Montants significatifs
        if (request.getMinAmount() != null) {
            builder.filter("minAmount", new FilterDefinition<>(
                TransactionPropertyRef.AMOUNT, 
                Op.GTE, 
                request.getMinAmount()
            ));
        }
        
        if (request.getMaxAmount() != null) {
            builder.filter("maxAmount", new FilterDefinition<>(
                TransactionPropertyRef.AMOUNT, 
                Op.LTE, 
                request.getMaxAmount()
            ));
        }
        
        // Types de transactions
        if (request.getTransactionTypes() != null && !request.getTransactionTypes().isEmpty()) {
            builder.filter("types", new FilterDefinition<>(
                TransactionPropertyRef.TYPE, 
                Op.IN, 
                request.getTransactionTypes()
            ));
        }
        
        // Catégories
        if (request.getCategories() != null && !request.getCategories().isEmpty()) {
            builder.filter("categories", new FilterDefinition<>(
                TransactionPropertyRef.CATEGORY, 
                Op.IN, 
                request.getCategories()
            ));
        }
        
        // Transactions internationales
        if (request.isInternationalOnly()) {
            builder.filter("international", new FilterDefinition<>(
                TransactionPropertyRef.IS_INTERNATIONAL, 
                Op.EQ, 
                true
            ));
        }
        
        // Transactions suspectes (montants élevés)
        if (request.isSuspiciousTransactions()) {
            builder.filter("suspicious", new FilterDefinition<>(
                TransactionPropertyRef.AMOUNT, 
                Op.GT, 
                new BigDecimal("10000")
            ));
        }
        
        // Statut de la transaction
        if (request.getStatuses() != null && !request.getStatuses().isEmpty()) {
            builder.filter("statuses", new FilterDefinition<>(
                TransactionPropertyRef.STATUS, 
                Op.IN, 
                request.getStatuses()
            ));
        }
        
        // Combinaison logique pour l'analyse financière
        String combination = buildFinancialLogic(request);
        if (StringUtils.hasText(combination)) {
            builder.combineWith(combination);
        }
        
        return builder.build();
    }
    
    private String buildFinancialLogic(FinancialReportRequest request) {
        List<String> baseConditions = new ArrayList<>();
        
        // Conditions temporelles (obligatoires)
        if (request.getStartDate() != null && request.getEndDate() != null) {
            baseConditions.add("dateRange");
        }
        
        // Conditions de filtrage principal
        List<String> filterConditions = new ArrayList<>();
        if (request.getTransactionTypes() != null && !request.getTransactionTypes().isEmpty()) {
            filterConditions.add("types");
        }
        if (request.getCategories() != null && !request.getCategories().isEmpty()) {
            filterConditions.add("categories");
        }
        if (request.getStatuses() != null && !request.getStatuses().isEmpty()) {
            filterConditions.add("statuses");
        }
        
        // Conditions de montant
        List<String> amountConditions = new ArrayList<>();
        if (request.getMinAmount() != null) {
            amountConditions.add("minAmount");
        }
        if (request.getMaxAmount() != null) {
            amountConditions.add("maxAmount");
        }
        
        // Conditions spéciales (analyse de risque)
        List<String> riskConditions = new ArrayList<>();
        if (request.isInternationalOnly()) {
            riskConditions.add("international");
        }
        if (request.isSuspiciousTransactions()) {
            riskConditions.add("suspicious");
        }
        
        // Construction de la logique
        StringBuilder logic = new StringBuilder();
        
        // Base temporelle obligatoire
        if (!baseConditions.isEmpty()) {
            logic.append(String.join(" & ", baseConditions));
        }
        
        // Filtres principaux (ET)
        if (!filterConditions.isEmpty()) {
            if (logic.length() > 0) logic.append(" & ");
            logic.append("(").append(String.join(" & ", filterConditions)).append(")");
        }
        
        // Conditions de montant (ET)
        if (!amountConditions.isEmpty()) {
            if (logic.length() > 0) logic.append(" & ");
            logic.append("(").append(String.join(" & ", amountConditions)).append(")");
        }
        
        // Conditions de risque (OU pour l'analyse)
        if (!riskConditions.isEmpty()) {
            if (logic.length() > 0) logic.append(" & ");
            logic.append("(").append(String.join(" | ", riskConditions)).append(")");
        }
        
        return logic.toString();
    }
    
    // Méthodes d'analyse spécialisées avec mapping moderne
    public List<Transaction> findAnomalousTransactions(LocalDate from, LocalDate to) {
        BigDecimal averageAmount = calculateAverageTransactionAmount(from, to);
        BigDecimal threshold = averageAmount.multiply(new BigDecimal("3")); // 3x la moyenne
        
        // Mapping function avec closure
        Function<FilterDefinition<TransactionPropertyRef>, Object> mappingFunction = definition -> {
            return switch (definition.ref()) {
                case TRANSACTION_DATE -> "transactionDate";
                case AMOUNT -> "amount";  
                case STATUS -> "status";
                case ANOMALY_DETECTION -> new PredicateResolverMapping<Transaction, TransactionPropertyRef>() {
                    @Override
                    public PredicateResolver<Transaction> resolve() {
                        // definition accessible via closure
                        BigDecimal thresholdValue = (BigDecimal) definition.getValue();
                        return (root, query, cb) -> {
                            // Logique complexe d'anomalie
                            return cb.and(
                                cb.gt(root.get("amount"), thresholdValue),
                                cb.equal(root.get("status"), TransactionStatus.COMPLETED),
                                // Conditions supplémentaires...
                            );
                        };
                    }
                };
            };
        };
        
        FilterRequest<TransactionPropertyRef> request = FilterRequest.<TransactionPropertyRef>builder()
            .filter("dateRange", new FilterDefinition<>(
                TransactionPropertyRef.TRANSACTION_DATE, 
                Op.RANGE, 
                Arrays.asList(from, to)
            ))
            .filter("anomalyCheck", new FilterDefinition<>(
                TransactionPropertyRef.ANOMALY_DETECTION, 
                Op.CUSTOM, 
                threshold
            ))
            .filter("completed", new FilterDefinition<>(
                TransactionPropertyRef.STATUS, 
                Op.EQ, 
                TransactionStatus.COMPLETED
            ))
            .combineWith("dateRange & anomalyCheck & completed")
            .build();
        
        return transactionRepository.findAll(filterResolver.resolve(request));
    }
    
    public Map<String, BigDecimal> getCategoryBreakdown(LocalDate from, LocalDate to) {
        FilterRequest<TransactionPropertyRef> request = FilterRequest.<TransactionPropertyRef>builder()
            .filter("dateRange", new FilterDefinition<>(
                TransactionPropertyRef.TRANSACTION_DATE, 
                Op.RANGE, 
                Arrays.asList(from, to)
            ))
            .filter("completed", new FilterDefinition<>(
                TransactionPropertyRef.STATUS, 
                Op.EQ, 
                TransactionStatus.COMPLETED
            ))
            .combineWith("dateRange & completed")
            .build();
        
        List<Transaction> transactions = transactionRepository.findAll(filterResolver.resolve(request));
        
        return transactions.stream()
            .collect(Collectors.groupingBy(
                Transaction::getCategory,
                Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
            ));
    }
}
```

## 🚀 Tests d'Intégration

{% include code_block.html title="Classe de test complète" language="java" file="src/test/java/service/ProductSearchServiceIntegrationTest.java" %}
```java
@SpringBootTest
@Transactional
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ProductSearchServiceIntegrationTest {
    
    @Autowired
    private ProductSearchService searchService;
    
    @Autowired
    private ProductRepository productRepository;
    
    @BeforeEach
    void setUp() {
        // Créer des données de test
        createTestProducts();
    }
    
    @Test
    void searchProducts_WithMultipleFilters_ShouldReturnFilteredResults() {
        // Given
        ProductSearchRequest searchRequest = ProductSearchRequest.builder()
            .query("Phone")
            .categories(Arrays.asList("Electronics"))
            .minPrice(new BigDecimal("500"))
            .maxPrice(new BigDecimal("1500"))
            .inStockOnly(true)
            .build();
        
        Pageable pageable = PageRequest.of(0, 10);
        
        // When
        Page<Product> results = searchService.searchProducts(searchRequest, pageable);
        
        // Then
        assertThat(results).isNotEmpty();
        assertThat(results.getContent()).allSatisfy(product -> {
            assertThat(product.getName()).containsIgnoringCase("Phone");
            assertThat(product.getCategory()).isEqualTo("Electronics");
            assertThat(product.getPrice()).isBetween(new BigDecimal("500"), new BigDecimal("1500"));
            assertThat(product.getStockQuantity()).isGreaterThan(0);
        });
    }
    
    @Test
    void searchProducts_WithComplexLogicalCombination_ShouldWork() {
        // Given
        ProductSearchRequest searchRequest = ProductSearchRequest.builder()
            .categories(Arrays.asList("Electronics", "Clothing"))
            .promotedOnly(true)
            .minRating(4.0)
            .build();
        
        // When
        Page<Product> results = searchService.searchProducts(searchRequest, PageRequest.of(0, 20));
        
        // Then
        assertThat(results.getContent()).allSatisfy(product -> {
            assertThat(Arrays.asList("Electronics", "Clothing")).contains(product.getCategory());
            assertThat(product.getIsPromoted()).isTrue();
            assertThat(product.getRating()).isGreaterThanOrEqualTo(4.0);
        });
    }
    
    private void createTestProducts() {
        List<Product> products = Arrays.asList(
            createProduct("iPhone 14", "Electronics", new BigDecimal("999"), 10, true, "Apple", 4.5),
            createProduct("Samsung Galaxy", "Electronics", new BigDecimal("799"), 5, false, "Samsung", 4.3),
            createProduct("Nike Shoes", "Clothing", new BigDecimal("129"), 20, true, "Nike", 4.7),
            createProduct("Laptop", "Electronics", new BigDecimal("1299"), 0, false, "Dell", 4.1),
            createProduct("T-Shirt", "Clothing", new BigDecimal("29"), 50, true, "Adidas", 4.0)
        );
        
        productRepository.saveAll(products);
    }
    
    private Product createProduct(String name, String category, BigDecimal price, 
                                 Integer stock, Boolean promoted, String brand, Double rating) {
        Product product = new Product();
        product.setName(name);
        product.setCategory(category);
        product.setPrice(price);
        product.setStockQuantity(stock);
        product.setIsPromoted(promoted);
        product.setBrand(brand);
        product.setRating(rating);
        product.setCreatedDate(LocalDateTime.now());
        return product;
    }
}
```

---

{% include alert.html type="success" title="Prêt à commencer ?" content="Ces exemples montrent la puissance et la flexibilité de FilterQL. Consultez notre [documentation](/docs/) pour plus de détails sur l'implémentation." %}