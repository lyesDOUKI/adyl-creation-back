package ld.application.infra.security;

import ld.application.api.docs.ApiForbiddenResponse;
import ld.application.api.docs.ApiUnauthorizedResponse;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@ApiUnauthorizedResponse
@ApiForbiddenResponse
@Target({
        ElementType.TYPE,
        ElementType.METHOD
})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasRole('ADMIN')")
public @interface AdminOnly {
}
