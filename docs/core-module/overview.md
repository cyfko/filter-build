# FilterQL Core Module

Le module core de FilterQL constitue le cœur de la bibliothèque, fournissant tous les composants essentiels pour créer des systèmes de filtrage dynamiques, type-safe et performants.

## Vue d'ensemble

FilterQL Core est conçu selon une architecture modulaire et extensible qui sépare clairement les responsabilités :

- **Parsing & Validation** : Analyse et validation des requêtes de filtrage
- **Type Safety** : Système de validation des types et opérateurs au moment de la compilation
- **Expression Building** : Construction d'arbres de prédicats logiques
- **Abstraction des Données** : Interface indépendante de la technologie de persistance

## Architecture du Module Core

```
filterql-core/
├── model/           # Modèles de données et DTOs
├── validation/      # Système de validation type-safe
├── impl/           # Implémentations concrètes
├── domain/         # Interfaces de domaine
├── exception/      # Gestion des erreurs
├── mappings/       # Système de mapping des propriétés
├── utils/          # Utilitaires et helpers
└── lang/           # Annotations et outils de langage
```

**Architecture en Couches** :

```
┌─────────────────────────────────────────┐
│              Couche DSL                 │
│  (Parser, FilterTree, Logique Booléenne)│
├─────────────────────────────────────────┤
│           Couche Validation             │
│   (PropertyReference, Operateurs, Op)   │
├─────────────────────────────────────────┤
│            Couche Modèle                │
│  (FilterDefinition, FilterRequest)      │
├─────────────────────────────────────────┤
│          Couche Exécution               │
│  (Context, Condition, PredicateResolver)│
└─────────────────────────────────────────┘
```

## Composants Principaux

### 1. FilterResolver
**Rôle** : Point d'entrée principal pour la résolution des filtres

```java
// Création d'un resolver
FilterResolver resolver = FilterResolver.of(context);

// Résolution d'une requête de filtrage
PredicateResolver<User> predicateResolver = resolver.resolve(User.class, filterRequest);
```

**Responsabilités Clés** :
- Orchestration de la pipeline complète de filtrage
- Combinaison du parsing DSL, gestion du contexte et résolution des prédicats
- API claire et de haut niveau
- Validation automatique des requêtes
- Gestion d'erreurs intégrée

### 2. Parser & FilterTree
**Rôle** : Transformation des expressions DSL en logique booléenne exécutable

```java
Parser parser = new DSLParser();
FilterTree tree = parser.parse("(filter1 & filter2) | !filter3");
```

**Syntaxe DSL Supportée** :
- `&` - Opérateur AND (précédence: 2)
- `|` - Opérateur OR (précédence: 1)
- `!` - Opérateur NOT (précédence: 3)
- `( )` - Parenthèses de groupement

**Exemples** :
```java
// AND simple
"nameFilter & ageFilter"

// Complexe avec précédence
"(active & premium) | vip"

// Négation
"!deleted & (active | pending)"

// Expressions imbriquées
"((age > 18 & age < 65) | premium) & !suspended"
```

### 3. PropertyReference
**Rôle** : Interface enum type-safe pour définir les propriétés filtrables

```java
public enum UserPropertyRef implements PropertyReference {
    NAME(String.class, OperatorUtils.FOR_TEXT),
    AGE(Integer.class, OperatorUtils.FOR_NUMBER),
    EMAIL(String.class, Set.of(Op.EQ, Op.MATCHES)),
    STATUS(UserStatus.class, Set.of(Op.EQ, Op.NE, Op.IN));

    private final Class<?> type;
    private final Set<Op> supportedOperators;

    @Override
    public Class<?> getType() { return type; }
    
    @Override
    public Set<Op> getSupportedOperators() { return supportedOperators; }
}
```

**Avantages** :
- Sécurité au moment de la compilation
- Validation automatique
- Contrats d'API clairs
- Auto-complétion IDE
- Documentation intégrée

