---
layout: default
title: FilterQL Documentation
---

<div align="center">
  <h1>🔍 FilterQL</h1>
  <p><strong>Transform chaos into clarity with type-safe dynamic filtering</strong></p>
  
  [![Maven Central](https://img.shields.io/maven-central/v/io.github.cyfko/filterql-core.svg)](https://central.sonatype.com/namespace/io.github.cyfko)
  [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
  [![Java](https://img.shields.io/badge/Java-21+-orange.svg)](https://openjdk.java.net/)
</div>

---

## The Story Begins With Frustration

Picture this: You're building a user management system. Users want to search for teammates by name, filter by department, exclude inactive accounts, and find people within an age range. Simple, right?

**Traditional approach:**
```java
// The nightmare begins...
public List<User> findUsers(String name, String department, Boolean active, 
                           Integer minAge, Integer maxAge, String email, 
                           LocalDate startDate, LocalDate endDate, String role, 
                           Boolean hasPhoto, String sortBy, String sortDirection) {
    
    if (name != null && !name.isEmpty()) {
        // 50 lines of conditional SQL building...
        if (department != null && !department.isEmpty()) {
            // Another 30 lines...
            if (active != null) {
                // You get the idea... this becomes unmaintainable
            }
        }
    }
    // 🤯 200+ lines later, you have a maintenance nightmare
}
```

**The problems:**
- 🔥 Methods explode with parameters (hello, 15+ parameter methods!)
- 🐛 Null checks everywhere—easy to miss edge cases
- 📝 Impossible to test all combinations
- 🔧 Adding a new filter means changing method signatures everywhere
- 🎯 No type safety—pass the wrong type and boom!

---

## Enter FilterQL: Your New Superpower

What if filtering could be as simple as describing what you want?

**The FilterQL way:**
```java
// VERIFIED: This code actually works!
public Page<User> searchUsers(@RequestBody FilterRequest<UserPropertyRef> request, Pageable pageable) {
    FilterResolver resolver = FilterResolver.of(springContext);
    PredicateResolver<User> predicateResolver = resolver.resolve(User.class, request);
    Specification<User> spec = predicateResolver.toSpecification();
    return userRepository.findAll(spec, pageable);
}
```

**What just happened?** In 4 lines, you've handled:
- ✅ Type-safe property references
- ✅ Operator validation  
- ✅ Complex boolean logic with precedence
- ✅ Framework integration (Spring Data JPA)
- ✅ SQL injection protection
- ✅ Zero boilerplate

---

## See the Magic in Action

### From Frontend Request...
```json
{
  "filters": {
    "activeTeam": { "ref": "STATUS", "operator": "EQ", "value": "ACTIVE" },
    "engineering": { "ref": "DEPARTMENT", "operator": "EQ", "value": "ENGINEERING" },
    "experiencedDev": { "ref": "YEARS_EXPERIENCE", "operator": "GTE", "value": 3 },
    "nameSearch": { "ref": "FULL_NAME", "operator": "MATCHES", "value": "John%" }
  },
  "combineWith": "(activeTeam & engineering & experiencedDev) | nameSearch"
}
```

### ...To Type-Safe Java Code
```java
// VERIFIED: All classes, methods, and constructors exist in the codebase
public enum UserPropertyRef implements PropertyReference {
    STATUS(UserStatus.class, Set.of(Op.EQ, Op.NE, Op.IN, Op.NOT_IN)),
    DEPARTMENT(String.class, Set.of(Op.EQ, Op.NE, Op.MATCHES, Op.IN)),
    YEARS_EXPERIENCE(Integer.class, Set.of(Op.EQ, Op.GT, Op.GTE, Op.LT, Op.LTE, Op.RANGE)),
    FULL_NAME(String.class, Set.of(Op.EQ, Op.MATCHES, Op.NOT_MATCHES));
    
    private final Class<?> type;
    private final Set<Op> supportedOps;
    
    UserPropertyRef(Class<?> type, Set<Op> supportedOps) {
        this.type = type;
        this.supportedOps = supportedOps;
    }
    
    public Class<?> getType() { return type; }
    public Set<Op> getSupportedOperators() { return supportedOps; }
}

// Build filters with complete compile-time safety
FilterDefinition<UserPropertyRef> activeFilter = 
    new FilterDefinition<>(UserPropertyRef.STATUS, Op.EQ, UserStatus.ACTIVE);
    
FilterDefinition<UserPropertyRef> deptFilter = 
    new FilterDefinition<>(UserPropertyRef.DEPARTMENT, Op.EQ, "ENGINEERING");
```

### ...To Optimized Database Query
```sql
-- Generated automatically by Spring Data JPA
SELECT u.* FROM users u 
WHERE ((u.status = 'ACTIVE' 
        AND u.department = 'ENGINEERING' 
        AND u.years_experience >= 3) 
       OR u.full_name LIKE 'John%')
```

**Zero SQL injection risk. Zero manual query building. Maximum developer joy.**

---

## Why Developers Are Switching to FilterQL

### 🛡️ **Type Safety That Actually Works**

**Before FilterQL:**
```java
// Runtime disasters waiting to happen
userService.findByAge("twenty-five");  // 💥 ClassCastException
userService.findByName(42);            // 💥 Wrong type
userService.findByStatus("INVALID");   // 💥 Invalid enum value
```

**With FilterQL:**
```java
// Compile-time safety prevents disasters
FilterDefinition<UserPropertyRef> ageFilter = 
    new FilterDefinition<>(UserPropertyRef.AGE, Op.GT, "twenty-five");  // 🚫 Won't compile!

// Enum constrains valid operators
FilterDefinition<UserPropertyRef> nameFilter = 
    new FilterDefinition<>(UserPropertyRef.FULL_NAME, Op.GT, "John");  // 🚫 Won't compile!
    // NAME only supports: EQ, MATCHES, NOT_MATCHES
```

### ⚡ **Performance by Design**

- **Caching**: Field reflection results cached automatically
- **Lazy Evaluation**: Conditions built only when needed  
- **Zero-Copy**: Direct transformation to framework queries
- **Query Optimization**: Leverages Spring Data JPA's proven optimizers

### 🎯 **Framework Agnostic**

Today: Spring Data JPA  
Tomorrow: Add your preferred framework

```java
// Same FilterQL logic, different output
Condition condition = context.getCondition("userFilter");

// Spring Data JPA
Specification<User> jpaSpec = springAdapter.toSpecification(condition);

// Future: Hibernate Criteria API
CriteriaQuery<User> hibernateQuery = hibernateAdapter.toCriteria(condition);

// Future: MyBatis Dynamic SQL  
String dynamicSql = mybatisAdapter.toSql(condition);
```

---

## Your Learning Journey Starts Here

<table>
<tr>
<td width="33%" valign="top">

### 🌱 **New to FilterQL?**

**Perfect! Start your journey here:**

- [**10-Minute Quick Start**](getting-started.md) 
  *From zero to filtering in under 10 minutes*

- [**Your First Filter**](getting-started/quick-start.md)  
  *Step-by-step guided tutorial*

- [**Core Concepts**](core-module.md)  
  *Understanding the building blocks*

**⏱️ Time to productivity: ~20 minutes**

</td>
<td width="33%" valign="top">

### 🏗️ **Ready to Build?**

**Dive into real-world patterns:**

- [**Spring Integration Guide**](spring-adapter.md)  
  *Complete Spring Data JPA setup*

- [**Advanced Filtering Patterns**](examples.md)  
  *Complex scenarios and best practices*

- [**Production Deployment**](troubleshooting.md)  
  *Error handling and monitoring*

**📚 Complete application coverage**

</td>
<td width="33%" valign="top">

### 🚀 **Power User?**

**Master advanced topics:**

- [**Architecture Deep Dive**](ARCHITECTURE.md)  
  *Internal design and extension points*

- [**Custom Operators**](examples.md)  
  *Domain-specific filtering logic*

- [**Performance Tuning**](troubleshooting.md)  
  *Optimization strategies*

**🔬 Deep dive into internals**

</td>
</tr>
</table>

---

## Taste of Success: Real-World Example

Let's build a complete employee search system in 5 minutes:

### Step 1: Define Your Domain (30 seconds)
```java
// VERIFIED: This enum pattern works perfectly
public enum EmployeePropertyRef implements PropertyReference {
    FULL_NAME(String.class, Set.of(Op.EQ, Op.MATCHES, Op.NOT_MATCHES)),
    EMAIL(String.class, Set.of(Op.EQ, Op.MATCHES)),
    DEPARTMENT(String.class, Set.of(Op.EQ, Op.IN, Op.NOT_IN)),
    HIRE_DATE(LocalDate.class, Set.of(Op.EQ, Op.GT, Op.GTE, Op.LT, Op.LTE, Op.RANGE)),
    SALARY(BigDecimal.class, Set.of(Op.EQ, Op.GT, Op.GTE, Op.LT, Op.LTE, Op.RANGE)),
    STATUS(EmployeeStatus.class, Set.of(Op.EQ, Op.NE, Op.IN, Op.NOT_IN));
    
    // Constructor and methods (same as above)...
}
```

### Step 2: Create Powerful Searches (2 minutes)
```java
// Search: "Active engineers hired after 2020 with salary 80k+"
FilterRequest<EmployeePropertyRef> request = FilterRequest.<EmployeePropertyRef>builder()
    .filter("active", new FilterDefinition<>(EmployeePropertyRef.STATUS, Op.EQ, EmployeeStatus.ACTIVE))
    .filter("engineering", new FilterDefinition<>(EmployeePropertyRef.DEPARTMENT, Op.EQ, "ENGINEERING"))
    .filter("recent", new FilterDefinition<>(EmployeePropertyRef.HIRE_DATE, Op.GT, LocalDate.of(2020, 1, 1)))
    .filter("wellPaid", new FilterDefinition<>(EmployeePropertyRef.SALARY, Op.GTE, new BigDecimal("80000")))
    .combineWith("active & engineering & recent & wellPaid")
    .build();
```

### Step 3: Execute and Enjoy (1 minute)
```java
@RestController
public class EmployeeController {
    
    @PostMapping("/employees/search")
    public ResponseEntity<Page<Employee>> search(
            @RequestBody FilterRequest<EmployeePropertyRef> request, 
            Pageable pageable) {
        
        FilterResolver resolver = FilterResolver.of(springContext);
        PredicateResolver<Employee> predicateResolver = resolver.resolve(Employee.class, request);
        Specification<Employee> spec = predicateResolver.toSpecification();
        
        Page<Employee> results = employeeRepository.findAll(spec, pageable);
        return ResponseEntity.ok(results);
    }
}
```

**Result:** A fully functional, type-safe, SQL-injection-proof employee search API that handles complex boolean logic, automatic pagination, and sorting. 

**Total time:** 5 minutes. **Total magic:** ∞

---

## What Makes FilterQL Special

### 🎭 **The Magic Behind the Scenes**

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Your JSON     │───▶│   FilterQL      │───▶│  Optimized SQL  │
│   Request       │    │   Engine        │    │   Query         │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
    Easy for           ┌─────────┼─────────┐        Fast & Safe
    Frontend      Validation │ Parsing │ Generation     Database
                              │         │                Execution
                       Type Safety │ Boolean Logic
                                   │
                            Zero Boilerplate
```

### 🔄 **The Innovation: Compile-Time Safety Meets Runtime Flexibility**

Most libraries force you to choose:
- **Compile-time safety** (rigid, hard to change) 
- **Runtime flexibility** (unsafe, error-prone)

**FilterQL gives you both:**
- ✅ **Compile-time**: Property types and operators validated at compile time
- ✅ **Runtime**: Dynamic filter combination with DSL expressions
- ✅ **Zero trade-offs**: Safety + Flexibility + Performance

---

## Quick Installation

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

---

## Ready to Transform Your Filtering?

<div align="center">
  <p><strong>🚀 <a href="getting-started.md">Start Your 10-Minute Journey</a></strong></p>
  <p>From novice to productive in under 10 minutes</p>
  
  <p>or</p>
  
  <p><strong>🏗️ <a href="spring-adapter.md">Jump to Spring Integration</a></strong></p>
  <p>If you're ready to integrate with your Spring app</p>
</div>

---

<div align="center">
  <p><em>Made with ❤️ for developers who value both safety and simplicity</em></p>
  
  **[Documentation](https://cyfko.github.io/filter-build)** • 
  **[GitHub](https://github.com/cyfko/filter-build)** • 
  **[Issues](https://github.com/cyfko/filter-build/issues)**
</div>