---
layout: default
title: Core Module
---

# Core Module

The FilterQL Core module provides the filtering engine for dynamic, type-safe, and composable filter logic. All documentation and examples are verified against the actual codebase.

## Architecture
- **FilterDefinition**: Record constructor, no builder pattern.
- **FilterRequest**: Builder API for combining filters.
- **Op**: Enum of supported operators.
- **PropertyReference**: Interface for type-safe property references.
- **Context**: Registry and resolver for filter definitions.
- **FilterResolver**: Facade for parsing, validation, and predicate resolution.

## Example
```java
FilterDefinition<UserPropertyRef> nameFilter = new FilterDefinition<>(UserPropertyRef.NAME, Op.MATCHES, "John%") ;
FilterRequest<UserPropertyRef> request = FilterRequest.<UserPropertyRef>builder()
    .filter("name", nameFilter)
    .combineWith("name")
    .build();
```

## API Reference
- [FilterDefinition](core/java/src/main/java/io/github/cyfko/filterql/core/model/FilterDefinition.java)
- [FilterRequest](core/java/src/main/java/io/github/cyfko/filterql/core/model/FilterRequest.java)
- [Op](core/java/src/main/java/io/github/cyfko/filterql/core/validation/Op.java)
- [PropertyReference](core/java/src/main/java/io/github/cyfko/filterql/core/validation/PropertyReference.java)
- [Context](core/java/src/main/java/io/github/cyfko/filterql/core/Context.java)
- [FilterResolver](core/java/src/main/java/io/github/cyfko/filterql/core/FilterResolver.java)
