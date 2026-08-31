package ld.application.config;

import ld.application.infra.db.jooq.DiscountProviderJooqAdapter;
import ld.domain.features.order.lifecycle.DiscountProvider;
import org.jooq.DSLContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DiscountProviderTestConfiguration {

    @Bean
    DiscountProvider discountProvider(DSLContext dslContext) {
        return new DiscountProviderJooqAdapter(dslContext);
    }
}
