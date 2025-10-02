# FilterQL Documentation Index

Welcome to the complete FilterQL documentation. FilterQL is a type-safe, framework-agnostic Java library for building dynamic filters with Spring Data JPA integration.

## 📚 Documentation Structure

### Getting Started
- **[Quick Start Guide](getting-started/quick-start.md)** - Get up and running with FilterQL in 10 minutes
- **[Installation](getting-started/installation.md)** - Dependency management and setup instructions
- **[Core Concepts](core-concepts.md)** - Understanding FilterQL's fundamental concepts

### Core Documentation
- **[Core Module Overview](core-module/overview.md)** - Framework-agnostic filtering engine
- **[Spring Adapter](spring-adapter/overview.md)** - Spring Data JPA integration
- **[API Reference](api-reference.md)** - Complete API documentation

### Practical Guides
- **[Best Practices](guides/best-practices.md)** - Comprehensive best practices and patterns
- **[Migration Guide](guides/migration-guide.md)** - Migrating from other query solutions
- **[Troubleshooting](guides/troubleshooting.md)** - Common issues and solutions

### Examples & Use Cases
- **[Comprehensive Examples](examples/comprehensive-examples.md)** - Real-world implementation examples
- **[Repository Integration](examples/repository-integration.md)** - Spring Data integration patterns
- **[Performance Optimization](examples/performance-optimization.md)** - Query optimization techniques

### Advanced Topics
- **[Architecture](ARCHITECTURE.md)** - Detailed architecture documentation
- **[Advanced Usage](advanced-usage.md)** - Complex scenarios and customizations
- **[Security Considerations](guides/security.md)** - Security best practices

## 🚀 Quick Navigation

### For Beginners
1. Start with [Quick Start Guide](getting-started/quick-start.md)
2. Read [Core Concepts](core-concepts.md)
3. Try [Basic Examples](examples/comprehensive-examples.md#basic-filtering-examples)

### For Existing Projects
1. Check [Migration Guide](guides/migration-guide.md)
2. Review [Best Practices](guides/best-practices.md)
3. Implement [Performance Optimizations](examples/performance-optimization.md)

### For Advanced Users
1. Study [Architecture](ARCHITECTURE.md)
2. Explore [Advanced Usage](advanced-usage.md)
3. Review [API Reference](api-reference.md)

## 📖 Key Features

### Type Safety
- **Compile-time validation** of filter properties and operators
- **Generic type parameters** throughout the API
- **Enum-based property references** for IntelliSense support

### Framework Integration
- **Spring Data JPA** adapter with Specification support
- **Framework-agnostic** core for other persistence solutions
- **Seamless integration** with existing Spring applications

### Powerful Querying
- **Boolean logic** with AND, OR, NOT operations
- **Range queries** for dates, numbers, and other comparable types
- **Pattern matching** with SQL LIKE support
- **Custom business logic** through flexible mapping functions

### Performance
- **Query optimization** with fetch joins and efficient SQL generation
- **Caching support** for frequently used filters
- **Stateless design** for high-concurrency applications

## 🔧 Common Use Cases

### E-commerce Search
```java
// Product search with multiple criteria
FilterRequest<ProductPropertyRef> request = FilterRequest.<ProductPropertyRef>builder()
    .filter("category", new FilterDefinition<>(ProductPropertyRef.CATEGORY, Op.IN, categories))
    .filter("priceRange", new FilterDefinition<>(ProductPropertyRef.PRICE, Op.RANGE, Arrays.asList(min, max)))
    .filter("inStock", new FilterDefinition<>(ProductPropertyRef.STOCK_QUANTITY, Op.GT, 0))
    .combineWith("category & priceRange & inStock")
    .build();
```

### HR Management
```java
// Employee search with complex criteria
FilterRequest<EmployeePropertyRef> request = FilterRequest.<EmployeePropertyRef>builder()
    .filter("department", new FilterDefinition<>(EmployeePropertyRef.DEPARTMENT, Op.EQ, "Engineering"))
    .filter("experience", new FilterDefinition<>(EmployeePropertyRef.YEARS_EXPERIENCE, Op.GTE, 5))
    .filter("performance", new FilterDefinition<>(EmployeePropertyRef.PERFORMANCE_RATING, Op.GTE, 4.0))
    .combineWith("department & (experience | performance)")
    .build();
```

### Financial Analytics
```java
// Transaction analysis with date ranges and amounts
FilterRequest<TransactionPropertyRef> request = FilterRequest.<TransactionPropertyRef>builder()
    .filter("dateRange", new FilterDefinition<>(TransactionPropertyRef.DATE, Op.RANGE, Arrays.asList(start, end)))
    .filter("highValue", new FilterDefinition<>(TransactionPropertyRef.AMOUNT, Op.GT, threshold))
    .filter("international", new FilterDefinition<>(TransactionPropertyRef.IS_INTERNATIONAL, Op.EQ, true))
    .combineWith("dateRange & (highValue | international)")
    .build();
```

## 🎯 Design Principles

### Simplicity
FilterQL prioritizes **developer experience** with intuitive APIs and minimal boilerplate code.

### Type Safety
**Compile-time validation** prevents runtime errors and provides excellent IDE support.

### Performance
Optimized for **high-performance** applications with efficient query generation and caching.

### Flexibility
**Extensible architecture** supports custom business logic and complex filtering requirements.

### Integration
**Framework-friendly** design works seamlessly with Spring and other Java frameworks.

## 📋 Version Information

- **Current Version**: 2.0.0
- **Minimum Java Version**: 21
- **Spring Boot Compatibility**: 3.3.4+
- **Jakarta Persistence API**: 3.1.0+

## 🤝 Community & Support

### Getting Help
- **[Troubleshooting Guide](guides/troubleshooting.md)** - Common issues and solutions
- **[Best Practices](guides/best-practices.md)** - Recommended patterns and approaches
- **[API Reference](api-reference.md)** - Complete method documentation

### Contributing
- Follow the coding standards outlined in the documentation
- Add comprehensive tests for new features
- Update documentation for any API changes

### License
FilterQL is distributed under the Apache License 2.0. See LICENSE file for details.

## 📈 Roadmap

### Version 2.1 (Planned)
- Enhanced validation framework
- Additional operator support (REGEX, CONTAINS)
- Performance improvements

### Version 2.2 (Planned)
- GraphQL integration adapter
- MongoDB adapter
- Advanced caching strategies

### Future Versions
- Multi-database support
- Query plan optimization
- Real-time filtering capabilities

---

**Ready to get started?** Begin with the [Quick Start Guide](getting-started/quick-start.md) or explore [comprehensive examples](examples/comprehensive-examples.md) for your use case.
