---
title: Guide d'Installation
description: Installation et configuration de FilterQL dans votre projet
sidebar_position: 2
---

# Guide d'Installation FilterQL

> **Configuration rapide et complète de FilterQL dans votre projet Java**

---

## Vue d'Ensemble

FilterQL est une bibliothèque Java modulaire disponible via Maven Central. L'installation varie selon vos besoins :

- **Module Core** : Filtrage agnostique de framework (requis)
- **Adaptateur Spring** : Intégration Spring Data JPA (optionnel)

### Prérequis Système

| Composant | Version Minimale | Recommandée |
|-----------|-----------------|-------------|
| **Java** | 21 | 21 LTS |
| **Maven** | 3.9.0 | Latest |
| **Gradle** | 8.0 | Latest |
| **Spring Boot** | 3.3.4 | Latest 3.x |

---

## Installation Standard

### Maven

**1. Ajouter les Dépendances**

```xml
<dependencies>
    <!-- Module Core FilterQL (REQUIS) -->
    <dependency>
        <groupId>io.github.cyfko</groupId>
        <artifactId>filterql-core</artifactId>
        <version>3.0.0</version>
    </dependency>
    
    <!-- Adaptateur Spring Data JPA (si Spring utilisé) -->
    <dependency>
        <groupId>io.github.cyfko</groupId>
        <artifactId>filterql-spring</artifactId>
        <version>3.0.0</version>
    </dependency>
</dependencies>
```

**2. Configuration Maven (optionnel)**

```xml
<properties>
    <!-- Version Java -->
    <maven.compiler.source>21</maven.compiler.source>
    <maven.compiler.target>21</maven.compiler.target>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    
    <!-- Version FilterQL centralisée -->
    <filterql.version>3.0.0</filterql.version>
</properties>

<dependencies>
    <dependency>
        <groupId>io.github.cyfko</groupId>
        <artifactId>filterql-core</artifactId>
        <version>${filterql.version}</version>
    </dependency>
    <dependency>
        <groupId>io.github.cyfko</groupId>
        <artifactId>filterql-spring</artifactId>
        <version>${filterql.version}</version>
    </dependency>
</dependencies>
```

### Gradle

**1. Configuration build.gradle**

```gradle
dependencies {
    // Module Core FilterQL (REQUIS)
    implementation 'io.github.cyfko:filterql-core:3.0.0'
    
    // Adaptateur Spring Data JPA (si Spring utilisé)
    implementation 'io.github.cyfko:filterql-spring:3.0.0'
}
```

**2. Configuration avec variables (optionnel)**

```gradle
ext {
    filterqlVersion = '3.0.0'
}

dependencies {
    implementation "io.github.cyfko:filterql-core:${filterqlVersion}"
    implementation "io.github.cyfko:filterql-spring:${filterqlVersion}"
}
```

### Gradle (Kotlin DSL)

```kotlin
val filterqlVersion = "3.0.0"

dependencies {
    implementation("io.github.cyfko:filterql-core:$filterqlVersion")
    implementation("io.github.cyfko:filterql-spring:$filterqlVersion")
}
```

---

## Installation par Cas d'Usage

### Projet Spring Boot

**Dépendances complètes pour Spring Boot** :

```xml
<dependencies>
    <!-- Spring Boot Starters -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    
    <!-- FilterQL -->
    <dependency>
        <groupId>io.github.cyfko</groupId>
        <artifactId>filterql-core</artifactId>
        <version>3.0.0</version>
    </dependency>
    <dependency>
        <groupId>io.github.cyfko</groupId>
        <artifactId>filterql-spring</artifactId>
        <version>3.0.0</version>
    </dependency>
    
    <!-- Base de données (exemple H2) -->
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

### Projet Non-Spring (Core Only)

```xml
<dependencies>
    <!-- Module Core uniquement -->
    <dependency>
        <groupId>io.github.cyfko</groupId>
        <artifactId>filterql-core</artifactId>
        <version>3.0.0</version>
    </dependency>
    
    <!-- JPA Implementation (exemple Hibernate) -->
    <dependency>
        <groupId>org.hibernate</groupId>
        <artifactId>hibernate-core</artifactId>
        <version>6.4.0.Final</version>
    </dependency>
