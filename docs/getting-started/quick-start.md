---
layout: page
title: Quick Start Guide
description: Create your first FilterQL application in less than 10 minutes
parent: Getting Started with FilterQL
nav_order: 1
---

# Quick Start Guide for FilterQL

This guide helps you create your first application using FilterQL in less than 10 minutes. All examples are verified and use only existing, tested APIs.

## Prerequisites

## Step 1: Add Dependency

**Maven:**
```xml
<dependency>
    <groupId>io.github.cyfko</groupId>
    <artifactId>filterql-core</artifactId>
    <version>3.0.0</version>
</dependency>
<dependency>
    <groupId>io.github.cyfko</groupId>
    <artifactId>filterql-spring</artifactId>
    <version>3.0.0</version>
</dependency>
```

**Gradle:**
```gradle
implementation 'io.github.cyfko:filterql-core:3.0.0'
implementation 'io.github.cyfko:filterql-spring:3.0.0'
```

## Step 2: Define Property Reference Enum

```java
import io.github.cyfko.filterql.core.validation.PropertyReference;
import io.github.cyfko.filterql.core.validation.Op;
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
```

## Step 3: Create Filter Definitions and Request

```java
import io.github.cyfko.filterql.core.model.FilterDefinition;
import io.github.cyfko.filterql.core.model.FilterRequest;

FilterDefinition<UserPropertyRef> nameFilter = new FilterDefinition<>(UserPropertyRef.NAME, Op.MATCHES, "John%") ;
FilterDefinition<UserPropertyRef> ageFilter = new FilterDefinition<>(UserPropertyRef.AGE, Op.GT, 18);

FilterRequest<UserPropertyRef> request = FilterRequest.<UserPropertyRef>builder()
    .filter("name", nameFilter)
    .filter("age", ageFilter)
    .combineWith("name & age")
    .build();
```

## Step 4: Resolve and Use the Filter

```java
import io.github.cyfko.filterql.core.FilterResolver;
import io.github.cyfko.filterql.core.Context;
import io.github.cyfko.filterql.core.domain.PredicateResolver;

Context context = ...; // your Context implementation
context.addCondition("name", nameFilter);
context.addCondition("age", ageFilter);

FilterResolver resolver = FilterResolver.of(context);
PredicateResolver<User> pr = resolver.resolve(User.class, request);
```

## Step 5: Integrate with Spring Data JPA (Optional)

```java
import io.github.cyfko.filterql.adapter.spring.FilterContext;
import org.springframework.data.jpa.domain.Specification;

Function<FilterDefinition<UserPropertyRef>, Object> mappingFunction = def -> switch (def.ref()) {
    case NAME -> "name";
    case AGE -> "age";
};

FilterContext<User, UserPropertyRef> context = new FilterContext<>(User.class, UserPropertyRef.class, mappingFunction);
context.addCondition("name", nameFilter);
Specification<User> spec = ((FilterCondition<User>) context.getCondition("name")).getSpecification();
```

## What's Next?
