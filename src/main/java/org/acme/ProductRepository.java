package org.acme;

import javax.enterprise.context.ApplicationScoped;

import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;

@ApplicationScoped
public class ProductRepository implements PanacheRepository<Product> {

    public Uni<Product> findBySkuOrName(String sku, String name) {
        return find("sku = ?1 or name = ?2 and deletedAt is null", sku, name).firstResult();
    }

    public Uni<Product> findBySku(String sku) {
        return find("sku = ?1 and deletedAt is null", sku).firstResult();
    }

    public Uni<Product> findByName(String name) {
        return find("name = ?1 and deletedAt is null", name).firstResult();
    }
}