</dependencies>
```

### Projet Multi-Module

**Parent POM** :
```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.cyfko</groupId>
            <artifactId>filterql-core</artifactId>
            <version>3.0.0</version>
        </dependency>
        <dependency>
            <groupId>io.github.cyfko</groupId>
            <artifactId>filterql-spring</artifactId>
            <version>3.0.0</version>
        </dependency>
    </dependencies>
</dependencyManagement>
```

**Module Enfant** :
```xml
<dependencies>
    <dependency>
        <groupId>io.github.cyfko</groupId>
        <artifactId>filterql-core</artifactId>
        <!-- Version héritée du parent -->
    </dependency>
</dependencies>
```

---

## Configuration Post-Installation

### Configuration Spring Boot

**1. Créer Configuration Bean**

```java
@Configuration
@EnableJpaRepositories
public class FilterQLConfig {
    
    /**
     * Configuration FilterContext pour entité User
     */
    @Bean
    public FilterContext<User, UserPropertyRef> userFilterContext() {
        return new FilterContext<>(
            User.class, 
            UserPropertyRef.class,
            this::mapUserProperty
        );
    }
    
    /**
     * Configuration FilterContext pour entité Product
     */
    @Bean
    public FilterContext<Product, ProductPropertyRef> productFilterContext() {
        return new FilterContext<>(
            Product.class, 
            ProductPropertyRef.class,
            this::mapProductProperty
        );
    }
    
    /**
     * Mapping User properties vers champs JPA
     */
    private Object mapUserProperty(FilterDefinition<UserPropertyRef> def) {
        return switch (def.ref()) {
            case NAME -> "name";
            case EMAIL -> "email";
            case AGE -> "age";
            case CREATED_DATE -> "createdDate";
            case DEPARTMENT_NAME -> "department.name"; // Navigation relation
        };
    }
    
    /**
     * Mapping Product properties vers champs JPA
     */
    private Object mapProductProperty(FilterDefinition<ProductPropertyRef> def) {
        return switch (def.ref()) {
            case NAME -> "name";
            case PRICE -> "price";
            case CATEGORY -> "category.name";
            case IN_STOCK -> new InStockPredicateMapping(def); // Logique custom
        };
    }
}
```

**2. Configuration application.yml**

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        format_sql: true
        use_sql_comments: true
  
  # Configuration base de données
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password: 

# Configuration logging FilterQL (optionnel)
logging:
  level:
    io.github.cyfko.filterql: DEBUG
    org.hibernate.SQL: DEBUG
```

### Configuration Non-Spring

**Configuration JPA manuelle** :

```java
@Configuration
public class JPAConfig {
    
    @Bean
    public EntityManagerFactory entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource());
        em.setPackagesToScan("com.yourpackage.entity");
        
        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        
        return em.getObject();
    }
    
    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:testdb");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        return dataSource;
    }
}
```

---

## Vérification de l'Installation

### Test de Base

**1. Créer PropertyReference**

```java
public enum TestPropertyRef implements PropertyReference {
    NAME(String.class, Set.of(Op.EQ, Op.MATCHES)),
    AGE(Integer.class, Set.of(Op.GT, Op.LT));

    private final Class<?> type;
    private final Set<Op> supportedOperators;

    TestPropertyRef(Class<?> type, Set<Op> supportedOperators) {
        this.type = type;
        this.supportedOperators = Set.copyOf(supportedOperators);
    }

    @Override
    public Class<?> getType() { return type; }

    @Override
    public Set<Op> getSupportedOperators() { return supportedOperators; }
}
```

**2. Test Unit Simple**

