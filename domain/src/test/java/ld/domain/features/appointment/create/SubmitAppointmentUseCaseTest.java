package ld.domain.features.appointment.create;

import ld.domain.features.appointment.availability.InMemoryOpeningHoursCalendar;
import ld.domain.features.appointment.model.AppointmentEvent;
import ld.domain.features.appointment.model.AppointmentStatus;
import ld.domain.features.appointment.model.OpeningInterval;
import ld.domain.features.appointment.model.TimeSlot;
import ld.standard.lib.helper.test.InMemoryAggregateEventDispatcher;
import ld.standard.lib.helper.test.InMemoryUnitOfWork;
import ld.standard.lib.validation.FailureType;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.UUID;

import static ld.standard.lib.helper.test.ResultTestSupport.*;

class SubmitAppointmentUseCaseTest {

    static final ZoneId ZONE_ID = ZoneId.of("Europe/Paris");
    static final LocalDate DATE = LocalDate.of(2026, 9, 14);
    static final Clock FIXED_CLOCK =
            Clock.fixed(DATE.atStartOfDay(ZONE_ID).toInstant(), ZONE_ID);

    InMemoryAppointmentCreator appointmentCreator = new InMemoryAppointmentCreator();
    InMemoryScheduleClaimer scheduleClaimer = new InMemoryScheduleClaimer();
    InMemoryOpeningHoursCalendar openingHoursCalendar = new InMemoryOpeningHoursCalendar();
    InMemoryAggregateEventDispatcher<AppointmentEvent> aggregateEventDispatcher =
            new InMemoryAggregateEventDispatcher<>();
    InMemoryUnitOfWork unitOfWork = new InMemoryUnitOfWork();

    SubmitAppointmentUseCaseImpl submitAppointmentUseCase = new SubmitAppointmentUseCaseImpl(
            appointmentCreator,
            scheduleClaimer,
            openingHoursCalendar,
            aggregateEventDispatcher,
            unitOfWork,
            FIXED_CLOCK
    );

    @Nested
    @DisplayName("Quand le créneau demandé est en dehors des horaires d'ouverture")
    public class WhenSlotIsOutsideOpeningHours {

        @BeforeEach
        public void setup() {
            openingHoursCalendar.withOpeningIntervals(DATE,
                    new OpeningInterval(LocalTime.of(9, 0), LocalTime.of(12, 0)));
        }

        @Test
        @DisplayName("Le résultat de la soumission doit être un échec")
        public void shouldReturnFailureResult() {
            var command = SubmitAppointmentCommandTestBuilder.aSubmitAppointmentCommand()
                    .withStart(ZonedDateTime.of(DATE, LocalTime.of(14, 0), ZONE_ID))
                    .withEnd(ZonedDateTime.of(DATE, LocalTime.of(14, 30), ZONE_ID))
                    .build();

            var result = submitAppointmentUseCase.execute(command);
            assertFailure(result, FailureType.BUSINESS_RULE, AppointmentErrorCode.SLOT_OUTSIDE_OPENING_HOURS);
        }

        @Test
        @DisplayName("Aucun rendez-vous n'est persisté, aucun évenement n'est émis")
        public void shouldNotPersistAndDispatchEvent() {
            var command = SubmitAppointmentCommandTestBuilder.aSubmitAppointmentCommand()
                    .withStart(ZonedDateTime.of(DATE, LocalTime.of(14, 0), ZONE_ID))
                    .withEnd(ZonedDateTime.of(DATE, LocalTime.of(14, 30), ZONE_ID))
                    .build();

            assertFailure(submitAppointmentUseCase.execute(command));

            Assertions.assertThat(appointmentCreator.count())
                    .isZero();
            Assertions.assertThat(aggregateEventDispatcher.count())
                    .isZero();
        }

        @Test
        @DisplayName("Aucun accès exclusif n'est demandé (échec avant l'acquisition du claim)")
        public void shouldNotClaimExclusiveAccess() {
            var command = SubmitAppointmentCommandTestBuilder.aSubmitAppointmentCommand()
                    .withStart(ZonedDateTime.of(DATE, LocalTime.of(14, 0), ZONE_ID))
                    .withEnd(ZonedDateTime.of(DATE, LocalTime.of(14, 30), ZONE_ID))
                    .build();

            assertFailure(submitAppointmentUseCase.execute(command));

            Assertions.assertThat(scheduleClaimer.claimCount())
                    .isZero();
        }
    }

    @Nested
    @DisplayName("Quand le créneau demandé est déjà réservé")
    public class WhenSlotIsAlreadyBooked {

        ZonedDateTime bookedStart = ZonedDateTime.of(DATE, LocalTime.of(9, 0), ZONE_ID);
        ZonedDateTime bookedEnd = ZonedDateTime.of(DATE, LocalTime.of(9, 30), ZONE_ID);

