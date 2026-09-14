package ld.application.tests.write.appointment;

import ld.application.config.appointment.SwitchableAppointmentCreator;
import ld.application.config.common.SharedPostgresContainer;
import ld.application.context.AppointmentIntegrationTest;
import ld.domain.features.appointment.create.SubmitAppointmentCommand;
import ld.domain.features.appointment.create.SubmitAppointmentUseCase;
import ld.domain.features.appointment.model.AppointmentSnapshot;
import ld.domain.features.appointment.model.AppointmentStatus;
import ld.standard.lib.helper.test.ResultTestSupport;
import ld.standard.lib.validation.Result;
import org.jooq.DSLContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.stream.Stream;

import static ld.application.jooq.tables.Appointment.APPOINTMENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@AppointmentIntegrationTest
class SubmitAppointmentServiceIntegrationTest {

    @ServiceConnection
    static PostgreSQLContainer<?> POSTGRES = SharedPostgresContainer.INSTANCE;

    static final ZoneId ZONE_ID = ZoneId.of("Europe/Paris");
    static final LocalDate DATE = LocalDate.of(2026, 9, 14);

    @Autowired
    private SubmitAppointmentUseCase submitAppointmentUseCase;

    @Autowired
    private DSLContext dsl;

    @BeforeEach
    void setUp() {
        dsl.deleteFrom(APPOINTMENT).execute();
    }

    @Test
    void should_persist_appointment_and_dispatch_event_when_submission_succeeds() {

        var identitySubject = UUID.randomUUID();

        var command = SubmitAppointmentCommandTestBuilder.aSubmitAppointmentCommand()
                .withStart(ZonedDateTime.of(
                        DATE,
                        LocalTime.of(9, 0),
                        ZONE_ID
                ))
                .withEnd(ZonedDateTime.of(
                        DATE,
                        LocalTime.of(9, 30),
                        ZONE_ID
                ))
                .withIdentitySubject(identitySubject)
                .build();

        Result<AppointmentSnapshot> result =
                submitAppointmentUseCase.execute(command);

        assertThat(result.isSuccess()).isTrue();

        var snapshot = ResultTestSupport.extractValue(result);

        assertThat(snapshot.timeSlot().start())
                .isEqualTo(command.start());

        assertThat(snapshot.timeSlot().end())
                .isEqualTo(command.end());

        assertThat(snapshot.identitySubject())
                .isEqualTo(identitySubject);

        assertThat(snapshot.appointmentStatus())
                .isInstanceOf(AppointmentStatus.Submitted.class);

        assertThat(
                dsl.fetchCount(
                        APPOINTMENT,
                        APPOINTMENT.ID.eq(snapshot.appointmentId())
                )
        ).isEqualTo(1);
    }

    @Test
    void should_not_persist_nor_dispatch_when_slot_is_outside_opening_hours() {

        var command = SubmitAppointmentCommandTestBuilder.aSubmitAppointmentCommand()
                .withStart(ZonedDateTime.of(
                        DATE,
                        LocalTime.of(18, 0),
                        ZONE_ID
                ))
                .withEnd(ZonedDateTime.of(
                        DATE,
                        LocalTime.of(18, 30),
                        ZONE_ID
                ))
                .build();

        Result<AppointmentSnapshot> result =
                submitAppointmentUseCase.execute(command);

        assertThat(result.isFailure()).isTrue();

        assertThat(dsl.fetchCount(APPOINTMENT))
                .isZero();
    }

    @Test
    void should_not_persist_nor_dispatch_when_slot_is_already_booked() {

        var firstCommand = SubmitAppointmentCommandTestBuilder.aSubmitAppointmentCommand()
                .withStart(ZonedDateTime.of(
                        DATE,
                        LocalTime.of(9, 0),
                        ZONE_ID
                ))
                .withEnd(ZonedDateTime.of(
                        DATE,
                        LocalTime.of(9, 30),
                        ZONE_ID
                ))
                .build();

        Result<AppointmentSnapshot> firstResult =
                submitAppointmentUseCase.execute(firstCommand);

        assertThat(firstResult.isSuccess()).isTrue();

        var secondCommand = SubmitAppointmentCommandTestBuilder.aSubmitAppointmentCommand()
                .withStart(firstCommand.start())
                .withEnd(firstCommand.end())
                .build();

        Result<AppointmentSnapshot> secondResult =
                submitAppointmentUseCase.execute(secondCommand);

        assertThat(secondResult.isFailure()).isTrue();

        assertThat(dsl.fetchCount(APPOINTMENT))
                .isEqualTo(1);
    }

    @Nested
    class RollbackScenario {

