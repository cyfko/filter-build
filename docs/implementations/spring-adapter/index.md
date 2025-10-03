---
title: Adaptateur Spring Data JPA
description: Intégration native Spring Data JPA avec Specifications et PredicateResolver
sidebar_position: 2
---

# Adaptateur Spring Data JPA

> **Intégration native Spring Data JPA avec support complet des Specifications et conversion automatique vers PredicateResolver**

---

## Résumé

L'**adaptateur Spring** de FilterQL fournit une intégration native avec Spring Data JPA, transformant automatiquement les conditions FilterQL en `Specification<T>` Spring. Il permet l'utilisation complète de l'écosystème Spring Data (pagination, sorting, caching) tout en maintenant la sécurité type-safe de FilterQL.

**Extrait du fichier adapters/java/spring/pom.xml :**
```xml
<groupId>io.github.cyfko</groupId>
<artifactId>filterql-spring</artifactId>
<version>3.0.0</version>
<description>Spring Data JPA Specification adapter for dynamic filtering</description>
```

---

## Problème / Contexte

### Défis de l'Intégration Spring Data

L'intégration de filtrage dynamique avec Spring Data JPA présente des défis spécifiques :

- **Conversion de types** : Transformer les conditions abstraites en `Specification<T>` Spring
- **Navigation de propriétés** : Support des chemins imbriqués (`user.address.city`)
- **Préservation des fonctionnalités** : Maintien de pagination, tri, cache Spring
- **Type Safety** : Validation des types lors de la conversion JPA

### Solution Adaptateur Spring

L'adaptateur résout ces problèmes via :
- **FilterContext** : Contextualisation pour entités Spring JPA
- **FilterCondition** : Wrapper `Specification<T>` avec opérations logiques
- **PathResolverUtils** : Résolution automatique des chemins imbriqués
- **PredicateResolverMapping** : Support de logique business personnalisée

---

## État de l'Art

### Solutions Spring Data Existantes

**Spring Data Specifications natives** :
- ✅ Intégration complète écosystème Spring
- ❌ Code verbeux et répétitif
- ❌ Pas de DSL expressif
- ❌ Validation manuelle requise

**Query by Example (QBE)** :
- ✅ API simple pour cas basiques
- ❌ Limité aux correspondances exactes
- ❌ Pas de logique booléenne complexe

**@Query avec SpEL** :
- ✅ Flexibilité maximale
- ❌ Requêtes statiques uniquement
- ❌ Risques d'injection si mal utilisé

### Avantages FilterQL Spring

| Caractéristique | Specifications | QBE | @Query/SpEL | FilterQL Spring |
|-----------------|----------------|-----|-------------|-----------------|
| **DSL Expressif** | ❌ | ❌ | ❌ | ✅ |
| **Type Safety** | ⚠️ | ✅ | ❌ | ✅ |
| **Logique Complexe** | ✅ | ❌ | ✅ | ✅ |
| **Dynamique Runtime** | ✅ | ⚠️ | ❌ | ✅ |
| **Réutilisabilité** | ❌ | ⚠️ | ❌ | ✅ |

---

## Approche / Architecture

### Composants Principaux

```
┌─────────────────────────────────────────┐
│            FilterContext<E,P>           │
│   (Configuration + Mapping Strategy)   │
├─────────────────────────────────────────┤
│           FilterCondition<T>            │
│      (Specification Wrapper +          │
│       Logical Operations)               │
├─────────────────────────────────────────┤
│         PathResolverUtils               │
│     (Nested Path Navigation)            │
├─────────────────────────────────────────┤
│      PredicateResolverMapping           │
│     (Custom Business Logic)             │
└─────────────────────────────────────────┘
```

### Flux d'Intégration

1. **Configuration** : `FilterContext` avec mapping entity ↔ PropertyReference
2. **Conversion** : `FilterDefinition` → `Specification<T>` ou `PredicateResolverMapping`
3. **Combinaison** : Opérations logiques via `FilterCondition`
4. **Exécution** : Integration native `JpaSpecificationExecutor`

---

## Installation & Prérequis

### Dépendances Maven

**Extrait du fichier adapters/java/spring/pom.xml :**

