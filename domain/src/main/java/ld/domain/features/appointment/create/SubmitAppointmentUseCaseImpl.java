package ld.domain.features.appointment.create;

import ld.domain.features.appointment.availability.OpeningHoursCalendar;
import ld.domain.features.appointment.model.*;
import ld.standard.lib.AggregateEventDispatcher;
import ld.standard.lib.UnitOfWork;
import ld.standard.lib.validation.BusinessGuard;
import ld.standard.lib.validation.Result;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

public class SubmitAppointmentUseCaseImpl implements SubmitAppointmentUseCase {

    private final AppointmentCreator appointmentCreator;
    private final ScheduleClaimer scheduleClaimer;
    private final OpeningHoursCalendar openingHoursCalendar;
    private final AggregateEventDispatcher<AppointmentEvent> aggregateEventDispatcher;
    private final BusinessGuard<SubmitAppointmentCommand> submitAppointmentGuard;
    private final UnitOfWork unitOfWork;
    private final Clock clock;

    public SubmitAppointmentUseCaseImpl(AppointmentCreator appointmentCreator,
                                        ScheduleClaimer scheduleClaimer,
                                        OpeningHoursCalendar openingHoursCalendar,
                                        AggregateEventDispatcher<AppointmentEvent> aggregateEventDispatcher,
                                        UnitOfWork unitOfWork,
                                        Clock clock) {
        this.appointmentCreator = appointmentCreator;
        this.scheduleClaimer = scheduleClaimer;
        this.openingHoursCalendar = openingHoursCalendar;
        this.aggregateEventDispatcher = aggregateEventDispatcher;
        this.submitAppointmentGuard = initGuard(openingHoursCalendar);
        this.unitOfWork = unitOfWork;
        this.clock = clock;
    }

    private static BusinessGuard<SubmitAppointmentCommand> initGuard(OpeningHoursCalendar openingHoursCalendar) {
        return BusinessGuard.of(new SlotWithinOpeningHoursRule(openingHoursCalendar));
    }

    @Override
    public Result<AppointmentSnapshot> execute(SubmitAppointmentCommand submitAppointmentCommand) {
        return this.submitAppointmentGuard.validate(submitAppointmentCommand)
                .flatMap(_ -> this.unitOfWork.executeInTransaction(() ->
                        createAppointmentUnderExclusiveAccess(submitAppointmentCommand)))
                .map(appointment -> {
                    appointment.getDomainEvents().forEach(this.aggregateEventDispatcher::dispatch);
                    return appointment.toSnapshot();
                });
    }

    private Result<Appointment> createAppointmentUnderExclusiveAccess(SubmitAppointmentCommand command) {
        ZoneId businessZone = this.openingHoursCalendar.zoneId();
        TimeSlot requestedSlot = new TimeSlot(
                command.start().withZoneSameInstant(businessZone),
                command.end().withZoneSameInstant(businessZone)
        );
        LocalDate date = requestedSlot.start().toLocalDate();

        ScheduleClaim claim = this.scheduleClaimer.claimExclusiveAccess(date, businessZone);

        List<TimeSlot> bookedSlots = this.scheduleClaimer.bookedSlotsFor(claim);

        DailySchedule dailySchedule = new DailySchedule(claim, bookedSlots);

        return switch (dailySchedule.attemptToBook(requestedSlot)) {
            case SchedulingDecision.Rejected _ -> Result.businessFailure(
                    AppointmentErrorCode.SLOT_ALREADY_BOOKED,
                    "Créneau déjà réservé",
                    "Ce créneau est déjà réservé"
            );
            case SchedulingDecision.Accepted accepted -> {
                Appointment appointment = Appointment.create(
                        accepted.slot(),
                        command.identitySubject(), command.note(),
                        clock
                );
                this.appointmentCreator.create(appointment.toSnapshot());
                yield Result.success(appointment);
            }
        };
    }
}