package ld.application.api;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenApiCustomizer apiInfo() throws IOException {
        var resource = new ClassPathResource("META-INF/version.txt");
        String apiVersion;

        try (var inputStream = resource.getInputStream()) {
            apiVersion = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8).trim();
        }

        if (apiVersion.isBlank()) {
            throw new IllegalStateException(
                    "La version du contrat API ne peut pas être vide."
            );
        }

        return openApi -> openApi.info(new Info()
                .title("Adyl Creation API")
                .version(apiVersion)
                .description("""
                        API de gestion des commandes, produits et rendez-vous d'Adyl Creation.
                        Les erreurs sont retournées en `application/problem+json` (RFC 9457) :
                        le champ `code` est l'identifiant unique d'une erreur."""));
    }

    @Bean
    @Profile("openapi")
    OpenApiCustomizer relativeServerCustomizer() {
        return openApi -> openApi.servers(List.of(
                new Server()
                        .url("/")
                        .description("Résolu dynamiquement selon l'hôte de déploiement")
        ));
    }
}