```xml
<dependencies>
    <!-- Dépendance vers le module core -->
    <dependency>
        <groupId>io.github.cyfko</groupId>
        <artifactId>filterql-core</artifactId>
        <version>3.0.0</version>
    </dependency>

    <!-- Spring Data JPA (fourni aux utilisateurs via leur version) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

### Configuration Projet

```xml
<!-- Core module (requis) -->
<dependency>
    <groupId>io.github.cyfko</groupId>
    <artifactId>filterql-core</artifactId>
    <version>3.0.0</version>
</dependency>

<!-- Spring Data JPA adapter -->
<dependency>
    <groupId>io.github.cyfko</groupId>
    <artifactId>filterql-spring</artifactId>
    <version>3.0.0</version>
</dependency>
```

### Prérequis

- **Java 21+**
- **Spring Boot 3.3.4+** (défini dans dependencyManagement)
- **Spring Data JPA**
- **Jakarta Persistence API 3.1+**

---

## Quickstart (Débutant)

### Étape 1 : Définir l'Entité JPA

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "email", unique = true)
    private String email;
    
    @Column(name = "age")
    private Integer age;
    
    @Column(name = "active")
    private Boolean active;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    // Getters/Setters...
}
```

### Étape 2 : Créer PropertyReference

```java
public enum UserPropertyRef implements PropertyReference {
    NAME(String.class, Set.of(Op.EQ, Op.MATCHES, Op.IN)),
    EMAIL(String.class, Set.of(Op.EQ, Op.MATCHES)),
    AGE(Integer.class, Set.of(Op.EQ, Op.GT, Op.GTE, Op.LT, Op.LTE, Op.RANGE)),
    ACTIVE(Boolean.class, Set.of(Op.EQ, Op.NE)),
    CREATED_AT(LocalDateTime.class, Set.of(Op.EQ, Op.GT, Op.GTE, Op.LT, Op.LTE, Op.RANGE));

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

### Étape 3 : Configurer FilterContext

**Extrait de adapters/java/spring/src/main/java/.../FilterContext.java :**

```java
@Configuration
public class FilterConfig {
    
    @Bean
    public FilterContext<User, UserPropertyRef> userFilterContext() {
        return new FilterContext<>(
            User.class, 
            UserPropertyRef.class,
            def -> switch (def.ref()) {
                case NAME -> "name";           // Mapping vers champ JPA
                case EMAIL -> "email";         // Mapping vers champ JPA
                case AGE -> "age";             // Mapping vers champ JPA
                case ACTIVE -> "active";       // Mapping vers champ JPA
                case CREATED_AT -> "createdAt"; // Mapping vers champ JPA
            }
        );
    }
}
```

### Étape 4 : Créer Repository

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    // JpaSpecificationExecutor fournit findAll(Specification<T> spec)
}
```

### Étape 5 : Utiliser dans un Service

**Extrait basé sur les tests Spring :**

```java
@Service
@Transactional(readOnly = true)
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private FilterContext<User, UserPropertyRef> filterContext;
    
    public List<User> searchUsers(String name, Integer minAge) {
        // 1. Créer les filter definitions
        FilterDefinition<UserPropertyRef> nameFilter = new FilterDefinition<>(
            UserPropertyRef.NAME, Op.MATCHES, name + "%"
        );
        FilterDefinition<UserPropertyRef> ageFilter = new FilterDefinition<>(
            UserPropertyRef.AGE, Op.GTE, minAge
        );
        
        // 2. Ajouter au context
        Condition nameCondition = filterContext.addCondition("nameFilter", nameFilter);
        Condition ageCondition = filterContext.addCondition("ageFilter", ageFilter);
        
        // 3. Combiner les conditions
        Condition combined = nameCondition.and(ageCondition);
        
        // 4. Convertir en PredicateResolver
        PredicateResolver<User> resolver = filterContext.toResolver(User.class, combined);
        
        // 5. Créer Specification et exécuter
        Specification<User> spec = (root, query, cb) -> resolver.resolve(root, query, cb);
        return userRepository.findAll(spec);
    }
}
```

---

## Cas d'Usage

### Débutant : Filtrage Simple avec Repository

