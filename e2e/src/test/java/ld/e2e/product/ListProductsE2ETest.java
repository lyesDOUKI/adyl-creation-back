package ld.e2e.product;

import io.restassured.response.Response;
import ld.e2e.E2EBase;
import org.junit.jupiter.api.Test;

import static com.diffplug.selfie.Selfie.expectSelfie;
import static org.hamcrest.Matchers.equalTo;

class ListProductsE2ETest extends E2EBase {

    @Test
    void emptyCatalogIsAnEmptyPage() {
        Response page = api.anonymous()
                .get("/products")
                .then().statusCode(200)
                .extract().response();

        expectSelfie(snapshots.of(page)).toBe("""
{
    "content": [
       \s
    ],
    "page": 0,
    "size": 20,
    "totalElements": 0,
    "totalPages": 0
}""");
    }

    @Test
    void anyoneCanListTheCatalog() {
        products.bonnet();

        // Sans token : le catalogue est public
        Response page = api.anonymous()
                .get("/products")
                .then().statusCode(200)
                .extract().response();

        expectSelfie(snapshots.of(page)).toBe("""
{
    "content": [
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
        }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1
}""");
    }

    @Test
    void catalogIsSortedByName() {
        products.named("Snood");
        products.named("Bonnet");

        Response page = api.anonymous()
                .queryParam("sort", "name,asc")
                .get("/products")
                .then().statusCode(200)
                .extract().response();

        expectSelfie(snapshots.of(page)).toBe("""
{
    "content": [
        {
            "productId": "<uuid-1>",
            "productCategory": "CLOTHING",
            "name": "Bonnet",
            "price": 24.9,
            "colors": [
                "BLEU",
                "ROUGE"
            ],
            "photosUri": [
               \s
            ],
            "numberOfOrders": 0
        },
        {
            "productId": "<uuid-2>",
            "productCategory": "CLOTHING",
            "name": "Snood",
            "price": 24.9,
            "colors": [
                "BLEU",
                "ROUGE"
            ],
            "photosUri": [
               \s
            ],
            "numberOfOrders": 0
        }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 2,
    "totalPages": 1
}""");
    }

    @Test
    void catalogIsPaginated() {
        products.named("Bonnet");
        products.named("Echarpe");
        products.named("Snood");

        Response page = api.anonymous()
                .queryParam("page", 1)
                .queryParam("size", 2)
                .queryParam("sort", "name,asc")
                .get("/products")
                .then().statusCode(200)
                .extract().response();

        expectSelfie(snapshots.of(page)).toBe("""
{
    "content": [
        {
            "productId": "<uuid-1>",
            "productCategory": "CLOTHING",
            "name": "Snood",
            "price": 24.9,
            "colors": [
                "BLEU",
                "ROUGE"
            ],
            "photosUri": [
               \s
            ],
            "numberOfOrders": 0
        }
    ],
    "page": 1,
    "size": 2,
    "totalElements": 3,
    "totalPages": 2
}""");
    }

    @Test
    void invalidSortFieldIsRefused() {
        Response bad = api.anonymous()
                .queryParam("sort", "doesNotExist,asc")
                .get("/products")
                .then().statusCode(400)
                .body("code", equalTo("INVALID_SORT_FIELD"))
                .extract().response();

        expectSelfie(snapshots.of(bad)).toBe("""
{
    "code": "INVALID_SORT_FIELD",
    "title": "Champ de tri invalide",
    "status": 400,
    "detail": "Tri non supporté sur le champ: doesNotExist",
    "reason": "Tri non supporté sur le champ: doesNotExist"
}""");
    }
}