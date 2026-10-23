package ld.application.api.product;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import ld.application.infra.security.AdminOnly;
import ld.application.problems.product.CreateProductErrorCode;
import ld.application.problems.product.CreateProductProblem;
import ld.application.problems.product.get.GetProductErrorCode;
import ld.application.problems.product.get.GetProductProblem;
import ld.application.read.GetProductService;
import ld.application.request.CreateProductRequest;
import ld.application.response.CreateProductResponse;
import ld.application.response.GetProductResponse;
import ld.domain.features.product.CreateProductUseCase;
import ld.spring.web.lib.ApiProblems;
import ld.spring.web.lib.ApiResponseBody;
import ld.application.response.PageResponse;
import ld.spring.web.lib.ResultToResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/products")
@Tag(name = "Produits", description = "API pour la gestion des produits")
public class ProductController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductController.class);

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
            operationId = "createProduct",
            summary = "Création d'un produit",
            description = "Crée un nouveau produit de crochet adyl-creation."
    )
    @ApiProblems(CreateProductErrorCode.class)
    @ApiResponse(
            responseCode = "201",
            description = "Produit créé avec succès",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = CreateProductResponse.class)))
    public ResponseEntity<ApiResponseBody> create(
            @RequestBody @Valid CreateProductRequest createProductRequest,
            HttpServletRequest httpServletRequest) {

        var response = this.createProductUseCase.execute(createProductRequest.to())
                .map(CreateProductResponse::from);

        return ResultToResponse.created(
                response,
                CreateProductResponse::productId,
                CreateProductProblem::from,
                httpServletRequest);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @SecurityRequirements
    @Operation(
            operationId = "listProducts",
            summary = "Récupération des produits",
            description = "Retourne, de façon paginée, le catalogue des produits. Endpoint public."
    )
    public ResponseEntity<PageResponse<GetProductResponse>> listProducts(
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {

        LOGGER.info("Get products ...");
        return ResponseEntity.ok(PageResponse.from(this.getProductService.findAll(pageable)));
    }

    @GetMapping("{id}")
    @SecurityRequirements
    @Operation(
            operationId = "getProduct",
            summary = "Récupération d'un produit",
            description = "Retourne un produit du catalogue. Endpoint public."
    )
    @ApiProblems(GetProductErrorCode.class)
    @ApiResponse(
            responseCode = "200",
            description = "Produit récupéré avec succès",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = GetProductResponse.class)))
    public ResponseEntity<ApiResponseBody> getById(
            @Parameter(description = "Identifiant du produit")
            @PathVariable("id") UUID productId) {

        LOGGER.info("Loading id {} ...", productId);
        return ResultToResponse.okOrNotFound(
                this.getProductService.findById(productId),
                () -> new GetProductProblem.ProductNotFound(productId));
    }
}