package ld.application.api.customer;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import ld.application.infra.security.CurrentCustomerId;
import ld.application.infra.security.CustomerOnly;
import ld.application.request.RegisterCustomerRequest;
import ld.application.response.CustomerResponse;
import ld.application.write.RegisterCustomerService;
import ld.spring.web.lib.ApiResponseBody;
import ld.spring.web.lib.ResultToResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/customer")
@Tag(name = "Customer", description = "API pour la gestion des clients")
public class CustomerController {

    private final RegisterCustomerService registerCustomerService;

    public CustomerController(RegisterCustomerService registerCustomerService) {
        this.registerCustomerService = registerCustomerService;
    }

    @CustomerOnly
    @PostMapping("me")
    @Operation(
            summary = "Enregistrer un client"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Client enregistré avec succès"
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
    public ResponseEntity<ApiResponseBody> register(
            @Parameter(hidden = true) @CurrentCustomerId UUID currentCustomerId,
            @RequestBody @Valid RegisterCustomerRequest registerCustomerRequest,
            HttpServletRequest httpServletRequest
    ) {
        var result = this.registerCustomerService.register(currentCustomerId, registerCustomerRequest);
        return ResultToResponse.created(result, CustomerResponse::id, httpServletRequest);
    }
}
