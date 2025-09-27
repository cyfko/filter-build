package io.github.cyfko.filterql.adapter.spring.utils;

import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class PathResolverUtilsTest {

    @Autowired
    private TestEntityManager em;

    private CriteriaBuilder cb;

    @BeforeEach
    void setup() {
        cb = em.getEntityManager().getCriteriaBuilder();
    }

    // --- Cas simples ---

    @Test
    void resolvePath_simpleField() {
        CriteriaQuery<TestEntity> cq = cb.createQuery(TestEntity.class);
        Root<TestEntity> root = cq.from(TestEntity.class);

        Path<?> result = PathResolverUtils.resolvePath(root, "id");

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
    void resolvePath_trailingComma() {
        CriteriaQuery<TestEntity> cq = cb.createQuery(TestEntity.class);
        Root<TestEntity> root = cq.from(TestEntity.class);

        Path<?> result = PathResolverUtils.resolvePath(root, "id.");

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
        assertThrows(IllegalArgumentException.class,
                () -> PathResolverUtils.resolvePath(null, "name"));
    }

    @Test
    void resolvePath_nullPath_throwsException() {
        CriteriaQuery<TestEntity> cq = cb.createQuery(TestEntity.class);
        Root<TestEntity> root = cq.from(TestEntity.class);

        assertThrows(IllegalArgumentException.class,
                () -> PathResolverUtils.resolvePath(root, null));
    }

    @Test
    void resolvePath_nonExistentField_throwsException() {
        CriteriaQuery<TestEntity> cq = cb.createQuery(TestEntity.class);
        Root<TestEntity> root = cq.from(TestEntity.class);

        assertThrows(IllegalArgumentException.class, () -> PathResolverUtils.resolvePath(root, ""));
        assertThrows(IllegalArgumentException.class, () -> PathResolverUtils.resolvePath(root, "."));
        assertThrows(IllegalArgumentException.class, () -> PathResolverUtils.resolvePath(root, ".id"));
        assertThrows(IllegalArgumentException.class, () -> PathResolverUtils.resolvePath(root, "x"));
        assertThrows(IllegalArgumentException.class, () -> PathResolverUtils.resolvePath(root, "x.id"));
        assertThrows(IllegalArgumentException.class, () -> PathResolverUtils.resolvePath(root, "id.user"));
        assertThrows(IllegalArgumentException.class, () -> PathResolverUtils.resolvePath(root, "user.y"));
        assertThrows(IllegalArgumentException.class, () -> PathResolverUtils.resolvePath(root, "children.user.w"));
    }

    @MappedSuperclass
    static class SuperBasePart {
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private Long id;
    }

    @MappedSuperclass
    static class BasePart extends SuperBasePart {
        private String name;
    }

    // --- Classe test persistable ---
    @Entity(name = "PathResolverTestEntity")
    static class TestEntity extends BasePart {

        @ManyToOne
        private TestEntity user;

        @OneToMany(mappedBy = "user")
        private java.util.List<TestEntity> children;
    }
}
