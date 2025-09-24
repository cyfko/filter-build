# Adaptateurs Basés sur Lambda - Approche Finale

## 🎯 **Exigence Architecturale**

> "Exige que chaque implémentation d'adaptateur attend une lambda avec son unique paramètre d'entrée de type FilterDefinition"

## ✅ **Architecture Finale**

### **Interface Context Modifiée**
```java
public interface Context {
    /**
     * Creates a condition from a FilterDefinition.
     * Each adapter implementation provides its own lambda to interpret PropertyRef.
     */
    Condition createCondition(FilterDefinition filterDefinition);
    
    /**
     * Gets the condition factory function for this context.
     * This allows external code to create conditions using the adapter's logic.
     */
    Function<FilterDefinition, Condition> getConditionFactory();
}
```

### **FilterDefinition avec PropertyRef Direct**
```java
public class FilterDefinition {
    private final PropertyRef ref;        // PropertyRef directement
    private final String operator;
    private final Object value;
    
    public FilterDefinition(PropertyRef ref, String operator, Object value) {
        this.ref = ref;
        this.operator = operator;
        this.value = value;
    }
}
```

## 🏗️ **Implémentation des Adaptateurs**

### **1. JPA Adapter**
```java
public class JpaContextAdapter implements Context {
    private final CriteriaBuilder criteriaBuilder;
    private final Root<?> root;
    private final Function<FilterDefinition, Condition> conditionFactory;
    
    public JpaContextAdapter(CriteriaBuilder criteriaBuilder, Root<?> root) {
        this.criteriaBuilder = criteriaBuilder;
        this.root = root;
        this.conditionFactory = this::createCondition; // Lambda !
    }
    
    @Override
    public Condition createCondition(FilterDefinition filterDefinition) {
        PropertyRef propertyRef = filterDefinition.getRef();
        Operator operator = Operator.fromString(filterDefinition.getOperator());
        
        // Validation
        propertyRef.validateOperator(operator);
        
        // JPA-specific logic
        Predicate predicate = createPredicateForProperty(propertyRef, operator, filterDefinition.getValue());
        return new JpaConditionAdapter(predicate, criteriaBuilder);
    }
    
    @Override
    public Function<FilterDefinition, Condition> getConditionFactory() {
        return conditionFactory;
    }
}
```

### **2. Prisma Adapter**
```typescript
export class PrismaContextAdapter implements Context {
    private readonly conditionFactory: (filter: FilterDefinition) => Condition;
    
    constructor() {
        this.conditionFactory = this.createCondition.bind(this);
    }
    
    createCondition(filterDefinition: FilterDefinition): Condition {
        const propertyRef = filterDefinition.ref;
        const operator = filterDefinition.operator;
        const value = filterDefinition.value;
        
        // Prisma-specific logic
        if (propertyRef.name === "USER_NAME") {
            if (operator === "LIKE") {
                return new PrismaCondition(
                    `where: { OR: [{ first_name: { contains: '${value}' } }, { last_name: { contains: '${value}' } }] }`
                );
            }
        }
        // ... autres mappings
    }
    
    getConditionFactory(): (filter: FilterDefinition) => Condition {
        return this.conditionFactory;
    }
}
```

### **3. SQLAlchemy Adapter**
```python
class SqlAlchemyContextAdapter(Context):
    def __init__(self):
        self.condition_factory = self.create_condition
    
    def create_condition(self, filter_definition: FilterDefinition) -> Condition:
        property_ref = filter_definition.ref
        operator = filter_definition.operator
        value = filter_definition.value
        
        # SQLAlchemy-specific logic
        if property_ref.name == "USER_NAME":
            if operator == "LIKE":
                return SqlAlchemyCondition(
                    f"User.query.filter(or_(User.first_name.like('%{value}%'), User.last_name.like('%{value}%')))"
                )
        # ... autres mappings
    
    def get_condition_factory(self) -> Callable[[FilterDefinition], Condition]:
        return self.condition_factory
```

## 🔄 **Flux d'Utilisation**

### **1. Création des Filtres**
```java
// Création directe avec PropertyRef
Map<String, FilterDefinition> filters = new HashMap<>();
filters.put("f1", new FilterDefinition(
    UserPropertyRef.USER_NAME,  // PropertyRef directement
    "LIKE",
    "Smith"
));

FilterRequest request = new FilterRequest(filters, "f1 & f2");
```

### **2. Utilisation des Adaptateurs**
```java
// Chaque adaptateur fournit sa lambda
Context jpaContext = new JpaContextAdapter(criteriaBuilder, root);
Function<FilterDefinition, Condition> jpaFactory = jpaContext.getConditionFactory();

// Utilisation de la lambda
FilterDefinition filter = new FilterDefinition(UserPropertyRef.USER_NAME, "LIKE", "Smith");
Condition condition = jpaFactory.apply(filter);
```

