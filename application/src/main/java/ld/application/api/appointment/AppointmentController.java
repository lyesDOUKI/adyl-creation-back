package ld.application.api.appointment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import ld.application.infra.security.CurrentCustomerId;
import ld.application.infra.security.CustomerOnly;
import ld.application.read.FindAvailableSlotsService;
import ld.application.request.SubmitAppointmentRequest;
import ld.application.response.AvailableSlotsResponse;
import ld.application.response.SubmitAppointmentResponse;
import ld.domain.features.appointment.create.SubmitAppointmentUseCase;
import ld.spring.web.lib.ApiResponseBody;
import ld.spring.web.lib.ResultToResponse;
import ld.standard.lib.validation.Result;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/appointments")
@Tag(name = "Appointment", description = "API pour la gestion des rendez-vous")
public class AppointmentController {

    private final FindAvailableSlotsService findAvailableSlotsService;
    private final SubmitAppointmentUseCase submitAppointmentUseCase;

    public AppointmentController(FindAvailableSlotsService findAvailableSlotsService,
                                 SubmitAppointmentUseCase submitAppointmentUseCase) {
        this.findAvailableSlotsService = findAvailableSlotsService;
        this.submitAppointmentUseCase = submitAppointmentUseCase;
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

    @CustomerOnly
    @Operation(
            summary = "Crée un rendez-vous",
            description = """
                Soumet une demande de rendez-vous sur le créneau donné, pour le client authentifié.
                Le créneau doit être disponible : s'il chevauche un rendez-vous déjà réservé ou tombe
                en dehors des horaires d'ouverture, la création est refusée.
                """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Rendez-vous créé avec succès",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SubmitAppointmentResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Requête invalide (ex: 'end' antérieur ou égal à 'start', champs manquants)",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Client non authentifié",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Le créneau demandé n'est plus disponible (déjà réservé ou hors horaires d'ouverture)",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PostMapping
    public ResponseEntity<ApiResponseBody> create(
            @Parameter(hidden = true) @CurrentCustomerId UUID currentCustomerId,
            @Valid @RequestBody SubmitAppointmentRequest request,
            HttpServletRequest httpServletRequest
    ) {
        Result<SubmitAppointmentResponse> response = this.submitAppointmentUseCase
                .execute(request.to(currentCustomerId))
                .map(SubmitAppointmentResponse::from);

        return ResultToResponse.created(response, SubmitAppointmentResponse::appointmentId, httpServletRequest);
    }
}