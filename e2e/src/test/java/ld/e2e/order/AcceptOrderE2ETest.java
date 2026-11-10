package ld.e2e.order;

import io.restassured.response.Response;
import ld.e2e.E2EBase;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static com.diffplug.selfie.Selfie.expectSelfie;
import static org.hamcrest.Matchers.equalTo;

class AcceptOrderE2ETest extends E2EBase {

    @Test
    void adminAcceptsAPendingOrder() {
        UUID orderId = orders.pending();

        Response accepted = api.admin()
                .post("/orders/{id}/accept", orderId)
                .then().statusCode(200)
                .extract().response();

        expectSelfie(snapshots.of(accepted)).toBe("""
{
    "orderId": "<uuid-1>",
    "discountRate": 0,
    "status": "ACCEPTED",
    "acceptedAt": "<date>"
}""");

        api.customer().get("/orders/{id}", orderId)
                .then().statusCode(200)
                .body("status", equalTo("ACCEPTED"));
    }

    @Test
    void unknownOrderIsNotFound() {
        Response notFound = api.admin()
                .post("/orders/{id}/accept", UUID.randomUUID())
                .then().statusCode(404)
                .body("code", equalTo("ORDER_NOT_FOUND"))
                .extract().response();

        expectSelfie(snapshots.of(notFound)).toBe("""
{
    "code": "ORDER_NOT_FOUND",
    "title": "Commande introuvable",
    "status": 404,
    "detail": "La commande est introuvable.",
    "orderId": "<uuid-1>"
}""");
    }

    @Test
    void rejectedOrderCannotBeAccepted() {
        UUID orderId = orders.rejected();

        Response conflict = api.admin()
                .post("/orders/{id}/accept", orderId)
                .then().statusCode(409)
                .body("code", equalTo("ORDER_ALREADY_REJECTED"))
                .extract().response();

        expectSelfie(snapshots.of(conflict)).toBe("""
{
    "code": "ORDER_ALREADY_REJECTED",
    "title": "Commande rejetée",
    "status": 409,
    "detail": "La commande a été rejetée, elle ne peut plus être acceptée."
}""");
    }

    @Test
    void deliveredOrderCannotBeAccepted() {
        UUID orderId = orders.delivered();

        Response conflict = api.admin()
                .post("/orders/{id}/accept", orderId)
                .then().statusCode(409)
                .body("code", equalTo("ORDER_ALREADY_DELIVERED"))
                .extract().response();

        expectSelfie(snapshots.of(conflict)).toBe("""
{
    "code": "ORDER_ALREADY_DELIVERED",
    "title": "Commande livrée",
    "status": 409,
    "detail": "La commande a déjà été livrée, elle ne peut plus être acceptée."
}""");
    }

    @Test
    void invalidOrderIdIsRefused() {
        Response bad = api.admin()
                .post("/orders/{id}/accept", "not-a-uuid")
                .then().statusCode(400)
                .body("code", equalTo("INVALID_PARAMETER"))
                .extract().response();

        expectSelfie(snapshots.of(bad)).toBe("""
{
    "code": "INVALID_PARAMETER",
    "title": "Paramètre invalide",
    "status": 400,
    "detail": "Le paramètre 'id' est invalide : valeur incompatible avec le type attendu.",
    "parameter": "id",
    "reason": "valeur incompatible avec le type attendu."
}""");
    }

    @Test
    void customerCannotAcceptAnOrder() {
        UUID orderId = orders.pending();

        api.customer().post("/orders/{id}/accept", orderId)
                .then().statusCode(403)
                .body("code", equalTo("FORBIDDEN"));
    }

    @Test
    void anonymousCannotAcceptAnOrder() {
        api.anonymous().post("/orders/{id}/accept", UUID.randomUUID())
                .then().statusCode(401)
                .body("code", equalTo("UNAUTHENTICATED"));
    }
}