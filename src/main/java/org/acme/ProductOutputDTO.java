package org.acme;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductOutputDTO {

    private Long id;
    private String sku;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ProductOutputDTO fromEntity(Product product) {
        return new ProductOutputDTO(product.id, product.sku, product.name, product.createdAt, product.updatedAt);
    }

    public static List<ProductOutputDTO> fromEntities(List<Product> products) {
        return products.stream()
                .map(ProductOutputDTO::fromEntity)
                .collect(Collectors.toList());
    }
}