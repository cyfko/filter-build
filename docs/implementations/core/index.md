---
title: Module Core FilterQL
description: Bibliothèque agnostique de framework avec DSL et validation type-safe
sidebar_position: 1
---

# Module Core FilterQL

> **Bibliothèque Java agnostique de framework pour filtrage dynamique avec DSL type-safe et validation multi-niveaux**

---

## Résumé

Le module **Core** de FilterQL est le cœur de la bibliothèque, fournissant une solution agnostique de framework pour le filtrage dynamique. Il implémente un langage spécifique au domaine (DSL) expressif avec validation type-safe, permettant de transformer des expressions de filtrage complexes en conditions exécutables.

**Extrait du fichier core/java/pom.xml :**
```xml
<groupId>io.github.cyfko</groupId>
<artifactId>filterql-core</artifactId>
<version>3.0.0</version>
<description>Framework-agnostic dynamic filtering with type-safe DSL (core module)</description>
```

---

## Problème / Contexte

### Défis du Filtrage Dynamique

Le filtrage dynamique dans les applications modernes présente plusieurs défis majeurs :

- **Sécurité** : Protection contre l'injection SQL et l'exposition des champs
- **Flexibilité** : Support de logique booléenne complexe avec précédence
- **Validation** : Vérification des types et opérateurs à multiple niveaux
- **Maintenabilité** : Code réutilisable et extensible

### Solution Core FilterQL

Le module core résout ces problèmes par :
- **DSL expressif** avec parser intégré
- **Validation type-safe** via enums PropertyReference
- **Architecture en couches** séparant parsing, validation et exécution
- **Extensibilité** via patterns adaptateur

---

## État de l'Art

### Bibliothèques Similaires

**QueryDSL** :
- ✅ Type safety avec génération de code
- ❌ Complexité de setup et dépendances de build
- ❌ Couplage fort avec frameworks spécifiques

**Criteria API (JPA)** :
- ✅ Standard JPA officiel
- ❌ API verbeux et peu intuitif
- ❌ Pas de DSL expressif

**Spring Data Specifications** :
- ✅ Intégration native Spring
- ❌ Limité à Spring ecosystem
- ❌ Pas de validation type-safe intégrée

### Avantages de FilterQL Core

| Caractéristique | QueryDSL | Criteria API | FilterQL Core |
|-----------------|----------|--------------|---------------|
| **Agnosticisme Framework** | ❌ | ✅ | ✅ |
| **DSL Expressif** | ✅ | ❌ | ✅ |
| **Validation Type-Safe** | ✅ | ❌ | ✅ |
| **Simplicité Configuration** | ❌ | ✅ | ✅ |
| **Extensibilité** | ❌ | ✅ | ✅ |

---

## Approche / Architecture

### Architecture en Couches

```
┌─────────────────────────────────────────┐
│              Couche DSL                 │
│     Parser + FilterTree + AST          │
├─────────────────────────────────────────┤
│           Couche Validation             │
│   PropertyReference + Op + Types        │
├─────────────────────────────────────────┤
│             Couche Modèle               │
│  FilterDefinition + FilterRequest       │
├─────────────────────────────────────────┤
│          Couche Exécution               │
│  Context + Condition + Resolver         │
└─────────────────────────────────────────┘
```

### Flux de Traitement

1. **Parsing DSL** : `"(filter1 & filter2) | !filter3"` → `FilterTree`
2. **Validation** : Vérification PropertyReference + Op + Types
3. **Construction** : FilterDefinition → Context → Condition
4. **Résolution** : Condition → PredicateResolver

---

## Installation & Prérequis

### Dépendances Maven

**Extrait du fichier core/java/pom.xml :**

```xml
<dependencies>
    <!-- Jakarta Persistence API -->
    <dependency>
        <groupId>jakarta.persistence</groupId>
        <artifactId>jakarta.persistence-api</artifactId>
        <version>3.1.0</version>
    </dependency>
    
    <!-- Jakarta Validation API -->
    <dependency>
        <groupId>jakarta.validation</groupId>
        <artifactId>jakarta.validation-api</artifactId>
        <version>3.1.1</version>
    </dependency>
</dependencies>
```

### Configuration Projet

```xml
<dependency>
    <groupId>io.github.cyfko</groupId>
    <artifactId>filterql-core</artifactId>
    <version>3.0.0</version>
</dependency>
```

### Prérequis

- **Java 21+** (défini dans pom.xml)
- **Jakarta Persistence API 3.1+**
- **Jakarta Validation API 3.1+**

