---
title: Guide de Contribution
description: Guide pour contribuer au développement de FilterQL
sidebar_position: 4
---

# Guide de Contribution FilterQL

> **Contribuez au développement de FilterQL et aidez à créer la meilleure bibliothèque de filtrage dynamique Java**

---

## Bienvenue Contributors !

Nous accueillons chaleureusement les contributions à FilterQL ! Que vous souhaitiez corriger un bug, ajouter une fonctionnalité, améliorer la documentation ou proposer de nouvelles idées, votre aide est précieuse.

### Types de Contributions

- 🐛 **Corrections de bugs** : Identification et résolution de problèmes
- ✨ **Nouvelles fonctionnalités** : Ajout de capacités et d'améliorations
- 📚 **Documentation** : Amélioration guides, exemples, et API docs
- 🧪 **Tests** : Ajout de couverture de test et cas limites
- 🚀 **Performance** : Optimisations et améliorations de vitesse
- 🔧 **Outillage** : Amélioration du processus de build et développement

---

## Code de Conduite

### Notre Engagement

En tant que contributeurs et mainteneurs de ce projet, nous nous engageons à créer un environnement ouvert et accueillant pour tous, indépendamment de l'âge, de la taille, du handicap, de l'origine ethnique, de l'identité de genre, du niveau d'expérience, de la nationalité, de l'apparence, de la race, de la religion ou de l'identité et orientation sexuelle.

### Standards Attendus

**Comportements encouragés** :
- Utiliser un langage accueillant et inclusif
- Respecter les points de vue et expériences différents
- Accepter gracieusement les critiques constructives
- Se concentrer sur ce qui est le mieux pour la communauté
- Faire preuve d'empathie envers les autres membres

**Comportements inacceptables** :
- Commentaires désobligeants, insultes ou attaques personnelles
- Harcèlement public ou privé
- Publication d'informations privées sans permission
- Conduite professionnellement inappropriée

### Application

Les violations peuvent être signalées en contactant l'équipe du projet à [frank.kossi@kunrin.com](mailto:frank.kossi@kunrin.com). Toutes les plaintes seront examinées et investiguées équitablement.

---

## Configuration de Développement

### Prérequis

- **Java 21+** (version minimale requise)
- **Maven 3.9.x+** ou utiliser les wrappers fournis
- **Git** pour contrôle de version
- **IDE** recommandé : IntelliJ IDEA, Eclipse, VS Code

### Clone et Setup

**1. Fork et Clone**
```bash
# Fork le repository sur GitHub puis clone votre fork
git clone https://github.com/YOUR-USERNAME/filter-build.git
cd filter-build
```

**2. Configuration Branches**
```bash
# Ajouter remote upstream
git remote add upstream https://github.com/cyfko/filter-build.git

# Vérifier les remotes
git remote -v
```

**3. Build Initial**
```bash
# Module Core
cd core/java
./mvnw clean compile test

# Module Spring Adapter  
cd ../../adapters/java/spring
./mvnw clean compile test
```

### Structure du Projet

```
filter-build/
├── core/java/                  # Module core FilterQL
│   ├── src/main/java/         # Code source principal
│   ├── src/test/java/         # Tests unitaires
│   └── pom.xml                # Configuration Maven
├── adapters/java/spring/      # Adaptateur Spring Data JPA
│   ├── src/main/java/         # Code adaptateur
│   ├── src/test/java/         # Tests intégration Spring
│   └── pom.xml                # Configuration Maven Spring
├── docs/                      # Documentation (cette structure)
├── LICENSE                    # Licence MIT
└── README.md                 # Documentation principale
```

### Configuration IDE

**IntelliJ IDEA** :
```xml
<!-- .idea/compiler.xml -->
<component name="CompilerConfiguration">
  <annotationProcessing>
    <profile default="true" name="Default" enabled="true" />
  </annotationProcessing>
  <bytecodeTargetLevel target="21" />
</component>
```

