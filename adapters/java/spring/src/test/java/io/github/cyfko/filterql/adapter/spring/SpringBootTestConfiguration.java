package io.github.cyfko.filterql.adapter.spring;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Configuration Spring Boot principale pour les tests.
 * Cette configuration est nécessaire pour que @SpringBootTest fonctionne.
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@EntityScan(basePackages = "io.github.cyfko.filterql.adapter.spring")
@EnableJpaRepositories(basePackages = "io.github.cyfko.filterql.adapter.spring")
@ComponentScan(basePackages = "io.github.cyfko.filterql.adapter.spring")
public class SpringBootTestConfiguration {
    // Configuration vide - Spring Boot s'occupe du reste
}

