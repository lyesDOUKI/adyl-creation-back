package ld.bootstrap.api.order;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import ld.bootstrap.infra.security.AdminOnly;
import ld.bootstrap.infra.security.CurrentCustomerId;
import ld.bootstrap.infra.security.CustomerOnly;
import ld.bootstrap.problems.order.accept.AcceptOrderErrorCode;
import ld.bootstrap.problems.order.accept.AcceptOrderProblem;
import ld.bootstrap.problems.order.cancel.CancelOrderErrorCode;
import ld.bootstrap.problems.order.cancel.CancelOrderProblem;
import ld.bootstrap.problems.order.create.CreateOrderErrorCode;
import ld.bootstrap.problems.order.create.CreateOrderProblem;
import ld.bootstrap.problems.order.deliver.DeliverOrderErrorCode;
import ld.bootstrap.problems.order.deliver.DeliverOrderProblem;
import ld.bootstrap.problems.order.get.GetOrderErrorCode;
import ld.bootstrap.problems.order.get.GetOrderProblem;
import ld.bootstrap.problems.order.reject.RejectOrderErrorCode;
import ld.bootstrap.problems.order.reject.RejectOrderProblem;
import ld.bootstrap.read.GetOrderService;
import ld.bootstrap.request.CreateOrderRequest;
import ld.bootstrap.request.DeliverOrderRequest;
import ld.bootstrap.request.RejectOrderRequest;
import ld.bootstrap.response.*;
import ld.domain.features.order.CreateOrderUseCase;
import ld.domain.features.order.accept.AcceptOrderCommand;
import ld.domain.features.order.accept.AcceptOrderUseCase;
import ld.domain.features.order.cancel.CancelOrderCommand;
import ld.domain.features.order.cancel.CancelOrderUseCase;
import ld.domain.features.order.deliver.DeliverOrderUseCase;
import ld.domain.features.order.reject.RejectOrderUseCase;
import ld.spring.web.lib.ApiProblems;
import ld.spring.web.lib.ApiResponseBody;
import ld.spring.web.lib.ResultToResponse;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/orders")
@Tag(name = "Commandes", description = "API pour la gestion des commandes")
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final AcceptOrderUseCase acceptOrderUseCase;
    private final RejectOrderUseCase rejectOrderUseCase;
    private final DeliverOrderUseCase deliverOrderUseCase;
    private final CancelOrderUseCase cancelOrderUseCase;
    private final GetOrderService getOrderService;

    public OrderController(CreateOrderUseCase createOrderUseCase,
                           AcceptOrderUseCase acceptOrderUseCase,
                           RejectOrderUseCase rejectOrderUseCase,
                           DeliverOrderUseCase deliverOrderUseCase,
                           CancelOrderUseCase cancelOrderUseCase,
                           GetOrderService getOrderService) {
        this.createOrderUseCase = createOrderUseCase;
        this.acceptOrderUseCase = acceptOrderUseCase;
        this.rejectOrderUseCase = rejectOrderUseCase;
        this.deliverOrderUseCase = deliverOrderUseCase;
        this.cancelOrderUseCase = cancelOrderUseCase;
        this.getOrderService = getOrderService;
    }

    @CustomerOnly
    @PostMapping
    @Operation(
            operationId = "createOrder",
            summary = "Création d'une demande de commande",
            description = "Envoie la demande de commande pour prise en compte et traitement.")
    @ApiProblems(CreateOrderErrorCode.class)
    @ApiResponse(
            responseCode = "201",
            description = "Demande de commande enregistrée avec succès",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = CreateOrderResponse.class)))
    public ResponseEntity<ApiResponseBody> createOrder(
            @Parameter(hidden = true) @CurrentCustomerId UUID customerId,
            @RequestBody @Valid CreateOrderRequest request,
            HttpServletRequest httpServletRequest
    ) {
        return ResultToResponse.created(
                createOrderUseCase.execute(request.toCommand(customerId)).map(CreateOrderResponse::from),
                CreateOrderResponse::orderId,
                CreateOrderProblem::from,
                httpServletRequest);
    }

    @AdminOnly
    @PostMapping("{id}/accept")
    @Operation(
            operationId = "acceptOrder",
            summary = "Accepter une commande",
            description = "Accepte une commande en attente et applique, le cas échéant, la remise de première commande."
    )
    @ApiProblems(AcceptOrderErrorCode.class)
    @ApiResponse(
            responseCode = "200",
            description = "Commande acceptée avec succès",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = OrderAcceptedResponse.class)))
    public ResponseEntity<ApiResponseBody> acceptOrder(
            @Parameter(description = "Identifiant de la commande à accepter")
            @PathVariable("id") UUID orderId
    ) {
        var response = this.acceptOrderUseCase
                .execute(new AcceptOrderCommand(orderId))
                .map(OrderAcceptedResponse::from);

        return ResultToResponse.ok(response, AcceptOrderProblem::from);
    }

    @AdminOnly
    @PostMapping("{id}/reject")
    @Operation(
            operationId = "rejectOrder",
            summary = "Rejeter une commande",
            description = "Rejette une commande, avec un motif optionnel. Une commande déjà livrée ne peut plus être rejetée."
    )
    @ApiProblems(RejectOrderErrorCode.class)
    @ApiResponse(
            responseCode = "200",
            description = "Commande rejetée avec succès",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = OrderRejectedResponse.class)))
    public ResponseEntity<ApiResponseBody> rejectOrder(
            @Parameter(description = "Identifiant de la commande à rejeter")
            @PathVariable("id") UUID orderId,
            @RequestBody @Valid RejectOrderRequest rejectOrderRequest
    ) {
        var response = this.rejectOrderUseCase
                .execute(rejectOrderRequest.to(orderId))
                .map(OrderRejectedResponse::from);

        return ResultToResponse.ok(response, RejectOrderProblem::from);
    }

    @AdminOnly
    @PostMapping("{id}/deliver")
    @Operation(
            operationId = "deliverOrder",
            summary = "Livrer une commande",
            description = "Marque une commande acceptée comme livrée, avec son mode de livraison."
    )
    @ApiProblems(DeliverOrderErrorCode.class)
    @ApiResponse(
            responseCode = "200",
            description = "Commande livrée avec succès",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = OrderDeliveredResponse.class)))
    public ResponseEntity<ApiResponseBody> deliverOrder(
            @Parameter(description = "Identifiant de la commande à livrer")
            @PathVariable("id") UUID orderId,
            @RequestBody @Valid DeliverOrderRequest deliverOrderRequest
    ) {
        var result = this.deliverOrderUseCase
                .execute(deliverOrderRequest.toCommand(orderId))
                .map(OrderDeliveredResponse::from);

        return ResultToResponse.ok(result, DeliverOrderProblem::from);
    }


    @CustomerOnly
    @PostMapping("{id}/cancel")
    @Operation(
            operationId = "cancelOrder",
            summary = "Annumer une commande",
            description = "Annule une commande qui n'est pas livrée ou acceptée"
    )
    @ApiProblems(CancelOrderErrorCode.class)
    @ApiResponse(
            responseCode = "200",
            description = "Commande annulée avec succès",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = OrderCancelledResponse.class)))
    public ResponseEntity<ApiResponseBody> cancelOrder(
            @Parameter(description = "Identifiant de la commande à annulée")
            @PathVariable("id") UUID orderId
    ) {
        var result = this.cancelOrderUseCase
                .execute(new CancelOrderCommand(orderId))
                .map(OrderCancelledResponse::from);

        return ResultToResponse.ok(result, CancelOrderProblem::from);
    }

    @CustomerOnly
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            operationId = "listOrders",
            summary = "Récupération des commandes",
            description = "Retourne, de façon paginée, les demandes de commandes du client authentifié."
    )
    public ResponseEntity<PageResponse<GetOrderResponse>> listOrders(
            @ParameterObject @PageableDefault(size = 20) Pageable pageable,
            @Parameter(hidden = true) @CurrentCustomerId UUID currentCustomerId
    ) {
        return ResponseEntity.ok(
                PageResponse.from(
                        this.getOrderService.findAll(pageable, currentCustomerId)
                )
        );
    }

    @CustomerOnly
    @GetMapping("{id}")
    @Operation(
            operationId = "getOrder",
            summary = "Récupération d'une commande",
            description = "Retourne une commande du client authentifié."
    )
    @ApiProblems(GetOrderErrorCode.class)
    @ApiResponse(
            responseCode = "200",
            description = "Commande récupérée avec succès",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = GetOrderResponse.class)))
    public ResponseEntity<ApiResponseBody> getById(
            @Parameter(description = "Identifiant de la commande")
            @PathVariable("id") UUID orderId,
            @Parameter(hidden = true) @CurrentCustomerId UUID currentCustomerId
    ) {
        return ResultToResponse.okOrNotFound(
                getOrderService.findById(orderId, currentCustomerId),
                () -> new GetOrderProblem.OrderNotFound(orderId));
    }
}