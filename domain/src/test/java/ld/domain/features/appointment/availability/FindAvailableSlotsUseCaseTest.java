package ld.domain.features.appointment.availability;


import ld.domain.features.appointment.model.OpeningInterval;
import ld.domain.features.appointment.model.TimeSlot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FindAvailableSlotsUseCaseTest {

    private static final ZoneId ZONE = ZoneId.of("Europe/Paris");
    private static final LocalDate DAY = LocalDate.of(2026, 9, 14);
    private static final OpeningInterval NINE_TO_TEN = new OpeningInterval(LocalTime.of(9, 0), LocalTime.of(10, 0));

    private final InMemoryLoadBookedAppointments loadBookedAppointmentsPort =
            new InMemoryLoadBookedAppointments();

    private FindAvailableSlotsUseCaseImpl useCaseWith(InMemoryOpeningHoursCalendar calendar) {
        return new FindAvailableSlotsUseCaseImpl(calendar, loadBookedAppointmentsPort);
    }

    private TimeSlot slotOn(LocalDate date, int startHour, int startMinute, int endHour, int endMinute) {
        return new TimeSlot(
                ZonedDateTime.of(date, LocalTime.of(startHour, startMinute), ZONE),
                ZonedDateTime.of(date, LocalTime.of(endHour, endMinute), ZONE)
        );
    }

    @Nested
    @DisplayName("Quand aucun rendez-vous n'est déjà réservé")
    class WhenNoAppointmentIsBooked {

        @Test
        @DisplayName("tous les créneaux théoriques sont retournés")
        void shouldReturnAllTheoreticalSlots() {
            var calendar = new InMemoryOpeningHoursCalendar()
                    .withOpeningIntervals(DAY, NINE_TO_TEN);
            var query = new FindAvailableSlotsQuery(DAY, DAY);

            var result = useCaseWith(calendar).findAvailableSlots(query);

            assertThat(result).containsExactlyInAnyOrder(
                    slotOn(DAY, 9, 0, 9, 30),
                    slotOn(DAY, 9, 30, 10, 0)
            );
        }
    }

    @Nested
    @DisplayName("Quand un rendez-vous chevauche un créneau théorique")
    class WhenAnAppointmentOverlapsATheoreticalSlot {

        @Test
        @DisplayName("seul le créneau chevauché est exclu, les autres restent disponibles")
        void shouldExcludeOnlyTheOverlappingSlot() {
            var calendar = new InMemoryOpeningHoursCalendar()
                    .withOpeningIntervals(DAY, NINE_TO_TEN);
            loadBookedAppointmentsPort.addBookedSlot(slotOn(DAY, 9, 0, 9, 30));

            var result = useCaseWith(calendar).findAvailableSlots(new FindAvailableSlotsQuery(DAY, DAY));

            assertThat(result).containsExactly(slotOn(DAY, 9, 30, 10, 0));
        }

        @Test
        @DisplayName("un rendez-vous qui chevauche partiellement deux créneaux les exclut tous les deux")
        void shouldExcludeBothSlotsWhenAppointmentSpansAcrossThem() {
            var calendar = new InMemoryOpeningHoursCalendar()
                    .withOpeningIntervals(DAY, NINE_TO_TEN);
            loadBookedAppointmentsPort.addBookedSlot(slotOn(DAY, 9, 15, 9, 45));

            var result = useCaseWith(calendar).findAvailableSlots(new FindAvailableSlotsQuery(DAY, DAY));

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Quand un rendez-vous est strictement adjacent à un créneau (pas de chevauchement réel)")
    class WhenAnAppointmentIsExactlyAdjacentToASlot {

        @Test
        @DisplayName("le créneau voisin reste disponible")
        void shouldNotExcludeTheAdjacentSlot() {
            var calendar = new InMemoryOpeningHoursCalendar()
                    .withOpeningIntervals(DAY, NINE_TO_TEN);
            loadBookedAppointmentsPort.addBookedSlot(slotOn(DAY, 9, 30, 10, 0));

            var result = useCaseWith(calendar).findAvailableSlots(new FindAvailableSlotsQuery(DAY, DAY));

            assertThat(result).containsExactly(slotOn(DAY, 9, 0, 9, 30));
        }
    }

    @Nested
    @DisplayName("Quand le calendrier n'a aucun intervalle pour une date")
    class WhenTheCalendarHasNoIntervalForADate {

        @Test
        @DisplayName("aucun créneau n'est généré ce jour-là")
        void shouldReturnNoSlotForThatDate() {
            var calendar = new InMemoryOpeningHoursCalendar();

            var result = useCaseWith(calendar).findAvailableSlots(new FindAvailableSlotsQuery(DAY, DAY));

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Quand la période demandée couvre plusieurs jours")
    class WhenPeriodSpansMultipleDays {

        @Test
        @DisplayName("les créneaux sont générés uniquement pour les jours ayant un intervalle d'ouverture")
        void shouldGenerateSlotsOnlyForConfiguredDaysAcrossThePeriod() {
            var otherDay = DAY.plusDays(2);
            var calendar = new InMemoryOpeningHoursCalendar()
                    .withOpeningIntervals(DAY, NINE_TO_TEN)
                    .withOpeningIntervals(otherDay, NINE_TO_TEN);

            var result = useCaseWith(calendar)
                    .findAvailableSlots(new FindAvailableSlotsQuery(DAY, otherDay));

            assertThat(result).containsExactlyInAnyOrder(
                    slotOn(DAY, 9, 0, 9, 30),
                    slotOn(DAY, 9, 30, 10, 0),
                    slotOn(otherDay, 9, 0, 9, 30),
                    slotOn(otherDay, 9, 30, 10, 0)
            );
        }
    }

    @Nested
    @DisplayName("Concernant la période transmise au port de lecture")
    class RegardingThePeriodRequestedFromThePort {

        @Test
        @DisplayName("le port est interrogé du début du jour 'from' jusqu'au début du jour suivant 'to'")
        void shouldRequestBookedSlotsOverTheFullDayBoundaries() {
            var to = DAY.plusDays(3);
            var calendar = new InMemoryOpeningHoursCalendar();

            useCaseWith(calendar).findAvailableSlots(new FindAvailableSlotsQuery(DAY, to));

            assertThat(loadBookedAppointmentsPort.lastRequestedFrom())
                    .isEqualTo(DAY.atStartOfDay(ZONE));
            assertThat(loadBookedAppointmentsPort.lastRequestedTo())
                    .isEqualTo(to.plusDays(1).atStartOfDay(ZONE));
        }

        @Test
        @DisplayName("les bornes de la requête respectent le fuseau horaire du calendrier")
        void shouldUseTheCalendarTimeZoneForRequestBoundaries() {
            var utc = ZoneId.of("UTC");
            var calendar = new InMemoryOpeningHoursCalendar().withZoneId(utc);

            useCaseWith(calendar).findAvailableSlots(new FindAvailableSlotsQuery(DAY, DAY));

            assertThat(loadBookedAppointmentsPort.lastRequestedFrom().getZone()).isEqualTo(utc);
            assertThat(loadBookedAppointmentsPort.lastRequestedTo().getZone()).isEqualTo(utc);
        }
    }

    @Nested
    @DisplayName("Quand le use case est construit avec des dépendances nulles")
    class WhenConstructedWithNullDependencies {

        @Test
        @DisplayName("le calendrier d'horaires d'ouverture ne peut pas être null")
        void shouldRejectNullCalendar() {
            assertThatThrownBy(() -> new FindAvailableSlotsUseCaseImpl(null, loadBookedAppointmentsPort))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("le port de lecture des rendez-vous ne peut pas être null")
        void shouldRejectNullPort() {
            assertThatThrownBy(() -> new FindAvailableSlotsUseCaseImpl(new InMemoryOpeningHoursCalendar(), null))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}