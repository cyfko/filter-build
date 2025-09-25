package io.github.cyfko.filterql.core.model;

import io.github.cyfko.filterql.core.validation.Operator;
import io.github.cyfko.filterql.core.validation.PropertyRef;

/**
 * Represents a single filter definition with property reference, operator, and value.
 * The ref must be an enum implementing PropertyRef for type safety and performance.
 *
 * @param <P> type de la propriété de référence (enum implémentant {@link PropertyRef})
 */
public class FilterDefinition<P extends Enum<P> & PropertyRef> {
    
    private final P ref;
    private final Operator operator;
    private final Object value;
    
    public FilterDefinition(P ref, Operator operator, Object value) {
        this.ref = ref;
        this.operator = operator;
        this.value = value;
    }
    
    public P getRef() {
        return ref;
    }
    
    public Operator getOperator() {
        return operator;
    }
    
    public Object getValue() {
        return value;
    }
    
    @Override
    public String toString() {
        return String.format("FilterDefinition{ref=%s, operator=%s, value=%s}", 
                           ref, operator, value);
    }
}
