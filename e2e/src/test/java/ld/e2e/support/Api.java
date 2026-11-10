package ld.e2e.support;

import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.mapper.ObjectMapperDeserializationContext;
import io.restassured.mapper.ObjectMapperSerializationContext;
import io.restassured.specification.RequestSpecification;
import no.nav.security.mock.oauth2.MockOAuth2Server;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;

/** Point d'entrée HTTP des tests : une requête préconfigurée par acteur. */
public final class Api {

    private final int port;
    private final String basePath;
    private final JsonMapper jsonMapper;
    private final MockOAuth2Server oidc;
    private final String issuer;
    private final String audience;

    public Api(int port, String basePath, JsonMapper jsonMapper,
               MockOAuth2Server oidc, String issuer, String audience) {
        this.port = port;
        this.basePath = basePath;
        this.jsonMapper = jsonMapper;
        this.oidc = oidc;
        this.issuer = issuer;
        this.audience = audience;
    }

    public RequestSpecification anonymous() {
        io.restassured.mapper.ObjectMapper appMapper = new io.restassured.mapper.ObjectMapper() {
            @Override
            public Object serialize(ObjectMapperSerializationContext context) {
                return jsonMapper.writeValueAsString(context.getObjectToSerialize());
            }

            @Override
            public Object deserialize(ObjectMapperDeserializationContext context) {
                return jsonMapper.readValue(
                        context.getDataToDeserialize().asString(),
                        jsonMapper.constructType(context.getType()));
            }
        };

        return RestAssured.given()
                .config(RestAssuredConfig.config()
                        .objectMapperConfig(new ObjectMapperConfig().defaultObjectMapper(appMapper)))
                .port(port)
                .basePath(basePath)
                .contentType(ContentType.JSON)
                .log().ifValidationFails();
    }

    public RequestSpecification as(Actor actor) {
        return anonymous().header("Authorization", "Bearer " + token(actor));
    }

    public RequestSpecification admin() {
        return as(Actor.ADMIN);
    }

    public RequestSpecification customer() {
        return as(Actor.CUSTOMER);
    }

    public RequestSpecification otherCustomer() {
        return as(Actor.OTHER_CUSTOMER);
    }

    public String token(Actor actor) {
        Map<String, Object> claims = Map.of("realm_access", Map.of("roles", List.of(actor.role())));
        return oidc.issueToken(issuer, actor.subject().toString(), audience, claims).serialize();
    }
}