**VS Code** :
```json
// .vscode/settings.json
{
  "java.compile.nullAnalysis.mode": "automatic",
  "java.configuration.updateBuildConfiguration": "automatic",
  "java.runtime.version": "21"
}
```

---

## Processus de Contribution

### Workflow Git

**1. Créer une Branche de Fonctionnalité**
```bash
# Partir de main à jour
git checkout main
git pull upstream main

# Créer branche pour votre travail
git checkout -b feature/amazing-new-feature
# ou
git checkout -b bugfix/fix-issue-123
```

**2. Développement**
```bash
# Faire vos modifications
# Ajouter tests appropriés
# Mettre à jour documentation si nécessaire

# Commit réguliers avec messages clairs
git add .
git commit -m "feat: add amazing new validation feature

- Add custom PropertyReference validation
- Update tests for new validation logic  
- Add documentation examples"
```

**3. Mise à Jour et Push**
```bash
# Rebaser sur main pour historique propre
git fetch upstream
git rebase upstream/main

# Push vers votre fork
git push origin feature/amazing-new-feature
```

**4. Pull Request**
- Ouvrir une PR depuis votre fork vers `cyfko/filter-build:main`
- Utiliser le template de PR fourni
- Lier les issues relevantes avec "Fixes #123"

### Standards de Code

#### Convention de Nommage

**Classes** :
```java
// PascalCase pour classes et interfaces
public class FilterResolver { }
public interface PropertyReference { }
```

**Méthodes et Variables** :
```java
// camelCase pour méthodes et variables
public PredicateResolver<T> toResolver(Class<T> entityClass) { }
private final Map<String, FilterCondition<?>> conditions;
```

**Constantes** :
```java
// SCREAMING_SNAKE_CASE pour constantes
public static final String DEFAULT_FILTER_KEY = "default";
```

**Packages** :
```java
// Tout en minuscule, structure cohérente
package io.github.cyfko.filterql.core.model;
package io.github.cyfko.filterql.adapter.spring;
```

#### Style de Code

**Formatage** :
```java
// Indentation 4 espaces, ligne max 120 caractères
public class ExampleClass {
    
    private final String property;
    
    public ExampleClass(String property) {
        this.property = Objects.requireNonNull(property, "Property cannot be null");
    }
    
    public Optional<String> getProperty() {
        return Optional.ofNullable(property);
    }
}
```

**Documentation** :
```java
/**
 * Resolves filter definitions into executable predicates.
 * <p>
 * This class provides the main entry point for the FilterQL filtering system,
 * orchestrating DSL parsing, validation, and predicate resolution.
 * </p>
 * 
 * <p><strong>Usage Example:</strong></p>
 * <pre>{@code
 * FilterResolver resolver = FilterResolver.of(context);
 * PredicateResolver<User> predicate = resolver.resolve(User.class, request);
 * }</pre>
 * 
 * @param <T> the entity type being filtered
 * @author Your Name
 * @since 3.1.0
 */
public class FilterResolver<T> {
    // Implementation...
}
```

#### Java Features Modernes

**Records** (Java 14+) :
```java
// Utiliser records pour data classes immutables
public record FilterDefinition<P extends Enum<P> & PropertyReference>(
    P ref, 
    Op operator, 
    Object value
) {
    // Validation dans compact constructor si nécessaire
    public FilterDefinition {
        Objects.requireNonNull(ref, "Property reference cannot be null");
        Objects.requireNonNull(operator, "Operator cannot be null");
    }
}
```

**Switch Expressions** (Java 14+) :
```java
// Utiliser switch expressions avec yield
public String mapPropertyToPath(UserPropertyRef property) {
    return switch (property) {
        case NAME -> "name";
        case EMAIL -> "email";  
        case AGE -> "age";
        case ADDRESS_CITY -> "address.city.name";
    };
}
```

