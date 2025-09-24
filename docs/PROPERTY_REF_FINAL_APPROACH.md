# Approche PropertyRef Enum Finale - Tous Langages

## Vue d'ensemble

L'approche `PropertyRef` utilise un enum/classe de base avec des méthodes concrètes, permettant aux développeurs de créer leurs propres enums qui étendent `PropertyRef` en ne définissant que les données nécessaires.

## Avantages de cette Approche

1. **Simplicité** : Les développeurs n'ont qu'à étendre l'enum et appeler `super()`
2. **Type Safety** : Validation des propriétés et opérateurs à la compilation
3. **Réutilisabilité** : Toutes les méthodes sont héritées automatiquement
4. **Extensibilité** : Facile d'ajouter de nouvelles propriétés
5. **Sécurité** : Impossible d'utiliser des propriétés non définies
6. **Cohérence** : Même approche dans tous les langages

## Implémentation par Langage

### Java

```java
// Enum de base avec toutes les méthodes
public enum PropertyRef {
    BASE("", Object.class, Set.of());
    
    private final String entityField;
    private final Class<?> type;
    private final Set<Operator> supportedOperators;
    
    // Constructeur et toutes les méthodes
    public String getEntityField() { return entityField; }
    public Class<?> getType() { return type; }
    public boolean supportsOperator(Operator operator) { return supportedOperators.contains(operator); }
    // ... autres méthodes
}

// Enum utilisateur - ultra simple !
public enum UserPropertyRef extends PropertyRef {
    USER_NAME("userName", String.class, Set.of(Operator.LIKE, Operator.EQUALS)),
    USER_AGE("age", Integer.class, Set.of(Operator.EQUALS, Operator.GREATER_THAN, Operator.BETWEEN));
    
    UserPropertyRef(String entityField, Class<?> type, Set<Operator> supportedOperators) {
        super(entityField, type, supportedOperators); // C'est tout !
    }
}
```

### TypeScript

```typescript
// Classe de base avec toutes les méthodes
export abstract class PropertyRef {
  constructor(
    public readonly entityField: string,
    public readonly type: string,
    public readonly supportedOperators: Operator[]
  ) {}

  supportsOperator(operator: Operator): boolean { return this.supportedOperators.includes(operator); }
  validateOperator(operator: Operator): void { /* validation logic */ }
  // ... autres méthodes
}

// Classe utilisateur - ultra simple !
export class UserPropertyRef extends PropertyRef {
  static readonly USER_NAME = new UserPropertyRef("userName", "string", [Operator.LIKE, Operator.EQUALS]);
  static readonly USER_AGE = new UserPropertyRef("age", "number", [Operator.EQUALS, Operator.GREATER_THAN]);
  
  private constructor(entityField: string, type: string, supportedOperators: Operator[]) {
    super(entityField, type, supportedOperators); // C'est tout !
  }
}
```

### Python

```python
# Classe de base avec toutes les méthodes
class PropertyRef:
    def __init__(self, entity_field: str, type_name: str, supported_operators: List[Operator]):
        self.entity_field = entity_field
        self.type_name = type_name
        self.supported_operators = supported_operators
    
    def supports_operator(self, operator: Operator) -> bool: return operator in self.supported_operators
    def validate_operator(self, operator: Operator) -> None: # validation logic
    # ... autres méthodes

# Classe utilisateur - ultra simple !
class UserPropertyRef(PropertyRef):
    USER_NAME = PropertyRef("userName", "str", [Operator.LIKE, Operator.EQUALS])
    USER_AGE = PropertyRef("age", "int", [Operator.EQUALS, Operator.GREATER_THAN])
    # C'est tout ! Pas besoin de constructeur personnalisé
```

### C#

```csharp
// Classe de base avec toutes les méthodes
public abstract class PropertyRef
{
    public string EntityField { get; }
    public string Type { get; }
    public IReadOnlyList<Operator> SupportedOperators { get; }
    
    protected PropertyRef(string entityField, string type, Operator[] supportedOperators)
    {
        EntityField = entityField;
        Type = type;
        SupportedOperators = supportedOperators.ToList().AsReadOnly();
    }
    
    public bool SupportsOperator(Operator @operator) => SupportedOperators.Contains(@operator);
    public void ValidateOperator(Operator @operator) { /* validation logic */ }
    // ... autres méthodes
}

// Classe utilisateur - ultra simple !
public class UserPropertyRef : PropertyRef
{
    public static readonly UserPropertyRef UserName = new UserPropertyRef("userName", "string", new[] { Operator.Like, Operator.Equals });
    public static readonly UserPropertyRef UserAge = new UserPropertyRef("age", "int", new[] { Operator.Equals, Operator.GreaterThan });
    
    private UserPropertyRef(string entityField, string type, Operator[] supportedOperators) 
        : base(entityField, type, supportedOperators) // C'est tout !
    {
    }
}
```

## Utilisation dans les Filtres

### Format JSON (Universel)

```json
{
  "filters": {
    "f1": { "ref": "USER_NAME", "operator": "LIKE", "value": "Smith" },
    "f2": { "ref": "USER_STATUS", "operator": "=", "value": "ACTIVE" },
    "f3": { "ref": "USER_AGE", "operator": ">=", "value": "18" }
  },
  "combineWith": "(f1 & f2) | f3"
}
```

### Code d'Utilisation par Langage

#### Java

```java
// Configuration
PropertyRegistry registry = new PropertyRegistry();
registry.registerAll(UserPropertyRef.class);

JpaFilterService filterService = new JpaFilterService(parser, registry, entityManager);

// Utilisation
FilterRequest request = new FilterRequest(filters, "(f1 & f2) | f3");
List<User> users = filterService.executeFilter(request, User.class);
```

