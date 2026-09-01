package ld.application.config;

import ld.domain.features.product.model.ProductEvent;
import ld.standard.lib.AggregateEventDispatcher;
import ld.standard.lib.helper.test.InMemoryAggregateEventDispatcher;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class ProductEventDispatcherTestConfiguration {

    @Bean
    AggregateEventDispatcher<ProductEvent> productEventAggregateEventDispatcher() {
        return new InMemoryAggregateEventDispatcher<>();
    }
}
