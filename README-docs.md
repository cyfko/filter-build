# Documentation FilterQL

Cette documentation est construite avec Jekyll et déployée automatiquement sur GitHub Pages.

## 🚀 Site en ligne

La documentation est disponible à l'adresse :
**https://cyfko.github.io/filter-build/**

## 📁 Structure du projet

```
filter-build/
├── _config.yml           # Configuration Jekyll
├── _layouts/             # Templates Jekyll
│   ├── default.html      # Layout principal
│   └── page.html         # Layout pour les pages
├── _includes/            # Composants réutilisables
│   ├── navigation.html   # Navigation principale
│   ├── footer.html       # Pied de page
│   ├── alert.html        # Composant d'alertes
│   └── code_block.html   # Blocs de code avancés
├── assets/               # Ressources statiques
│   └── css/
│       └── main.css      # Styles personnalisés
├── docs/                 # Pages de documentation
│   ├── index.md          # Page d'accueil docs
│   ├── getting-started/  # Guide de démarrage
│   ├── core-concepts/    # Concepts fondamentaux
│   ├── api-reference/    # Référence API
│   └── spring-adapter/   # Documentation Spring
├── examples/             # Exemples pratiques
│   └── index.md          # Page d'exemples
├── features.md           # Page des fonctionnalités
├── index.md              # Page d'accueil du site
├── Gemfile               # Dépendances Ruby
└── README-docs.md        # Ce fichier
```

## 🛠️ Développement local

### Prérequis

1. **Ruby** (version 2.7+)
   ```bash
   # Windows avec Chocolatey
   choco install ruby
   
   # Windows avec winget
   winget install RubyInstallerTeam.Ruby
   
   # macOS avec Homebrew
   brew install ruby
   
   # Ubuntu/Debian
   sudo apt-get install ruby-full
   ```

2. **Bundler**
   ```bash
   gem install bundler
   ```

### Installation

1. **Cloner le repository**
   ```bash
   git clone https://github.com/cyfko/filter-build.git
   cd filter-build
   ```

2. **Installer les dépendances Jekyll**
   ```bash
   bundle install
   ```

3. **Lancer le serveur de développement**
   ```bash
   bundle exec jekyll serve
   ```

4. **Accéder au site local**
   ```
   http://localhost:4000
   ```

### Commandes utiles

```bash
# Serveur avec rechargement automatique
bundle exec jekyll serve --livereload

# Build du site statique
bundle exec jekyll build

# Serveur accessible depuis le réseau local
bundle exec jekyll serve --host 0.0.0.0

# Mode drafts (inclut les brouillons)
bundle exec jekyll serve --drafts

# Mode verbose pour le debug
bundle exec jekyll serve --verbose
```

## ✏️ Créer du contenu

### Nouvelle page

1. **Créer un fichier .md dans le dossier approprié**
   ```markdown
   ---
   layout: page
   title: Ma Nouvelle Page
   description: Description de la page
   nav_order: 5
   category: docs
   permalink: /docs/ma-page/
   show_toc: true
   ---
   
   # Contenu de la page
   ```

2. **Front Matter options**
   - `layout`: Template à utiliser (`page`, `default`)
   - `title`: Titre de la page
   - `description`: Description SEO
   - `nav_order`: Ordre dans la navigation
   - `category`: Catégorie (`docs`, `examples`, `guides`)
   - `permalink`: URL personnalisée
   - `show_toc`: Afficher la table des matières
   - `badges`: Badges à afficher (version, java, etc.)

### Composants disponibles

#### Alertes
```markdown
{% include alert.html type="info" title="Information" content="Votre message ici" %}
{% include alert.html type="warning" content="Message d'avertissement" %}
{% include alert.html type="danger" title="Attention" content="Message d'erreur" %}
{% include alert.html type="success" content="Message de succès" %}
{% include alert.html type="tip" title="Conseil" content="Conseil utile" %}
```

#### Blocs de code
```markdown
{% include code_block.html title="Titre du code" language="java" file="chemin/vers/fichier.java" %}
```java
// Votre code Java ici
public class Example {
    // ...
}
```

### Grilles
```markdown
<div class="grid">
    <div class="grid-item">
        <h3>Titre 1</h3>
        <p>Description...</p>
        <a href="/lien" class="btn btn-primary">Action</a>
    </div>
    
    <div class="grid-item">
        <h3>Titre 2</h3>
        <p>Description...</p>
        <a href="/lien" class="btn btn-secondary">Action</a>
    </div>
