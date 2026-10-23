package ld.application.problems;

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.Discriminator;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import ld.application.problems.request.RequestErrorCode;
import ld.application.problems.security.SecurityErrorCode;
import ld.application.problems.server.ServerErrorCode;
import ld.spring.web.lib.ApiErrorCode;
import ld.spring.web.lib.ApiProblems;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.method.HandlerMethod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.util.Comparator.comparingInt;

/**
 * Génère toutes les réponses d'erreur : celles déclarées via @ApiProblems, plus celles déduites
 * de la signature du endpoint (body, pagination, multipart, paramètres, sécurité, 500).
 * Une réponse par statut : un oneOf discriminé sur `code`.
 */
@Component
public class ApiProblemsCustomizer implements OperationCustomizer, OpenApiCustomizer {

    private static final String DISCRIMINATOR = "code";
    private static final String REF_PREFIX = "#/components/schemas/";
    private static final Set<String> COMMON_FIELDS = Set.of("code", "title", "status", "detail");

    private final Map<String, Schema<?>> components = new ConcurrentHashMap<>();

    @Override
    public Operation customize(Operation operation, HandlerMethod method) {
        if (operation.getResponses() == null) operation.setResponses(new ApiResponses());

        codesOf(method).stream()
                .collect(Collectors.groupingBy(
                        ApiErrorCode::status,
                        () -> new TreeMap<>(comparingInt(HttpStatus::value)),
                        Collectors.toList()))
                .forEach((status, codes) -> operation.getResponses()
                        .addApiResponse(String.valueOf(status.value()), response(status, codes)));

        var declared = method.getMethodAnnotation(ApiProblems.class);
        if (declared != null) Arrays.stream(declared.value()).forEach(this::registerEnum);
        return operation;
    }

    @Override
    public void customise(OpenAPI openApi) {
        if (openApi.getComponents() == null) openApi.setComponents(new Components());
        components.forEach((name, schema) -> openApi.getComponents().addSchemas(name, schema));

        openApi.getComponents().getSchemas().values().forEach(s -> {
            if (s.getProperties() != null && (s.getTypes() == null || s.getTypes().isEmpty())) {
                s.addType("object");
            }
        });
    }

    private List<ApiErrorCode> codesOf(HandlerMethod method) {
        var codes = new LinkedHashSet<ApiErrorCode>();

        var declared = method.getMethodAnnotation(ApiProblems.class);
        if (declared != null) {
            Arrays.stream(declared.value()).forEach(type -> codes.addAll(List.of(constantsOf(type))));
        }

        var params = method.getMethodParameters();
        var mapping = AnnotatedElementUtils.findMergedAnnotation(method.getMethod(), RequestMapping.class);
        boolean multipart = mapping != null
                && Arrays.asList(mapping.consumes()).contains(MediaType.MULTIPART_FORM_DATA_VALUE);

        if (Arrays.stream(params).anyMatch(p -> p.hasParameterAnnotation(RequestBody.class))) {
            codes.add(RequestErrorCode.VALIDATION_FAILED);
            codes.add(RequestErrorCode.INVALID_REQUEST_BODY);
        }
        if (multipart) {
            codes.add(RequestErrorCode.INVALID_MULTIPART_REQUEST);
            codes.add(RequestErrorCode.VALIDATION_FAILED);
        }
        if (Arrays.stream(params).anyMatch(p -> Pageable.class.isAssignableFrom(p.getParameterType()))) {
            codes.add(RequestErrorCode.INVALID_SORT_FIELD);
        }
        if (Arrays.stream(params).anyMatch(p ->
                p.hasParameterAnnotation(PathVariable.class) || p.hasParameterAnnotation(RequestParam.class))) {
            codes.add(RequestErrorCode.INVALID_PARAMETER);
        }

        if (ApiAccess.isSecured(method)) codes.addAll(List.of(SecurityErrorCode.values()));
        codes.add(ServerErrorCode.INTERNAL_ERROR);
        return List.copyOf(codes);
    }

