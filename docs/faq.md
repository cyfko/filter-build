layout: default
title: FAQ

# FAQ

All answers are based on verified codebase behavior.

## How do I define a property reference?
Use an enum implementing `PropertyReference`:
```java
public enum UserPropertyRef implements PropertyReference {
    NAME(String.class, Set.of(Op.EQ, Op.MATCHES)),
    AGE(Integer.class, Set.of(Op.GT, Op.LT, Op.RANGE));
    // ...
}
```

## Can I use FilterQL with Spring Data JPA?
Yes. Use `FilterContext` and `FilterCondition` for integration.

## What operators are supported?
All operators in the `Op` enum: EQ, NE, GT, GTE, LT, LTE, MATCHES, NOT_MATCHES, IN, NOT_IN, IS_NULL, NOT_NULL, RANGE, NOT_RANGE.

## Is FilterQL thread-safe?
Core types are immutable/stateless. Context implementations should be used per request or made thread-safe for concurrent use.

