package ld.e2e.appointment;

import io.restassured.response.Response;
import ld.bootstrap.request.SubmitAppointmentRequest;
import ld.e2e.E2EBase;
import ld.e2e.support.AppointmentFixtures.Slot;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static com.diffplug.selfie.Selfie.expectSelfie;
import static ld.e2e.support.AppointmentFixtures.DAY;
import static org.hamcrest.Matchers.equalTo;

class CreateAppointmentE2ETest extends E2EBase {

    @Test
    void customerBooksAFreeSlot() {
        customers.register();
        Slot slot = appointments.freeSlots(DAY).getFirst();

        Response created = api.customer()
                .body(new SubmitAppointmentRequest(slot.startAt(), slot.endAt(), "Première visite"))
                .post("/appointments")
                .then().statusCode(201)
                .extract().response();

        expectSelfie(snapshots.withSlotTimes(created)).toBe("""
{
    "appointmentId": "<uuid-1>",
    "startAt": "2030-03-18T09:00:00+01:00",
    "endAt": "2030-03-18T09:30:00+01:00",
    "status": "SUBMITTED",
    "submittedAt": "<date>"
}""");
    }

    @Test
    void notesAreOptional() {
        customers.register();
        Slot slot = appointments.freeSlots(DAY).getFirst();

        Response created = api.customer()
                .body(new SubmitAppointmentRequest(slot.startAt(), slot.endAt(), null))
                .post("/appointments")
                .then().statusCode(201)
                .extract().response();

        expectSelfie(snapshots.withSlotTimes(created)).toBe("""
{
    "appointmentId": "<uuid-1>",
    "startAt": "2030-03-18T09:00:00+01:00",
    "endAt": "2030-03-18T09:30:00+01:00",
    "status": "SUBMITTED",
    "submittedAt": "<date>"
}""");
    }

    @Test
    void slotAlreadyBookedIsRefused() {
        customers.register();
        customers.registerOther();
        Slot slot = appointments.freeSlots(DAY).getFirst();
        appointments.book(api.customer(), slot);

        Response conflict = api.otherCustomer()
                .body(new SubmitAppointmentRequest(slot.startAt(), slot.endAt(), null))
                .post("/appointments")
                .then().statusCode(409)
                .body("code", equalTo("SLOT_ALREADY_BOOKED"))
                .extract().response();

        expectSelfie(snapshots.withSlotTimes(conflict)).toBe("""
{
    "code": "SLOT_ALREADY_BOOKED",
    "title": "Créneau déjà réservé",
    "status": 409,
    "detail": "Ce créneau est déjà réservé.",
    "requestedSlot": {
        "startAt": "2030-03-18T09:00:00+01:00",
        "endAt": "2030-03-18T09:30:00+01:00"
    }
}""");
    }


    @Test
    void slotOutsideOpeningHoursIsRefused() {
        customers.register();
        Slot open = appointments.freeSlots(DAY).getFirst();

        ZonedDateTime night = open.startAt().withHour(3).withMinute(0);

        Response invalid = api.customer()
                .body(new SubmitAppointmentRequest(night, night.plusMinutes(30), null))
                .post("/appointments")
                .then().statusCode(422)
                .extract().response();

        expectSelfie(snapshots.withSlotTimes(invalid)).toBe("""
{
    "code": "SLOT_OUTSIDE_OPENING_HOURS",
    "title": "Créneau hors horaires",
    "status": 422,
    "detail": "Le créneau demandé est hors des horaires autorisés.",
    "requestedSlot": {
        "startAt": "2030-03-18T03:00:00+01:00",
        "endAt": "2030-03-18T03:30:00+01:00"
    }
}""");
    }


    @Test
    void missingDatesIsRefused() {
        customers.register();
        Slot slot = appointments.freeSlots(DAY).getFirst();

        Response invalidStartDate = api.customer()
                .body(new SubmitAppointmentRequest(null, slot.endAt(), null))
                .post("/appointments")
                .then().statusCode(400)
                .body("code", equalTo("VALIDATION_FAILED"))
                .extract().response();

        expectSelfie(snapshots.of(invalidStartDate)).toBe("""
{
    "code": "VALIDATION_FAILED",
    "title": "Validation échouée",
    "status": 400,
    "detail": "Un ou plusieurs champs sont invalides.",
    "errors": [
        {
            "field": "startAt",
            "message": "La date de début est obligatoire"
        }
    ]
}""");

        Response invalidEndDate = api.customer()
                .body(new SubmitAppointmentRequest(slot.startAt(), null, null))
                .post("/appointments")
                .then().statusCode(400)
                .body("code", equalTo("VALIDATION_FAILED"))
                .extract().response();

        expectSelfie(snapshots.of(invalidEndDate)).toBe("""
{
    "code": "VALIDATION_FAILED",
    "title": "Validation échouée",
    "status": 400,
    "detail": "Un ou plusieurs champs sont invalides.",
    "errors": [
        {
            "field": "endAt",
            "message": "La date de fin est obligatoire"
        }
    ]
}""");
    }

    @Test
    void adminCannotBookAnAppointment() {
        customers.register();
        Slot slot = appointments.freeSlots(DAY).getFirst();

        api.admin()
                .body(new SubmitAppointmentRequest(slot.startAt(), slot.endAt(), null))
                .post("/appointments")
                .then().statusCode(403)
                .body("code", equalTo("FORBIDDEN"));
    }

    @Test
    void anonymousCannotBookAnAppointment() {
        customers.register();
        Slot slot = appointments.freeSlots(DAY).getFirst();

        api.anonymous()
                .body(new SubmitAppointmentRequest(slot.startAt(), slot.endAt(), null))
                .post("/appointments")
                .then().statusCode(401)
                .body("code", equalTo("UNAUTHENTICATED"));
    }
}