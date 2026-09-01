package ld.application.config;

import ld.domain.features.product.photos.ProductPhotoStoragePort;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.UUID;

@TestConfiguration
public class ProductPhotoStorageTestConfiguration {

    @Bean
    public ProductPhotoStoragePort productPhotoStoragePort() {
        return (productId, fileName, content) -> "stub-storage-key-" + UUID.randomUUID();
    }
}