**Recherche d'utilisateurs actifs par nom**

**Extrait de adapters/java/spring/src/test/java/.../SpringIntegrationTest.java :**

```java
@DataJpaTest
class SimpleFilterExample {
    
    @Autowired
    private UserRepository userRepository;
    
    private FilterContext<User, UserPropertyRef> filterContext;
    
    @BeforeEach
    void setUp() {
        filterContext = new FilterContext<>(User.class, UserPropertyRef.class, 
            def -> switch (def.ref()) {
                case NAME -> "name";
                case ACTIVE -> "active";
            });
    }
    
    @Test
    void testActiveUsersByName() {
        // 1. Créer filtres
        FilterDefinition<UserPropertyRef> nameFilter = new FilterDefinition<>(
            UserPropertyRef.NAME, Op.MATCHES, "John%"
        );
        FilterDefinition<UserPropertyRef> activeFilter = new FilterDefinition<>(
            UserPropertyRef.ACTIVE, Op.EQ, true
        );
        
        // 2. Ajouter conditions
        Condition nameCondition = filterContext.addCondition("name", nameFilter);
        Condition activeCondition = filterContext.addCondition("active", activeFilter);
        
        // 3. Combiner
        Condition combined = nameCondition.and(activeCondition);
        
        // 4. Convertir et exécuter
        PredicateResolver<User> resolver = filterContext.toResolver(User.class, combined);
        Specification<User> spec = (root, query, cb) -> resolver.resolve(root, query, cb);
        
        List<User> results = userRepository.findAll(spec);
        
        // Vérifie que tous les résultats correspondent aux critères
        assertThat(results).allMatch(user -> 
            user.getName().startsWith("John") && user.getActive()
        );
    }
}
```

### Intermédiaire : Propriétés Imbriquées

**Navigation dans les relations JPA**

**Extrait de adapters/java/spring/src/main/java/.../utils/PathResolverUtils.java :**

```java
// Entité avec relations
@Entity
public class User {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id")
    private Address address;
    // ...
}

@Entity  
public class Address {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id")
    private City city;
    // ...
}

@Entity
public class City {
    @Column(name = "name")
    private String name;
    // ...
}

// PropertyReference avec chemin imbriqué
public enum UserPropertyRef implements PropertyReference {
    NAME(String.class, Set.of(Op.EQ, Op.MATCHES)),
    CITY_NAME(String.class, Set.of(Op.EQ, Op.MATCHES, Op.IN));
    // ...
}

// Configuration mapping avec chemin imbriqué
@Bean
public FilterContext<User, UserPropertyRef> userFilterContext() {
    return new FilterContext<>(User.class, UserPropertyRef.class,
        def -> switch (def.ref()) {
            case NAME -> "name";                    // Propriété simple
            case CITY_NAME -> "address.city.name"; // Chemin imbriqué
        });
}

// Utilisation - PathResolverUtils gère automatiquement les JOINs
FilterDefinition<UserPropertyRef> cityFilter = new FilterDefinition<>(
    UserPropertyRef.CITY_NAME, Op.EQ, "Paris"
);
// Génère automatiquement: 
// LEFT JOIN user.address address
// LEFT JOIN address.city city  
// WHERE city.name = 'Paris'
```

### Avancé : Logique Business Personnalisée

**Implémentation de PredicateResolverMapping**

**Extrait basé sur la structure de PredicateResolverMapping :**

