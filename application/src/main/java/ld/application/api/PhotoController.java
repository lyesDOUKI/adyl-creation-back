package ld.application.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotEmpty;
import ld.application.request.AddProductPhotosRequestMapper;
import ld.application.response.AddProductPhotosResponse;
import ld.domain.features.product.photos.AddProductPhotosUseCase;
import ld.domain.features.product.photos.ProductPhotoUrlResolver;
import ld.spring.web.lib.ApiResponseBody;
import ld.spring.web.lib.ResultToResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/{id}/photos")
@Tag(name = "photos", description = "API pour la gestion des photos des produits")
public class PhotoController {

    private final AddProductPhotosUseCase addProductPhotosUseCase;
    private final ProductPhotoUrlResolver productPhotoUrlResolver;

    public PhotoController(
            AddProductPhotosUseCase addProductPhotosUseCase,
            ProductPhotoUrlResolver productPhotoUrlResolver
    ) {
        this.addProductPhotosUseCase = addProductPhotosUseCase;
        this.productPhotoUrlResolver = productPhotoUrlResolver;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Ajout de photos à un produit",
            description = "Ajoute une ou plusieurs photos à un produit de crochet adyl-creation existant"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Photos ajoutées avec succès",
                    content = @Content(schema = @Schema(implementation = AddProductPhotosResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Requête invalide",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Produit introuvable",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Incohérence métier sur les photos fournies",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erreur interne du serveur",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponseBody> addPhotos(
            @PathVariable UUID productId,
            @RequestParam("photos") @NotEmpty(message = "La liste des photos ne peut pas être vide") List<MultipartFile> photos,
            HttpServletRequest httpServletRequest
    ) {
        var response = this.addProductPhotosUseCase
                .execute(AddProductPhotosRequestMapper.toCommand(productId, photos))
                .map(result -> AddProductPhotosResponse.from(result, this.productPhotoUrlResolver));

        return ResultToResponse.created(response, AddProductPhotosResponse::productId, httpServletRequest);
    }
}