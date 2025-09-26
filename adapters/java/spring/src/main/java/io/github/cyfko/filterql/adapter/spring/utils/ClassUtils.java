package io.github.cyfko.filterql.core.utils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

/**
 * Utilitaires avancés pour la réflexion Java et la manipulation dynamique de types, notamment dans le cadre
 * de l'introspection et l'analyse des collections et des hiérarchies de classes.
 *
 * <p>
 * Propose des fonctions de recherche de champ à travers la hiérarchie des classes, d'extraction du type générique
 * d'une collection, de détermination du type commun à une collection d'objets, ainsi qu'un outil de capture
 * de type générique à l'exécution.
 * </p>
 *
 * <h3>Exemple d'utilisation</h3>
 * <pre>{@code
 * // Recherche du champ "id" dans une classe ou ses superclasses
 * Optional<Field> idField = ClassUtils.getAnyField(MyClass.class, "id");
 *
 * // Extraction du type générique d'une collection
 * Optional<Class<?>> itemType = idField.flatMap(f -> ClassUtils.getCollectionGenericType(f, 0));
 *
 * // Déterminer la plus haute classe commune d'une collection
 * Class<?> superClass = ClassUtils.getCommonSuperclass(List.of("abc", "def", "ghi")); // -> String.class
 * }</pre>
 *
 * @author Cyfko
 * @since 1.0
 */
public class ClassUtils {

    /**
     * Recherche récursive d'un champ nommé dans la classe et ses superclasses.
     *
     * @param clazz la classe de départ pour la recherche, non nulle
     * @param name  le nom du champ recherché, non nul
     * @return un {@link Optional} contenant le champ, ou vide si aucun champ nommé n'est trouvé
     * @throws NullPointerException si clazz ou name est nul
     */
    public static Optional<Field> getAnyField(Class<?> clazz, String name) {
        Objects.requireNonNull(clazz, "Class must not be null");
        Objects.requireNonNull(name, "Field name must not be null");
        Class<?> current = clazz;
        while (current != null) {
            try {
                return Optional.of(current.getDeclaredField(name));
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        return Optional.empty();
    }

    /**
     * Extrait dynamiquement la classe du type générique d'une collection à partir d'un champ déclaré.
     *
     * @param field      champ représentant une collection générique, non nul
     * @param paramIndex index du paramètre générique ({@code 0} pour List&lt;T&gt;, {@code 0} ou {@code 1} pour Map&lt;K,V&gt;)
     * @return un {@link Optional} contenant la classe du paramètre générique, ou vide si indéterminé
     * @throws NullPointerException si field est nul
     */
    public static Optional<Class<?>> getCollectionGenericType(Field field, int paramIndex) {
        Objects.requireNonNull(field, "Field must not be null");
        Type type = field.getGenericType();
        if (type instanceof ParameterizedType) {
            Type[] args = ((ParameterizedType) type).getActualTypeArguments();
            if (paramIndex >= 0 && paramIndex < args.length && args[paramIndex] instanceof Class<?>) {
                return Optional.of((Class<?>) args[paramIndex]);
            }
        }
        return Optional.empty();
    }

    /**
     * Détermine la classe la plus haute (la plus commune) dans la hiérarchie des éléments d'une collection.
     * Ignore les éléments nuls. Retourne {@code Object.class} si tous sont nuls.
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
        Class<? super T> result = null;
        for (T element : collection) {
            if (element == null) continue;
            if (result == null) {
                result = (Class<? super T>) element.getClass();
            } else {
                while (result != Object.class && !result.isAssignableFrom(element.getClass())) {
                    result = result.getSuperclass();
                }
            }
        }
        return result != null ? result : Object.class;
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
        if (collection.isEmpty()) return Optional.empty();
        Class<?> clazz = null;
        for (var element : collection) {
            if (element == null) continue;
            if (clazz == null) {
                clazz = element.getClass();
            } else if (!clazz.equals(element.getClass())) {
                return Optional.empty();
            }
        }
        return Optional.ofNullable(clazz);
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
        if (collection.isEmpty()) throw new IllegalStateException("Collection must not be empty");

        for (T element : collection) {
            if (element == null) continue;
            if (!baseClass.isAssignableFrom(element.getClass()) && !element.getClass().isAssignableFrom(baseClass)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Classe utilitaire permettant de capturer dynamiquement le type effectif d'un paramètre générique à l'instanciation.
     * <p>
     * Ne gère pas les types complexes ou imbriqués. Permet d'obtenir à l'exécution la {@code Class<T>}.
     * </p>
     *
     * @param <T> type générique à capturer
     */
    public static abstract class TypeReference<T> {
        private final Class<T> typeClass;
        private final Type type;

        @SuppressWarnings("unchecked")
        protected TypeReference() {
            Type superClass = getClass().getGenericSuperclass();
            if (superClass instanceof ParameterizedType) {
                this.type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
                if (type instanceof Class<?>) {
                    this.typeClass = (Class<T>) type;
                } else {
                    throw new IllegalArgumentException("Type générique n'est pas une classe simple");
                }
            } else {
                throw new IllegalArgumentException("TypeReference doit être instancié avec un type générique");
            }
        }

        /**
         * Retourne la classe capturée correspondant au type générique T.
         *
         * @return la classe représentant T
         */
        public Class<T> getTypeClass() {
            return this.typeClass;
        }

        /**
         * Retourne le {@link Type} capturé, tel qu'un type paramétré ou une classe concrète.
         *
         * @return une instance de {@link Type} correspondant à T
         */
        public Type getType() {
            return this.type;
        }
    }
}