### 4. Opérateurs (Op)
**Rôle** : Ensemble complet d'opérateurs de filtrage avec validation intégrée

| Opérateur | Symbole | Description | Type de Valeur | Exemples |
|-----------|---------|-------------|----------------|----------|
| `EQ` | `=` | Égalité | Valeur simple | `name = "John"` |
| `NE` | `!=` | Différent | Valeur simple | `status != "INACTIVE"` |
| `GT` | `>` | Supérieur | Valeur simple | `age > 18` |
| `GTE` | `>=` | Supérieur ou égal | Valeur simple | `price >= 100` |
| `LT` | `<` | Inférieur | Valeur simple | `stock < 10` |
| `LTE` | `<=` | Inférieur ou égal | Valeur simple | `discount <= 50` |
| `MATCHES` | `LIKE` | Pattern matching | Valeur simple (avec %) | `email LIKE "%@gmail.com"` |
| `NOT_MATCHES` | `NOT LIKE` | Pattern négatif | Valeur simple (avec %) | `name NOT LIKE "temp%"` |
| `IN` | `IN` | Appartenance à ensemble | Collection | `status IN ["ACTIVE", "PENDING"]` |
| `NOT_IN` | `NOT IN` | Non-appartenance | Collection | `id NOT IN [1, 2, 3]` |
| `IS_NULL` | `IS NULL` | Vérification nulle | Aucune valeur | `description IS NULL` |
| `NOT_NULL` | `IS NOT NULL` | Vérification non-nulle | Aucune valeur | `email IS NOT NULL` |
| `RANGE` | `BETWEEN` | Vérification d'intervalle | Collection (2 valeurs) | `age BETWEEN [18, 65]` |
| `NOT_RANGE` | `NOT BETWEEN` | Hors intervalle | Collection (2 valeurs) | `price NOT BETWEEN [10, 100]` |
| `CONTAINS` | `CONTAINS` | Contient (collections) | Valeur simple | `tags CONTAINS "premium"` |
| `NOT_CONTAINS` | `NOT CONTAINS` | Ne contient pas | Valeur simple | `tags NOT CONTAINS "deprecated"` |

**Utilitaires d'Opérateurs** :
```java
// Ensembles pré-configurés
Set<Op> textOperators = OperatorUtils.FOR_TEXT;
// EQ, NE, MATCHES, NOT_MATCHES, IN, NOT_IN, IS_NULL, NOT_NULL

Set<Op> numberOperators = OperatorUtils.FOR_NUMBER;
// EQ, NE, GT, GTE, LT, LTE, RANGE, NOT_RANGE, IN, NOT_IN, IS_NULL, NOT_NULL

Set<Op> collectionOperators = OperatorUtils.FOR_COLLECTION;
// CONTAINS, NOT_CONTAINS, IN, NOT_IN, IS_NULL, NOT_NULL

// Validation d'opérateur
boolean supported = OperatorUtils.isOperatorSupported(Op.RANGE, Integer.class);
```

### 5. FilterDefinition & FilterRequest
**Rôle** : Conteneurs type-safe pour les données et logique de filtre

**FilterDefinition** - Une condition de filtrage individuelle :
```java
// Définition simple
FilterDefinition<UserPropertyRef> nameFilter = 
    new FilterDefinition<>(UserPropertyRef.NAME, Op.MATCHES, "John%");

// Avec validation automatique
FilterDefinition<UserPropertyRef> ageFilter = 
    new FilterDefinition<>(UserPropertyRef.AGE, Op.RANGE, List.of(18, 65));

// Validation des types à la construction
try {
    new FilterDefinition<>(UserPropertyRef.AGE, Op.MATCHES, "invalid");
} catch (FilterValidationException e) {
    // Opérateur MATCHES non supporté pour le type Integer
}
```

