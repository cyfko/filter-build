# Quick Start Guide

Get up and running with FilterQL in under 5 minutes! This guide will walk you through creating your first dynamic filter.

## What You'll Build

By the end of this guide, you'll have a working Spring Boot application that can filter users with a flexible, type-safe API.

```java
// This is what you'll achieve:
FilterRequest<UserPropertyRef> request = FilterRequest.builder()
    .filter("nameFilter", new FilterDefinition<>(UserPropertyRef.NAME, Op.MATCHES, "John%"))
    .filter("ageFilter", new FilterDefinition<>(UserPropertyRef.AGE, Op.GT, 25))
    .combineWith("nameFilter & ageFilter")
    .build();

Page<User> results = userService.search(request, pageable);
```

## Prerequisites

- Java 21+
- Maven or Gradle
- 10 minutes of your time

## Step 1: Create a Spring Boot Project

### Using Spring Initializr

1. Go to [start.spring.io](https://start.spring.io)
2. Configure your project:
   - **Project**: Maven or Gradle
   - **Language**: Java
   - **Spring Boot**: 3.3.4 or later
   - **Java**: 21
   - **Dependencies**: Spring Web, Spring Data JPA, H2 Database

3. Generate and download the project

### Or use this `pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.4</version>
        <relativePath/>
    </parent>
    
    <groupId>com.example</groupId>
    <artifactId>filterql-quickstart</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>
    
    <properties>
        <java.version>21</java.version>
    </properties>
    
    <dependencies>
        <!-- Spring Boot -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        
        <!-- FilterQL -->
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
        
        <!-- Database -->
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>
    </dependencies>
</project>
```

## Step 2: Create Your Entity

Create a simple `User` entity:

```java
package com.example.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false)
    private Integer age;
    
    @Enumerated(EnumType.STRING)
    private UserStatus status;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // Constructors
    public User() {}
    
    public User(String name, String email, Integer age, UserStatus status) {
        this.name = name;
        this.email = email;
        this.age = age;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    
    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

enum UserStatus {
    ACTIVE, INACTIVE, PENDING
}
```

## Step 3: Define Property References

Create an enum that defines which properties can be filtered:

```java
package com.example.config;

import io.github.cyfko.filterql.core.validation.Op;
import io.github.cyfko.filterql.core.validation.PropertyReference;
import io.github.cyfko.filterql.core.utils.OperatorUtils;
import com.example.model.UserStatus;

import java.time.LocalDateTime;
import java.util.Set;

public enum UserPropertyRef implements PropertyReference {
    NAME(String.class, OperatorUtils.FOR_TEXT),
    EMAIL(String.class, OperatorUtils.FOR_TEXT),
    AGE(Integer.class, OperatorUtils.FOR_NUMBER),
    STATUS(UserStatus.class, Set.of(Op.EQ, Op.NE, Op.IN, Op.NOT_IN, Op.IS_NULL, Op.NOT_NULL)),
    CREATED_AT(LocalDateTime.class, OperatorUtils.FOR_NUMBER);

    private final Class<?> type;
    private final Set<Op> supportedOperators;

    UserPropertyRef(Class<?> type, Set<Op> supportedOperators) {
        this.type = type;
        this.supportedOperators = Set.copyOf(supportedOperators);
    }

    @Override
    public Class<?> getType() {
        return type;
    }

    @Override
    public Set<Op> getSupportedOperators() {
        return supportedOperators;
    }
}
```

## Step 4: Configure FilterQL

Create a configuration class:

```java
package com.example.config;

import io.github.cyfko.filterql.adapter.spring.FilterContext;
import io.github.cyfko.filterql.core.model.FilterDefinition;
import com.example.model.User;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Function;

@Configuration
public class FilterConfig {
    
    @Bean
    public FilterContext<User, UserPropertyRef> userFilterContext() {
        return new FilterContext<>(
            User.class,
            UserPropertyRef.class,
            this::mapUserProperty
        );
    }
    
    private Object mapUserProperty(FilterDefinition<UserPropertyRef> definition) {
        return switch (definition.ref()) {
            case NAME -> "name";
            case EMAIL -> "email";
            case AGE -> "age";
            case STATUS -> "status";
            case CREATED_AT -> "createdAt";
        };
    }
}
```

## Step 5: Create Repository and Service

### Repository
```java
package com.example.repository;

import com.example.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
}
```

### Service
```java
package com.example.service;

import com.example.config.UserPropertyRef;
import com.example.model.User;
import com.example.repository.UserRepository;
import io.github.cyfko.filterql.adapter.spring.FilterContext;
import io.github.cyfko.filterql.core.Condition;
import io.github.cyfko.filterql.core.FilterResolver;
import io.github.cyfko.filterql.core.domain.PredicateResolver;
import io.github.cyfko.filterql.core.model.FilterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private FilterContext<User, UserPropertyRef> filterContext;
    
    public Page<User> search(FilterRequest<UserPropertyRef> request, Pageable pageable) {
        // Create filter resolver
        FilterResolver resolver = FilterResolver.of(filterContext);
        
        // Resolve filter request to executable predicate
        PredicateResolver<User> predicateResolver = resolver.resolve(User.class, request);
        
        // Convert to Spring Specification
        Specification<User> specification = (root, query, cb) -> 
            predicateResolver.resolve(root, query, cb);
        
        // Execute query with pagination
        return userRepository.findAll(specification, pageable);
    }
}
```

## Step 6: Create REST Controller

```java
package com.example.controller;

