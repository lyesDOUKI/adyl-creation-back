package ld.application.infra.db.mapper;

import ld.application.infra.db.entity.Product;
import ld.domain.features.product.model.ProductColor;
import ld.domain.features.product.model.ProductSnapshot;

import java.util.HashSet;
import java.util.stream.Collectors;

public class ProductMapper {
    private ProductMapper(){}

    public static Product from(ProductSnapshot productSnapshot) {
        var product = new Product();
        product.setId(productSnapshot.productId());
        product.setName(productSnapshot.name());
        product.setUnitPrice(productSnapshot.price());
        product.setStatus(productSnapshot.productStatus());
        product.setColors(new HashSet<>(productSnapshot.colors().stream().
                map(ProductColor::value).collect(Collectors.toSet())));
        return product;
    }

    public static ProductSnapshot toSnapshot(Product product) {
        return new ProductSnapshot(
                product.getId(),
                product.getName(),
                product.getUnitPrice(),
                product.getColors().stream().map(ProductColor::new).toList(),
                product.getStatus()
        );
    }
}
