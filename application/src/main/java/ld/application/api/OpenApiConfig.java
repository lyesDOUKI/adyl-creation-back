package ld.application.api;

import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenApiCustomizer apiInfo(@Value("${app.version:1.0.0}") String version) {
        return openApi -> openApi.info(new Info()
                .title("Adyl Creation API")
                .version(version)
                .description("""
                        API de gestion des commandes, produits et rendez-vous d'Adyl Creation.
                        Les erreurs sont retournées en `application/problem+json` (RFC 9457) :
                        Le champ `code` est l'identifiant unique d'une erreur."""));
    }
}