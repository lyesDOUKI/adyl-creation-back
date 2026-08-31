package ld.application.api.order;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import ld.application.request.CreateOrderRequest;
import ld.application.request.RejectOrderRequest;
import ld.application.response.CreateOrderResponse;
import ld.application.response.OrderAcceptedResponse;
import ld.application.response.OrderRejectedResponse;
import ld.domain.features.order.CreateOrderUseCase;
import ld.domain.features.order.accept.AcceptOrderCommand;
import ld.domain.features.order.accept.AcceptOrderUseCase;
import ld.domain.features.order.reject.RejectOrderUseCase;
import ld.spring.web.lib.ApiResponseBody;
import ld.spring.web.lib.ResultToResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/orders")
@Tag(name = "Commandes", description = "API pour la création d'une commande")
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final AcceptOrderUseCase acceptOrderUseCase;
    private final RejectOrderUseCase rejectOrderUseCase;

    public OrderController(CreateOrderUseCase createOrderUseCase,
                           AcceptOrderUseCase acceptOrderUseCase,
                           RejectOrderUseCase rejectOrderUseCase) {
        this.createOrderUseCase = createOrderUseCase;
        this.acceptOrderUseCase = acceptOrderUseCase;
        this.rejectOrderUseCase = rejectOrderUseCase;
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
                    responseCode = "422",
                    description = "Incohérence sur la création de la demande d'une commande",
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

    @PostMapping("{id}/accept")
    @Operation(
            summary = "Accepter une commande",
            description = "Accepte une commande en attente et applique, le cas échéant, la remise de première commande."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Commande acceptée avec succès",
                    content = @Content(
                            schema = @Schema(implementation = OrderAcceptedResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Identifiant de commande invalide",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Commande inexistante",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "La commande ne peut pas être acceptée dans son état actuel",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erreur interne du serveur",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponseBody> acceptOrder(
            @Parameter(
                    description = "Identifiant de la commande à accepter",
                    required = true
            )
            @PathVariable("id") UUID orderId,
            HttpServletRequest httpServletRequest
    ) {
        var response = this.acceptOrderUseCase
                .execute(new AcceptOrderCommand(orderId))
                .map(OrderAcceptedResponse::from);

        return ResultToResponse.created(
                response,
                OrderAcceptedResponse::orderId,
                httpServletRequest
        );
    }

    @PostMapping("{id}/reject")
    @Operation(
            summary = "Rejeter une commande"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Commande rejetée avec succès",
                    content = @Content(
                            schema = @Schema(implementation = OrderAcceptedResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Identifiant de commande invalide",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Commande inexistante",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "La commande ne peut pas être rejetée dans son état actuel",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erreur interne du serveur",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponseBody> rejectOrder(
            @Parameter(
                    description = "Identifiant de la commande à rejeter",
                    required = true
            )
            @PathVariable("id") UUID orderId,
            @RequestBody RejectOrderRequest rejectOrderRequest,
            HttpServletRequest httpServletRequest
    ) {
        var response = this.rejectOrderUseCase
                .execute(rejectOrderRequest.to(orderId))
                .map(OrderRejectedResponse::from);

        return ResultToResponse.created(
                response,
                OrderRejectedResponse::orderId,
                httpServletRequest
        );
    }
}
