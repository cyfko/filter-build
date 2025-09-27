package io.github.cyfko.filterql.adapter.spring.utils;

import io.github.cyfko.filterql.core.utils.ClassUtils;
import jakarta.persistence.criteria.*;
import org.springframework.lang.NonNull;

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
public class PathResolverUtils {

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
     * @param <T>         le type de l'entité root
     * @return une instance {@link Path} correspondant au chemin résolu
     * @throws IllegalArgumentException si path est {@code null} ou {@code empty} ou si un segment du chemin ne correspond à aucun champ dans la classe
     * @throws IllegalStateException    si le type générique d'une collection ne peut être déterminé
     */
    public static <T> Path<?> resolvePath(Root<T> root, String path) {
        if (root == null || path == null) throw new IllegalArgumentException("path cannot be null or empty");

        String[] parts = path.split("\\.");
        From<?, ?> current = root;
        Class<?> currentClass = root.getJavaType();

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];

            final Class<?> finalCurrentClass = currentClass;
            Field field = ClassUtils.getAnyField(currentClass, part)
                    .orElseThrow(() -> new IllegalArgumentException(String.format("Field not found: %s in %s", part, finalCurrentClass)));

            boolean isCollection = Collection.class.isAssignableFrom(field.getType());

            if (i < parts.length - 1) {
                if (isCollection) {
                    current = current.join(part, JoinType.LEFT);
                    currentClass = (Class<?>) ClassUtils.getCollectionGenericType(field,0)
                            .orElseThrow(() -> new IllegalStateException("Unable to determine the parameter type of the joined collection: " + part));
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

}

