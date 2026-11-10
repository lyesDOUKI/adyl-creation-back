package ld.e2e.support;

import io.restassured.specification.RequestSpecification;
import ld.bootstrap.request.SubmitAppointmentRequest;

import java.time.*;
import java.util.List;
import java.util.stream.IntStream;

public final class AppointmentFixtures {

    public static final ZoneId ZONE = ZoneId.of("Europe/Paris");


    public static final LocalDate DAY = LocalDate.of(2030, 3, 18);

    public record Slot(ZonedDateTime startAt, ZonedDateTime endAt) {
        public Slot shiftedBy(Duration duration) {
            return new Slot(startAt.plus(duration), endAt.plus(duration));
        }
    }

    private final Api api;

    public AppointmentFixtures(Api api) {
        this.api = api;
    }

    public List<Slot> freeSlots(LocalDate day) {
        var response = api.customer()
                .queryParam("from", day).queryParam("to", day)
                .get("/appointments/available-slots")
                .then().statusCode(200)
                .extract().response();

        List<String> starts = response.path("availability[0].slots.startAt");
        List<String> ends = response.path("availability[0].slots.endAt");

        if (starts == null || starts.isEmpty()) {
            throw new IllegalStateException(
                    "Aucun créneau libre le " + day + " : choisis un jour ouvré dans AppointmentFixtures.DAY");
        }
        return IntStream.range(0, starts.size())
                .mapToObj(i -> new Slot(inZone(starts.get(i)), inZone(ends.get(i))))
                .toList();
    }

    public void book(RequestSpecification customer, Slot slot) {
        customer
                .body(new SubmitAppointmentRequest(slot.startAt(), slot.endAt(), "Première visite"))
                .post("/appointments")
                .then().statusCode(201)
                .extract().path("appointmentId");
    }

    private static ZonedDateTime inZone(String iso) {
        return OffsetDateTime.parse(iso).atZoneSameInstant(ZONE);
    }
}