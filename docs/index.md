---
title: FilterQL Documentation
description: Framework-agnostic dynamic filtering library with type-safe DSL
sidebar_position: 1
---

# FilterQL - Dynamic Filter Builder

> **Transformez des exigences de filtrage complexes et dynamiques en solutions simples, sécurisées et maintenables**

---

## Résumé du Projet

**FilterQL** est une bibliothèque Java agnostique de framework qui transforme des exigences de filtrage complexes et dynamiques en solutions simples, sécurisées et maintenables. Elle fournit un langage spécifique au domaine (DSL) pour construire des conditions de filtre complexes tout en maintenant la sécurité des types et en prévenant les injections SQL.

La bibliothèque implémente une architecture en couches qui sépare les préoccupations à travers quatre couches distinctes, permettant un filtrage dynamique sécurisé avec une validation complète.

---

## Problème Adressé

Les applications web modernes ont souvent besoin de **recherche et de filtrage dynamiques pilotés par l'utilisateur**. Les approches traditionnelles présentent des limitations importantes :

### ❌ Limitations des Approches Traditionnelles

**Filtres Statiques** : Conditions codées en dur qui ne peuvent pas s'adapter aux besoins des utilisateurs
```java
// Inflexible - que faire si les utilisateurs veulent une logique OR au lieu de AND ?
public List<User> findUsers(String name, UserStatus status, Integer minAge) {
    return repository.findByNameAndStatusAndAgeGreaterThan(name, status, minAge);
}
```

**Exposition des Requêtes Brutes** : Risques de sécurité et couplage serré
```java
// Dangereux - expose la structure de base de données et les risques d'injection SQL
public List<User> search(String whereClause) {
    return entityManager.createQuery("SELECT u FROM User u WHERE " + whereClause).getResultList();
}
```

**Explosion des Paramètres** : Signatures de méthodes ingérables
```java
// Non maintenable - la méthode grandit avec chaque nouveau filtre
public Page<User> findUsers(String name, String email, Integer minAge, Integer maxAge, 
                           UserStatus status, List<String> roles, LocalDateTime createdAfter, 
                           Boolean isActive, String department, String sortBy, String sortDirection) {
    // Logique conditionnelle complexe...
}
```

---

## État de l'Art

### Solutions Existantes

**Query Builders Traditionnels** : 
- **Avantages** : Composition dynamique de requêtes
- **Inconvénients** : Exposition directe des champs de base de données, risques de sécurité
- **Exemples** : QueryDSL, Criteria API directe

**Solutions ORM Spécifiques** :
- **Avantages** : Intégration native avec les frameworks
- **Inconvénients** : Couplage fort, manque de validation de types
- **Exemples** : Spring Data Specifications, Hibernate Criteria

**Bibliothèques de Filtrage** :
- **Avantages** : APIs simplifiées pour des cas d'usage courants
- **Inconvénients** : Flexibilité limitée, manque de validation complète

### Positionnement de FilterQL

FilterQL se différencie en combinant :
- **Sécurité par design** avec des références abstraites de propriétés
- **Validation multi-niveaux** (compilation, construction, exécution)  
- **Agnosticisme de framework** avec des adaptateurs spécialisés
- **DSL expressif** supportant la logique booléenne complexe
- **Sécurité des types** avec validation à la compilation

---

## Approche Choisie

### Architecture en Couches

FilterQL suit une **architecture à quatre couches** avec séparation claire des responsabilités :

```
┌─────────────────────────────────────────┐
│              Couche DSL                 │
│  (Parser, FilterTree, Logique Booléenne)│
├─────────────────────────────────────────┤
│           Couche Validation             │
│   (PropertyReference, Operators, Op)    │
├─────────────────────────────────────────┤
│             Couche Modèle               │
│  (FilterDefinition, FilterRequest)      │
├─────────────────────────────────────────┤
│          Couche Exécution               │
│  (Context, Condition, PredicateResolver)│
└─────────────────────────────────────────┘
```

### Processus de Filtrage

