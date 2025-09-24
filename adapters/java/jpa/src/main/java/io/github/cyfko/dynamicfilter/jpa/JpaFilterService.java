package io.github.cyfko.dynamicfilter.jpa;

import io.github.cyfko.dynamicfilter.core.Condition;
import io.github.cyfko.dynamicfilter.core.FilterExecutor;
import io.github.cyfko.dynamicfilter.core.FilterTree;
import io.github.cyfko.dynamicfilter.core.Parser;
import io.github.cyfko.dynamicfilter.core.model.FilterRequest;
import io.github.cyfko.dynamicfilter.core.validation.PropertyRegistry;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

/**
 * Main service class for JPA-based dynamic filtering.
 * This class orchestrates the entire filtering pipeline using JPA adapters.
 */
public class JpaFilterService {
    
    private final Parser parser;
    private final PropertyRegistry propertyRegistry;
    private final EntityManager entityManager;
    
    public JpaFilterService(Parser parser, PropertyRegistry propertyRegistry, EntityManager entityManager) {
        this.parser = parser;
        this.propertyRegistry = propertyRegistry;
        this.entityManager = entityManager;
    }
    
    /**
     * Executes a filter request against the specified entity class.
     * 
     * @param <T> The type of entities being filtered
     * @param filterRequest The filter request containing filters and DSL expression
     * @param entityClass The class of entities to filter
     * @return A list of entities that match the filter criteria
     */
    public <T> List<T> executeFilter(FilterRequest filterRequest, Class<T> entityClass) {
        // Parse DSL expression
        FilterTree filterTree = parser.parse(filterRequest.getCombineWith());
        
        // Create JPA context
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(entityClass);
        Root<T> root = query.from(entityClass);
        
        JpaContextAdapter context = new JpaContextAdapter(
            filterRequest.getFilters(),
            propertyRegistry,
            cb,
            root
        );
        
        // Generate global condition
        Condition globalCondition = filterTree.generate(context);
        
        // Execute filter
        FilterExecutor executor = new JpaFilterExecutor(entityManager);
        return executor.execute(globalCondition, entityClass);
    }
    
    /**
     * Creates a CriteriaQuery with the applied filter conditions.
     * This method is useful when you need to customize the query further.
     * 
     * @param <T> The type of entities being filtered
     * @param filterRequest The filter request containing filters and DSL expression
     * @param entityClass The class of entities to filter
     * @return A CriteriaQuery with the filter conditions applied
     */
    public <T> CriteriaQuery<T> createFilteredQuery(FilterRequest filterRequest, Class<T> entityClass) {
        // Parse DSL expression
        FilterTree filterTree = parser.parse(filterRequest.getCombineWith());
        
        // Create JPA context
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(entityClass);
        Root<T> root = query.from(entityClass);
        
        JpaContextAdapter context = new JpaContextAdapter(
            filterRequest.getFilters(),
            propertyRegistry,
            cb,
            root
        );
        
        // Generate global condition
        Condition globalCondition = filterTree.generate(context);
        
        // Apply condition to query
        if (globalCondition instanceof JpaConditionAdapter) {
            JpaConditionAdapter jpaCondition = (JpaConditionAdapter) globalCondition;
            query.where(jpaCondition.getPredicate());
        }
        
        return query;
    }
}
