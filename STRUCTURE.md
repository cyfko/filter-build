# Structure du Projet - Dynamic Filter

## Organisation Modulaire

Le projet est organisé en deux dossiers principaux :
- **`core/`** : Modules Core pour chaque langage
- **`adapters/`** : Adaptateurs spécifiques pour chaque technologie

## Structure des Dossiers

```
filter-build/
├── core/                          # Modules Core par langage
│   ├── java/                      # Core Java
│   │   ├── src/main/java/io/github/cyfko/dynamicfilter/core/
│   │   │   ├── interfaces/        # Interfaces principales
│   │   │   ├── impl/             # Implémentation du parseur DSL
│   │   │   ├── validation/       # Validation et registre des propriétés
│   │   │   ├── model/           # Modèles de données
│   │   │   └── exception/       # Exceptions personnalisées
│   │   └── pom.xml               # Configuration Maven
│   │
│   ├── typescript/                # Core TypeScript
│   │   ├── src/
│   │   │   ├── interfaces.ts     # Interfaces principales
│   │   │   ├── validation.ts     # Validation et opérateurs
│   │   │   └── parser.ts         # Parseur DSL
│   │   └── package.json          # Configuration npm
│   │
│   ├── python/                    # Core Python
│   │   ├── dynamic_filter_core/
│   │   │   ├── __init__.py
│   │   │   ├── interfaces.py     # Interfaces principales
│   │   │   ├── validation.py     # Validation et opérateurs
│   │   │   └── parser.py         # Parseur DSL
│   │   └── setup.py              # Configuration PyPI
│   │
│   └── csharp/                    # Core C#
│       ├── DynamicFilter.Core.csproj
│       ├── Interfaces/           # Interfaces principales
│       ├── Models/               # Modèles de données
│       ├── Exceptions/           # Exceptions personnalisées
│       └── Validation/           # Validation et opérateurs
│
├── adapters/                      # Adaptateurs par langage
│   ├── java/                      # Adaptateurs Java
│   │   ├── jpa/                  # Adaptateur JPA
│   │   │   ├── src/main/java/io/github/cyfko/dynamicfilter/jpa/
│   │   │   │   ├── JpaConditionAdapter.java
│   │   │   │   ├── JpaContextAdapter.java
│   │   │   │   ├── JpaFilterExecutor.java
│   │   │   │   └── JpaFilterService.java
│   │   │   └── pom.xml
│   │   │
│   │   └── spring/               # Adaptateur Spring Data JPA
│   │       ├── src/main/java/io/github/cyfko/dynamicfilter/spring/
│   │       │   ├── SpringSpecificationAdapter.java
│   │       │   ├── SpringSpecificationCondition.java
│   │       │   └── SpringFilterService.java
│   │       └── pom.xml
│   │
│   ├── typescript/                # Adaptateurs TypeScript
│   │   ├── prisma/               # Adaptateur Prisma
│   │   │   ├── src/
│   │   │   │   └── prisma-adapter.ts
│   │   │   └── package.json
│   │   │
│   │   └── typeorm/              # Adaptateur TypeORM
│   │       ├── src/
│   │       │   └── typeorm-adapter.ts
│   │       └── package.json
│   │
│   ├── python/                    # Adaptateurs Python
│   │   ├── sqlalchemy/           # Adaptateur SQLAlchemy
│   │   │   ├── sqlalchemy_adapter.py
│   │   │   └── setup.py
│   │   │
│   │   └── django/               # Adaptateur Django ORM
│   │       ├── django_adapter.py
│   │       └── setup.py
│   │
│   └── csharp/                    # Adaptateurs C#
│       ├── DynamicFilter.EntityFramework.csproj
│       └── Adapters/
│           ├── EntityFrameworkCondition.cs
│           ├── EntityFrameworkContextAdapter.cs
│           └── EntityFrameworkFilterService.cs
│
├── examples/                      # Exemples d'utilisation
│   ├── java-example/
│   ├── typescript-example/
│   ├── python-example/
│   └── csharp-example/
│
├── tests/                         # Tests d'intégration
│   └── integration-test-example.md
│
├── docs/                          # Documentation
│   └── ARCHITECTURE.md
│
├── pom.xml                        # POM parent Maven (Java uniquement)
├── README.md                      # Documentation principale
└── ARCHITECTURE_IMPLEMENTATION.md # Documentation technique
```

## Modules Core

### Java (`core/java/`)
- **Package** : `io.github.cyfko.dynamicfilter.core`
- **Artifact** : `dynamic-filter-core`
- **Dépendances** : Aucune (module pur)
- **Build** : Maven

### TypeScript (`core/typescript/`)
- **Package** : `@cyfko/dynamic-filter-core`
- **Dépendances** : Aucune (module pur)
- **Build** : npm/TypeScript

### Python (`core/python/`)
- **Package** : `dynamic-filter-core`
- **Dépendances** : Aucune (module pur)
- **Build** : PyPI/setuptools

### C# (`core/csharp/`)
- **Package** : `DynamicFilter.Core`
- **Dépendances** : Aucune (module pur)
- **Build** : NuGet/.NET

## Adaptateurs

### Java
- **JPA** : `dynamic-filter-jpa` - Adaptateur JPA CriteriaBuilder
- **Spring** : `dynamic-filter-spring` - Adaptateur Spring Data JPA Specifications

### TypeScript
- **Prisma** : `@cyfko/dynamic-filter-adapter-prisma` - Adaptateur Prisma
- **TypeORM** : `@cyfko/dynamic-filter-adapter-typeorm` - Adaptateur TypeORM

### Python
- **SQLAlchemy** : `dynamic-filter-sqlalchemy` - Adaptateur SQLAlchemy
- **Django** : `dynamic-filter-django` - Adaptateur Django ORM

### C#
- **Entity Framework** : `DynamicFilter.EntityFramework` - Adaptateur Entity Framework

## Avantages de cette Structure

1. **Séparation claire** : Core et adaptateurs sont clairement séparés
2. **Organisation par langage** : Chaque langage a son propre dossier
3. **Modularité** : Chaque adaptateur est indépendant
4. **Maintenabilité** : Structure claire et logique
5. **Extensibilité** : Facile d'ajouter de nouveaux adaptateurs
6. **Packaging** : Chaque module peut être publié indépendamment

## Utilisation

### Installation des dépendances

#### Java (Maven)
```xml
<dependency>
    <groupId>io.github.cyfko</groupId>
    <artifactId>dynamic-filter-core</artifactId>
    <version>1.0.0</version>
</dependency>

<dependency>
    <groupId>io.github.cyfko</groupId>
    <artifactId>dynamic-filter-jpa</artifactId>
    <version>1.0.0</version>
</dependency>
```

#### TypeScript (npm)
```bash
npm install @cyfko/dynamic-filter-core
npm install @cyfko/dynamic-filter-adapter-prisma
```

#### Python (pip)
```bash
pip install dynamic-filter-core
pip install dynamic-filter-sqlalchemy
```

#### C# (NuGet)
```xml
<PackageReference Include="DynamicFilter.Core" Version="1.0.0" />
<PackageReference Include="DynamicFilter.EntityFramework" Version="1.0.0" />
```

Cette structure permet une organisation claire et modulaire du projet, facilitant la maintenance et l'extensibilité de l'architecture de filtrage dynamique.