1. **Parsing DSL et Validation** : Transformation du DSL textuel en logique booléenne exécutable
2. **Construction de Filtres Type-Safe** : Validation des propriétés-opérateurs à la compilation
3. **Population du Contexte** : Stockage des conditions validées et construction de l'arbre
4. **Exécution Native** : Conversion vers des constructions spécifiques au framework

### Exemple Complet

```java
// 1. Définir les références de propriétés
public enum UserPropertyRef implements PropertyReference {
    NAME(String.class, Set.of(Op.EQ, Op.MATCHES, Op.IN)),
    EMAIL(String.class, Set.of(Op.EQ, Op.MATCHES)),
    AGE(Integer.class, Set.of(Op.EQ, Op.GT, Op.GTE, Op.LT, Op.LTE, Op.RANGE)),
    STATUS(UserStatus.class, Set.of(Op.EQ, Op.NE, Op.IN));
}

// 2. Créer le contexte avec mappings
FilterContext<User, UserPropertyRef> context = new FilterContext<>(
    User.class, UserPropertyRef.class,
    definition -> switch (definition.ref()) {
        case NAME -> "name";
        case EMAIL -> "email";
        case AGE -> "age";
        case STATUS -> "status";
    }
);

// 3. Construire et exécuter les filtres
FilterResolver resolver = FilterResolver.of(context);
PredicateResolver<User> predicateResolver = resolver.resolve(User.class, request);
Specification<User> specification = (root, query, cb) -> 
    predicateResolver.resolve(root, query, cb);
Page<User> results = userRepository.findAll(specification, pageable);
```

---

## Public Cible et Rôles

### Développeurs Backend
- **Besoin** : Intégration simple dans les APIs REST existantes
- **Solution** : Adaptateurs prêts à l'emploi pour Spring Data JPA
- **Avantage** : Réduction drastique du code boilerplate

### Architectes Système
- **Besoin** : Solutions évolutives et sécurisées
- **Solution** : Architecture en couches avec séparation des responsabilités
- **Avantage** : Extensibilité et maintenabilité à long terme

### Équipes Frontend
- **Besoin** : APIs de filtrage flexibles et expressives
- **Solution** : DSL JSON intuitif avec logique booléenne
- **Avantage** : Construction dynamique d'interfaces utilisateur complexes

---

## Caractéristiques Principales

### 🔒 Sécurité par Design
- **Références abstraites** : Pas d'exposition directe des champs de base de données
- **Validation multi-niveaux** : Compilation, construction, valeur, parsing
- **Prévention d'injection** : Liaisons de paramètres type-safe

### ⚡ Composition Dynamique
- **DSL expressif** : Support de `&` (AND), `|` (OR), `!` (NOT), parenthèses
- **Filtres réutilisables** : Conditions nommées combinables
- **Logique complexe** : Expression booléenne avec précédence

### 🎯 Sécurité des Types
- **Validation à la compilation** : Énumérations PropertyReference 
- **Vérification d'opérateurs** : Compatibilité propriété-opérateur validée
- **Types de valeurs** : Validation automatique des types de données

### 🔧 Intégration Framework
- **Adaptateur Spring** : Support natif Spring Data JPA prêt à l'emploi
- **Architecture extensible** : Pattern adaptateur pour autres frameworks
- **Préservation des fonctionnalités** : Pagination, tri, cache, transactions

---

## Table des Matières

### Implémentations

| Module | Description | Documentation |
|--------|-------------|---------------|
| **[Core](./implementations/core/)** | Bibliothèque agnostique de framework avec DSL et validation | [Guide Core →](./implementations/core/) |
| **[Spring Adapter](./implementations/spring-adapter/)** | Adaptateur Spring Data JPA avec Specifications | [Guide Spring →](./implementations/spring-adapter/) |

### Guides et Références

