package ld.application.config.product;

import ld.application.infra.db.jooq.JooqGetProductQueryRepository;
import ld.application.infra.db.jpa.ProductJpaRepository;
import ld.application.infra.db.jpa.adapter.ProductJpaAdapter;
import ld.application.infra.db.read.GetProductQueryRepository;
import org.jooq.DSLContext;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@TestConfiguration
@EnableJpaRepositories(basePackageClasses = {
        ProductJpaRepository.class
})
public class ProductPersistenceTestConfiguration {

    @Bean
    ProductJpaAdapter productJpaAdapter(ProductJpaRepository productJpaRepository) {
        return new ProductJpaAdapter(productJpaRepository);
    }

    @Bean
    GetProductQueryRepository getProductQueryRepository(DSLContext dslContext) {
        return new JooqGetProductQueryRepository(dslContext);
    }

    @Bean
    @Primary
    SwitchableProductCreator switchableProductCreator(
            ProductJpaAdapter delegate) {
        return new SwitchableProductCreator(delegate);
    }

    @Bean
    @Primary
    SwitchableProductEditor switchableProductEditor(
            ProductJpaAdapter delegate) {
        return new SwitchableProductEditor(delegate);
    }
}