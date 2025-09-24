# Architecture de Filtrage Dynamique Modulaire - Implémentation

## Vue d'ensemble

Cette implémentation fournit une architecture de filtrage dynamique modulaire avec séparation stricte entre un module Core indépendant et des adaptateurs spécifiques pour différentes technologies.

## Structure du Projet

```
filter-build/
├── core/                          # Module Core Java
│   ├── src/main/java/
│   │   └── io/github/cyfko/dynamicfilter/core/
│   │       ├── interfaces/        # Interfaces principales
│   │       ├── impl/             # Implémentation du parseur DSL
│   │       ├── validation/       # Validation et registre des propriétés
│   │       ├── model/           # Modèles de données
│   │       └── exception/       # Exceptions personnalisées
│   └── pom.xml
├── adapters/
│   ├── java-jpa/                # Adaptateur JPA
│   ├── java-spring/             # Adaptateur Spring Data JPA
│   └── ...
├── typescript/
│   ├── core/                    # Module Core TypeScript
│   └── adapters/
│       ├── prisma/              # Adaptateur Prisma
│       └── typeorm/             # Adaptateur TypeORM
├── python/
│   ├── core/                    # Module Core Python
│   └── adapters/
│       ├── sqlalchemy/          # Adaptateur SQLAlchemy
│       └── django/               # Adaptateur Django ORM
├── csharp/
│   ├── DynamicFilter.Core/       # Module Core C#
│   └── DynamicFilter.EntityFramework/ # Adaptateur Entity Framework
└── examples/                    # Exemples d'utilisation
```

## Module Core

### Interfaces Principales

#### Java
```java
public interface Parser {
    FilterTree parse(String dslExpression) throws DSLSyntaxException;
}

public interface Condition {
    Condition and(Condition other);
    Condition or(Condition other);
    Condition not();
}

public interface Context {
    Condition getCondition(String filterKey);
}

public interface FilterTree {
    Condition generate(Context context);
}

public interface FilterExecutor {
    <T> List<T> execute(Condition globalCondition, Class<T> entityClass);
}
```

#### TypeScript
```typescript
export interface Condition {
  and(other: Condition): Condition;
  or(other: Condition): Condition;
  not(): Condition;
}

export interface Context {
  getCondition(filterKey: string): Condition | null;
}

export interface FilterTree {
  generate(context: Context): Condition;
}

export interface Parser {
  parse(dslExpression: string): FilterTree;
}
```

### Parseur DSL

Le parseur DSL supporte :
- **Opérateurs logiques** : `&` (AND), `|` (OR), `!` (NOT)
- **Parenthèses** pour le groupement d'expressions
- **Identifiants** correspondant aux clés de filtre

**Exemples de syntaxe DSL :**
```
f1                    # Filtre simple
f1 & f2               # ET logique
f1 | f2               # OU logique
!f1                   # Négation
(f1 & f2) | !f3       # Expression complexe avec parenthèses
```

### Validation et Sécurité

#### PropertyRegistry
Le registre des propriétés assure la sécurité en :
- Validant que seules les propriétés autorisées peuvent être utilisées
- Mappant les références de propriétés aux types réels
- Empêchant l'accès aux champs sensibles

```java
PropertyRegistry registry = new PropertyRegistry();
registry.registerProperty("USER_NAME", String.class);
registry.registerProperty("STATUS", String.class);
registry.registerProperty("AGE", Integer.class);
```

#### Opérateurs Supportés
- `=` / `EQ` : Égalité
- `!=` / `NE` : Inégalité
- `>` / `GT` : Supérieur à
- `>=` / `GTE` : Supérieur ou égal à
- `<` / `LT` : Inférieur à
- `<=` / `LTE` : Inférieur ou égal à
- `LIKE` : Contient (pour les chaînes)
- `NOT LIKE` : Ne contient pas
- `IN` : Dans une liste de valeurs
- `NOT IN` : Pas dans une liste de valeurs
- `IS NULL` : Est null
- `IS NOT NULL` : N'est pas null
- `BETWEEN` : Entre deux valeurs
- `NOT BETWEEN` : Pas entre deux valeurs

## Adaptateurs

### Java - JPA

#### JpaConditionAdapter
```java
public class JpaConditionAdapter implements Condition {
    private final Predicate predicate;
    private final CriteriaBuilder criteriaBuilder;
    
    @Override
    public Condition and(Condition other) {
        Predicate combinedPredicate = criteriaBuilder.and(predicate, otherJpa.predicate);
        return new JpaConditionAdapter(combinedPredicate, criteriaBuilder);
    }
    // ... autres méthodes
}
```

#### JpaContextAdapter
```java
public class JpaContextAdapter implements Context {
    private Predicate createPredicate(Path<?> propertyPath, Operator operator, Object value, Class<?> expectedType) {
        switch (operator) {
            case EQUALS:
                return criteriaBuilder.equal(propertyPath, convertValue(value, expectedType));
            case LIKE:
                return criteriaBuilder.like((Path<String>) propertyPath, "%" + value + "%");
            // ... autres opérateurs
        }
    }
}
```

### Java - Spring Data JPA

#### SpringSpecificationCondition
```java
public class SpringSpecificationCondition implements Condition {
    private final Specification<?> specification;
    
    @Override
    public Condition and(Condition other) {
        Specification<?> combinedSpec = specification.and(otherSpring.specification);
        return new SpringSpecificationCondition(combinedSpec);
    }
}
```

### TypeScript - Prisma

#### PrismaConditionAdapter
```typescript
export class PrismaConditionAdapter implements PrismaCondition {
  constructor(private whereClause: any) {}

  and(other: Condition): Condition {
    return new PrismaConditionAdapter({
      AND: [this.whereClause, other.whereClause]
    });
  }
}
```

