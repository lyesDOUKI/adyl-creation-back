package ld.application.problems;

import io.swagger.v3.oas.models.Operation;
import ld.spring.web.lib.ApiProblems;
import ld.spring.web.lib.ProblemSpecs;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class ApiProblemsCustomizer implements OperationCustomizer {

    @Override
    public Operation customize(Operation operation, HandlerMethod method) {
        var annotation = method.getMethodAnnotation(ApiProblems.class);
        if (annotation == null) return operation;

        Map<HttpStatus, List<String>> byStatus = new TreeMap<>(Comparator.comparingInt(HttpStatus::value));
        for (Class<?> leaf : ProblemSpecs.leaves(annotation.value())) {
            byStatus.computeIfAbsent(ProblemSpecs.statusOf(leaf), _ -> new ArrayList<>())
                    .add(ProblemSpecs.codeOf(leaf).code());
        }

        byStatus.forEach((status, codes) -> {
            var response = operation.getResponses().get(String.valueOf(status.value()));
            if (response == null) {
                throw new IllegalStateException(
                        "Réponse %s non documentée sur %s".formatted(status.value(), method));
            }
            var list = codes.stream().map(c -> "- `" + c + "`").collect(Collectors.joining("\n"));
            response.setDescription(response.getDescription()
                    + "\n\nValeurs possibles de `code` :\n" + list);
        });
        return operation;
    }
}
