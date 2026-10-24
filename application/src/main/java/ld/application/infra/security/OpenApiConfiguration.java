package ld.application.infra.security;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class OpenApiConfiguration {

    private static final String SECURITY_SCHEME_NAME = "oAuth2";
    private static final String OPENID_SCOPE = "openid";
    private static final String OPENID_SCOPE_DESCRIPTION = "OpenID";

    @Bean
    @Profile("!openapi")
    public OpenAPI openAPI(
            @Value("${oidc.authorization-url}") String authorizationUrl,
            @Value("${oidc.token-url}") String tokenUrl
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
                )
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
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