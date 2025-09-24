package io.github.cyfko.dynamicfilter.example;

import io.github.cyfko.dynamicfilter.core.Condition;
import io.github.cyfko.dynamicfilter.core.Context;
import io.github.cyfko.dynamicfilter.core.model.FilterDefinition;
import io.github.cyfko.dynamicfilter.core.model.FilterRequest;
import io.github.cyfko.dynamicfilter.core.validation.Operator;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Example demonstrating the lambda-based approach where each adapter
 * receives a lambda that maps FilterDefinition to Condition.
 * This gives adapters complete control over PropertyRef interpretation.
 */
public class LambdaBasedFilterExample {
    
    public static void main(String[] args) {
        System.out.println("=== Lambda-Based Filter Example ===");
        
        // Create filter definitions
        Map<String, FilterDefinition> filters = createFilterDefinitions();
        FilterRequest request = new FilterRequest(filters, "f1 & f2");
        
        System.out.println("Filter Request: " + request);
        System.out.println("\nFilter Definitions:");
        filters.forEach((key, filter) -> {
            System.out.println("  " + key + ": " + filter);
            System.out.println("    PropertyRef: " + filter.getRef());
            System.out.println("    Type: " + filter.getRef().getType().getSimpleName());
            System.out.println("    Supported Operators: " + filter.getRef().getSupportedOperators());
        });
        
        System.out.println("\n=== Adapter Implementations ===");
        demonstrateAdapters();
    }
    
    private static Map<String, FilterDefinition> createFilterDefinitions() {
        Map<String, FilterDefinition> filters = new HashMap<>();
        
        filters.put("f1", new FilterDefinition(
            UserPropertyRef.USER_NAME,
            "LIKE",
            "Smith"
        ));
        
        filters.put("f2", new FilterDefinition(
            UserPropertyRef.USER_AGE,
            ">=",
            "18"
        ));
        
        filters.put("f3", new FilterDefinition(
            UserPropertyRef.USER_STATUS,
            "=",
            "ACTIVE"
        ));
        
        return filters;
    }
    
    private static void demonstrateAdapters() {
        System.out.println("JPA Adapter:");
        Context jpaContext = new JpaContextAdapter(null, null); // Simplified for demo
        demonstrateAdapter(jpaContext, "JPA");
        
        System.out.println("\nPrisma Adapter:");
        Context prismaContext = new PrismaContextAdapter();
        demonstrateAdapter(prismaContext, "Prisma");
        
        System.out.println("\nSQLAlchemy Adapter:");
        Context sqlAlchemyContext = new SqlAlchemyContextAdapter();
        demonstrateAdapter(sqlAlchemyContext, "SQLAlchemy");
    }
    
