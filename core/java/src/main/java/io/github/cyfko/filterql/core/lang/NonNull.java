package io.github.cyfko.filterql.core.lang;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation indiquant qu'un élément (paramètre, champ, méthode) ne doit jamais être null.
 * <p>
 * Utilisée comme un contrat pour assurer la non-nullité d'une variable, d'un paramètre
 * ou d'une valeur de retour, cette annotation sert principalement à la documentation
 * et à la vérification statique par des outils ou frameworks adaptés.
 * </p>
 * <p>
 * Cette annotation ne réalise aucune validation automatique à l'exécution.
 * </p>
 *
 * Usage typique :
 * <pre>{@code
 * public void setName(@NotNull String name) {
 *     this.name = Objects.requireNonNull(name);
 * }
 * }</pre>
 *
 * @author Frank KOSSI
 * @since 1.0
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NonNull {
}

