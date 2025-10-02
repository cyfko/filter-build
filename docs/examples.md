---
layout: default
title: Examples
---

# Examples

All examples below use only verified, compilable APIs from the FilterQL codebase.

## Basic Usage
```java
FilterDefinition<UserPropertyRef> nameFilter = new FilterDefinition<>(UserPropertyRef.NAME, Op.MATCHES, "John%") ;
FilterRequest<UserPropertyRef> request = FilterRequest.<UserPropertyRef>builder()
    .filter("name", nameFilter)
    .combineWith("name")
    .build();
```

## Advanced Patterns
```java
FilterDefinition<UserPropertyRef> ageFilter = new FilterDefinition<>(UserPropertyRef.AGE, Op.GT, 18);
FilterRequest<UserPropertyRef> request = FilterRequest.<UserPropertyRef>builder()
    .filter("name", nameFilter)
    .filter("age", ageFilter)
    .combineWith("name & age")
    .build();
```

## Spring Data JPA Integration
```java
Function<FilterDefinition<UserPropertyRef>, Object> mappingFunction = def -> switch (def.ref()) {
    case NAME -> "name";
    case AGE -> "age";
};
FilterContext<User, UserPropertyRef> context = new FilterContext<>(User.class, UserPropertyRef.class, mappingFunction);
context.addCondition("name", nameFilter);
Specification<User> spec = ((FilterCondition<User>) context.getCondition("name")).getSpecification();
```
