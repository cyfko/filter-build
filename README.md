# Dynamic Filter Builder – Framework-Agnostic Solution

[![License](https://img.shields.io/badge/license-MIT-green)](LICENSE)
[![Build Status](https://img.shields.io/github/actions/workflow/status/yourrepo/build.yml)]
[![Coverage](https://img.shields.io/codecov/c/github/yourrepo)]

---

## Table of Contents
- [Motivation](#motivation)
- [Features](#features)
- [How It Works](#how-it-works)
- [JSON API Example](#json-api-example)
- [DSL Syntax](#dsl-syntax)
- [Implementation Overview](#implementation-overview)
- [Dynamic Filter Flow](#dynamic-filter-flow)
- [Portability](#portability)
- [Advantages](#advantages)
- [Future Enhancements](#future-enhancements)
- [License](#license)

---

## Motivation

Modern web applications often need **dynamic, user-driven search and filtering**. Traditional approaches like static filters or exposing raw queries are inflexible or expose security risks.

This library proposes a reusable, framework-agnostic solution that:
- Allows clients to build complex filters dynamically combining multiple conditions with AND, OR, NOT.
- Prevents direct exposure of database fields by using abstract tokens.
- Supports multiple conditions on the same property by separating filter token from property reference.
- Works seamlessly across different backend technologies.

---

## Features

- Expressive, simple *DSL-based* filter language  
- Filter tokens uniquely identify conditions, with clear `ref` mapping to backend properties  
- Complex boolean logic with `AND`, `OR`, `NOT`, and parentheses  
- Secure enum or whitelist-based field mapping preventing access to sensitive data  
- Fully framework-agnostic and reusable across diverse entities and data models  
- Compatible with various query toolkits and ORMs  

---

## How It Works

1. The client sends a JSON request:
   - `filters`: an object mapping unique filter tokens (keys) to filter conditions, each condition including a `ref` field referencing the backend property.
   - `combineWith`: a DSL string referencing the tokens, expressing the boolean logic.

2. The backend:
   - Maps tokens to validated properties via `ref`.
   - Parses the DSL into a boolean expression tree.
   - Builds native queries or criteria filters accordingly.
   - Executes the query and returns filtered results.

---

## JSON API Example

**Request:**

```json
{
  "filters": {
    "filter1": { "ref": "NAME", "operator": "LIKE", "value": "Smith" },
    "filter2": { "ref": "STATUS", "operator": "=", "value": "ACTIVE" },
    "filter3": { "ref": "CREATED_DATE", "operator": ">=", "value": "2024-01-01" },
    "filter4": { "ref": "NAME", "operator": "NOT LIKE", "value": "John" }
  },
  "combineWith": "(filter1 & filter2) | (filter3 & !filter4)"
}
```

- Each token (`filter1`, `filter2`, etc.) uniquely identifies a filter condition.
- The token’s `ref` points to the backend property it targets.
- This enables multiple filters targeting the same property (`NAME` here) without JSON key duplication issues.
- The `combineWith` DSL combines these tokens with logical operators.

**Interpreted as:**

```sql
(NAME LIKE 'Smith' AND STATUS = 'ACTIVE') OR (CREATED_DATE >= '2024-01-01' AND NOT(NAME LIKE 'John'))
```
> That interpretation **IS NOT** an SQL syntax! It just help to understand the meaning behind the DSL constructed based on the filters and the combinator.


---

## DSL Syntax

- Logical operators supported:  
  - `&` for AND  
  - `|` for OR  
  - `!` for NOT  
- Parentheses for grouping expressions.  
- Identifiers correspond to filter tokens in the `filters` object, not directly to properties.

**Example:**

```cpp
(filter1 & filter2) | !filter3
```

---

## Implementation Overview

- **Filter Map:** JSON object with unique tokens → filter conditions including `ref`, operator, and value.  
- **DSL Expression:** String combining filter tokens via `&`, `|`, `!`, and parentheses.  
- **Mapping:** Tokens map to allowed properties defined in a backend whitelist or enum.  
- **Query Builder:** The DSL parser generates a boolean expression tree, which is translated into native query objects or ORM filters in a framework-neutral manner.

---

## Dynamic Filter Flow

```mermaid
flowchart TD
A[Client JSON Request (filters + DSL string)] --> B[DSL Parser]
B --> C[Boolean Expression Tree]
C --> D[Token to Property Reference Mapping]
D --> E[Build Native Query/Filter]
E --> F[Execute Query]
F --> G[Return Results]
```


---

## Portability

This library’s core is **framework-agnostic**:  
- Parsing and token-to-property mapping are shared and standalone.  
- Only the last step, query building, needs adaptation to the backend language and ORM.

| Language / Stack              | Integration Approach                                      | Notes                              |
|------------------------------|-----------------------------------------------------------|-----------------------------------|
| Java/Spring Boot              | Translate tree into JPA `CriteriaQuery` or Specifications | Map tokens to entity attributes   |
| .NET / Entity Framework       | Use LINQ expressions                                       | Use controlled predicate builders |
| Python / Django ORM           | Use `Q` object composition                                | Map DSL to Q filters              |
| Node.js / Prisma              | Build nested JSON `where`                                  | Map tokens to Prisma filters      |
| Node.js / TypeORM             | Chain query builder conditions                             | Maintain whitelist of tokens      |

---

## Advantages

- **Security:** Fields exposed only through controlled mappings.  
- **Flexibility:** Arbitrary complex filters expressed via DSL.  
- **Framework neutrality:** Use in any backend stack with minimal adaptation.  
- **Maintainability:** Centralized token-to-property mapping.  
- **Extensibility:** Add operators, predicates, and DSL improvements independently.

---

## Future Enhancements

- Support additional operators (`IN`, `BETWEEN`, `IS NULL`, etc.).  
- Type-aware value validation (dates, enums, numbers).  
- Optimize query plans by merging overlapping filters.  
- Pagination and sorting integrated with filters.  
- Improved tooling for DSL syntax validation and debugging.

---

## License

MIT License

---