        @Autowired
        private SwitchableAppointmentCreator appointmentCreator;

        @AfterEach
        void reset() {
            appointmentCreator.reset();
        }
        @Test
        void should_rollback_appointment_creation_when_persistence_fails_after_insert() {
            appointmentCreator.failAfterCreateWith(
                    new RuntimeException("Simulated failure after insert"));
            var command = SubmitAppointmentCommandTestBuilder.aSubmitAppointmentCommand()
                    .withStart(ZonedDateTime.of(
                            DATE,
                            LocalTime.of(9, 0),
                            ZONE_ID
                    ))
                    .withEnd(ZonedDateTime.of(
                            DATE,
                            LocalTime.of(9, 30),
                            ZONE_ID
                    ))
                    .build();

            assertThatThrownBy(() ->
                    submitAppointmentUseCase.execute(command)
            )
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Simulated failure after insert");

            assertThat(dsl.fetchCount(APPOINTMENT))
                    .isZero();
        }
    }

    private static final class SubmitAppointmentCommandTestBuilder {

        private ZonedDateTime start =
                ZonedDateTime.of(
                        DATE,
                        LocalTime.of(9, 0),
                        ZONE_ID
                );

        private ZonedDateTime end =
                ZonedDateTime.of(
                        DATE,
                        LocalTime.of(9, 30),
                        ZONE_ID
                );

        private UUID identitySubject = UUID.randomUUID();

        static SubmitAppointmentCommandTestBuilder aSubmitAppointmentCommand() {
            return new SubmitAppointmentCommandTestBuilder();
        }

        SubmitAppointmentCommandTestBuilder withStart(
                ZonedDateTime start
        ) {
            this.start = start;
            return this;
        }

        SubmitAppointmentCommandTestBuilder withEnd(
                ZonedDateTime end
        ) {
            this.end = end;
            return this;
        }

        SubmitAppointmentCommandTestBuilder withIdentitySubject(
                UUID identitySubject
        ) {
            this.identitySubject = identitySubject;
            return this;
        }

        SubmitAppointmentCommand build() {
            return new SubmitAppointmentCommand(
                    start,
                    end,
                    identitySubject,
                    "default notes"
            );
        }
    }

    @Nested
    class ConcurrentSubmissionScenario {

        @Test
        void should_reject_exactly_one_of_two_concurrent_submissions_on_the_same_slot() throws Exception {

            var slotStart = ZonedDateTime.of(DATE, LocalTime.of(9, 0), ZONE_ID);
            var slotEnd = ZonedDateTime.of(DATE, LocalTime.of(9, 30), ZONE_ID);

            var firstCommand = SubmitAppointmentCommandTestBuilder.aSubmitAppointmentCommand()
                    .withStart(slotStart)
                    .withEnd(slotEnd)
                    .build();

            var secondCommand = SubmitAppointmentCommandTestBuilder.aSubmitAppointmentCommand()
                    .withStart(slotStart)
                    .withEnd(slotEnd)
                    .build();

            var readyToStart = new CyclicBarrier(2);
            var pool = Executors.newFixedThreadPool(2);
            try {
                Callable<Result<AppointmentSnapshot>> submitFirst = () -> {
                    readyToStart.await();
                    return submitAppointmentUseCase.execute(firstCommand);
                };
                Callable<Result<AppointmentSnapshot>> submitSecond = () -> {
                    readyToStart.await();
                    return submitAppointmentUseCase.execute(secondCommand);
                };

                Future<Result<AppointmentSnapshot>> firstFuture = pool.submit(submitFirst);
                Future<Result<AppointmentSnapshot>> secondFuture = pool.submit(submitSecond);

                Result<AppointmentSnapshot> firstResult = firstFuture.get(10, TimeUnit.SECONDS);
                Result<AppointmentSnapshot> secondResult = secondFuture.get(10, TimeUnit.SECONDS);

                long successCount = Stream.of(firstResult, secondResult)
                        .filter(Result::isSuccess)
                        .count();
                long failureCount = Stream.of(firstResult, secondResult)
                        .filter(Result::isFailure)
                        .count();

                assertThat(successCount)
                        .as("exactement une des deux soumissions concurrentes doit réussir")
                        .isEqualTo(1);
                assertThat(failureCount)
                        .as("exactement une des deux soumissions concurrentes doit échouer")
                        .isEqualTo(1);

                assertThat(dsl.fetchCount(APPOINTMENT))
                        .as("un seul appointment doit être persisté pour ce créneau, jamais deux")
                        .isEqualTo(1);
            } finally {
                pool.shutdownNow();
            }
        }
    }
}
