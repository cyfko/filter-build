package io.github.cyfko.filterql.adapter.spring;


/**
 * Interface représentant un accès à un chemin d'une propriété imbriquée dans une structure de données.
 * <p>
 * Le chemin utilise la syntaxe classique des propriétés imbriquées, similaire à celle utilisée
 * par Spring pour définir des prédicats sur des champs dans les spécifications.
 * </p>
 *
 * <p>Ce type est utile pour décrire et manipuler dynamiquement les chemins d'accès aux champs dans
 * des objets complexes ou des graphes d'entités.</p>
 *
 * @return le chemin complet sous forme de chaîne, par exemple : "fieldA.fieldB.fieldC"
 */
public interface PathShape {

    /**
     * Retourne le chemin d'accès complet du champ dans la structure de données.
     * <p>
     * Le chemin décrit la navigation dans l'objet, segmentée par des points,
     * permettant de cibler précisément une propriété imbriquée.
     * </p>
     *
     * @return le chemin sous forme de chaîne (exemple : "fieldA.fieldB.fieldC")
     */
    String getPath();
}





