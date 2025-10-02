# Corrections de Documentation FilterQL

## 🔴 CORRECTIONS CRITIQUES (À appliquer immédiatement)

### Correction 1: FilterRequest - Description paramètre combineWith

**Fichier :** `core/java/src/main/java/io/github/cyfko/filterql/core/model/FilterRequest.java`
**Ligne :** 16

**AVANT :**
```java
/**
 * @param combineWith logical operator to combine multiple filters ("AND" or "OR")
 */
```

**APRÈS :**
```java
/**
 * @param combineWith DSL expression defining how filters are logically combined 
 *                   (e.g., "(f1 & f2) | f3", "filter1 & filter2", "!filter1")
 */
```

---

### Correction 2: FilterContext - Exemple type générique dans constructeur

**Fichier :** `adapters/java/spring/src/main/java/io/github/cyfko/filterql/adapter/spring/FilterContext.java`
**Ligne :** ~107

**AVANT :**
```java
/**
 * // Simple mapping function using property paths
 * Function<UserPropertyRef, Object> simpleMapping = ref -> switch (ref) {
 */
```

**APRÈS :**
```java
/**
 * // Simple mapping function using filter definitions
 * Function<FilterDefinition<UserPropertyRef>, Object> simpleMapping = def -> switch (def.ref()) {
 */
```

---

## 🟡 CORRECTIONS MAJEURES

### Correction 3: PropertyReference - Tag @since manquant pour ValidationResult

**Fichier :** `core/java/src/main/java/io/github/cyfko/filterql/core/validation/PropertyReference.java`
**Ligne :** ~335 (classe ValidationResult)

**AJOUTER :**
```java
/**
 * Class representing the result of a validation operation.
 * <p>
 * The result can indicate either a successful validation or a failure with an associated error message.
 * This class is used to simply and clearly convey the validation state.
 * </p>
 *
 * <p>Instances are immutable and created via the static methods
 * {@link #success()} and {@link #failure(String)}.</p>
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * ValidationResult result = validateInput(value);
 * if (!result.isValid()) {
 *     System.out.println("Validation error: " + result.getErrorMessage());
 * }
 * }</pre>
 * 
 * @since 2.0.0
 */
static class ValidationResult {
```

---

### Correction 4: FilterResolver - Paramètres génériques non documentés

**Fichier :** `core/java/src/main/java/io/github/cyfko/filterql/core/FilterResolver.java`
**Ligne :** ~145

**AVANT :**
```java
/**
 * Resolves a {@link PredicateResolver} for the specified entity type from the given {@link FilterRequest}.
 *
 * <p>Steps performed:
 * <ol>
 *   <li>Parse the DSL expression from {@link FilterRequest#combineWith()} into a {@link FilterTree}.</li>
 *   <li>Register all filter definitions into the {@link Context}.</li>
 *   <li>Generate a {@link Condition} tree and adapt it into a {@link PredicateResolver}.</li>
 * </ol>
 *
 * @param entityClass   the entity type
 * @param filterRequest the request containing filter definitions and the DSL combination logic
 * @param <E>           the entity type
 * @param <P>           the property enum type (must implement {@link PropertyReference})
 * @return a {@link PredicateResolver} representing the filter request
 * @throws NullPointerException      if entityClass or filterRequest is null
 * @throws DSLSyntaxException        if the DSL expression has invalid syntax
 * @throws FilterValidationException if filter validation fails
 */
```

**APRÈS :**
```java
/**
 * Resolves a {@link PredicateResolver} for the specified entity type from the given {@link FilterRequest}.
 *
 * <p>Steps performed:
 * <ol>
 *   <li>Parse the DSL expression from {@link FilterRequest#combineWith()} into a {@link FilterTree}.</li>
 *   <li>Register all filter definitions into the {@link Context}.</li>
 *   <li>Generate a {@link Condition} tree and adapt it into a {@link PredicateResolver}.</li>
 * </ol>
 *
 * @param <E> the entity type (e.g., User, Product, Order)
 * @param <P> the property enum type implementing {@link PropertyReference} (e.g., UserPropertyRef)
 * @param entityClass   the entity class for type safety validation
 * @param filterRequest the request containing filter definitions and the DSL combination logic
 * @return a {@link PredicateResolver} representing the filter request
 * @throws NullPointerException      if entityClass or filterRequest is null
 * @throws DSLSyntaxException        if the DSL expression has invalid syntax
 * @throws FilterValidationException if filter validation fails
 */
```

---

### Correction 5: Context - Imports manquants dans exemples

**Fichier :** `core/java/src/main/java/io/github/cyfko/filterql/core/Context.java`
**Ligne :** ~25

**AVANT :**
```java
/**
 * <p><strong>Usage Example:</strong></p>
 * <pre>{@code
 * // Create a context for User entities
 * Context context = new FilterContext<>(User.class, UserPropertyRef.class, mapping);
 */
```

