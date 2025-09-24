package io.github.cyfko.dynamicfilter.example;

import io.github.cyfko.dynamicfilter.core.model.FilterDefinition;
import io.github.cyfko.dynamicfilter.core.model.FilterRequest;
import io.github.cyfko.dynamicfilter.core.validation.Operator;

import java.util.HashMap;
import java.util.Map;

/**
 * Example demonstrating the flexibility of PropertyRef without PropertyRegistry.
 * Each adapter can interpret PropertyRef in its own way, allowing:
 * - Single PropertyRef mapping to multiple entity fields
 * - Complex conditions from simple PropertyRef
 * - Adapter-specific logic
 */
public class FlexiblePropertyRefExample {
    
    public static void main(String[] args) {
        System.out.println("=== Flexible PropertyRef Example ===");
        
        // Create filter definitions with PropertyRef directly
        Map<String, FilterDefinition> filters = createFilterDefinitions();
        
        // Create filter request
        FilterRequest request = new FilterRequest(filters, "f1 & f2");
        
        System.out.println("Filter Request: " + request);
        System.out.println("\nFilter Definitions:");
        filters.forEach((key, filter) -> {
            System.out.println("  " + key + ": " + filter);
            System.out.println("    PropertyRef: " + filter.getRef());
            System.out.println("    Type: " + filter.getRef().getType().getSimpleName());
            System.out.println("    Supported Operators: " + filter.getRef().getSupportedOperators());
        });
        
        System.out.println("\n=== Adapter Flexibility Examples ===");
        demonstrateAdapterFlexibility();
    }
    
    private static Map<String, FilterDefinition> createFilterDefinitions() {
        Map<String, FilterDefinition> filters = new HashMap<>();
        
        // USER_NAME can map to multiple fields in different adapters
        filters.put("f1", new FilterDefinition(
            UserPropertyRef.USER_NAME,
            "LIKE",
            "Smith"
        ));
        
        // USER_AGE simple mapping
        filters.put("f2", new FilterDefinition(
            UserPropertyRef.USER_AGE,
            ">=",
            "18"
        ));
        
        // USER_STATUS simple mapping
        filters.put("f3", new FilterDefinition(
            UserPropertyRef.USER_STATUS,
            "=",
            "ACTIVE"
        ));
        
        return filters;
    }
    
    private static void demonstrateAdapterFlexibility() {
        System.out.println("JPA Adapter Interpretation:");
        System.out.println("  USER_NAME LIKE 'Smith' -> WHERE (firstName LIKE '%Smith%' OR lastName LIKE '%Smith%')");
        System.out.println("  USER_AGE >= 18 -> WHERE age >= 18");
        System.out.println("  USER_STATUS = 'ACTIVE' -> WHERE status = 'ACTIVE'");
        
        System.out.println("\nPrisma Adapter Interpretation:");
        System.out.println("  USER_NAME LIKE 'Smith' -> where: { OR: [{ first_name: { contains: 'Smith' } }, { last_name: { contains: 'Smith' } }] }");
        System.out.println("  USER_AGE >= 18 -> where: { age: { gte: 18 } }");
        System.out.println("  USER_STATUS = 'ACTIVE' -> where: { status: 'ACTIVE' }");
        
        System.out.println("\nSQLAlchemy Adapter Interpretation:");
        System.out.println("  USER_NAME LIKE 'Smith' -> User.query.filter(or_(User.first_name.like('%Smith%'), User.last_name.like('%Smith%')))");
        System.out.println("  USER_AGE >= 18 -> User.query.filter(User.age >= 18)");
        System.out.println("  USER_STATUS = 'ACTIVE' -> User.query.filter(User.user_status == 'ACTIVE')");
    }
    
    /**
     * Example of how different adapters can interpret the same PropertyRef differently.
     */
    public static class AdapterInterpretations {
        
