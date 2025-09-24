package io.github.cyfko.dynamicfilter.core.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PropertyRegistryTest {
    
    private PropertyRegistry registry;
    
    @BeforeEach
    void setUp() {
        registry = new PropertyRegistry();
    }
    
    @Test
    void testRegisterProperty() {
        registry.registerProperty("name", String.class);
        
        assertTrue(registry.hasProperty("name"));
        PropertyRef ref = registry.getProperty("name");
        assertNotNull(ref);
        assertEquals("name", ref.getName());
        assertEquals(String.class, ref.getType());
        assertTrue(ref.isNullable());
    }
    
    @Test
    void testRegisterPropertyWithNullable() {
        registry.registerProperty("id", Long.class, false);
        
        PropertyRef ref = registry.getProperty("id");
        assertNotNull(ref);
        assertEquals("id", ref.getName());
        assertEquals(Long.class, ref.getType());
        assertFalse(ref.isNullable());
    }
    
    @Test
    void testGetNonExistentProperty() {
        assertFalse(registry.hasProperty("nonexistent"));
        assertNull(registry.getProperty("nonexistent"));
    }
    
    @Test
    void testGetPropertyNames() {
        registry.registerProperty("name", String.class);
        registry.registerProperty("age", Integer.class);
        registry.registerProperty("active", Boolean.class);
        
        Set<String> names = registry.getPropertyNames();
        assertEquals(3, names.size());
        assertTrue(names.contains("name"));
        assertTrue(names.contains("age"));
        assertTrue(names.contains("active"));
    }
    
    @Test
    void testClear() {
        registry.registerProperty("name", String.class);
        assertTrue(registry.hasProperty("name"));
        
        registry.clear();
        assertFalse(registry.hasProperty("name"));
        assertEquals(0, registry.size());
    }
    
    @Test
    void testSize() {
        assertEquals(0, registry.size());
        
        registry.registerProperty("name", String.class);
        assertEquals(1, registry.size());
        
        registry.registerProperty("age", Integer.class);
        assertEquals(2, registry.size());
    }
    
    @Test
    void testGetAllProperties() {
        registry.registerProperty("name", String.class);
        registry.registerProperty("age", Integer.class);
        
        var allProperties = registry.getAllProperties();
        assertEquals(2, allProperties.size());
        assertTrue(allProperties.containsKey("name"));
        assertTrue(allProperties.containsKey("age"));
        
        // Verify it's a copy, not the original
        allProperties.clear();
        assertEquals(2, registry.size());
    }
}
