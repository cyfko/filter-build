package io.github.cyfko.filterql.adapter.jpa;

import io.github.cyfko.filterql.adapter.jpa.utils.PathResolverUtils;
import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PathResolverUtilsTest {

    // Entités de test
    @Entity
    static class TestEntity {
        @Id Long id;
        String simpleField;
        @OneToOne TestRelatedEntity singleRelation;
        @OneToMany List<TestRelatedEntity> listRelation;
        @ManyToMany Set<TestRelatedEntity> setRelation;
        @ManyToOne TestRelatedEntity manyToOne;
    }

    @Entity
    static class TestRelatedEntity {
        @Id Long id;
        String name;
        @ManyToOne TestDeepEntity deepRelation;
        @ManyToOne TestDeepEntity singleRelation;
    }

    @Entity
    static class TestDeepEntity {
        @Id Long id;
        String deepField;
        String name;
        @ManyToOne TestRelatedEntity singleRelation;
    }

    @Mock Root<TestEntity> root;
    @Mock Join<Object, Object> join;
    @Mock Path<Object> path;

    @Nested
    @DisplayName("Tests de validation d'entrée")
    class InputValidationTests {

        @Test
        @DisplayName("Root null devrait lever une exception")
        void shouldThrowOnNullRoot() {
            assertThatThrownBy(() -> PathResolverUtils.resolvePath(null, "validPath"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("root and path cannot be null");
        }

        @Test
        @DisplayName("Path null devrait lever une exception")
        void shouldThrowOnNullPath() {
            assertThatThrownBy(() -> PathResolverUtils.resolvePath(root, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("root and path cannot be null");
        }

        @ParameterizedTest
        @EmptySource
        @ValueSource(strings = {" ", "\t", "\n"})
        @DisplayName("Chemin invalide devrait lever une exception")
        void shouldThrowOnInvalidPath(String invalidPath) {
            when((Class<TestEntity>) root.getJavaType()).thenReturn(TestEntity.class);

            assertThatThrownBy(() -> PathResolverUtils.resolvePath(root, invalidPath))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(String.format("Field not found: <%s> in <%s>",invalidPath, root.getJavaType().getSimpleName()));
        }
    }

    @Nested
    @DisplayName("Tests de résolution de chemin simple")
    class SimplePathResolutionTests {

        @BeforeEach
        void setUp() {
            //noinspection unchecked
            when((Class<TestEntity>) root.getJavaType()).thenReturn(TestEntity.class);
        }

        @Test
        @DisplayName("Devrait résoudre un champ simple")
        void shouldResolveSimpleField() {
            when(root.get("simpleField")).thenReturn(path);
            
            Path<?> result = PathResolverUtils.resolvePath(root, "simpleField");
            
            assertThat(result).isSameAs(path);
            verify(root).get("simpleField");
        }

        @Test
        @DisplayName("Devrait lever une exception pour un champ inexistant")
        void shouldThrowForNonExistentField() {
            assertThatThrownBy(() -> PathResolverUtils.resolvePath(root, "nonExistentField"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Field not found");
        }
    }

    @Nested
    @DisplayName("Tests de résolution de relations")
    class RelationResolutionTests {

        @Test
        @DisplayName("Devrait résoudre une relation OneToOne")
        void shouldResolveOneToOneRelation() {
            // Arrange
            when((Class<TestEntity>) root.getJavaType()).thenReturn(TestEntity.class);
            when(root.join(anyString(), eq(JoinType.LEFT))).thenReturn(join);
            when(join.get(anyString())).thenReturn(path);

            // Act
            String relationPath = "singleRelation.name";
            PathResolverUtils.resolvePath(root, relationPath);

            // Assert
            verify(root).join("singleRelation", JoinType.LEFT);
            verify(join).get("name");
        }

        @Test
        @DisplayName("Devrait résoudre une relation OneToMany")
        void shouldResolveOneToManyRelation() {
            // Arrange
            when((Class<TestEntity>) root.getJavaType()).thenReturn(TestEntity.class);
            when(root.join(anyString(), eq(JoinType.LEFT))).thenReturn(join);
            when(join.get(anyString())).thenReturn(path);

            // Act
            String relationPath = "listRelation.name";
            PathResolverUtils.resolvePath(root, relationPath);

            // Assert
            verify(root).join("listRelation", JoinType.LEFT);
            verify(join).get("name");
        }

        @Test
        @DisplayName("Devrait résoudre une relation profonde")
        void shouldResolveDeepRelation() {
            // Arrange
            when((Class<TestEntity>) root.getJavaType()).thenReturn(TestEntity.class);
            when(root.join(anyString(), eq(JoinType.LEFT))).thenReturn(join);
            when(join.join(anyString(), eq(JoinType.LEFT))).thenReturn(join);
            when(join.get(anyString())).thenReturn(path);

            // Act
            String deepPath = "listRelation.deepRelation.deepField";
            PathResolverUtils.resolvePath(root, deepPath);

            // Assert
            verify(root).join("listRelation", JoinType.LEFT);
            verify(join).join("deepRelation", JoinType.LEFT);
            verify(join).get("deepField");
        }
    }

    @Nested
    @DisplayName("Tests de performance")
    class PerformanceTests {

        @Test
        @DisplayName("Devrait gérer efficacement les chemins longs")
        @Timeout(1) // 1 seconde
        void shouldHandleLongPathsEfficiently() {
            //noinspection unchecked
            when((Class<TestEntity>) root.getJavaType()).thenReturn(TestEntity.class);
            when(root.join(anyString(), any())).thenReturn(join);
            when(join.join(anyString(), any())).thenReturn(join);
            when(join.get(anyString())).thenReturn(path);

            // Créer un chemin long
            StringBuilder longPath = new StringBuilder();
            for (int i = 0; i < 100; i++) {
                if (i > 0) longPath.append(".");
                longPath.append("singleRelation");
            }
            longPath.append(".name");

            PathResolverUtils.resolvePath(root, longPath.toString());
        }
    }

    @Nested
    @DisplayName("Tests de cas particuliers")
    class EdgeCasesTests {

        @SuppressWarnings("unchecked")
        @BeforeEach
        void setUp() {
            when((Class<TestEntity>) root.getJavaType()).thenReturn(TestEntity.class);
        }

        @Test
        @DisplayName("Devrait gérer les caractères spéciaux dans les noms de champs")
        void shouldHandleSpecialCharactersInFieldNames() {
            assertThatThrownBy(() -> PathResolverUtils.resolvePath(root, "simple@Field"))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Devrait gérer les chemins circulaires")
        void shouldHandleCircularPaths() {
            when(root.join(anyString(), any())).thenReturn(join);
            when(join.join(anyString(), any())).thenReturn(join);
            when(join.get(anyString())).thenReturn(path);

            String circularPath = "singleRelation.deepRelation.singleRelation.name";
            PathResolverUtils.resolvePath(root, circularPath);
            
            verify(root, times(1)).join(eq("singleRelation"), any());
            verify(join, times(2)).join(anyString(), any());
            verify(join, times(1)).get("name");
        }
    }
}