    private static void demonstrateAdapter(Context context, String adapterName) {
        // Get the condition factory lambda
        Function<FilterDefinition, Condition> conditionFactory = context.getConditionFactory();
        
        // Create sample filter definitions
        FilterDefinition nameFilter = new FilterDefinition(
            UserPropertyRef.USER_NAME,
            "LIKE",
            "Smith"
        );
        
        FilterDefinition ageFilter = new FilterDefinition(
            UserPropertyRef.USER_AGE,
            ">=",
            "18"
        );
        
        // Use the lambda to create conditions
        try {
            Condition nameCondition = conditionFactory.apply(nameFilter);
            System.out.println("  USER_NAME LIKE 'Smith' -> " + nameCondition);
            
            Condition ageCondition = conditionFactory.apply(ageFilter);
            System.out.println("  USER_AGE >= 18 -> " + ageCondition);
        } catch (Exception e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }
    
    /**
     * Example Prisma Context Adapter
     */
    public static class PrismaContextAdapter implements Context {
        
        @Override
        public Condition createCondition(FilterDefinition filterDefinition) {
            PropertyRef propertyRef = filterDefinition.getRef();
            String operator = filterDefinition.getOperator();
            Object value = filterDefinition.getValue();
            
            // Prisma-specific logic
            if (propertyRef.name().equals("USER_NAME")) {
                if (operator.equals("LIKE")) {
                    return new PrismaCondition(
                        "where: { OR: [{ first_name: { contains: '" + value + "' } }, { last_name: { contains: '" + value + "' } }] }"
                    );
                } else if (operator.equals("=")) {
                    return new PrismaCondition(
                        "where: { first_name: '" + value + "', last_name: '" + value + "' }"
                    );
                }
            } else if (propertyRef.name().equals("USER_AGE")) {
                return new PrismaCondition(
                    "where: { age: { " + getPrismaOperator(operator) + ": " + value + " } }"
                );
            } else if (propertyRef.name().equals("USER_STATUS")) {
                return new PrismaCondition(
                    "where: { status: { " + getPrismaOperator(operator) + ": '" + value + "' } }"
                );
            }
            
            return new PrismaCondition("where: {}");
        }
        
        @Override
        public Function<FilterDefinition, Condition> getConditionFactory() {
            return this::createCondition;
        }
        
        private String getPrismaOperator(String operator) {
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
     * Example SQLAlchemy Context Adapter
     */
    public static class SqlAlchemyContextAdapter implements Context {
        
        @Override
        public Condition createCondition(FilterDefinition filterDefinition) {
            PropertyRef propertyRef = filterDefinition.getRef();
            String operator = filterDefinition.getOperator();
            Object value = filterDefinition.getValue();
            
            // SQLAlchemy-specific logic
            if (propertyRef.name().equals("USER_NAME")) {
                if (operator.equals("LIKE")) {
                    return new SqlAlchemyCondition(
                        "User.query.filter(or_(User.first_name.like('%" + value + "%'), User.last_name.like('%" + value + "%')))"
                    );
                } else if (operator.equals("=")) {
                    return new SqlAlchemyCondition(
                        "User.query.filter(and_(User.first_name == '" + value + "', User.last_name == '" + value + "'))"
                    );
                }
            } else if (propertyRef.name().equals("USER_AGE")) {
                return new SqlAlchemyCondition(
                    "User.query.filter(User.age " + operator + " " + value + ")"
                );
            } else if (propertyRef.name().equals("USER_STATUS")) {
                return new SqlAlchemyCondition(
                    "User.query.filter(User.user_status " + operator + " '" + value + "')"
                );
            }
            
            return new SqlAlchemyCondition("User.query");
        }
        
        @Override
        public Function<FilterDefinition, Condition> getConditionFactory() {
            return this::createCondition;
        }
    }
    
    /**
     * Example Prisma Condition
     */
    public static class PrismaCondition implements Condition {
        private final String prismaQuery;
        
        public PrismaCondition(String prismaQuery) {
            this.prismaQuery = prismaQuery;
        }
        
        @Override
        public Condition and(Condition other) {
            return new PrismaCondition("AND(" + prismaQuery + ", " + other + ")");
        }
        
        @Override
        public Condition or(Condition other) {
            return new PrismaCondition("OR(" + prismaQuery + ", " + other + ")");
        }
        
        @Override
        public Condition not() {
            return new PrismaCondition("NOT(" + prismaQuery + ")");
        }
        
        @Override
        public String toString() {
            return prismaQuery;
        }
    }
    
    /**
     * Example SQLAlchemy Condition
     */
    public static class SqlAlchemyCondition implements Condition {
        private final String sqlAlchemyQuery;
        
        public SqlAlchemyCondition(String sqlAlchemyQuery) {
            this.sqlAlchemyQuery = sqlAlchemyQuery;
        }
        
        @Override
        public Condition and(Condition other) {
            return new SqlAlchemyCondition(sqlAlchemyQuery + ".filter(" + other + ")");
        }
        
        @Override
        public Condition or(Condition other) {
            return new SqlAlchemyCondition(sqlAlchemyQuery + ".filter(or_(" + other + "))");
        }
        
        @Override
        public Condition not() {
            return new SqlAlchemyCondition(sqlAlchemyQuery + ".filter(not_(" + this + "))");
        }
        
        @Override
        public String toString() {
            return sqlAlchemyQuery;
        }
    }
}
