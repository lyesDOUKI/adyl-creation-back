package ld.bootstrap.api.hal.conf;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class ApiLinksConfig {

    @Bean
    OpenApiCustomizer halLinksSchemaCustomizer(ApiLinksOperationCustomizer customizer) {
        return customizer.schemaCustomizer();
    }
}