**FilterRequest** - Requête de filtrage complète :
```java
FilterRequest<UserPropertyRef> request = FilterRequest.<UserPropertyRef>builder()
    .filter("nameFilter", nameFilter)
    .filter("ageFilter", ageFilter)
    .filter("statusFilter", statusFilter)
    .combineWith("nameFilter & ageFilter | statusFilter")
    .build();

// Méthodes d'accès
Map<String, FilterDefinition<UserPropertyRef>> filters = request.getFilters();
String expression = request.getCombineWith();
boolean hasFilter = request.hasFilter("nameFilter");
```

### 6. Context & Condition
**Rôle** : Pont entre les définitions de filtres et les prédicats exécutables

**Interface Context** :
```java
public interface Context {
    void addCondition(String filterKey, Condition condition);
    Optional<Condition> getCondition(String filterKey);
    FilterResolver toResolver();
}
```

**Interface Condition** :
```java
public interface Condition {
    // Combinaison de conditions
    Condition and(Condition other);
    Condition or(Condition other);
    Condition not();
    
    // Conversion vers PredicateResolver
    <T> PredicateResolver<T> toPredicateResolver(Class<T> entityClass);
}
```

**Exemple d'utilisation** :
```java
// Ajout de conditions au contexte
context.addCondition("nameFilter", nameCondition);
context.addCondition("ageFilter", ageCondition);

// Récupération et combinaison
Condition combined = context.getCondition("nameFilter")
    .flatMap(name -> context.getCondition("ageFilter")
        .map(age -> name.and(age)))
    .orElseThrow();
```

## Patterns de Conception Utilisés

### 1. Pattern Builder
Utilisé partout pour la construction d'objets complexes :

```java
// Construction de FilterRequest
FilterRequest<UserPropertyRef> request = FilterRequest.<UserPropertyRef>builder()
    .filter("filter1", definition1)
    .filter("filter2", definition2)
    .combineWith("filter1 & filter2")
    .build();

// Construction de FilterTree
FilterTree tree = FilterTree.builder()
    .condition("nameFilter", nameCondition)
    .condition("ageFilter", ageCondition)
    .combineWith("nameFilter & ageFilter")
    .build();
```

### 2. Pattern Strategy
PropertyReference permet différentes stratégies de validation :

```java
// Propriétés texte supportent le pattern matching
NAME(String.class, OperatorUtils.FOR_TEXT)

// Propriétés numériques supportent les opérations de plage
AGE(Integer.class, OperatorUtils.FOR_NUMBER)

// Propriétés enum supportent l'appartenance à ensemble
STATUS(UserStatus.class, Set.of(Op.EQ, Op.NE, Op.IN))
```

### 3. Pattern Composite
Les conditions peuvent être combinées avec la logique booléenne :

```java
Condition nameCondition = context.getCondition("nameFilter");
Condition ageCondition = context.getCondition("ageFilter");

// Combinaisons
Condition combined = nameCondition.and(ageCondition);
Condition alternative = nameCondition.or(ageCondition);
Condition negated = nameCondition.not();

// Complexe
Condition complex = nameCondition.and(ageCondition.or(statusCondition)).not();
```

### 4. Pattern Template Method
FilterResolver orchestre un processus défini :

1. **Parsing** de l'expression DSL
2. **Population** du contexte avec les définitions de filtres
3. **Génération** de l'arbre de conditions
4. **Conversion** vers un prédicat exécutable

```java
public class FilterResolver {
    public <T> PredicateResolver<T> resolve(Class<T> entityClass, FilterRequest<?> request) {
        // 1. Parse DSL
        FilterTree tree = parser.parse(request.getCombineWith(), /* conditions */);
        
        // 2. Populate context
        populateContext(request.getFilters());
        
        // 3. Generate condition tree
        Condition rootCondition = tree.toCondition(context);
        
        // 4. Convert to predicate
        return rootCondition.toPredicateResolver(entityClass);
    }
}
```

## Système de Validation

FilterQL Core inclut une validation complète à plusieurs niveaux :