**Text Blocks** (Java 15+) :
```java
// Utiliser text blocks pour strings multi-lignes
private static final String COMPLEX_QUERY = """
    SELECT u FROM User u 
    LEFT JOIN u.address a 
    LEFT JOIN a.city c 
    WHERE %s
    """;
```

---

## Standards de Tests

### Structure des Tests

**Tests Unitaires** :
```java
@ExtendWith(MockitoExtension.class)
class FilterResolverTest {
    
    @Mock
    private Context context;
    
    @Mock
    private Parser parser;
    
    @InjectMocks
    private FilterResolver resolver;
    
    @Test
    void shouldResolveValidFilterRequest() {
        // Given
        FilterRequest<UserPropertyRef> request = createValidRequest();
        when(parser.parse(anyString())).thenReturn(mockFilterTree);
        
        // When
        PredicateResolver<User> result = resolver.resolve(User.class, request);
        
        // Then
        assertThat(result).isNotNull();
        verify(context).addCondition(anyString(), any(FilterDefinition.class));
    }
    
    @Test
    void shouldThrowExceptionForInvalidDSL() {
        // Given / When / Then
        assertThatThrownBy(() -> parser.parse("invalid & & syntax"))
            .isInstanceOf(DSLSyntaxException.class)
            .hasMessageContaining("Invalid DSL syntax");
    }
}
```

**Tests d'Intégration Spring** :
```java
@DataJpaTest
@Import(FilterConfig.class)
class SpringFilterIntegrationTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private FilterContext<User, UserPropertyRef> filterContext;
    
    @Test
    @Transactional
    void shouldFilterUsersWithComplexConditions() {
        // Given
        createTestData();
        FilterRequest<UserPropertyRef> request = createComplexFilterRequest();
        
        // When
        List<User> results = executeFilter(request);
        
        // Then
        assertThat(results).hasSize(2);
        assertThat(results).allMatch(this::meetsFilterCriteria);
    }
    
    private void createTestData() {
        // Create test users with known data
    }
}
```

### Couverture de Tests

**Objectifs** :
- **Couverture ligne** : Minimum 85%
- **Couverture branche** : Minimum 80%
- **Tests critiques** : 100% pour validation et sécurité

**Commandes de Vérification** :
```bash
# Core module
cd core/java
./mvnw clean test jacoco:report

# Spring adapter
cd adapters/java/spring  
./mvnw clean test jacoco:report

# Consulter rapports dans target/site/jacoco/
```

### Types de Tests Requis

**1. Tests Unitaires** : Chaque classe publique
**2. Tests d'Intégration** : Workflow complets
**3. Tests de Performance** : Opérations critiques
**4. Tests de Sécurité** : Validation et injection
**5. Tests de Compatibility** : Versions Java et Spring

---

## Ajout de Nouvelles Fonctionnalités

### Processus de Design

**1. Issue de Discussion**
- Créer une issue pour discuter de la fonctionnalité
- Expliquer le problème résolu et cas d'usage
- Proposer approche technique de haut niveau

**2. Design Document**
- Créer un design document pour fonctionnalités majeures
- Inclure API design, impact sur architecture, tests nécessaires
- Obtenir review avant implémentation

**3. Implémentation Incrémentale**
- Diviser en petites PRs reviewables
- Commencer par tests (TDD when possible)
- Maintenir backward compatibility

### Exemple : Ajouter un Nouvel Opérateur

**1. Définir l'Opérateur** :
```java
// Dans Op.java
public enum Op {
    // Existing operators...
    
    /** Full-text search operator: "@@" */
    FULL_TEXT_SEARCH("@@", "FULL_TEXT_SEARCH");
    
    // Implementation...
}
```

**2. Ajouter Support PropertyReference** :
```java
// Dans PropertyReference.java
default boolean supportsFullTextSearch() {
    return getSupportedOperators().contains(Op.FULL_TEXT_SEARCH);
}
```

