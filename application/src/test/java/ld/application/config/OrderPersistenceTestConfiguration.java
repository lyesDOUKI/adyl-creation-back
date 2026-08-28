package ld.application.config;

import ld.application.infra.db.adapter.CreateOrderJpaRepositoryAdapter;
import ld.application.infra.db.adapter.GetProductJpaRepositoryAdapter;
import ld.application.infra.db.jpa.OrderJpaRepository;
import ld.application.infra.db.jpa.ProductJpaRepository;
import ld.domain.features.order.CreateOrderRepository;
import ld.domain.features.product.GetProductRepository;
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
    CreateOrderJpaRepositoryAdapter createOrderJpaRepositoryAdapter(
            OrderJpaRepository repository
    ) {
        return new CreateOrderJpaRepositoryAdapter(repository);
    }

    @Bean
    CreateOrderRepository createOrderRepository(
            CreateOrderJpaRepositoryAdapter adapter
    ) {
        return adapter;
    }

    @Bean
    GetProductJpaRepositoryAdapter getProductJpaRepositoryAdapter(
            ProductJpaRepository repository
    ) {
        return new GetProductJpaRepositoryAdapter(repository);
    }

    @Bean
    GetProductRepository getProductRepository(
            GetProductJpaRepositoryAdapter adapter
    ) {
        return adapter;
    }
}