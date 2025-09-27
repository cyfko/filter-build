// Java

package io.github.cyfko.filterql.adapter.jpa;

import io.github.cyfko.filterql.adapter.jpa.impl.BasicFilterExecutor;
import io.github.cyfko.filterql.adapter.jpa.impl.PathNameSpecificationBuilder;
import io.github.cyfko.filterql.core.Condition;
import io.github.cyfko.filterql.core.FilterTree;
import io.github.cyfko.filterql.core.exception.DSLSyntaxException;
import io.github.cyfko.filterql.core.impl.DSLParser;
import io.github.cyfko.filterql.core.model.FilterDefinition;
import io.github.cyfko.filterql.core.model.FilterRequest;
import io.github.cyfko.filterql.core.utils.OperatorUtils;
import io.github.cyfko.filterql.core.validation.Operator;
import io.github.cyfko.filterql.core.validation.PropertyRef;
import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BasicFilterExecutorTest {

    // --- Test Entities and Enums ---
    @Entity(name = "TestUser")
    public static class TestUser {
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        private String name;
        private String email;
        private Integer age;
        private Boolean active;
        private LocalDateTime createdAt;
        @ManyToOne(fetch = FetchType.LAZY)
        private TestGroup group;

        // Constructors, getters, setters
        public TestUser() {}
        public TestUser(String name, String email, Integer age, Boolean active, LocalDateTime createdAt, TestGroup group) {
            this.name = name; this.email = email; this.age = age; this.active = active; this.createdAt = createdAt; this.group = group;
        }
        public Long getId() { return id; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public Integer getAge() { return age; }
        public Boolean getActive() { return active; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public TestGroup getGroup() { return group; }
        public void setGroup(TestGroup group) { this.group = group; }
    }

    @Entity(name = "TestGroup")
    public static class TestGroup {
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        private String name;
        @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
        private List<TestUser> users = new ArrayList<>();
        public TestGroup() {}
        public TestGroup(String name) { this.name = name; }
        public Long getId() { return id; }
        public String getName() { return name; }
        public List<TestUser> getUsers() { return users; }
        public void addUser(TestUser user) { users.add(user); user.setGroup(this); }
    }

    public enum TestUserPropertyRef implements PropertyRef {
        NAME(String.class, OperatorUtils.FOR_TEXT),
        AGE(Integer.class, OperatorUtils.FOR_NUMBER),
        EMAIL(String.class, Set.of(Operator.EQUALS, Operator.NOT_EQUALS, Operator.LIKE, Operator.NOT_LIKE, Operator.IS_NULL, Operator.IS_NOT_NULL)),
        ACTIVE(Boolean.class, Set.of(Operator.EQUALS, Operator.NOT_EQUALS)),
        CREATED_AT(LocalDateTime.class, OperatorUtils.FOR_TEXT),
        GROUP_NAME(String.class, OperatorUtils.FOR_TEXT);

        private final Class<?> type;
        private final Set<Operator> supportedOperators;

        TestUserPropertyRef(Class<?> type, Set<Operator> supportedOperators) {
            this.type = type;
            this.supportedOperators = supportedOperators;
        }

        @Override
        public Class<?> getType() {
            return type;
        }

        @Override
        public Set<Operator> getSupportedOperators() {
            return supportedOperators;
        }
    }

    public enum TestGroupPropertyRef implements PropertyRef {
        NAME(String.class, OperatorUtils.FOR_TEXT);

        private final Class<?> type;
        private final Set<Operator> supportedOperators;

        TestGroupPropertyRef(Class<?> type, Set<Operator> supportedOperators) {
            this.type = type;
            this.supportedOperators = supportedOperators;
        }

        @Override
        public Class<?> getType() {
            return type;
        }

        @Override
        public Set<Operator> getSupportedOperators() {
            return supportedOperators;
        }
    }


    // --- Test Data Builders ---

    static class TestDataBuilder {
        static TestUser user(String name, String email, int age, boolean active, LocalDateTime createdAt, TestGroup group) {
            return new TestUser(name, email, age, active, createdAt, group);
        }
        static TestGroup group(String name) {
            return new TestGroup(name);
        }
    }

    // --- Mocks for Unit Tests ---
    @Mock EntityManager mockEm;
    @Mock CriteriaBuilder mockCb;
    @Mock CriteriaQuery<TestUser> mockQuery;
    @Mock Root<TestUser> mockRoot;
    @Mock Predicate mockPredicate;
    @Mock TypedQuery<TestUser> mockTypedQuery;
    @Mock
    DSLParser mockDslParser;
    @Mock FilterTree mockFilterTree;
    @Mock ContextAdapter<TestUser, TestUserPropertyRef> mockContext;
    @Mock ConditionAdapter<TestUser> mockConditionAdapter;
    @Mock Specification<TestUser> mockSpecification;

    // --- Registry Builder for Unit Tests ---
    @SuppressWarnings("unchecked")
    private static final PathNameSpecificationBuilder<TestUser, TestUserPropertyRef> USER_BUILDER =
            new PathNameSpecificationBuilder<>(prop -> {
                switch (prop) {
                    case NAME:
                        return "name";
                    case EMAIL:
                        return "email";
                    case AGE:
                        return "age";
                    case ACTIVE:
                        return "active";
                    case CREATED_AT:
                        return "createdAt";
                    case GROUP_NAME:
                        return "group.name";
                    default:
                        throw new IllegalArgumentException("Unknown property: " + prop);
                }
            });

    // --- Unit Tests ---

    @Nested
    @DisplayName("Tests unitaires - Construction et Configuration")
    class ConstructorAndConfigurationTests {
        @Test
        @DisplayName("Construction avec EntityManager valide")
        void constructor_WithValidEntityManager_ShouldSucceed() {
            BasicFilterExecutor executor = new BasicFilterExecutor(mockEm);
            assertThat(executor.getEntityManager()).isSameAs(mockEm);
            assertThat(executor.getRegisteredBuildersCount()).isZero();
        }

        @Test
        @DisplayName("Construction avec EntityManager null")
        void constructor_WithNullEntityManager_ShouldThrow() {
            assertThatThrownBy(() -> new BasicFilterExecutor(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("EntityManager must not be null");
        }

        @Test
        @DisplayName("Initialisation du registry vide")
        void registry_ShouldBeEmpty_OnConstruction() {
            BasicFilterExecutor executor = new BasicFilterExecutor(mockEm);
            assertThat(executor.getRegisteredCombinations()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Tests unitaires - Gestion du Registre")
    class RegistryManagementTests {
        BasicFilterExecutor executor;

        @BeforeEach
        void setUp() {
            executor = new BasicFilterExecutor(mockEm);
        }

        @Test
        @DisplayName("Enregistrement builder valide")
        void registerPathNameBuilder_Valid_ShouldRegister() {
            executor.registerPathNameBuilder(TestUser.class, TestUserPropertyRef.class, USER_BUILDER);
            assertThat(executor.isRegistered(TestUser.class, TestUserPropertyRef.class)).isTrue();
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("Enregistrement avec paramètres null")
        void registerPathNameBuilder_NullParams_ShouldThrow(Class<?> entityClass) {
            assertThatThrownBy(() -> executor.registerPathNameBuilder(
                    (Class<TestUser>) entityClass, TestUserPropertyRef.class, USER_BUILDER))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> executor.registerPathNameBuilder(
                    TestUser.class, null, USER_BUILDER))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> executor.registerPathNameBuilder(
                    TestUser.class, TestUserPropertyRef.class, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Double enregistrement (même combinaison)")
        void registerPathNameBuilder_Duplicate_ShouldThrow() {
            executor.registerPathNameBuilder(TestUser.class, TestUserPropertyRef.class, USER_BUILDER);
            assertThatThrownBy(() -> executor.registerPathNameBuilder(
                    TestUser.class, TestUserPropertyRef.class, USER_BUILDER))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("already registered");
        }

        @Test
        @DisplayName("Vérification isRegistered() - true/false")
        void isRegistered_ShouldReturnCorrectValue() {
            assertThat(executor.isRegistered(TestUser.class, TestUserPropertyRef.class)).isFalse();
            executor.registerPathNameBuilder(TestUser.class, TestUserPropertyRef.class, USER_BUILDER);
            assertThat(executor.isRegistered(TestUser.class, TestUserPropertyRef.class)).isTrue();
        }

        @Test
        @DisplayName("Désenregistrement existant/inexistant")
        void unregisterPathNameBuilder_ShouldRemoveOrReturnNull() {
            assertThat(executor.unregisterPathNameBuilder(TestUser.class, TestUserPropertyRef.class)).isNull();
            executor.registerPathNameBuilder(TestUser.class, TestUserPropertyRef.class, USER_BUILDER);
            assertThat(executor.unregisterPathNameBuilder(TestUser.class, TestUserPropertyRef.class)).isNotNull();
            assertThat(executor.isRegistered(TestUser.class, TestUserPropertyRef.class)).isFalse();
        }

        @Test
        @DisplayName("Statistiques (count, combinations)")
        void registryStats_ShouldBeAccurate() {
            assertThat(executor.getRegisteredBuildersCount()).isZero();
            executor.registerPathNameBuilder(TestUser.class, TestUserPropertyRef.class, USER_BUILDER);
            assertThat(executor.getRegisteredBuildersCount()).isOne();
            assertThat(executor.getRegisteredCombinations())
                    .containsExactly(TestUser.class.getName() + ":" + TestUserPropertyRef.class.getName());
        }

        @Test
        @DisplayName("Nettoyage registry")
        void clearRegistry_ShouldRemoveAll() {
            executor.registerPathNameBuilder(TestUser.class, TestUserPropertyRef.class, USER_BUILDER);
            executor.clearRegistry();
            assertThat(executor.getRegisteredBuildersCount()).isZero();
        }
    }

    @Nested
    @DisplayName("Tests unitaires - Méthode findAll")
    class FindAllUnitTests {
        BasicFilterExecutor executor;

        @BeforeEach
        void setUp() {
            executor = new BasicFilterExecutor(mockEm);
            executor.registerPathNameBuilder(TestUser.class, TestUserPropertyRef.class, USER_BUILDER);
        }

        @Test
        @DisplayName("findAll() - entityClass null")
        void findAll_EntityClassNull_ShouldThrow() {
            FilterRequest<TestUserPropertyRef> req = mock(FilterRequest.class);
            assertThatThrownBy(() -> executor.findAll(null, req))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Entity class must not be null");
        }

        @Test
        @DisplayName("findAll() - request null")
        void findAll_RequestNull_ShouldThrow() {
            assertThatThrownBy(() -> executor.findAll(TestUser.class, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Filter request must not be null");
        }

        @Test
        @DisplayName("findAll() - request.filters vide")
        void findAll_EmptyFilters_ShouldThrow() {
            FilterRequest<TestUserPropertyRef> req = mock(FilterRequest.class);
            when(req.getFilters()).thenReturn(Collections.emptyMap());
            when(req.getCombineWith()).thenReturn("A");
            assertThatThrownBy(() -> executor.findAll(TestUser.class, req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("at least one filter");
        }

        @Test
        @DisplayName("findAll() - Builder non enregistré pour la combinaison")
        void findAll_BuilderNotRegistered_ShouldThrow() {
            BasicFilterExecutor ex2 = new BasicFilterExecutor(mockEm);
            FilterRequest<TestUserPropertyRef> req = mock(FilterRequest.class);
            FilterDefinition<TestUserPropertyRef> filter = mock(FilterDefinition.class);
            Map<String, FilterDefinition<TestUserPropertyRef>> filters = Map.of("A", filter);
            when(req.getFilters()).thenReturn(filters);
            when(req.getCombineWith()).thenReturn("A");
            when(filter.getRef()).thenReturn(TestUserPropertyRef.NAME);
            assertThatThrownBy(() -> ex2.findAll(TestUser.class, req))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("No PathNameSpecificationBuilder registered");
        }

        @Test
        @DisplayName("findAll() - DSL expression valide simple")
        void findAll_ValidDsl_ShouldParseAndQuery() {
            // Setup mocks for JPA/Criteria
            when(mockEm.getCriteriaBuilder()).thenReturn(mockCb);
            when(mockCb.createQuery(TestUser.class)).thenReturn(mockQuery);
            when(mockQuery.from(TestUser.class)).thenReturn(mockRoot);
            when(mockEm.createQuery(mockQuery)).thenReturn(mockTypedQuery);
            when(mockTypedQuery.getResultList()).thenReturn(List.of(new TestUser("Alice", "a@a.com", 30, true, LocalDateTime.now(), null)));

            // Build request
            FilterRequest<TestUserPropertyRef> req = FilterRequest.<TestUserPropertyRef>builder()
                    .combineWith("A")
                    .filter("A", new FilterDefinition<>(TestUserPropertyRef.NAME, Operator.EQUALS, "Alice"))
                    .build();

            // Spy on executor to mock DSLParser/FilterTree/ConditionAdapter/Specification
            BasicFilterExecutor spyExec = spy(executor);
            doReturn(mockDslParser).when(spyExec);
            when(mockDslParser.parse(anyString())).thenReturn(mockFilterTree);
            when(mockFilterTree.generate(any())).thenReturn(mockConditionAdapter);
            when(mockConditionAdapter.getSpecification()).thenReturn(mockSpecification);
            when(mockSpecification.toPredicate(any(), any(), any())).thenReturn(mockPredicate);

            List<TestUser> result = spyExec.findAll(TestUser.class, req);

            assertThat(result).hasSize(1).extracting(TestUser::getName).containsExactly("Alice");
            verify(mockEm).getCriteriaBuilder();
            verify(mockEm).createQuery(mockQuery);
            verify(mockTypedQuery).getResultList();
        }

        @Test
        @DisplayName("findAll() - DSL expression complexe avec parenthèses")
        void findAll_ComplexDsl_ShouldParseAndQuery() {
            // Similar to above, but with complex DSL and multiple filters
            when(mockEm.getCriteriaBuilder()).thenReturn(mockCb);
            when(mockCb.createQuery(TestUser.class)).thenReturn(mockQuery);
            when(mockQuery.from(TestUser.class)).thenReturn(mockRoot);
            when(mockEm.createQuery(mockQuery)).thenReturn(mockTypedQuery);
            when(mockTypedQuery.getResultList()).thenReturn(List.of());

            FilterRequest<TestUserPropertyRef> req = FilterRequest.<TestUserPropertyRef>builder()
                    .combineWith("(A | B) & C")
                    .filter("A", new FilterDefinition<>(TestUserPropertyRef.NAME, Operator.LIKE, "A%"))
                    .filter("B", new FilterDefinition<>(TestUserPropertyRef.EMAIL, Operator.LIKE, "%@a.com"))
                    .filter("C", new FilterDefinition<>(TestUserPropertyRef.ACTIVE, Operator.EQUALS, true))
                    .build();

            BasicFilterExecutor spyExec = spy(executor);
            doReturn(mockDslParser).when(spyExec);
            when(mockDslParser.parse(anyString())).thenReturn(mockFilterTree);
            when(mockFilterTree.generate(any())).thenReturn(mockConditionAdapter);
            when(mockConditionAdapter.getSpecification()).thenReturn(mockSpecification);
            when(mockSpecification.toPredicate(any(), any(), any())).thenReturn(mockPredicate);

            List<TestUser> result = spyExec.findAll(TestUser.class, req);

            assertThat(result).isEmpty();
            verify(mockEm).getCriteriaBuilder();
            verify(mockEm).createQuery(mockQuery);
            verify(mockTypedQuery).getResultList();
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t\n"})
        @DisplayName("DSL expressions invalides")
        void findAll_InvalidDsl_ShouldThrow(String invalidDsl) {
            FilterRequest<TestUserPropertyRef> req = FilterRequest.<TestUserPropertyRef>builder()
                    .combineWith(invalidDsl)
                    .filter("A", new FilterDefinition<>(TestUserPropertyRef.NAME, Operator.EQUALS, "Alice"))
                    .build();
            BasicFilterExecutor spyExec = spy(executor);
            doReturn(mockDslParser).when(spyExec);
            when(mockDslParser.parse(anyString())).thenThrow(new DSLSyntaxException("Invalid DSL"));
            assertThatThrownBy(() -> spyExec.findAll(TestUser.class, req))
                    .isInstanceOf(DSLSyntaxException.class)
                    .hasMessage("Invalid DSL");
        }

        @Test
        @DisplayName("findAll() - FilterTree génère autre type de Condition")
        void findAll_FilterTreeReturnsNonConditionAdapter_ShouldThrow() {
            FilterRequest<TestUserPropertyRef> req = FilterRequest.<TestUserPropertyRef>builder()
                    .combineWith("A")
                    .filter("A", new FilterDefinition<>(TestUserPropertyRef.NAME, Operator.EQUALS, "Alice"))
                    .build();
            BasicFilterExecutor spyExec = spy(executor);
            doReturn(mockDslParser).when(spyExec);
            when(mockDslParser.parse(anyString())).thenReturn(mockFilterTree);
            when(mockFilterTree.generate(any())).thenReturn(mock(Condition.class));
            assertThatThrownBy(() -> spyExec.findAll(TestUser.class, req))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not a ConditionAdapter");
        }

        @Test
        @DisplayName("findAll() - Requête avec Predicate null")
        void findAll_QueryWithNullPredicate_ShouldReturnAll() {
            when(mockEm.getCriteriaBuilder()).thenReturn(mockCb);
            when(mockCb.createQuery(TestUser.class)).thenReturn(mockQuery);
            when(mockQuery.from(TestUser.class)).thenReturn(mockRoot);
            when(mockEm.createQuery(mockQuery)).thenReturn(mockTypedQuery);
            when(mockTypedQuery.getResultList()).thenReturn(List.of(new TestUser("Bob", "b@b.com", 25, false, LocalDateTime.now(), null)));
            FilterRequest<TestUserPropertyRef> req = FilterRequest.<TestUserPropertyRef>builder()
                    .combineWith("A")
                    .filter("A", new FilterDefinition<>(TestUserPropertyRef.NAME, Operator.EQUALS, "Bob"))
                    .build();

            BasicFilterExecutor spyExec = spy(executor);
            doReturn(mockDslParser).when(spyExec);
            when(mockDslParser.parse(anyString())).thenReturn(mockFilterTree);
            when(mockFilterTree.generate(any())).thenReturn(mockConditionAdapter);
            when(mockConditionAdapter.getSpecification()).thenReturn(mockSpecification);
            when(mockSpecification.toPredicate(any(), any(), any())).thenReturn(null);

            List<TestUser> result = spyExec.findAll(TestUser.class, req);

            assertThat(result).hasSize(1).extracting(TestUser::getName).containsExactly("Bob");
        }
    }

    // --- Integration Tests ---

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("Tests d'intégration - Base de données")
    class DatabaseIntegrationTests {
        private EntityManagerFactory emf;
        private EntityManager em;
        private BasicFilterExecutor executor;

        @BeforeAll
        void setupDatabase() {
            Map<String, String> props = new HashMap<>();
            props.put("javax.persistence.jdbc.driver", "org.h2.Driver");
            props.put("javax.persistence.jdbc.url", "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
            props.put("javax.persistence.jdbc.user", "sa");
            props.put("javax.persistence.jdbc.password", "");
            props.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
            props.put("hibernate.hbm2ddl.auto", "create-drop");
            props.put("hibernate.show_sql", "false");
            emf = Persistence.createEntityManagerFactory("test-unit", props);
            em = emf.createEntityManager();
            executor = new BasicFilterExecutor(em);
            executor.registerPathNameBuilder(TestUser.class, TestUserPropertyRef.class, USER_BUILDER);
        }

        @AfterAll
        void tearDownDatabase() {
            if (em != null && em.isOpen()) em.close();
            if (emf != null && emf.isOpen()) emf.close();
        }

        @BeforeEach
        void insertTestData() {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM TestUser").executeUpdate();
            em.createQuery("DELETE FROM TestGroup").executeUpdate();
            TestGroup groupA = TestDataBuilder.group("Admins");
            TestGroup groupB = TestDataBuilder.group("Users");
            TestUser u1 = TestDataBuilder.user("Alice", "alice@a.com", 30, true, LocalDateTime.now().minusDays(1), groupA);
            TestUser u2 = TestDataBuilder.user("Bob", "bob@b.com", 25, false, LocalDateTime.now().minusDays(2), groupB);
            TestUser u3 = TestDataBuilder.user("Charlie", "charlie@c.com", 40, true, LocalDateTime.now().minusDays(3), groupA);
            groupA.addUser(u1); groupA.addUser(u3);
            groupB.addUser(u2);
            em.persist(groupA); em.persist(groupB);
            em.getTransaction().commit();
        }

        @Test
        @DisplayName("Requête simple sur champ String (LIKE, EQUALS)")
        void findAll_StringField_ShouldReturnCorrect() {
            FilterRequest<TestUserPropertyRef> req = FilterRequest.<TestUserPropertyRef>builder()
                    .combineWith("A")
                    .filter("A", new FilterDefinition<>(TestUserPropertyRef.NAME, Operator.EQUALS, "Alice"))
                    .build();
            List<TestUser> result = executor.findAll(TestUser.class, req);
            assertThat(result).hasSize(1).extracting(TestUser::getName).containsExactly("Alice");
        }

        @Test
        @DisplayName("Requête sur champ numérique (GT, LT, BETWEEN)")
        void findAll_NumericField_ShouldReturnCorrect() {
            FilterRequest<TestUserPropertyRef> req = FilterRequest.<TestUserPropertyRef>builder()
                    .combineWith("A")
                    .filter("A", new FilterDefinition<>(TestUserPropertyRef.AGE, Operator.GREATER_THAN, 29))
                    .build();
            List<TestUser> result = executor.findAll(TestUser.class, req);
            assertThat(result).extracting(TestUser::getName).containsExactlyInAnyOrder("Alice", "Charlie");
        }

        @Test
        @DisplayName("Requête sur champ Boolean")
        void findAll_BooleanField_ShouldReturnCorrect() {
            FilterRequest<TestUserPropertyRef> req = FilterRequest.<TestUserPropertyRef>builder()
                    .combineWith("A")
                    .filter("A", new FilterDefinition<>(TestUserPropertyRef.ACTIVE, Operator.EQUALS, true))
                    .build();
            List<TestUser> result = executor.findAll(TestUser.class, req);
            assertThat(result).extracting(TestUser::getName).containsExactlyInAnyOrder("Alice", "Charlie");
        }

        @Test
        @DisplayName("Requête avec opérateurs IN/NOT_IN")
        void findAll_InNotIn_ShouldReturnCorrect() {
            FilterRequest<TestUserPropertyRef> req = FilterRequest.<TestUserPropertyRef>builder()
                    .combineWith("A")
                    .filter("A", new FilterDefinition<>(TestUserPropertyRef.NAME, Operator.IN, List.of("Alice", "Bob")))
                    .build();
            List<TestUser> result = executor.findAll(TestUser.class, req);
            assertThat(result).extracting(TestUser::getName).containsExactlyInAnyOrder("Alice", "Bob");
        }

        @Test
        @DisplayName("Expression DSL complexe (AND, OR, NOT, parenthèses)")
        void findAll_ComplexDsl_ShouldReturnCorrect() {
            FilterRequest<TestUserPropertyRef> req = FilterRequest.<TestUserPropertyRef>builder()
                    .combineWith("(A | B) & C")
                    .filter("A", new FilterDefinition<>(TestUserPropertyRef.NAME, Operator.EQUALS, "Alice"))
                    .filter("B", new FilterDefinition<>(TestUserPropertyRef.NAME, Operator.EQUALS, "Bob"))
                    .filter("C", new FilterDefinition<>(TestUserPropertyRef.ACTIVE, Operator.EQUALS, true))
                    .build();
            List<TestUser> result = executor.findAll(TestUser.class, req);
            assertThat(result).extracting(TestUser::getName).containsExactly("Alice");
        }

        @Test
        @DisplayName("Requête sur relation ManyToOne")
        void findAll_Relation_ShouldReturnCorrect() {
            FilterRequest<TestUserPropertyRef> req = FilterRequest.<TestUserPropertyRef>builder()
                    .combineWith("A")
                    .filter("A", new FilterDefinition<>(TestUserPropertyRef.GROUP_NAME, Operator.EQUALS, "Admins"))
                    .build();
            List<TestUser> result = executor.findAll(TestUser.class, req);
            assertThat(result).extracting(TestUser::getName).containsExactlyInAnyOrder("Alice", "Charlie");
        }

        @Test
        @DisplayName("Cas limites (caractères spéciaux, Unicode)")
        void findAll_SpecialCharacters_ShouldReturnCorrect() {
            em.getTransaction().begin();
            TestUser special = TestDataBuilder.user("Élise", "élise@unicode.com", 28, true, LocalDateTime.now(), null);
            em.persist(special);
            em.getTransaction().commit();
            FilterRequest<TestUserPropertyRef> req = FilterRequest.<TestUserPropertyRef>builder()
                    .combineWith("A")
                    .filter("A", new FilterDefinition<>(TestUserPropertyRef.NAME, Operator.LIKE, "É%"))
                    .build();
            List<TestUser> result = executor.findAll(TestUser.class, req);
            assertThat(result).extracting(TestUser::getName).contains("Élise");
        }
    }

    @Nested
    @DisplayName("Tests de performance et charge")
    class PerformanceTests {
        private EntityManagerFactory emf;
        private EntityManager em;
        private BasicFilterExecutor executor;

        @BeforeEach
        void setupDatabase() {
            Map<String, String> props = new HashMap<>();
            props.put("javax.persistence.jdbc.driver", "org.h2.Driver");
            props.put("javax.persistence.jdbc.url", "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
            props.put("javax.persistence.jdbc.user", "sa");
            props.put("javax.persistence.jdbc.password", "");
            props.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
            props.put("hibernate.hbm2ddl.auto", "create-drop");
            emf = Persistence.createEntityManagerFactory("test-unit", props);
            em = emf.createEntityManager();
            executor = new BasicFilterExecutor(em);
            executor.registerPathNameBuilder(TestUser.class, TestUserPropertyRef.class, USER_BUILDER);
        }

        @AfterEach
        void tearDownDatabase() {
            if (em != null && em.isOpen()) em.close();
            if (emf != null && emf.isOpen()) emf.close();
        }

        @Test
        @DisplayName("Performance - 1000 entités")
        @Timeout(value = 5, unit = TimeUnit.SECONDS)
        void testPerformanceLargeDataset() {
            em.getTransaction().begin();
            for (int i = 0; i < 1000; i++) {
                TestUser u = TestDataBuilder.user("User" + i, "user" + i + "@mail.com", 20 + (i % 30), i % 2 == 0, LocalDateTime.now().minusDays(i % 10), null);
                em.persist(u);
            }
            em.getTransaction().commit();
            FilterRequest<TestUserPropertyRef> req = FilterRequest.<TestUserPropertyRef>builder()
                    .combineWith("A & B")
                    .filter("A", new FilterDefinition<>(TestUserPropertyRef.AGE, Operator.GREATER_THAN, 25))
                    .filter("B", new FilterDefinition<>(TestUserPropertyRef.ACTIVE, Operator.EQUALS, true))
                    .build();
            List<TestUser> result = executor.findAll(TestUser.class, req);
            assertThat(result).allMatch(u -> u.getAge() > 25 && Boolean.TRUE.equals(u.getActive()));
        }
    }
}