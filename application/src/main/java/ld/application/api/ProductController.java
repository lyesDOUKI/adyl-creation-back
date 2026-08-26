package ld.application.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import ld.application.read.GetProductService;
import ld.application.request.CreateProductRequest;
import ld.application.response.CreateOrderResponse;
import ld.application.response.CreateProductResponse;
import ld.application.response.GetProductsResponse;
import ld.domain.features.product.CreateProductUseCase;
import ld.spring.web.lib.ApiResponseBody;
import ld.spring.web.lib.ResultToResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PostMapping
    @Operation(
            summary = "Création d'un produit",
            description = "Créer un nouveau produit de crochet adyl-creation"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Produit créé avec succès",
                    content = @Content(schema = @Schema(implementation = CreateOrderResponse.class))
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
                    description = "Produits récupéré avec succès",
                    content = @Content(schema = @Schema(implementation = GetProductsResponse.class))
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
    public ResponseEntity<List<GetProductsResponse>> get() {
        return ResponseEntity.ok(this.getProductService.findAll());
    }

}
