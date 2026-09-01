package ld.application.config.order;

import ld.application.infra.db.jpa.adapter.DiscountClaimerJpaAdapter;
import ld.application.infra.db.jpa.adapter.OrderCreatorJpaAdapter;
import ld.application.infra.db.jpa.adapter.OrderEditorJpaAdapter;
import ld.application.infra.db.jpa.adapter.ProductJpaFinderAdapter;
import ld.application.infra.db.jpa.DiscountClaimJpaRepository;
import ld.application.infra.db.jpa.OrderJpaRepository;
import ld.application.infra.db.jpa.ProductJpaRepository;
import ld.application.infra.db.jooq.CustomerOrderHistoryFinderJooqAdapter;
import org.jooq.DSLContext;
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
    OrderCreatorJpaAdapter orderCreatorJpaAdapter(
            OrderJpaRepository repository
    ) {
        return new OrderCreatorJpaAdapter(repository);
    }

    @Bean
    OrderEditorJpaAdapter orderEditorJpaAdapter(OrderJpaRepository orderJpaRepository) {
        return new OrderEditorJpaAdapter(orderJpaRepository);
    }

    @Bean
    ProductJpaFinderAdapter productJpaFinderAdapter(
            ProductJpaRepository repository
    ) {
        return new ProductJpaFinderAdapter(repository);
    }


    @Bean
    DiscountClaimerJpaAdapter discountClaimerJpaAdapter(DiscountClaimJpaRepository discountClaimJpaRepository) {
        return new DiscountClaimerJpaAdapter(discountClaimJpaRepository);
    }


    @Bean
    CustomerOrderHistoryFinderJooqAdapter customerOrderHistoryFinderJooqAdapter(DSLContext dslContext) {
        return new CustomerOrderHistoryFinderJooqAdapter(dslContext);
    }
}