```java
// PropertyReference avec logique complexe
public enum UserPropertyRef implements PropertyReference {
    NAME(String.class, Set.of(Op.EQ, Op.MATCHES)),
    FULL_NAME_SEARCH(String.class, Set.of(Op.MATCHES)),
    IS_VIP_CUSTOMER(Boolean.class, Set.of(Op.EQ));
    // ...
}

// Configuration avec mappings personnalisés
@Bean
public FilterContext<User, UserPropertyRef> advancedUserFilterContext() {
    return new FilterContext<>(User.class, UserPropertyRef.class,
        def -> switch (def.ref()) {
            case NAME -> "name";
            
            // Recherche full-text sur prénom + nom
            case FULL_NAME_SEARCH -> new PredicateResolverMapping<User, UserPropertyRef>() {
                @Override
                public PredicateResolver<User> resolve() {
                    return (root, query, cb) -> {
                        String searchTerm = "%" + def.value() + "%";
                        return cb.or(
                            cb.like(cb.lower(root.get("firstName")), searchTerm.toLowerCase()),
                            cb.like(cb.lower(root.get("lastName")), searchTerm.toLowerCase()),
                            cb.like(cb.lower(
                                cb.concat(
                                    cb.concat(root.get("firstName"), " "), 
                                    root.get("lastName")
                                )
                            ), searchTerm.toLowerCase())
                        );
                    };
                }
            };
            
            // Logique business VIP (commandes > 10K€ OU statut premium)
            case IS_VIP_CUSTOMER -> new PredicateResolverMapping<User, UserPropertyRef>() {
                @Override
                public PredicateResolver<User> resolve() {
                    return (root, query, cb) -> {
                        boolean isVip = (Boolean) def.value();
                        if (!isVip) {
                            return cb.conjunction(); // Pas de filtre si non-VIP
                        }
                        
                        // Sous-requête pour total des commandes
                        Subquery<BigDecimal> totalOrdersSubquery = query.subquery(BigDecimal.class);
                        Root<Order> orderRoot = totalOrdersSubquery.from(Order.class);
                        totalOrdersSubquery.select(cb.sum(orderRoot.get("totalAmount")))
                                         .where(cb.equal(orderRoot.get("customer"), root));
                        
                        return cb.or(
                            // Total commandes > 10K€
                            cb.greaterThan(totalOrdersSubquery, new BigDecimal("10000")),
                            // OU statut premium/gold
                            cb.in(root.get("customerTier")).value(CustomerTier.PREMIUM, CustomerTier.GOLD)
                        );
                    };
                }
            };
        });
}

// Utilisation avec logique complexe
FilterDefinition<UserPropertyRef> vipFilter = new FilterDefinition<>(
    UserPropertyRef.IS_VIP_CUSTOMER, Op.EQ, true
);
FilterDefinition<UserPropertyRef> searchFilter = new FilterDefinition<>(
    UserPropertyRef.FULL_NAME_SEARCH, Op.MATCHES, "Smith"
);

Condition vipCondition = filterContext.addCondition("vip", vipFilter);
Condition searchCondition = filterContext.addCondition("search", searchFilter);
Condition combined = vipCondition.and(searchCondition);
```

---

## Référence API

### Classes Principales

#### FilterContext<E,P>
**Localisation** : `adapters/java/spring/src/main/java/.../FilterContext.java`

| Méthode | Signature | Description |
|---------|-----------|-------------|
| `FilterContext(Class<E>, Class<P>, Function)` | Constructeur avec mapping function | Configure le contexte pour une entité et enum PropertyReference |
| `addCondition(String, FilterDefinition<P>)` | `Condition addCondition(String filterKey, FilterDefinition<P> definition)` | Ajoute une condition avec validation et conversion |
| `getCondition(String)` | `Condition getCondition(String filterKey)` | Récupère une condition par clé |
| `toResolver(Class<U>, Condition)` | `<U> PredicateResolver<U> toResolver(Class<U> entityClass, Condition condition)` | Convertit condition en PredicateResolver |
| `setMappingBuilder(Function)` | `Function<FilterDefinition<P>, Object> setMappingBuilder(Function mappingBuilder)` | Met à jour la fonction de mapping |

#### FilterCondition<T>
**Localisation** : `adapters/java/spring/src/main/java/.../FilterCondition.java`

| Méthode | Signature | Description |
|---------|-----------|-------------|
| `FilterCondition(Specification<T>)` | Constructeur avec Specification | Wrappe une Specification Spring |
| `and(Condition)` | `Condition and(Condition other)` | Combine avec AND logique |
| `or(Condition)` | `Condition or(Condition other)` | Combine avec OR logique |
| `not()` | `Condition not()` | Négation logique |
| `getSpecification()` | `Specification<T> getSpecification()` | Retourne la Specification wrappée |

#### PathResolverUtils
**Localisation** : `adapters/java/spring/src/main/java/.../utils/PathResolverUtils.java`

