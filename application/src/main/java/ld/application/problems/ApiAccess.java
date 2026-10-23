package ld.application.problems;

import ld.application.infra.security.AdminOnly;
import ld.application.infra.security.CustomerOnly;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.method.HandlerMethod;

import java.lang.annotation.Annotation;

public final class ApiAccess {
    private ApiAccess() {}

    public static boolean isSecured(HandlerMethod method) {
        return has(method, AdminOnly.class) || has(method, CustomerOnly.class);
    }

    private static boolean has(HandlerMethod m, Class<? extends Annotation> a) {
        return AnnotatedElementUtils.hasAnnotation(m.getMethod(), a)
                || AnnotatedElementUtils.hasAnnotation(m.getBeanType(), a);
    }
}