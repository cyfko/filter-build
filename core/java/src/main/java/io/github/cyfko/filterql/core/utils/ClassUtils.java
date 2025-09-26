package io.github.cyfko.filterql.core.utils;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utilitaires avancés pour la réflexion Java et la manipulation dynamique de types,
 * notamment dans le cadre de l'introspection et de l'analyse des collections et
 * des hiérarchies de classes.
 * <p>
 * Cette classe propose des fonctions pour :
 * <ul>
 *   <li>rechercher un champ dans une hiérarchie de classes,</li>
 *   <li>extraire le type générique d'une collection,</li>
 *   <li>déterminer la classe commune la plus haute dans une collection d'objets,</li>
 *   <li>capturer dynamiquement le type générique d'un paramètre à l'exécution.</li>
 * </ul>
 *
 * <h2>Exemple d'utilisation</h2>
 * <pre>{@code
 * // Recherche du champ "id" dans une classe ou ses superclasses
 * Optional<Field> idField = ClassUtils.getAnyField(MyClass.class, "id");
 *
 * // Extraction du type générique d'une collection
 * Optional<Type> itemType = idField.flatMap(f -> ClassUtils.getCollectionGenericType(f, 0));
 *
 * // Déterminer la plus haute classe commune d'une collection
 * Class<?> superClass = ClassUtils.getCommonSuperclass(List.of("abc", "def", "ghi")); // -> String.class
 * }</pre>
 *
 * @author Frank KOSSI
 * @since 1.1.0
 */
public class ClassUtils {

    // Cache pour améliorer les performances des recherches répétitives
    private static final Map<String, Optional<Field>> FIELD_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Class<?>> SUPERCLASS_CACHE = new ConcurrentHashMap<>();

    /**
     * Recherche récursive d'un champ nommé dans la classe et ses superclasses.
     * Le champ retourné est utilisable pour l'introspection (getType, getGenericType, getName, etc.)
     * mais pas pour la manipulation des valeurs.
     *
     * @param clazz la classe de départ pour la recherche, non nulle
     * @param name  le nom du champ recherché, non nul
     * @return un {@link Optional} contenant le champ, ou vide si aucun champ nommé n'est trouvé
     * @throws NullPointerException si clazz ou name est nul
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
     * Extrait dynamiquement le type générique d'une collection à partir d'un champ déclaré.
     * Gère les types complexes incluant les wildcards, les types paramétrés imbriqués et les tableaux génériques.
     *
     * @param field      champ représentant une collection générique, non nul
     * @param paramIndex index du paramètre générique ({@code 0} pour List&lt;T&gt;, {@code 0} ou {@code 1} pour Map&lt;K,V&gt;)
     * @return un {@link Optional} contenant le type du paramètre générique, ou vide si indéterminé
     * @throws NullPointerException si field est nul
     * @throws IndexOutOfBoundsException si paramIndex est invalide
     */
    public static Optional<Type> getCollectionGenericType(Field field, int paramIndex) {
        Objects.requireNonNull(field, "Field must not be null");

        Type genericType = field.getGenericType();
        return extractGenericTypeAt(genericType, paramIndex);
    }

    /**
     * Extrait le type générique à un index donné depuis un type générique.
     *
     * @param genericType le type générique à analyser
     * @param paramIndex l'index du paramètre à extraire
     * @return le type extrait ou empty si impossible
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

        // Gestion des différents types de paramètres génériques
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
     * Gère les types wildcard (? extends Type, ? super Type).
     */
    private static Optional<Type> handleWildcardType(WildcardType wildcardType) {
        Type[] upperBounds = wildcardType.getUpperBounds();
        Type[] lowerBounds = wildcardType.getLowerBounds();

        // ? extends Type -> prendre la borne supérieure
        if (upperBounds.length > 0 && upperBounds[0] != Object.class) {
            return Optional.of(upperBounds[0]);
        }

        // ? super Type -> prendre la borne inférieure
        if (lowerBounds.length > 0) {
            return Optional.of(lowerBounds[0]);
        }

        // ? seul -> Object
        return Optional.of(Object.class);
    }

    /**
     * Gère les tableaux génériques (T[], List<String>[]).
     */
    private static Optional<Type> handleGenericArrayType(GenericArrayType arrayType) {
        Type componentType = arrayType.getGenericComponentType();
        return Optional.of(componentType);
    }

    /**
     * Gère les variables de type (T, K, V, etc.).
     */
    private static Optional<Type> handleTypeVariable(TypeVariable<?> typeVar) {
        Type[] bounds = typeVar.getBounds();
        // Retourner la première borne (généralement Object si pas de contrainte)
        return bounds.length > 0 ? Optional.of(bounds[0]) : Optional.of(Object.class);
    }

