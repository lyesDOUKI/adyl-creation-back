package ld.application.problems;

import io.swagger.v3.oas.models.Operation;
import ld.spring.web.lib.ApiProblems;
import ld.spring.web.lib.ProblemSpecs;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class ApiProblemsCustomizer implements OperationCustomizer {

    private static final String SAMPLE_UUID = "123e4567-e89b-12d3-a456-426614174000";
    private static final Pattern PATH_VARIABLE = Pattern.compile("\\{[^}]+\\}");

    @Override
    public Operation customize(Operation operation, HandlerMethod method) {
        injectInstancePath(operation, resolvePath(method));

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

    private static void injectInstancePath(Operation operation, String path) {
        if (operation.getResponses() == null || path == null || path.isEmpty()) return;

        var pattern = Pattern.compile("\"instance\"\\s*:\\s*\"[^\"]*\"");
        var replacement = Matcher.quoteReplacement("\"instance\": \"" + path + "\"");

        operation.getResponses().values().forEach(response -> {
            if (response.getContent() == null) return;
            response.getContent().values().forEach(mediaType -> {
                if (mediaType.getExample() != null) {
                    mediaType.setExample(pattern.matcher(mediaType.getExample().toString()).replaceAll(replacement));
                }
                if (mediaType.getExamples() != null) {
                    mediaType.getExamples().values().forEach(example -> {
                        if (example.getValue() != null) {
                            example.setValue(pattern.matcher(example.getValue().toString()).replaceAll(replacement));
                        }
                    });
                }
            });
        });
    }

    private static String resolvePath(HandlerMethod method) {
        var classMapping = AnnotatedElementUtils.findMergedAnnotation(method.getBeanType(), RequestMapping.class);
        var methodMapping = AnnotatedElementUtils.findMergedAnnotation(method.getMethod(), RequestMapping.class);

        var base = classMapping != null && classMapping.path().length > 0 ? classMapping.path()[0] : "";
        var sub  = methodMapping != null && methodMapping.path().length > 0 ? methodMapping.path()[0] : "";

        var combined = (base + "/" + sub).replaceAll("/+", "/");
        if (combined.length() > 1 && combined.endsWith("/")) {
            combined = combined.substring(0, combined.length() - 1);
        }
        if (combined.isEmpty()) return "/";

        return PATH_VARIABLE.matcher(combined).replaceAll(SAMPLE_UUID);
    }
}