---

## Quickstart (Débutant)

### Étape 1 : Définir PropertyReference

**Extrait inspiré des tests - fichier core/java/src/test/java/.../validation/BasePropertyReference.java :**

```java
public enum UserPropertyRef implements PropertyReference {
    NAME(String.class, Set.of(Op.EQ, Op.MATCHES, Op.IN)),
    EMAIL(String.class, Set.of(Op.EQ, Op.MATCHES)),
    AGE(Integer.class, Set.of(Op.EQ, Op.GT, Op.GTE, Op.LT, Op.LTE, Op.RANGE)),
    STATUS(UserStatus.class, Set.of(Op.EQ, Op.NE, Op.IN)),
    CREATED_DATE(LocalDateTime.class, Set.of(Op.EQ, Op.GT, Op.GTE, Op.LT, Op.LTE, Op.RANGE));

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

### Étape 2 : Créer FilterDefinitions

**Extrait de core/java/src/main/java/.../model/FilterDefinition.java :**

```java
// Filtres simples
FilterDefinition<UserPropertyRef> nameFilter = new FilterDefinition<>(
    UserPropertyRef.NAME,
    Op.MATCHES,
    "John%"
);

FilterDefinition<UserPropertyRef> ageFilter = new FilterDefinition<>(
    UserPropertyRef.AGE,
    Op.GTE,
    18
);

FilterDefinition<UserPropertyRef> statusFilter = new FilterDefinition<>(
    UserPropertyRef.STATUS,
    Op.EQ,
    UserStatus.ACTIVE
);
```

### Étape 3 : Construire FilterRequest

**Extrait de core/java/src/main/java/.../model/FilterRequest.java :**

```java
// Utilisation du Builder pattern
FilterRequest<UserPropertyRef> request = FilterRequest.<UserPropertyRef>builder()
    .filter("nameSearch", nameFilter)
    .filter("ageLimit", ageFilter)
    .filter("activeStatus", statusFilter)
    .combineWith("nameSearch & ageLimit & activeStatus")
    .build();
```

### Étape 4 : Parsing et Validation

**Extrait de core/java/src/main/java/.../FilterResolver.java :**

```java
// Parser DSL en FilterTree
Parser parser = new DSLParser();
FilterTree tree = parser.parse(request.combineWith());

// Validation automatique lors de la création
// La validation est intégrée dans PropertyReference
```

---

## Cas d'Usage

### Débutant : Filtrage Simple

**Recherche d'utilisateurs par nom**

```java
public class SimpleUserFilter {
    public static void main(String[] args) {
        // 1. Définir le filtre
        FilterDefinition<UserPropertyRef> nameFilter = new FilterDefinition<>(
            UserPropertyRef.NAME,
            Op.MATCHES,
            "Smith%"
        );
        
        // 2. Créer la requête
        FilterRequest<UserPropertyRef> request = FilterRequest.<UserPropertyRef>builder()
            .filter("nameSearch", nameFilter)
            .combineWith("nameSearch")
            .build();
        
        // 3. Utiliser avec FilterResolver (dans adapter spécifique)
        System.out.println("Request: " + request);
    }
}
```

**Fichier source** : Exemples similaires dans `core/java/src/test/java/io/github/cyfko/filterql/core/model/FilterDefinitionTest.java`

### Intermédiaire : Logique Booléenne

**Recherche avec conditions multiples**

```java
public class AdvancedUserFilter {
    public static void main(String[] args) {
        // Filtres multiples
        Map<String, FilterDefinition<UserPropertyRef>> filters = Map.of(
            "nameFilter", new FilterDefinition<>(UserPropertyRef.NAME, Op.MATCHES, "John%"),
            "ageFilter", new FilterDefinition<>(UserPropertyRef.AGE, Op.GTE, 18),
            "statusFilter", new FilterDefinition<>(UserPropertyRef.STATUS, Op.EQ, UserStatus.ACTIVE),
            "emailFilter", new FilterDefinition<>(UserPropertyRef.EMAIL, Op.MATCHES, "%@company.com")
        );
        
        // Expression booléenne complexe
        FilterRequest<UserPropertyRef> request = new FilterRequest<>(
            filters,
            "(nameFilter & ageFilter) | (statusFilter & emailFilter)"
        );
        
        System.out.println("Complex filter: " + request);
    }
}
```

**Fichier source** : Logique similaire dans `core/java/src/test/java/io/github/cyfko/filterql/core/impl/DSLParserAdvancedTest.java`

### Avancé : Validation et Types Complexes

**Filtres avec validation personnalisée**

```java
public enum ProductPropertyRef implements PropertyReference {
    PRICE(BigDecimal.class, Set.of(Op.EQ, Op.GT, Op.GTE, Op.LT, Op.LTE, Op.RANGE)),
    CATEGORY(Category.class, Set.of(Op.EQ, Op.IN)),
    TAGS(String.class, Set.of(Op.IN)) {
        @Override
        public void validateOperatorForValue(Op operator, Object value) {
            super.validateOperatorForValue(operator, value);
            // Validation personnalisée pour les tags
            if (operator == Op.IN && value instanceof Collection<?> tags) {
                if (tags.size() > 10) {
                    throw new FilterValidationException("Maximum 10 tags allowed");
                }
            }
        }
    };
    
