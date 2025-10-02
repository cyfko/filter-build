# FilterQL Core Module Overview

The FilterQL Core module provides a framework-agnostic filtering engine for building dynamic, type-safe, and composable filter logic in Java applications. All documentation and examples below are verified against the actual codebase.

## Architecture

- **FilterDefinition**: Represents a single filter condition. Verified constructor:
  ```java
  FilterDefinition<UserPropertyRef> def = new FilterDefinition<>(UserPropertyRef.NAME, Op.MATCHES, "John%")
  ```
- **FilterRequest**: Groups multiple filter definitions and a DSL expression for logical combination. Verified builder API:
  ```java
  FilterRequest<UserPropertyRef> request = FilterRequest.<UserPropertyRef>builder()
      .filter("name", def)
      .combineWith("name")
      .build();
  ```
- **Op**: Enum of supported operators (EQ, NE, GT, GTE, LT, LTE, MATCHES, NOT_MATCHES, IN, NOT_IN, IS_NULL, NOT_NULL, RANGE, NOT_RANGE)
- **PropertyReference**: Interface for type-safe property references. Implemented by user enums.
- **Context**: Registry and resolver for filter definitions. Verified API:
  ```java
  context.addCondition("name", def);
  Condition cond = context.getCondition("name");
  PredicateResolver<User> resolver = context.toResolver(User.class, cond);
  ```
- **FilterResolver**: High-level facade for parsing, validation, and predicate resolution. Verified API:
  ```java
  FilterResolver resolver = FilterResolver.of(context);
  PredicateResolver<User> pr = resolver.resolve(User.class, request);
  ```

## Example: Building a Filter

```java
import io.github.cyfko.filterql.core.model.FilterDefinition;
import io.github.cyfko.filterql.core.model.FilterRequest;
import io.github.cyfko.filterql.core.validation.Op;
import io.github.cyfko.filterql.core.validation.PropertyReference;
import io.github.cyfko.filterql.core.FilterResolver;
import io.github.cyfko.filterql.core.Context;
import io.github.cyfko.filterql.core.domain.PredicateResolver;
import java.util.Set;

public enum UserPropertyRef implements PropertyReference {
    NAME(String.class, Set.of(Op.EQ, Op.MATCHES)),
    AGE(Integer.class, Set.of(Op.GT, Op.LT, Op.RANGE));
    private final Class<?> type;
    private final Set<Op> ops;
    UserPropertyRef(Class<?> type, Set<Op> ops) { this.type = type; this.ops = ops; }
    public Class<?> getType() { return type; }
    public Set<Op> getSupportedOperators() { return ops; }
}

// Create filter definitions
FilterDefinition<UserPropertyRef> nameFilter = new FilterDefinition<>(UserPropertyRef.NAME, Op.MATCHES, "John%") ;
FilterDefinition<UserPropertyRef> ageFilter = new FilterDefinition<>(UserPropertyRef.AGE, Op.GT, 18);

// Build filter request
FilterRequest<UserPropertyRef> request = FilterRequest.<UserPropertyRef>builder()
    .filter("name", nameFilter)
    .filter("age", ageFilter)
    .combineWith("name & age")
    .build();

// Resolve filter
Context context = ...; // your Context implementation
context.addCondition("name", nameFilter);
context.addCondition("age", ageFilter);
Condition cond = context.getCondition("name").and(context.getCondition("age"));
PredicateResolver<User> resolver = context.toResolver(User.class, cond);

// Or use FilterResolver
FilterResolver fr = FilterResolver.of(context);
PredicateResolver<User> pr = fr.resolve(User.class, request);
```

## Thread Safety
- All core types are immutable or stateless.
- Context implementations should be used per request or made thread-safe for concurrent use.

## References
- [FilterDefinition](../../core/java/src/main/java/io/github/cyfko/filterql/core/model/FilterDefinition.java)
- [FilterRequest](../../core/java/src/main/java/io/github/cyfko/filterql/core/model/FilterRequest.java)
- [Op](../../core/java/src/main/java/io/github/cyfko/filterql/core/validation/Op.java)
- [PropertyReference](../../core/java/src/main/java/io/github/cyfko/filterql/core/validation/PropertyReference.java)
- [Context](../../core/java/src/main/java/io/github/cyfko/filterql/core/Context.java)
- [FilterResolver](../../core/java/src/main/java/io/github/cyfko/filterql/core/FilterResolver.java)