        /**
         * JPA Adapter: USER_NAME maps to firstName + lastName for LIKE operations
         */
        public static String getJpaCondition(PropertyRef propertyRef, String operator, Object value) {
            if (propertyRef.name().equals("USER_NAME")) {
                if (operator.equals("LIKE")) {
                    return String.format("(firstName LIKE '%%%s%%' OR lastName LIKE '%%%s%%')", value, value);
                } else if (operator.equals("=")) {
                    return String.format("CONCAT(firstName, ' ', lastName) = '%s'", value);
                }
            } else if (propertyRef.name().equals("USER_EMAIL")) {
                return String.format("email %s '%s'", operator, value);
            } else if (propertyRef.name().equals("USER_AGE")) {
                return String.format("age %s %s", operator, value);
            }
            return "Unknown property";
        }
        
        /**
         * Prisma Adapter: USER_NAME maps to first_name + last_name
         */
        public static String getPrismaCondition(PropertyRef propertyRef, String operator, Object value) {
            if (propertyRef.name().equals("USER_NAME")) {
                if (operator.equals("LIKE")) {
                    return String.format("where: { OR: [{ first_name: { contains: '%s' } }, { last_name: { contains: '%s' } }] }", value, value);
                } else if (operator.equals("=")) {
                    return String.format("where: { first_name: '%s', last_name: '%s' }", value.toString().split(" ")[0], value.toString().split(" ")[1]);
                }
            } else if (propertyRef.name().equals("USER_EMAIL")) {
                return String.format("where: { email: { %s: '%s' } }", getPrismaOperator(operator), value);
            } else if (propertyRef.name().equals("USER_AGE")) {
                return String.format("where: { age: { %s: %s } }", getPrismaOperator(operator), value);
            }
            return "Unknown property";
        }
        
        /**
         * SQLAlchemy Adapter: USER_NAME maps to first_name + last_name
         */
        public static String getSqlAlchemyCondition(PropertyRef propertyRef, String operator, Object value) {
            if (propertyRef.name().equals("USER_NAME")) {
                if (operator.equals("LIKE")) {
                    return String.format("User.query.filter(or_(User.first_name.like('%%%s%%'), User.last_name.like('%%%s%%')))", value, value);
                } else if (operator.equals("=")) {
                    return String.format("User.query.filter(and_(User.first_name == '%s', User.last_name == '%s'))", value.toString().split(" ")[0], value.toString().split(" ")[1]);
                }
            } else if (propertyRef.name().equals("USER_EMAIL")) {
                return String.format("User.query.filter(User.email %s '%s')", operator, value);
            } else if (propertyRef.name().equals("USER_AGE")) {
                return String.format("User.query.filter(User.age %s %s)", operator, value);
            }
            return "Unknown property";
        }
        
        private static String getPrismaOperator(String operator) {
            return switch (operator) {
                case "=" -> "equals";
                case "!=" -> "not";
                case ">" -> "gt";
                case ">=" -> "gte";
                case "<" -> "lt";
                case "<=" -> "lte";
                case "LIKE" -> "contains";
                default -> operator;
            };
        }
    }
    
    /**
     * Example of complex PropertyRef that can map to multiple fields.
     */
    public static class ComplexPropertyRefExample {
        
        public static void demonstrateComplexMapping() {
            System.out.println("\n=== Complex PropertyRef Mapping ===");
            
            // Example: USER_FULL_NAME can map to different fields in different adapters
            FilterDefinition fullNameFilter = new FilterDefinition(
                UserPropertyRef.USER_NAME, // This is just an example
                "LIKE",
                "John Smith"
            );
            
            System.out.println("JPA: USER_NAME LIKE 'John Smith'");
            System.out.println("  -> WHERE (firstName LIKE '%John%' AND lastName LIKE '%Smith%')");
            System.out.println("  -> OR WHERE CONCAT(firstName, ' ', lastName) LIKE '%John Smith%'");
            
            System.out.println("\nPrisma: USER_NAME LIKE 'John Smith'");
            System.out.println("  -> where: { AND: [{ first_name: { contains: 'John' } }, { last_name: { contains: 'Smith' } }] }");
            
            System.out.println("\nSQLAlchemy: USER_NAME LIKE 'John Smith'");
            System.out.println("  -> User.query.filter(and_(User.first_name.like('%John%'), User.last_name.like('%Smith%')))");
        }
    }
}
