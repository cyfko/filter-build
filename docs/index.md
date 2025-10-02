---
layout: page
title: Documentation
description: Guide complet pour utiliser FilterQL efficacement
nav_order: 2
category: docs
permalink: /docs/
show_toc: true
badges:
  - type: version
    text: v3.0.0
  - type: java
    text: Java 21+
---

# 📚 Documentation FilterQL

Bienvenue dans la documentation complète de FilterQL, une bibliothèque Java moderne pour le filtrage flexible et type-safe.

## 🚀 Démarrage rapide

{% include alert.html type="tip" title="Première utilisation ?" content="Consultez notre [Guide de démarrage rapide](/docs/getting-started/) pour être opérationnel en quelques minutes." %}

<div class="grid">
    <div class="grid-item">
        <h3>🏁 Guide de démarrage</h3>
        <p>Installation, configuration de base et premiers pas avec FilterQL.</p>
        <a href="/docs/getting-started/" class="btn btn-primary">Commencer →</a>
    </div>
    
    <div class="grid-item">
        <h3>🧠 Concepts fondamentaux</h3>
        <p>Comprenez les concepts clés : FilterRequest, Context, Operators et plus.</p>
        <a href="/docs/core-concepts/" class="btn btn-secondary">Explorer →</a>
    </div>
    
    <div class="grid-item">
        <h3>🔧 Référence API</h3>
        <p>Documentation complète de toutes les classes et méthodes publiques.</p>
        <a href="/docs/api-reference/" class="btn btn-secondary">Consulter →</a>
    </div>
    
    <div class="grid-item">
        <h3>⚡ Usage avancé</h3>
        <p>Fonctionnalités avancées, optimisations et cas d'usage complexes.</p>
        <a href="/docs/advanced-usage/" class="btn btn-secondary">Approfondir →</a>
    </div>
</div>

## 🔌 Adapters disponibles

FilterQL propose plusieurs adapters pour s'intégrer facilement dans votre écosystème :

### Spring Adapter
- **Version** : 3.0.0
- **Compatibilité** : Spring Boot 3.x, Spring Framework 6.x
- **Fonctionnalités** : Intégration transparente avec Spring Data
- [📖 Documentation Spring](/docs/spring-adapter/)

### JPA Adapter  
- **Version** : 1.0.1
- **Compatibilité** : JPA 3.x, Hibernate 6.x
- **Fonctionnalités** : Génération automatique de requêtes Criteria API
- [📖 Documentation JPA](/docs/jpa-adapter/)

## 🏗️ Architecture

FilterQL suit une architecture hexagonale claire :

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│     Client      │    │   FilterQL      │    │    Adapters     │
│   Application   │◄──►│      Core       │◄──►│  (Spring/JPA)   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

{% include alert.html type="info" content="L'architecture modulaire permet d'ajouter facilement de nouveaux adapters selon vos besoins." %}

## � Structure de la documentation

- **[Guide de démarrage](/docs/getting-started/)** - Installation et premiers pas
- **[Concepts fondamentaux](/docs/core-concepts/)** - Bases théoriques 
- **[Référence API](/docs/api-reference/)** - Documentation technique complète
- **[Usage avancé](/docs/advanced-usage/)** - Techniques avancées
- **[Spring Adapter](/docs/spring-adapter/)** - Intégration Spring
- **[JPA Adapter](/docs/jpa-adapter/)** - Intégration JPA

## 💡 Exemples pratiques

Découvrez des exemples concrets d'utilisation :

- [Exemples complets](/examples/) - Cas d'usage réels
- [Bonnes pratiques](/guides/best-practices/) - Recommandations
- [Guide de migration](/guides/migration-guide/) - Migration depuis d'autres solutions

## 🔧 Cas d'usage courants

### Recherche e-commerce
{% include code_block.html title="Recherche produits avec critères multiples" language="java" %}
```java
FilterRequest<ProductPropertyRef> request = FilterRequest.<ProductPropertyRef>builder()
    .filter("category", new FilterDefinition<>(ProductPropertyRef.CATEGORY, Op.IN, categories))
    .filter("priceRange", new FilterDefinition<>(ProductPropertyRef.PRICE, Op.RANGE, Arrays.asList(min, max)))
    .filter("inStock", new FilterDefinition<>(ProductPropertyRef.STOCK_QUANTITY, Op.GT, 0))
    .combineWith("category & priceRange & inStock")
    .build();
```

### Gestion RH
{% include code_block.html title="Recherche employés avec critères complexes" language="java" %}
```java
FilterRequest<EmployeePropertyRef> request = FilterRequest.<EmployeePropertyRef>builder()
    .filter("department", new FilterDefinition<>(EmployeePropertyRef.DEPARTMENT, Op.EQ, "Engineering"))
    .filter("experience", new FilterDefinition<>(EmployeePropertyRef.YEARS_EXPERIENCE, Op.GTE, 5))
    .filter("performance", new FilterDefinition<>(EmployeePropertyRef.PERFORMANCE_RATING, Op.GTE, 4.0))
    .combineWith("department & (experience | performance)")
    .build();
```

## 🎯 Principes de conception

### Simplicité
FilterQL privilégie l'**expérience développeur** avec des APIs intuitives et un minimum de code boilerplate.

### Type Safety
La **validation au moment de la compilation** prévient les erreurs runtime et offre un excellent support IDE.

### Performance
Optimisé pour les **applications haute performance** avec génération de requêtes efficaces et mise en cache.

### Flexibilité
L'**architecture extensible** supporte la logique métier personnalisée et les exigences de filtrage complexes.

## 📋 Informations sur la version

- **Version actuelle** : 3.0.0
- **Version Java minimale** : 21
- **Compatibilité Spring Boot** : 3.3.4+
- **Jakarta Persistence API** : 3.1.0+

## 🆘 Besoin d'aide ?

- 🐛 [Signaler un bug](https://github.com/cyfko/filter-build/issues)
- 💬 [Poser une question](https://github.com/cyfko/filter-build/discussions)
- 📧 [Contact direct](mailto:support@filterql.dev)

---

{% include alert.html type="success" title="Documentation mise à jour" content="Cette documentation est automatiquement synchronisée avec le code source pour garantir sa précision." %}