**3. Implémenter dans Adaptateurs** :
```java
// Dans FilterContext.java (Spring adapter)
case FULL_TEXT_SEARCH -> {
    String searchTerm = (String) value;
    yield cb.function("to_tsvector", String.class, path)
              .get("@@")
              .function("plainto_tsquery", String.class, cb.literal(searchTerm));
}
```

**4. Ajouter Tests Complets** :
```java
@Test
void shouldSupportFullTextSearch() {
    // Test nouvel opérateur avec différents types de propriétés
}
```

### Guidelines API Design

**Backward Compatibility** :
- Ne jamais casser les APIs existantes
- Déprécier avant suppression (minimum 2 versions)
- Fournir chemins de migration clairs

**Extensibilité** :
- Utiliser interfaces pour nouvelles abstractions
- Prévoir extension points pour utilisateurs
- Documenter contracts et limitations

**Performance** :
- Considérer impact performance de nouvelles features
- Ajouter benchmarks pour changements significatifs
- Préserver lazy evaluation et optimisations existantes

---

## Documentation

### Types de Documentation

**1. Javadoc** : APIs publiques complètes
**2. README** : Overview et quick start
**3. Guides** : Tutorials détaillés et cas d'usage
**4. Architecture** : Design decisions et rationale
**5. Examples** : Code samples working

### Standards Javadoc

```java
/**
 * Resolves filter definitions into executable predicates.
 * <p>
 * This resolver provides the main entry point for the FilterQL system,
 * handling the complete pipeline from DSL parsing to predicate generation.
 * The implementation is thread-safe and can be reused across multiple requests.
 * </p>
 * 
 * <p><strong>Supported Features:</strong></p>
 * <ul>
 *   <li>Type-safe property validation</li>
 *   <li>Complex boolean logic with precedence</li>
 *   <li>Framework-agnostic predicate generation</li>
 *   <li>Extensible operator system</li>
 * </ul>
 * 
 * <p><strong>Usage Example:</strong></p>
 * <pre>{@code
 * // Create context with property mappings
 * FilterContext<User, UserPropertyRef> context = new FilterContext<>(
 *     User.class, UserPropertyRef.class, this::mapProperty
 * );
 * 
 * // Create resolver and process request
 * FilterResolver resolver = FilterResolver.of(context);
 * PredicateResolver<User> predicate = resolver.resolve(User.class, request);
 * 
 * // Execute with framework
 * Specification<User> spec = predicate.toSpecification();
 * List<User> results = userRepository.findAll(spec);
 * }</pre>
 * 
 * <p><strong>Thread Safety:</strong></p>
 * <p>This class is thread-safe. All methods can be called concurrently
 * from multiple threads without external synchronization.</p>
 * 
 * <p><strong>Performance Considerations:</strong></p>
 * <ul>
 *   <li>DSL parsing is optimized with caching</li>
 *   <li>Property validation uses compile-time checks</li>
 *   <li>Predicate generation is lazy and efficient</li>
 * </ul>
 *
 * @param <T> the entity type being filtered (e.g., User, Product, Order)
 * @see FilterContext
 * @see PredicateResolver
 * @see FilterRequest
 * @throws DSLSyntaxException if the DSL expression has invalid syntax
 * @throws FilterValidationException if filter validation fails
 * @author Frank KOSSI
 * @since 3.0.0
 */
public class FilterResolver<T> {
    // Implementation...
}
```

### Exemples de Code

**Doit Inclure** :
- Setup complet avec imports
- Data setup si nécessaire  
- Code exécutable et testé
- Commentaires expliquant les étapes clés
- Gestion d'erreurs appropriée

