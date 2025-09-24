package io.github.cyfko.dynamicfilter.spring;

import io.github.cyfko.dynamicfilter.core.Condition;
import io.github.cyfko.dynamicfilter.core.Context;
import io.github.cyfko.dynamicfilter.core.model.FilterDefinition;
import io.github.cyfko.dynamicfilter.core.validation.Operator;
import io.github.cyfko.dynamicfilter.core.validation.PropertyRegistry;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Spring Data JPA Specification implementation of the Context interface.
 * This adapter creates Spring Specifications from filter definitions.
 */
public class SpringSpecificationAdapter implements Context {
    
    private final String filterKey;
    private final FilterDefinition filter;
    private final PropertyRegistry propertyRegistry;
    
    public SpringSpecificationAdapter(String filterKey, 
                                    FilterDefinition filter,
                                    PropertyRegistry propertyRegistry) {
        this.filterKey = filterKey;
        this.filter = filter;
        this.propertyRegistry = propertyRegistry;
    }
    
    @Override
    public Condition getCondition(String filterKey) {
        if (!this.filterKey.equals(filterKey)) {
            throw new IllegalArgumentException("Filter key mismatch: expected " + this.filterKey + ", got " + filterKey);
        }
        
        return createSpecificationCondition();
    }
    
    private Condition createSpecificationCondition() {
        // Validate property reference
        var propertyRef = propertyRegistry.getProperty(filter.getRef());
        if (propertyRef == null) {
            throw new IllegalArgumentException("Property not found: " + filter.getRef());
        }
        
        // Validate operator
        Operator operator = Operator.fromString(filter.getOperator());
        if (operator == null) {
            throw new IllegalArgumentException("Invalid operator: " + filter.getOperator());
        }
        
        // Create specification
        Specification<?> specification = createSpecification(operator, propertyRef.getType());
        
        return new SpringSpecificationCondition(specification);
    }
    
    private Specification<?> createSpecification(Operator operator, Class<?> expectedType) {
        return (Root<?> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            Path<?> propertyPath = getPropertyPath(root, filter.getRef());
            return createPredicate(propertyPath, operator, filter.getValue(), expectedType, cb);
        };
    }
    
    private Path<?> getPropertyPath(Root<?> root, String propertyRef) {
        String[] parts = propertyRef.split("\\.");
        Path<?> path = root.get(parts[0]);
        
        for (int i = 1; i < parts.length; i++) {
            path = path.get(parts[i]);
        }
        
        return path;
    }
    
    @SuppressWarnings("unchecked")
    private Predicate createPredicate(Path<?> propertyPath, Operator operator, Object value, Class<?> expectedType, CriteriaBuilder cb) {
        switch (operator) {
            case EQUALS:
                return cb.equal(propertyPath, convertValue(value, expectedType));
                
            case NOT_EQUALS:
                return cb.notEqual(propertyPath, convertValue(value, expectedType));
                
            case GREATER_THAN:
                return cb.gt((Path<Number>) propertyPath, (Number) convertValue(value, expectedType));
                
            case GREATER_THAN_OR_EQUAL:
                return cb.ge((Path<Number>) propertyPath, (Number) convertValue(value, expectedType));
                
            case LESS_THAN:
                return cb.lt((Path<Number>) propertyPath, (Number) convertValue(value, expectedType));
                
            case LESS_THAN_OR_EQUAL:
                return cb.le((Path<Number>) propertyPath, (Number) convertValue(value, expectedType));
                
            case LIKE:
                return cb.like((Path<String>) propertyPath, "%" + value + "%");
                
            case NOT_LIKE:
                return cb.notLike((Path<String>) propertyPath, "%" + value + "%");
                
            case IN:
                if (value instanceof List) {
                    return propertyPath.in((List<?>) value);
                } else {
                    return propertyPath.in(value);
                }
                
            case NOT_IN:
                if (value instanceof List) {
                    return cb.not(propertyPath.in((List<?>) value));
                } else {
                    return cb.not(propertyPath.in(value));
                }
                
            case IS_NULL:
                return cb.isNull(propertyPath);
                
            case IS_NOT_NULL:
                return cb.isNotNull(propertyPath);
                
            case BETWEEN:
                if (value instanceof List && ((List<?>) value).size() == 2) {
                    List<?> values = (List<?>) value;
                    Comparable<?> from = (Comparable<?>) convertValue(values.get(0), expectedType);
                    Comparable<?> to = (Comparable<?>) convertValue(values.get(1), expectedType);
                    return cb.between((Path<Comparable>) propertyPath, from, to);
                } else {
                    throw new IllegalArgumentException("BETWEEN operator requires exactly 2 values");
                }
                
            case NOT_BETWEEN:
                if (value instanceof List && ((List<?>) value).size() == 2) {
                    List<?> values = (List<?>) value;
                    Comparable<?> from = (Comparable<?>) convertValue(values.get(0), expectedType);
                    Comparable<?> to = (Comparable<?>) convertValue(values.get(1), expectedType);
                    return cb.not(cb.between((Path<Comparable>) propertyPath, from, to));
                } else {
                    throw new IllegalArgumentException("NOT BETWEEN operator requires exactly 2 values");
                }
                
            default:
                throw new IllegalArgumentException("Unsupported operator: " + operator);
        }
    }
    
    private Object convertValue(Object value, Class<?> expectedType) {
        if (value == null) {
            return null;
        }
        
        if (expectedType.isAssignableFrom(value.getClass())) {
            return value;
        }
        
        // Handle string to other type conversions
        if (value instanceof String) {
            String stringValue = (String) value;
            
            if (expectedType == String.class) {
                return stringValue;
            } else if (expectedType == Integer.class || expectedType == int.class) {
                return Integer.parseInt(stringValue);
            } else if (expectedType == Long.class || expectedType == long.class) {
                return Long.parseLong(stringValue);
            } else if (expectedType == Double.class || expectedType == double.class) {
                return Double.parseDouble(stringValue);
            } else if (expectedType == Boolean.class || expectedType == boolean.class) {
                return Boolean.parseBoolean(stringValue);
            } else if (expectedType == LocalDate.class) {
                return LocalDate.parse(stringValue);
            } else if (expectedType == LocalDateTime.class) {
                return LocalDateTime.parse(stringValue);
            } else if (expectedType.isEnum()) {
                @SuppressWarnings("unchecked")
                Class<Enum> enumClass = (Class<Enum>) expectedType;
                return Enum.valueOf(enumClass, stringValue);
            }
        }
        
        throw new IllegalArgumentException("Cannot convert value " + value + " to type " + expectedType);
    }
}
