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
    private final BusinessGuard<SubmitAppointmentCommand, SubmitAppointmentError> submitAppointmentGuard;
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

    private static BusinessGuard<SubmitAppointmentCommand, SubmitAppointmentError> initGuard(
            OpeningHoursCalendar openingHoursCalendar) {
        return BusinessGuard.of(new SlotWithinOpeningHoursRule(openingHoursCalendar));
    }

    @Override
    public Result<AppointmentSnapshot, SubmitAppointmentError> execute(SubmitAppointmentCommand command) {
        return this.submitAppointmentGuard.validate(command)
                .andThen(_ -> this.unitOfWork.executeInTransaction(() ->
                        createAppointmentUnderExclusiveAccess(command)))
                .map(this::dispatchEventsAndSnapshot);
    }

    private Result<Appointment, SubmitAppointmentError> createAppointmentUnderExclusiveAccess(
            SubmitAppointmentCommand command) {

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
            case SchedulingDecision.Rejected _ ->
                    Result.failure(new SubmitAppointmentError.SlotAlreadyBooked(requestedSlot));

            case SchedulingDecision.Accepted accepted -> {
                Appointment appointment = Appointment.create(
                        accepted.slot(), command.identitySubject(), command.note(), clock);
                this.appointmentCreator.create(appointment.toSnapshot());
                yield Result.success(appointment);
            }
        };
    }

    private AppointmentSnapshot dispatchEventsAndSnapshot(Appointment appointment) {
        appointment.getDomainEvents().forEach(this.aggregateEventDispatcher::dispatch);
        return appointment.toSnapshot();
    }
}