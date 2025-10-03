# 🎨 PROMPT DE TRANSFORMATION JEKYLL POUR FILTERQL DOCUMENTATION

## 🎯 OBJECTIF
Transformer la documentation FilterQL existante en un site Jekyll magnifique pour GitHub Pages en gardant **EXACTEMENT** la même structure de fichiers et de contenu, en modifiant UNIQUEMENT les layouts et la présentation visuelle.

## 📋 CONTRAINTES NON-NÉGOCIABLES
- ❌ **NE PAS CHANGER LA STRUCTURE** des dossiers docs/
- ❌ **NE PAS DÉPLACER** les fichiers .md existants
- ❌ **NE PAS MODIFIER** le contenu des fichiers documentation
- ✅ **SEULEMENT AMÉLIORER** le layout et le design visuel
- ✅ **AJOUTER** les fichiers Jekyll nécessaires (_config.yml, layouts, etc.)

## 🏗️ ARCHITECTURE CIBLE

### Structure finale souhaitée :
```
docs/
├── _config.yml                    # Configuration Jekyll (NOUVEAU)
├── _layouts/                      # Layouts Jekyll (NOUVEAU)
│   ├── default.html              # Layout principal
│   ├── page.html                 # Layout pour pages normales
│   └── home.html                 # Layout pour index.md
├── _includes/                     # Composants réutilisables (NOUVEAU)
│   ├── header.html
│   ├── navigation.html
│   ├── footer.html
│   └── code-highlight.html
├── _sass/                         # Styles SCSS (NOUVEAU)
│   ├── _variables.scss
│   ├── _layout.scss
│   ├── _syntax.scss
│   └── _components.scss
├── assets/                        # Assets statiques (NOUVEAU)
│   ├── css/
│   │   └── main.scss
│   ├── js/
│   │   └── main.js
│   └── images/
│       └── logo.png
├── index.md                       # EXISTANT - garder tel quel
├── getting-started.md             # EXISTANT - garder tel quel
├── core-module.md                 # EXISTANT - garder tel quel
├── spring-adapter.md              # EXISTANT - garder tel quel
├── examples.md                    # EXISTANT - garder tel quel
├── faq.md                         # EXISTANT - garder tel quel
├── troubleshooting.md             # EXISTANT - garder tel quel
├── ARCHITECTURE.md                # EXISTANT - garder tel quel
├── core-module/                   # EXISTANT - garder tel quel
│   └── overview.md
├── spring-adapter/                # EXISTANT - garder tel quel
│   └── overview.md
└── getting-started/               # EXISTANT - garder tel quel
    └── quick-start.md
```

## 🎨 DESIGN REQUIREMENTS

