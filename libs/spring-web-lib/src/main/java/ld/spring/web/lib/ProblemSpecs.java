package ld.spring.web.lib;

import ld.standard.lib.validation.ErrorCode;
import org.springframework.http.HttpStatus;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

public final class ProblemSpecs {

    private ProblemSpecs() { }

    public static ProblemDetailResponse toResponse(ApiProblem p) {
        var code = codeOf(p.getClass());
        return switch (p) {
            case ConflictProblem _ -> Problems.conflict(code, p.title(), p.detail(), p.properties());
            case NotFoundProblem _ -> Problems.notFound(code, p.title(), p.detail(), p.properties());
            case UnprocessableProblem _ -> Problems.unprocessable(code, p.title(), p.detail(), p.properties());
            default -> throw new IllegalStateException("Statut non défini pour " + p.getClass());
        };
    }

    public static HttpStatus statusOf(Class<?> type) {
        if (ConflictProblem.class.isAssignableFrom(type)) return HttpStatus.CONFLICT;
        if (NotFoundProblem.class.isAssignableFrom(type)) return HttpStatus.NOT_FOUND;
        if (UnprocessableProblem.class.isAssignableFrom(type)) return HttpStatus.UNPROCESSABLE_CONTENT;
        throw new IllegalStateException("Statut non défini pour " + type);
    }

    public static ErrorCode codeOf(Class<?> type) {
        for (Annotation a : type.getAnnotations()) {
            if (a.annotationType().isAnnotationPresent(ProblemCode.class)) {
                try {
                    return (ErrorCode) a.annotationType().getMethod("value").invoke(a);
                } catch (ReflectiveOperationException e) {
                    throw new IllegalStateException("value() illisible sur " + a.annotationType(), e);
                }
            }
        }
        throw new IllegalStateException("Aucun @ProblemCode (ex. @OrderProblem) sur " + type);
    }

    public static List<Class<?>> leaves(Class<?> sealedType) {
        if (!sealedType.isSealed()) return List.of(sealedType);
        return Arrays.stream(sealedType.getPermittedSubclasses())
                .flatMap(s -> leaves(s).stream()).toList();
    }
}
