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
import ld.application.api.ApplicationResourceKind;
import ld.application.api.docs.*;
import ld.application.infra.security.AdminOnly;
import ld.application.infra.security.CurrentCustomerId;
import ld.application.infra.security.CustomerOnly;
import ld.application.problems.AcceptOrderProblemMapper;
import ld.application.problems.DeliverOrderProblemMapper;
import ld.application.problems.OrderCreateProblemMapper;
import ld.application.problems.RejectOrderProblemMapper;
import ld.application.read.GetOrderService;
import ld.application.request.CreateOrderRequest;
import ld.application.request.DeliverOrderRequest;
import ld.application.request.RejectOrderRequest;
import ld.application.response.*;
import ld.domain.features.order.CreateOrderUseCase;
import ld.domain.features.order.accept.AcceptOrderCommand;
import ld.domain.features.order.accept.AcceptOrderUseCase;
import ld.domain.features.order.deliver.DeliverOrderUseCase;
import ld.domain.features.order.reject.RejectOrderUseCase;
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
@ApiInternalServerErrorResponse
@RequestMapping("/orders")
@Tag(name = "Commandes", description = "API pour la gestion des commandes")
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final AcceptOrderUseCase acceptOrderUseCase;
    private final RejectOrderUseCase rejectOrderUseCase;
    private final DeliverOrderUseCase deliverOrderUseCase;
    private final GetOrderService getOrderService;
    public OrderController(CreateOrderUseCase createOrderUseCase,
                           AcceptOrderUseCase acceptOrderUseCase,
                           RejectOrderUseCase rejectOrderUseCase,
                           DeliverOrderUseCase deliverOrderUseCase,
                           GetOrderService getOrderService) {
        this.createOrderUseCase = createOrderUseCase;
        this.acceptOrderUseCase = acceptOrderUseCase;
        this.rejectOrderUseCase = rejectOrderUseCase;
        this.deliverOrderUseCase = deliverOrderUseCase;
        this.getOrderService = getOrderService;
    }

    @CustomerOnly
    @PostMapping
    @Operation(
            summary = "Création d'une demande de commande",
            description = "Envoi la demande de commande pour prise en compte et traitement"
    )
    @ApiBadRequestResponse
    @ApiResourceNotFoundResponse
    @ApiProcessProblemResponse
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Demande de commande enregistré avec succès",
                    content = @Content(schema = @Schema(implementation = CreateOrderResponse.class))
            )
    })
    public ResponseEntity<ApiResponseBody> createOrder(
            @Parameter(hidden = true) @CurrentCustomerId UUID customerId,
            @RequestBody @Valid CreateOrderRequest request,
            HttpServletRequest httpServletRequest
    ) {
        var response = this.createOrderUseCase.execute(request.toCommand(customerId))
                .map(CreateOrderResponse::from);
        return ResultToResponse.created(response, CreateOrderResponse::orderId, new OrderCreateProblemMapper(), httpServletRequest);
    }

    @AdminOnly
    @PostMapping("{id}/accept")
    @Operation(
            summary = "Accepter une commande",
            description = "Accepte une commande en attente et applique, le cas échéant, la remise de première commande."
    )
    @ApiBadRequestResponse
    @ApiResourceNotFoundResponse
    @ApiConflictResponse
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Commande acceptée avec succès",
                    content = @Content(
                            schema = @Schema(implementation = OrderAcceptedResponse.class)
                    )
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

        return ResultToResponse.ok(
                response,
                new AcceptOrderProblemMapper(),
                httpServletRequest
        );
    }

    @AdminOnly
    @PostMapping("{id}/reject")
    @Operation(
            summary = "Rejeter une commande"
    )
    @ApiBadRequestResponse
    @ApiResourceNotFoundResponse
    @ApiProcessProblemResponse
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Commande rejetée avec succès",
                    content = @Content(
                            schema = @Schema(implementation = OrderRejectedResponse.class)
                    )
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

        return ResultToResponse.ok(
                response,
                new RejectOrderProblemMapper(),
                httpServletRequest
        );
    }

    @AdminOnly
    @PostMapping("{id}/deliver")
    @Operation(
            summary = "Livrer une commande"
    )
    @ApiBadRequestResponse
    @ApiResourceNotFoundResponse
    @ApiProcessProblemResponse
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Commande livrée avec succès",
                    content = @Content(
                            schema = @Schema(implementation = OrderDeliveredResponse.class)
                    )
            )
    })
    public ResponseEntity<ApiResponseBody> deliverOrder(
            @Parameter(
                    description = "Identifiant de la commande à livrer",
                    required = true
            )
            @PathVariable("id") UUID orderId,
            @RequestBody @Valid DeliverOrderRequest deliverOrderRequest,
            HttpServletRequest httpServletRequest
    ) {
        var result = this.deliverOrderUseCase.execute(deliverOrderRequest.toCommand(orderId))
                .map(OrderDeliveredResponse::from);
        return ResultToResponse.ok(result, new DeliverOrderProblemMapper(), httpServletRequest);
    }

    @CustomerOnly
    @GetMapping
    @Operation(
            summary = "Récupération des commandes"
    )
    @ApiBadRequestResponse
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Commandes récupérées avec succès"
            )
    })
    public ResponseEntity<PageResponse<GetOrderResponse>> get(
            @ParameterObject @PageableDefault(size = 20) Pageable pageable,
            @Parameter(hidden = true) @CurrentCustomerId UUID currentCustomerId
    ) {
        return ResponseEntity.ok(PageResponse.from(this.getOrderService.findAll(pageable, currentCustomerId)));
    }

    @CustomerOnly
    @GetMapping("{id}")
    @Operation(
            summary = "Récupération d'une commande"
    )
    @ApiBadRequestResponse
    @ApiResourceNotFoundResponse
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Commande récupérée avec succès",
                    content = @Content(schema = @Schema(implementation = GetOrderResponse.class))
            )
    })
    public ResponseEntity<ApiResponseBody> getById(
            @PathVariable("id") UUID orderId,
            @Parameter(hidden = true) @CurrentCustomerId UUID currentCustomerId,
            HttpServletRequest httpServletRequest) {

        return ResultToResponse.okOrNotFound(
                getOrderService.findById(orderId, currentCustomerId),
                ApplicationResourceKind.ORDER,
                orderId,
                httpServletRequest);
    }
}
