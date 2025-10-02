package io.github.cyfko.filterql.core.utils;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Advanced utilities for Java reflection and dynamic type manipulation,
 * especially for introspection and analysis of collections and class hierarchies.
 * <p>
 * This class provides functions for:
 * <ul>
 *   <li>searching for a field in a class hierarchy,</li>
 *   <li>extracting the generic type of a collection,</li>
 *   <li>determining the highest common superclass in a collection of objects,</li>
 *   <li>dynamically capturing the generic type of a parameter at runtime.</li>
 * </ul>
 *
 * <h2>Usage example</h2>
 * <pre>{@code
 * // Search for the "id" field in a class or its superclasses
 * Optional<Field> idField = ClassUtils.getAnyField(MyClass.class, "id");
 *
 * // Extract the generic type of a collection
 * Optional<Type> itemType = idField.flatMap(f -> ClassUtils.getCollectionGenericType(f, 0));
 *
 * // Determine the highest common superclass in a collection
 * Class<?> superClass = ClassUtils.getCommonSuperclass(List.of("abc", "def", "ghi")); // -> String.class
 * }</pre>
 *
 * @author Frank KOSSI
 * @since 1.1.0
 */
public final class ClassUtils {

    // Cache to improve performance of repetitive searches
    private static final Map<String, Optional<Field>> FIELD_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Class<?>> SUPERCLASS_CACHE = new ConcurrentHashMap<>();

    /**
     * Recursively searches for a named field in the class and its superclasses.
     * The returned field can be used for introspection (getType, getGenericType, getName, etc.)
     * but not for value manipulation.
     *
     * @param clazz the starting class for the search, not null
     * @param name  the name of the field to search for, not null
     * @return an {@link Optional} containing the field, or empty if no field with that name is found
     * @throws NullPointerException if clazz or name is null
     */
    public static Optional<Field> getAnyField(Class<?> clazz, String name) {
        Objects.requireNonNull(clazz, "Class must not be null");
        Objects.requireNonNull(name, "Field name must not be null");

        String cacheKey = clazz.getName() + "#" + name;
        return FIELD_CACHE.computeIfAbsent(cacheKey, key -> {
            Class<?> current = clazz;
            while (current != null && current != Object.class) {
                try {
                    Field field = current.getDeclaredField(name);
                    return Optional.of(field);
                } catch (NoSuchFieldException e) {
                    current = current.getSuperclass();
                }
            }
            return Optional.empty();
        });
    }

    /**
     * Dynamically extracts the generic type of a collection from a declared field.
     * Handles complex types including wildcards, nested parameterized types, and generic arrays.
     *
     * @param field      field representing a generic collection, not null
     * @param paramIndex index of the generic parameter ({@code 0} for List&lt;T&gt;, {@code 0} or {@code 1} for Map&lt;K,V&gt;)
     * @return an {@link Optional} containing the generic parameter type, or empty if undetermined
     * @throws NullPointerException if field is null
     * @throws IndexOutOfBoundsException if paramIndex is invalid
     */
    public static Optional<Type> getCollectionGenericType(Field field, int paramIndex) {
        Objects.requireNonNull(field, "Field must not be null");

        Type genericType = field.getGenericType();
        return extractGenericTypeAt(genericType, paramIndex);
    }

    /**
     * Extracts the generic type at a given index from a generic type.
     *
     * @param genericType the generic type to analyze
     * @param paramIndex the index of the parameter to extract
     * @return the extracted type or empty if not possible
     */
    private static Optional<Type> extractGenericTypeAt(Type genericType, int paramIndex) {
        if (!(genericType instanceof ParameterizedType)) {
            return Optional.empty();
        }

        ParameterizedType paramType = (ParameterizedType) genericType;
        Type[] typeArgs = paramType.getActualTypeArguments();

        if (paramIndex < 0 || paramIndex >= typeArgs.length) {
            throw new IndexOutOfBoundsException("Parameter index " + paramIndex + " out of bounds for " + Arrays.toString(typeArgs));
        }

        Type targetType = typeArgs[paramIndex];

        // Handle different types of generic parameters
        if (targetType instanceof WildcardType) {
            return handleWildcardType((WildcardType) targetType);
        } else if (targetType instanceof GenericArrayType) {
            return handleGenericArrayType((GenericArrayType) targetType);
        } else if (targetType instanceof TypeVariable) {
            return handleTypeVariable((TypeVariable<?>) targetType);
        }

        return Optional.of(targetType);
    }

    /**
     * Handles wildcard types (? extends Type, ? super Type).
     */
    private static Optional<Type> handleWildcardType(WildcardType wildcardType) {
        Type[] upperBounds = wildcardType.getUpperBounds();
        Type[] lowerBounds = wildcardType.getLowerBounds();

        // ? extends Type -> take the upper bound
        if (upperBounds.length > 0 && upperBounds[0] != Object.class) {
            return Optional.of(upperBounds[0]);
        }

        // ? super Type -> take the lower bound
        if (lowerBounds.length > 0) {
            return Optional.of(lowerBounds[0]);
        }

        // ? alone -> Object
        return Optional.of(Object.class);
    }