| Méthode | Signature | Description |
|---------|-----------|-------------|
| `resolvePath(Root<T>, String)` | `static <T> Path<?> resolvePath(Root<T> root, String path)` | Résout chemin imbriqué avec JOINs automatiques |

### Types de Mapping Supportés

#### 1. String (Chemin de Propriété)
```java
Function<FilterDefinition<UserPropertyRef>, Object> simpleMapping = def -> switch (def.ref()) {
    case NAME -> "name";                    // Propriété directe
    case EMAIL -> "email";                  // Propriété directe  
    case CITY_NAME -> "address.city.name"; // Chemin imbriqué avec JOINs auto
};
```

#### 2. PredicateResolverMapping (Logique Personnalisée)
```java
Function<FilterDefinition<UserPropertyRef>, Object> complexMapping = def -> switch (def.ref()) {
    case CUSTOM_LOGIC -> new PredicateResolverMapping<User, UserPropertyRef>() {
        @Override
        public PredicateResolver<User> resolve() {
            return (root, query, cb) -> {
                // Logique JPA Criteria personnalisée
                return cb.someComplexCondition();
            };
        }
    };
};
```

---

## Dépannage & FAQ

### Erreurs Courantes

#### ClassCastException lors de Conversion

**Problème** : Tentative de cast d'une condition non-Spring
```java
// ❌ ERREUR
Condition nonSpringCondition = // ... condition from another adapter
FilterCondition<User> springCondition = (FilterCondition<User>) nonSpringCondition;
```

**Solution** : Vérifier le type avant cast
```java
// ✅ CORRECT
if (condition instanceof FilterCondition<?>) {
    FilterCondition<User> springCondition = (FilterCondition<User>) condition;
    Specification<User> spec = springCondition.getSpecification();
}
```

#### IllegalArgumentException sur Path Invalide

**Problème** : Chemin de propriété inexistant
```java
// ❌ ERREUR - champ "invalidField" n'existe pas
Function<FilterDefinition<UserPropertyRef>, Object> badMapping = def -> switch (def.ref()) {
    case NAME -> "invalidField";
};
```

**Solution** : Valider les chemins de propriétés JPA
```java
// ✅ CORRECT
Function<FilterDefinition<UserPropertyRef>, Object> validMapping = def -> switch (def.ref()) {
    case NAME -> "name";              // Champ existant
    case CITY -> "address.city.name"; // Chemin valide avec relations JPA
};
```

#### LazyInitializationException avec Relations

**Problème** : Accès à propriétés lazy hors transaction
```java
// ❌ ERREUR - accès lazy hors transaction
@Service
public class UserService {
    public List<User> findUsers() {
        // ... création specification avec relations lazy
        return userRepository.findAll(spec); // Échec lazy loading
    }
}
```

**Solution** : Utiliser @Transactional approprié
```java
// ✅ CORRECT
@Service
@Transactional(readOnly = true)
public class UserService {
    
    public List<User> findUsers() {
        // Transaction active pour lazy loading
        return userRepository.findAll(spec);
    }
}
```

### Questions Fréquentes

**Q: Comment gérer des relations @ManyToMany ?**

R: PathResolverUtils gère automatiquement les JOINs de collections :

```java
// Entité avec ManyToMany
@Entity
public class User {
    @ManyToMany
    @JoinTable(name = "user_roles")
    private Set<Role> roles;
}

// Mapping vers collection
case USER_ROLES -> "roles.name"; // JOIN automatique sur table user_roles
```

**Q: Peut-on utiliser des sous-requêtes ?**

R: Oui, via PredicateResolverMapping personnalisé :

```java
case ORDERS_COUNT -> new PredicateResolverMapping<User, UserPropertyRef>() {
    @Override
    public PredicateResolver<User> resolve() {
        return (root, query, cb) -> {
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<Order> orderRoot = subquery.from(Order.class);
            subquery.select(cb.count(orderRoot))
                   .where(cb.equal(orderRoot.get("user"), root));
            return cb.greaterThan(subquery, 5L);
        };
    }
};
```

**Q: Comment utiliser avec pagination Spring ?**

R: L'intégration est transparente :

