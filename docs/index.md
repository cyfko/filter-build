---
layout: default
title: FilterQL - Dynamic Query Filtering Made Simple
description: A powerful Java library that transforms complex dynamic filtering into simple, readable DSL expressions
---

<div class="hero">
    <div class="container">
        <h1>FilterQL</h1>
        <p>Dynamic Query Filtering Made Simple</p>
        <p>Transform complex dynamic filtering into simple, readable DSL expressions. Built for modern Java applications with first-class support for JPA and Spring Data.</p>
        <div class="hero-buttons">
            <a href="{{ '/getting-started' | relative_url }}" class="btn btn-primary">Get Started</a>
            <a href="https://github.com/cyfko/filter-build" class="btn btn-secondary" target="_blank">View on GitHub</a>
        </div>
    </div>
</div>

<div class="features">
    <div class="feature">
        <div class="feature-icon">🔍</div>
        <h3>Intuitive DSL</h3>
        <p>Write filters like <code>name="John" AND (age>25 OR status="active")</code> instead of complex QueryBuilder chains.</p>
    </div>
    <div class="feature">
        <div class="feature-icon">🌐</div>
        <h3>Multi-framework</h3>
        <p>Native support for JPA Criteria API and Spring Data JPA Specifications with the same simple interface.</p>
    </div>
    <div class="feature">
        <div class="feature-icon">⚡</div>
        <h3>Type-safe</h3>
        <p>Zero runtime errors with compile-time validation and strong typing throughout the entire filtering pipeline.</p>
    </div>
    <div class="feature">
        <div class="feature-icon">🧩</div>
        <h3>Extensible</h3>
        <p>Easy to adapt for custom requirements with pluggable operators and framework adapters.</p>
    </div>
    <div class="feature">
        <div class="feature-icon">☕</div>
        <h3>Modern Java</h3>
        <p>Built for Java 21+ with latest best practices, zero dependencies in core, and full test coverage.</p>
    </div>
    <div class="feature">
        <div class="feature-icon">📚</div>
        <h3>Well Documented</h3>
        <p>Comprehensive documentation with examples, guides, and API reference to get you started quickly.</p>
    </div>
</div>

[![Maven Central](https://img.shields.io/maven-central/v/io.github.cyfko/filterql-core.svg)](https://central.sonatype.com/artifact/io.github.cyfko/filterql-core)
[![Java](https://img.shields.io/badge/Java-21+-orange.svg)](https://openjdk.java.net/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

## 🚀 Quick Start

### Installation

Add FilterQL to your project:

```xml
<!-- Core library -->
<dependency>
    <groupId>io.github.cyfko</groupId>
    <artifactId>filterql-core</artifactId>
    <version>1.2.0</version>
</dependency>

<!-- For JPA support -->
<dependency>
    <groupId>io.github.cyfko</groupId>
    <artifactId>filterql-adapter-jpa</artifactId>
    <version>1.0.1</version>
</dependency>

<!-- For Spring Data support -->
<dependency>
    <groupId>io.github.cyfko</groupId>
    <artifactId>filterql-spring</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Basic Usage

```java
// Define your filterable properties
public enum UserProperty implements PropertyRef, PathShape {
    NAME("name"),
    EMAIL("email"),
    AGE("age"),
    DEPARTMENT_NAME("department.name");
    
    private final String path;
    
    UserProperty(String path) {
        this.path = path;
    }
    
    @Override
    public String getPath() {
        return path;
    }
}

// Create filter request
FilterRequest<UserProperty> request = FilterRequest.<UserProperty>builder()
    .filter("name-filter", UserProperty.NAME, Operator.EQUALS, "John")
    .filter("age-filter", UserProperty.AGE, Operator.GREATER_THAN, 25)
    .combineWith("name-filter AND age-filter")
    .build();

// Spring Data approach
Specification<User> spec = SpecificationBuilder.toSpecification(request);
List<User> users = userRepository.findAll(spec);

// JPA EntityManager approach  
BasicFilterExecutor<User, UserProperty> executor = 
    new BasicFilterExecutor<>(entityManager, User.class);
List<User> users = executor.executeFilter(request);
```

## 🎯 Key Features

- **🔍 Intuitive DSL**: Write filters like `name="John" AND (age>25 OR status="active")`
- **🌐 Multi-framework**: Native support for JPA and Spring Data
- **⚡ Type-safe**: Zero runtime errors with compile-time validation
- **🧩 Extensible**: Easy to adapt for custom requirements
- **☕ Modern Java**: Built for Java 21+ with latest best practices

## 📚 Documentation

- [Getting Started](getting-started.md)
- [Core Concepts](core-concepts.md)
- [JPA Adapter Guide](jpa-adapter.md)
- [Spring Data Guide](spring-adapter.md)
- [Advanced Usage](advanced-usage.md)
- [API Reference](api-reference.md)

## 🛠️ Examples

### Complex Filtering
```java
FilterRequest<UserProperty> complexFilter = FilterRequest.<UserProperty>builder()
    .filter("senior", UserProperty.AGE, Operator.GREATER_THAN, 30)
    .filter("engineering", UserProperty.DEPARTMENT_NAME, Operator.EQUALS, "Engineering")
    .filter("sales", UserProperty.DEPARTMENT_NAME, Operator.EQUALS, "Sales")
    .filter("active", UserProperty.STATUS, Operator.EQUALS, "ACTIVE")
    .combineWith("active AND (senior OR (engineering OR sales))")
    .build();
```

### Dynamic API Filtering
```java
@RestController
public class UserController {
    
    @GetMapping("/users")
    public List<User> getUsers(
        @RequestParam(required = false) String name,
        @RequestParam(required = false) Integer minAge,
        @RequestParam(required = false) String department) {
        
        FilterRequest.Builder<UserProperty> builder = FilterRequest.builder();
        List<String> conditions = new ArrayList<>();
        
        if (name != null) {
            builder.filter("name", UserProperty.NAME, Operator.CONTAINS, name);
            conditions.add("name");
        }
        
        if (minAge != null) {
            builder.filter("age", UserProperty.AGE, Operator.GREATER_THAN_OR_EQUAL, minAge);
            conditions.add("age");
        }
        
        if (department != null) {
            builder.filter("dept", UserProperty.DEPARTMENT_NAME, Operator.EQUALS, department);
            conditions.add("dept");
        }
        
        if (!conditions.isEmpty()) {
            FilterRequest<UserProperty> request = builder
                .combineWith(String.join(" AND ", conditions))
                .build();
            
            Specification<User> spec = SpecificationBuilder.toSpecification(request);
            return userRepository.findAll(spec);
        }
        
        return userRepository.findAll();
    }
}
```

## 🏗️ Architecture

FilterQL follows a clean, modular architecture:

- **Core**: Framework-agnostic filtering logic
- **Adapters**: Framework-specific implementations (JPA, Spring)
- **DSL Parser**: Intelligent expression parsing and validation

## 🤝 Contributing

We welcome contributions! Please see our [Contributing Guide](CONTRIBUTING.md) for details.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

Built with ❤️ for the Java community. Special thanks to all contributors and the open-source ecosystem that makes projects like this possible.

---

**Made with Java 21 • Spring Boot • JPA**