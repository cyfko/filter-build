---
layout: default
title: Spring Adapter
---

# Spring Adapter

The FilterQL Spring Adapter integrates the core engine with Spring Data JPA. All configuration and examples are verified against the codebase.

## Architecture
- **FilterContext**: Spring Data JPA implementation of Context.
- **FilterCondition**: Spring-compatible filter condition.
- **PathResolverUtils**: Utility for nested JPA property paths.

## Example
```java
Function<FilterDefinition<UserPropertyRef>, Object> mappingFunction = def -> switch (def.ref()) {
    case NAME -> "name";
    case AGE -> "age";
};
FilterContext<User, UserPropertyRef> context = new FilterContext<>(User.class, UserPropertyRef.class, mappingFunction);
FilterDefinition<UserPropertyRef> nameFilter = new FilterDefinition<>(UserPropertyRef.NAME, Op.MATCHES, "John%") ;
context.addCondition("name", nameFilter);
Specification<User> spec = ((FilterCondition<User>) context.getCondition("name")).getSpecification();
```

## Spring Boot Integration
```java
@RestController
public class UserController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FilterContext<User, UserPropertyRef> filterContext;

    @PostMapping("/users/search")
    public Page<User> searchUsers(@RequestBody FilterRequest<UserPropertyRef> request, Pageable pageable) {
        request.filters().forEach(filterContext::addCondition);
        Condition combined = filterContext.getCondition("name").and(filterContext.getCondition("age"));
        Specification<User> spec = ((FilterCondition<User>) combined).getSpecification();
        return userRepository.findAll(spec, pageable);
    }
}
```

## API Reference
- [FilterContext](adapters/java/spring/src/main/java/io/github/cyfko/filterql/adapter/spring/FilterContext.java)
- [FilterCondition](adapters/java/spring/src/main/java/io/github/cyfko/filterql/adapter/spring/FilterCondition.java)
- [PathResolverUtils](adapters/java/spring/src/main/java/io/github/cyfko/filterql/adapter/spring/utils/PathResolverUtils.java)