    /**
     * Handles generic arrays (T[], List<String>[]).
     */
    private static Optional<Type> handleGenericArrayType(GenericArrayType arrayType) {
        Type componentType = arrayType.getGenericComponentType();
        return Optional.of(componentType);
    }

    /**
     * Handles type variables (T, K, V, etc.).
     */
    private static Optional<Type> handleTypeVariable(TypeVariable<?> typeVar) {
        Type[] bounds = typeVar.getBounds();
        // Return the first bound (usually Object if no constraint)
        return bounds.length > 0 ? Optional.of(bounds[0]) : Optional.of(Object.class);
    }

    /**
     * Determines the highest (most common) class in the hierarchy of elements in a collection.
     * Ignores null elements. Returns {@code Object.class} if all are null.
     * Optimized version with cache for better performance.
     *
     * @param collection reference collection, not null and not empty
     * @param <T>        type of the collection elements
     * @return the common ancestor class or {@code Object.class} if all elements are null
     * @throws NullPointerException     if the collection is null
     * @throws IllegalArgumentException if the collection is empty
     */
    public static <T> Class<? super T> getCommonSuperclass(Collection<T> collection) {
        Objects.requireNonNull(collection, "Collection must not be null");
        if (collection.isEmpty()) {
            throw new IllegalArgumentException("Collection must not be empty");
        }

        // Collect all unique types
        Set<Class<?>> uniqueTypes = new LinkedHashSet<>();
        for (T element : collection) {
            if (element != null) {
                uniqueTypes.add(element.getClass());
            }
        }

        if (uniqueTypes.isEmpty()) {
            return Object.class;
        }

        if (uniqueTypes.size() == 1) {
            return (Class<? super T>) uniqueTypes.iterator().next();
        }

        // Compute common class with cache
        String cacheKey = uniqueTypes.stream()
                .map(Class::getName)
                .sorted()
                .reduce((a, b) -> a + "," + b)
                .orElse("");

        return (Class<? super T>) SUPERCLASS_CACHE.computeIfAbsent(cacheKey, key ->
                computeCommonSuperclass(uniqueTypes)
        );
    }

    /**
     * Computes the common class among several types.
     */
    private static Class<?> computeCommonSuperclass(Set<Class<?>> types) {
        Class<?> result = null;

        for (Class<?> type : types) {
            if (result == null) {
                result = type;
            } else {
                result = findCommonSuperclass(result, type);
            }
        }

        return result != null ? result : Object.class;
    }

    /**
     * Finds the common class between two specific types.
     */
    private static Class<?> findCommonSuperclass(Class<?> class1, Class<?> class2) {
        if (class1.isAssignableFrom(class2)) {
            return class1;
        }
        if (class2.isAssignableFrom(class1)) {
            return class2;
        }

        // Traverse the hierarchy of class1 until finding a common ancestor
        Class<?> current = class1;
        while (current != null && current != Object.class) {
            if (current.isAssignableFrom(class2)) {
                return current;
            }
            current = current.getSuperclass();
        }

        return Object.class;
    }

    /**
     * Checks if all non-null elements in a collection are strictly of the same concrete type.
     *
     * @param collection collection to test, not null
     * @return an {@link Optional} containing the common class, or empty if types differ or all are null
     * @throws NullPointerException if the collection is null
     */
    public static Optional<Class<?>> getCommonClass(Collection<?> collection) {
        Objects.requireNonNull(collection, "Collection must not be null");
        if (collection.isEmpty()) {
            return Optional.empty();
        }

        Class<?> commonClass = null;
        for (Object element : collection) {
            if (element == null) {
                continue;
            }

            if (commonClass == null) {
                commonClass = element.getClass();
            } else if (!commonClass.equals(element.getClass())) {
                return Optional.empty();
            }
        }

        return Optional.ofNullable(commonClass);
    }