| Section | Description | Lien |
|---------|-------------|------|
| **[Architecture](./architecture.md)** | Architecture détaillée et patterns de conception | [Architecture →](./architecture.md) |
| **[Installation](./installation.md)** | Guide d'installation et configuration | [Installation →](./installation.md) |
| **[Contributing](./contributing.md)** | Guide de contribution et développement | [Contributing →](./contributing.md) |
| **[Changelog](./changelog.md)** | Historique des versions et migrations | [Changelog →](./changelog.md) |

### Ressources

- **[Exemples Complets](./examples/)** : Cas d'usage réels et patterns avancés
- **[FAQ](./faq.md)** : Questions fréquemment posées
- **[Troubleshooting](./troubleshooting.md)** : Dépannage et solutions communes
- **[API Reference](./api/)** : Documentation complète des APIs

---

## Démarrage Rapide

### 1. Installation

```xml
<!-- Module core (requis) -->
<dependency>
    <groupId>io.github.cyfko</groupId>
    <artifactId>filterql-core</artifactId>
    <version>3.0.0</version>
</dependency>

<!-- Adaptateur Spring Data JPA -->
<dependency>
    <groupId>io.github.cyfko</groupId>
    <artifactId>filterql-spring</artifactId>
    <version>3.0.0</version>
</dependency>
```

### 2. Configuration

```java
@Configuration
public class FilterConfig {
    @Bean
    public FilterContext<User, UserPropertyRef> userFilterContext() {
        return new FilterContext<>(User.class, UserPropertyRef.class, 
            definition -> switch (definition.ref()) {
                case NAME -> "name";
                case EMAIL -> "email";
                case AGE -> "age";
                case STATUS -> "status";
            });
    }
}
```

### 3. Utilisation

```java
@RestController
public class UserController {
    @PostMapping("/users/search")
    public Page<User> searchUsers(
            @RequestBody FilterRequest<UserPropertyRef> request,
            Pageable pageable) {
        
        FilterResolver resolver = FilterResolver.of(userFilterContext);
        PredicateResolver<User> predicateResolver = resolver.resolve(User.class, request);
        Specification<User> specification = (root, query, cb) -> 
            predicateResolver.resolve(root, query, cb);
        return userRepository.findAll(specification, pageable);
    }
}
```

---

## Exemples de Requêtes

### Recherche E-commerce
```json
{
  "filters": {
    "category": { "ref": "CATEGORY", "operator": "IN", "value": ["ELECTRONICS", "BOOKS"] },
    "priceRange": { "ref": "PRICE", "operator": "RANGE", "value": [10.0, 100.0] },
    "inStock": { "ref": "STOCK_QUANTITY", "operator": "GT", "value": 0 },
    "featured": { "ref": "IS_FEATURED", "operator": "EQ", "value": true }
  },
  "combineWith": "category & priceRange & inStock & featured"
}
```

### Gestion RH
```json
{
  "filters": {
    "highPerformance": { "ref": "PERFORMANCE_RATING", "operator": "GTE", "value": 4.5 },
    "experience": { "ref": "YEARS_EXPERIENCE", "operator": "GTE", "value": 3 },
    "activeStatus": { "ref": "STATUS", "operator": "EQ", "value": "ACTIVE" },
    "eligibleDepts": { "ref": "DEPARTMENT", "operator": "IN", "value": ["ENGINEERING", "PRODUCT"] }
  },
  "combineWith": "activeStatus & eligibleDepts & (highPerformance | experience)"
}
```

---

## Contribution et Support

### Contribution

Nous accueillons les contributions ! Consultez notre [Guide de Contribution](./contributing.md) pour :
- Code de conduite
- Configuration de développement  
- Processus de pull request
- Standards de codage

### Support

- **[GitHub Issues](https://github.com/cyfko/filter-build/issues)** : Rapports de bugs et demandes de fonctionnalités
- **[Documentation](./troubleshooting.md)** : Guides de dépannage
- **[Discussions](https://github.com/cyfko/filter-build/discussions)** : Questions et bonnes pratiques

---

## Licence

Ce projet est sous licence MIT - voir le fichier [LICENSE](../LICENSE) pour plus de détails.

---

**FilterQL** - Rendre le filtrage dynamique simple, sûr et puissant.