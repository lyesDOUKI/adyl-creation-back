package ld.application.config.product;

import ld.application.api.resolver.LocalProductPhotoUrlResolver;
import ld.domain.features.product.photos.ProductPhotoStoragePort;
import ld.domain.features.product.photos.ProductPhotoUrlResolver;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.UUID;

@TestConfiguration
public class ProductPhotoStorageTestConfiguration {

    @Bean
    public ProductPhotoUrlResolver productPhotoUrlResolver() {
        return new LocalProductPhotoUrlResolver();
    }

    @Bean
    public ProductPhotoStoragePort productPhotoStoragePort() {
        return (productId, fileName, content) -> "stub-storage-key-" + UUID.randomUUID();
    }
}