        @BeforeEach
        public void setup() {
            openingHoursCalendar.withOpeningIntervals(DATE,
                    new OpeningInterval(LocalTime.of(9, 0), LocalTime.of(12, 0)));
            scheduleClaimer.addBookedSlot(new TimeSlot(bookedStart, bookedEnd));
        }

        @Test
        @DisplayName("Le résultat de la soumission doit être un échec")
        public void shouldReturnFailureResult() {
            var command = SubmitAppointmentCommandTestBuilder.aSubmitAppointmentCommand()
                    .withStart(bookedStart)
                    .withEnd(bookedEnd)
                    .build();

            var result = submitAppointmentUseCase.execute(command);
            assertFailure(result, FailureType.BUSINESS_RULE, AppointmentErrorCode.SLOT_ALREADY_BOOKED);
        }

        @Test
        @DisplayName("Aucun rendez-vous supplémentaire n'est persisté, aucun évenement n'est émis")
        public void shouldNotPersistAndDispatchEvent() {
            var command = SubmitAppointmentCommandTestBuilder.aSubmitAppointmentCommand()
                    .withStart(bookedStart)
                    .withEnd(bookedEnd)
                    .build();

            assertFailure(submitAppointmentUseCase.execute(command));

            Assertions.assertThat(appointmentCreator.count())
                    .isZero();
            Assertions.assertThat(aggregateEventDispatcher.count())
                    .isZero();
        }
    }

    @Nested
    @DisplayName("Quand le rendez-vous peut être soumis")
    public class WhenAppointmentCanBeSubmitted {

        @BeforeEach
        public void setup() {
            openingHoursCalendar.withOpeningIntervals(DATE,
                    new OpeningInterval(LocalTime.of(9, 0), LocalTime.of(12, 0)));
        }

        @Test
        @DisplayName("Le rendez-vous se crée, se persiste et un évenement est émis")
        public void shouldCreateAndPersistAppointmentAndDispatchEvent() {
            ZonedDateTime start = ZonedDateTime.of(DATE, LocalTime.of(9, 0), ZONE_ID);
            ZonedDateTime end = ZonedDateTime.of(DATE, LocalTime.of(9, 30), ZONE_ID);
            UUID identitySubject = UUID.randomUUID();

            var command = SubmitAppointmentCommandTestBuilder.aSubmitAppointmentCommand()
                    .withStart(start)
                    .withEnd(end)
                    .withIdentitySubject(identitySubject)
                    .build();

            var result = submitAppointmentUseCase.execute(command);
            assertSuccess(result);

            Assertions.assertThat(appointmentCreator.count())
                    .isOne();
            Assertions.assertThat(aggregateEventDispatcher.count())
                    .isOne();

            var persistedAppointment = extractValue(result);

            Assertions.assertThat(persistedAppointment.timeSlot().start())
                    .isEqualTo(start);
            Assertions.assertThat(persistedAppointment.timeSlot().end())
                    .isEqualTo(end);
            Assertions.assertThat(persistedAppointment.identitySubject())
                    .isEqualTo(identitySubject);
            Assertions.assertThat(persistedAppointment.appointmentStatus())
                    .isInstanceOf(AppointmentStatus.Submitted.class);
        }

        @Test
        @DisplayName("Un accès exclusif est demandé pour la journée concernée")
        public void shouldClaimExclusiveAccessForTheDay() {
            ZonedDateTime start = ZonedDateTime.of(DATE, LocalTime.of(9, 0), ZONE_ID);
            ZonedDateTime end = ZonedDateTime.of(DATE, LocalTime.of(9, 30), ZONE_ID);

            var command = SubmitAppointmentCommandTestBuilder.aSubmitAppointmentCommand()
                    .withStart(start)
                    .withEnd(end)
                    .build();

            assertSuccess(submitAppointmentUseCase.execute(command));

            Assertions.assertThat(scheduleClaimer.claimCount())
                    .isOne();
        }
    }

    private static final class SubmitAppointmentCommandTestBuilder {

        private ZonedDateTime start = ZonedDateTime.of(DATE, LocalTime.of(9, 0), ZONE_ID);
        private ZonedDateTime end = ZonedDateTime.of(DATE, LocalTime.of(9, 30), ZONE_ID);
        private UUID identitySubject = UUID.randomUUID();

        public static SubmitAppointmentCommandTestBuilder aSubmitAppointmentCommand() {
            return new SubmitAppointmentCommandTestBuilder();
        }

        public SubmitAppointmentCommandTestBuilder withStart(ZonedDateTime start) {
            this.start = start;
            return this;
        }

        public SubmitAppointmentCommandTestBuilder withEnd(ZonedDateTime end) {
            this.end = end;
            return this;
        }

        public SubmitAppointmentCommandTestBuilder withIdentitySubject(UUID identitySubject) {
            this.identitySubject = identitySubject;
            return this;
        }

        public SubmitAppointmentCommand build() {
            return new SubmitAppointmentCommand(start, end, identitySubject, "default note");
        }
    }
}