package ld.e2e.customer;

import io.restassured.response.Response;
import ld.bootstrap.request.RegisterCustomerRequest;
import ld.e2e.E2EBase;
import org.junit.jupiter.api.Test;

import static com.diffplug.selfie.Selfie.expectSelfie;
import static org.hamcrest.Matchers.equalTo;

class CustomerE2ETest extends E2EBase {

    private static final String EMAIL = "marie@example.com";
    private static final String PHONE = "+33612345678";

    @Test
    void customerRegistersHimself() {
        Response registered = api.customer()
                .body(new RegisterCustomerRequest(EMAIL, PHONE))
                .post("/customer/me")
                .then().statusCode(201)
                .extract().response();

        expectSelfie(snapshots.of(registered)).toBe("""
{
    "id": "<uuid-1>",
    "identitySubject": "<uuid-2>",
    "email": "marie@example.com",
    "phone": "+33612345678"
}""");
    }

    @Test
    void identityComesFromTheToken() {
        Response marie = api.customer()
                .body(new RegisterCustomerRequest(EMAIL, PHONE))
                .post("/customer/me")
                .then().statusCode(201)
                .extract().response();

        Response paul = api.otherCustomer()
                .body(new RegisterCustomerRequest("paul@example.com", "+33698765432"))
                .post("/customer/me")
                .then().statusCode(201)
                .extract().response();

        expectSelfie(snapshots.of(marie) + "\n" + snapshots.of(paul)).toBe("""
{
    "id": "<uuid-1>",
    "identitySubject": "<uuid-2>",
    "email": "marie@example.com",
    "phone": "+33612345678"
}
{
    "id": "<uuid-3>",
    "identitySubject": "<uuid-4>",
    "email": "paul@example.com",
    "phone": "+33698765432"
}""");
    }

    @Test
    void phoneWithoutPlusSignIsAccepted() {
        Response registered = api.customer()
                .body(new RegisterCustomerRequest(EMAIL, "0612345678"))
                .post("/customer/me")
                .then().statusCode(201)
                .extract().response();

        expectSelfie(snapshots.of(registered)).toBe("""
{
    "id": "<uuid-1>",
    "identitySubject": "<uuid-2>",
    "email": "marie@example.com",
    "phone": "0612345678"
}""");
    }

    @Test
    void missingEmailIsRefused() {
        Response invalid = api.customer()
                .body(new RegisterCustomerRequest(null, PHONE))
                .post("/customer/me")
                .then().statusCode(400)
                .body("code", equalTo("VALIDATION_FAILED"))
                .extract().response();

        expectSelfie(snapshots.of(invalid)).toBe("""
{
    "code": "VALIDATION_FAILED",
    "title": "Validation échouée",
    "status": 400,
    "detail": "Un ou plusieurs champs sont invalides.",
    "errors": [
        {
            "field": "email",
            "message": "L'email est obligatoire"
        }
    ]
}""");
    }

    @Test
    void malformedEmailIsRefused() {
        Response invalid = api.customer()
                .body(new RegisterCustomerRequest("pas-un-email", PHONE))
                .post("/customer/me")
                .then().statusCode(400)
                .body("code", equalTo("VALIDATION_FAILED"))
                .extract().response();

        expectSelfie(snapshots.of(invalid)).toBe("""
{
    "code": "VALIDATION_FAILED",
    "title": "Validation échouée",
    "status": 400,
    "detail": "Un ou plusieurs champs sont invalides.",
    "errors": [
        {
            "field": "email",
            "message": "Email invalide"
        }
    ]
}""");
    }

    @Test
    void missingPhoneIsRefused() {
        Response invalid = api.customer()
                .body(new RegisterCustomerRequest(EMAIL, null))
                .post("/customer/me")
                .then().statusCode(400)
                .body("code", equalTo("VALIDATION_FAILED"))
                .extract().response();

        expectSelfie(snapshots.of(invalid)).toBe("""
{
    "code": "VALIDATION_FAILED",
    "title": "Validation échouée",
    "status": 400,
    "detail": "Un ou plusieurs champs sont invalides.",
    "errors": [
        {
            "field": "phone",
            "message": "Le numéro de téléphone est obligatoire"
        }
    ]
}""");
    }

    @Test
    void phoneTooShortIsRefused() {
        Response invalid = api.customer()
                .body(new RegisterCustomerRequest(EMAIL, "12345"))
                .post("/customer/me")
                .then().statusCode(400)
                .body("code", equalTo("VALIDATION_FAILED"))
                .extract().response();

        expectSelfie(snapshots.of(invalid)).toBe("""
{
    "code": "VALIDATION_FAILED",
    "title": "Validation échouée",
    "status": 400,
    "detail": "Un ou plusieurs champs sont invalides.",
    "errors": [
        {
            "field": "phone",
            "message": "Numéro de téléphone invalide"
        }
    ]
}""");
    }

    @Test
    void phoneWithSpacesIsRefused() {
        Response invalid = api.customer()
                .body(new RegisterCustomerRequest(EMAIL, "+33 6 12 34 56 78"))
                .post("/customer/me")
                .then().statusCode(400)
                .body("code", equalTo("VALIDATION_FAILED"))
                .extract().response();

        expectSelfie(snapshots.of(invalid)).toBe("""
{
    "code": "VALIDATION_FAILED",
    "title": "Validation échouée",
    "status": 400,
    "detail": "Un ou plusieurs champs sont invalides.",
    "errors": [
        {
            "field": "phone",
            "message": "Numéro de téléphone invalide"
        }
    ]
}""");
    }


    @Test
    void registeringTwiceIsObserved() {
        customers.register();

        Response second = api.customer()
                .body(new RegisterCustomerRequest(EMAIL, PHONE))
                .post("/customer/me")
                .then().statusCode(201)
                .extract().response();

        expectSelfie( snapshots.of(second)).toBe("""
{
    "id": "<uuid-1>",
    "identitySubject": "<uuid-2>",
    "email": "marie@example.com",
    "phone": "+33612345678"
}""");
    }

    @Test
    void adminCannotRegisterAsACustomer() {
        api.admin()
                .body(new RegisterCustomerRequest(EMAIL, PHONE))
                .post("/customer/me")
                .then().statusCode(403)
                .body("code", equalTo("FORBIDDEN"));
    }

    @Test
    void anonymousCannotRegister() {
        api.anonymous()
                .body(new RegisterCustomerRequest(EMAIL, PHONE))
                .post("/customer/me")
                .then().statusCode(401)
                .body("code", equalTo("UNAUTHENTICATED"));
    }
}