---
title: Changelog FilterQL
description: Historique des versions et migrations
sidebar_position: 5
---

# Changelog

> **Historique des versions, changements et guide de migration pour FilterQL**

Toutes les modifications notables de ce projet seront documentées dans ce fichier.

Le format est basé sur [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
et ce projet adhère au [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [3.0.0] - 2024-10-03

> **Version actuelle - Refactorisation majeure et stabilisation API**

### ✨ Ajouté
- **Architecture modulaire** : Séparation claire core/adapters
- **Module core agnostique** : Bibliothèque indépendante du framework
- **Adaptateur Spring Data JPA** : Intégration native avec Specifications
- **DSL Parser avancé** : Support complet logique booléenne avec précédence
- **Validation type-safe** : Validation multi-niveaux (compilation, construction, valeur, parsing)
- **PropertyReference interface** : Contract type-safe pour propriétés filtrable
- **PredicateResolverMapping** : Support logique business personnalisée
- **PathResolverUtils** : Navigation automatique propriétés imbriquées
- **Exception hiérarchy** : `DSLSyntaxException` et `FilterValidationException`
- **Documentation complète** : Guides, exemples, et référence API

### 🔄 Changé
- **BREAKING** : API complètement refactorisée pour type safety
- **BREAKING** : Structure package réorganisée (`io.github.cyfko.filterql.*`)
- **BREAKING** : Nouveaux contracts PropertyReference avec validation
- **BREAKING** : Context API repensé pour extensibilité
- **Performance** : Cache optimisé pour réflexion et validation
- **Build** : Migration vers Maven Central avec nouveau groupId

### 🐛 Corrigé
- Gestion robuste des erreurs de parsing DSL
- Validation cohérente types et opérateurs
- Thread safety pour utilisations concurrentes
- Memory leaks dans cache de réflexion

### 🗑️ Supprimé
- **BREAKING** : Ancienne API 2.x non type-safe
- **BREAKING** : Support Java < 21
- Dépendances transitives inutiles

### 📦 Infrastructure
- **Build** : Configuration Maven Central publishing
- **CI/CD** : Pipeline GitHub Actions automatisé
- **Quality** : Intégration SonarQube et Jacoco
- **Docs** : Site documentation avec GitHub Pages

### 🔧 Configuration Requise
- **Java** : 21+ (LTS recommandé)
- **Spring Boot** : 3.3.4+ (pour adaptateur Spring)
- **Jakarta** : Persistence API 3.1+, Validation API 3.1+

### 📚 Migration depuis 2.x

**Dépendances** :
```xml
<!-- Ancien (2.x) -->
<dependency>
    <groupId>old.groupid</groupId>
    <artifactId>filterql</artifactId>
    <version>2.9.x</version>
</dependency>

<!-- Nouveau (3.0) -->
<dependency>
    <groupId>io.github.cyfko</groupId>
    <artifactId>filterql-core</artifactId>
    <version>3.0.0</version>
</dependency>
<dependency>
    <groupId>io.github.cyfko</groupId>
    <artifactId>filterql-spring</artifactId>
    <version>3.0.0</version>
</dependency>
```

**Packages** :
```java
// Ancien
import old.package.filterql.*;

// Nouveau  
import io.github.cyfko.filterql.core.*;
import io.github.cyfko.filterql.adapter.spring.*;
```

**PropertyReference** :
```java
// Ancien (2.x) - Properties loose
public enum UserProperty {
    NAME, EMAIL, AGE
}

// Nouveau (3.0) - Type-safe PropertyReference
public enum UserPropertyRef implements PropertyReference {
    NAME(String.class, Set.of(Op.EQ, Op.MATCHES)),
    EMAIL(String.class, Set.of(Op.EQ, Op.MATCHES)),
    AGE(Integer.class, Set.of(Op.EQ, Op.GT, Op.GTE, Op.LT, Op.LTE));
    
    private final Class<?> type;
    private final Set<Op> supportedOperators;
    
    // Implementation PropertyReference...
}
```

---

## [2.9.x] - 2024-09 (Legacy)

> **Dernière version 2.x - Support de maintenance uniquement**

### 🔧 Maintenance
- Corrections de sécurité critiques uniquement
- Support Java 17+ maintenu temporairement
- Documentation legacy préservée

### ⚠️ Deprecation Notice
- **Support terminé** : 31 décembre 2024
- **Migration recommandée** : vers FilterQL 3.0+
- **Support communautaire** : GitHub Discussions jusqu'à fin 2024

---

## [2.5.0] - 2024-06 (Legacy)

### ✨ Ajouté
- Support Spring Boot 3.x initial
- Opérateurs RANGE et NOT_RANGE
- Validation basique des types

### 🐛 Corrigé
- Problèmes de performance avec grandes datasets
- Incompatibilités Spring Boot 2.x vs 3.x

---

## [2.0.0] - 2024-01 (Legacy)

### ✨ Ajouté
- Première version avec DSL expressif
- Support Spring Data JPA basique
- Opérateurs de base (EQ, GT, LT, MATCHES, IN)

### 🔄 Changé
- Réécriture complète depuis 1.x
- API Java 17 compatible

---

## [1.x] - 2023 (Archived)

> **Versions archivées - Non supportées**

Versions expérimentales et proof-of-concept. Documentation et support non disponibles.

---

## Guide de Migration Détaillé

### Migration 2.x → 3.0

#### 1. Mise à jour des Dépendances

**Maven** :
```xml
<!-- Remplacer -->
<dependency>
    <groupId>old.groupid</groupId>
    <artifactId>filterql</artifactId>
    <version>2.9.x</version>
</dependency>

<!-- Par -->
<dependency>
    <groupId>io.github.cyfko</groupId>
    <artifactId>filterql-core</artifactId>
    <version>3.0.0</version>
</dependency>
<dependency>
    <groupId>io.github.cyfko</groupId>
    <artifactId>filterql-spring</artifactId>
    <version>3.0.0</version>
</dependency>
```

**Gradle** :
```gradle
// Remplacer
implementation 'old.groupid:filterql:2.9.x'

// Par
implementation 'io.github.cyfko:filterql-core:3.0.0'
implementation 'io.github.cyfko:filterql-spring:3.0.0'
```

#### 2. Refactorisation PropertyReference

**Avant (2.x)** :
```java
public enum UserProperty {
    NAME, EMAIL, AGE, STATUS
}

// Usage
Filter filter = new Filter(UserProperty.NAME, "LIKE", "John%");
```

**Après (3.0)** :
```java
public enum UserPropertyRef implements PropertyReference {
    NAME(String.class, Set.of(Op.EQ, Op.MATCHES, Op.IN)),
    EMAIL(String.class, Set.of(Op.EQ, Op.MATCHES)),
    AGE(Integer.class, Set.of(Op.EQ, Op.GT, Op.GTE, Op.LT, Op.LTE, Op.RANGE)),
    STATUS(UserStatus.class, Set.of(Op.EQ, Op.NE, Op.IN));

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

// Usage
FilterDefinition<UserPropertyRef> filter = new FilterDefinition<>(
    UserPropertyRef.NAME, Op.MATCHES, "John%"
);
```

#### 3. Nouveaux Contracts Context

**Avant (2.x)** :
```java
FilterBuilder builder = new FilterBuilder();
builder.add("name", UserProperty.NAME, "LIKE", "John%");
Specification<User> spec = builder.build();
```

**Après (3.0)** :
```java
// Configuration context
FilterContext<User, UserPropertyRef> context = new FilterContext<>(
    User.class, UserPropertyRef.class,
    def -> switch (def.ref()) {
        case NAME -> "name";
        case EMAIL -> "email";
        case AGE -> "age";
        case STATUS -> "status";
    }
);

// Utilisation
FilterDefinition<UserPropertyRef> nameFilter = new FilterDefinition<>(
    UserPropertyRef.NAME, Op.MATCHES, "John%"
);
Condition condition = context.addCondition("nameFilter", nameFilter);
PredicateResolver<User> resolver = context.toResolver(User.class, condition);
Specification<User> spec = (root, query, cb) -> resolver.resolve(root, query, cb);
```

#### 4. DSL Expression Updates

**Avant (2.x)** :
```java
// DSL limité
String expression = "name:John AND age:>25";
```

**Après (3.0)** :
```java
// DSL complet avec précédence
FilterRequest<UserPropertyRef> request = FilterRequest.<UserPropertyRef>builder()
    .filter("nameFilter", nameFilter)
    .filter("ageFilter", ageFilter)
    .filter("statusFilter", statusFilter)
    .combineWith("(nameFilter & ageFilter) | statusFilter")
    .build();
```

### Outils de Migration Automatique

#### Script de Migration Maven

```bash
#!/bin/bash
# migrate-to-3.0.sh

echo "🚀 Migration FilterQL 2.x → 3.0"

# 1. Update Maven dependencies
echo "📦 Updating pom.xml dependencies..."
sed -i 's/old\.groupid/io.github.cyfko/g' pom.xml
sed -i 's/filterql/filterql-core/g' pom.xml

# 2. Add Spring adapter if Spring detected
if grep -q "spring-boot-starter-data-jpa" pom.xml; then
    echo "🌱 Adding Spring adapter dependency..."
    # Add filterql-spring dependency
fi

# 3. Update imports
echo "📝 Updating import statements..."
find src -name "*.java" -exec sed -i 's/old\.package\.filterql/io.github.cyfko.filterql.core/g' {} \;

echo "✅ Migration completed! Please review and test."
echo "📚 See migration guide: https://your-docs-site.com/migration"
```

#### Template PropertyReference Generator

```java
/**
 * Migration tool: Generate PropertyReference from old enum
 */
public class PropertyReferenceGenerator {
    
    public static void generateFromOldEnum(Class<? extends Enum<?>> oldEnum) {
        System.out.println("// Generated PropertyReference from " + oldEnum.getSimpleName());
        System.out.println("public enum " + oldEnum.getSimpleName() + "Ref implements PropertyReference {");
        
        for (Enum<?> constant : oldEnum.getEnumConstants()) {
            String name = constant.name();
            // Guess common types - manual adjustment needed
            String type = guessType(name);
            String operators = getCommonOperators(type);
            
            System.out.printf("    %s(%s.class, %s),%n", name, type, operators);
        }
        
        System.out.println("""
            
            private final Class<?> type;
            private final Set<Op> supportedOperators;
            
            // Standard PropertyReference implementation...
            """);
    }
}
```

---

## Roadmap Futur

### Version 3.1.0 (Q1 2025)

**Nouvelles Fonctionnalités** :
- [ ] Opérateurs géospatiaux (ST_WITHIN, ST_DISTANCE)
- [ ] Support expressions régulières (REGEX)
- [ ] Opérateurs full-text search améliorés
- [ ] Cache de performance pour requêtes répétées

**Améliorations** :
- [ ] Performance DSL parsing +30%
- [ ] Réduction memory footprint
- [ ] Support MongoDB adapter (preview)

### Version 3.2.0 (Q2 2025)

**Adaptateurs** :
- [ ] MongoDB adapter stable
- [ ] Elasticsearch adapter 
- [ ] R2DBC adapter (reactive)

**Framework Support** :
- [ ] Spring WebFlux integration
- [ ] Quarkus adapter
- [ ] Micronaut adapter

### Version 4.0.0 (Q4 2025)

**Architecture** :
- [ ] Plugin system pour opérateurs custom
- [ ] Multi-tenant filtering
- [ ] GraphQL integration native
- [ ] Performance analytics et monitoring

**Breaking Changes** :
- [ ] Java 21+ minimum requis
- [ ] API cleanup et simplification
- [ ] Nouvelle architecture plugin

---

## Support des Versions

| Version | Status | Java | Spring Boot | Support jusqu'à |
|---------|--------|------|-------------|-----------------|
| **3.0.x** | ✅ **Active** | 21+ | 3.3.4+ | Décembre 2025 |
| **2.9.x** | ⚠️ **Maintenance** | 17+ | 3.x | Décembre 2024 |
| **2.8.x** | ❌ **End of Life** | 17+ | 3.x | Septembre 2024 |
| **1.x** | ❌ **Archived** | 11+ | 2.x | Mars 2024 |

### Politique de Support

- **Active** : Nouvelles fonctionnalités + bug fixes + sécurité
- **Maintenance** : Bug fixes critiques + sécurité uniquement  
- **End of Life** : Aucun support, migration fortement recommandée
- **Archived** : Documentation et artefacts préservés, aucun support

---

## Contributing au Changelog

### Format des Entrées

```markdown
### ✨ Ajouté
- **Fonctionnalité** : Description brève avec impact utilisateur
- **API** : Nouvelle interface ou méthode avec exemple usage

### 🔄 Changé  
- **BREAKING** : Changement non-compatible avec indication migration
- **Performance** : Amélioration mesurable avec métriques

### 🐛 Corrigé
- **Bug** : Description du problème et impact
- **Security** : Correction de sécurité avec niveau de sévérité

### 🗑️ Supprimé
- **BREAKING** : API supprimée avec alternative recommandée
- **Deprecated** : Fonctionnalité dépréciée avec timeline suppression
```

### Guidelines

1. **Clarity** : Descriptions claires pour utilisateurs non-techniques
2. **Impact** : Indiquer l'impact utilisateur de chaque changement
3. **Migration** : Fournir guide migration pour breaking changes
4. **Examples** : Inclure exemples code pour nouvelles APIs
5. **References** : Lier issues/PRs pour contexte détaillé

---

## Ressources

### Links Utiles
- **[Semantic Versioning](https://semver.org/)** : Principes de versioning
- **[Keep a Changelog](https://keepachangelog.com/)** : Format changelog standardisé
- **[Migration Guide](./contributing.md#migration)** : Guide détaillé contribution
- **[GitHub Releases](https://github.com/cyfko/filter-build/releases)** : Archives et downloads

### Support
- **[GitHub Issues](https://github.com/cyfko/filter-build/issues)** : Bugs et feature requests
- **[Discussions](https://github.com/cyfko/filter-build/discussions)** : Questions et aide communautaire
- **[Email](mailto:frank.kossi@kunrin.com)** : Support direct pour questions complexes

---

*Le changelog est maintenu par l'équipe FilterQL et la communauté des contributeurs.*