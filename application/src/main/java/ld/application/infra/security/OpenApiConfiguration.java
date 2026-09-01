package ld.application.infra.security;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    private static final String SECURITY_SCHEME_NAME = "keycloak";
    private static final String OPENID_SCOPE = "openid";
    private static final String OPENID_SCOPE_DESCRIPTION = "OpenID";

    @Bean
    public OpenAPI openAPI(
            @Value("${keycloak.authorization-url}") String authorizationUrl,
            @Value("${keycloak.token-url}") String tokenUrl
    ) {
        var securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.OAUTH2)
                .flows(createOAuthFlows(authorizationUrl, tokenUrl));

        return new OpenAPI()
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        SECURITY_SCHEME_NAME,
                                        securityScheme
                                )
                );
    }

    private OAuthFlows createOAuthFlows(
            String authorizationUrl,
            String tokenUrl
    ) {
        var authorizationCodeFlow = new OAuthFlow()
                .authorizationUrl(authorizationUrl)
                .tokenUrl(tokenUrl)
                .scopes(
                        new Scopes()
                                .addString(
                                        OPENID_SCOPE,
                                        OPENID_SCOPE_DESCRIPTION
                                )
                );

        return new OAuthFlows()
                .authorizationCode(authorizationCodeFlow);
    }
}