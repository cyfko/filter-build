# Dynamic Filtering System Architecture

---

## Description of the Filtering Query Execution Process

1. **Parsing and validating the DSL**  
   The parser checks the DSL’s syntax and ensures all referenced filter keys exist in the filter object.  
   The outcome is a combined filter tree (`FilterTree`).  
   > *An error is returned if the DSL is invalid.*

2. **Constructing and validating filters**  
   Each filter condition is validated to confirm the referenced property supports the specified operator for the given type/value.  
   The corresponding condition is built, but filtering is not yet performed.  
   > *An error is returned if a condition cannot be built.*

3. **Generating the global condition**  
   From the combination tree, generate the global condition that will be used to filter data.

4. **Executing and returning results**  
   The global condition applies to the dataset to filter results, which are then returned.

---

## Main Interfaces

| Interface / Contract | Required Methods       | Parameters             | Return Type        | Description                                                                                |
|----------------------|-------------------------|-----------------------|--------------------|--------------------------------------------------------------------------------------------|
| **Parser**           | parse                   | DSL expression (text) | `FilterTree`       | Parses the DSL expression and builds the logical combined filter tree.                     |
| **FilterTree**       | generate                | `Context`             | `Condition`        | Produces the global condition for filtering based on the filter combination tree.          |
| **Context**          | getCondition            | Filter key (text)   | `Condition`        | Retrieves the condition associated with the filter key within the execution context.       |
| **Condition**        | and                     | `Condition`           | `Condition`        | Returns a new condition representing the logical AND of the current and given condition.   |
|                      | or                      | `Condition`           | `Condition`        | Returns a new condition representing the logical OR of the current and given condition.    |
|                      | not                     | *None*                | `Condition`        | Returns a new condition representing the logical negation of the current condition.        |
| **FilterExecutor**   | execute                 | `Condition`             | *Filtered results* | Executes the filtering operation based on the global condition and returns filtered results.  |

---

## Additional Explanations

- **Parser**: Translates the DSL into an exploitable logical tree structure.  
- **FilterTree**: Represents the logical structure combining filters to generate a global condition.  
- **Context**: Holds the set of valid filters and enables mapping of filter keys to concrete predicates.  
- **Condition**: Abstract entity representing a condition or logical combination of conditions on the data.  
- **FilterExecutor**: Applies the global condition to the data source and produces filtered results.

---

## UML Class Diagram

```mermaid
classDiagram
    class Parser {
      +FilterTree parse(String dslExpression)
    }

    class FilterTree {
      +Condition generate(Context context)
    }

    class Context {
      +Condition getCondition(String filterKey)
    }

    class Condition {
      +Condition and(Condition p)
      +Condition or(Condition p)
      +Condition not()
    }

    class FilterExecutor {
      +Result execute(Condition globalCondition)
    }

    Parser --> FilterTree : produces
    FilterTree --> Condition : generates
    Context --> Condition : provides by key
    Condition --> Condition : and
    Condition --> Condition : or
    Condition --> Condition : not
    FilterExecutor --> Result : produces
    FilterExecutor --> Condition : uses
```

### Explanation
- **Parser** produces a FilterTree from a DSL expression string.

- **FilterTree** generates a global Condition from the combination tree.

- **Context** manages filter predicates keyed by unique filter tokens.

- **Condition** supports logical combinators: and, or, not, returning new predicates.

- **FilterExecutor** uses the global condition to execute the filtering and return results.

This diagram represents the main components and their method interactions at a high level in the dynamic filtering system, independently from any specific programming language.