    /**
     * Détermine la classe la plus haute (la plus commune) dans la hiérarchie des éléments d'une collection.
     * Ignore les éléments nuls. Retourne {@code Object.class} si tous sont nuls.
     * Version optimisée avec cache pour améliorer les performances.
     *
     * @param collection collection de référence, non nulle et non vide
     * @param <T>        type des éléments de la collection
     * @return la classe ancêtre commune ou {@code Object.class} si tous les éléments sont nuls
     * @throws NullPointerException     si la collection est nulle
     * @throws IllegalArgumentException si la collection est vide
     */
    public static <T> Class<? super T> getCommonSuperclass(Collection<T> collection) {
        Objects.requireNonNull(collection, "Collection must not be null");
        if (collection.isEmpty()) {
            throw new IllegalArgumentException("Collection must not be empty");
        }

        // Collecter tous les types uniques
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

        // Calculer la classe commune avec cache
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
     * Calcule la classe commune entre plusieurs types.
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
     * Trouve la classe commune entre deux types spécifiques.
     */
    private static Class<?> findCommonSuperclass(Class<?> class1, Class<?> class2) {
        if (class1.isAssignableFrom(class2)) {
            return class1;
        }
        if (class2.isAssignableFrom(class1)) {
            return class2;
        }

        // Remonter la hiérarchie de class1 jusqu'à trouver un ancêtre commun
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
     * Vérifie si tous les éléments non nuls d'une collection sont strictement du même type concret.
     *
     * @param collection collection à tester, non nulle
     * @return un {@link Optional} contenant la classe commune, ou vide si types différents ou tous nuls
     * @throws NullPointerException si la collection est nulle
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
     * Vérifie que tous les éléments non nuls d'une collection sont compatibles entre eux
     * par rapport à une classe de base donnée.
     * <p>
     * La compatibilité signifie qu'il existe une relation de sous-classe ou de super-classe
     * entre la {@code baseClass} et la classe de chaque élément non nul de la collection.
     * Autrement dit, chaque élément doit être une instance de {@code baseClass} ou une classe parente,
     * ou inversement {@code baseClass} doit pouvoir être affectée à une variable du type de l'élément.
     * </p>
     *
     * <p>Les éléments nuls sont ignorés dans la vérification.
     * Si la collection est vide, la méthode lève une {@link IllegalStateException}.</p>
     *
     * @param baseClass  la classe de référence pour la compatibilité, non nulle
     * @param collection la collection d'éléments à vérifier, non nulle et non vide
     * @param <T>        le type des éléments dans la collection
     * @return {@code true} si tous les éléments (non nuls) sont compatibles avec {@code baseClass}, {@code false} sinon
     * @throws NullPointerException     si {@code baseClass} ou {@code collection} est {@code null}
     * @throws IllegalStateException    si la collection est vide
     *
     * @implNote
     * Cette méthode utilise {@link Class#isAssignableFrom(Class)} pour tester la compatibilité de type,
     * en considérant les relations d'héritage et d'implémentation.
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
     * Vide les caches internes. Utile pour les tests ou la gestion mémoire dans des applications long-running.
     */
    public static void clearCaches() {
        FIELD_CACHE.clear();
        SUPERCLASS_CACHE.clear();
    }

    /**
     * Retourne les statistiques des caches pour le monitoring.
     *
     * @return une map contenant les tailles des caches
     */
    public static Map<String, Integer> getCacheStats() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("fieldCacheSize", FIELD_CACHE.size());
        stats.put("superclassCacheSize", SUPERCLASS_CACHE.size());
        return Collections.unmodifiableMap(stats);
    }

    /**
     * Classe utilitaire permettant de capturer dynamiquement le type effectif d'un paramètre générique à l'instanciation.
     * Version sécurisée contre les attaques par héritage multiple et avec validation renforcée.
     * <p>
     * Gère maintenant les types complexes et offre une meilleure sécurité.
     * </p>
     *
     * @param <T> type générique à capturer
     */
    public static abstract class TypeReference<T> {
        private final Class<T> typeClass;
        private final Type type;

        @SuppressWarnings("unchecked")
        protected TypeReference() {
            // Validation de sécurité contre l'héritage multiple
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

            // Résolution du type réel
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
         * Retourne la classe capturée correspondant au type générique T.
         *
         * @return la classe représentant T
         */
        public final Class<T> getTypeClass() {
            return this.typeClass;
        }

        /**
         * Retourne le {@link Type} capturé, tel qu'un type paramétré ou une classe concrète.
         *
         * @return une instance de {@link Type} correspondant à T
         */
        public final Type getType() {
            return this.type;
        }

        /**
         * Vérifie si le type capturé est assignable depuis une classe donnée.
         *
         * @param clazz la classe à tester
         * @return true si compatible
         */
        public final boolean isAssignableFrom(Class<?> clazz) {
            return this.typeClass.isAssignableFrom(clazz);
        }

        /**
         * Vérifie si le type capturé est une instance d'une classe donnée.
         *
         * @param clazz la classe à tester
         * @return true si instance
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