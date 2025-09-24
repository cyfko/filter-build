package io.github.cyfko.dynamicfilter.core.validation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Registry for managing allowed property references.
 * This class provides security by ensuring only whitelisted properties can be used in filters.
 */
public class PropertyRegistry {
    
    private final Map<String, PropertyRef> properties;
    
    public PropertyRegistry() {
        this.properties = new HashMap<>();
    }
    
    /**
     * Registers a property reference.
     * 
     * @param name The property name
     * @param type The property type
     * @param nullable Whether the property can be null
     */
    public void registerProperty(String name, Class<?> type, boolean nullable) {
        properties.put(name, new PropertyRef(name, type, nullable));
    }
    
    /**
     * Registers a property reference (nullable by default).
     * 
     * @param name The property name
     * @param type The property type
     */
    public void registerProperty(String name, Class<?> type) {
        registerProperty(name, type, true);
    }
    
    /**
     * Gets a property reference by name.
     * 
     * @param name The property name
     * @return The property reference, or null if not found
     */
    public PropertyRef getProperty(String name) {
        return properties.get(name);
    }
    
    /**
     * Checks if a property is registered.
     * 
     * @param name The property name
     * @return true if the property is registered, false otherwise
     */
    public boolean hasProperty(String name) {
        return properties.containsKey(name);
    }
    
    /**
     * Gets all registered property names.
     * 
     * @return A set of all registered property names
     */
    public Set<String> getPropertyNames() {
        return properties.keySet();
    }
    
    /**
     * Gets all registered property references.
     * 
     * @return A map of all registered property references
     */
    public Map<String, PropertyRef> getAllProperties() {
        return new HashMap<>(properties);
    }
    
    /**
     * Clears all registered properties.
     */
    public void clear() {
        properties.clear();
    }
    
    /**
     * Gets the number of registered properties.
     * 
     * @return The number of registered properties
     */
    public int size() {
        return properties.size();
    }
}