</div>
```

#### Badges
```markdown
badges:
  - type: version
    text: v2.0.0
  - type: java
    text: Java 21+
  - type: license
    text: MIT
```

## 🎨 Thème et styles

### Variables CSS personnalisées

Le thème utilise des variables CSS définies dans `assets/css/main.css` :

```css
:root {
    --color-primary: #667eea;
    --color-secondary: #764ba2;
    --color-accent: #f093fb;
    --font-family-primary: 'Inter', sans-serif;
    --font-family-mono: 'JetBrains Mono', monospace;
    /* ... */
}
```

### Classes utilitaires

- `.btn`, `.btn-primary`, `.btn-secondary` : Boutons
- `.badge`, `.badge-version`, `.badge-java` : Badges
- `.grid`, `.grid-item` : Système de grille
- `.alert`, `.alert-info`, `.alert-warning` : Alertes
- `.code-block` : Blocs de code améliorés

## 🚀 Déploiement

### GitHub Pages automatique

Le site est automatiquement déployé via GitHub Actions :

1. **Push vers `main`** déclenche le build
2. **Jekyll** génère les fichiers statiques
3. **GitHub Pages** publie le site

### Configuration GitHub Pages

1. Aller dans **Settings** → **Pages**
2. Source : **GitHub Actions**
3. Le workflow Jekyll est dans `.github/workflows/jekyll.yml`

### Déploiement manuel

```bash
# Build local
bundle exec jekyll build

# Les fichiers sont dans _site/
# Vous pouvez les déployer sur n'importe quel serveur web
```

## 🔧 Configuration Jekyll

### _config.yml principal

```yaml
title: FilterQL
description: Bibliothèque Java moderne pour le filtrage flexible et type-safe
baseurl: "/filter-build"
url: "https://cyfko.github.io"

markdown: kramdown
highlighter: rouge
theme: just-the-docs

plugins:
  - jekyll-feed
  - jekyll-sitemap
  - jekyll-seo-tag

# Configuration Just-the-Docs
search_enabled: true
search:
  button: true
  heading_level: 2
  previews: 3

# Navigation
nav_sort: case_insensitive
nav_external_links:
  - title: GitHub
    url: https://github.com/cyfko/filter-build
```

### Customisation du thème

Le thème Just-the-Docs est personnalisé via :
- `assets/css/main.css` : Styles supplémentaires
- `_layouts/` : Templates modifiés
- `_includes/` : Composants personnalisés

## 📊 Métriques et analytics

### Google Analytics (optionnel)

Ajouter dans `_config.yml` :
```yaml
google_analytics: "G-XXXXXXXXXX"
```

### Search Console

Ajouter un fichier `google-verification.html` à la racine.

## 🐛 Dépannage

### Problèmes courants

1. **Jekyll ne démarre pas**
   ```bash
   bundle update
   bundle exec jekyll serve --verbose
   ```

2. **Styles non appliqués**
   - Vérifier `assets/css/main.css`
   - Effacer le cache : `bundle exec jekyll clean`

3. **Navigation cassée**
   - Vérifier les `nav_order` dans le front matter
   - Vérifier la syntaxe YAML

4. **Images non affichées**
   - Utiliser des chemins relatifs : `{{ "/assets/images/image.png" | relative_url }}`

### Logs de debug

```bash
# Mode verbose
bundle exec jekyll serve --verbose

# Logs détaillés
bundle exec jekyll build --trace
```

## 📚 Ressources

- [Documentation Jekyll](https://jekyllrb.com/docs/)
- [Thème Just-the-Docs](https://just-the-docs.github.io/just-the-docs/)
- [Markdown Guide](https://www.markdownguide.org/)
- [Liquid Template Language](https://shopify.github.io/liquid/)
- [GitHub Pages Documentation](https://docs.github.com/en/pages)

## 🤝 Contribution

Pour contribuer à la documentation :

1. **Fork** le repository
2. **Créer** une branche (`git checkout -b feature/nouvelle-doc`)
3. **Modifier** la documentation
4. **Tester** localement (`bundle exec jekyll serve`)
5. **Commit** et **push**
6. **Créer** une Pull Request

### Standards de documentation

- Utiliser un français correct et accessible
- Inclure des exemples pratiques
- Ajouter des captures d'écran si nécessaire
- Tester tous les liens
- Respecter la structure existante

---

**Documentation maintenue avec ❤️ pour la communauté FilterQL**