import com.example.config.UserPropertyRef;
import com.example.model.User;
import com.example.service.UserService;
import io.github.cyfko.filterql.core.model.FilterDefinition;
import io.github.cyfko.filterql.core.model.FilterRequest;
import io.github.cyfko.filterql.core.validation.Op;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/search")
    public Page<User> searchUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer minAge) {
        
        // Build dynamic filter request
        FilterRequest.Builder<UserPropertyRef> builder = FilterRequest.builder();
        
        if (name != null && !name.isEmpty()) {
            builder.filter("nameFilter", 
                new FilterDefinition<>(UserPropertyRef.NAME, Op.MATCHES, name + "%"));
        }
        
        if (minAge != null) {
            builder.filter("ageFilter", 
                new FilterDefinition<>(UserPropertyRef.AGE, Op.GTE, minAge));
        }
        
        // Combine filters
        String combination;
        if (name != null && minAge != null) {
            combination = "nameFilter & ageFilter";
        } else if (name != null) {
            combination = "nameFilter";
        } else if (minAge != null) {
            combination = "ageFilter";
        } else {
            // No filters - return all
            Pageable pageable = PageRequest.of(page, size);
            return userService.userRepository.findAll(pageable);
        }
        
        FilterRequest<UserPropertyRef> request = builder
            .combineWith(combination)
            .build();
        
        Pageable pageable = PageRequest.of(page, size);
        return userService.search(request, pageable);
    }
}
```

## Step 7: Add Sample Data

Create a data initialization class:

```java
package com.example.config;

import com.example.model.User;
import com.example.model.UserStatus;
import com.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            userRepository.save(new User("John Doe", "john@example.com", 28, UserStatus.ACTIVE));
            userRepository.save(new User("Jane Smith", "jane@example.com", 32, UserStatus.ACTIVE));
            userRepository.save(new User("Bob Johnson", "bob@example.com", 24, UserStatus.INACTIVE));
            userRepository.save(new User("Alice Brown", "alice@example.com", 29, UserStatus.PENDING));
            userRepository.save(new User("Charlie Wilson", "charlie@example.com", 35, UserStatus.ACTIVE));
        }
    }
}
```

## Step 8: Run and Test

### Start the Application
```bash
mvn spring-boot:run
```

### Test Your Filter API

Try these URLs in your browser or with curl:

```bash
# All users
http://localhost:8080/api/users/search

# Users with names starting with "J"
http://localhost:8080/api/users/search?name=J

# Users aged 25 or older
http://localhost:8080/api/users/search?minAge=25

# Users with names starting with "J" AND aged 25 or older
http://localhost:8080/api/users/search?name=J&minAge=25
```

## 🎉 Congratulations!

You've successfully created your first FilterQL application! You now have:

- ✅ A working Spring Boot application
- ✅ Dynamic, type-safe filtering
- ✅ Flexible filter combinations
- ✅ REST API endpoints

## What's Next?

Now that you have the basics working, explore more advanced features:

1. **[Build a Complete Application](first-application.md)** - More complex filtering scenarios
2. **[Learn Core Concepts](../core-module/overview.md)** - Understand FilterQL's architecture
3. **[Explore Spring Integration](../spring-adapter/overview.md)** - Advanced Spring features
4. **[See Real-World Examples](../core-module/examples/)** - Production-ready patterns

## Common Enhancements

### Add Complex Filters
```java
// Complex business logic filters
FilterRequest<UserPropertyRef> request = FilterRequest.builder()
    .filter("youngAdults", new FilterDefinition<>(UserPropertyRef.AGE, Op.RANGE, Arrays.asList(18, 30)))
    .filter("activeUsers", new FilterDefinition<>(UserPropertyRef.STATUS, Op.EQ, UserStatus.ACTIVE))
    .filter("recentUsers", new FilterDefinition<>(UserPropertyRef.CREATED_AT, Op.GT, LocalDateTime.now().minusDays(30)))
    .combineWith("(youngAdults | recentUsers) & activeUsers")
    .build();
```

### Add Custom Mappings
```java
// Custom search logic
private Object mapUserProperty(FilterDefinition<UserPropertyRef> definition) {
    return switch (definition.ref()) {
        case NAME -> "name";
        case EMAIL -> "email";
        case AGE -> "age";
        case STATUS -> "status";
        case CREATED_AT -> "createdAt";
        case FULL_TEXT_SEARCH -> new FullTextSearchMapping(definition); // Custom logic
    };
}
```

## Troubleshooting

### Build Issues
- Ensure Java 21+ is installed
- Check Maven/Gradle versions
- Verify dependency versions match

### Runtime Issues
- Check application.properties for database configuration
- Ensure H2 console is enabled for debugging
- Verify entity scanning is configured correctly

### Get Help
- [Troubleshooting Guide](../guides/troubleshooting.md)
- [GitHub Issues](https://github.com/cyfko/filter-build/issues)
- [Community Discussions](https://github.com/cyfko/filter-build/discussions)