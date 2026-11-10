package ld.e2e.product;

import io.restassured.response.Response;
import ld.e2e.E2EBase;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static com.diffplug.selfie.Selfie.expectSelfie;
import static ld.bootstrap.request.ProductCategoryRequest.ACCESSORIES;
import static org.hamcrest.Matchers.equalTo;

class GetProductE2ETest extends E2EBase {

    @Test
    void anyoneCanReadAProduct() {
        UUID productId = products.bonnet();

        Response product = api.anonymous()
                .get("/products/{id}", productId)
                .then().statusCode(200)
                .extract().response();

        expectSelfie(snapshots.of(product)).toBe("""
{
    "productId": "<uuid-1>",
    "productCategory": "CLOTHING",
    "name": "Bonnet en laine",
    "price": 24.9,
    "colors": [
        "BLEU",
        "ROUGE"
    ],
    "photosUri": [
       \s
    ],
    "numberOfOrders": 0
}""");
    }

    @Test
    void anAccessoryIsExposedWithItsCategory() {
        UUID productId = products.create("Snood", ACCESSORIES, List.of("gris"), new BigDecimal("12.50"));

        Response product = api.anonymous()
                .get("/products/{id}", productId)
                .then().statusCode(200)
                .extract().response();

        expectSelfie(snapshots.of(product)).toBe("""
{
    "productId": "<uuid-1>",
    "productCategory": "ACCESSORIES",
    "name": "Snood",
    "price": 12.5,
    "colors": [
        "GRIS"
    ],
    "photosUri": [
       \s
    ],
    "numberOfOrders": 0
}""");
    }

    @Test
    void productCountsTheOrdersContainingIt() {
        UUID productId = products.bonnet();
        customers.register();
        orders.place(api.customer(), productId);

        Response product = api.anonymous()
                .get("/products/{id}", productId)
                .then().statusCode(200)
                .extract().response();

        expectSelfie(snapshots.of(product)).toBe("""
{
    "productId": "<uuid-1>",
    "productCategory": "CLOTHING",
    "name": "Bonnet en laine",
    "price": 24.9,
    "colors": [
        "BLEU",
        "ROUGE"
    ],
    "photosUri": [
       \s
    ],
    "numberOfOrders": 1
}""");
    }

    @Test
    void unknownProductIsNotFound() {
        Response notFound = api.anonymous()
                .get("/products/{id}", UUID.randomUUID())
                .then().statusCode(404)
                .body("code", equalTo("PRODUCT_NOT_FOUND"))
                .extract().response();

        expectSelfie(snapshots.of(notFound)).toBe("""
{
    "code": "PRODUCT_NOT_FOUND",
    "title": "Produit introuvable",
    "status": 404,
    "detail": "Le produit est introuvable.",
    "productId": "<uuid-1>"
}""");
    }

    @Test
    void invalidProductIdIsRefused() {
        Response bad = api.anonymous()
                .get("/products/{id}", "not-a-uuid")
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
}