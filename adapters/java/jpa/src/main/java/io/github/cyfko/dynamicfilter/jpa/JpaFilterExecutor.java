package io.github.cyfko.dynamicfilter.jpa;

import io.github.cyfko.dynamicfilter.core.Condition;
import io.github.cyfko.dynamicfilter.core.FilterExecutor;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

/**
 * JPA implementation of the FilterExecutor interface.
 * This adapter executes filter conditions using JPA CriteriaQuery.
 */
public class JpaFilterExecutor<E,R> implements FilterExecutor<R> {
    
    private final JpaContextAdapter<E,?> context;
    
    public JpaFilterExecutor(JpaContextAdapter<E,?> context) {
        this.context = context;
    }
    
    @Override
    public R execute(Condition globalCondition) {

        // 1. Vérifier que la condition est bien un JpaConditionAdapter
        if (!(globalCondition instanceof JpaConditionAdapter<?>)) {
            throw new IllegalArgumentException("Condition must be a JpaConditionAdapter");
        }

        // 2. Caster vers JpaConditionAdapter pour accéder au Predicate
        @SuppressWarnings("unchecked")
        JpaConditionAdapter<E> jpaCondition = (JpaConditionAdapter<E>) globalCondition;

        // 3. Utiliser la query et le root du contexte (déjà configurés)
        CriteriaQuery<E> query = context.getQuery();
        query.where(jpaCondition.getPredicate());

        // 4. Exécuter la requête
        EntityManager entityManager = context.getEntityManager();
        TypedQuery<E> typedQuery = entityManager.createQuery(query);
        List<E> results = typedQuery.getResultList();

        // 5. Retourner le résultat (cast vers R)
        return (R) results;
    }
}