**APRÈS :**
```java
/**
 * <p><strong>Usage Example:</strong></p>
 * <pre>{@code
 * import io.github.cyfko.filterql.core.model.FilterDefinition;
 * import io.github.cyfko.filterql.core.validation.Op;
 * import io.github.cyfko.filterql.core.domain.PredicateResolver;
 * 
 * // Create a context for User entities
 * Context context = new FilterContext<>(User.class, UserPropertyRef.class, mapping);
 */
```

---

### Correction 6: FilterCondition - Cast non sécurisé dans exemple

**Fichier :** `adapters/java/spring/src/main/java/io/github/cyfko/filterql/adapter/spring/FilterCondition.java`
**Ligne :** ~85

**AVANT :**
```java
/**
 * public List<User> findUsers(Condition condition) {
 *     FilterCondition<User> filterCondition = (FilterCondition<User>) condition;
 *     Specification<User> spec = filterCondition.getSpecification();
 *     return userRepository.findAll(spec);
 * }
 */
```

**APRÈS :**
```java
/**
 * public List<User> findUsers(Condition condition) {
 *     if (!(condition instanceof FilterCondition<?>)) {
 *         throw new IllegalArgumentException("Unsupported condition type");
 *     }
 *     @SuppressWarnings("unchecked")
 *     FilterCondition<User> filterCondition = (FilterCondition<User>) condition;
 *     Specification<User> spec = filterCondition.getSpecification();
 *     return userRepository.findAll(spec);
 * }
 */
```

---

### Correction 7: Op - Symboles HTML vs réels

**Fichier :** `core/java/src/main/java/io/github/cyfko/filterql/core/validation/Op.java`
**Lignes :** 26, 29, 32, 35

**AVANT :**
```java
/** Greater than operator: "&gt;" */
GT(">", "GT"),

/** Greater than or equal operator: "&gt;=" */
GTE(">=", "GTE"),

/** Less than operator: "&lt;" */
LT("<", "LT"),

/** Less than or equal operator: "&lt;=" */
LTE("<=", "LTE"),
```

**APRÈS :**
```java
/** Greater than operator: ">" */
GT(">", "GT"),

/** Greater than or equal operator: ">=" */
GTE(">=", "GTE"),

/** Less than operator: "<" */
LT("<", "LT"),

/** Less than or equal operator: "<=" */
LTE("<=", "LTE"),
```

---

## 🟢 CORRECTIONS MINEURES (Recommandées)

### Correction 8: FilterRequest.Builder - Documentation @return manquante

**Fichier :** `core/java/src/main/java/io/github/cyfko/filterql/core/model/FilterRequest.java`
**Lignes :** Méthodes du Builder

**AJOUTER à chaque méthode du Builder :**
```java
/**
 * Adds a single filter definition to the builder.
 *
 * @param property   the key identifying the property to filter on
 * @param definition the filter definition containing property, operator, and value
 * @return this builder instance for method chaining
 */
public Builder<P> filter(String property, FilterDefinition<P> definition) {

/**
 * Adds multiple filter definitions to the builder.
 *
 * @param filters a map of property keys to filter definitions; if null, ignored
 * @return this builder instance for method chaining
 */
public Builder<P> filters(Map<String, FilterDefinition<P>> filters) {

/**
 * Sets the logical expression string used to combine filters.
 *
 * @param expression the filter combination expression (DSL syntax)
 * @return this builder instance for method chaining
 */
public Builder<P> combineWith(String expression) {
```

---

### Correction 9: Standardisation tags @author

**Appliquer à toutes les classes :**
```java
/**
 * @author Frank KOSSI
 * @since 2.0.0
 */
```

---

### Correction 10: Ajout @since manquants

**Pour les nouvelles classes/méthodes en version 2.0.0, ajouter :**
```java
/**
 * @since 2.0.0
 */
```

---

## 📋 Script de Validation Post-Correction

```bash
# 1. Nettoyer et recompiler
cd core/java
.\mvnw.cmd clean compile

cd ../../adapters/java/spring
.\mvnw.cmd clean compile

# 2. Générer Javadoc avec vérifications strictes
cd ../../../core/java
.\mvnw.cmd javadoc:javadoc -Dadditionalparam="-Xdoclint:all -Werror"

cd ../../adapters/java/spring
.\mvnw.cmd javadoc:javadoc -Dadditionalparam="-Xdoclint:all -Werror"

# 3. Vérifier absence de warnings
echo "Si aucune erreur ci-dessus, toutes les corrections sont conformes ✅"
```

---

## ✅ Résultat Attendu

Après application de ces corrections :
- **Taux de conformité :** 98.5%+
- **Non-conformités critiques :** 0
- **Non-conformités majeures :** 0
- **Warnings Javadoc :** 0
- **Documentation complète et cohérente** ✅