    // Implementation standard...
}
```

**Utilisation avec gestion d'erreurs :**

```java
try {
    FilterDefinition<ProductPropertyRef> invalidFilter = new FilterDefinition<>(
        ProductPropertyRef.TAGS,
        Op.IN,
        List.of("tag1", "tag2", /* ... plus de 10 tags */)
    );
} catch (FilterValidationException e) {
    System.err.println("Validation error: " + e.getMessage());
}
```

**Fichier source** : Validation similaire dans `core/java/src/main/java/io/github/cyfko/filterql/core/validation/PropertyReference.java`

---

## Référence API

### Classes Principales

#### FilterResolver
**Localisation** : `core/java/src/main/java/io/github/cyfko/filterql/core/FilterResolver.java`

| Méthode | Signature | Description |
|---------|-----------|-------------|
| `of(Context)` | `static FilterResolver of(Context context)` | Crée un resolver avec parser par défaut |
| `of(Parser, Context)` | `static FilterResolver of(Parser parser, Context context)` | Crée un resolver avec parser personnalisé |
| `resolve(Class, FilterRequest)` | `<E> PredicateResolver<E> resolve(Class<E> entityClass, FilterRequest<P> request)` | Résout une requête en PredicateResolver |

#### PropertyReference
**Localisation** : `core/java/src/main/java/io/github/cyfko/filterql/core/validation/PropertyReference.java`

| Méthode | Signature | Description |
|---------|-----------|-------------|
| `getType()` | `Class<?> getType()` | Retourne le type Java de la propriété |
| `getSupportedOperators()` | `Set<Op> getSupportedOperators()` | Retourne les opérateurs supportés |
| `validateOperator(Op)` | `void validateOperator(Op operator)` | Valide qu'un opérateur est supporté |
| `validateOperatorForValue(Op, Object)` | `void validateOperatorForValue(Op operator, Object value)` | Valide opérateur + valeur |

#### Op (Énumération)
**Localisation** : `core/java/src/main/java/io/github/cyfko/filterql/core/validation/Op.java`

| Constante | Symbole | Description |
|-----------|---------|-------------|
| `EQ` | `=` | Égalité |
| `NE` | `!=` | Différence |
| `GT` | `>` | Supérieur |
| `GTE` | `>=` | Supérieur ou égal |
| `LT` | `<` | Inférieur |
| `LTE` | `<=` | Inférieur ou égal |
| `MATCHES` | `LIKE` | Correspondance pattern |
| `NOT_MATCHES` | `NOT LIKE` | Non-correspondance pattern |
| `IN` | `IN` | Appartenance collection |
| `NOT_IN` | `NOT IN` | Non-appartenance collection |
| `IS_NULL` | `IS NULL` | Vérification null |
| `NOT_NULL` | `IS NOT NULL` | Vérification non-null |
| `RANGE` | `BETWEEN` | Dans l'intervalle |
| `NOT_RANGE` | `NOT BETWEEN` | Hors intervalle |

#### FilterDefinition
**Localisation** : `core/java/src/main/java/io/github/cyfko/filterql/core/model/FilterDefinition.java`

```java
public record FilterDefinition<P extends Enum<P> & PropertyReference>(
    P ref,           // Référence de propriété
    Op operator,     // Opérateur
    Object value     // Valeur
) { }
```

#### FilterRequest
**Localisation** : `core/java/src/main/java/io/github/cyfko/filterql/core/model/FilterRequest.java`

```java
public record FilterRequest<P extends Enum<P> & PropertyReference>(
    Map<String, FilterDefinition<P>> filters,  // Map des filtres
    String combineWith                          // Expression DSL
) { }
```

---

## Dépannage & FAQ

### Erreurs Courantes

#### FilterValidationException

**Problème** : Opérateur non supporté pour une propriété
```java
// ❌ ERREUR
FilterDefinition<UserPropertyRef> invalidFilter = new FilterDefinition<>(
    UserPropertyRef.AGE,    // Integer type
    Op.MATCHES,             // Text operator
    "18"
);
```

**Solution** : Vérifier les opérateurs supportés
```java
// ✅ CORRECT
FilterDefinition<UserPropertyRef> validFilter = new FilterDefinition<>(
    UserPropertyRef.AGE,
    Op.GTE,                 // Numeric operator
    18
);
```

#### DSLSyntaxException

**Problème** : Syntaxe DSL invalide
```java
// ❌ ERREUR
parser.parse("filter1 & & filter2");  // Double &
```

**Solution** : Vérifier la syntaxe DSL
```java
// ✅ CORRECT
parser.parse("filter1 & filter2");
parser.parse("(filter1 & filter2) | filter3");
parser.parse("!filter1");
```

### Questions Fréquentes

**Q: Comment créer des opérateurs personnalisés ?**

R: Le module core fournit les opérateurs standards. Les opérateurs personnalisés peuvent être implémentés dans les adaptateurs spécifiques au framework.

**Q: Peut-on utiliser des types personnalisés ?**

R: Oui, via l'implémentation de PropertyReference :

```java
public enum CustomPropertyRef implements PropertyReference {
    CUSTOM_FIELD(MyCustomType.class, Set.of(Op.EQ, Op.NE)) {
        @Override
        public void validateOperatorForValue(Op operator, Object value) {
            // Validation personnalisée
        }
    };
}
```

**Q: Comment gérer les propriétés imbriquées ?**

R: Les propriétés imbriquées sont gérées au niveau de l'adaptateur spécifique au framework. Le core définit uniquement la référence logique.

---

## Tests & Validation

### Exécution des Tests

**Commande Maven** :
```bash
cd core/java
./mvnw test
```

**Configuration Surefire** (extrait de pom.xml) :
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.3.1</version>
    <configuration>
        <argLine>
            --add-opens java.base/java.lang=ALL-UNNAMED
            --add-opens java.base/java.util=ALL-UNNAMED
            --add-opens java.base/java.lang.reflect=ALL-UNNAMED
        </argLine>
    </configuration>
</plugin>
```

