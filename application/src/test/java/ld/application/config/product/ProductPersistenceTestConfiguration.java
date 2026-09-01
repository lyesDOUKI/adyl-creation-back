package ld.application.config.product;

import ld.application.infra.db.jpa.ProductJpaRepository;
import ld.application.infra.db.jpa.adapter.ProductJpaAdapter;
import ld.domain.features.product.ProductChecker;
import ld.domain.features.product.ProductCreator;
import ld.domain.features.product.lifecycle.ProductEditor;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@TestConfiguration
@EnableJpaRepositories(basePackageClasses = {
        ProductJpaRepository.class
})
public class ProductPersistenceTestConfiguration {

    @Bean
    ProductCreator productCreator(ProductJpaRepository productJpaRepository) {
        return new ProductJpaAdapter(productJpaRepository);
    }

    @Bean
    ProductEditor productEditor(ProductJpaRepository productJpaRepository) {
        return new ProductJpaAdapter(productJpaRepository);
    }

    @Bean
    ProductChecker productChecker(ProductJpaRepository productJpaRepository) {
        return new ProductJpaAdapter(productJpaRepository);
    }
}
