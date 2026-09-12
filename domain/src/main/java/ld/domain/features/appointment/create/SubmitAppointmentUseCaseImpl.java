package ld.domain.features.appointment.create;

import ld.domain.features.appointment.availability.LoadBookedAppointmentsPort;
import ld.domain.features.appointment.availability.OpeningHoursCalendar;
import ld.domain.features.appointment.model.Appointment;
import ld.domain.features.appointment.model.AppointmentEvent;
import ld.domain.features.appointment.model.AppointmentSnapshot;
import ld.standard.lib.AggregateEventDispatcher;
import ld.standard.lib.UnitOfWork;
import ld.standard.lib.validation.BusinessGuard;
import ld.standard.lib.validation.Result;

import java.time.Clock;

public class SubmitAppointmentUseCaseImpl implements SubmitAppointmentUseCase {

    private final AppointmentCreator appointmentCreator;
    private final AggregateEventDispatcher<AppointmentEvent> aggregateEventDispatcher;
    private final BusinessGuard<SubmitAppointmentCommand> submitAppointmentGuard;
    private final UnitOfWork unitOfWork;
    private final Clock clock;

    public SubmitAppointmentUseCaseImpl(AppointmentCreator appointmentCreator,
                                        OpeningHoursCalendar openingHoursCalendar,
                                        LoadBookedAppointmentsPort loadBookedAppointmentsPort,
                                        AggregateEventDispatcher<AppointmentEvent> aggregateEventDispatcher,
                                        UnitOfWork unitOfWork,
                                        Clock clock) {
        this.appointmentCreator = appointmentCreator;
        this.aggregateEventDispatcher = aggregateEventDispatcher;
        this.submitAppointmentGuard = initGuard(openingHoursCalendar, loadBookedAppointmentsPort);
        this.unitOfWork = unitOfWork;
        this.clock = clock;
    }

    private static BusinessGuard<SubmitAppointmentCommand> initGuard(OpeningHoursCalendar openingHoursCalendar,
                                                                     LoadBookedAppointmentsPort loadBookedAppointmentsPort) {
        return BusinessGuard.of(new AppointmentSlotAvailabilityRule(openingHoursCalendar, loadBookedAppointmentsPort));
    }

    @Override
    public Result<AppointmentSnapshot> execute(SubmitAppointmentCommand submitAppointmentCommand) {
        return this.submitAppointmentGuard.validate(submitAppointmentCommand)
                .flatMap(_ -> this.unitOfWork.executeInTransaction(() -> {
                    Appointment appointment = Appointment.create(
                            submitAppointmentCommand.start(),
                            submitAppointmentCommand.end(),
                            submitAppointmentCommand.identitySubject(),
                            submitAppointmentCommand.note(),
                            clock
                    );
                    this.appointmentCreator.create(appointment.toSnapshot());
                    return Result.success(appointment);
                }))
                .map(appointment -> {
                    appointment.getDomainEvents().forEach(this.aggregateEventDispatcher::dispatch);
                    return appointment.toSnapshot();
                });
    }
}