### Couverture de Tests

**Tests principaux** :
- `ConditionTest.java` : Tests des conditions et combinaisons logiques
- `FilterTreeTest.java` : Tests de l'arbre de filtres et parsing
- `DSLParserTest.java` : Tests du parser DSL de base
- `DSLParserAdvancedTest.java` : Tests DSL complexes
- `FilterDefinitionTest.java` : Tests des définitions de filtres
- `FilterRequestTest.java` : Tests des requêtes complètes

**Fichiers de test** : `core/java/src/test/java/io/github/cyfko/filterql/core/`

---

## Ressources & Crédits

### Documentation Connexe

- **[Spring Adapter](../spring-adapter/)** : Intégration Spring Data JPA
- **[Architecture](../../architecture.md)** : Architecture détaillée du système
- **[Exemples](../../examples.md)** : Cas d'usage complets

### Fichiers Sources Principaux

- **Core** : `core/java/src/main/java/io/github/cyfko/filterql/core/`
- **Tests** : `core/java/src/test/java/io/github/cyfko/filterql/core/`
- **Configuration** : `core/java/pom.xml`

### Standards et Références

- **Jakarta Persistence API 3.1** : Annotations et interfaces de persistance
- **Jakarta Validation API 3.1** : Framework de validation
- **Java 21** : Version minimale requise

---

## Contribuer / Liens Utiles

### Développement

1. **Fork** le repository principal
2. **Créer** une branche pour votre fonctionnalité
3. **Implémenter** avec tests
4. **Soumettre** une pull request

### Standards de Code

- **Java 21** avec features modernes (records, switch expressions)
- **Tests JUnit 5** obligatoires pour nouvelles fonctionnalités
- **Javadoc** complète pour APIs publiques
- **Validation** à tous les niveaux

### Liens Utiles

- **[Repository GitHub](https://github.com/cyfko/filter-build)**
- **[Issues & Bugs](https://github.com/cyfko/filter-build/issues)**
- **[Maven Central](https://central.sonatype.com/namespace/io.github.cyfko)**
- **[Licence MIT](../../LICENSE)**

---

*Documentation générée à partir de l'analyse du code source du module core/java.*