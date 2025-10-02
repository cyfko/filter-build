---
layout: default
title: Getting Started
---

# Getting Started

This guide helps you create your first FilterQL application. All examples use only verified, compilable APIs.

## Prerequisites
- Java 21 or higher
- Maven or Gradle
- Spring Boot 3.x (optional)

## Installation
**Maven:**
```xml
<dependency>
    <groupId>io.github.cyfko</groupId>
    <artifactId>filterql-core</artifactId>
    <version>1.0.0</version>
</dependency>
<dependency>
    <groupId>io.github.cyfko</groupId>
    <artifactId>filterql-spring-adapter</artifactId>
    <version>1.0.0</version>
</dependency>
```
**Gradle:**
```gradle
implementation 'io.github.cyfko:filterql-core:1.0.0'
implementation 'io.github.cyfko:filterql-spring-adapter:1.0.0'
```

## Quick Start Example
```java
public enum UserPropertyRef implements PropertyReference {
    NAME(String.class, Set.of(Op.EQ, Op.MATCHES)),
    AGE(Integer.class, Set.of(Op.GT, Op.LT, Op.RANGE));
    private final Class<?> type;
    private final Set<Op> ops;
    UserPropertyRef(Class<?> type, Set<Op> ops) { this.type = type; this.ops = ops; }
    public Class<?> getType() { return type; }
    public Set<Op> getSupportedOperators() { return ops; }
}

FilterDefinition<UserPropertyRef> nameFilter = new FilterDefinition<>(UserPropertyRef.NAME, Op.MATCHES, "John%") ;
FilterRequest<UserPropertyRef> request = FilterRequest.<UserPropertyRef>builder()
    .filter("name", nameFilter)
    .combineWith("name")
    .build();
```

## Next Steps
- [Core Module](core-module.md)
- [Spring Adapter](spring-adapter.md)
- [Examples](examples.md)
