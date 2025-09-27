package io.github.cyfko.filterql.adapter.spring;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entité de test pour les tests JPA.
 * Cette entité est utilisée dans tous les tests d'intégration.
 */
@Entity
@Table(name = "test_user")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "age")
    private Integer age;
    
    @Column(name = "email")
    private String email;
    
    @Column(name = "active")
    private Boolean active;
    
    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt;
    
    // Constructeurs
    public User() {}

    // Ajouter ce constructeur manquant
    public User(String name, Integer age, String email) {
        this.name = name;
        this.age = age;
        this.email = email;
        this.active = true; // valeur par défaut
        this.createdAt = LocalDateTime.now();
    }
    
    public User(String name, Integer age, String email, Boolean active) {
        this.name = name;
        this.age = age;
        this.email = email;
        this.active = active;
        this.createdAt = java.time.LocalDateTime.now();
    }

    public User(String name, Integer age, String email, LocalDateTime localDateTime) {
        this.name = name;
        this.age = age;
        this.email = email;
        this.active = true;
        this.createdAt = localDateTime;
    }
    
    // Getters et Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Integer getAge() {
        return age;
    }
    
    public void setAge(Integer age) {
        this.age = age;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public Boolean getActive() {
        return active;
    }
    
    public void setActive(Boolean active) {
        this.active = active;
    }
    
    public java.time.LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(java.time.LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public String toString() {
        return "TestEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", email='" + email + '\'' +
                ", active=" + active +
                ", createdAt=" + createdAt +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User that = (User) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
