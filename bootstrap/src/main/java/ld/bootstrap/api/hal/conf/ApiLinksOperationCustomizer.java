package ld.bootstrap.api.hal.conf;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Schema;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
class ApiLinksOperationCustomizer implements OperationCustomizer {

    // nom du schéma -> rels à exposer
    private final Map<String, List<ApiLinkRel>> linksBySchema = new ConcurrentHashMap<>();

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        ApiLinks annotation = handlerMethod.getMethodAnnotation(ApiLinks.class);
        if (annotation == null || operation.getResponses() == null) {
            return operation;
        }
        var ok = operation.getResponses().get("200");
        if (ok == null) {
            return operation;
        }

        ApiLinkRel[] rels = annotation.value().getEnumConstants();
        StringBuilder doc = new StringBuilder("\n\n**Liens HAL possibles (`_links`)**\n");

        for (ApiLinkRel rel : rels) {
            var link = new io.swagger.v3.oas.models.links.Link()
                    .operationId(rel.operationId())
                    .description(rel.description());
            link.setParameters(rel.parameters());
            ok.addLink(rel.rel(), link);

            doc.append("- `").append(rel.rel()).append("` : ").append(rel.description()).append('\n');
        }
        operation.setDescription(
                (operation.getDescription() == null ? "" : operation.getDescription()) + doc);

        if (ok.getContent() != null) {
            ok.getContent().values().forEach(mt -> {
                if (mt.getSchema() != null && mt.getSchema().get$ref() != null) {
                    String ref = mt.getSchema().get$ref();
                    linksBySchema.put(ref.substring(ref.lastIndexOf('/') + 1), List.of(rels));
                }
            });
        }
        return operation;
    }

    OpenApiCustomizer schemaCustomizer() {
        return openApi -> linksBySchema.forEach((schemaName, rels) -> {
            Schema<?> schema = openApi.getComponents().getSchemas().get(schemaName);
            if (schema == null || schema.getProperties() == null) return;
            Schema<?> linksProp = (Schema<?>) schema.getProperties().get("_links");
            if (linksProp == null) return;

            Map<String, Object> example = new LinkedHashMap<>();
            for (ApiLinkRel rel : rels) {
                String path = findPath(openApi, rel.operationId());
                example.put(rel.rel(), Map.of("href", path));
            }
            linksProp.setExample(example);
        });
    }

    private static String findPath(OpenAPI openApi, String operationId) {
        return openApi.getPaths().entrySet().stream()
                .filter(e -> e.getValue().readOperations().stream()
                        .anyMatch(op -> operationId.equals(op.getOperationId())))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Aucune opération OpenAPI avec operationId='" + operationId
                                + "' : l'enum de liens est désynchronisé du controller"));
    }
}