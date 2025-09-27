package io.github.cyfko.filterql.adapter.spring;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Repository pour l'entité de test.
 * Ce repository est utilisé dans les tests d'intégration.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    // Méthodes de base fournies par JpaRepository et JpaSpecificationExecutor
}
