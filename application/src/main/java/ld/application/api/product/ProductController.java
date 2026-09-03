package ld.application.api.product;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import ld.application.infra.security.AdminOnly;
import ld.application.read.GetProductService;
import ld.application.request.CreateProductRequest;
import ld.application.response.CreateProductResponse;
import ld.application.response.GetProductResponse;
import ld.domain.features.product.CreateProductUseCase;
import ld.spring.web.lib.ApiResponseBody;
import ld.spring.web.lib.PageResponse;
import ld.spring.web.lib.ResultToResponse;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/products")
@Tag(name = "Produits", description = "API pour la gestion des produits")
public class ProductController {

    private final CreateProductUseCase createProductUseCase;
    private final GetProductService getProductService;

    public ProductController(CreateProductUseCase createProductUseCase,
                             GetProductService getProductService) {
        this.createProductUseCase = createProductUseCase;
        this.getProductService = getProductService;
    }

    @AdminOnly
    @PostMapping
    @Operation(
            summary = "Création d'un produit",
            description = "Créer un nouveau produit de crochet adyl-creation"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Produit créé avec succès",
                    content = @Content(schema = @Schema(implementation = CreateProductResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Requête invalide",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Incohérence sur la création du produit",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erreur interne du serveur",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponseBody> create(@RequestBody @Valid CreateProductRequest createProductRequest,
                                                  HttpServletRequest httpServletRequest) {
        var response = this.createProductUseCase.execute(createProductRequest.to())
                .map(CreateProductResponse::from);
        return ResultToResponse.created(response, CreateProductResponse::productId, httpServletRequest);
    }

    @GetMapping
    @Operation(
            summary = "Récupération des produits"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Produits récupéré avec succès"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Requête invalide",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erreur interne du serveur",
                    content = @Content
            )
    })
    public ResponseEntity<PageResponse<GetProductResponse>> get(@ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(PageResponse.from(this.getProductService.findAll(pageable)));
    }

    @GetMapping("{id}")
    @Operation(
            summary = "Récupération d'un produit"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Produit récupéré avec succès",
                    content = @Content(schema = @Schema(implementation = GetProductResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Produit introuvable",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Requête invalide",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erreur interne du serveur",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponseBody> getById(@PathVariable("id") UUID productId, HttpServletRequest httpServletRequest) {
        return ResultToResponse.ok(this.getProductService.findById(productId), httpServletRequest);
    }
}