### TypeScript - TypeORM

#### TypeORMConditionAdapter
```typescript
export class TypeORMConditionAdapter implements TypeORMCondition {
  constructor(private conditionBuilder: (qb: SelectQueryBuilder<any>) => SelectQueryBuilder<any>) {}

  applyToQuery(queryBuilder: SelectQueryBuilder<any>): SelectQueryBuilder<any> {
    return this.conditionBuilder(queryBuilder);
  }
}
```

### Python - SQLAlchemy

#### SQLAlchemyCondition
```python
class SQLAlchemyCondition(Condition):
    def __init__(self, clause: ClauseElement):
        self.clause = clause
    
    def and_(self, other: Condition) -> Condition:
        return SQLAlchemyCondition(and_(self.clause, other.clause))
```

### Python - Django ORM

#### DjangoCondition
```python
class DjangoCondition(Condition):
    def __init__(self, q_object: Q):
        self.q_object = q_object
    
    def and_(self, other: Condition) -> Condition:
        return DjangoCondition(self.q_object & other.q_object)
```

### C# - Entity Framework

#### EntityFrameworkCondition
```csharp
public class EntityFrameworkCondition<T> : ICondition
{
    private readonly Expression<Func<T, bool>> _expression;
    
    public ICondition And(ICondition other)
    {
        var combinedExpression = Expression.Lambda<Func<T, bool>>(
            Expression.AndAlso(_expression.Body, otherEf._expression.Body),
            _expression.Parameters);
        return new EntityFrameworkCondition<T>(combinedExpression);
    }
}
```

## Exemples d'Utilisation

### Format JSON d'Entrée

```json
{
  "filters": {
    "f1": { "ref": "USER_NAME", "operator": "LIKE", "value": "Smith" },
    "f2": { "ref": "USER_STATUS", "operator": "EQ", "value": "ACTIVE" },
    "f3": { "ref": "AGE", "operator": ">=", "value": "18" }
  },
  "combineWith": "(f1 & f2) | !f3"
}
```

### Java - JPA
```java
// Configuration
PropertyRegistry registry = new PropertyRegistry();
registry.registerProperty("USER_NAME", String.class);
registry.registerProperty("STATUS", String.class);

JpaFilterService filterService = new JpaFilterService(parser, registry, entityManager);

// Utilisation
FilterRequest request = new FilterRequest(filters, "(f1 & f2) | !f3");
List<User> users = filterService.executeFilter(request, User.class);
```

### TypeScript - Prisma
```typescript
// Configuration
const propertyRegistry = new PropertyRegistry();
propertyRegistry.registerProperty('USER_NAME', 'string');
propertyRegistry.registerProperty('STATUS', 'string');

const prismaService = new PrismaFilterService(parser, propertyRegistry, prisma, 'User');

// Utilisation
const users = await prismaService.executeFilter(filterRequest, User);
```

### Python - Django
```python
# Configuration
property_registry = PropertyRegistry()
property_registry.register_property('USER_NAME', 'string')
property_registry.register_property('STATUS', 'string')

django_service = DjangoFilterService(parser, property_registry, User)

# Utilisation
users = django_service.execute_filter(filter_request, User)
```

## Packaging et Déploiement

### Java (Maven)
```xml
<groupId>io.github.cyfko</groupId>
<artifactId>dynamic-filter-core</artifactId>
<version>1.0.0</version>

<artifactId>dynamic-filter-jpa</artifactId>
<version>1.0.0</version>

<artifactId>dynamic-filter-spring</artifactId>
<version>1.0.0</version>
```

### TypeScript (npm)
```json
{
  "name": "@cyfko/dynamic-filter-core",
  "version": "1.0.0"
}

{
  "name": "@cyfko/dynamic-filter-adapter-prisma",
  "version": "1.0.0"
}

{
  "name": "@cyfko/dynamic-filter-adapter-typeorm",
  "version": "1.0.0"
}
```

### Python (PyPI)
```python
# dynamic-filter-core
# dynamic-filter-sqlalchemy
# dynamic-filter-django
```

### C# (NuGet)
```xml
<PackageId>DynamicFilter.Core</PackageId>
<PackageId>DynamicFilter.EntityFramework</PackageId>
```

## Tests

### Tests du Parseur DSL
- Parsing d'expressions simples et complexes
- Validation des parenthèses
- Gestion des erreurs de syntaxe
- Support des opérateurs logiques

### Tests de Validation
- Validation des références de propriétés
- Validation des opérateurs autorisés
- Conversion de types (String → LocalDateTime, Enum, etc.)

### Tests des Adaptateurs
- Génération correcte des requêtes (JPA, LINQ, Prisma, etc.)
- Combinaison de conditions (AND, OR, NOT)
- Support de tous les opérateurs

## Avantages de l'Architecture

1. **Séparation stricte** : Core indépendant des adaptateurs
2. **Sécurité** : Validation des propriétés via PropertyRef
3. **Extensibilité** : Ajout facile de nouveaux opérateurs et adaptateurs
4. **Performance** : Pas de réflexion inutile, mapping enum direct
5. **Framework-agnostic** : Core réutilisable dans différents écosystèmes
6. **Maintenabilité** : Code clair et bien structuré
7. **Testabilité** : Interfaces bien définies facilitant les tests

## Patterns Utilisés

- **Adapter Pattern** : Adaptation des conditions aux différents ORMs
- **Composite Pattern** : Structure arborescente des conditions
- **Builder Pattern** : Construction des requêtes complexes
- **Strategy Pattern** : Différentes stratégies de parsing et validation
- **Factory Pattern** : Création des adaptateurs selon la technologie

Cette architecture respecte les principes SOLID et offre une solution robuste, sécurisée et extensible pour le filtrage dynamique dans différents écosystèmes technologiques.
