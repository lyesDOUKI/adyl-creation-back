package ld.application.api.customer;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import ld.application.infra.security.CurrentCustomerId;
import ld.application.infra.security.CustomerOnly;
import ld.application.request.RegisterCustomerRequest;
import ld.application.response.CustomerResponse;
import ld.application.write.RegisterCustomerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/customer")
@Tag(name = "Clients", description = "API pour la gestion des clients")
public class CustomerController {

    private final RegisterCustomerService registerCustomerService;

    public CustomerController(RegisterCustomerService registerCustomerService) {
        this.registerCustomerService = registerCustomerService;
    }

    @CustomerOnly
    @PostMapping(path = "me", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            operationId = "registerCustomer",
            summary = "Enregistrer un client",
            description = "Enregistre le client authentifié (identifié par son jeton JWT) avec son e-mail et son téléphone."
    )
    @ApiResponse(
            responseCode = "201",
            description = "Client enregistré avec succès",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = CustomerResponse.class)))
    public ResponseEntity<CustomerResponse> register(
            @Parameter(hidden = true) @CurrentCustomerId UUID currentCustomerId,
            @RequestBody @Valid RegisterCustomerRequest registerCustomerRequest
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(this.registerCustomerService.register(currentCustomerId, registerCustomerRequest));
    }
}