package ld.application.infra.security;

import org.jspecify.annotations.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class JwtAuthConverter
        implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtAuthenticationConverter delegate =
            new JwtAuthenticationConverter();
    private final JwtProperties jwtProperties;
    public JwtAuthConverter(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;

        delegate.setJwtGrantedAuthoritiesConverter(
                this::extractAuthorities
        );
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {

        var realmAccess = jwt.getClaimAsMap("realm_access");

        if (realmAccess == null) {
            return List.of();
        }

        var roles = realmAccess.get("roles");

        if (!(roles instanceof Collection<?> roleCollection)) {
            return List.of();
        }

        return roleCollection.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .map(role -> new SimpleGrantedAuthority(
                        "ROLE_" + role
                ))
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public AbstractAuthenticationToken convert(@NonNull Jwt jwt) {
        return delegate.convert(jwt);
    }

    public Jwt fromAuthentication(@NonNull Authentication authentication) {
        return (Jwt) authentication.getPrincipal();
    }

    public String getPrincipalClaimName(Jwt jwt) {
        var claimName = Objects.nonNull(this.jwtProperties.getPrincipalAttribute()) ?
                jwtProperties.getPrincipalAttribute() : JwtClaimNames.SUB;
        return jwt.getClaim(claimName);
    }
}
