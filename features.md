---
layout: page
title: Fonctionnalités
description: Découvrez toutes les fonctionnalités de FilterQL
nav_order: 3
category: features
permalink: /features/
show_toc: true
---

# ⚡ Fonctionnalités FilterQL

FilterQL offre un ensemble complet de fonctionnalités pour le filtrage avancé en Java.

## 🎯 Fonctionnalités principales

### Type Safety Complet
{% include alert.html type="info" title="Validation à la compilation" content="FilterQL garantit que tous vos filtres sont validés au moment de la compilation, éliminant les erreurs runtime courantes." %}

{% include code_block.html title="Exemple de type safety" language="java" %}
```java
// ✅ Valide - le compilateur vérifie la compatibilité des types
FilterDefinition<String> nameFilter = 
    new FilterDefinition<>(UserPropertyRef.NAME, Op.EQ, "John");

// ❌ Erreur de compilation - incompatibilité de types
FilterDefinition<String> ageFilter = 
    new FilterDefinition<>(UserPropertyRef.AGE, Op.EQ, "invalid"); // Integer attendu
```

### Architecture Modulaire

<div class="grid">
    <div class="grid-item">
        <h4>🔧 Core Module</h4>
        <p>Moteur de filtrage framework-agnostic</p>
        <span class="badge badge-version">v2.0.0</span>
    </div>
    
    <div class="grid-item">
        <h4>🌱 Spring Adapter</h4>
        <p>Intégration Spring Data JPA</p>
        <span class="badge badge-version">v2.0.0</span>
    </div>
    
    <div class="grid-item">
        <h4>📊 JPA Adapter</h4>
        <p>Support JPA natif</p>
        <span class="badge badge-version">v1.0.1</span>
    </div>
</div>

### Opérateurs Riches

FilterQL supporte une large gamme d'opérateurs pour tous vos besoins de filtrage :

| Opérateur | Description | Exemple |
|-----------|-------------|---------|
| `EQ` | Égalité | `name = "John"` |
| `NE` | Différent | `status != "INACTIVE"` |
| `GT` | Supérieur | `age > 18` |
| `GTE` | Supérieur ou égal | `score >= 50` |
| `LT` | Inférieur | `price < 100` |
| `LTE` | Inférieur ou égal | `quantity <= 0` |
| `IN` | Dans une liste | `category IN ["A", "B"]` |
| `NOT_IN` | Pas dans une liste | `status NOT IN ["DELETED"]` |
| `LIKE` | Pattern matching | `name LIKE "%john%"` |
| `NOT_LIKE` | Pattern exclusion | `email NOT LIKE "%@spam.com"` |
| `IS_NULL` | Valeur nulle | `deletedAt IS NULL` |
| `IS_NOT_NULL` | Valeur non nulle | `createdAt IS NOT NULL` |
| `RANGE` | Entre deux valeurs | `date BETWEEN start AND end` |

### Logique Booléenne Complexe

{% include code_block.html title="Combinaisons logiques avancées" language="java" %}
```java
FilterRequest<ProductPropertyRef> complexFilter = FilterRequest.<ProductPropertyRef>builder()
    .filter("highEnd", new FilterDefinition<>(ProductPropertyRef.PRICE, Op.GT, 1000))
    .filter("electronics", new FilterDefinition<>(ProductPropertyRef.CATEGORY, Op.EQ, "Electronics"))
    .filter("inStock", new FilterDefinition<>(ProductPropertyRef.STOCK, Op.GT, 0))
    .filter("promoted", new FilterDefinition<>(ProductPropertyRef.IS_PROMOTED, Op.EQ, true))
    .filter("recentlyAdded", new FilterDefinition<>(ProductPropertyRef.CREATED_DATE, Op.GTE, lastWeek))
    // (highEnd & electronics & inStock) | (promoted & recentlyAdded)
    .combineWith("(highEnd & electronics & inStock) | (promoted & recentlyAdded)")
    .build();
```

## 🔧 Fonctionnalités avancées

### Validation Personnalisée

{% include alert.html type="tip" title="Extensibilité" content="Ajoutez vos propres validateurs pour des règles métier spécifiques." %}

{% include code_block.html title="Validation personnalisée" language="java" %}
```java
@Component
public class CustomFilterValidator implements FilterValidator<ProductPropertyRef> {
    
    @Override
    public ValidationResult validate(FilterDefinition<ProductPropertyRef> definition) {
        if (definition.getProperty() == ProductPropertyRef.PRICE && 
            definition.getOperator() == Op.GT && 
            (Double) definition.getValue() > 10000) {
            return ValidationResult.error("Prix maximum autorisé : 10,000€");
        }
        return ValidationResult.success();
    }
}
```

