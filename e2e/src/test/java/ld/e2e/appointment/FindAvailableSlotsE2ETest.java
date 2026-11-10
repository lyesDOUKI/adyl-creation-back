package ld.e2e.appointment;

import io.restassured.response.Response;
import ld.e2e.E2EBase;
import ld.e2e.support.AppointmentFixtures.Slot;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.diffplug.selfie.Selfie.expectSelfie;
import static ld.e2e.support.AppointmentFixtures.DAY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

class FindAvailableSlotsE2ETest extends E2EBase {

    @Test
    void customerSeesTheSlotsOfAnOpenDay() {
        customers.register();

        Response slots = api.customer()
                .queryParam("from", DAY).queryParam("to", DAY)
                .get("/appointments/available-slots")
                .then().statusCode(200)
                .extract().response();

        expectSelfie(snapshots.withSlotTimes(slots)).toBe("""
{
    "availability": [
        {
            "date": "2030-03-18",
            "slots": [
                {
                    "startAt": "2030-03-18T09:00:00+01:00",
                    "endAt": "2030-03-18T09:30:00+01:00"
                },
                {
                    "startAt": "2030-03-18T09:30:00+01:00",
                    "endAt": "2030-03-18T10:00:00+01:00"
                },
                {
                    "startAt": "2030-03-18T10:00:00+01:00",
                    "endAt": "2030-03-18T10:30:00+01:00"
                },
                {
                    "startAt": "2030-03-18T10:30:00+01:00",
                    "endAt": "2030-03-18T11:00:00+01:00"
                },
                {
                    "startAt": "2030-03-18T11:00:00+01:00",
                    "endAt": "2030-03-18T11:30:00+01:00"
                },
                {
                    "startAt": "2030-03-18T11:30:00+01:00",
                    "endAt": "2030-03-18T12:00:00+01:00"
                },
                {
                    "startAt": "2030-03-18T14:00:00+01:00",
                    "endAt": "2030-03-18T14:30:00+01:00"
                },
                {
                    "startAt": "2030-03-18T14:30:00+01:00",
                    "endAt": "2030-03-18T15:00:00+01:00"
                },
                {
                    "startAt": "2030-03-18T15:00:00+01:00",
                    "endAt": "2030-03-18T15:30:00+01:00"
                },
                {
                    "startAt": "2030-03-18T15:30:00+01:00",
                    "endAt": "2030-03-18T16:00:00+01:00"
                },
                {
                    "startAt": "2030-03-18T16:00:00+01:00",
                    "endAt": "2030-03-18T16:30:00+01:00"
                },
                {
                    "startAt": "2030-03-18T16:30:00+01:00",
                    "endAt": "2030-03-18T17:00:00+01:00"
                },
                {
                    "startAt": "2030-03-18T17:00:00+01:00",
                    "endAt": "2030-03-18T17:30:00+01:00"
                },
                {
                    "startAt": "2030-03-18T17:30:00+01:00",
                    "endAt": "2030-03-18T18:00:00+01:00"
                }
            ]
        }
    ]
}""");
    }

    @Test
    void everyDateOfThePeriodIsPresentEvenWithoutSlots() {
        customers.register();

        Response slots = api.customer()
                .queryParam("from", DAY.plusDays(5)).queryParam("to", DAY.plusDays(6))
                .get("/appointments/available-slots")
                .then().statusCode(200)
                .extract().response();

        expectSelfie(snapshots.withSlotTimes(slots)).toBe("""
{
    "availability": [
        {
            "date": "2030-03-23",
            "slots": [
               \s
            ]
        },
        {
            "date": "2030-03-24",
            "slots": [
               \s
            ]
        }
    ]
}""");
    }

    @Test
    void aBookedSlotIsNoLongerProposed() {
        customers.register();
        List<Slot> before = appointments.freeSlots(DAY);
        Slot booked = before.getFirst();
        appointments.book(api.customer(), booked);

        assertThat(appointments.freeSlots(DAY)).doesNotContain(booked);
    }

    @Test
    void missingBoundIsRefused() {
        customers.register();

        Response bad = api.customer()
                .queryParam("from", DAY)
                .get("/appointments/available-slots")
                .then().statusCode(400)
                .extract().response();

        expectSelfie(snapshots.of(bad)).toBe("""
{
    "detail": "Required parameter 'to' is not present.",
    "instance": "/api/appointments/available-slots",
    "status": 400,
    "title": "Bad Request"
}""");
    }

    @Test
    void malformedDateIsRefused() {
        customers.register();

        Response bad = api.customer()
                .queryParam("from", "15/03/2030").queryParam("to", DAY)
                .get("/appointments/available-slots")
                .then().statusCode(400)
                .body("code", equalTo("INVALID_PARAMETER"))
                .extract().response();

        expectSelfie(snapshots.of(bad)).toBe("""
{
    "code": "INVALID_PARAMETER",
    "title": "Paramètre invalide",
    "status": 400,
    "detail": "Le paramètre 'from' est invalide : valeur incompatible avec le type attendu.",
    "parameter": "from",
    "reason": "valeur incompatible avec le type attendu."
}""");
    }

    @Test
    void adminCannotUseTheCustomerEndpoint() {
        api.admin()
                .queryParam("from", DAY).queryParam("to", DAY)
                .get("/appointments/available-slots")
                .then().statusCode(403)
                .body("code", equalTo("FORBIDDEN"));
    }

    @Test
    void anonymousCannotSearchSlots() {
        api.anonymous()
                .queryParam("from", DAY).queryParam("to", DAY)
                .get("/appointments/available-slots")
                .then().statusCode(401)
                .body("code", equalTo("UNAUTHENTICATED"));
    }
}