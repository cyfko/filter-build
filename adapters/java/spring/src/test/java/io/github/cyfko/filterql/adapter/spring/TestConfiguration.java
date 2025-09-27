package io.github.cyfko.filterql.adapter.spring;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Configuration de test pour Spring Boot.
 * Cette configuration permet aux tests d'intégration de fonctionner.
 */
@org.springframework.boot.test.context.TestConfiguration
@EnableAutoConfiguration
@EntityScan(basePackages = "io.github.cyfko.filterql.adapter.spring")
//@EnableJpaRepositories(basePackages = "io.github.cyfko.filterql.adapter.spring")
@ComponentScan(basePackages = "io.github.cyfko.filterql.adapter.spring")
public class TestConfiguration {
    // Configuration vide - Spring Boot s'occupe du reste
}

