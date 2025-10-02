---
layout: default
title: Accueil
nav_order: 1
description: "FilterQL - Librairie de filtrage dynamique pour Java/Spring avec DSL intuitif"
permalink: /
---

# FilterQL
{: .fs-9 }

Une librairie Java moderne pour le filtrage dynamique avec un DSL intuitif et une architecture hexagonale.
{: .fs-6 .fw-300 }

[Démarrer maintenant](/docs/getting-started/quick-start/){: .btn .btn-primary .fs-5 .mb-4 .mb-md-0 .mr-2 }
[Voir sur GitHub](https://github.com/cyfko/filter-build){: .btn .fs-5 .mb-4 .mb-md-0 }

---

## ✨ Fonctionnalités Principales

<div class="code-example" markdown="1">

### 🏗️ Architecture Hexagonale
Séparation claire entre logique métier et détails techniques avec adaptateurs pour différentes technologies.

### ⚡ DSL Intuitif
Expression de requêtes complexes avec une syntaxe simple : `"(nameFilter & ageFilter) | statusFilter"`

### 🔌 Intégration Spring
Support natif de Spring Data JPA avec auto-configuration et spécifications JPA.

### 📦 Sans Dépendances Core
Le module Core n'a aucune dépendance externe pour une intégration flexible.

</div>

---

## 🚀 Installation Rapide

<div class="code-example" markdown="1">

**Maven**
```xml
<dependency>
    <groupId>io.github.cyfko</groupId>
    <artifactId>filterql-core</artifactId>
    <version>3.0.0</version>
</dependency>

<!-- Pour Spring Boot -->
<dependency>
    <groupId>io.github.cyfko</groupId>
    <artifactId>filterql-spring</artifactId>
    <version>3.0.0</version>
</dependency>
```

**Gradle**
```gradle
implementation 'io.github.cyfko:filterql-core:3.0.0'
implementation 'io.github.cyfko:filterql-spring:3.0.0'
```

</div>

---

## 📖 Premier Exemple

```java
// 1. Définir les propriétés filtrables
public enum UserPropertyRef implements PropertyReference {
    NAME(String.class, OperatorUtils.FOR_TEXT),
    AGE(Integer.class, OperatorUtils.FOR_NUMBER),
    EMAIL(String.class, OperatorUtils.FOR_TEXT);
    
    // Implémentation PropertyReference...
}

// 2. Créer le contexte
FilterContext<User, UserPropertyRef> context = new FilterContext<>(
    User.class, 
    UserPropertyRef.class,
    def -> switch (def.ref()) {
        case NAME -> "name";
        case AGE -> "age"; 
        case EMAIL -> "email";
    }
);

// 3. Créer la requête avec DSL
Map<String, FilterDefinition<UserPropertyRef>> filters = Map.of(
    "nameFilter", new FilterDefinition<>(UserPropertyRef.NAME, Op.LIKE, "John%"),
    "ageFilter", new FilterDefinition<>(UserPropertyRef.AGE, Op.GT, 25)
);

FilterRequest<UserPropertyRef> request = new FilterRequest<>(
    filters,
    "nameFilter & ageFilter"  // DSL expression
);

// 4. Résoudre et exécuter
FilterResolver resolver = FilterResolver.of(context);
PredicateResolver<User> predicateResolver = resolver.resolve(User.class, request);

// Utilisation avec JPA
CriteriaBuilder cb = entityManager.getCriteriaBuilder();
CriteriaQuery<User> query = cb.createQuery(User.class);
Root<User> root = query.from(User.class);
query.where(predicateResolver.resolve(root, query, cb));
List<User> results = entityManager.createQuery(query).getResultList();
```

[Voir le guide complet →](/docs/getting-started/quick-start/)

---

## 📚 Documentation

<div class="grid">
  <div class="grid-item">
    <h3>🎯 Démarrage</h3>
    <p>Installation, configuration et premier projet</p>
    <a href="/docs/getting-started/">Guide de démarrage →</a>
  </div>
  
  <div class="grid-item">
    <h3>🧩 Module Core</h3>
    <p>Architecture, API et exemples d'utilisation</p>
    <a href="/docs/core-module/">Documentation Core →</a>
  </div>
  
  <div class="grid-item">
    <h3>🍃 Spring Adapter</h3>
    <p>Intégration Spring Boot et configuration</p>
    <a href="/docs/spring-adapter/">Documentation Spring →</a>
  </div>
  
  <div class="grid-item">
    <h3>📋 Guides</h3>
    <p>Tutoriels, FAQ et dépannage</p>
    <a href="/docs/guides/">Voir les guides →</a>
  </div>
</div>

---

## 🎭 Architecture

FilterQL utilise une **architecture hexagonale** qui sépare clairement :

- **Core** : Logique métier sans dépendances
- **Adaptateurs** : Intégration avec technologies spécifiques (Spring, JPA)
- **DSL** : Langage de requête intuitif avec support de la syntaxe booléenne

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Client    │───▶│  FilterQL   │───▶│  Adapter    │
│             │    │    Core     │    │  (Spring)   │
└─────────────┘    └─────────────┘    └─────────────┘
                          │
                          ▼
                   ┌─────────────┐
                   │     DSL     │
                   │   Parser    │
                   └─────────────┘
```

[En savoir plus sur l'architecture →](/docs/ARCHITECTURE/)

---

## 🔗 Liens Utiles

- [📖 Javadoc API Reference](/api/javadoc/)
- [🏗️ Architecture détaillée](/docs/ARCHITECTURE/)
- [📝 Exemples complets](/docs/examples/)
- [❓ FAQ](/docs/guides/faq/)
- [🐛 Issues GitHub](https://github.com/cyfko/filter-build/issues)

---

## 🤝 Contribution

FilterQL est un projet open source. Les contributions sont les bienvenues !

1. Fork le projet
2. Créez votre branche de fonctionnalité
3. Commitez vos modifications  
4. Ouvrez une Pull Request

[Guide de contribution →](https://github.com/cyfko/filter-build/blob/main/CONTRIBUTING.md)