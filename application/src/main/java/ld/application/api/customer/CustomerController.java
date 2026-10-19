package ld.application.api.customer;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import ld.application.api.docs.ApiBadRequestResponse;
import ld.application.api.docs.ApiInternalServerErrorResponse;
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
@ApiInternalServerErrorResponse
@RequestMapping("/customer")
@Tag(name = "Customer", description = "API pour la gestion des clients")
public class CustomerController {

    private final RegisterCustomerService registerCustomerService;

    public CustomerController(RegisterCustomerService registerCustomerService) {
        this.registerCustomerService = registerCustomerService;
    }

    @CustomerOnly
    @PostMapping(path = "me", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Enregistrer un client"
    )
    @ApiBadRequestResponse
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Client enregistré avec succès",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomerResponse.class))
            )
    })
    public ResponseEntity<CustomerResponse> register(
            @Parameter(hidden = true) @CurrentCustomerId UUID currentCustomerId,
            @RequestBody @Valid RegisterCustomerRequest registerCustomerRequest
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(this.registerCustomerService.register(currentCustomerId, registerCustomerRequest));
    }
}