    /**
     * Checks that all non-null elements in a collection are compatible with each other
     * with respect to a given base class.
     * <p>
     * Compatibility means there is a subclass or superclass relationship
     * between {@code baseClass} and the class of each non-null element in the collection.
     * In other words, each element must be an instance of {@code baseClass} or a parent class,
     * or conversely {@code baseClass} must be assignable to a variable of the element's type.
     * </p>
     *
     * <p>Null elements are ignored in the check.
     * If the collection is empty, the method throws an {@link IllegalStateException}.</p>
     *
     * @param baseClass  the reference class for compatibility, not null
     * @param collection the collection of elements to check, not null and not empty
     * @param <T>        the type of elements in the collection
     * @return {@code true} if all (non-null) elements are compatible with {@code baseClass}, {@code false} otherwise
     * @throws NullPointerException     if {@code baseClass} or {@code collection} is {@code null}
     * @throws IllegalStateException    if the collection is empty
     *
     * @implNote
     * This method uses {@link Class#isAssignableFrom(Class)} to test type compatibility,
     * considering inheritance and implementation relationships.
     */
    public static <T> boolean allCompatible(Class<?> baseClass, Collection<T> collection) {
        Objects.requireNonNull(baseClass, "Base class must not be null");
        Objects.requireNonNull(collection, "Collection must not be null");
        if (collection.isEmpty()) {
            throw new IllegalStateException("Collection must not be empty");
        }

        for (T element : collection) {
            if (element == null) {
                continue;
            }

            Class<?> elementClass = element.getClass();
            if (!baseClass.isAssignableFrom(elementClass) && !elementClass.isAssignableFrom(baseClass)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Clears internal caches. Useful for tests or memory management in long-running applications.
     */
    public static void clearCaches() {
        FIELD_CACHE.clear();
        SUPERCLASS_CACHE.clear();
    }

    /**
     * Returns cache statistics for monitoring.
     *
     * @return a map containing the cache sizes
     */
    public static Map<String, Integer> getCacheStats() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("fieldCacheSize", FIELD_CACHE.size());
        stats.put("superclassCacheSize", SUPERCLASS_CACHE.size());
        return Collections.unmodifiableMap(stats);
    }

    /**
     * Utility method to obtain the {@link Class} object for the generic type {@code T}.
     *
     * @param <T> the generic type
     * @return the {@link Class} of {@code T}
     */
    public static <T> Class<T> getClazz() {
        return (new TypeReference<T>() {}).getTypeClass();
    }

    /**
     * Utility class for dynamically capturing the actual type of a generic parameter at instantiation.
     * Secure version against multiple inheritance attacks and with enhanced validation.
     * <p>
     * Now handles complex types and offers better security.
     * </p>
     *
     * @param <T> generic type to capture
     */
    public static abstract class TypeReference<T> {
        private final Class<T> typeClass;
        private final Type type;

        @SuppressWarnings("unchecked")
        protected TypeReference() {
            // Security validation against multiple inheritance
            Class<?> thisClass = getClass();
            if (!thisClass.getName().contains("$") && thisClass != TypeReference.class) {
                throw new IllegalStateException("TypeReference ne doit pas être étendu par une classe nommée. Utilisez une classe anonyme.");
            }

            Type superClass = thisClass.getGenericSuperclass();
            if (!(superClass instanceof ParameterizedType)) {
                throw new IllegalArgumentException("TypeReference doit être instancié avec un type générique explicite");
            }

            ParameterizedType paramType = (ParameterizedType) superClass;
            Type[] typeArgs = paramType.getActualTypeArguments();

            if (typeArgs.length != 1) {
                throw new IllegalArgumentException("TypeReference doit avoir exactement un paramètre de type");
            }

            this.type = typeArgs[0];

            // Resolution of the actual type
            if (type instanceof Class<?>) {
                this.typeClass = (Class<T>) type;
            } else if (type instanceof ParameterizedType) {
                Type rawType = ((ParameterizedType) type).getRawType();
                if (rawType instanceof Class<?>) {
                    this.typeClass = (Class<T>) rawType;
                } else {
                    throw new IllegalArgumentException("Type générique trop complexe: " + type);
                }
            } else if (type instanceof WildcardType) {
                WildcardType wildcard = (WildcardType) type;
                Type[] upperBounds = wildcard.getUpperBounds();
                if (upperBounds.length > 0 && upperBounds[0] instanceof Class<?>) {
                    this.typeClass = (Class<T>) upperBounds[0];
                } else {
                    this.typeClass = (Class<T>) Object.class;
                }
            } else {
                throw new IllegalArgumentException("Type générique non supporté: " + type.getClass().getName());
            }
        }

    /**
     * Returns the captured class corresponding to the generic type T.
     *
     * @return the class representing T
     */
        public final Class<T> getTypeClass() {
            return this.typeClass;
        }

    /**
     * Returns the captured {@link Type}, such as a parameterized type or a concrete class.
     *
     * @return an instance of {@link Type} corresponding to T
     */
        public final Type getType() {
            return this.type;
        }

    /**
     * Checks if the captured type is assignable from a given class.
     *
     * @param clazz the class to test
     * @return true if compatible
     */
        public final boolean isAssignableFrom(Class<?> clazz) {
            return this.typeClass.isAssignableFrom(clazz);
        }

    /**
     * Checks if the captured type is an instance of a given class.
     *
     * @param clazz the class to test
     * @return true if instance
     */
        public final boolean isInstanceOf(Class<?> clazz) {
            return clazz.isAssignableFrom(this.typeClass);
        }

        @Override
        public final boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof TypeReference)) return false;
            TypeReference<?> other = (TypeReference<?>) obj;
            return Objects.equals(this.type, other.type);
        }

        @Override
        public final int hashCode() {
            return Objects.hash(type);
        }

        @Override
        public final String toString() {
            return "TypeReference<" + type.getTypeName() + ">";
        }
    }
}