### Cache Intelligent

{% include code_block.html title="Configuration du cache" language="java" %}
```java
@Configuration
@EnableCaching
public class FilterCacheConfig {
    
    @Bean
    @CacheConfig(cacheNames = "filterResults")
    public FilterService filterService() {
        return new CachedFilterService();
    }
    
    @Cacheable(key = "#request.hashCode()")
    public List<Product> findProducts(FilterRequest<ProductPropertyRef> request) {
        return repository.findAll(filterService.toSpecification(request));
    }
}
```

### Métadonnées de Requête

{% include code_block.html title="Informations sur la requête" language="java" %}
```java
FilterResult<Product> result = filterService.findWithMetadata(request);

System.out.println("Résultats trouvés: " + result.getTotalElements());
System.out.println("Temps d'exécution: " + result.getExecutionTime() + "ms");
System.out.println("Requête SQL générée: " + result.getGeneratedQuery());
System.out.println("Utilisation du cache: " + result.isCacheHit());
```

## 📊 Performance et Optimisation

### Génération SQL Optimisée

FilterQL génère automatiquement des requêtes SQL optimisées :

{% include code_block.html title="SQL généré automatiquement" language="sql" %}
```sql
SELECT p.* FROM products p 
WHERE p.category = ? 
  AND p.price BETWEEN ? AND ? 
  AND p.stock_quantity > 0
ORDER BY p.created_date DESC
LIMIT 20 OFFSET 0
```

### Statistiques en Temps Réel

{% include alert.html type="success" title="Monitoring intégré" content="Suivez les performances de vos filtres avec des métriques détaillées." %}

- 🕐 **Temps d'exécution moyen** : 15ms
- 📈 **Taux de cache hit** : 85%
- 🔍 **Requêtes optimisées** : 100%
- 💾 **Utilisation mémoire** : < 50MB

## 🔒 Sécurité

### Protection contre l'Injection SQL

{% include alert.html type="danger" title="Sécurité garantie" content="FilterQL utilise exclusivement des requêtes préparées, éliminant totalement les risques d'injection SQL." %}

{% include code_block.html title="Paramètres liés automatiquement" language="java" %}
```java
// ✅ Sécurisé automatiquement
FilterDefinition<String> userInput = 
    new FilterDefinition<>(ProductPropertyRef.NAME, Op.EQ, userProvidedValue);

// Génère : WHERE name = ? (paramètre lié)
// Au lieu de : WHERE name = 'userProvidedValue' (vulnérable)
```

### Validation d'Entrée

{% include code_block.html title="Validation automatique" language="java" %}
```java
@ValidatedFilter
public class ProductFilter {
    
    @ValidProperty(allowedOperators = {Op.EQ, Op.IN})
    private FilterDefinition<String> category;
    
    @ValidProperty(min = 0, max = 10000)
    private FilterDefinition<Double> price;
    
    @ValidProperty(pattern = "^[A-Za-z0-9]+$")
    private FilterDefinition<String> sku;
}
```

## 🌐 Intégrations

### Spring Boot Starter

{% include code_block.html title="Configuration automatique" language="xml" %}
```xml
<dependency>
    <groupId>io.github.filterql</groupId>
    <artifactId>filterql-spring-boot-starter</artifactId>
    <version>2.0.0</version>
</dependency>
```

### REST API Integration

{% include code_block.html title="Endpoint REST automatique" language="java" %}
```java
@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    @GetMapping("/search")
    public Page<Product> search(
        @FilterRequest FilterRequest<ProductPropertyRef> filter,
        Pageable pageable) {
        
        return productService.findAll(filter, pageable);
    }
}
```

## 📱 Support Multi-Framework

<div class="grid">
    <div class="grid-item">
        <h4>🌱 Spring Framework</h4>
        <p>Support complet Spring Boot 3.x</p>
        <span class="badge badge-java">Spring 6.x</span>
    </div>
    
    <div class="grid-item">
        <h4>🏢 Jakarta EE</h4>
        <p>Compatible Jakarta EE 10</p>
        <span class="badge badge-java">Jakarta 10</span>
    </div>
    
    <div class="grid-item">
        <h4>🔧 Framework Agnostic</h4>
        <p>Core utilisable partout</p>
        <span class="badge badge-version">Standalone</span>
    </div>
</div>

---

{% include alert.html type="info" title="En savoir plus" content="Consultez notre [documentation complète](/docs/) pour découvrir toutes les possibilités de FilterQL." %}