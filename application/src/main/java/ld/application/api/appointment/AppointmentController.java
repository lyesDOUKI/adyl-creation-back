package ld.application.api.appointment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import ld.application.infra.security.CustomerOnly;
import ld.application.read.FindAvailableSlotsService;
import ld.application.response.AvailableSlotsResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/appointments")
@Tag(name = "Appointment", description = "API pour la gestion des rendez-vous")
public class AppointmentController {

    private final FindAvailableSlotsService findAvailableSlotsService;

    public AppointmentController(FindAvailableSlotsService findAvailableSlotsService) {
        this.findAvailableSlotsService = findAvailableSlotsService;
    }

    @CustomerOnly
    @Operation(
            summary = "Recherche les créneaux disponibles",
            description = """
                    Retourne les créneaux disponibles à la réservation sur la période demandée,
                    organisés par date. Chaque date de la période est présente dans la réponse,
                    même celles ne comportant aucun créneau disponible.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Liste des créneaux disponibles par date",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AvailableSlotsResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Paramètres invalides (ex: 'to' antérieur à 'from', dates manquantes ou mal formées)",
                    content = @Content(mediaType = "application/json")
            )
    })
    @GetMapping("/available-slots")
    public ResponseEntity<AvailableSlotsResponse> findAvailableSlots(
            @Parameter(
                    description = "Date de début de la période recherchée (incluse)",
                    example = "2026-09-15",
                    required = true
            )
            @RequestParam("from") LocalDate from,

            @Parameter(
                    description = "Date de fin de la période recherchée (incluse)",
                    example = "2026-09-20",
                    required = true
            )
            @RequestParam("to") LocalDate to
    ) {
        return ResponseEntity.ok(findAvailableSlotsService.findAvailableSlots(from, to));
    }
}