package io.github.cyfko.filterql.adapter.spring.utils;

import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class PathResolverUtilsTest {

    private static EntityManagerFactory emf;
    private EntityManager em;
    private CriteriaBuilder cb;

    @BeforeAll
    static void init() {
        emf = Persistence.createEntityManagerFactory("test-pu");
    }

    @AfterAll
    static void close() {
        emf.close();
    }

    @BeforeEach
    void setup() {
        em = emf.createEntityManager();
        cb = em.getCriteriaBuilder();
    }

    @AfterEach
    void tearDown() {
        if (em.isOpen()) em.close();
    }

    // --- Cas simples ---

    @Test
    void resolvePath_simpleField() {
        CriteriaQuery<TestEntity> cq = cb.createQuery(TestEntity.class);
        Root<TestEntity> root = cq.from(TestEntity.class);

        Path<?> result = PathResolverUtils.resolvePath(root, "name");

        assertNotNull(result);
    }

    @Test
    void resolvePath_nestedField() {
        CriteriaQuery<TestEntity> cq = cb.createQuery(TestEntity.class);
        Root<TestEntity> root = cq.from(TestEntity.class);

        Path<?> result = PathResolverUtils.resolvePath(root, "user.name");

        assertNotNull(result);
    }

    @Test
    void resolvePath_collectionField() {
        CriteriaQuery<TestEntity> cq = cb.createQuery(TestEntity.class);
        Root<TestEntity> root = cq.from(TestEntity.class);

        Path<?> result = PathResolverUtils.resolvePath(root, "children.name");

        assertNotNull(result);
    }

    // --- Cas invalides / particuliers ---

    @Test
    void resolvePath_nullRoot_throwsException() {
        assertThrows(NullPointerException.class,
                () -> PathResolverUtils.resolvePath(null, "name"));
    }

    @Test
    void resolvePath_nullPath_throwsException() {
        CriteriaQuery<TestEntity> cq = cb.createQuery(TestEntity.class);
        Root<TestEntity> root = cq.from(TestEntity.class);

        assertThrows(NullPointerException.class,
                () -> PathResolverUtils.resolvePath(root, null));
    }

    @Test
    void resolvePath_emptyOrDots_returnsRoot() {
        CriteriaQuery<TestEntity> cq = cb.createQuery(TestEntity.class);
        Root<TestEntity> root = cq.from(TestEntity.class);

        assertEquals(root, PathResolverUtils.resolvePath(root, ""));
        assertEquals(root, PathResolverUtils.resolvePath(root, " "));
        assertEquals(root, PathResolverUtils.resolvePath(root, "."));
        assertEquals(root, PathResolverUtils.resolvePath(root, ".."));
        assertEquals(root, PathResolverUtils.resolvePath(root, "   ...  "));
    }

    @Test
    void resolvePath_nonExistentField_throwsException() {
        CriteriaQuery<TestEntity> cq = cb.createQuery(TestEntity.class);
        Root<TestEntity> root = cq.from(TestEntity.class);

        assertThrows(IllegalArgumentException.class,
                () -> PathResolverUtils.resolvePath(root, "badField"));
    }

    @Test
    void resolvePath_nonExistentNestedField_throwsException() {
        CriteriaQuery<TestEntity> cq = cb.createQuery(TestEntity.class);
        Root<TestEntity> root = cq.from(TestEntity.class);

        assertThrows(IllegalArgumentException.class,
                () -> PathResolverUtils.resolvePath(root, "user.badField"));
    }

    // --- Classe test persistable ---
    @Entity(name = "TestEntity")
    static class TestEntity {
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        public Long id;

        public String name;

        @ManyToOne
        public TestEntity user;

        @OneToMany(mappedBy = "user")
        public java.util.List<TestEntity> children;
    }
}
