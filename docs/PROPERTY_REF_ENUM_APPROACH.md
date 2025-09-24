# Approche PropertyRef Enum - Guide du Développeur

## Vue d'ensemble

L'approche `PropertyRef` utilise un enum de base avec des méthodes concrètes, permettant aux développeurs de créer leurs propres enums qui étendent `PropertyRef` en ne définissant que les données nécessaires.

## Avantages de cette Approche

1. **Simplicité** : Les développeurs n'ont qu'à étendre l'enum et appeler `super()`
2. **Type Safety** : Validation des propriétés et opérateurs à la compilation
3. **Réutilisabilité** : Toutes les méthodes sont héritées automatiquement
4. **Extensibilité** : Facile d'ajouter de nouvelles propriétés
5. **Sécurité** : Impossible d'utiliser des propriétés non définies

## Structure de Base

### PropertyRef (Enum de Base)

```java
public enum PropertyRef {
    BASE("", Object.class, Set.of()); // Constante de base
    
    private final String entityField;
    private final Class<?> type;
    private final Set<Operator> supportedOperators;
    
    PropertyRef(String entityField, Class<?> type, Set<Operator> supportedOperators) {
        this.entityField = entityField;
        this.type = type;
        this.supportedOperators = supportedOperators;
    }
    
    // Toutes les méthodes sont définies ici et héritées automatiquement
    public String getEntityField() { return entityField; }
    public Class<?> getType() { return type; }
    public boolean supportsOperator(Operator operator) { return supportedOperators.contains(operator); }
    // ... autres méthodes
}
```

## Utilisation par les Développeurs

### 1. Créer un Enum Personnalisé

```java
public enum UserPropertyRef extends PropertyRef {
    // Définir les propriétés avec leurs métadonnées
    USER_NAME("userName", String.class, Set.of(Operator.EQUALS, Operator.LIKE, Operator.IN)),
    USER_AGE("age", Integer.class, Set.of(Operator.EQUALS, Operator.GREATER_THAN, Operator.BETWEEN)),
    USER_STATUS("status", String.class, Set.of(Operator.EQUALS, Operator.IN));
    
    // Constructeur simple - juste appeler super()
    UserPropertyRef(String entityField, Class<?> type, Set<Operator> supportedOperators) {
        super(entityField, type, supportedOperators);
    }
}
```

### 2. Utilisation dans le Code

```java
// Toutes les méthodes sont disponibles automatiquement
UserPropertyRef.USER_NAME.getEntityField();           // "userName"
UserPropertyRef.USER_NAME.getType();                  // String.class
UserPropertyRef.USER_NAME.supportsOperator(Operator.LIKE); // true
UserPropertyRef.USER_NAME.supportsOperator(Operator.GREATER_THAN); // false

// Validation automatique
UserPropertyRef.USER_NAME.validateOperator(Operator.LIKE); // ✓ OK
UserPropertyRef.USER_NAME.validateOperator(Operator.GREATER_THAN); // ✗ Exception
```

### 3. Configuration du PropertyRegistry

```java
PropertyRegistry registry = new PropertyRegistry();

// Enregistrer toutes les propriétés d'un enum
registry.registerAll(UserPropertyRef.class);

// Ou enregistrer des propriétés spécifiques
registry.registerProperties(
    UserPropertyRef.USER_NAME,
    UserPropertyRef.USER_AGE
);
```

## Exemples Complets

### Entité User

```java
public enum UserPropertyRef extends PropertyRef {
    USER_NAME("userName", String.class, Set.of(Operator.EQUALS, Operator.NOT_EQUALS, Operator.LIKE, Operator.NOT_LIKE, Operator.IN, Operator.NOT_IN)),
    USER_EMAIL("email", String.class, Set.of(Operator.EQUALS, Operator.NOT_EQUALS, Operator.LIKE, Operator.NOT_LIKE, Operator.IN, Operator.NOT_IN)),
    USER_STATUS("status", String.class, Set.of(Operator.EQUALS, Operator.NOT_EQUALS, Operator.IN, Operator.NOT_IN)),
    USER_AGE("age", Integer.class, Set.of(Operator.EQUALS, Operator.NOT_EQUALS, Operator.GREATER_THAN, Operator.GREATER_THAN_OR_EQUAL, Operator.LESS_THAN, Operator.LESS_THAN_OR_EQUAL, Operator.IN, Operator.NOT_IN, Operator.BETWEEN, Operator.NOT_BETWEEN)),
    USER_CREATED_DATE("createdDate", LocalDateTime.class, Set.of(Operator.EQUALS, Operator.NOT_EQUALS, Operator.GREATER_THAN, Operator.GREATER_THAN_OR_EQUAL, Operator.LESS_THAN, Operator.LESS_THAN_OR_EQUAL, Operator.BETWEEN, Operator.NOT_BETWEEN)),
    USER_IS_ACTIVE("active", Boolean.class, Set.of(Operator.EQUALS, Operator.NOT_EQUALS));
    
    UserPropertyRef(String entityField, Class<?> type, Set<Operator> supportedOperators) {
        super(entityField, type, supportedOperators);
    }
}
```

### Entité Product

```java
public enum ProductPropertyRef extends PropertyRef {
    PRODUCT_NAME("name", String.class, Set.of(Operator.EQUALS, Operator.NOT_EQUALS, Operator.LIKE, Operator.NOT_LIKE, Operator.IN, Operator.NOT_IN)),
    PRODUCT_PRICE("price", Double.class, Set.of(Operator.EQUALS, Operator.NOT_EQUALS, Operator.GREATER_THAN, Operator.GREATER_THAN_OR_EQUAL, Operator.LESS_THAN, Operator.LESS_THAN_OR_EQUAL, Operator.BETWEEN, Operator.NOT_BETWEEN)),
    PRODUCT_CATEGORY("category", String.class, Set.of(Operator.EQUALS, Operator.NOT_EQUALS, Operator.IN, Operator.NOT_IN)),
    PRODUCT_IN_STOCK("inStock", Boolean.class, Set.of(Operator.EQUALS, Operator.NOT_EQUALS)),
    PRODUCT_QUANTITY("quantity", Integer.class, Set.of(Operator.EQUALS, Operator.NOT_EQUALS, Operator.GREATER_THAN, Operator.GREATER_THAN_OR_EQUAL, Operator.LESS_THAN, Operator.LESS_THAN_OR_EQUAL, Operator.IN, Operator.NOT_IN, Operator.BETWEEN, Operator.NOT_BETWEEN));
    
    ProductPropertyRef(String entityField, Class<?> type, Set<Operator> supportedOperators) {
        super(entityField, type, supportedOperators);
    }
}
```

## Utilisation dans les Filtres

### Format JSON

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

### Code Java

```java
// Configuration
PropertyRegistry registry = new PropertyRegistry();
registry.registerAll(UserPropertyRef.class);

JpaFilterService filterService = new JpaFilterService(parser, registry, entityManager);

// Utilisation
FilterRequest request = new FilterRequest(filters, "(f1 & f2) | f3");
List<User> users = filterService.executeFilter(request, User.class);
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

## Migration depuis l'Ancienne Approche

1. Créer un enum qui étend `PropertyRef`
2. Définir toutes les propriétés avec leurs métadonnées
3. Remplacer les appels `registry.registerProperty()` par `registry.registerAll(YourEnum.class)`
4. Mettre à jour les références de propriétés dans les filtres JSON

Cette approche simplifie considérablement l'utilisation de la bibliothèque tout en offrant une sécurité et une flexibilité maximales.
