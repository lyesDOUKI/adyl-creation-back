package ld.application.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import ld.application.request.CreateOrderRequest;
import ld.application.response.CreateOrderResponse;
import ld.domain.features.order.CreateOrderUseCase;
import ld.spring.web.lib.ApiResponseBody;
import ld.spring.web.lib.ResultToResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
@Tag(name = "Commandes", description = "API pour la création d'une commande")
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;

    public OrderController(CreateOrderUseCase createOrderUseCase) {
        this.createOrderUseCase = createOrderUseCase;
    }

    @PostMapping
    @Operation(
            summary = "Création d'une demande de commande",
            description = "Envoi la demande de commande pour prise en compte et traitement"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Demande de commande enregistré avec succès",
                    content = @Content(schema = @Schema(implementation = CreateOrderResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Requête invalide",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Produits demande inexistants dans l'application",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erreur interne du serveur",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponseBody> createOrder(
            @RequestBody @Valid CreateOrderRequest request,
            HttpServletRequest httpServletRequest
    ) {
        var response = this.createOrderUseCase.execute(request.toCommand())
                .map(CreateOrderResponse::from);
        return ResultToResponse.created(response, CreateOrderResponse::orderId, httpServletRequest);
    }
}