### Validation des Types
```java
// S'assure que le type de valeur correspond au type de propriété
try {
    UserPropertyRef.AGE.validate(Op.GT, "invalid"); // Exception
} catch (FilterValidationException e) {
    // "Value type 'String' is not compatible with property type 'Integer'"
}

UserPropertyRef.AGE.validate(Op.GT, 25); // OK
```

### Validation des Opérateurs
```java
// S'assure que l'opérateur est supporté pour la propriété
try {
    UserPropertyRef.NAME.validate(Op.RANGE, List.of("A", "Z")); // Exception
} catch (FilterValidationException e) {
    // "Operator RANGE is not supported for property NAME of type String"
}

UserPropertyRef.NAME.validate(Op.MATCHES, "John%"); // OK
```

### Validation des Collections
```java
// Pour les opérateurs IN, NOT_IN
UserPropertyRef.STATUS.validate(Op.IN, List.of(UserStatus.ACTIVE, UserStatus.PENDING)); // OK

// Pour les opérateurs RANGE, NOT_RANGE
UserPropertyRef.AGE.validate(Op.RANGE, List.of(18, 65)); // OK
try {
    UserPropertyRef.AGE.validate(Op.RANGE, List.of(18)); // Exception
} catch (FilterValidationException e) {
    // "RANGE operator requires exactly 2 values, got 1"
}
```

### Validation DSL
```java
try {
    parser.parse("invalid & & expression", conditions);
} catch (DSLSyntaxException e) {
    // "Invalid syntax at position 9: unexpected token '&'"
    System.out.println("Error at position: " + e.getPosition());
    System.out.println("Problematic token: '" + e.getToken() + "'");
}
```

## Gestion des Erreurs

FilterQL Core fournit des exceptions spécifiques pour différents types d'erreurs :

### Hiérarchie d'Exceptions

```
RuntimeException
├── FilterValidationException    # Erreurs de validation des filtres
└── DSLSyntaxException          # Erreurs de syntaxe DSL
    ├── InvalidExpressionException
    ├── UnknownFilterException
    └── OperatorMismatchException
```

### FilterValidationException
Exception pour les erreurs de validation des filtres :

```java
public class FilterValidationException extends RuntimeException {
    private final String propertyName;
    private final Op operator;
    private final Object value;
    private final ValidationError errorType;
    
    // Constructeurs et getters...
}

// Exemple d'utilisation
try {
    new FilterDefinition<>(UserPropertyRef.AGE, Op.MATCHES, "invalid");
} catch (FilterValidationException e) {
    log.error("Validation error for property {}: {}", 
        e.getPropertyName(), e.getMessage());
}
```

### DSLSyntaxException
Exception pour les erreurs de syntaxe DSL :

```java
public class DSLSyntaxException extends RuntimeException {
    private final String expression;
    private final int position;
    private final String token;
    
    // Constructeurs et getters...
}

// Exemple d'utilisation
try {
    FilterTree tree = parser.parse("filter1 & & filter2", conditions);
} catch (DSLSyntaxException e) {
    log.error("DSL syntax error in expression '{}' at position {}: {}", 
        e.getExpression(), e.getPosition(), e.getMessage());
}
```

## Considérations de Performance

### Mise en Cache
- Les résultats de réflexion des champs sont mis en cache dans ClassUtils
- Les calculs de superclasse sont mis en cache pour de meilleures performances
- Les instances de PropertyReference sont réutilisées automatiquement

### Efficacité Mémoire
- Les objets immutables réduisent l'allocation mémoire
- Pattern Flyweight pour les opérateurs et résultats de validation
- Partage des instances de conditions communes

### Évaluation Paresseuse
- PredicateResolver utilise l'exécution différée
- Les conditions ne sont construites que lorsque nécessaire
- Optimisation automatique des expressions booléennes

