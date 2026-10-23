package ld.application.api.appointment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import ld.application.infra.security.CurrentCustomerId;
import ld.application.infra.security.CustomerOnly;
import ld.application.problems.appointment.SubmitAppointmentErrorCode;
import ld.application.problems.appointment.SubmitAppointmentProblem;
import ld.application.read.FindAvailableSlotsService;
import ld.application.read.GetAppointmentService;
import ld.application.request.SubmitAppointmentRequest;
import ld.application.response.AppointmentsResponse;
import ld.application.response.AvailableSlotsResponse;
import ld.application.response.SubmitAppointmentResponse;
import ld.domain.features.appointment.create.SubmitAppointmentUseCase;
import ld.spring.web.lib.ApiProblems;
import ld.spring.web.lib.ApiResponseBody;
import ld.spring.web.lib.ResultToResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/appointments")
@Tag(name = "Rendez-vous", description = "API pour la gestion des rendez-vous")
public class AppointmentController {

    private final FindAvailableSlotsService findAvailableSlotsService;
    private final SubmitAppointmentUseCase submitAppointmentUseCase;
    private final GetAppointmentService getAppointmentService;

    public AppointmentController(FindAvailableSlotsService findAvailableSlotsService,
                                 SubmitAppointmentUseCase submitAppointmentUseCase,
                                 GetAppointmentService getAppointmentService) {
        this.findAvailableSlotsService = findAvailableSlotsService;
        this.submitAppointmentUseCase = submitAppointmentUseCase;
        this.getAppointmentService = getAppointmentService;
    }

    @CustomerOnly
    @GetMapping("/available-slots")
    @Operation(
            operationId = "findAvailableSlots",
            summary = "Recherche les créneaux disponibles",
            description = """
                    Retourne les créneaux disponibles à la réservation sur la période demandée, \
                    organisés par date. Chaque date de la période est présente dans la réponse, \
                    même celles ne comportant aucun créneau disponible."""
    )
    @ApiResponse(
            responseCode = "200",
            description = "Liste des créneaux disponibles par date",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = AvailableSlotsResponse.class)))
    public ResponseEntity<AvailableSlotsResponse> findAvailableSlots(
            @Parameter(description = "Date de début de la période recherchée (incluse)", example = "2026-09-15")
            @RequestParam("from") LocalDate from,

            @Parameter(description = "Date de fin de la période recherchée (incluse)", example = "2026-09-20")
            @RequestParam("to") LocalDate to
    ) {
        return ResponseEntity.ok(findAvailableSlotsService.findAvailableSlots(from, to));
    }

    @CustomerOnly
    @PostMapping
    @Operation(
            operationId = "createAppointment",
            summary = "Crée un rendez-vous",
            description = """
                    Soumet une demande de rendez-vous sur le créneau donné, pour le client authentifié. \
                    Le créneau doit être disponible : s'il chevauche un rendez-vous déjà réservé ou tombe \
                    en dehors des horaires d'ouverture, la création est refusée."""
    )
    @ApiProblems(SubmitAppointmentErrorCode.class)
    @ApiResponse(
            responseCode = "201",
            description = "Rendez-vous créé avec succès",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = SubmitAppointmentResponse.class)))
    public ResponseEntity<ApiResponseBody> create(
            @Parameter(hidden = true) @CurrentCustomerId UUID currentCustomerId,
            @Valid @RequestBody SubmitAppointmentRequest request,
            HttpServletRequest httpServletRequest) {

        var result = submitAppointmentUseCase
                .execute(request.to(currentCustomerId))
                .map(SubmitAppointmentResponse::from);

        return ResultToResponse.created(
                result,
                SubmitAppointmentResponse::appointmentId,
                SubmitAppointmentProblem::from,
                httpServletRequest);
    }

    @CustomerOnly
    @GetMapping
    @Operation(
            operationId = "listAppointments",
            summary = "Lister mes rendez-vous",
            description = """
                    Retourne l'ensemble des rendez-vous du client authentifié, identifié à partir du \
                    token JWT. Aucun paramètre n'est requis : le périmètre est automatiquement \
                    restreint au client courant."""
    )
    @ApiResponse(
            responseCode = "200",
            description = "Liste des rendez-vous du client (peut être vide)",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array = @ArraySchema(schema = @Schema(implementation = AppointmentsResponse.class))))
    public ResponseEntity<List<AppointmentsResponse>> listAppointments(
            @Parameter(hidden = true) @CurrentCustomerId UUID currentCustomerId) {
        return ResponseEntity.ok(this.getAppointmentService.getAll(currentCustomerId));
    }
}