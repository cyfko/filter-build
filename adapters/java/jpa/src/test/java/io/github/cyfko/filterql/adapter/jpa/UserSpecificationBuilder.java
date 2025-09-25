package io.github.cyfko.filterql.adapter.jpa;

import io.github.cyfko.filterql.core.validation.Operator;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.Arrays;

/**
 * Concrete implementation of SpecificationBuilder for UserEntity testing.
 */
public class UserSpecificationBuilder implements SpecificationBuilder<UserEntity, UserPropertyRef> {
    
    @Override
    public Specification<UserEntity> build(UserPropertyRef ref, Operator op, Object value) {
        return (root, query, criteriaBuilder) -> createPredicate(ref, op, value, root, criteriaBuilder);
    }
    
    private Predicate createPredicate(UserPropertyRef ref, Operator op, Object value, Root<UserEntity> root, CriteriaBuilder criteriaBuilder) {
        Path<?> path = getPath(ref, root);
        
        switch (op) {
            case EQUALS:
                return criteriaBuilder.equal(path, value);
            case NOT_EQUALS:
                return criteriaBuilder.notEqual(path, value);
            case GREATER_THAN:
                return criteriaBuilder.gt((Path<Number>) path, (Number) value);
            case GREATER_THAN_OR_EQUAL:
                return criteriaBuilder.ge((Path<Number>) path, (Number) value);
            case LESS_THAN:
                return criteriaBuilder.lt((Path<Number>) path, (Number) value);
            case LESS_THAN_OR_EQUAL:
                return criteriaBuilder.le((Path<Number>) path, (Number) value);
            case LIKE:
                return criteriaBuilder.like((Path<String>) path, (String) value);
            case NOT_LIKE:
                return criteriaBuilder.not(criteriaBuilder.like((Path<String>) path, (String) value));
            case IN:
                return path.in(Arrays.asList((Object[]) value));
            case NOT_IN:
                return criteriaBuilder.not(path.in(Arrays.asList((Object[]) value)));
            case IS_NULL:
                return criteriaBuilder.isNull(path);
            case IS_NOT_NULL:
                return criteriaBuilder.isNotNull(path);
            default:
                throw new IllegalArgumentException("Unsupported operator: " + op);
        }
    }
    
    private Path<?> getPath(UserPropertyRef ref, Root<UserEntity> root) {
        switch (ref) {
            case USER_NAME:
                return root.get("name");
            case USER_AGE:
                return root.get("age");
            case USER_EMAIL:
                return root.get("email");
            case USER_STATUS:
                return root.get("status");
            default:
                throw new IllegalArgumentException("Unknown property: " + ref);
        }
    }
}