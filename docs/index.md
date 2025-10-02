---
layout: default
title: FilterQL Documentation
---

# FilterQL — Dynamic Filter Builder

[![Maven Central](https://img.shields.io/maven-central/v/io.github.cyfko/filterql-core.svg)](https://central.sonatype.com/namespace/io.github.cyfko)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-21+-orange.svg)](https://openjdk.java.net/)

---

## What is FilterQL?

FilterQL is a framework-agnostic Java library for building dynamic, type-safe, and secure filter logic. It provides a DSL for complex filtering, integrates with Spring Data JPA, and ensures 100% code fidelity in all documentation and examples.

## Documentation

- [Getting Started](getting-started.md)
- [Core Module](core-module.md)
- [Spring Adapter](spring-adapter.md)
- [Examples](examples.md)
- [FAQ](faq.md)
- [Troubleshooting](troubleshooting.md)
- [API Reference (Javadoc)](api/javadoc/index.html)

## Quick Start

```xml
<!-- Maven -->
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

```gradle
// Gradle
implementation 'io.github.cyfko:filterql-core:1.0.0'
implementation 'io.github.cyfko:filterql-spring-adapter:1.0.0'
```

## Example

```java
import io.github.cyfko.filterql.core.model.FilterDefinition;
import io.github.cyfko.filterql.core.model.FilterRequest;
import io.github.cyfko.filterql.core.validation.Op;

FilterDefinition<UserPropertyRef> nameFilter = new FilterDefinition<>(UserPropertyRef.NAME, Op.MATCHES, "John%") ;
FilterRequest<UserPropertyRef> request = FilterRequest.<UserPropertyRef>builder()
    .filter("name", nameFilter)
    .combineWith("name")
    .build();
```

## Project Status
- **Current Version**: 1.0.0
- **Java Compatibility**: Java 21+
- **Spring Boot Compatibility**: 3.x
- **License**: MIT