**Exemple d'optimisation** :
```java
// Expression originale
"(filter1 & filter2) | (filter1 & filter3)"

// Optimisée automatiquement vers
"filter1 & (filter2 | filter3)"
```

## Points d'Extension

FilterQL Core est conçu pour l'extensibilité :

### Opérateurs Personnalisés
```java
// Extension future pour ajouter de nouveaux opérateurs
public enum CustomOp implements OperatorInterface {
    FUZZY_MATCH("fuzzy_match"),
    REGEXP("regexp"),
    DISTANCE("distance");
    
    // Implémentation de l'interface
}
```

### Validation Personnalisée
```java
public enum CustomPropertyRef implements PropertyReference {
    EMAIL(String.class, Set.of(Op.EQ, Op.MATCHES)) {
        @Override
        public ValidationResult validate(Op operator, Object value) {
            ValidationResult baseResult = super.validate(operator, value);
            if (!baseResult.isValid()) {
                return baseResult;
            }
            
            // Validation email personnalisée
            if (operator == Op.MATCHES && value instanceof String pattern) {
                if (!isValidEmailPattern(pattern)) {
                    return ValidationResult.invalid("Invalid email pattern: " + pattern);
                }
            }
            
            return ValidationResult.valid();
        }
        
        private boolean isValidEmailPattern(String pattern) {
            // Logique de validation personnalisée
            return pattern.contains("@") || pattern.contains("%");
        }
    };
}
```

### Parsers Personnalisés
```java
// Parser DSL personnalisé
public class CustomDSLParser implements Parser {
    @Override
    public FilterTree parse(String expression, Map<String, Condition> conditions) {
        // Implémentation personnalisée du parsing
        // Support pour une syntaxe DSL étendue
    }
}

// Utilisation
FilterResolver resolver = FilterResolver.of(new CustomDSLParser(), context);
```

### Contextes Personnalisés
```java
public class CachedContext implements Context {
    private final Map<String, Condition> cache = new ConcurrentHashMap<>();
    private final Context delegate;
    
    public CachedContext(Context delegate) {
        this.delegate = delegate;
    }
    
    @Override
    public void addCondition(String key, Condition condition) {
        cache.put(key, condition);
        delegate.addCondition(key, condition);
    }
    
    @Override
    public Optional<Condition> getCondition(String key) {
        return Optional.ofNullable(cache.get(key));
    }
    
    @Override
    public FilterResolver toResolver() {
        return delegate.toResolver();
    }
}
```

## Bonnes Pratiques

### 1. Conception des PropertyReference
```java
public enum UserPropertyRef implements PropertyReference {
    // Utilisez des noms descriptifs
    USER_FULL_NAME(String.class, OperatorUtils.FOR_TEXT),
    
    // Groupez les opérateurs liés logiquement
    USER_AGE(Integer.class, OperatorUtils.FOR_NUMBER),
    
    // Soyez spécifique sur les opérations supportées
    USER_STATUS(UserStatus.class, Set.of(Op.EQ, Op.NE, Op.IN)),
    
    // Documentez les contraintes métier
    /**
     * Email de l'utilisateur - doit contenir un '@' pour les opérations MATCHES
     */
    USER_EMAIL(String.class, OperatorUtils.FOR_TEXT) {
        @Override
        public ValidationResult validate(Op operator, Object value) {
            ValidationResult result = super.validate(operator, value);
            if (result.isValid() && operator == Op.MATCHES) {
                String pattern = (String) value;
                if (!pattern.contains("@") && !pattern.contains("%")) {
                    return ValidationResult.invalid("Email pattern should contain '@' or '%'");
                }
            }
            return result;
        }
    };
}
```

### 2. Nommage des Filtres
```java
// Utilisez des noms significatifs métier
FilterRequest.builder()
    .filter("activeCustomers", activeDefinition)
    .filter("premiumTier", premiumDefinition)
    .filter("recentSignups", recentDefinition)
    .combineWith("activeCustomers & (premiumTier | recentSignups)")
    .build();

// Évitez les noms techniques génériques
// ❌ Mauvais
.filter("filter1", definition1)
.filter("filter2", definition2)

// ✅ Bon  
.filter("ageRange", ageDefinition)
.filter("activeStatus", statusDefinition)
```

