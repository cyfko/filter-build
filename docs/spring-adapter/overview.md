---
layout: page
title: Spring Adapter Overview
description: Integrate FilterQL with Spring Data JPA for dynamic, type-safe queries
parent: Spring Integration Mastery
nav_order: 1
---

# FilterQL Spring Adapter Overview

The FilterQL Spring Adapter integrates the core filtering engine with Spring Data JPA, allowing you to build dynamic, type-safe queries using FilterQL's DSL and property references. All documentation and examples below are verified against the actual codebase.

## Architecture

  ```java
  FilterContext<User, UserPropertyRef> context = new FilterContext<>(User.class, UserPropertyRef.class, mappingFunction);
  ```

## Example: Integrating with Spring Data JPA

```java
import io.github.cyfko.filterql.adapter.spring.FilterContext;
import io.github.cyfko.filterql.core.model.FilterDefinition;
import io.github.cyfko.filterql.core.validation.Op;
import org.springframework.data.jpa.domain.Specification;
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

// Mapping function for property references
Function<FilterDefinition<UserPropertyRef>, Object> mappingFunction = def -> switch (def.ref()) {
    case NAME -> "name";
    case AGE -> "age";
};

// Create context
FilterContext<User, UserPropertyRef> context = new FilterContext<>(User.class, UserPropertyRef.class, mappingFunction);

// Add filter definitions
FilterDefinition<UserPropertyRef> nameFilter = new FilterDefinition<>(UserPropertyRef.NAME, Op.MATCHES, "John%") ;
context.addCondition("name", nameFilter);

// Build Specification for Spring Data JPA
Specification<User> spec = ((FilterCondition<User>) context.getCondition("name")).getSpecification();
```

## Spring Boot Integration Example

```java
@RestController
public class UserController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FilterContext<User, UserPropertyRef> filterContext;

    @PostMapping("/users/search")
    public Page<User> searchUsers(@RequestBody FilterRequest<UserPropertyRef> request, Pageable pageable) {
        // Add filter definitions to context
        request.filters().forEach(filterContext::addCondition);
        // Build combined condition
        Condition combined = filterContext.getCondition("name").and(filterContext.getCondition("age"));
        // Convert to Specification
        Specification<User> spec = ((FilterCondition<User>) combined).getSpecification();
        return userRepository.findAll(spec, pageable);
    }
}
```

## References
```
