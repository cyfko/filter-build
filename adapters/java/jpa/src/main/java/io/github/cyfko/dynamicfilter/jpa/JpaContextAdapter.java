package io.github.cyfko.dynamicfilter.jpa;

import io.github.cyfko.dynamicfilter.core.Condition;
import io.github.cyfko.dynamicfilter.core.Context;
import io.github.cyfko.dynamicfilter.core.model.FilterDefinition;
import io.github.cyfko.dynamicfilter.core.validation.Operator;
import io.github.cyfko.dynamicfilter.core.validation.PropertyRegistry;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

/**
 * JPA implementation of the Context interface.
 * This adapter creates JPA conditions from filter definitions using CriteriaBuilder.
 */
public class JpaContextAdapter implements Context {
    
    private final Map<String, FilterDefinition> filters;
    private final PropertyRegistry propertyRegistry;
    private final CriteriaBuilder criteriaBuilder;
    private final Root<?> root;
    
    public JpaContextAdapter(Map<String, FilterDefinition> filters, 
                           PropertyRegistry propertyRegistry,
                           CriteriaBuilder criteriaBuilder, 
                           Root<?> root) {
        this.filters = filters;
        this.propertyRegistry = propertyRegistry;
        this.criteriaBuilder = criteriaBuilder;
        this.root = root;
    }
    
    @Override
    public Condition getCondition(String filterKey) {
        FilterDefinition filter = filters.get(filterKey);
        if (filter == null) {
            throw new IllegalArgumentException("No filter found for key: " + filterKey);
        }
        
        return createCondition(filter);
    }
    
    private Condition createCondition(FilterDefinition filter) {
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
        
        // Get property path
        Path<?> propertyPath = getPropertyPath(filter.getRef());
        
        // Create predicate based on operator
        Predicate predicate = createPredicate(propertyPath, operator, filter.getValue(), propertyRef.getType());
        
        return new JpaConditionAdapter(predicate, criteriaBuilder);
    }
    
    private Path<?> getPropertyPath(String propertyRef) {
        String[] parts = propertyRef.split("\\.");
        Path<?> path = root.get(parts[0]);
        
        for (int i = 1; i < parts.length; i++) {
            path = path.get(parts[i]);
        }
        
        return path;
    }
    
    @SuppressWarnings("unchecked")
    private Predicate createPredicate(Path<?> propertyPath, Operator operator, Object value, Class<?> expectedType) {
        switch (operator) {
            case EQUALS:
                return criteriaBuilder.equal(propertyPath, convertValue(value, expectedType));
                
            case NOT_EQUALS:
                return criteriaBuilder.notEqual(propertyPath, convertValue(value, expectedType));
                
            case GREATER_THAN:
                return criteriaBuilder.gt((Path<Number>) propertyPath, (Number) convertValue(value, expectedType));
                
            case GREATER_THAN_OR_EQUAL:
                return criteriaBuilder.ge((Path<Number>) propertyPath, (Number) convertValue(value, expectedType));
                
            case LESS_THAN:
                return criteriaBuilder.lt((Path<Number>) propertyPath, (Number) convertValue(value, expectedType));
                
            case LESS_THAN_OR_EQUAL:
                return criteriaBuilder.le((Path<Number>) propertyPath, (Number) convertValue(value, expectedType));
                
            case LIKE:
                return criteriaBuilder.like((Path<String>) propertyPath, "%" + value + "%");
                
            case NOT_LIKE:
                return criteriaBuilder.notLike((Path<String>) propertyPath, "%" + value + "%");
                
            case IN:
                if (value instanceof List) {
                    return propertyPath.in((List<?>) value);
                } else {
                    return propertyPath.in(value);
                }
                
            case NOT_IN:
                if (value instanceof List) {
                    return criteriaBuilder.not(propertyPath.in((List<?>) value));
                } else {
                    return criteriaBuilder.not(propertyPath.in(value));
                }
                
            case IS_NULL:
                return criteriaBuilder.isNull(propertyPath);
                
            case IS_NOT_NULL:
                return criteriaBuilder.isNotNull(propertyPath);
                
            case BETWEEN:
                if (value instanceof List && ((List<?>) value).size() == 2) {
                    List<?> values = (List<?>) value;
                    Comparable<?> from = (Comparable<?>) convertValue(values.get(0), expectedType);
                    Comparable<?> to = (Comparable<?>) convertValue(values.get(1), expectedType);
                    return criteriaBuilder.between((Path<Comparable>) propertyPath, from, to);
                } else {
                    throw new IllegalArgumentException("BETWEEN operator requires exactly 2 values");
                }
                
            case NOT_BETWEEN:
                if (value instanceof List && ((List<?>) value).size() == 2) {
                    List<?> values = (List<?>) value;
                    Comparable<?> from = (Comparable<?>) convertValue(values.get(0), expectedType);
                    Comparable<?> to = (Comparable<?>) convertValue(values.get(1), expectedType);
                    return criteriaBuilder.not(criteriaBuilder.between((Path<Comparable>) propertyPath, from, to));
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
