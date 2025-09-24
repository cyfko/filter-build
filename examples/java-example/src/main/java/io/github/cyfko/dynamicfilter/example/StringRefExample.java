package io.github.cyfko.dynamicfilter.example;

import io.github.cyfko.dynamicfilter.core.model.FilterDefinition;
import io.github.cyfko.dynamicfilter.core.model.FilterRequest;
import io.github.cyfko.dynamicfilter.core.validation.PropertyRef;

import java.util.HashMap;
import java.util.Map;

/**
 * Example demonstrating the String ref approach in FilterDefinition.
 * This shows how String refs are resolved to PropertyRef enums by adapters.
 * This approach is more flexible for JSON serialization/deserialization.
 */
public class StringRefExample {
    
    public static void main(String[] args) {
        System.out.println("=== String Ref Example ===");
        
        // Create filter definitions with String refs
        Map<String, FilterDefinition> filters = createFilterDefinitions();
        FilterRequest request = new FilterRequest(filters, "f1 & f2");
        
        System.out.println("Filter Request: " + request);
        System.out.println("\nFilter Definitions:");
        filters.forEach((key, filter) -> {
            System.out.println("  " + key + ": " + filter);
            System.out.println("    Ref: '" + filter.getRef() + "' (String)");
            System.out.println("    Operator: '" + filter.getOperator() + "'");
            System.out.println("    Value: " + filter.getValue());
        });
        
        System.out.println("\n=== Adapter Resolution ===");
        demonstrateAdapterResolution();
        
        System.out.println("\n=== JSON Compatibility ===");
        demonstrateJsonCompatibility();
    }
    
    private static Map<String, FilterDefinition> createFilterDefinitions() {
        Map<String, FilterDefinition> filters = new HashMap<>();
        
        // String refs - easy to serialize/deserialize
        filters.put("f1", new FilterDefinition(
            "USER_NAME",  // String ref
            "LIKE",
            "Smith"
        ));
        
        filters.put("f2", new FilterDefinition(
            "USER_AGE",   // String ref
            ">=",
            "18"
        ));
        
        filters.put("f3", new FilterDefinition(
            "USER_STATUS", // String ref
            "=",
            "ACTIVE"
        ));
        
        return filters;
    }
    
    private static void demonstrateAdapterResolution() {
        System.out.println("JPA Adapter Resolution:");
        System.out.println("  'USER_NAME' -> UserPropertyRef.USER_NAME");
        System.out.println("  'USER_AGE' -> UserPropertyRef.USER_AGE");
        System.out.println("  'USER_STATUS' -> UserPropertyRef.USER_STATUS");
        
        System.out.println("\nPrisma Adapter Resolution:");
        System.out.println("  'USER_NAME' -> UserPropertyRef.USER_NAME");
        System.out.println("  'USER_AGE' -> UserPropertyRef.USER_AGE");
        System.out.println("  'USER_STATUS' -> UserPropertyRef.USER_STATUS");
        
        System.out.println("\nSQLAlchemy Adapter Resolution:");
        System.out.println("  'USER_NAME' -> UserPropertyRef.USER_NAME");
        System.out.println("  'USER_AGE' -> UserPropertyRef.USER_AGE");
        System.out.println("  'USER_STATUS' -> UserPropertyRef.USER_STATUS");
    }
    
    private static void demonstrateJsonCompatibility() {
        System.out.println("JSON Input:");
        System.out.println("  {");
        System.out.println("    \"filters\": {");
        System.out.println("      \"f1\": { \"ref\": \"USER_NAME\", \"operator\": \"LIKE\", \"value\": \"Smith\" },");
        System.out.println("      \"f2\": { \"ref\": \"USER_AGE\", \"operator\": \">=\", \"value\": \"18\" }");
        System.out.println("    },");
        System.out.println("    \"combineWith\": \"f1 & f2\"");
        System.out.println("  }");
        
        System.out.println("\nDeserialization:");
        System.out.println("  String ref = \"USER_NAME\"");
        System.out.println("  FilterDefinition filter = new FilterDefinition(ref, \"LIKE\", \"Smith\")");
        System.out.println("  // Adapter resolves \"USER_NAME\" -> UserPropertyRef.USER_NAME");
    }
    
    /**
     * Example of how adapters resolve String refs to PropertyRef enums.
     */
    public static class AdapterResolutionExamples {
        
        /**
         * JPA Adapter resolution logic
         */
        public static PropertyRef resolvePropertyRefJpa(String ref) {
            // Search through UserPropertyRef
            for (UserPropertyRef userProp : UserPropertyRef.values()) {
                if (userProp.name().equals(ref)) {
                    return userProp;
                }
            }
            
            // Search through ProductPropertyRef
            for (ProductPropertyRef productProp : ProductPropertyRef.values()) {
                if (productProp.name().equals(ref)) {
                    return productProp;
                }
            }
            
            return null;
        }
        
        /**
         * Prisma Adapter resolution logic
         */
        public static PropertyRef resolvePropertyRefPrisma(String ref) {
            // Could use a registry or different logic
            return resolvePropertyRefJpa(ref); // Same logic for now
        }
        
        /**
         * SQLAlchemy Adapter resolution logic
         */
        public static PropertyRef resolvePropertyRefSqlAlchemy(String ref) {
            // Could use a registry or different logic
            return resolvePropertyRefJpa(ref); // Same logic for now
        }
    }
    
    /**
     * Example of how to create FilterDefinition from JSON-like data.
     */
    public static class JsonToFilterDefinition {
        
        public static FilterDefinition fromJsonData(String ref, String operator, Object value) {
            return new FilterDefinition(ref, operator, value);
        }
        
        public static Map<String, FilterDefinition> fromJsonFilters(Map<String, Map<String, Object>> jsonFilters) {
            Map<String, FilterDefinition> filters = new HashMap<>();
            
            for (Map.Entry<String, Map<String, Object>> entry : jsonFilters.entrySet()) {
                String filterKey = entry.getKey();
                Map<String, Object> filterData = entry.getValue();
                
                String ref = (String) filterData.get("ref");
                String operator = (String) filterData.get("operator");
                Object value = filterData.get("value");
                
                filters.put(filterKey, new FilterDefinition(ref, operator, value));
            }
            
            return filters;
        }
    }
    
    /**
     * Example of how to create FilterDefinition from PropertyRef enum.
     */
    public static class PropertyRefToFilterDefinition {
        
        public static FilterDefinition fromPropertyRef(PropertyRef propertyRef, String operator, Object value) {
            return new FilterDefinition(propertyRef.name(), operator, value);
        }
        
        public static Map<String, FilterDefinition> fromPropertyRefs(Map<String, PropertyRef> propertyRefs, 
                                                                   Map<String, String> operators, 
                                                                   Map<String, Object> values) {
            Map<String, FilterDefinition> filters = new HashMap<>();
            
            for (Map.Entry<String, PropertyRef> entry : propertyRefs.entrySet()) {
                String filterKey = entry.getKey();
                PropertyRef propertyRef = entry.getValue();
                String operator = operators.get(filterKey);
                Object value = values.get(filterKey);
                
                filters.put(filterKey, new FilterDefinition(propertyRef.name(), operator, value));
            }
            
            return filters;
        }
    }
}
