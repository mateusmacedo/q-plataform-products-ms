package org.acme;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Version;
import java.time.LocalDateTime;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;

@Entity
@Cacheable
public class Product extends PanacheEntity {

    @Column(length = 12, unique = true, nullable = false)
    public String sku;

    @Column(length = 40, unique = true, nullable = false)
    public String name;

    @Column(name = "created_at", nullable = false, updatable = false)
    public LocalDateTime createdAt;

    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    public LocalDateTime deletedAt;

    @Version
    public Long version;

    public Product() {
    }

    public Product(String sku, String name) {
        this.sku = sku;
        this.name = name;
    }

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "{" +
                "id=" + id +
                ",sku='" + sku + '\'' +
                ",name='" + name + '\'' +
                ",createdAt=" + createdAt +
                ",updatedAt=" + updatedAt +
                '}';
    }
}