### Theme et Couleurs
- **Palette principale** : Bleu tech (#2563eb), Vert success (#10b981), Orange accent (#f59e0b)
- **Style** : Moderne, clean, orienté développeur
- **Inspiration** : GitHub, GitLab docs, Stripe documentation

### Composants visuels requis
1. **Header avec navigation sticky**
   - Logo FilterQL
   - Menu de navigation horizontal
   - Search box (optionnel)
   - GitHub link

2. **Sidebar navigation** 
   - Menu déroulant par section
   - Active state pour page courante
   - Breadcrumbs

3. **Content area**
   - Typography optimisée pour la lecture
   - Code syntax highlighting
   - Callout boxes (tip, warning, info)
   - Progress indicators pour getting-started

4. **Footer**
   - Links rapides
   - Social/GitHub
   - Copyright

### Features spéciales demandées
- 📱 **Responsive design** (mobile-first)
- 🌙 **Dark/Light mode toggle**
- 🔍 **Search functionality** (Jekyll-search ou Algolia)
- ⚡ **Fast loading** (optimisation images, CSS inline critique)
- 🎯 **Call-to-action** buttons styled
- 📊 **Code block** avec copy button
- 🏷️ **Tags et catégories** pour navigation

## 🔧 CONFIGURATION TECHNIQUE

### _config.yml requis
```yaml
# Site settings
title: "FilterQL Documentation"
description: "Transform Filtering Forever - Advanced dynamic filtering protocol for Java"
baseurl: "/filter-build"
url: "https://cyfko.github.io"

# Build settings
markdown: kramdown
highlighter: rouge
theme: minima

# Plugins
plugins:
  - jekyll-feed
  - jekyll-sitemap
  - jekyll-seo-tag

# Navigation structure
navigation:
  - title: "Home"
    url: "/"
  - title: "Getting Started"
    url: "/getting-started/"
    children:
      - title: "Quick Start"
        url: "/getting-started/quick-start/"
  - title: "Core Module"
    url: "/core-module/"
    children:
      - title: "Overview"
        url: "/core-module/overview/"
  - title: "Spring Adapter"
    url: "/spring-adapter/"
    children:
      - title: "Overview"
        url: "/spring-adapter/overview/"
  - title: "Examples"
    url: "/examples/"
  - title: "FAQ"
    url: "/faq/"
  - title: "Troubleshooting"
    url: "/troubleshooting/"
  - title: "Architecture"
    url: "/ARCHITECTURE/"

# Sass configuration
sass:
  sass_dir: _sass
  style: compressed
```

### Front matter à ajouter automatiquement
```yaml
---
layout: page
title: [titre auto-détecté du fichier]
nav_order: [ordre logique]
parent: [si sous-page]
---
```

## 📝 TRANSFORMATIONS À EFFECTUER

### 1. Layouts à créer

#### default.html
- Structure HTML5 complète
- Header avec navigation
- Sidebar avec menu
- Footer
- Dark mode toggle
- Mobile responsive

#### page.html  
- Extend default.html
- Breadcrumb navigation
- Table of contents auto-générée
- Next/Previous navigation
- Edit on GitHub link

#### home.html
- Layout spécial pour index.md
- Hero section optimisée
- Feature highlights
- Quick start CTA

### 2. Styles SCSS à créer

#### _variables.scss
```scss
// Colors
$primary-color: #2563eb;
$success-color: #10b981;
$warning-color: #f59e0b;
$text-color: #1f2937;
$bg-color: #ffffff;

// Dark mode
$dark-bg: #1f2937;
$dark-text: #f9fafb;

// Typography
$font-family: 'Inter', -apple-system, sans-serif;
$code-font: 'JetBrains Mono', monospace;

// Breakpoints
$mobile: 768px;
$tablet: 1024px;
$desktop: 1200px;
```

#### _layout.scss
- Grid system responsive
- Header/sidebar/content layout
- Navigation styling

#### _components.scss
- Buttons
- Cards
- Callouts
- Code blocks
- Forms

### 3. JavaScript fonctionalités
- Dark mode toggle
- Mobile menu
- Smooth scrolling
- Copy code button
- Search functionality

## 🚀 INSTRUCTIONS D'EXÉCUTION

### Phase 1: Setup Jekyll
1. Créer `_config.yml` avec la configuration ci-dessus
2. Créer la structure des dossiers Jekyll (`_layouts`, `_includes`, `_sass`, `assets`)

### Phase 2: Layouts et Includes
1. Créer `_layouts/default.html` avec structure responsive
2. Créer `_includes/` pour navigation, header, footer
3. Implémenter la navigation basée sur la structure existante

### Phase 3: Styles
1. Créer le système de design SCSS
2. Implémenter le dark mode
3. Optimiser la typography et le code highlighting

### Phase 4: Fonctionalités
1. Ajouter JavaScript pour interactivité
2. Implémenter search (optionnel)
3. Optimiser les performances

### Phase 5: Front Matter
1. Ajouter front matter approprié à TOUS les .md existants
2. Configurer navigation order et parent/child relationships
3. Tester que tous les liens internes fonctionnent

## ✅ CRITÈRES DE SUCCÈS

### Must-Have
- [x] Tous les fichiers .md existants INCHANGÉS dans leur contenu
- [x] Structure docs/ EXACTEMENT préservée
- [x] Navigation fonctionnelle entre toutes les pages
- [x] Design moderne et responsive
- [x] Dark mode fonctionnel
- [x] Syntax highlighting parfait

### Nice-to-Have
- [x] Search functionality
- [x] Copy code buttons
- [x] Smooth animations
- [x] Performance optimisée (PageSpeed 90+)
- [x] SEO optimisé

## 🎯 RÉSULTAT ATTENDU

Un site Jekyll GitHub Pages magnifique qui :
- Conserve EXACTEMENT la structure et le contenu existant
- Offre une expérience utilisateur moderne et fluide
- Fonctionne parfaitement sur mobile et desktop
- Charge rapidement et est optimisé SEO
- Facilite la navigation dans la documentation
- Inspire confiance et professionnalisme

## 📞 VALIDATION FINALE

Le site transformé doit :
1. ✅ **Préserver** tous les chemins de fichiers existants
2. ✅ **Maintenir** tous les liens internes fonctionnels  
3. ✅ **Améliorer** significativement l'expérience visuelle
4. ✅ **Fonctionner** parfaitement sur GitHub Pages
5. ✅ **Charger** rapidement et être accessible

---

**🎨 CRÉER UN SITE JEKYLL EXCEPTIONNEL QUI RESPECTE L'EXISTANT ! 🚀**