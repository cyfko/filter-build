package io.github.cyfko.filterql.adapter.spring.utils;

import jakarta.persistence.criteria.*;
import java.lang.reflect.*;
import java.util.*;

/**
 * Utilitaire fournissant une méthode statique pour résoudre un chemin d'accès à une propriété
 * dans un root d'entité JPA afin de construire des critères de requête dynamiques.
 * <p>
 * Cette classe facilite la navigation dans les entités imbriquées en décomposant
 * un chemin de style « dot notation » (ex: "fieldA.fieldB.listField.fieldC")
 * en jointures successives dans une requête Criteria.
 * </p>
 *
 * <p>Elle gère également les collections en effectuant des jointures sur les relations
 * entre entités et en déterminant dynamiquement les types génériques des collections.</p>
 *
 * <p><b>Remarque :</b> Cette résolution est conçue pour être utilisée dans des
 * requêtes dynamiques construites avec Criteria API (javax.persistence.criteria).</p>
 *
 * <h3>Exemple d'utilisation :</h3>
 * <pre>{@code
 * CriteriaBuilder cb = entityManager.getCriteriaBuilder();
 * CriteriaQuery<MyEntity> query = cb.createQuery(MyEntity.class);
 * Root<MyEntity> root = query.from(MyEntity.class);
 *
 * Path<?> path = PathResolver.resolvePath(root, "orders.items.product.name", MyEntity.class);
 * Predicate predicate = cb.equal(path, "Book");
 * query.where(predicate);
 * }</pre>
 */
public class PathResolverUtil {

    /**
     * Résout le chemin fourni en une {@link Path} JPA à partir du root d'entité donné.
     * <p>
     * Le chemin est décomposé en segments, chacun représentant une propriété de l'entité ou de ses relations.
     * Si un segment correspond à une collection, une jointure gauche est effectuée et la résolution continue
     * dans le type générique de la collection.
     * </p>
     * <p>
     * En cas d'erreur (champ non trouvé, type générique impossible à déterminer), une exception est levée.
     * </p>
     *
     * @param root        la racine de la requête Criteria JPA (Root de l'entité d'origine)
     * @param path        le chemin complet vers la propriété, en notation « dot » (exemple : "fieldA.fieldB.listField.fieldC")
     * @param entityClass la classe de l'entité root (type de départ)
     * @param <T>         le type de l'entité root
     * @return une instance {@link Path} correspondant au chemin résolu
     * @throws IllegalArgumentException si un segment du chemin ne correspond à aucun champ dans la classe
     * @throws IllegalStateException    si le type générique d'une collection ne peut être déterminé
     */
    public static <T> Path<?> resolvePath(Root<T> root, String path) {
        String[] parts = path.split("\\.");
        From<?, ?> current = root;
        Class<?> currentClass = (new TypeReference<T>() {}).getTypeClass();

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];

            Field field;
            try {
                field = currentClass.getDeclaredField(part);
            } catch (NoSuchFieldException e) {
                throw new IllegalArgumentException("Field not found: " + part + " in " + currentClass.getName(), e);
            }

            boolean isCollection = Collection.class.isAssignableFrom(field.getType());

            if (i < parts.length - 1) {
                if (isCollection) {
                    current = current.join(part, JoinType.LEFT);
                    currentClass = getCollectionGenericType(field);
                } else {
                    current = current.join(part, JoinType.LEFT);
                    currentClass = field.getType();
                }
            } else {
                // Dernier segment du chemin : récupérer la propriété en tant que Path
                return current.get(part);
            }
        }
        throw new IllegalArgumentException("Invalid path: " + path);
    }

    /**
     * Détermine dynamiquement la classe du type générique d'une collection à partir du champ.
     *
     * @param field champ représentant une collection
     * @return la classe correspondant au type générique de la collection
     * @throws IllegalStateException si le type générique ne peut être déterminé
     */
    private static Class<?> getCollectionGenericType(Field field) {
        Type type = field.getGenericType();
        if (type instanceof ParameterizedType) {
            Type arg = ((ParameterizedType) type).getActualTypeArguments()[0];
            if (arg instanceof Class<?>) {
                return (Class<?>) arg;
            }
        }
        throw new IllegalStateException("Cannot determine generic type of collection " + field.getName());
    }

    /**
     * Classe abstraite utilisée pour capturer le type générique effectif lors de l'instanciation pour déterminer la classe type.
     *
     * @param <T> Type à capturer.
     */
    private static abstract class TypeReference<T> {
        private final Class<T> typeClass;

        @SuppressWarnings("unchecked")
        protected TypeReference() {
            Type superClass = getClass().getGenericSuperclass();
            if (superClass instanceof ParameterizedType) {
                Type actualType = ((ParameterizedType) superClass).getActualTypeArguments()[0];
                if (actualType instanceof Class<?>) {
                    this.typeClass = (Class<T>) actualType;
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
         * @return la classe de type T
         */
        public Class<T> getTypeClass() {
            return this.typeClass;
        }
    }
}

