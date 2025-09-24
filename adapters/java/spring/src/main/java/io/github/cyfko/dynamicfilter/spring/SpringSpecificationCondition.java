package io.github.cyfko.dynamicfilter.spring;

import io.github.cyfko.dynamicfilter.core.Condition;
import org.springframework.data.jpa.domain.Specification;

/**
 * Spring Data JPA Specification implementation of the Condition interface.
 * This adapter wraps Spring Specifications to work with the core filtering system.
 */
public class SpringSpecificationCondition implements Condition {
    
    private final Specification<?> specification;
    
    public SpringSpecificationCondition(Specification<?> specification) {
        this.specification = specification;
    }
    
    /**
     * Gets the underlying Spring Specification.
     * 
     * @return The Spring Specification
     */
    public Specification<?> getSpecification() {
        return specification;
    }
    
    @Override
    public Condition and(Condition other) {
        if (!(other instanceof SpringSpecificationCondition)) {
            throw new IllegalArgumentException("Cannot combine with non-Spring condition");
        }
        
        SpringSpecificationCondition otherSpring = (SpringSpecificationCondition) other;
        Specification<?> combinedSpec = specification.and(otherSpring.specification);
        return new SpringSpecificationCondition(combinedSpec);
    }
    
    @Override
    public Condition or(Condition other) {
        if (!(other instanceof SpringSpecificationCondition)) {
            throw new IllegalArgumentException("Cannot combine with non-Spring condition");
        }
        
        SpringSpecificationCondition otherSpring = (SpringSpecificationCondition) other;
        Specification<?> combinedSpec = specification.or(otherSpring.specification);
        return new SpringSpecificationCondition(combinedSpec);
    }
    
    @Override
    public Condition not() {
        Specification<?> negatedSpec = specification.not();
        return new SpringSpecificationCondition(negatedSpec);
    }
}
