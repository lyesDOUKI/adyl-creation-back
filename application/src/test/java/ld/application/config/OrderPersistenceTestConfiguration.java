package ld.application.config;

import ld.application.infra.db.adapter.OrderJpaCreatorAdapter;
import ld.application.infra.db.adapter.ProductJpaFinderAdapter;
import ld.application.infra.db.jpa.OrderJpaRepository;
import ld.application.infra.db.jpa.ProductJpaRepository;
import ld.domain.features.order.OrderCreator;
import ld.domain.features.product.ProductFinder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@TestConfiguration
@EnableJpaRepositories(basePackageClasses = {
        OrderJpaRepository.class,
        ProductJpaRepository.class,
})
public class OrderPersistenceTestConfiguration {

    @Bean
    OrderJpaCreatorAdapter orderJpaCreatorAdapter(
            OrderJpaRepository repository
    ) {
        return new OrderJpaCreatorAdapter(repository);
    }

    @Bean
    OrderCreator orderCreator(
            OrderJpaCreatorAdapter adapter
    ) {
        return adapter;
    }

    @Bean
    ProductJpaFinderAdapter productJpaFinderAdapter(
            ProductJpaRepository repository
    ) {
        return new ProductJpaFinderAdapter(repository);
    }

    @Bean
    ProductFinder productFinder(
            ProductJpaFinderAdapter adapter
    ) {
        return adapter;
    }
}