### 3. Gestion des Erreurs
```java
public List<User> findUsers(FilterRequest<UserPropertyRef> request) {
    try {
        FilterResolver resolver = FilterResolver.of(context);
        PredicateResolver<User> predicate = resolver.resolve(User.class, request);
        // Exécuter la requête...
        
    } catch (DSLSyntaxException e) {
        // Logger et retourner une erreur utilisateur conviviale
        log.error("Invalid filter expression: {}", e.getExpression(), e);
        throw new BadRequestException("Invalid filter expression: " + e.getMessage());
        
    } catch (FilterValidationException e) {
        // Logger et retourner l'erreur de validation
        log.error("Filter validation failed for property {}: {}", 
            e.getPropertyName(), e.getMessage(), e);
        throw new BadRequestException("Invalid filter: " + e.getMessage());
        
    } catch (Exception e) {
        // Erreur inattendue
        log.error("Unexpected error during filtering", e);
        throw new InternalServerErrorException("An error occurred while processing the filter");
    }
}
```

### 4. Tests et Validation
```java
@Test
public void testFilterValidation() {
    // Test toutes les combinaisons d'opérateurs supportés
    for (Op operator : UserPropertyRef.AGE.getSupportedOperators()) {
        Object testValue = generateValidValueFor(operator, Integer.class);
        
        assertDoesNotThrow(() -> 
            new FilterDefinition<>(UserPropertyRef.AGE, operator, testValue));
    }
    
    // Test les opérateurs non supportés
    assertThrows(FilterValidationException.class, () ->
        new FilterDefinition<>(UserPropertyRef.AGE, Op.MATCHES, "invalid"));
}

@Test 
public void testDSLExpressions() {
    Map<String, Condition> conditions = Map.of(
        "filter1", mockCondition(),
        "filter2", mockCondition()
    );
    
    // Test expressions valides
    assertDoesNotThrow(() -> parser.parse("filter1 & filter2", conditions));
    assertDoesNotThrow(() -> parser.parse("(filter1 | filter2) & !filter1", conditions));
    
    // Test expressions invalides
    assertThrows(DSLSyntaxException.class, () -> 
        parser.parse("filter1 & & filter2", conditions));
}
```

## Intégration avec d'Autres Modules

### Avec Spring Adapter
```java
// Le module Core fournit les bases
FilterRequest<UserPropertyRef> request = /* ... */;

// L'adaptateur Spring l'utilise
FilterContext<User, UserPropertyRef> springContext = new FilterContext<>(/* ... */);
FilterResolver resolver = FilterResolver.of(springContext);
```

### Avec des Adaptateurs Personnalisés
```java
// Créer un adaptateur pour MongoDB
public class MongoFilterContext implements Context {
    @Override
    public void addCondition(String key, Condition condition) {
        // Conversion vers critères MongoDB
    }
    
    // Autres méthodes...
}
```

## Prochaines Étapes

- **[Apprendre l'intégration Spring](../spring-adapter/overview.md)**
- **[Voir les exemples Core](examples/basic-usage.md)**
- **[Patterns avancés](examples/advanced-patterns.md)**
- **[Référence API](../api-reference.md)**
- **[Guide de migration](../guides/migration-guide.md)**

## Liens Connexes

- **[Guide de démarrage rapide](../getting-started/quick-start.md)** - Premiers pas avec FilterQL
- **[Architecture complète](../ARCHITECTURE.md)** - Vue d'ensemble de l'architecture
- **[Adaptateur Spring](../spring-adapter/overview.md)** - Intégration Spring Data JPA
- **[Guides pratiques](../guides/best-practices.md)** - Meilleures pratiques et conseils