```java
@RestController
public class UserController {
    
    @GetMapping("/users")
    public Page<User> getUsers(
        @RequestBody FilterRequest<UserPropertyRef> request,
        Pageable pageable) {
        
        PredicateResolver<User> resolver = // ... création via FilterQL
        Specification<User> spec = (root, query, cb) -> resolver.resolve(root, query, cb);
        
        return userRepository.findAll(spec, pageable); // Pagination automatique
    }
}
```

---

## Tests & Validation

### Exécution des Tests

**Commande Maven** :
```bash
cd adapters/java/spring
./mvnw test
```

**Configuration Tests** (extrait de pom.xml) :
```xml
<dependencies>
    <!-- H2 Database pour tests en mémoire -->
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>test</scope>
    </dependency>

    <!-- Spring Boot Test Starter pour tests -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### Couverture de Tests

**Tests principaux** :
- `SpringIntegrationTest.java` : Tests d'intégration complète avec base de données
- `FilterContextTest.java` : Tests du contexte et mappings
- `FilterConditionTest.java` : Tests des conditions et opérations logiques
- `PathResolverUtilsTest.java` : Tests de résolution de chemins imbriqués
- `SpringPerformanceTest.java` : Tests de performance et optimisation
- `SpringEdgeCaseTest.java` : Tests de cas limites et gestion d'erreurs

**Fichiers de test** : `adapters/java/spring/src/test/java/io/github/cyfko/filterql/adapter/spring/`

### Exemples de Tests

**Test d'intégration avec @DataJpaTest :**
```java
@DataJpaTest
class FilterContextIntegrationTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private UserRepository userRepository;
    
    @Test
    void testComplexFilterWithJoins() {
        // Test avec base de données réelle en mémoire
        // Validation de requêtes SQL générées
        // Vérification résultats attendus
    }
}
```

---

## Ressources & Crédits

### Documentation Connexe

- **[Module Core](../core/)** : Documentation du module core FilterQL
- **[Architecture](../../architecture.md)** : Architecture détaillée du système
- **[Installation](../../installation.md)** : Guide d'installation complet

### Fichiers Sources Principaux

- **FilterContext** : `adapters/java/spring/src/main/java/.../FilterContext.java`
- **FilterCondition** : `adapters/java/spring/src/main/java/.../FilterCondition.java`
- **PathResolverUtils** : `adapters/java/spring/src/main/java/.../utils/PathResolverUtils.java`
- **Tests** : `adapters/java/spring/src/test/java/.../`
- **Configuration** : `adapters/java/spring/pom.xml`

### Standards et Références

- **Spring Data JPA** : Framework de persistance et repositories
- **Spring Boot 3.3.4+** : Version minimale supportée
- **Jakarta Persistence API 3.1** : Standard JPA utilisé
- **H2 Database** : Base de données de test en mémoire

---

## Contribuer / Liens Utiles

### Développement

1. **Setup** : Projet Spring Boot avec JPA configuré
2. **Tests** : Base H2 en mémoire pour tests d'intégration
3. **Standards** : Code compatible Spring Boot 3.x + Jakarta

### Patterns Recommandés

**Configuration Bean** :
```java
@Configuration
public class FilterQLConfig {
    
    @Bean
    @ConditionalOnMissingBean
    public FilterContext<User, UserPropertyRef> userFilterContext() {
        return new FilterContext<>(/* ... */);
    }
}
```

**Service Pattern** :
```java
@Service
@Transactional(readOnly = true)
public class FilterableUserService {
    
    @Autowired
    private FilterContext<User, UserPropertyRef> filterContext;
    
    @Autowired  
    private UserRepository userRepository;
    
    public Page<User> search(FilterRequest<UserPropertyRef> request, Pageable pageable) {
        // Implementation avec FilterQL + Spring Data
    }
}
```

### Liens Utiles

- **[Spring Data JPA Reference](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)**
- **[Repository GitHub](https://github.com/cyfko/filter-build)**
- **[Maven Central](https://central.sonatype.com/artifact/io.github.cyfko/filterql-spring)**
- **[Issues](https://github.com/cyfko/filter-build/issues)**

---

*Documentation générée à partir de l'analyse du code source du module adapters/java/spring.*