package ld.application.response;

import io.swagger.v3.oas.annotations.media.Schema;
import ld.spring.web.lib.ApiResponseBody;
import org.springframework.data.domain.Page;

import java.util.List;

@Schema(
        description = """
                Représente une page de résultats.
                Contient les éléments de la page courante et les métadonnées
                nécessaires à la navigation entre les pages.
                """
)
public record PageResponse<T>(

        @Schema(
                description = "Éléments contenus dans la page courante. La liste peut être vide.",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        List<T> content,

        @Schema(
                description = "Index de la page courante, à partir de zéro.",
                example = "0",
                minimum = "0",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        int page,

        @Schema(
                description = "Taille de la page utilisée pour la pagination.",
                example = "20",
                minimum = "1",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        int size,

        @Schema(
                description = "Nombre total d'éléments correspondant à la requête, toutes pages confondues.",
                example = "125",
                minimum = "0",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        long totalElements,

        @Schema(
                description = "Nombre total de pages disponibles.",
                example = "7",
                minimum = "0",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        int totalPages

) implements ApiResponseBody {

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}