package ld.e2e.order.read;

import io.restassured.response.Response;
import ld.e2e.E2EBase;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static com.diffplug.selfie.Selfie.expectSelfie;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

class ListOrdersE2ETest extends E2EBase {

    @Test
    void customerWithoutOrdersGetsAnEmptyPage() {
        customers.register();

        Response page = api.customer()
                .get("/orders")
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
    void customerListsHisOrders() {
        var productId = products.bonnet();
        customers.register();
        orders.place(api.customer(), productId);
        var acceptedId = orders.place(api.customer(), productId);
        orders.accept(acceptedId);

        Response page = api.customer()
                .queryParam("sort", "createdAt,asc")
                .get("/orders")
                .then().statusCode(200)
                .extract().response();

        expectSelfie(snapshots.of(page)).toBe("""
{
    "content": [
        {
            "orderId": "<uuid-1>",
            "customerId": "<uuid-2>",
            "orderReference": "<reference>",
            "customerMessage": "Merci de livrer le matin",
            "createdAt": "<date>",
            "updatedAt": "<date>",
            "total": 24.9,
            "status": "PENDING",
            "lines": [
                {
                    "productId": "<uuid-3>",
                    "productName": "Bonnet en laine",
                    "productCategory": "CLOTHING",
                    "quantity": 1,
                    "unitPrice": 24.9,
                    "chosenColor": "ROUGE",
                    "subtotalBeforeDiscount": 24.9,
                    "discountRate": 0.0,
                    "discountAmount": 0.0,
                    "totalAmount": 24.9
                }
            ],
            "lineCount": 1,
            "totalQuantity": 1
        },
        {
            "orderId": "<uuid-4>",
            "customerId": "<uuid-2>",
            "orderReference": "<reference>",
            "customerMessage": "Merci de livrer le matin",
            "createdAt": "<date>",
            "updatedAt": "<date>",
            "total": 24.9,
            "status": "ACCEPTED",
            "lines": [
                {
                    "productId": "<uuid-3>",
                    "productName": "Bonnet en laine",
                    "productCategory": "CLOTHING",
                    "quantity": 1,
                    "unitPrice": 24.9,
                    "chosenColor": "ROUGE",
                    "subtotalBeforeDiscount": 24.9,
                    "discountRate": 0.0,
                    "discountAmount": 0.0,
                    "totalAmount": 24.9
                }
            ],
            "lineCount": 1,
            "totalQuantity": 1
        }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 2,
    "totalPages": 1
}""");
    }


    @Test
    void customerOnlySeesHisOwnOrders() {
        UUID productId = products.bonnet();
        customers.register();
        customers.registerOther();
        orders.place(api.customer(), productId);
        orders.place(api.otherCustomer(), productId);
        orders.place(api.otherCustomer(), productId);

        api.customer().get("/orders")
                .then().statusCode(200)
                .body("totalElements", equalTo(1))
                .body("content", hasSize(1));

        api.otherCustomer().get("/orders")
                .then().statusCode(200)
                .body("totalElements", equalTo(2))
                .body("content", hasSize(2));
    }

    @Test
    void ordersArePaginated() {
        UUID productId = products.bonnet();
        customers.register();
        orders.place(api.customer(), productId);
        orders.place(api.customer(), productId);
        orders.place(api.customer(), productId);

        Response secondPage = api.customer()
                .queryParam("page", 1).queryParam("size", 2)
                .get("/orders")
                .then().statusCode(200)
                .extract().response();

        expectSelfie(snapshots.of(secondPage)).toBe("""
{
    "content": [
        {
            "orderId": "<uuid-1>",
            "customerId": "<uuid-2>",
            "orderReference": "<reference>",
            "customerMessage": "Merci de livrer le matin",
            "createdAt": "<date>",
            "updatedAt": "<date>",
            "total": 24.9,
            "status": "PENDING",
            "lines": [
                {
                    "productId": "<uuid-3>",
                    "productName": "Bonnet en laine",
                    "productCategory": "CLOTHING",
                    "quantity": 1,
                    "unitPrice": 24.9,
                    "chosenColor": "ROUGE",
                    "subtotalBeforeDiscount": 24.9,
                    "discountRate": 0.0,
                    "discountAmount": 0.0,
                    "totalAmount": 24.9
                }
            ],
            "lineCount": 1,
            "totalQuantity": 1
        }
    ],
    "page": 1,
    "size": 2,
    "totalElements": 3,
    "totalPages": 2
}""");
    }

    @Test
    void pageBeyondTheLastOneIsEmpty() {
        orders.pending();

        Response page = api.customer()
                .queryParam("page", 5)
                .get("/orders")
                .then().statusCode(200)
                .extract().response();

        expectSelfie(snapshots.of(page)).toBe("""
{
    "content": [
       \s
    ],
    "page": 5,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1
}""");
    }

    @Test
    void invalidSortFieldIsRefused() {
        customers.register();

        api.customer()
                .queryParam("sort", "doesNotExist,asc")
                .get("/orders")
                .then().statusCode(400)
                .body("code", equalTo("INVALID_SORT_FIELD"))
                .extract().response();
    }


    @Test
    void adminCannotUseTheCustomerEndpoint() {
        api.admin().get("/orders")
                .then().statusCode(403)
                .body("code", equalTo("FORBIDDEN"));
    }

    @Test
    void anonymousCannotListOrders() {
        api.anonymous().get("/orders")
                .then().statusCode(401)
                .body("code", equalTo("UNAUTHENTICATED"));
    }
}