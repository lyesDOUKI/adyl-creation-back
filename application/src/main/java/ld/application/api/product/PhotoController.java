package ld.application.api.product;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotEmpty;
import ld.application.api.docs.ApiBadRequestResponse;
import ld.application.api.docs.ApiInternalServerErrorResponse;
import ld.application.api.docs.ApiProcessProblemResponse;
import ld.application.api.docs.ApiResourceNotFoundResponse;
import ld.application.infra.security.AdminOnly;
import ld.application.problems.product.photos.AddProductPhotosProblem;
import ld.application.problems.product.photos.AddProductPhotosProblemMapper;
import ld.application.request.AddProductPhotosRequestMapper;
import ld.application.response.AddProductPhotosResponse;
import ld.domain.features.product.photos.AddProductPhotosUseCase;
import ld.domain.features.product.photos.ProductPhotoUrlResolver;
import ld.spring.web.lib.ApiProblems;
import ld.spring.web.lib.ApiResponseBody;
import ld.spring.web.lib.ResultToResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@RestController
@ApiInternalServerErrorResponse
@RequestMapping("/products/{id}/photos")
@Tag(name = "photos", description = "API pour la gestion des photos des produits")
public class PhotoController {

    private final Logger LOGGER = LoggerFactory.getLogger(PhotoController.class);

    private final AddProductPhotosUseCase addProductPhotosUseCase;
    private final ProductPhotoUrlResolver productPhotoUrlResolver;
    private final Path rootDirectory;
    public PhotoController(
            AddProductPhotosUseCase addProductPhotosUseCase,
            ProductPhotoUrlResolver productPhotoUrlResolver,
            @Value("${app.photos.storage-path}") String storagePath
    ) {
        this.addProductPhotosUseCase = addProductPhotosUseCase;
        this.productPhotoUrlResolver = productPhotoUrlResolver;
        this.rootDirectory = Path.of(storagePath).toAbsolutePath().normalize();
    }

    @AdminOnly
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Ajout de photos à un produit",
            description = "Ajoute une ou plusieurs photos à un produit de crochet adyl-creation existant"
    )
    @ApiBadRequestResponse
    @ApiResourceNotFoundResponse
    @ApiProcessProblemResponse
    @ApiProblems(AddProductPhotosProblem.class)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Photos ajoutées avec succès",
                    content = @Content(schema = @Schema(implementation = AddProductPhotosResponse.class)))
    })
    public ResponseEntity<ApiResponseBody> addPhotos(
            @PathVariable("id") UUID productId,
            @RequestParam("photos") @NotEmpty(message = "La liste des photos ne peut pas être vide") List<MultipartFile> photos,
            HttpServletRequest httpServletRequest
    ) {
        var response = this.addProductPhotosUseCase
                .execute(AddProductPhotosRequestMapper.toCommand(productId, photos))
                .map(result -> AddProductPhotosResponse.from(result, this.productPhotoUrlResolver));

        return ResultToResponse.created(response, AddProductPhotosResponse::productId, new AddProductPhotosProblemMapper(), httpServletRequest);
    }

    @GetMapping("/{fileName}")
    @Operation(
            summary = "Récupération d'une photo produit",
            description = "Renvoie le fichier binaire d'une photo stockée pour un produit donné"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Photo trouvée et renvoyée",
                    content = @Content(mediaType = "application/octet-stream")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Photo introuvable pour ce produit (réponse sans corps)",
                    content = @Content
            )
    })
    public ResponseEntity<byte[]> get(
            @PathVariable("id") UUID productId,
            @PathVariable("fileName") String fileName
    ) {
        Path filePath = this.rootDirectory.resolve(productId + "/" + fileName).normalize();

        if (!filePath.startsWith(this.rootDirectory) || !Files.exists(filePath)) {
            LOGGER.warn("Warning: path does not exists");
            return ResponseEntity.notFound().build();
        }

        try {
            String contentType = Files.probeContentType(filePath);
            byte[] content = Files.readAllBytes(filePath);
            LOGGER.info("Success storing photo named {} for productId {}", productId, filePath);
            return ResponseEntity.ok()
                    .contentType(contentType != null
                            ? MediaType.parseMediaType(contentType)
                            : MediaType.APPLICATION_OCTET_STREAM)
                    .cacheControl(CacheControl.maxAge(Duration.ofDays(7)))
                    .body(content);
        } catch (IOException e) {
            throw new UncheckedIOException("Erreur lors de la lecture du fichier photo : " + filePath, e);
        }
    }
}