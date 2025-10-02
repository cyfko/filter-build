---
layout: page
title: Test Jekyll
description: Page de test pour valider la configuration Jekyll
nav_order: 99
category: test
permalink: /test/
show_toc: true
badges:
  - type: version
    text: Jekyll 4.3
  - type: java
    text: Test
---

# 🧪 Page de Test Jekyll

Cette page permet de valider que tous les composants Jekyll fonctionnent correctement.

## 📋 Tests des composants

### Alertes

{% include alert.html type="info" title="Information" content="Ceci est une alerte d'information." %}

{% include alert.html type="warning" title="Attention" content="Ceci est un avertissement." %}

{% include alert.html type="danger" title="Erreur" content="Ceci est une alerte d'erreur." %}

{% include alert.html type="success" title="Succès" content="Ceci est une alerte de succès." %}

{% include alert.html type="tip" title="Conseil" content="Ceci est un conseil utile." %}

### Grille de contenu

<div class="grid">
    <div class="grid-item">
        <h4>🔧 Composant 1</h4>
        <p>Test du premier composant de la grille.</p>
        <a href="#" class="btn btn-primary">Action primaire</a>
    </div>
    
    <div class="grid-item">
        <h4>⚙️ Composant 2</h4>
        <p>Test du deuxième composant de la grille.</p>
        <a href="#" class="btn btn-secondary">Action secondaire</a>
    </div>
    
    <div class="grid-item">
        <h4>🎯 Composant 3</h4>
        <p>Test du troisième composant de la grille.</p>
        <a href="#" class="btn btn-outline">Action outline</a>
    </div>
</div>

### Code Block

{% include code_block.html title="Exemple Java" language="java" file="src/test/Test.java" %}
```java
public class TestClass {
    private String name;
    private int value;
    
    public TestClass(String name, int value) {
        this.name = name;
        this.value = value;
    }
    
    public void displayInfo() {
        System.out.println("Name: " + name + ", Value: " + value);
    }
}
```

### Badges

Les badges définis dans le front matter s'affichent automatiquement :

<div class="badges">
    <span class="badge badge-version">v2.0.0</span>
    <span class="badge badge-java">Java 21+</span>
    <span class="badge badge-license">MIT</span>
</div>

### Styles CSS

#### Typographie

**Texte en gras**, *texte en italique*, `code inline`

#### Listes

- Élément 1
- Élément 2
  - Sous-élément A
  - Sous-élément B
- Élément 3

1. Premier élément numéroté
2. Deuxième élément numéroté
3. Troisième élément numéroté

#### Tables

| Colonne 1 | Colonne 2 | Colonne 3 |
|-----------|-----------|-----------|
| Donnée 1  | Donnée 2  | Donnée 3  |
| Test A    | Test B    | Test C    |
| Valeur X  | Valeur Y  | Valeur Z  |

### Liens et Navigation

- [Lien vers l'accueil](/)
- [Lien vers la documentation](/docs/)
- [Lien externe](https://github.com/cyfko/filter-build)

## ✅ Validation

Si vous pouvez voir cette page avec tous les composants correctement stylés, alors Jekyll fonctionne parfaitement !

### Checklist de validation

- [ ] Navigation principale visible
- [ ] Alertes avec couleurs appropriées
- [ ] Grille responsive
- [ ] Code highlighting fonctionnel
- [ ] Badges affichés
- [ ] Typographie correcte
- [ ] Tables stylées
- [ ] Liens fonctionnels
- [ ] Pied de page présent

---

{% include alert.html type="success" title="Test réussi !" content="Si tous les éléments s'affichent correctement, votre configuration Jekyll est opérationnelle." %}