package io.github.cyfko.filterql.core.exception;

/**
 * Exception levée lorsqu'une expression DSL contient des erreurs de syntaxe
 * ou des références invalides.
 * <p>
 * Cette exception est utilisée pour signaler des problèmes de validation syntaxique
 * dans le traitement d'expressions DSL (Domain Specific Language).
 * </p>
 * <p>
 * Elle permet de différencier clairement les erreurs provenant du parsing ou de la
 * validation de la syntaxe DSL des autres types d'exceptions.
 * </p>
 *
 * @author Frank KOSSI
 * @since 1.0
 */
public class DSLSyntaxException extends Exception {

    /**
     * Constructeur avec un message explicatif de l'erreur.
     *
     * @param message le message décrivant la cause de l'exception
     */
    public DSLSyntaxException(String message) {
        super(message);
    }

    /**
     * Constructeur avec un message explicatif et une cause sous-jacente.
     *
     * @param message le message décrivant la cause de l'exception
     * @param cause   la cause originelle de cette exception
     */
    public DSLSyntaxException(String message, Throwable cause) {
        super(message, cause);
    }
}

