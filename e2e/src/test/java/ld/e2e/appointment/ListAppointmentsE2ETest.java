package ld.e2e.appointment;

import io.restassured.response.Response;
import ld.e2e.E2EBase;
import ld.e2e.support.AppointmentFixtures.Slot;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.diffplug.selfie.Selfie.expectSelfie;
import static ld.e2e.support.AppointmentFixtures.DAY;
import static org.hamcrest.Matchers.equalTo;

class ListAppointmentsE2ETest extends E2EBase {

    @Test
    void customerWithoutAppointmentsGetsAnEmptyList() {
        customers.register();

        Response list = api.customer()
                .get("/appointments")
                .then().statusCode(200)
                .extract().response();

        expectSelfie(snapshots.withSlotTimes(list)).toBe("""
[
   \s
]""");
    }

    @Test
    void customerListsHisAppointment() {
        customers.register();
        Slot slot = appointments.freeSlots(DAY).getFirst();
        appointments.book(api.customer(), slot);

        Response list = api.customer()
                .get("/appointments")
                .then().statusCode(200)
                .extract().response();

        expectSelfie(snapshots.withSlotTimes(list)).toBe("""
[
    {
        "id": "<uuid-1>",
        "startAt": "2030-03-18T09:00:00+01:00",
        "endAt": "2030-03-18T09:30:00+01:00",
        "status": "SUBMITTED",
        "cancelledAt": null,
        "cancelledReason": null
    }
]""");
    }

    @Test
    void customerOnlySeesHisOwnAppointments() {
        customers.register();
        customers.registerOther();
        List<Slot> free = appointments.freeSlots(DAY);
        appointments.book(api.customer(), free.get(0));
        appointments.book(api.otherCustomer(), free.get(2));

        api.customer().get("/appointments")
                .then().statusCode(200)
                .body("size()", equalTo(1));

        api.otherCustomer().get("/appointments")
                .then().statusCode(200)
                .body("size()", equalTo(1));
    }

    @Test
    void adminCannotUseTheCustomerEndpoint() {
        api.admin().get("/appointments")
                .then().statusCode(403)
                .body("code", equalTo("FORBIDDEN"));
    }

    @Test
    void anonymousCannotListAppointments() {
        api.anonymous().get("/appointments")
                .then().statusCode(401)
                .body("code", equalTo("UNAUTHENTICATED"));
    }
}