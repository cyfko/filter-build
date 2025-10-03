---
layout: default
title: FilterQL - Transform Filtering Forever
---

# � FilterQL: Transform Filtering Forever

**Filtering in Java, reimagined for clarity, safety, and power**

![Maven Central](https://img.shields.io/maven-central/v/io.github.cyfko/filterql-core.svg)
![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)
![Java](https://img.shields.io/badge/Java-21+-orange.svg)

---

## Why Does FilterQL Exist?

**Every developer has faced the filtering nightmare:**
- Endless query parameters
- Unmaintainable code
- Brittle logic, bugs, and security risks
- No type safety, no flexibility

**FilterQL was born to solve this.**

## What Is FilterQL?

FilterQL is an **advanced dynamic filtering protocol** that bridges the gap between frontend and backend. The Java implementation provides type-safe, composable filters that work seamlessly across your entire stack:

**🌐 For Frontend Developers:**
- Send intuitive JSON filter requests to any endpoint
- Express complex queries without learning SQL or backend specifics
- Get predictable, validated responses

**⚙️ For Backend Developers:**
- Receive strongly-typed filter objects with guaranteed validation
- Transform filters to any data access framework (Spring Data JPA, etc.)
- Eliminate parameter explosion and brittle query building

**Key Capabilities:**
- Express complex filter logic as simple, readable objects
- Guarantee type safety and operator validation at compile time
- Protect against SQL injection and runtime errors
- Scale from simple apps to enterprise systems

## Who Is FilterQL For?

**🌐 Frontend Teams:**
- React, Vue, Angular developers building search interfaces
- Mobile developers creating filter-heavy apps
- UX designers wanting intuitive filtering experiences

**⚙️ Backend Teams:**
- Java developers tired of parameter explosion
- Spring Data JPA users needing dynamic queries
- API architects building search, analytics, or admin endpoints

**🏢 Organizations:**
- Teams wanting consistent filtering across projects
- Architects demanding maintainable, testable, secure code
- Companies building data-heavy applications

## The Problem: Filtering Without FilterQL

```java
// The old way: brittle, error-prone, and hard to extend
public List<User> findUsers(String name, String department, Boolean active, 
                           Integer minAge, Integer maxAge) {
    // 100+ lines of conditional logic, null checks, and manual query building
}
```

- Adding a new filter means changing method signatures everywhere
- Impossible to test all combinations
- Easy to introduce bugs and security holes

## The FilterQL Solution: Real Code, Real Simplicity

### 🔄 **The Complete Client-Server Flow**

**1. 🌐 Frontend sends intuitive filter requests:**

```javascript
// React/Vue/Angular - Natural filtering interface
const filterRequest = {
  "filters": {
    "activeUsers": { "ref": "STATUS", "operator": "EQ", "value": "ACTIVE" },
    "techTeam": { "ref": "DEPARTMENT", "operator": "EQ", "value": "ENGINEERING" },
    "experienced": { "ref": "YEARS_EXPERIENCE", "operator": "GTE", "value": 3 }
  },
  "combineWith": "activeUsers & (techTeam | experienced)"
};

// Send to backend
fetch('/api/users/search', {
  method: 'POST',
  body: JSON.stringify(filterRequest)
});
```

**2. ⚙️ Backend receives type-safe filter objects:**
```java
// VERIFIED: This is actual FilterQL usage in Java
@RestController
public class UserController {
    @PostMapping("/users/search")
    public Page<User> search(@RequestBody FilterRequest<UserPropertyRef> request, Pageable pageable) {
        // FilterQL automatically validates the request against your property definitions
        FilterResolver resolver = FilterResolver.of(springContext);
        PredicateResolver<User> predicateResolver = resolver.resolve(User.class, request);
        Specification<User> spec = predicateResolver.toSpecification();
        return userRepository.findAll(spec, pageable);
    }
}
```

**3. 🎯 Define your domain once, use everywhere:**
```java
public enum UserPropertyRef implements PropertyReference {
    STATUS(UserStatus.class, Set.of(Op.EQ, Op.NE, Op.IN, Op.NOT_IN)),
    DEPARTMENT(String.class, Set.of(Op.EQ, Op.MATCHES, Op.IN)),
    YEARS_EXPERIENCE(Integer.class, Set.of(Op.GT, Op.LT, Op.RANGE)),
    FULL_NAME(String.class, Set.of(Op.MATCHES));
    // Frontend gets automatic validation, Backend gets type safety
}
```

**4. 💾 Execute with any data access framework:**
```java
// Spring Data JPA (included)
Specification<User> spec = predicateResolver.toSpecification();
Page<User> results = userRepository.findAll(spec, pageable);

// Future: Other frameworks
// Criteria criteria = predicateResolver.toCriteria();
// String sql = predicateResolver.toSql();
```

## What Makes FilterQL Different?

### 🌍 **Universal Protocol, Not Just a Library**
- **Protocol-first design**: FilterQL defines a standard way to express filters
- **Language agnostic**: Today Java, tomorrow Python, TypeScript, etc.
- **Framework neutral**: Works with Spring Data JPA, Hibernate, MyBatis, etc.

### 🔄 **Perfect Client-Server Harmony**
- **Frontend**: Intuitive JSON requests, no backend knowledge needed
- **Backend**: Type-safe objects, no parameter explosion
- **Validation**: Automatic on both sides, consistent everywhere

### 🛡️ **Enterprise-Grade Safety**
- **Type safety**: Only valid properties and operators allowed
- **SQL injection proof**: No manual query building ever
- **Runtime validation**: Catch errors before they hit the database
- **Scalability**: Used in production for millions of records

## See FilterQL in Action

**Explore the docs:**
- [Getting Started Guide](getting-started.md)
- [Core Architecture](core-module.md)
- [Spring Integration](spring-adapter.md)
- [Real-World Examples](examples.md)
- [FAQ](faq.md)
- [Troubleshooting](troubleshooting.md)

## Quick Install

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

## Ready to Transform Filtering?

<div align="center">
  <p><strong>🚀 <a href="getting-started.md">Start Your 10-Minute Journey</a></strong></p>
  <p>From novice to productive in under 10 minutes</p>
  <p>or</p>
  <p><strong>🏗️ <a href="spring-adapter.md">Spring Integration Guide</a></strong></p>
  <p>Integrate FilterQL with your Spring Data JPA app</p>
  <p>or</p>
  <p><strong>📚 <a href="examples.md">Explore Real-World Examples</a></strong></p>
</div>

---

<div align="center">
  <strong>FilterQL: Filtering, Reimagined for Java</strong><br>
  <em>Made for developers who demand clarity, safety, and power</em>
  <br><br>
  <a href="https://github.com/cyfko/filter-build">GitHub</a> • 
  <a href="https://cyfko.github.io/filter-build">Documentation</a> • 
  <a href="https://github.com/cyfko/filter-build/issues">Issues</a>
</div>