```java
@Test
void testFilterQLInstallation() {
    // Test création FilterDefinition
    FilterDefinition<TestPropertyRef> filter = new FilterDefinition<>(
        TestPropertyRef.NAME, Op.EQ, "test"
    );
    
    assertThat(filter.ref()).isEqualTo(TestPropertyRef.NAME);
    assertThat(filter.operator()).isEqualTo(Op.EQ);
    assertThat(filter.value()).isEqualTo("test");
}

@Test
void testFilterRequestBuilder() {
    // Test Builder pattern
    FilterRequest<TestPropertyRef> request = FilterRequest.<TestPropertyRef>builder()
        .filter("nameFilter", new FilterDefinition<>(TestPropertyRef.NAME, Op.EQ, "John"))
        .filter("ageFilter", new FilterDefinition<>(TestPropertyRef.AGE, Op.GT, 25))
        .combineWith("nameFilter & ageFilter")
        .build();
        
    assertThat(request.filters()).hasSize(2);
    assertThat(request.combineWith()).isEqualTo("nameFilter & ageFilter");
}
```

### Test d'Intégration Spring

```java
@SpringBootTest
@DataJpaTest
class FilterQLSpringIntegrationTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private FilterContext<User, UserPropertyRef> filterContext;
    
    @Test
    void testSpringIntegration() {
        // Création test data
        User user = new User("John Doe", "john@example.com", 30);
        entityManager.persistAndFlush(user);
        
        // Test FilterContext configuration
        FilterDefinition<UserPropertyRef> nameFilter = new FilterDefinition<>(
            UserPropertyRef.NAME, Op.EQ, "John Doe"
        );
        
        Condition condition = filterContext.addCondition("nameFilter", nameFilter);
        PredicateResolver<User> resolver = filterContext.toResolver(User.class, condition);
        
        // Vérification que resolver est créé sans erreur
        assertThat(resolver).isNotNull();
        
        // Test conversion vers Specification
        Specification<User> spec = (root, query, cb) -> resolver.resolve(root, query, cb);
        assertThat(spec).isNotNull();
    }
}
```

---

## Versions et Compatibilité

### Matrice de Compatibilité

| FilterQL | Java | Spring Boot | JPA/Hibernate | Status |
|----------|------|-------------|---------------|--------|
| **3.0.x** | 21+ | 3.3.4+ | Jakarta 3.1+ | ✅ Current |
| **2.9.x** | 17+ | 3.x | Jakarta 3.x | ⚠️ Maintenance |
| **2.x** | 17+ | 2.7+ | Jakarta 3.x | ❌ EOL |

### Dépendances Transitives

**FilterQL Core** apporte :
- `jakarta.persistence:jakarta.persistence-api:3.1.0`
- `jakarta.validation:jakarta.validation-api:3.1.1`

**FilterQL Spring** apporte :
- `filterql-core:3.0.0` (transitive)
- `spring-boot-starter-data-jpa` (provided scope)

### Conflits de Versions Courants

**Problème** : `NoClassDefFoundError: jakarta/persistence/criteria/CriteriaBuilder`
```
Solution: Vérifier que votre projet utilise Jakarta EE (pas javax)
```

**Problème** : `UnsupportedClassVersionError` 
```
Solution: Vérifier Java 21+ installé et configuré
```

**Problème** : `NoSuchMethodError` sur API Spring Data
```
Solution: Vérifier Spring Boot 3.3.4+ 
```

---

## Gestion des Versions

### Version Pinning

**Maven** :
```xml
<properties>
    <filterql.version>3.0.0</filterql.version>
</properties>

<dependencies>
    <dependency>
        <groupId>io.github.cyfko</groupId>
        <artifactId>filterql-core</artifactId>
        <version>${filterql.version}</version>
    </dependency>
</dependencies>
```

**Gradle** :
```gradle
configurations.all {
    resolutionStrategy {
        force 'io.github.cyfko:filterql-core:3.0.0'
        force 'io.github.cyfko:filterql-spring:3.0.0'
    }
}
```

