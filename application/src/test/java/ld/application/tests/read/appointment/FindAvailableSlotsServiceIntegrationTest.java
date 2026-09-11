package ld.application.tests.read.appointment;

import ld.application.config.common.SharedPostgresContainer;
import ld.application.context.AppointmentIntegrationTest;
import ld.domain.features.appointment.availability.FindAvailableSlotsQuery;
import ld.domain.features.appointment.availability.FindAvailableSlotsUseCase;
import ld.domain.features.appointment.create.AppointmentCreator;
import ld.domain.features.appointment.model.AppointmentSnapshot;
import ld.domain.features.appointment.model.AppointmentStatus;
import ld.domain.features.appointment.model.TimeSlot;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.*;
import java.util.List;
import java.util.UUID;

import static ld.application.jooq.tables.Appointment.APPOINTMENT;
import static org.assertj.core.api.Assertions.assertThat;

@AppointmentIntegrationTest
class FindAvailableSlotsServiceIntegrationTest {

    private static final ZoneId ZONE_ID = ZoneId.of("Europe/Paris");
    private static final LocalDate MONDAY = LocalDate.of(2026, 9, 14);
    private static final LocalDate TUESDAY = LocalDate.of(2026, 9, 15);
    private static final LocalDate SATURDAY = LocalDate.of(2026, 9, 19);

    private static final int SLOTS_PER_DAY = 14;

    @ServiceConnection
    static PostgreSQLContainer<?> POSTGRES = SharedPostgresContainer.INSTANCE;

    @Autowired
    private FindAvailableSlotsUseCase findAvailableSlotsUseCase;

    @Autowired
    private DSLContext dsl;

    @Autowired
    private AppointmentCreator appointmentCreator;

    @Autowired
    Clock clock;
    @BeforeEach
    void setUp() {
        dsl.deleteFrom(APPOINTMENT).execute();
    }

    @Test
    void should_return_all_opening_slots_when_no_appointment_is_booked() {
        var slots = findAvailableSlots(MONDAY, MONDAY);

        assertThat(slots).hasSize(SLOTS_PER_DAY);
        assertThat(slots).contains(
                slot(MONDAY, 9, 0, 9, 30),
                slot(MONDAY, 11, 30, 12, 0),
                slot(MONDAY, 14, 0, 14, 30),
                slot(MONDAY, 17, 30, 18, 0));
    }

    @Test
    void should_exclude_booked_slot() {
        insertAppointment(MONDAY, 9, 0, 9, 30);

        var slots = findAvailableSlots(MONDAY, MONDAY);

        assertThat(slots).doesNotContain(slot(MONDAY, 9, 0, 9, 30));
        assertThat(slots).contains(slot(MONDAY, 9, 30, 10, 0));
    }

    @Test
    void should_exclude_multiple_booked_slots() {
        insertAppointment(MONDAY, 9, 0, 9, 30);
        insertAppointment(MONDAY, 10, 0, 10, 30);
        insertAppointment(MONDAY, 15, 0, 15, 30);

        var slots = findAvailableSlots(MONDAY, MONDAY);

        assertThat(slots).hasSize(SLOTS_PER_DAY - 3);
        assertThat(slots).doesNotContain(
                slot(MONDAY, 9, 0, 9, 30),
                slot(MONDAY, 10, 0, 10, 30),
                slot(MONDAY, 15, 0, 15, 30));
        assertThat(slots).contains(
                slot(MONDAY, 9, 30, 10, 0),
                slot(MONDAY, 10, 30, 11, 0),
                slot(MONDAY, 15, 30, 16, 0));
    }

    @Test
    void should_return_available_slots_across_multiple_days() {
        insertAppointment(TUESDAY, 9, 0, 9, 30);

        var slots = findAvailableSlots(MONDAY, TUESDAY);

        assertThat(slots).hasSize(2 * SLOTS_PER_DAY - 1);
        assertThat(slots).doesNotContain(slot(TUESDAY, 9, 0, 9, 30));
        assertThat(slots).contains(
                slot(MONDAY, 9, 0, 9, 30),
                slot(TUESDAY, 9, 30, 10, 0));
    }

    @Test
    void should_return_no_slots_on_weekend() {
        var slots = findAvailableSlots(SATURDAY, SATURDAY);

        assertThat(slots).isEmpty();
    }

    @Test
    void should_ignore_cancelled_appointment() {
        insertAppointment(MONDAY, 9, 0, 9, 30,
                new AppointmentStatus.Cancelled(Instant.now(clock), "no time"));

        var slots = findAvailableSlots(MONDAY, MONDAY);

        assertThat(slots).contains(slot(MONDAY, 9, 0, 9, 30));
    }

    @Test
    void should_ignore_appointment_outside_requested_period() {
        insertAppointment(TUESDAY, 9, 0, 9, 30);

        var slots = findAvailableSlots(MONDAY, MONDAY);

        assertThat(slots).hasSize(SLOTS_PER_DAY);
        assertThat(slots).contains(slot(MONDAY, 9, 0, 9, 30));
    }

    private List<TimeSlot> findAvailableSlots(LocalDate from, LocalDate to) {
        return findAvailableSlotsUseCase.findAvailableSlots(new FindAvailableSlotsQuery(from, to));
    }


    private void insertAppointment(LocalDate date, int startHour, int startMinute, int endHour, int endMinute) {
        var start = at(date, startHour, startMinute);
        var end = at(date, endHour, endMinute);
        var snapshot = new AppointmentSnapshot(
                UUID.randomUUID(),
                start,
                end,
                UUID.randomUUID(),
                new AppointmentStatus.Submitted(start.toInstant()));
        appointmentCreator.create(snapshot);
    }
    private void insertAppointment(LocalDate date, int startHour, int startMinute, int endHour, int endMinute, AppointmentStatus appointmentStatus) {
        var start = at(date, startHour, startMinute);
        var end = at(date, endHour, endMinute);
        var snapshot = new AppointmentSnapshot(
                UUID.randomUUID(),
                start,
                end,
                UUID.randomUUID(),
                appointmentStatus);
        appointmentCreator.create(snapshot);
    }

    private static ZonedDateTime at(LocalDate date, int hour, int minute) {
        return ZonedDateTime.of(date, LocalTime.of(hour, minute), ZONE_ID);
    }

    private static TimeSlot slot(LocalDate date, int startHour, int startMinute, int endHour, int endMinute) {
        return new TimeSlot(at(date, startHour, startMinute), at(date, endHour, endMinute));
    }
}