### **3. Flexibilité Maximale**
```java
// Chaque adaptateur peut interpréter PropertyRef différemment
Context prismaContext = new PrismaContextAdapter();
Function<FilterDefinition, Condition> prismaFactory = prismaContext.getConditionFactory();

// Même FilterDefinition, logique différente
Condition prismaCondition = prismaFactory.apply(filter);
// Prisma: where: { OR: [{ first_name: { contains: 'Smith' } }, { last_name: { contains: 'Smith' } }] }
```

## 🎯 **Exemples de Flexibilité**

### **1. USER_NAME - Mapping Multiple Champs**

#### **JPA Adapter**
```java
if (propertyRef.name().equals("USER_NAME")) {
    if (operator == Operator.LIKE) {
        // Recherche dans firstName ET lastName
        return criteriaBuilder.or(
            criteriaBuilder.like(firstNamePath, "%" + value + "%"),
            criteriaBuilder.like(lastNamePath, "%" + value + "%")
        );
    }
}
```

#### **Prisma Adapter**
```typescript
if (propertyRef.name === "USER_NAME") {
    if (operator === "LIKE") {
        return new PrismaCondition(
            `where: { OR: [{ first_name: { contains: '${value}' } }, { last_name: { contains: '${value}' } }] }`
        );
    }
}
```

#### **SQLAlchemy Adapter**
```python
if property_ref.name == "USER_NAME":
    if operator == "LIKE":
        return SqlAlchemyCondition(
            f"User.query.filter(or_(User.first_name.like('%{value}%'), User.last_name.like('%{value}%')))"
        )
```

### **2. USER_EMAIL - Mapping Simple**

#### **Tous les Adaptateurs**
```java
// Mapping simple vers un seul champ
Path<String> emailPath = root.get("email");
return createPredicate(emailPath, operator, value, propertyRef.getType());
```

## 🚀 **Avantages de cette Approche**

### **1. Lambda comme Interface Unique**
- **Un seul paramètre** : `FilterDefinition` → `Condition`
- **Flexibilité maximale** : Chaque adaptateur interprète `PropertyRef` à sa manière
- **Type safety** : Compilation-time validation

### **2. Séparation Totale des Responsabilités**
- **PropertyRef** : Logique métier pure (type, opérateurs)
- **FilterDefinition** : Conteneur simple avec `PropertyRef`
- **Adaptateurs** : Responsables du mapping via lambda

### **3. Extensibilité Maximale**
- **Nouveaux adaptateurs** : Implémentent simplement la lambda
- **Logique complexe** : Chaque adaptateur peut implémenter sa propre logique
- **Pas de couplage** : Aucune dépendance entre adaptateurs

### **4. Performance Optimale**
- **Pas de lookup** : Mapping direct via lambda
- **Pas de registry** : Suppression d'une couche d'indirection
- **Compilation-time** : Optimisations du compilateur

## 📊 **Comparaison des Approches**

| Aspect | Avec PropertyRegistry | Avec Lambda |
|--------|----------------------|-------------|
| **Interface** | ❌ Multiple méthodes | ✅ Lambda unique |
| **Flexibilité** | ❌ Limité par registry | ✅ Maximale par adaptateur |
| **Performance** | ❌ Lookup dans registry | ✅ Appel direct |
| **Simplicité** | ❌ Couche d'indirection | ✅ Mapping direct |
| **Type Safety** | ❌ Runtime validation | ✅ Compilation-time |
| **Extensibilité** | ❌ Modifications du registry | ✅ Nouvelle lambda |

## 🎉 **Exemple d'Utilisation Finale**

```java
public class FilterService {
    private final Function<FilterDefinition, Condition> conditionFactory;
    
    public FilterService(AdapterType adapterType) {
        Context context = createContext(adapterType);
        this.conditionFactory = context.getConditionFactory();
    }
    
    public List<Entity> filterEntities(FilterRequest request) {
        List<Condition> conditions = new ArrayList<>();
        
        // Utilisation de la lambda pour chaque filtre
        for (FilterDefinition filter : request.getFilters().values()) {
            Condition condition = conditionFactory.apply(filter);
            conditions.add(condition);
        }
        
        // Combinaison des conditions
        return executeQuery(conditions, request.getCombineWith());
    }
    
    private Context createContext(AdapterType adapterType) {
        return switch (adapterType) {
            case JPA -> new JpaContextAdapter(criteriaBuilder, root);
            case PRISMA -> new PrismaContextAdapter();
            case SQLALCHEMY -> new SqlAlchemyContextAdapter();
        };
    }
}
```

## 🎯 **Conclusion**

Cette approche finale :

1. **Exige une lambda unique** : `FilterDefinition` → `Condition`
2. **Donne le contrôle total aux adaptateurs** pour interpréter `PropertyRef`
3. **Permet la flexibilité maximale** : un `PropertyRef` peut mapper vers multiple champs
4. **Simplifie l'architecture** avec une interface unique
5. **Améliore les performances** avec des appels directs

C'est exactement ce que vous vouliez : **chaque implémentation d'adaptateur attend une lambda avec son unique paramètre d'entrée de type FilterDefinition** ! 🎉
