package io.github.cyfko.filterql.core.exception;

/**
 * Exception levée lorsqu'une condition de filtre ne peut pas être validée ou construite.
 * <p>
 * Cette exception permet d'indiquer explicitement les erreurs liées à la validation des filtres,
 * facilitant ainsi la gestion spécifique des erreurs dans le pipeline de filtrage.
 * </p>
 *
 * <p>Elle hérite de {@link Exception} et doit être traitée ou propagée par les appels en amont.</p>
 *
 * <p>Exemple d'utilisation :</p>
 * <pre>{@code
 * if (!filter.isValid()) {
 *     throw new FilterValidationException("Le filtre contient des valeurs invalides");
 * }
 * }</pre>
 *
 * @author Frank KOSSI
 * @since 1.0
 */
public class FilterValidationException extends Exception {

    /**
     * Crée une exception avec un message explicatif.
     *
     * @param message la description de la cause de l'exception
     */
    public FilterValidationException(String message) {
        super(message);
    }

    /**
     * Crée une exception avec un message explicatif et une cause sous-jacente.
     *
     * @param message la description de la cause de l'exception
     * @param cause   la cause originelle de l'exception
     */
    public FilterValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}