### Version Ranges (Non Recommandé)

```xml
<!-- ❌ ÉVITER - versions dynamiques -->
<version>[3.0,4.0)</version>
<version>3.+</version>

<!-- ✅ PRÉFÉRER - versions exactes -->
<version>3.0.0</version>
```

### Mise à jour Versions

**Vérification nouvelles versions** :
```bash
# Maven
mvn versions:display-dependency-updates

# Gradle  
./gradlew dependencyUpdates
```

---

## Dépannage Installation

### Problèmes Courants

#### 1. Version Java Incompatible

**Erreur** :
```
[ERROR] Source option 21 is no longer supported. Use 21 or later.
```

**Solution** :
```bash
# Vérifier version Java
java -version
javac -version

# Installer Java 21 si nécessaire
# Ubuntu/Debian
sudo apt install openjdk-21-jdk

# macOS (avec Homebrew)
brew install openjdk@21

# Windows
# Télécharger depuis https://openjdk.org/
```

#### 2. Maven Central Access

**Erreur** :
```
[ERROR] Could not find artifact io.github.cyfko:filterql-core:jar:3.0.0
```

**Solution** :
```xml
<!-- Vérifier repository Maven Central configuré -->
<repositories>
    <repository>
        <id>central</id>
        <url>https://repo1.maven.org/maven2</url>
    </repository>
</repositories>
```

#### 3. Conflits de Dépendances

**Diagnostic** :
```bash
# Maven - voir arbre dépendances
mvn dependency:tree

# Gradle - voir conflits
./gradlew dependencies --configuration compileClasspath
```

**Résolution exclusions** :
```xml
<dependency>
    <groupId>io.github.cyfko</groupId>
    <artifactId>filterql-spring</artifactId>
    <version>3.0.0</version>
    <exclusions>
        <exclusion>
            <groupId>conflicting.group</groupId>
            <artifactId>conflicting-artifact</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

### Outils de Diagnostic

#### Script de Vérification

```bash
#!/bin/bash
# check-filterql-setup.sh

echo "🔍 FilterQL Installation Check"

# Java version
echo "☕ Java Version:"
java -version

# Maven/Gradle
if command -v mvn &> /dev/null; then
    echo "📦 Maven Version:"
    mvn -version
fi

if command -v gradle &> /dev/null; then
    echo "📦 Gradle Version:"
    gradle -version
fi

# Check dependencies
echo "🔗 Checking FilterQL dependencies..."
if [ -f "pom.xml" ]; then
    grep -n "filterql" pom.xml || echo "❌ FilterQL not found in pom.xml"
elif [ -f "build.gradle" ]; then
    grep -n "filterql" build.gradle || echo "❌ FilterQL not found in build.gradle"
fi

echo "✅ Check completed"
```

#### Maven Helper Plugin

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-dependency-plugin</artifactId>
    <version>3.6.0</version>
    <executions>
        <execution>
            <goals>
                <goal>analyze</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

---

## Next Steps

### Après Installation Réussie

1. **[Configuration Initiale](./implementations/core/)** : Setup PropertyReference et Context
2. **[Quick Start Guide](./index.md#démarrage-rapide)** : Premier exemple fonctionnel
3. **[Spring Integration](./implementations/spring-adapter/)** : Configuration complète Spring
4. **[Examples](./examples/)** : Cas d'usage réels et patterns

### Ressources Utiles

- **[Maven Central](https://central.sonatype.com/namespace/io.github.cyfko)** : Artefacts et métadonnées
- **[GitHub Repository](https://github.com/cyfko/filter-build)** : Code source et issues
- **[Architecture Guide](./architecture.md)** : Compréhension approfondie
- **[Contributing](./contributing.md)** : Guide de contribution

---

*Installation réussie ? Découvrez maintenant la [documentation complète](./index.md) et les [exemples pratiques](./examples/) !*