#### TypeScript

```typescript
// Configuration
const registry = new PropertyRegistry();
registry.registerAll(UserPropertyRef);

const filterService = new PrismaFilterService(parser, registry, prismaClient);

// Utilisation
const request = new FilterRequest(filters, "(f1 & f2) | f3");
const users = await filterService.executeFilter(request, User);
```

#### Python

```python
# Configuration
registry = PropertyRegistry()
registry.register_all(UserPropertyRef)

filter_service = SQLAlchemyFilterService(parser, registry, session)

# Utilisation
request = FilterRequest(filters, "(f1 & f2) | f3")
users = filter_service.execute_filter(request, User)
```

#### C#

```csharp
// Configuration
var registry = new PropertyRegistry();
registry.RegisterAll(typeof(UserPropertyRef));

var filterService = new EntityFrameworkFilterService(parser, registry, dbContext);

// Utilisation
var request = new FilterRequest(filters, "(f1 & f2) | f3");
var users = filterService.ExecuteFilter(request, typeof(User));
```

## Avantages par Rapport aux Autres Approches

### vs. PropertyRegistry avec String

**Ancienne approche :**
```java
PropertyRegistry registry = new PropertyRegistry();
registry.registerProperty("USER_NAME", String.class);
// Pas de validation des opérateurs
// Pas de type safety
// Erreurs à l'exécution
```

**Nouvelle approche :**
```java
public enum UserPropertyRef extends PropertyRef {
    USER_NAME("userName", String.class, Set.of(Operator.LIKE, Operator.EQUALS));
    // Validation complète à la compilation
    // Type safety garantie
    // Erreurs détectées tôt
}
```

### vs. Interface PropertyRef

**Interface :**
```java
public interface PropertyRef {
    String getEntityField();
    Class<?> getType();
    boolean supportsOperator(Operator operator);
    // Chaque enum doit implémenter toutes les méthodes
}
```

**Enum de base :**
```java
public enum PropertyRef {
    // Toutes les méthodes sont déjà implémentées
    // Les enums n'ont qu'à appeler super()
}
```

## Bonnes Pratiques

1. **Nommage** : Utiliser des noms descriptifs pour les enums (ex: `UserPropertyRef`, `ProductPropertyRef`)

2. **Opérateurs** : Ne définir que les opérateurs logiquement supportés par le type de propriété

3. **Types** : Utiliser les types exacts des champs d'entité

4. **Organisation** : Grouper les propriétés par entité dans des enums séparés

5. **Documentation** : Documenter les enums avec des exemples d'utilisation

6. **Cohérence** : Utiliser la même approche dans tous les langages

## Migration depuis l'Ancienne Approche

1. Créer un enum qui étend `PropertyRef`
2. Définir toutes les propriétés avec leurs métadonnées
3. Remplacer les appels `registry.registerProperty()` par `registry.registerAll(YourEnum.class)`
4. Mettre à jour les références de propriétés dans les filtres JSON

## Exemples Complets

### Entité User (Tous Langages)

```java
// Java
public enum UserPropertyRef extends PropertyRef {
    USER_NAME("userName", String.class, Set.of(Operator.EQUALS, Operator.LIKE, Operator.IN)),
    USER_AGE("age", Integer.class, Set.of(Operator.EQUALS, Operator.GREATER_THAN, Operator.BETWEEN)),
    USER_STATUS("status", String.class, Set.of(Operator.EQUALS, Operator.IN));
    
    UserPropertyRef(String entityField, Class<?> type, Set<Operator> supportedOperators) {
        super(entityField, type, supportedOperators);
    }
}
```

```typescript
// TypeScript
export class UserPropertyRef extends PropertyRef {
  static readonly USER_NAME = new UserPropertyRef("userName", "string", [Operator.EQUALS, Operator.LIKE, Operator.IN]);
  static readonly USER_AGE = new UserPropertyRef("age", "number", [Operator.EQUALS, Operator.GREATER_THAN, Operator.BETWEEN]);
  static readonly USER_STATUS = new UserPropertyRef("status", "string", [Operator.EQUALS, Operator.IN]);
  
  private constructor(entityField: string, type: string, supportedOperators: Operator[]) {
    super(entityField, type, supportedOperators);
  }
}
```

```python
# Python
class UserPropertyRef(PropertyRef):
    USER_NAME = PropertyRef("userName", "str", [Operator.EQUALS, Operator.LIKE, Operator.IN])
    USER_AGE = PropertyRef("age", "int", [Operator.EQUALS, Operator.GREATER_THAN, Operator.BETWEEN])
    USER_STATUS = PropertyRef("status", "str", [Operator.EQUALS, Operator.IN])
```

```csharp
// C#
public class UserPropertyRef : PropertyRef
{
    public static readonly UserPropertyRef UserName = new UserPropertyRef("userName", "string", new[] { Operator.Equals, Operator.Like, Operator.In });
    public static readonly UserPropertyRef UserAge = new UserPropertyRef("age", "int", new[] { Operator.Equals, Operator.GreaterThan, Operator.Between });
    public static readonly UserPropertyRef UserStatus = new UserPropertyRef("status", "string", new[] { Operator.Equals, Operator.In });
    
    private UserPropertyRef(string entityField, string type, Operator[] supportedOperators) 
        : base(entityField, type, supportedOperators)
    {
    }
}
```

Cette approche simplifie considérablement l'utilisation de la bibliothèque tout en offrant une sécurité et une flexibilité maximales dans tous les langages supportés ! 🎉
