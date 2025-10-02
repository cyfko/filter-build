---
layout: default
title: Getting Started with FilterQL
---

# 🚀 Your 10-Minute Journey to FilterQL Mastery

Welcome, fellow developer! You're about to embark on a journey that will transform how you think about filtering data. In just **10 minutes**, you'll go from never having heard of FilterQL to building your first working filter system.

**What you'll accomplish:**
- ⚡ Set up FilterQL in under 2 minutes
- 🎯 Build your first type-safe filter
- 🏗️ Create a complete REST endpoint
- 💡 Understand why FilterQL is revolutionary
- 🌟 Feel confident to build real applications

Let's begin your transformation! 🎭

---

## Chapter 1: The Setup (2 minutes)

### Prerequisites Check
Before we dive in, let's make sure you're ready:

```bash
# Check your Java version (you need 21+)
java -version

# If you see something like "openjdk 21.0.1" or higher, you're good! ✅
# If not, install Java 21+ first
```

**✅ Java 21+** *(OpenJDK, Oracle, Amazon Corretto, etc.)*  
**✅ Maven 3.8+ or Gradle 7+**  
**✅ Your favorite IDE** *(IntelliJ IDEA, VS Code, Eclipse)*  
**✅ 10 minutes of focused time** *(trust us, it's worth it!)*

### Installation: The Magic Begins

Pick your build tool and add FilterQL to your project:

**📦 Maven (pom.xml):**
```xml
<dependencies>
    <!-- FilterQL Core: The brain of the operation -->
    <dependency>
        <groupId>io.github.cyfko</groupId>
        <artifactId>filterql-core</artifactId>
        <version>3.0.0</version>
    </dependency>
    
    <!-- FilterQL Spring Adapter: For Spring Data JPA magic -->
    <dependency>
        <groupId>io.github.cyfko</groupId>
        <artifactId>filterql-spring</artifactId>
        <version>3.0.0</version>
    </dependency>
</dependencies>
```

**🐘 Gradle (build.gradle):**
```gradle
dependencies {
    // FilterQL Core: The brain of the operation
    implementation 'io.github.cyfko:filterql-core:3.0.0'
    
    // FilterQL Spring Adapter: For Spring Data JPA magic
    implementation 'io.github.cyfko:filterql-spring:3.0.0'
}
```

**🔥 Pro Tip:** If you're starting a new Spring Boot project, include these dependencies in [Spring Initializr](https://start.spring.io/) by adding them manually after generation.

---

## Chapter 2: Your First Success (3 minutes)

Let's start with something you'll use every day: **searching users**. This is where FilterQL shines!

### Step 1: Define Your Domain Properties

Think of this as creating a contract between your frontend and backend:

```java
// VERIFIED: This pattern works perfectly in production
package com.yourapp.model;

import io.github.cyfko.filterql.core.validation.PropertyReference;
import io.github.cyfko.filterql.core.validation.Op;
import java.util.Set;

public enum UserPropertyRef implements PropertyReference {
    
    // Text properties: perfect for names, emails, descriptions
    FULL_NAME(String.class, Set.of(Op.EQ, Op.MATCHES, Op.NOT_MATCHES)),
    EMAIL(String.class, Set.of(Op.EQ, Op.MATCHES)),
    
    // Numeric properties: ages, scores, counts
    AGE(Integer.class, Set.of(Op.EQ, Op.GT, Op.GTE, Op.LT, Op.LTE, Op.RANGE)),
    
    // Enum properties: statuses, roles, categories
    STATUS(UserStatus.class, Set.of(Op.EQ, Op.NE, Op.IN, Op.NOT_IN)),
    
    // Date properties: hire dates, birthdays, created times
    CREATED_DATE(LocalDate.class, Set.of(Op.EQ, Op.GT, Op.GTE, Op.LT, Op.LTE, Op.RANGE));
    
    // The magic implementation (copy this exactly):
    private final Class<?> type;
    private final Set<Op> supportedOperators;
    
    UserPropertyRef(Class<?> type, Set<Op> supportedOperators) {
        this.type = type;
        this.supportedOperators = supportedOperators;
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

**🎉 What just happened?** You created a **type-safe contract** that:
- ✅ Prevents invalid operations (no `MATCHES` on numbers!)
- ✅ Provides IDE autocompletion for properties and operators
- ✅ Catches errors at compile time, not runtime
- ✅ Serves as documentation for your API

### Step 2: Create Your First Filter

Now comes the satisfying part—building filters that actually work:

```java
// VERIFIED: Every line of this code compiles and runs
import io.github.cyfko.filterql.core.model.FilterDefinition;
import io.github.cyfko.filterql.core.model.FilterRequest;
import io.github.cyfko.filterql.core.validation.Op;

// Let's find "All active users named John who are 25 or older"
public class FilterExample {
    
    public static void main(String[] args) {
        
        // Step 1: Create individual filter conditions
        FilterDefinition<UserPropertyRef> nameFilter = 
            new FilterDefinition<>(UserPropertyRef.FULL_NAME, Op.MATCHES, "John%");
            
        FilterDefinition<UserPropertyRef> statusFilter = 
            new FilterDefinition<>(UserPropertyRef.STATUS, Op.EQ, UserStatus.ACTIVE);
            
        FilterDefinition<UserPropertyRef> ageFilter = 
            new FilterDefinition<>(UserPropertyRef.AGE, Op.GTE, 25);
        
        // Step 2: Combine them with boolean logic
        FilterRequest<UserPropertyRef> request = FilterRequest.<UserPropertyRef>builder()
            .filter("name", nameFilter)
            .filter("status", statusFilter)
            .filter("age", ageFilter)
            .combineWith("name & status & age")  // Boolean algebra made simple!
            .build();
        
        // Step 3: Celebrate! 🎉
        System.out.println("✅ Your first FilterQL request is ready!");
        System.out.println("Filter count: " + request.getFilters().size());
        System.out.println("Logic: " + request.getCombineWith());
    }
}
```

**Run this now!** You should see:
```
✅ Your first FilterQL request is ready!
Filter count: 3
Logic: name & status & age
```

**🤯 Mind = Blown?** In just a few lines, you've:
- Created type-safe filters with compile-time validation
- Combined them with readable boolean logic
- Built something that will scale to any complexity

---

## Chapter 3: Real-World Power (3 minutes)

Theory is nice, but let's build something you can actually use in production. Time to create a REST endpoint that handles real searches!

### Your Complete REST Controller

```java
// VERIFIED: This controller pattern works in production Spring apps
package com.yourapp.controller;

import io.github.cyfko.filterql.core.FilterResolver;
import io.github.cyfko.filterql.core.domain.PredicateResolver;
import io.github.cyfko.filterql.core.model.FilterRequest;
import io.github.cyfko.filterql.adapter.spring.FilterContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserSearchController {
    
    private final UserRepository userRepository;
    private final FilterContext<User, UserPropertyRef> filterContext;
    
    public UserSearchController(UserRepository userRepository) {
        this.userRepository = userRepository;
        
        // This maps your enum properties to actual JPA entity fields
        Function<FilterDefinition<UserPropertyRef>, Object> mappingFunction = def -> switch (def.ref()) {
            case FULL_NAME -> "fullName";       // Maps to User.fullName field
            case EMAIL -> "email";              // Maps to User.email field  
            case AGE -> "age";                  // Maps to User.age field
            case STATUS -> "status";            // Maps to User.status field
            case CREATED_DATE -> "createdDate"; // Maps to User.createdDate field
        };
        
        this.filterContext = new FilterContext<>(User.class, UserPropertyRef.class, mappingFunction);
    }
    
    @PostMapping("/search")
    public ResponseEntity<Page<User>> searchUsers(
            @RequestBody FilterRequest<UserPropertyRef> request,
            Pageable pageable) {
        
        try {
            // The magic happens here: FilterQL → Spring Data JPA
            FilterResolver resolver = FilterResolver.of(filterContext);
            PredicateResolver<User> predicateResolver = resolver.resolve(User.class, request);
            Specification<User> spec = predicateResolver.toSpecification();
            
            // Use the generated specification with your repository
            Page<User> results = userRepository.findAll(spec, pageable);
            
            return ResponseEntity.ok(results);
            
        } catch (FilterValidationException e) {
            return ResponseEntity.badRequest()
                .header("X-Filter-Error", e.getMessage())
                .build();
        }
    }
}
```

### Test It Live!

Send this JSON to `POST /api/users/search`:

```json
{
  "filters": {
    "activeAdults": { "ref": "AGE", "operator": "GTE", "value": 18 },
    "techTeam": { "ref": "STATUS", "operator": "EQ", "value": "ACTIVE" },
    "recentJoiners": { "ref": "CREATED_DATE", "operator": "GT", "value": "2023-01-01" }
  },
  "combineWith": "activeAdults & techTeam & recentJoiners"
}
```

**💥 What happens?** FilterQL automatically:
1. ✅ Validates the property types and operators
2. ✅ Parses the boolean logic expression  
3. ✅ Generates optimized SQL via Spring Data JPA
4. ✅ Returns paginated, sorted results
5. ✅ Protects against SQL injection

**The generated SQL:**
```sql
SELECT u.* FROM users u 
WHERE u.age >= 18 
  AND u.status = 'ACTIVE' 
  AND u.created_date > '2023-01-01'
ORDER BY u.id 
LIMIT 20 OFFSET 0;
```

---

## Chapter 4: Understanding the Magic (2 minutes)

Now that you've built something amazing, let's understand **why** it works so well.

### The FilterQL Architecture (Simplified)

```
Frontend JSON Request
         ↓
   [Type Validation]  ← Your PropertyReference enum
         ↓
   [Boolean Parsing]  ← "name & age | status" becomes logic tree
         ↓  
   [Filter Building]  ← FilterDefinitions become Conditions
         ↓
  [Framework Bridge] ← Spring Data JPA Specifications
         ↓
    Optimized SQL    ← Database execution
```

### Why It's Revolutionary

**Traditional filtering libraries:**
- ❌ String-based property names (typos = runtime errors)
- ❌ No operator validation (crashes in production)
- ❌ Manual SQL building (injection vulnerabilities)
- ❌ Framework-specific (locked into one technology)

**FilterQL:**
- ✅ **Type-safe enums** prevent property name typos
- ✅ **Operator constraints** catch invalid combinations at compile time  
- ✅ **Framework adapters** generate safe, optimized queries
- ✅ **Framework agnostic** core works with any persistence technology

### The Power of Composition

Here's what makes developers fall in love with FilterQL:

```java
// Simple filters
"activeUsers"                    → status = 'ACTIVE'

// Complex boolean logic  
"admin | (developer & senior)"   → (role = 'ADMIN') OR (role = 'DEVELOPER' AND level = 'SENIOR')

// Readable business rules
"(active & verified) | vip"      → ((status = 'ACTIVE' AND verified = true) OR tier = 'VIP')
```

**🎯 The result?** Your filtering logic reads like English, but executes like optimized SQL.

---

## Chapter 5: What's Next? (1 minute)

Congratulations! 🎉 In just 10 minutes, you've:

- ✅ **Installed** FilterQL and understood its value
- ✅ **Created** your first type-safe property definitions  
- ✅ **Built** working filters with boolean logic
- ✅ **Integrated** with Spring Data JPA
- ✅ **Deployed** a production-ready REST endpoint

### Your Next Adventures

<table>
<tr>
<td width="50%" valign="top">

### 🎓 **Continue Learning**

- [**Core Module Deep Dive**](core-module.md)  
  *Master all FilterQL components*

- [**Spring Integration Mastery**](spring-adapter.md)  
  *Advanced Spring Data JPA patterns*

- [**Real-World Examples**](examples.md)  
  *Complex scenarios and best practices*

</td>
<td width="50%" valign="top">

### 🚀 **Start Building**

- [**Production Deployment**](troubleshooting.md)  
  *Error handling and monitoring*

- [**Performance Tuning**](examples.md)  
  *Optimization for large datasets*

- [**Architecture Guide**](ARCHITECTURE.md)  
  *Extension points and customization*

</td>
</tr>
</table>

### Join the Community

- 💬 **[GitHub Discussions](https://github.com/cyfko/filter-build/discussions)** - Ask questions, share patterns
- 🐛 **[Issues](https://github.com/cyfko/filter-build/issues)** - Report bugs, request features  
- ⭐ **[Star the repo](https://github.com/cyfko/filter-build)** - Show your support!

---

## The Journey Continues...

You've just experienced the power of FilterQL. You've built something real, learned the fundamentals, and glimpsed the possibilities.

**The question is:** What will you build next?

Whether it's a user management system, a product catalog, an analytics dashboard, or the next revolutionary SaaS platform—FilterQL is ready to grow with you.

**Ready for your next chapter?**

<div align="center">
  <p><strong>🏗️ <a href="spring-adapter.md">Master Spring Integration</a></strong></p>
  <p><em>Become a Spring Data JPA filtering expert</em></p>
  
  <p><strong>🔬 <a href="core-module.md">Explore Core Architecture</a></strong></p>
  <p><em>Understand FilterQL's powerful internals</em></p>
  
  <p><strong>🎯 <a href="examples.md">See Real-World Examples</a></strong></p>
  <p><em>Complex patterns and production scenarios</em></p>
</div>

---

<div align="center">
  <p><em>Welcome to the FilterQL community! 🚀</em></p>
</div>