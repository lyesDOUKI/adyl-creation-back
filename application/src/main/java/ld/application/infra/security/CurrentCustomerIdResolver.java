package ld.application.infra.security;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.UUID;

@Component
public class CurrentCustomerIdResolver implements HandlerMethodArgumentResolver {
    private final JwtAuthConverter jwtAuthConverter;

    public CurrentCustomerIdResolver(JwtAuthConverter jwtAuthConverter) {
        this.jwtAuthConverter = jwtAuthConverter;
    }

    @Override
    public boolean supportsParameter(@NonNull MethodParameter parameter) {
        return parameter.getParameterType().equals(UUID.class)
                && parameter.hasParameterAnnotation(CurrentCustomerId.class);
    }

    @Override
    public @Nullable Object resolveArgument(@NonNull MethodParameter parameter,
                                            @Nullable ModelAndViewContainer mavContainer,
                                            @NonNull NativeWebRequest webRequest,
                                            @Nullable WebDataBinderFactory binderFactory) {
        Authentication authentication = (Authentication) webRequest.getUserPrincipal();
        if (authentication == null) {
            throw new RuntimeException("Authentication is null, failed to resolve customer ID");
        }
        var jwt = jwtAuthConverter.fromAuthentication(authentication);
        return UUID.fromString(jwtAuthConverter.getPrincipalClaimName(jwt));
    }
}
