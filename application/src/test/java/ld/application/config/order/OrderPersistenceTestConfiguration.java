package ld.application.config.order;

import ld.application.infra.db.jooq.CustomerOrderHistoryFinderJooqAdapter;
import ld.application.infra.db.jooq.JooqGetOrderQueryRepository;
import ld.application.infra.db.jpa.OrderJpaRepository;
import ld.application.infra.db.jpa.ProductJpaRepository;
import ld.application.infra.db.jpa.adapter.DiscountClaimerJooqAdapter;
import ld.application.infra.db.jpa.adapter.OrderCreatorJpaAdapter;
import ld.application.infra.db.jpa.adapter.OrderEditorJpaAdapter;
import ld.application.infra.db.jpa.adapter.ProductJpaFinderAdapter;
import ld.application.infra.db.read.GetOrderQueryRepository;
import org.jooq.DSLContext;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
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
    DiscountClaimerJooqAdapter discountClaimerJooqAdapter(DSLContext dslContext) {
        return new DiscountClaimerJooqAdapter(dslContext);
    }


    @Bean
    CustomerOrderHistoryFinderJooqAdapter customerOrderHistoryFinderJooqAdapter(DSLContext dslContext) {
        return new CustomerOrderHistoryFinderJooqAdapter(dslContext);
    }

    @Bean
    GetOrderQueryRepository getOrderQueryRepository(DSLContext dslContext) {
        return new JooqGetOrderQueryRepository(dslContext);
    }

    @Bean
    @Primary
    SwitchableOrderEditor switchableOrderEditor(
            OrderEditorJpaAdapter delegate) {
        return new SwitchableOrderEditor(delegate);
    }

    @Bean
    @Primary
    SwitchableOrderCreator switchableOrderCreator(
            OrderCreatorJpaAdapter delegate) {
        return new SwitchableOrderCreator(delegate);
    }
}