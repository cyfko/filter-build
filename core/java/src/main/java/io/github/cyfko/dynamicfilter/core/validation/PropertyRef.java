package io.github.cyfko.dynamicfilter.core.validation;

/**
 * Represents a validated property reference that maps to an actual entity property.
 * This class ensures that only whitelisted properties can be used in filters.
 */
public class PropertyRef {
    
    private final String name;
    private final Class<?> type;
    private final boolean nullable;
    
    public PropertyRef(String name, Class<?> type, boolean nullable) {
        this.name = name;
        this.type = type;
        this.nullable = nullable;
    }
    
    public PropertyRef(String name, Class<?> type) {
        this(name, type, true);
    }
    
    public String getName() {
        return name;
    }
    
    public Class<?> getType() {
        return type;
    }
    
    public boolean isNullable() {
        return nullable;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PropertyRef that = (PropertyRef) obj;
        return name.equals(that.name);
    }
    
    @Override
    public int hashCode() {
        return name.hashCode();
    }
    
    @Override
    public String toString() {
        return String.format("PropertyRef{name='%s', type=%s, nullable=%s}", 
                           name, type.getSimpleName(), nullable);
    }
}