**Exemple Complet** :
```java
// ✅ BON EXEMPLE
/**
 * Example: Advanced filtering with custom business logic
 */
public class AdvancedFilteringExample {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private FilterContext<User, UserPropertyRef> filterContext;
    
    /**
     * Find VIP customers with complex business rules.
     * VIP = customers with either high order value OR premium status.
     */
    public List<User> findVipCustomers(String namePattern, BigDecimal minOrderValue) {
        try {
            // 1. Create filter definitions
            FilterDefinition<UserPropertyRef> nameFilter = new FilterDefinition<>(
                UserPropertyRef.NAME, Op.MATCHES, namePattern
            );
            FilterDefinition<UserPropertyRef> vipFilter = new FilterDefinition<>(
                UserPropertyRef.IS_VIP, Op.EQ, true
            );
            
            // 2. Add conditions to context
            Condition nameCondition = filterContext.addCondition("name", nameFilter);
            Condition vipCondition = filterContext.addCondition("vip", vipFilter);
            
            // 3. Combine with business logic
            Condition combined = nameCondition.and(vipCondition);
            
            // 4. Execute query
            PredicateResolver<User> resolver = filterContext.toResolver(User.class, combined);
            Specification<User> spec = (root, query, cb) -> resolver.resolve(root, query, cb);
            
            return userRepository.findAll(spec);
            
        } catch (FilterValidationException e) {
            log.warn("Filter validation failed: {}", e.getMessage());
            throw new BusinessException("Invalid filter criteria", e);
        }
    }
}
```

---

## Release Process

### Versioning

FilterQL suit [Semantic Versioning](https://semver.org/) :

- **MAJOR** (3.x.x) : Breaking changes
- **MINOR** (x.1.x) : Nouvelles fonctionnalités backward compatible
- **PATCH** (x.x.1) : Bug fixes backward compatible

### Release Checklist

**Pre-Release** :
- [ ] Tous tests passent (core + adapters)
- [ ] Documentation mise à jour
- [ ] CHANGELOG.md mis à jour
- [ ] Version bumped dans tous pom.xml
- [ ] Performance regressions vérifiées

**Release** :
- [ ] Tag Git créé (`v3.1.0`)
- [ ] Build et deploy vers Maven Central
- [ ] GitHub Release avec notes
- [ ] Documentation site mise à jour

**Post-Release** :
- [ ] Announce sur GitHub Discussions
- [ ] Update exemple repositories
- [ ] Monitor issues post-release

---

## Support et Questions

### Canaux de Communication

**GitHub Issues** : Bugs, feature requests, questions techniques
- Template bug report pour reproductibilité
- Template feature request pour spécifications
- Labels pour catégorisation et priorité

**GitHub Discussions** : Questions générales, showcase, aide
- Q&A pour questions d'usage
- Show and tell pour partager projets
- Ideas pour propositions de fonctionnalités

**Email** : [frank.kossi@kunrin.com](mailto:frank.kossi@kunrin.com)
- Questions sensibles ou privées
- Sujets de sécurité
- Partenariats et collaborations

### Reconnaissance

Tous les contributeurs sont reconnus dans :
- **README.md** : Section contributors
- **CHANGELOG.md** : Crédits par version
- **GitHub** : Contributor graphs automatiques

### Types de Reconnaissance

- **Code Contributors** : Code, tests, bug fixes
- **Documentation Contributors** : Guides, examples, translations
- **Community Contributors** : Support utilisateurs, promotion
- **Sponsor Contributors** : Support financier développement

---

## Conclusion

Merci de votre intérêt pour contribuer à FilterQL ! Votre aide est essentielle pour faire de FilterQL la meilleure solution de filtrage dynamique pour Java.

**Prochaines Étapes** :
1. Configurez votre environnement de développement
2. Explorez les issues "good first issue" 
3. Rejoignez les discussions communautaires
4. Lisez la documentation technique détaillée

**Ressources Importantes** :
- **[Architecture](./architecture.md)** : Compréhension approfondie du design
- **[Documentation Core](./implementations/core/)** : APIs et concepts principaux
- **[Documentation Spring](./implementations/spring-adapter/)** : Intégration Spring Data JPA
- **[Examples](./examples/)** : Cas d'usage et patterns avancés

---

*Ensemble, créons l'avenir du filtrage dynamique Java !* 🚀