    private ApiResponse response(HttpStatus status, List<ApiErrorCode> codes) {
        var union = new Schema<>();
        var discriminator = new Discriminator().propertyName(DISCRIMINATOR);

        for (var code : codes) {
            var ref = REF_PREFIX + registerVariant(code);
            union.addOneOfItem(new Schema<>().$ref(ref));
            discriminator.mapping(code.name(), ref);
        }
        union.setDiscriminator(discriminator);

        return new ApiResponse()
                .description(describe(status, codes))
                .content(new Content().addMediaType(
                        MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                        new io.swagger.v3.oas.models.media.MediaType().schema(union)));
    }

    private String describe(HttpStatus status, List<ApiErrorCode> codes) {
        var list = codes.stream()
                .sorted(Comparator.comparing(ApiErrorCode::name))
                .map(this::formatCodeDescription)
                .collect(Collectors.joining("\n"));
        return summary(status) + "\n\n" + list;
    }

    private String formatCodeDescription(ApiErrorCode code) {
        String base = "- `%s` : %s".formatted(code.name(), code.title());
        List<String> extraFields = getAdditionalFields(code);
        if (!extraFields.isEmpty()) {
            base += " (champs additionnels : " + String.join(", ", extraFields) + ")";
        }
        return base;
    }

    private List<String> getAdditionalFields(ApiErrorCode code) {
        if (code.payload() == null) return List.of();

        return Arrays.stream(code.payload().getDeclaredFields())
                .map(java.lang.reflect.Field::getName)
                .filter(name -> !COMMON_FIELDS.contains(name))
                .sorted()
                .toList();
    }

    private static String summary(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST -> "Requête invalide";
            case UNAUTHORIZED -> "Authentification manquante ou invalide";
            case FORBIDDEN -> "Accès refusé";
            case NOT_FOUND -> "Ressource introuvable";
            case CONFLICT -> "Conflit avec l'état actuel de la ressource";
            case UNPROCESSABLE_CONTENT -> "Règle métier non respectée";
            case INTERNAL_SERVER_ERROR -> "Erreur interne du serveur";
            default -> status.getReasonPhrase();
        };
    }

    private String registerVariant(ApiErrorCode code) {
        var name = code.schemaName();
        if (components.containsKey(name)) return name;

        var resolved = ModelConverters.getInstance(true)   // true = OpenAPI 3.1
                .resolveAsResolvedSchema(new AnnotatedType(code.payload()).resolveAsRef(false));
        Schema<?> schema = resolved.schema;

        resolved.referencedSchemas.forEach((n, s) -> {
            if (s != schema && !n.equals(code.payload().getSimpleName())) {
                s.setDescription(null);
                components.putIfAbsent(n, s);
            }
        });

        if (schema.getTypes() == null || schema.getTypes().isEmpty()) schema.addType("object");

        schema.addProperty(DISCRIMINATOR, new StringSchema()._enum(List.of(code.name()))
                .description("Code stable de l'erreur (seul à utiliser pour la logique client)"));
        schema.addProperty("title", new StringSchema()
                .description("Titre court, destiné à un humain").example(code.title()));
        schema.addProperty("status", new IntegerSchema()._enum(List.of(code.status().value()))
                .description("Statut HTTP"));

        Schema<?> detail = schema.getProperties().get("detail");
        if (detail != null) detail.setDescription("Explication lisible, destinée à un humain");

        schema.getProperties().values().forEach(p -> {
            if (p.getItems() != null) p.getItems().setDescription(null);
        });

        schema.setTitle(code.title());
        schema.setRequired(new ArrayList<>(schema.getProperties().keySet()));

        components.putIfAbsent(name, schema);
        return name;
    }

    private void registerEnum(Class<? extends ApiErrorCode> type) {
        var constants = constantsOf(type);
        var schema = new StringSchema();
        Arrays.stream(constants).forEach(c -> schema.addEnumItem(c.name()));
        schema.setDescription(Arrays.stream(constants)
                .map(this::formatCodeDescription)
                .collect(Collectors.joining("\n")));
        components.putIfAbsent(type.getSimpleName(), schema);
    }

    private static ApiErrorCode[] constantsOf(Class<? extends ApiErrorCode> type) {
        var constants = type.getEnumConstants();
        if (constants == null) throw new IllegalStateException(type + " doit être un enum");
        return constants;
    }
}